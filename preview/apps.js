/* =============================================================================
   Gutter & Rule — interactive prototypes.
   These are browser mirrors of the Android screens in Antonio_Task1…5.
   The maths and the state machines are deliberately the same as the Java,
   so this page doubles as a way to demo the UI before an APK is built.
   ============================================================================= */
(function () {
  'use strict';

  /* ---------------------------------------------------------------- helpers */

  function h(html) {
    var t = document.createElement('template');
    t.innerHTML = html.trim();
    return t.content.firstElementChild;
  }

  function pad2(n) { return n < 10 ? '0' + n : String(n); }

  function groupInt(n) {
    var neg = n.startsWith('-');
    var body = neg ? n.slice(1) : n;
    var dot = body.indexOf('.');
    var int = dot < 0 ? body : body.slice(0, dot);
    var frac = dot < 0 ? '' : body.slice(dot);
    if (int.length > 3) int = int.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return (neg ? '-' : '') + int + frac;
  }

  /* Instrument formatting: 6dp max, grouping, scientific at the extremes. */
  function fmt(v) {
    if (!isFinite(v)) return '—';
    var abs = Math.abs(v);
    if (abs < 5e-7) return '0';
    if (abs >= 1e12 || abs < 1e-6) {
      return v.toExponential(6).toUpperCase().replace('E+', 'E+').replace('E-', 'E-');
    }
    var fixed = v.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    return groupInt(fixed);
  }

  function toast(screen, message) {
    var old = screen.querySelector('.toast');
    if (old) old.remove();
    var node = h('<div class="toast">' + message + '</div>');
    screen.appendChild(node);
    requestAnimationFrame(function () { node.classList.add('on'); });
    clearTimeout(node._t);
    node._t = setTimeout(function () {
      node.classList.remove('on');
      setTimeout(function () { node.remove(); }, 200);
    }, 2400);
  }

  /* Bottom sheet used for "new entry" and "confirm delete". */
  function openSheet(screen, innerHtml, onMount) {
    var mask = h('<div class="sheet-mask"><div class="sheet">' + innerHtml + '</div></div>');
    screen.appendChild(mask);
    requestAnimationFrame(function () { mask.classList.add('on'); });
    function close() {
      mask.classList.remove('on');
      setTimeout(function () { mask.remove(); }, 140);
    }
    mask.addEventListener('click', function (e) {
      if (e.target === mask) close();
    });
    onMount(mask.querySelector('.sheet'), close);
    return close;
  }

  function stamp(ms) {
    var d = new Date(ms);
    var months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
    return pad2(d.getDate()) + ' ' + months[d.getMonth()] + ' · ' + pad2(d.getHours()) + ':' + pad2(d.getMinutes());
  }

  function fitReadout(node, text, max, min) {
    var size = max;
    node.style.fontSize = size + 'px';
    while (size > min && node.scrollWidth > node.clientWidth) {
      size -= 2;
      node.style.fontSize = size + 'px';
    }
  }

  /* =============================================================================
     01 · CONVERTER
     ========================================================================== */

  var CONVERTER = {
    id: 'converter',
    number: '01',
    name: 'Converter',
    spec: 'Task 1 · Java + XML · 26 units',
    edges: [
      'Empty or non-numeric input → toast',
      'Below absolute zero → refused',
      'Huge values → scientific readout',
      'Category switch resets both spinners',
      'Swap re-converts without a tap'
    ],
    mount: function (screen) {
      var cats = [
        {
          label: 'Length', note: '8 units · metric ↔ imperial · base: metre', base: 'metre',
          units: [['Millimetre', 'mm', 0.001], ['Centimetre', 'cm', 0.01], ['Metre', 'm', 1], ['Kilometre', 'km', 1000],
                  ['Inch', 'in', 0.0254], ['Foot', 'ft', 0.3048], ['Yard', 'yd', 0.9144], ['Mile', 'mi', 1609.344]]
        },
        {
          label: 'Weight', note: '7 units · avoirdupois ↔ metric · base: gram', base: 'gram',
          units: [['Milligram', 'mg', 0.001], ['Gram', 'g', 1], ['Kilogram', 'kg', 1000], ['Tonne', 't', 1000000],
                  ['Ounce', 'oz', 28.349523125], ['Pound', 'lb', 453.59237], ['Stone', 'st', 6350.29318]]
        },
        {
          label: 'Volume', note: '8 units · SI ↔ US customary · base: litre', base: 'litre',
          units: [['Millilitre', 'mL', 0.001], ['Litre', 'L', 1], ['Cubic metre', 'm³', 1000], ['US gallon', 'US gal', 3.785411784],
                  ['US quart', 'US qt', 0.946352946], ['US pint', 'US pt', 0.473176473], ['US fluid ounce', 'US fl oz', 0.0295735295625],
                  ['US cup', 'US cup', 0.2365882365]]
        },
        {
          label: 'Temp', note: '3 scales · absolute zero at −273.15 °C', base: 'celsius',
          units: [['Celsius', '°C', 1], ['Fahrenheit', '°F', 1], ['Kelvin', 'K', 1]]
        }
      ];

      var cat = 0, from = 0, to = 1, hasResult = false;

      screen.innerHTML =
        '<div class="s-hdr">' +
          '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 01</span><span class="legend">Rev 1.0</span></div>' +
          '<hr class="rule rule--ink">' +
          '<h3 class="s-title">Converter</h3>' +
          '<p class="s-standfirst">Length, weight, volume, temperature. Twenty-six units in one ruled column, and no rounding tricks you did not ask for.</p>' +
        '</div>' +
        '<div class="tabs" role="tablist" data-tabs></div>' +
        '<div data-cat-note class="legend" style="padding:10px 16px 0"></div>' +
        '<div class="scroll">' +
          '<div class="row"><span class="legend gutter">01 / Value</span>' +
            '<input class="field" data-input inputmode="decimal" placeholder="enter a number" aria-label="Value to convert"></div>' +
          '<div class="row"><span class="legend gutter">02 / From</span><select class="field" data-from aria-label="Source unit"></select></div>' +
          '<div class="row"><span class="legend gutter">03 / To</span><select class="field" data-to aria-label="Target unit"></select></div>' +
          '<div class="row row--tight" style="border-bottom:0;justify-content:flex-end"><button class="btn-quiet" data-swap style="padding-right:0">Swap from / to</button></div>' +
          '<p class="colophon-line" style="padding:0 16px">SI base units · 1 in = 2.54 cm exact · 1 lb = 453.59237 g</p>' +
        '</div>' +
        '<div class="readout readout--converter">' +
          '<div class="readout-row"><span class="legend">Converted value</span><span class="legend" data-stale></span></div>' +
          '<p class="readout-value" data-result>—</p>' +
          '<hr class="readout-rule">' +
          '<p class="readout-note" data-sentence>Awaiting a value. Type a number, pick two units, then press convert.</p>' +
        '</div>' +
        '<div class="foot"><button class="cta" data-convert>Convert</button></div>';

      var tabs = screen.querySelector('[data-tabs]');
      var note = screen.querySelector('[data-cat-note]');
      var input = screen.querySelector('[data-input]');
      var fromSel = screen.querySelector('[data-from]');
      var toSel = screen.querySelector('[data-to]');
      var result = screen.querySelector('[data-result]');
      var sentence = screen.querySelector('[data-sentence]');
      var stale = screen.querySelector('[data-stale]');

      cats.forEach(function (c, i) {
        tabs.appendChild(h('<button class="tab" role="tab" aria-selected="' + (i === 0) + '">' + c.label + '</button>'));
      });

      function fillSelects() {
        var c = cats[cat];
        [fromSel, toSel].forEach(function (sel, which) {
          sel.innerHTML = '';
          c.units.forEach(function (u, i) {
            var o = document.createElement('option');
            o.value = String(i);
            o.textContent = u[0] + ' · ' + u[1];
            sel.appendChild(o);
          });
          sel.value = String(which === 0 ? from : to);
        });
      }

      function toCelsius(v, sym) {
        if (sym === '°F') return (v - 32) * 5 / 9;
        if (sym === 'K') return v - 273.15;
        return v;
      }
      function fromCelsius(c, sym) {
        if (sym === '°F') return c * 9 / 5 + 32;
        if (sym === 'K') return c + 273.15;
        return c;
      }

      function convert(loud) {
        var raw = input.value.trim();
        if (!raw) {
          if (loud) toast(screen, 'Enter a value to convert.');
          return;
        }
        var cleaned = raw.replace(/\s/g, '');
        if (cleaned.indexOf('.') < 0 && cleaned.indexOf(',') >= 0) cleaned = cleaned.replace(',', '.');
        else cleaned = cleaned.replace(/,/g, '');
        if (!/^[+-]?(\d+(\.\d*)?|\.\d+)$/.test(cleaned)) {
          if (loud) toast(screen, 'That is not a number.');
          return;
        }
        var value = parseFloat(cleaned);
        if (Math.abs(value) > 1e15) {
          if (loud) toast(screen, 'Too large to convert reliably. Keep it under 1 × 10¹⁵.');
          return;
        }
        var c = cats[cat];
        var a = c.units[from], b = c.units[to];
        var out;
        if (cat === 3) {
          var celsius = toCelsius(value, a[1]);
          if (celsius < -273.15 - 1e-9) {
            if (loud) toast(screen, 'Below absolute zero. −273.15 °C is the floor.');
            return;
          }
          out = fromCelsius(celsius, b[1]);
        } else {
          out = value * a[2] / b[2];
        }
        var text = fmt(out);
        var changed = result.textContent !== text;
        result.textContent = text;
        fitReadout(result, text, 34, 16);
        sentence.textContent = fmt(value).toUpperCase() + ' ' + a[1].toUpperCase() + '  =  ' +
          text + ' ' + b[1].toUpperCase() + '  ·  via ' + c.base;
        stale.textContent = 'Exact';
        hasResult = true;
        if (changed) {
          result.style.opacity = '0.25';
          requestAnimationFrame(function () { result.style.opacity = '1'; });
        }
      }

      function selectCategory(next) {
        cat = next;
        from = 0;
        to = cats[cat].units.length > 1 ? 1 : 0;
        Array.prototype.forEach.call(tabs.children, function (t, i) {
          t.setAttribute('aria-selected', String(i === cat));
        });
        note.textContent = cats[cat].note;
        fillSelects();
        hasResult = false;
        result.textContent = '—';
        result.style.fontSize = '34px';
        stale.textContent = '';
        sentence.textContent = 'Awaiting a value. Type a number, pick two units, then press convert.';
        input.value = '';
      }

      Array.prototype.forEach.call(tabs.children, function (tab, i) {
        tab.addEventListener('click', function () { if (i !== cat) selectCategory(i); });
      });

      screen.querySelector('[data-convert]').addEventListener('click', function () { convert(true); });
      screen.querySelector('[data-swap]').addEventListener('click', function () {
        var a = from; from = to; to = a;
        fromSel.value = String(from);
        toSel.value = String(to);
        if (hasResult) convert(false);
      });
      fromSel.addEventListener('change', function () { from = Number(fromSel.value); if (hasResult) convert(false); });
      toSel.addEventListener('change', function () { to = Number(toSel.value); if (hasResult) convert(false); });
      input.addEventListener('input', function () { if (hasResult) convert(false); });
      input.addEventListener('keydown', function (e) { if (e.key === 'Enter') convert(true); });

      selectCategory(0);
      input.value = '250';
      from = 1; to = 4;
      fromSel.value = '1'; toSel.value = '4';
      convert(false);
    }
  };

  /* =============================================================================
     02 · LEDGER  (accounts + SQLite-shaped task table, stored in localStorage here)
     ========================================================================== */

  var Ledger = {
    KEY: 'gr.ledger.v3',

    load: function () {
      try {
        var raw = JSON.parse(localStorage.getItem(this.KEY));
        if (raw && raw.users && raw.tasks) return raw;
      } catch (e) { /* fall through */ }
      return { users: [], tasks: [], session: null, nextTask: 1 };
    },
    save: function (db) { localStorage.setItem(this.KEY, JSON.stringify(db)); },

    salt: function () {
      var bytes = new Uint8Array(16);
      (window.crypto || {}).getRandomValues ? window.crypto.getRandomValues(bytes) : bytes.forEach(function (_, i) { bytes[i] = Math.floor(Math.random() * 256); });
      return Array.prototype.map.call(bytes, function (b) { return b.toString(16).padStart(2, '0'); }).join('');
    },

    hash: function (password, salt) {
      var text = salt + ':' + password;
      if (window.crypto && window.crypto.subtle && window.crypto.subtle.digest) {
        return window.crypto.subtle.digest('SHA-256', new TextEncoder().encode(text)).then(function (buf) {
          return Array.prototype.map.call(new Uint8Array(buf), function (b) { return b.toString(16).padStart(2, '0'); }).join('');
        });
      }
      var h1 = 0x811c9dc5;
      for (var i = 0; i < text.length; i++) { h1 ^= text.charCodeAt(i); h1 = (h1 * 0x01000193) >>> 0; }
      return Promise.resolve('fnv' + h1.toString(16));
    },

    seedDemo: function (db) {
      var self = this;
      var email = 'antonio@oibsip.demo';
      if (db.users.some(function (u) { return u.email === email; })) return Promise.resolve(db);
      var salt = this.salt();
      var now = Date.now();
      return this.hash('demo-passphrase', salt).then(function (digest) {
      db.users.push({ id: db.users.length + 1, name: 'Antonio', email: email, salt: salt, hash: digest, created: now });
      var rows = [
        ['Measure the lab bench and convert to imperial', 'Bench A is 183 cm by 74 cm. Convert both, then check against the cutting list before Saturday.', 0],
        ['Lap times for the 400 m set', '', 0],
        ['Read the SQLite docs on foreign keys', 'The ledger table uses ON DELETE CASCADE so deleting an account cannot leave orphaned entries behind.', 0],
        ['Rotate the phone mid-quiz without reshuffling', 'The deck is dealt from a stored seed, so onSaveInstanceState can re-deal the identical order after a configuration change.', 1],
        ['A deliberately long title that has to ellipsize rather than push the delete button off the row, because real task lists contain sentences, not labels', '', 0]
      ];
      rows.forEach(function (r, i) {
        db.tasks.push({
          id: db.nextTask++, user: db.users[db.users.length - 1].id, title: r[0], notes: r[1], done: r[2],
          created: now - (i + 1) * 3600000, completed: r[2] ? now - 900000 : null
        });
      });
      return db;
      });
    }
  };

  var LEDGER = {
    id: 'ledger',
    number: '02',
    name: 'Ledger',
    spec: 'Task 2 · Java + SQLite · PBKDF2',
    edges: [
      'Empty ledger, per filter',
      '120-character titles ellipsize',
      'Salted hash, never plaintext',
      'Delete confirms, then is permanent',
      'Log out clears the session'
    ],
    mount: function (screen) {
      var db = Ledger.load();
      var filter = 'ALL';
      var errorText = '';

      function me() {
        return db.users.filter(function (u) { return u.id === db.session; })[0] || null;
      }

      function tasksFor(user, mode) {
        return db.tasks
          .filter(function (t) { return t.user === user.id && (mode === 'ALL' || (mode === 'OPEN' ? !t.done : t.done)); })
          .sort(function (a, b) {
            if (a.done !== b.done) return a.done ? 1 : -1;
            return (b.completed || b.created) - (a.completed || a.created);
          });
      }

      /* ---------------- sign in ---------------- */

      function renderLogin() {
        screen.innerHTML =
          '<div class="s-hdr">' +
            '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 02</span><span class="legend">Rev 1.0</span></div>' +
            '<hr class="rule rule--ink">' +
            '<h3 class="s-title">The Ledger</h3>' +
            '<p class="s-standfirst">One account, one list, kept on this device. Sign in to pick up where you stopped.</p>' +
          '</div>' +
          '<div class="scroll"><div class="auth">' +
            '<div class="auth-fields">' +
              '<div class="row" style="padding:0"><span class="legend gutter">01 / Email</span><input class="field" data-email placeholder="you@example.com" autocomplete="username"></div>' +
              '<div class="row" style="padding:0"><span class="legend gutter">02 / Password</span><input class="field" type="password" data-password placeholder="your password" autocomplete="current-password"></div>' +
            '</div>' +
            '<p class="error-line" data-error style="padding:12px 0 0;' + (errorText ? '' : 'display:none') + '">' + errorText + '</p>' +
            '<button class="cta" data-signin style="margin-top:18px">Sign in</button>' +
            '<div class="link-row"><button class="btn-quiet" data-register>No account? Register</button></div>' +
            '<div class="link-row"><button class="btn-quiet" data-demo style="color:var(--oxide)">Use the demo account</button></div>' +
            '<p class="colophon-line">PBKDF2-SHA256 · 120,000 iterations · one salt per account · session in shared preferences</p>' +
          '</div></div>';

        var email = screen.querySelector('[data-email]');
        var password = screen.querySelector('[data-password]');
        var error = screen.querySelector('[data-error]');

        function fail(msg) {
          error.style.display = 'block';
          error.textContent = msg;
          errorText = msg;
        }

        screen.querySelector('[data-signin]').addEventListener('click', function () {
          if (!email.value.trim() || !password.value) { fail('Both fields are required.'); return; }
          var user = db.users.filter(function (u) {
            return u.email === email.value.trim().toLowerCase();
          })[0];
          var attempted = password.value;
          if (!user) {
            /* same message and same work either way: never confirm which half was wrong */
            Ledger.hash(attempted, 'no-such-account').then(function () {
              fail('No account matches that email and password.');
            });
            return;
          }
          Ledger.hash(attempted, user.salt).then(function (digest) {
            var ok = user.hash === digest;
            if (!ok) { fail('No account matches that email and password.'); password.value = ''; return; }
            db.session = user.id;
            Ledger.save(db);
            errorText = '';
            renderTasks();
          });
        });

        screen.querySelector('[data-register]').addEventListener('click', function () { errorText = ''; renderRegister(); });

        screen.querySelector('[data-demo]').addEventListener('click', function () {
          Ledger.seedDemo(db).then(function (seeded) {
            db = seeded;
            var demo = db.users.filter(function (u) { return u.email === 'antonio@oibsip.demo'; })[0];
            db.session = demo.id;
            Ledger.save(db);
            filter = 'ALL';
            errorText = '';
            renderTasks();
          });
        });

        password.addEventListener('keydown', function (e) { if (e.key === 'Enter') screen.querySelector('[data-signin]').click(); });
      }

      /* ---------------- register ---------------- */

      function renderRegister() {
        screen.innerHTML =
          '<div class="s-hdr">' +
            '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 02</span><span class="legend">Rev 1.0</span></div>' +
            '<hr class="rule rule--ink">' +
            '<h3 class="s-title">Open an account</h3>' +
            '<p class="s-standfirst">Your name and email live in the app database. The password is salted and hashed before it is stored, never written as text.</p>' +
          '</div>' +
          '<div class="scroll"><div class="auth">' +
            '<div class="auth-fields">' +
              '<div class="row" style="padding:0"><span class="legend gutter">01 / Name</span><input class="field" data-name placeholder="what should the page say"></div>' +
              '<div class="row" style="padding:0"><span class="legend gutter">02 / Email</span><input class="field" data-email placeholder="you@example.com"></div>' +
              '<div class="row" style="padding:0"><span class="legend gutter">03 / Password</span><input class="field" type="password" data-password placeholder="at least 8 characters"></div>' +
            '</div>' +
            '<p class="error-line" data-error style="padding:12px 0 0;display:none"></p>' +
            '<button class="cta" data-create style="margin-top:18px">Create account</button>' +
            '<div class="link-row"><button class="btn-quiet" data-back>Already registered? Sign in</button></div>' +
            '<p class="colophon-line">Salted + hashed · nothing leaves this browser · no network calls</p>' +
          '</div></div>';

        var name = screen.querySelector('[data-name]');
        var email = screen.querySelector('[data-email]');
        var password = screen.querySelector('[data-password]');
        var error = screen.querySelector('[data-error]');

        function fail(msg) { error.style.display = 'block'; error.textContent = msg; }

        screen.querySelector('[data-back]').addEventListener('click', function () { errorText = ''; renderLogin(); });

        screen.querySelector('[data-create]').addEventListener('click', function () {
          var n = name.value.trim();
          var e = email.value.trim().toLowerCase();
          var p = password.value;
          if (n.length < 2) { fail('Enter the name you want on the account.'); return; }
          if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)) { fail('That address is missing an @ or a domain.'); return; }
          if (p.length < 8) { fail('Use at least 8 characters. Longer is better than clever.'); return; }
          if (db.users.some(function (u) { return u.email === e; })) { fail('An account already exists for that email.'); return; }
          var salt = Ledger.salt();
          Ledger.hash(p, salt).then(function (digest) {
            db.users.push({ id: db.users.length + 1, name: n, email: e, salt: salt, hash: digest, created: Date.now() });
            db.session = db.users[db.users.length - 1].id;
            Ledger.save(db);
            filter = 'ALL';
            errorText = '';
            renderTasks();
          });
        });
      }

      /* ---------------- ledger ---------------- */

      function renderTasks() {
        var user = me();
        if (!user) { renderLogin(); return; }

        var rows = tasksFor(user, filter);
        var open = db.tasks.filter(function (t) { return t.user === user.id && !t.done; }).length;
        var done = db.tasks.filter(function (t) { return t.user === user.id && t.done; }).length;

        var emptyCopy = {
          ALL: ['00 / no entries', 'The ledger is clean.', 'Add the first entry. It stays on this device.'],
          OPEN: ['00 / no open entries', 'Nothing open.', 'Every entry on the page is struck through.'],
          DONE: ['00 / no finished entries', 'Nothing finished yet.', 'Tap an entry to strike it through.']
        }[filter];

        screen.innerHTML =
          '<div class="s-hdr" style="padding-bottom:0">' +
            '<div class="s-masthead">' +
              '<span class="legend">OIBSIP / Instrument 02</span>' +
              '<button class="btn-quiet" data-logout style="padding:0">Log out</button>' +
            '</div>' +
            '<hr class="rule rule--ink">' +
            '<h3 class="s-title">The Ledger</h3>' +
            '<p class="s-standfirst">Tasks are keyed to your account and stored in SQLite on the device.</p>' +
          '</div>' +
          '<div class="count-block">' +
            '<span class="count-number">' + (open > 99 ? open : pad2(open)) + '</span>' +
            '<span class="count-side"><span class="legend">Open</span><span class="legend" style="font-size:8.5px">' + pad2(done) + ' done</span></span>' +
            '<span class="count-name">' + user.name.replace(/[<>&]/g, '') + '</span>' +
          '</div>' +
          '<hr class="rule rule--ink" style="margin:8px 16px 0">' +
          '<div class="tabs" style="padding:0 16px" data-filters>' +
            ['ALL', 'OPEN', 'DONE'].map(function (f) {
              return '<button class="tab" role="tab" aria-selected="' + (f === filter) + '" data-filter="' + f + '">' + f.toLowerCase() + '</button>';
            }).join('') +
          '</div>' +
          '<div class="task-list" data-list></div>' +
          '<div class="foot"><button class="cta" data-add>Add entry</button></div>';

        var list = screen.querySelector('[data-list]');

        if (!rows.length) {
          list.appendChild(h(
            '<div class="empty">' +
              '<p class="legend">' + emptyCopy[0] + '</p>' +
              '<p class="empty-title">' + emptyCopy[1] + '</p>' +
              '<p class="error-line" style="color:var(--muted);padding:7px 0 0;letter-spacing:0">' + emptyCopy[2] + '</p>' +
            '</div>'));
        }

        rows.forEach(function (task) {
          var node = h(
            '<div class="task' + (task.done ? ' task-done' : '') + '">' +
              '<span class="task-open"></span>' +
              '<span class="task-check"><svg width="11" height="9" viewBox="0 0 14 11"><path d="M1 5.4 L5 9.4 L13 1.2" fill="none" stroke="#f3f0e9" stroke-width="2"/></svg></span>' +
              '<div class="task-body">' +
                '<div class="task-title"></div>' +
                (task.notes ? '<p class="task-notes"></p>' : '') +
                '<p class="task-stamp">' + (task.done ? 'Done ' : 'Added ') + stamp(task.completed || task.created) + '</p>' +
              '</div>' +
              '<button class="task-del" aria-label="Delete entry">Del</button>' +
            '</div>');
          node.querySelector('.task-title').textContent = task.title;
          if (task.notes) node.querySelector('.task-notes').textContent = task.notes;
          node.querySelector('.task-body').addEventListener('click', function () {
            task.done = !task.done;
            task.completed = task.done ? Date.now() : null;
            Ledger.save(db);
            renderTasks();
          });
          node.querySelector('.task-del').addEventListener('click', function () { confirmDelete(task); });
          list.appendChild(node);
        });

        screen.querySelector('[data-logout]').addEventListener('click', function () {
          db.session = null;
          Ledger.save(db);
          filter = 'ALL';
          errorText = '';
          renderLogin();
        });

        Array.prototype.forEach.call(screen.querySelectorAll('[data-filter]'), function (tab) {
          tab.addEventListener('click', function () { filter = tab.getAttribute('data-filter'); renderTasks(); });
        });

        screen.querySelector('[data-add]').addEventListener('click', addEntry);
      }

      function addEntry() {
        openSheet(screen,
          '<p class="legend">OIBSIP / Ledger / New entry</p>' +
          '<hr class="rule rule--ink">' +
          '<h4>Add an entry</h4>' +
          '<div style="margin-top:12px">' +
            '<div class="row" style="padding:0"><span class="legend gutter">01 / Task</span><input class="field" data-title maxlength="120" placeholder="What needs doing?"></div>' +
            '<div class="row" style="padding:0;align-items:flex-start"><span class="legend gutter" style="padding-top:12px">02 / Notes</span>' +
              '<textarea class="field" data-notes rows="2" maxlength="400" placeholder="Context, a link, a deadline" style="resize:none;text-align:right;padding-top:11px"></textarea></div>' +
          '</div>' +
          '<div class="sheet-actions"><button class="btn-ghost" data-cancel>Cancel</button><button class="cta" data-save>Add</button></div>' +
          '<p class="sheet-note">Notes are optional. Titles are capped at 120 characters so the row can never break its rhythm.</p>',
          function (sheet, close) {
            var title = sheet.querySelector('[data-title]');
            title.focus();
            sheet.querySelector('[data-cancel]').addEventListener('click', close);
            sheet.querySelector('[data-save]').addEventListener('click', function () {
              var value = title.value.replace(/\s+/g, ' ').trim();
              if (!value) { title.focus(); toast(screen, 'An entry needs a title.'); return; }
              db.tasks.push({
                id: db.nextTask++, user: me().id, title: value,
                notes: sheet.querySelector('[data-notes]').value.trim(),
                done: false, created: Date.now(), completed: null
              });
              Ledger.save(db);
              if (filter === 'DONE') filter = 'ALL';
              close();
              renderTasks();
            });
          });
      }

      function confirmDelete(task) {
        openSheet(screen,
          '<p class="legend">OIBSIP / Ledger / Delete</p>' +
          '<hr class="rule rule--ink">' +
          '<h4>Delete this entry?</h4>' +
          '<p class="task-title" style="margin:10px 0 0;font-size:12px"></p>' +
          '<p class="sheet-note">This is permanent. There is no archive, and no undo.</p>' +
          '<div class="sheet-actions"><button class="btn-ghost" data-cancel>Cancel</button><button class="cta" data-delete>Delete</button></div>',
          function (sheet, close) {
            sheet.querySelector('.task-title').textContent = task.title;
            sheet.querySelector('[data-cancel]').addEventListener('click', close);
            sheet.querySelector('[data-delete]').addEventListener('click', function () {
              db.tasks = db.tasks.filter(function (t) { return t.id !== task.id; });
              Ledger.save(db);
              if (filter === 'DONE') filter = 'ALL';
              close();
              renderTasks();
            });
          });
      }

      if (db.session) renderTasks(); else renderLogin();
    }
  };

  /* =============================================================================
     03 · CALCULATOR  (same token model as Evaluator.java)
     ========================================================================== */

  var CALCULATOR = {
    id: 'calculator',
    number: '03',
    name: 'Calculator',
    spec: 'Task 3 · Java + XML · GridLayout',
    edges: [
      'Precedence: 2 + 3 × 4 = 14',
      'Divide by zero → ERROR, no crash',
      'Tape capped at 24 tokens',
      'Backspace walks back through a number',
      'Result chained into the next sum'
    ],
    mount: function (screen) {
      var keys = [
        ['AC', 'fn'], ['±', 'fn'], ['%', 'fn'], ['÷', 'op'],
        ['7', ''], ['8', ''], ['9', ''], ['×', 'op'],
        ['4', ''], ['5', ''], ['6', ''], ['−', 'op'],
        ['1', ''], ['2', ''], ['3', ''], ['+', 'op'],
        ['0', ''], ['.', ''], ['Del', 'fn'], ['=', 'eq']
      ];

      screen.innerHTML =
        '<div class="s-hdr">' +
          '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 03</span><span class="legend">Rev 1.0</span></div>' +
          '<hr class="rule rule--ink">' +
          '<h3 class="s-title" style="font-size:22px">Calculator</h3>' +
          '<p class="s-standfirst" style="font-size:9px">Four functions, twenty keys. No history, nothing saved when you leave.</p>' +
        '</div>' +
        '<div class="readout calc-display">' +
          '<div class="calc-tape"><span class="legend">03 / Display</span><span class="legend" data-tape>New tape</span></div>' +
          '<p class="readout-value" data-result>0</p>' +
        '</div>' +
        '<div class="calc-keys" data-keys></div>';

      var tapeView = screen.querySelector('[data-tape]');
      var result = screen.querySelector('[data-result]');
      var keyPad = screen.querySelector('[data-keys]');

      var tokens = [];
      var operand = '';
      var resultRaw = null;
      var lastExpression = '';
      var evaluated = false;
      var error = false;
      var reason = '';

      var OPS = ['+', '−', '×', '÷'];
      var MAX_TOKENS = 24, MAX_OPERAND = 14;

      function isOp(t) { return OPS.indexOf(t) >= 0; }

      function evaluate(list) {
        if (!list.length) return 0;
        if (list.length % 2 === 0) throw new Error('Incomplete');
        var nums = [], ops = [];
        list.forEach(function (t, i) {
          if (i % 2 === 0) {
            var n = parseFloat(String(t).replace(/,/g, '').replace('−', '-'));
            if (isNaN(n)) throw new Error('Not a number');
            nums.push(n);
          } else {
            if (!isOp(t)) throw new Error('Expected an operator');
            ops.push(t);
          }
        });
        var folded = [nums[0]], adds = [];
        for (var i = 0; i < ops.length; i++) {
          var op = ops[i], next = nums[i + 1], last = folded.length - 1;
          if (op === '×') folded[last] = folded[last] * next;
          else if (op === '÷') {
            if (next === 0) throw new Error('DIVIDE BY ZERO');
            folded[last] = folded[last] / next;
          } else { adds.push(op); folded.push(next); }
        }
        var total = folded[0];
        adds.forEach(function (op, i) { total = op === '+' ? total + folded[i + 1] : total - folded[i + 1]; });
        if (!isFinite(total) || isNaN(total)) throw new Error('OUT OF RANGE');
        return total;
      }

      function calcFmt(v) {
        var abs = Math.abs(v);
        if (abs < 5e-12) return '0';
        if (abs >= 1e12 || abs < 1e-9) return v.toExponential(6).toUpperCase();
        var fixed = v.toFixed(10).replace(/0+$/, '').replace(/\.$/, '');
        return groupInt(fixed);
      }

      function clearError() {
        if (error) { error = false; reason = ''; tokens = []; operand = ''; }
      }

      function render() {
        var big;
        if (error) big = 'ERROR';
        else if (operand) big = groupInt(operand);
        else if (evaluated && resultRaw) big = groupInt(resultRaw);
        else if (tokens.length && !isOp(tokens[tokens.length - 1])) big = groupInt(tokens[tokens.length - 1]);
        else big = resultRaw ? groupInt(resultRaw) : '0';
        result.textContent = big;
        fitReadout(result, big, 34, 15);

        var tape;
        if (error) tape = reason;
        else if (evaluated) tape = lastExpression;
        else if (tokens.length) tape = tokens.join(' ');
        else tape = 'New tape';
        tapeView.textContent = tape.toUpperCase();
      }

      function pressKey(key) {
        if (/^\d$/.test(key)) {
          clearError();
          if (evaluated) { tokens = []; operand = ''; resultRaw = null; lastExpression = ''; evaluated = false; }
          if (operand.length >= MAX_OPERAND) return;
          if (operand === '0') operand = key;
          else if (operand === '-0') operand = '-' + key;
          else operand += key;
        } else if (key === '.') {
          clearError();
          if (evaluated) { tokens = []; operand = ''; resultRaw = null; evaluated = false; }
          if (operand.indexOf('.') >= 0 || operand.length >= MAX_OPERAND) return;
          operand = operand ? operand + '.' : '0.';
        } else if (OPS.indexOf(key) >= 0) {
          if (error) return;
          if (evaluated) {
            tokens = []; if (resultRaw !== null) tokens.push(resultRaw);
            operand = ''; lastExpression = ''; evaluated = false;
          }
          if (operand) { tokens.push(operand); operand = ''; }
          if (!tokens.length) tokens.push('0');
          if (tokens.length >= MAX_TOKENS) return;
          var last = tokens[tokens.length - 1];
          if (isOp(last)) tokens[tokens.length - 1] = key;
          else tokens.push(key);
        } else if (key === '=') {
          if (error) return;
          if (operand) { tokens.push(operand); operand = ''; }
          while (tokens.length && isOp(tokens[tokens.length - 1])) tokens.pop();
          if (!tokens.length) { render(); return; }
          lastExpression = tokens.join(' ');
          try {
            var value = evaluate(tokens);
            var text = calcFmt(value);
            resultRaw = text.replace(/,/g, '');
            evaluated = true;
            tokens = [];
            render();
            result.style.opacity = '0.3';
            requestAnimationFrame(function () { result.style.opacity = '1'; });
            return;
          } catch (e) {
            error = true;
            reason = e.message === 'DIVIDE BY ZERO' ? 'Division by zero · the tape stops here' : 'Out of range · clear to start again';
            tokens = []; operand = ''; resultRaw = null; evaluated = false;
          }
        } else if (key === 'AC') {
          tokens = []; operand = ''; resultRaw = null; lastExpression = ''; evaluated = false; error = false; reason = '';
        } else if (key === 'Del') {
          if (error) { tokens = []; operand = ''; error = false; reason = ''; }
          else if (evaluated) { tokens = []; operand = ''; resultRaw = null; lastExpression = ''; evaluated = false; }
          else if (operand) {
            operand = operand.slice(0, -1);
            if (operand === '-') operand = '';
          } else if (tokens.length) {
            var removed = tokens.pop();
            if (!isOp(removed)) operand = removed.length > 1 ? removed.slice(0, -1) : '';
          }
        } else if (key === '±') {
          clearError();
          if (evaluated) {
            if (resultRaw === null) return;
            resultRaw = resultRaw.startsWith('-') ? resultRaw.slice(1) : '-' + resultRaw;
          } else {
            if (!operand) return;
            operand = operand.startsWith('-') ? operand.slice(1) : '-' + operand;
          }
        } else if (key === '%') {
          clearError();
          var source = operand || (evaluated ? resultRaw : null);
          if (source === null || source === '') return;
          operand = String(parseFloat(source) / 100).replace(/\.?0+$/, '');
          if (operand === '' || operand === '-') operand = '0';
          evaluated = false;
        }
        render();
      }

      keys.forEach(function (k) {
        var cls = 'key' + (k[1] ? ' key--' + k[1] : '');
        var btn = h('<button class="' + cls + '">' + k[0] + '</button>');
        btn.addEventListener('click', function () { pressKey(k[0]); });
        keyPad.appendChild(btn);
      });

      /* typing on a physical keyboard, because a demo is usually on a laptop */
      screen.setAttribute('tabindex', '0');
      screen.addEventListener('keydown', function (e) {
        var map = { '*': '×', '/': '÷', 'x': '×', '-': '−', '+': '+', 'Enter': '=', '=': '=', 'Backspace': 'Del', 'Escape': 'AC', 'c': 'AC', '.': '.', ',': '.' };
        if (/^\d$/.test(e.key)) { pressKey(e.key); e.preventDefault(); }
        else if (map[e.key]) { pressKey(map[e.key]); e.preventDefault(); }
      });

      render();
    }
  };

  /* =============================================================================
     04 · PASSAGE (quiz)
     ========================================================================== */

  var BANK = [
    ['One minute of latitude spans how far across the Earth\u2019s surface?',
      ['One nautical mile', 'One kilometre', 'One statute mile', 'One degree of longitude'], 0,
      'By definition one minute of latitude is one nautical mile, 1,852 m. That is why distance on a paper chart is measured against the latitude scale at the side, never the longitude scale at the top.'],
    ['The international nautical mile is defined as exactly',
      ['1,609.344 metres', '1,829 metres', '1,852 metres', '1,000 metres'], 2,
      'Fixed at 1,852 m by the 1929 International Hydrographic Conference. The old British Admiralty mile was 1,853.184 m, which is why very old charts disagree with a GPS fix.'],
    ['A knot is',
      ['One statute mile per hour', 'One hundred fathoms per hour', 'One metre per second', 'One nautical mile per hour'], 3,
      'Ten knots is ten nautical miles an hour, about 18.5 km/h. Speed logs and GPS both report knots, and charts are scaled in nautical miles.'],
    ['Which instrument measures the angle between the horizon and a celestial body?',
      ['Chronometer', 'Sextant', 'Barograph', 'Lead line'], 1,
      'A sextant reads that angle to a tenth of a minute of arc. The chronometer supplies the time and the time supplies longitude; the sextant alone only gives latitude.'],
    ['A chronometer set to GMT is used to find',
      ['Latitude', 'Depth', 'Longitude', 'Magnetic variation'], 2,
      'Longitude is a time problem. The Earth turns 15 degrees every hour, so one hour of chronometer error becomes 15 degrees of longitude — up to 900 nautical miles at the equator.'],
    ['Which star sits within one degree of the north celestial pole?',
      ['Sirius', 'Vega', 'Canopus', 'Polaris'], 3,
      'Polaris is about 0.65 degrees from the pole today, so its altitude is a usable latitude in the northern hemisphere. The southern hemisphere has no equivalent bright pole star.'],
    ['Under IALA Region A, the mark on the port side of a channel entering from seaward is',
      ['Green', 'Red', 'Yellow', 'White'], 1,
      'Region B — the Americas, Japan, Korea and the Philippines — reverses red and green. Mixing the two conventions is a classic cause of grounding.'],
    ['A light charted as Fl(3) 20s shows',
      ['A fixed light for 20 seconds', 'Three occultations per minute', 'Three flashes every 20 seconds', 'Three separate lights'], 2,
      'The bracket is the group count; the number after it is the period of one full cycle. Fl(3) 20s is three flashes, then darkness, repeating every twenty seconds.'],
    ['A sounding of 12 on a metric chart means',
      ['12 fathoms below the keel', '12 metres of clearance under the keel', '12 metres at mean high water', '12 metres of water below chart datum'], 3,
      'Soundings are depths below chart datum, not below your keel. Subtract your draft, then add the height of tide, before deciding the water is safe.'],
    ['Mercator\u2019s projection keeps compass bearings straight but distorts',
      ['The compass rose', 'Scale, increasingly with latitude', 'Depth', 'Magnetic variation'], 1,
      'That is why Greenland looks the size of Africa. The trade is deliberate: a straight line on a Mercator chart is a constant bearing on the water.'],
    ['Dead reckoning is a position found from',
      ['Course, speed and time since the last fix', 'The last satellite fix', 'A line of soundings', 'The height of the tide'], 0,
      'It accumulates error with every course change and every current you did not allow for, so it is corrected by a fix as soon as one is available.'],
    ['Facing forward on a vessel, port and starboard are',
      ['Port right, starboard left', 'Port aft, starboard forward', 'Port forward, starboard aft', 'Port left, starboard right'], 3,
      'Port is left, starboard is right, and both are fixed to the vessel rather than to the person on deck — the reason an order is repeated back before it is carried out.']
  ];

  function mulberry32(seed) {
    var a = seed >>> 0;
    return function () {
      a = (a + 0x6D2B79F5) >>> 0;
      var t = a;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  function shuffle(list, rand) {
    var copy = list.slice();
    for (var i = copy.length - 1; i > 0; i--) {
      var j = Math.floor(rand() * (i + 1));
      var t = copy[i]; copy[i] = copy[j]; copy[j] = t;
    }
    return copy;
  }

  function deal(seed) {
    var rand = mulberry32(seed);
    return shuffle(BANK, rand).map(function (row) {
      var correctText = row[1][row[2]];
      var options = shuffle(row[1], rand);
      return { prompt: row[0], options: options, correct: options.indexOf(correctText), note: row[3] };
    });
  }

  var QUIZ = {
    id: 'quiz',
    number: '04',
    name: 'Passage',
    spec: 'Task 4 · Java + XML · 12 questions',
    edges: [
      'Seeded shuffle survives a rotation',
      'Answer locks on the first tap',
      'Notes explain every answer',
      'Long options wrap, never clip',
      'Restart deals a new seed'
    ],
    mount: function (screen) {
      var deck = [];
      var index = 0;
      var answers = [];
      var seed = 0;

      function renderWelcome() {
        screen.innerHTML =
          '<div class="s-hdr">' +
            '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 04</span><span class="legend">Rev 1.0</span></div>' +
            '<hr class="rule rule--ink">' +
            '<h3 class="s-title" style="font-size:34px;margin-top:22px">Passage</h3>' +
            '<p class="s-standfirst" style="font-size:11px">Twelve questions on latitude, lights and the long way home. Every answer carries a note, whether you get it right or not.</p>' +
          '</div>' +
          '<div class="scroll">' +
            '<div style="padding:0 16px;margin-top:20px">' +
              '<hr class="rule rule--ink" style="margin:0">' +
              specRow('01 / Questions', '12') +
              specRow('02 / Order', 'Shuffled each run') +
              specRow('03 / Scoring', 'One point, first answer') +
              specRow('04 / Topic', 'Coastal navigation') +
              '<hr class="rule rule--ink" style="margin:0">' +
            '</div>' +
          '</div>' +
          '<div class="foot foot-stack">' +
            '<button class="cta" data-start>Start the quiz</button>' +
            '<p class="colophon-line">No score is saved · the card is wiped after each run</p>' +
          '</div>';

        screen.querySelector('[data-start]').addEventListener('click', function () {
          seed = Math.floor(Math.random() * 1e9);
          deck = deal(seed);
          index = 0;
          answers = deck.map(function () { return -1; });
          renderQuestion();
        });
      }

      function specRow(label, value) {
        return '<div class="row" style="padding:0"><span class="legend gutter">' + label + '</span>' +
          '<span class="field" style="text-align:right;font-size:11.5px">' + value + '</span></div>';
      }

      function renderQuestion() {
        var card = deck[index];
        var answered = answers[index] >= 0;
        var done = index + (answered ? 1 : 0);

        screen.innerHTML =
          '<div class="s-hdr">' +
            '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 04</span>' +
              '<span class="legend" data-counter>Question ' + pad2(index + 1) + ' / ' + pad2(deck.length) + '</span></div>' +
            '<div class="progress"><i style="width:' + (done / deck.length * 100) + '%"></i><i></i></div>' +
            '<h3 class="question" data-question></h3>' +
            '<p class="legend" data-hint style="margin:12px 0 0">Tap an answer. It locks immediately.</p>' +
          '</div>' +
          '<div class="scroll"><div class="options" data-options></div><p class="option-note" data-note style="display:none"></p></div>' +
          '<div class="foot"><button class="cta" data-next disabled>' +
            (index === deck.length - 1 ? 'See results' : 'Next question') + '</button></div>';

        screen.querySelector('[data-question]').textContent = card.prompt;
        var optionsBox = screen.querySelector('[data-options]');
        var nextBtn = screen.querySelector('[data-next]');

        card.options.forEach(function (text, i) {
          var btn = h('<button class="option"><span class="idx">' + String.fromCharCode(65 + i) + '</span><span data-text></span></button>');
          btn.querySelector('[data-text]').textContent = text;
          btn.addEventListener('click', function () { answer(i); });
          optionsBox.appendChild(btn);
        });

        if (answered) paint(card, answers[index]);

        nextBtn.disabled = !answered;
        nextBtn.addEventListener('click', advance);

        function answer(choice) {
          if (answers[index] >= 0) return;
          answers[index] = choice;
          paint(card, choice);
          nextBtn.disabled = false;
        }

        function paint(current, choice) {
          Array.prototype.forEach.call(optionsBox.children, function (btn, i) {
            btn.disabled = true;
            btn.classList.remove('option--correct', 'option--wrong', 'option--muted');
            if (i === current.correct) btn.classList.add('option--correct');
            else if (i === choice) btn.classList.add('option--wrong');
            else btn.classList.add('option--muted');
            btn.style.opacity = '0.55';
            requestAnimationFrame(function () { btn.style.opacity = '1'; });
          });
          screen.querySelector('[data-hint]').style.display = 'none';
          var note = screen.querySelector('[data-note]');
          note.style.display = 'block';
          note.textContent = 'NOTE · ' + current.note;
          screen.querySelector('[data-counter]').textContent = 'Question ' + pad2(index + 1) + ' / ' + pad2(deck.length);
          var bar = screen.querySelector('.progress i');
          bar.style.width = ((index + 1) / deck.length * 100) + '%';
        }
      }

      function advance() {
        if (answers[index] < 0) return;
        if (index < deck.length - 1) { index++; renderQuestion(); return; }
        var correct = answers.filter(function (a, i) { return a >= 0 && a === deck[i].correct; }).length;
        renderResult(correct, deck.length);
      }

      function verdictFor(correct, total) {
        if (correct === total) return 'A clean landfall. Nothing to correct.';
        var f = correct / total;
        if (f >= 0.8) return 'Within a cable\u2019s length of perfect.';
        if (f >= 0.55) return 'A workable fix. Read the notes, then run it again.';
        if (f >= 0.3) return 'Off the chart. The notes matter more than the score.';
        return 'Hard aground. Start with the definitions, then come back.';
      }

      function renderResult(correct, total) {
        var incorrect = total - correct;
        var percent = Math.round(correct * 100 / total);
        screen.innerHTML =
          '<div class="s-hdr">' +
            '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 04</span><span class="legend">Result</span></div>' +
            '<hr class="rule rule--ink">' +
          '</div>' +
          '<div class="scroll">' +
            '<div style="padding:0 16px">' +
              '<div style="display:flex;align-items:flex-end;gap:8px;margin-top:18px">' +
                '<span class="count-number" style="font-size:64px">' + pad2(correct) + '</span>' +
                '<span style="font-family:var(--serif);font-style:italic;font-size:22px;color:var(--muted);padding-bottom:6px">of ' + total + '</span>' +
              '</div>' +
              '<p style="font-family:var(--serif);font-style:italic;font-size:16px;line-height:1.15;margin:6px 0 0">' + verdictFor(correct, total) + '</p>' +
              '<hr class="rule rule--ink" style="margin:18px 0 0">' +
              resultRow('Correct', pad2(correct), 'var(--moss)') +
              resultRow('Incorrect', pad2(incorrect), 'var(--oxide)') +
              resultRow('Score', percent + '%', 'var(--ink)') +
              '<hr class="rule rule--ink" style="margin:0">' +
            '</div>' +
          '</div>' +
          '<div class="foot foot-stack">' +
            '<button class="cta" data-restart>Restart quiz</button>' +
            '<button class="btn-ghost" data-close>Close</button>' +
            '<p class="colophon-line">Nothing is stored · no account · no network</p>' +
          '</div>';

        screen.querySelector('[data-restart]').addEventListener('click', function () {
          seed = Math.floor(Math.random() * 1e9);
          deck = deal(seed);
          index = 0;
          answers = deck.map(function () { return -1; });
          renderQuestion();
        });
        screen.querySelector('[data-close]').addEventListener('click', renderWelcome);
      }

      function resultRow(label, value, colour) {
        return '<div class="row" style="padding:0"><span class="legend gutter">' + label + '</span>' +
          '<span class="field" style="text-align:right;color:' + colour + '">' + value + '</span></div>';
      }

      renderWelcome();
    }
  };

  /* =============================================================================
     05 · CHRONOGRAPH
     ========================================================================== */

  var CHRONO = {
    id: 'chrono',
    number: '05',
    name: 'Chronograph',
    spec: 'Task 5 · Java + XML · Handler',
    edges: [
      'Elapsed survives a reload',
      'Start greys out while running',
      'Lap records a split, does not stop',
      'Best and worst tagged live',
      '100 laps scroll without jank'
    ],
    mount: function (screen) {
      var STORE = 'gr.chrono.v3';
      var state = { accumulated: 0, startedAt: null, laps: [] };

      try {
        var saved = JSON.parse(localStorage.getItem(STORE));
        if (saved && typeof saved.accumulated === 'number') {
          state.accumulated = saved.accumulated;
          state.startedAt = saved.running ? saved.startedAt : null;
          state.laps = saved.laps || [];
        }
      } catch (e) { /* start clean */ }

      function running() { return state.startedAt !== null; }
      function elapsed() {
        return state.accumulated + (running() ? Math.max(0, Date.now() - state.startedAt) : 0);
      }
      function persist() {
        localStorage.setItem(STORE, JSON.stringify({
          accumulated: running() ? state.accumulated : elapsed(),
          running: running(),
          startedAt: state.startedAt,
          laps: state.laps
        }));
      }
      function stop() {
        if (!running()) return;
        state.accumulated = elapsed();
        state.startedAt = null;
        persist();
      }

      function mainTime(ms) {
        var s = Math.floor(ms / 1000);
        var hrs = Math.floor(s / 3600), mins = Math.floor((s % 3600) / 60), secs = s % 60;
        return hrs > 0 ? hrs + ':' + pad2(mins) + ':' + pad2(secs) : pad2(mins) + ':' + pad2(secs);
      }
      function frac(ms) { return '.' + pad2(Math.floor((ms % 1000) / 10)); }

      screen.innerHTML =
        '<div class="s-hdr">' +
          '<div class="s-masthead"><span class="legend">OIBSIP / Instrument 05</span><span class="legend">Rev 1.0</span></div>' +
          '<hr class="rule rule--ink">' +
          '<h3 class="s-title" style="font-size:22px">Chronograph</h3>' +
          '<p class="s-standfirst" style="font-size:9px">Forty-millisecond resolution, measured from the boot clock rather than the wall clock, so putting the phone down does not lose time.</p>' +
        '</div>' +
        '<div class="readout" style="margin:12px 16px 0">' +
          '<div class="readout-row"><span class="legend">Elapsed</span>' +
            '<span class="legend"><i class="run-dot" data-dot></i><span data-state>Idle</span></span></div>' +
          '<p class="chrono-time" data-time>00:00<small>.00</small></p>' +
          '<hr class="readout-rule">' +
          '<p class="readout-note" data-last>No lap recorded</p>' +
        '</div>' +
        '<div style="padding:16px 16px 0" data-lapzone>' +
          '<div style="display:flex;justify-content:space-between"><span class="legend">Laps</span><span class="legend" data-lapcount>0 recorded</span></div>' +
          '<hr class="rule rule--ink" style="margin:7px 0 0">' +
          '<div class="lap-head" style="padding-left:0;padding-right:0"><span class="legend lap-num">#</span><span class="legend lap-split">Split</span><span class="legend lap-tag"></span><span class="legend lap-total">Total</span></div>' +
          '<div data-laps style="max-height:150px;overflow-y:auto"></div>' +
        '</div>' +
        '<div class="foot foot-stack" style="margin-top:auto">' +
          '<div class="foot--pair">' +
            '<button class="cta" data-start>Start</button>' +
            '<button class="btn-ink" data-stop disabled>Stop</button>' +
          '</div>' +
          '<div class="foot--pair">' +
            '<button class="btn-ghost" data-lap disabled>Lap</button>' +
            '<button class="btn-ghost" data-reset disabled>Reset</button>' +
          '</div>' +
        '</div>';

      var timeView = screen.querySelector('[data-time]');
      var stateView = screen.querySelector('[data-state]');
      var dot = screen.querySelector('[data-dot]');
      var lastView = screen.querySelector('[data-last]');
      var lapCount = screen.querySelector('[data-lapcount]');
      var lapBox = screen.querySelector('[data-laps]');
      var startBtn = screen.querySelector('[data-start]');
      var stopBtn = screen.querySelector('[data-stop]');
      var lapBtn = screen.querySelector('[data-lap]');
      var resetBtn = screen.querySelector('[data-reset]');

      function paintTime() {
        var ms = elapsed();
        timeView.innerHTML = '';
        timeView.appendChild(document.createTextNode(mainTime(ms)));
        timeView.appendChild(h('<small>' + frac(ms) + '</small>'));
      }

      function paintLaps() {
        var chronological = [];
        var previous = 0;
        state.laps.forEach(function (total, i) {
          chronological.push({ n: i + 1, split: total - previous, total: total });
          previous = total;
        });
        var newestFirst = chronological.slice().reverse();

        var best = -1, worst = -1;
        if (newestFirst.length >= 2) {
          best = 0; worst = 0;
          newestFirst.forEach(function (l, i) {
            if (l.split < newestFirst[best].split) best = i;
            if (l.split > newestFirst[worst].split) worst = i;
          });
          if (best === worst) worst = -1;
        }

        lapBox.innerHTML = '';
        newestFirst.forEach(function (lap, i) {
          var tag = i === best ? '<span class="lap-tag lap-tag--best">Best</span>'
                  : (i === worst ? '<span class="lap-tag lap-tag--worst">Worst</span>' : '<span class="lap-tag"></span>');
          var row = h('<div class="lap" style="padding-left:0;padding-right:0">' +
            '<span class="legend lap-num">L' + pad2(lap.n) + '</span>' +
            '<span class="lap-split">' + mainTime(lap.split) + frac(lap.split) + '</span>' +
            tag +
            '<span class="lap-total">' + mainTime(lap.total) + frac(lap.total) + '</span></div>');
          lapBox.appendChild(row);
        });

        if (!newestFirst.length) {
          lapBox.appendChild(h('<p class="empty-title" style="font-size:17px;margin-top:12px">No laps yet.</p>' +
            '<p class="caption" style="margin:6px 0 0">Lap records a split without stopping the clock. Stop freezes the total.</p>'));
        }

        lapCount.textContent = state.laps.length + ' recorded';
        if (chronological.length) {
          var last = chronological[chronological.length - 1];
          lastView.textContent = 'Last lap ' + mainTime(last.split) + frac(last.split) + ' · total ' + mainTime(last.total) + frac(last.total);
        } else {
          lastView.textContent = 'No lap recorded';
        }
      }

      function paintControls() {
        var isRunning = running();
        var hasTime = elapsed() > 0;
        startBtn.disabled = isRunning;
        startBtn.textContent = hasTime ? 'Resume' : 'Start';
        stopBtn.disabled = !isRunning;
        lapBtn.disabled = !isRunning;
        resetBtn.disabled = !hasTime && !state.laps.length;
        dot.classList.toggle('on', isRunning);
        stateView.textContent = isRunning ? 'Running' : (hasTime ? 'Paused' : 'Idle');
      }

      function paintAll() { paintTime(); paintLaps(); paintControls(); }

      startBtn.addEventListener('click', function () {
        if (running()) return;
        state.startedAt = Date.now();
        persist();
        paintAll();
      });
      stopBtn.addEventListener('click', function () { stop(); paintAll(); });
      lapBtn.addEventListener('click', function () {
        if (!running()) return;
        state.laps.push(elapsed());
        persist();
        paintLaps();
      });
      resetBtn.addEventListener('click', function () {
        state.accumulated = 0;
        state.startedAt = null;
        state.laps = [];
        persist();
        paintAll();
      });

      paintAll();
      setInterval(function () {
        if (running()) { paintTime(); }
      }, 40);
    }
  };

  /* =============================================================================
     shelf rendering
     ========================================================================== */

  var INSTRUMENTS = [CONVERTER, LEDGER, CALCULATOR, QUIZ, CHRONO];
  var shelf = document.querySelector('[data-shelf]');

  INSTRUMENTS.forEach(function (app) {
    var article = h(
      '<article class="instrument" id="i' + app.number + '">' +
        '<header class="instrument-head">' +
          '<span class="legend"><span>' + app.number + ' / ' + app.spec + '</span><span>OIBSIP</span></span>' +
          '<hr class="rule rule--ink">' +
          '<h3>' + app.name + '</h3>' +
        '</header>' +
        '<div class="phone"><div class="screen"></div></div>' +
        '<ul class="edge-cases">' + app.edges.map(function (e) { return '<li>' + e + '</li>'; }).join('') + '</ul>' +
      '</article>');

    shelf.appendChild(article);
    app.mount(article.querySelector('.screen'));
  });
})();
