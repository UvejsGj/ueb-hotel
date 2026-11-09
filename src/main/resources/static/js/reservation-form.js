// Faza 2: kur zgjidhen datat, <select>-i i dhomave mbushet vetëm me dhomat e lira
// (GET /api/rooms/available) dhe tregohet çmimi total (net × çmimi për natë).
(function () {
    const form = document.querySelector('form[data-available-url]');
    if (!form) return;

    const checkIn = form.querySelector('#checkIn');
    const checkOut = form.querySelector('#checkOut');
    const roomSelect = form.querySelector('#roomId');
    const hint = form.querySelector('#room-hint');
    const total = form.querySelector('#total');
    const url = form.dataset.availableUrl;
    const exclude = form.dataset.reservationId;

    // Çmimet e dhomave nga lista fillestare (server-side), që totali të punojë edhe pa fetch.
    const prices = new Map();
    for (const opt of roomSelect.options) {
        if (opt.value) prices.set(opt.value, Number(opt.dataset.price));
    }

    let requestId = 0;
    let freeText = '';

    function nights() {
        if (!checkIn.value || !checkOut.value) return 0;
        const ms = new Date(checkOut.value) - new Date(checkIn.value);
        return ms > 0 ? Math.round(ms / 86400000) : 0;
    }

    function formatEuro(value) {
        return value.toLocaleString('sq-AL', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';
    }

    function updateTotal() {
        const n = nights();
        const price = prices.get(roomSelect.value);
        if (n > 0 && price) {
            total.textContent = `${n} ${n === 1 ? 'natë' : 'netë'} × ${formatEuro(price)} = ${formatEuro(n * price)}`;
            total.hidden = false;
        } else {
            total.hidden = true;
        }
    }

    function setHint(text, isError) {
        hint.textContent = text || '';
        hint.hidden = !text;
        hint.classList.toggle('field-error', !!isError);
        hint.classList.toggle('muted', !isError);
    }

    async function loadAvailable() {
        updateTotal();
        if (nights() === 0) {
            setHint(checkIn.value && checkOut.value ? 'Data e largimit duhet të jetë pas datës së ardhjes.' : '', true);
            return;
        }
        const params = new URLSearchParams({ from: checkIn.value, to: checkOut.value });
        if (exclude) params.set('exclude', exclude);

        const current = ++requestId;
        let rooms;
        try {
            const res = await fetch(`${url}?${params}`, { headers: { Accept: 'application/json' } });
            if (!res.ok) throw new Error(res.status);
            rooms = await res.json();
        } catch (e) {
            setHint('Lista e dhomave të lira nuk u ngarkua; serveri do ta kontrollojë kur ta ruani.', true);
            return;
        }
        if (current !== requestId) return; // një kërkesë më e re ka ardhur ndërkohë

        const selected = roomSelect.value;
        roomSelect.length = 1; // lë vetëm "— zgjidhni —"
        for (const r of rooms) {
            prices.set(String(r.id), Number(r.pricePerNight));
            const opt = new Option(
                `${r.number} · ${r.typeLabel} · ${r.capacity} persona · ${formatEuro(Number(r.pricePerNight))}`,
                r.id);
            opt.dataset.price = r.pricePerNight;
            roomSelect.add(opt);
        }
        const stillFree = rooms.some(r => String(r.id) === selected);
        roomSelect.value = stillFree ? selected : '';

        freeText = rooms.length === 0 ? ''
            : `${rooms.length} ${rooms.length === 1 ? 'dhomë e lirë' : 'dhoma të lira'} në këto data.`;
        if (rooms.length === 0) {
            setHint('Asnjë dhomë nuk është e lirë në këto data.', true);
        } else if (selected && !stillFree) {
            setHint('Dhoma e zgjedhur është e zënë në këto data; zgjidhni një tjetër.', true);
        } else {
            setHint(freeText, false);
        }
        updateTotal();
    }

    checkIn.addEventListener('change', loadAvailable);
    checkOut.addEventListener('change', loadAvailable);
    roomSelect.addEventListener('change', () => {
        // paralajmërimi "dhoma e zgjedhur është e zënë" s'vlen më pasi zgjidhet tjetër
        if (roomSelect.value && freeText) setHint(freeText, false);
        updateTotal();
    });

    if (checkIn.value && checkOut.value) loadAvailable();
})();
