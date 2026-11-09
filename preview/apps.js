/* =============================================================================
   Browser prototypes for the five OIBSIP Android apps.

   Each phone mounts one module. The maths and state machines mirror the Java
   versions closely enough to demo the screens, and everything is wired with
   plain DOM calls so the file stays readable and dependency free.
   ============================================================================= */

(function () {
  'use strict';

  /* ------------------------------------------------------------------ helpers */

  var ICON = {
    converter:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M4 8h13l-3-3M20 16H7l3 3"/></svg>',
    tasks:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><rect x="4.2" y="3.4" width="15.6" height="17.2" rx="4"/><path d="M8 8.6h8M8 12.2h8M8 15.8h4.6"/></svg>',
    calculator:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><rect x="4.6" y="3" width="14.8" height="18" rx="4.4"/><path d="M8.2 7.6h7.6"/><path d="M8.4 12h.01M12 12h.01M15.6 12h.01M8.4 16h.01M12 16h.01M15.6 16h.01"/></svg>',
    quiz:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M20.4 12.4c0 4.1-3.7 7.2-8.2 7.2-1 0-2-.15-2.9-.42L4.4 20.5l1.3-3.7c-1-1.2-1.6-2.7-1.6-4.4C4.1 8.3 7.8 5.2 12.2 5.2s8.2 3.1 8.2 7.2z"/><path d="M9.7 11.4a2.4 2.4 0 1 1 3.2 2.3v1"/><path d="M12.9 17.1h.01"/></svg>',
    chrono:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="13.4" r="7.4"/><path d="M12 10v3.6l2.4 1.6M9.6 3.4h4.8M12 3.4v2.6M18.9 7.1l1.3-1.3"/></svg>',
    swap:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M7 4.6L3.6 8 7 11.4M3.6 8h11.6M17 19.4L20.4 16 17 12.6M20.4 16H8.8"/></svg>',
    plus:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round"><path d="M12 5.5v13M5.5 12h13"/></svg>',
    check:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4.8 12.4l4.6 4.6L19.2 7.2"/></svg>',
    trash:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M4.6 7.2h14.8M9.4 7.2V5.4h5.2v1.8M6.6 7.2l.9 11a2 2 0 0 0 2 1.9h5a2 2 0 0 0 2-1.9l.9-11"/></svg>',
    close:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"><path d="M6.4 6.4l11.2 11.2M17.6 6.4L6.4 17.6"/></svg>',
    backspace:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9.2 5.4h9.4a2.4 2.4 0 0 1 2.4 2.4v8.4a2.4 2.4 0 0 1-2.4 2.4H9.2L2.9 12z"/><path d="M11.8 9.6l5 4.8M16.8 9.6l-5 4.8"/></svg>',
    restart:
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M4.4 12a7.6 7.6 0 1 0 2.6-5.7"/><path d="M4.4 4.6V10h5.4"/></svg>'
  };

  function render(html) {
    var tpl = document.createElement('template');
    tpl.innerHTML = html.trim();
    return tpl.content.firstElementChild;
  }

  function cash(root, selector) {
    return root.querySelector(selector);
  }

  function pad2(n) {
    return n < 10 ? '0' + n : String(n);
  }

  function toast(el) {
    // Re-trigger the little fade each time a value changes.
    el.classList.remove('fade-in');
    void el.offsetWidth;
    el.classList.add('fade-in');
  }

  /* ------------------------------------------------------------------ numbers */

  function groupThousands(raw) {
    var negative = raw.charAt(0) === '-';
    var body = negative ? raw.slice(1) : raw;
    var dot = body.indexOf('.');
    var whole = dot < 0 ? body : body.slice(0, dot);
    var frac = dot < 0 ? '' : body.slice(dot);
    if (whole.length > 3) {
      whole = whole.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }
    return (negative ? '-' : '') + whole + frac;
  }

  /* Six decimals at most, scientific at the extremes, same as ConverterEngine. */
  function tidy(value) {
    if (!isFinite(value)) {
      return 'Out of range';
    }
    var abs = Math.abs(value);
    if (abs === 0) {
      return '0';
    }
    if (abs >= 1e12 || abs < 1e-9) {
      return value.toExponential(6);
    }
    var text = value.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    return groupThousands(text);
  }

  /* ------------------------------------------------------------------ apps */

  var mount = {
    converter: mountConverter,
    tasks: mountTasks,
    calculator: mountCalculator,
    quiz: mountQuiz,
    stopwatch: mountStopwatch
  };

  function bootPhones() {
    document.querySelectorAll('.phone[data-app]').forEach(function (phone) {
      var name = phone.getAttribute('data-app');
      var screen = render(
        '<div class="screen">' +
          '<div class="statusbar">' +
            '<span>9:41</span>' +
            '<span class="bars"><i></i><i></i><i></i><span class="battery"></span></span>' +
          '</div>' +
          '<div class="app"></div>' +
        '</div>'
      );
      phone.appendChild(screen);
      if (mount[name]) {
        mount[name](cash(screen, '.app'));
      }
    });
  }

  /* ================================================================= converter */

  var CATEGORIES = {
    Length: {
      note: '8 units. Metric and imperial.',
      units: [
        ['Millimetre', 0.001, 'mm'],
        ['Centimetre', 0.01, 'cm'],
        ['Metre', 1, 'm'],
        ['Kilometre', 1000, 'km'],
        ['Inch', 0.0254, 'in'],
        ['Foot', 0.3048, 'ft'],
        ['Yard', 0.9144, 'yd'],
        ['Mile', 1609.344, 'mi']
      ]
    },
    Weight: {
      note: '7 units. Metric and imperial.',
      units: [
        ['Milligram', 0.001, 'mg'],
        ['Gram', 1, 'g'],
        ['Kilogram', 1000, 'kg'],
        ['Ounce', 28.349523125, 'oz'],
        ['Pound', 453.59237, 'lb'],
        ['Stone', 6350.29318, 'st'],
        ['Tonne', 1000000, 't']
      ]
    },
    Volume: {
      note: '8 units. Metric and US customary.',
      units: [
        ['Millilitre', 0.001, 'ml'],
        ['Litre', 1, 'l'],
        ['Cubic metre', 1000, 'm\u00B3'],
        ['Teaspoon', 0.00492892159375, 'tsp'],
        ['Tablespoon', 0.01478676478125, 'tbsp'],
        ['Fluid ounce', 0.0295735295625, 'fl oz'],
        ['Cup', 0.2365882365, 'cup'],
        ['Gallon', 3.785411784, 'gal']
      ]
    },
    Temperature: {
      note: '3 scales. Celsius, Fahrenheit and Kelvin.',
      units: [['Celsius', 0, '\u00B0C'], ['Fahrenheit', 0, '\u00B0F'], ['Kelvin', 0, 'K']]
    }
  };

  function symbolOf(category, name) {
    var found = CATEGORIES[category].units.filter(function (u) { return u[0] === name; })[0];
    return found ? found[2] : name;
  }

  function toCelsius(value, unit) {
    if (unit === 'Fahrenheit') {
      return (value - 32) * 5 / 9;
    }
    if (unit === 'Kelvin') {
      return value - 273.15;
    }
    return value;
  }

  function fromCelsius(value, unit) {
    if (unit === 'Fahrenheit') {
      return value * 9 / 5 + 32;
    }
    if (unit === 'Kelvin') {
      return value + 273.15;
    }
    return value;
  }

  function convertValue(value, category, fromName, toName) {
    if (category === 'Temperature') {
      return fromCelsius(toCelsius(value, fromName), toName);
    }
    var list = CATEGORIES[category].units;
    var from = list.filter(function (u) { return u[0] === fromName; })[0][1];
    var to = list.filter(function (u) { return u[0] === toName; })[0][1];
    return value * from / to;
  }

  function parseInput(raw) {
    var text = raw.trim();
    if (!text) {
      return null;
    }
    if (text.indexOf(',') > -1 && text.indexOf('.') < 0) {
      text = text.replace(',', '.');
    } else {
      text = text.replace(/,/g, '');
    }
    if (!/^[+-]?(\d+(\.\d*)?|\.\d+)$/.test(text)) {
      return null;
    }
    return parseFloat(text);
  }

  function mountConverter(root) {
    var state = {
      category: 'Length',
      from: 'Centimetre',
      to: 'Inch',
      hasResult: false
    };

    root.innerHTML =
      '<header class="app-head">' +
        '<span class="tile">' + ICON.converter + '</span>' +
        '<div><div class="app-title">Converter</div>' +
        '<div class="app-sub">Length, weight, volume and temperature</div></div>' +
      '</header>' +
      '<div class="row-of-chips" style="margin-top:14px">' +
        Object.keys(CATEGORIES).map(function (name) {
          return '<button class="chip" type="button" data-cat="' + name + '">' + name + '</button>';
        }).join('') +
      '</div>' +
      '<div class="card" style="margin-top:12px">' +
        '<p class="label">Value</p>' +
        '<input class="field" data-value inputmode="decimal" placeholder="Enter a number" style="margin-top:6px" />' +
        '<div class="conv-pair">' +
          '<div class="conv-side"><p class="label">From</p><select class="field" data-from></select></div>' +
          '<button class="iconbtn" type="button" data-swap aria-label="Swap units">' + ICON.swap + '</button>' +
          '<div class="conv-side"><p class="label">To</p><select class="field" data-to></select></div>' +
        '</div>' +
        '<p class="label" data-note style="margin-top:11px;text-transform:none;letter-spacing:.02em"></p>' +
      '</div>' +
      '<div class="card result-card">' +
        '<p class="label">Result</p>' +
        '<div class="result-value" data-result>0</div>' +
        '<div class="result-line" data-sentence>Enter a value and press Convert.</div>' +
      '</div>' +
      '<p class="errorline" data-error></p>' +
      '<div class="dock">' +
        '<button class="pill pill--primary pill--block" type="button" data-convert>Convert</button>' +
      '</div>';

    var input = cash(root, '[data-value]');
    var fromSel = cash(root, '[data-from]');
    var toSel = cash(root, '[data-to]');
    var result = cash(root, '[data-result]');
    var sentence = cash(root, '[data-sentence]');
    var note = cash(root, '[data-note]');
    var error = cash(root, '[data-error]');

    function units() {
      return CATEGORIES[state.category].units.map(function (u) { return u[0]; });
    }

    function fillSelects() {
      var list = units();
      fromSel.innerHTML = list.map(function (n) { return '<option>' + n + '</option>'; }).join('');
      toSel.innerHTML = fromSel.innerHTML;
      fromSel.value = state.from;
      toSel.value = state.to;
    }

    function paintChips() {
      Array.prototype.forEach.call(root.querySelectorAll('[data-cat]'), function (chip) {
        chip.setAttribute('aria-pressed', String(chip.getAttribute('data-cat') === state.category));
      });
    }

    function convert(loud) {
      error.textContent = '';
      var raw = input.value;
      if (!raw.trim()) {
        if (loud) {
          error.textContent = 'Enter a value to convert.';
        }
        return;
      }
      var value = parseInput(raw);
      if (value === null) {
        if (loud) {
          error.textContent = 'That is not a number.';
        }
        return;
      }
      if (Math.abs(value) > 1e15) {
        if (loud) {
          error.textContent = 'That number is too large to convert.';
        }
        return;
      }
      if (state.category === 'Temperature' && toCelsius(value, state.from) < -273.15 - 1e-9) {
        if (loud) {
          error.textContent = 'Below absolute zero. The lowest temperature is -273.15 °C.';
        }
        return;
      }

      var out = convertValue(value, state.category, state.from, state.to);
      result.textContent = tidy(out);
      sentence.textContent = tidy(value) + ' ' + symbolOf(state.category, state.from) +
        ' = ' + tidy(out) + ' ' + symbolOf(state.category, state.to);
      state.hasResult = true;
      toast(result);
    }

    function setCategory(name) {
      if (state.category === name) {
        return;
      }
      state.category = name;
      var list = units();
      state.from = list[0];
      state.to = list[1];
      state.hasResult = false;
      result.textContent = '0';
      sentence.textContent = 'Enter a value and press Convert.';
      error.textContent = '';
      note.textContent = CATEGORIES[name].note;
      paintChips();
      fillSelects();
      toast(result);
    }

    Array.prototype.forEach.call(root.querySelectorAll('[data-cat]'), function (chip) {
      chip.addEventListener('click', function () {
        setCategory(chip.getAttribute('data-cat'));
      });
    });

    fromSel.addEventListener('change', function () {
      state.from = fromSel.value;
      if (state.hasResult) {
        convert(false);
      }
    });
    toSel.addEventListener('change', function () {
      state.to = toSel.value;
      if (state.hasResult) {
        convert(false);
      }
    });

    cash(root, '[data-swap]').addEventListener('click', function () {
      var held = state.from;
      state.from = state.to;
      state.to = held;
      fromSel.value = state.from;
      toSel.value = state.to;
      if (state.hasResult) {
        convert(false);
      }
    });

    input.addEventListener('input', function () {
      error.textContent = '';
      if (state.hasResult) {
        convert(false);
      }
    });
    input.addEventListener('keydown', function (event) {
      if (event.key === 'Enter') {
        convert(true);
      }
    });

    cash(root, '[data-convert]').addEventListener('click', function () {
      convert(true);
    });

    note.textContent = CATEGORIES[state.category].note;
    paintChips();
    fillSelects();
  }

  /* ===================================================================== tasks */

  function mountTasks(root) {
    var db = {
      users: {},
      tasks: [],
      nextTaskId: 1,
      session: null,
      view: 'login',
      filter: 'All'
    };

    function stamp(at) {
      var d = new Date(at);
      var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      return d.getDate() + ' ' + months[d.getMonth()] + ', ' + pad2(d.getHours()) + ':' + pad2(d.getMinutes());
    }

    function mine() {
      return db.tasks.filter(function (t) {
        return t.owner === db.session;
      });
    }

    function counts() {
      var list = mine();
      var done = list.filter(function (t) { return t.done; }).length;
      return { open: list.length - done, done: done };
    }

    function visible() {
      return mine().filter(function (t) {
        if (db.filter === 'Open') { return !t.done; }
        if (db.filter === 'Done') { return t.done; }
        return true;
      }).sort(function (a, b) {
        if (a.done !== b.done) { return a.done ? 1 : -1; }
        return b.at - a.at;
      });
    }

    function normalise(email) {
      return email.trim().toLowerCase();
    }

    function seedDemo() {
      var email = 'demo@example.com';
      if (db.users[email]) {
        return email;
      }
      db.users[email] = { name: 'Antonio', password: 'oibsip1234' };
      [
        ['Finish the quiz screen', 'Check the note line under the options'],
        ['Water the plants', ''],
        ['Read for twenty minutes', '']
      ].forEach(function (pair, i) {
        db.tasks.push({
          id: db.nextTaskId++,
          owner: email,
          title: pair[0],
          note: pair[1],
          done: i === 2,
          at: Date.now() - (i + 1) * 3600000
        });
      });
      return email;
    }

    function shell(body) {
      root.innerHTML = body;
    }

    function signIn(email, password) {
      var key = normalise(email);
      var account = db.users[key];
      if (!account || account.password !== password) {
        return false;
      }
      db.session = key;
      db.view = 'list';
      db.filter = 'All';
      renderList();
      return true;
    }

    function renderLogin(message) {
      db.view = 'login';
      shell(
        '<div class="screen-views">' +
          '<div class="auth-head">' +
            '<span class="tile" style="width:52px;height:52px">' + ICON.tasks + '</span>' +
            '<h1 class="auth-title">Welcome back</h1>' +
            '<p class="auth-sub">Sign in to open your list.</p>' +
          '</div>' +
          '<div class="card auth-form">' +
            '<div><p class="label">Email</p>' +
              '<input class="field" data-email type="email" placeholder="you@example.com" style="margin-top:5px" /></div>' +
            '<div><p class="label">Password</p>' +
              '<input class="field" data-password type="password" placeholder="Your password" style="margin-top:5px" /></div>' +
            '<p class="errorline" data-error style="margin-top:0">' + (message || '') + '</p>' +
            '<button class="pill pill--primary pill--block" type="button" data-signin>Sign in</button>' +
          '</div>' +
          '<button class="plain-link" type="button" data-register>New here? Create an account</button>' +
          '<p style="font-size:11px;color:var(--ink-faint);text-align:center;margin-top:auto">' +
            'Demo account: <strong>demo@example.com</strong> / <strong>oibsip1234</strong></p>' +
        '</div>'
      );

      var emailField = cash(root, '[data-email]');
      var passwordField = cash(root, '[data-password]');
      var error = cash(root, '[data-error]');

      function submit() {
        var email = emailField.value;
        var password = passwordField.value;
        if (!email.trim()) {
          error.textContent = 'Enter your email.';
          return;
        }
        if (!password) {
          error.textContent = 'Enter your password.';
          return;
        }
        if (!signIn(email, password)) {
          error.textContent = 'Email or password is incorrect.';
          passwordField.value = '';
        }
      }

      cash(root, '[data-signin]').addEventListener('click', submit);
      passwordField.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') { submit(); }
      });
      cash(root, '[data-register]').addEventListener('click', function () {
        renderRegister();
      });
    }

    function renderRegister(message) {
      db.view = 'register';
      shell(
        '<div class="screen-views">' +
          '<div class="auth-head">' +
            '<h1 class="auth-title" style="margin-top:0">Create account</h1>' +
            '<p class="auth-sub">Each account keeps its own tasks on this phone.</p>' +
          '</div>' +
          '<div class="card auth-form">' +
            '<div><p class="label">Name</p>' +
              '<input class="field" data-name placeholder="Your name" style="margin-top:5px" /></div>' +
            '<div><p class="label">Email</p>' +
              '<input class="field" data-email type="email" placeholder="you@example.com" style="margin-top:5px" /></div>' +
            '<div><p class="label">Password</p>' +
              '<input class="field" data-password type="password" placeholder="At least 8 characters" style="margin-top:5px" /></div>' +
            '<div><p class="label">Confirm password</p>' +
              '<input class="field" data-confirm type="password" placeholder="Type it again" style="margin-top:5px" /></div>' +
            '<p class="errorline" data-error style="margin-top:0">' + (message || '') + '</p>' +
            '<button class="pill pill--primary pill--block" type="button" data-create>Create account</button>' +
          '</div>' +
          '<button class="plain-link" type="button" data-back>Already have an account? Sign in</button>' +
        '</div>'
      );

      var error = cash(root, '[data-error]');
      var name = cash(root, '[data-name]');
      var email = cash(root, '[data-email]');
      var password = cash(root, '[data-password]');
      var confirm = cash(root, '[data-confirm]');

      cash(root, '[data-create]').addEventListener('click', function () {
        var key = normalise(email.value);
        if (!name.value.trim()) {
          error.textContent = 'Enter your name.';
          return;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(key)) {
          error.textContent = 'That email does not look right.';
          return;
        }
        if (password.value.length < 8) {
          error.textContent = 'Use at least 8 characters.';
          return;
        }
        if (password.value !== confirm.value) {
          error.textContent = 'The passwords do not match.';
          return;
        }
        if (db.users[key]) {
          error.textContent = 'That email is already registered.';
          return;
        }
        db.users[key] = { name: name.value.trim(), password: password.value };
        db.session = key;
        renderList();
      });

      cash(root, '[data-back]').addEventListener('click', function () {
        renderLogin();
      });
    }

    function renderList() {
      db.view = 'list';
      var account = db.users[db.session] || { name: 'You' };
      var c = counts();
      var list = visible();

      shell(
        '<div class="screen-views">' +
          '<header class="app-head">' +
            '<div style="flex:1"><div class="app-title">Your tasks</div>' +
            '<div class="countline">' + c.open + ' open, ' + c.done + ' done</div></div>' +
            '<button class="pill pill--ghost pill--small" type="button" data-logout>Log out</button>' +
          '</header>' +
          '<div class="row-of-chips" style="margin-top:12px">' +
            ['All', 'Open', 'Done'].map(function (name) {
              return '<button class="chip" type="button" data-filter="' + name + '" aria-pressed="' +
                (db.filter === name) + '">' + name + '</button>';
            }).join('') +
          '</div>' +
          (list.length ?
            '<div class="task-list" data-list>' + list.map(taskRow).join('') + '</div>' :
            '<div class="empty"><span class="tile">' + ICON.check + '</span>' +
              '<div><strong>' + (db.filter === 'All' ? 'Nothing here yet' :
                db.filter === 'Open' ? 'All clear' : 'Nothing finished') + '</strong>' +
              '<p>' + (db.filter === 'All' ? 'Tap the plus button to write down the first thing.' :
                db.filter === 'Open' ? 'Everything on the list is finished.' :
                'Tasks show up here once you tick them off.') + '</p></div></div>') +
          '<button class="fab" type="button" data-add aria-label="New task">' + ICON.plus + '</button>' +
        '</div>'
      );

      Array.prototype.forEach.call(root.querySelectorAll('[data-filter]'), function (chip) {
        chip.addEventListener('click', function () {
          db.filter = chip.getAttribute('data-filter');
          renderList();
        });
      });

      Array.prototype.forEach.call(root.querySelectorAll('[data-toggle]'), function (tick) {
        tick.addEventListener('click', function () {
          var id = Number(tick.getAttribute('data-toggle'));
          db.tasks.forEach(function (t) {
            if (t.id === id) {
              t.done = !t.done;
              t.at = Date.now();
            }
          });
          renderList();
        });
      });

      Array.prototype.forEach.call(root.querySelectorAll('[data-delete]'), function (button) {
        button.addEventListener('click', function () {
          var id = Number(button.getAttribute('data-delete'));
          confirmDelete(id);
        });
      });

      cash(root, '[data-add]').addEventListener('click', addSheet);
      cash(root, '[data-logout]').addEventListener('click', function () {
        db.session = null;
        renderLogin('Signed out.');
      });
    }

    function taskRow(task) {
      return '<article class="task' + (task.done ? ' is-done' : '') + '">' +
        '<button class="tick" type="button" data-toggle="' + task.id + '" aria-label="' +
          (task.done ? 'Mark as open' : 'Mark as done') + '">' + ICON.check + '</button>' +
        '<div class="task-body">' +
          '<div class="task-title">' + escapeHtml(task.title) + '</div>' +
          (task.note ? '<div class="task-note">' + escapeHtml(task.note) + '</div>' : '') +
          '<div class="task-stamp">' + (task.done ? 'Done' : 'Added') + ' ' + stamp(task.at) + '</div>' +
        '</div>' +
        '<button class="iconbtn" type="button" data-delete="' + task.id + '" aria-label="Delete">' +
          ICON.trash + '</button>' +
      '</article>';
    }

    function escapeHtml(text) {
      return String(text).replace(/[&<>"]/g, function (ch) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[ch];
      });
    }

    function addSheet() {
      var scrim = render(
        '<div class="scrim"><div class="sheet" role="dialog" aria-label="New task">' +
          '<h3>New task</h3>' +
          '<div><p class="label">Task</p>' +
            '<input class="field" data-title placeholder="What needs doing?" style="margin-top:5px" /></div>' +
          '<div><p class="label">Note (optional)</p>' +
            '<textarea class="field" data-note-input placeholder="Anything worth remembering" style="margin-top:5px"></textarea></div>' +
          '<p class="errorline" data-sheet-error style="margin-top:0"></p>' +
          '<div class="sheet-actions">' +
            '<button class="pill pill--ghost" type="button" data-cancel>Cancel</button>' +
            '<button class="pill pill--primary" type="button" data-save>Add</button>' +
          '</div>' +
        '</div></div>'
      );
      root.appendChild(scrim);
      var title = cash(scrim, '[data-title]');
      var note = cash(scrim, '[data-note-input]');
      var error = cash(scrim, '[data-sheet-error]');
      title.focus();

      function close() {
        scrim.remove();
      }

      cash(scrim, '[data-cancel]').addEventListener('click', close);
      scrim.addEventListener('click', function (event) {
        if (event.target === scrim) { close(); }
      });
      cash(scrim, '[data-save]').addEventListener('click', function () {
        if (!title.value.trim()) {
          error.textContent = 'Give the task a name.';
          return;
        }
        db.tasks.push({
          id: db.nextTaskId++,
          owner: db.session,
          title: title.value.trim(),
          note: note.value.trim(),
          done: false,
          at: Date.now()
        });
        db.filter = 'All';
        close();
        renderList();
      });
    }

    function confirmDelete(id) {
      var task = db.tasks.filter(function (t) { return t.id === id; })[0];
      if (!task) {
        return;
      }
      var scrim = render(
        '<div class="scrim"><div class="sheet" role="dialog" aria-label="Delete task">' +
          '<h3>Delete this task?</h3>' +
          '<p>' + escapeHtml(task.title) + '</p>' +
          '<div class="sheet-actions">' +
            '<button class="pill pill--ghost" type="button" data-cancel>Cancel</button>' +
            '<button class="pill pill--primary" type="button" data-confirm>Delete</button>' +
          '</div>' +
        '</div></div>'
      );
      root.appendChild(scrim);
      cash(scrim, '[data-cancel]').addEventListener('click', function () {
        scrim.remove();
      });
      cash(scrim, '[data-confirm]').addEventListener('click', function () {
        db.tasks = db.tasks.filter(function (t) { return t.id !== id; });
        scrim.remove();
        renderList();
      });
    }

    seedDemo();
    renderLogin();
    db.previewSignIn = function () { return signIn('demo@example.com', 'oibsip1234'); };
  }

  /* ================================================================ calculator */

  function mountCalculator(root) {
    var MAX_TOKENS = 24;
    var MAX_OPERAND = 14;
    var OPS = { '+': '+', '−': '−', '×': '×', '÷': '÷' };

    var tokens = [];
    var operand = '';
    var resultRaw = null;
    var lastExpression = '';
    var evaluated = false;
    var errorReason = '';

    root.innerHTML =
      '<header class="app-head">' +
        '<span class="tile">' + ICON.calculator + '</span>' +
        '<div style="flex:1"><div class="app-title" style="font-size:19px">Calculator</div></div>' +
        '<p class="label">Tap a number</p>' +
      '</header>' +
      '<div class="card calc-card">' +
        '<div class="calc-tape" data-tape>Ready</div>' +
        '<div class="calc-readout" data-readout>0</div>' +
      '</div>' +
      '<div class="keypad">' +
        '<div class="key-row">' +
          '<button class="key key--action" data-key="C" type="button">C</button>' +
          '<button class="key key--action" data-key="del" type="button" aria-label="Delete last digit">' + ICON.backspace + '</button>' +
          '<button class="key key--action" data-key="%" type="button">%</button>' +
          '<button class="key key--op" data-key="÷" type="button">÷</button>' +
        '</div>' +
        keyRow(['7', '8', '9', '×']) +
        keyRow(['4', '5', '6', '−']) +
        keyRow(['1', '2', '3', '+']) +
        '<div class="key-row">' +
          '<button class="key key--action" data-key="sign" type="button">±</button>' +
          '<button class="key" data-key="0" type="button">0</button>' +
          '<button class="key" data-key="." type="button">.</button>' +
          '<button class="key key--equals" data-key="=" type="button">=</button>' +
        '</div>' +
      '</div>';

    function keyRow(keys) {
      return '<div class="key-row">' + keys.map(function (key) {
        var op = OPS[key];
        return '<button class="key' + (op ? ' key--op' : '') + '" data-key="' + key + '" type="button">' + key + '</button>';
      }).join('') + '</div>';
    }

    var readout = cash(root, '[data-readout]');
    var tape = cash(root, '[data-tape]');

    function pushDigit(digit) {
      clearError();
      if (evaluated) { reset(); }
      if (operand.length >= MAX_OPERAND) { return; }
      if (operand === '0') { operand = digit; }
      else if (operand === '-0') { operand = '-' + digit; }
      else { operand += digit; }
      paint();
    }

    function pushDot() {
      clearError();
      if (evaluated) { reset(); }
      if (operand.indexOf('.') > -1 || operand.length >= MAX_OPERAND) { return; }
      operand = operand ? operand + '.' : '0.';
      paint();
    }

    function pushOperator(op) {
      if (errorReason) { return; }
      if (evaluated) {
        tokens = resultRaw ? [resultRaw] : [];
        operand = '';
        lastExpression = '';
        evaluated = false;
      }
      if (operand) {
        tokens.push(operand);
        operand = '';
      }
      if (!tokens.length) { tokens.push('0'); }
      if (tokens.length >= MAX_TOKENS) { return; }
      var last = tokens[tokens.length - 1];
      if (OPS[last]) { tokens[tokens.length - 1] = op; }
      else { tokens.push(op); }
      paint();
    }

    function pushEquals() {
      if (errorReason) { return; }
      if (operand) {
        tokens.push(operand);
        operand = '';
      }
      while (tokens.length && OPS[tokens[tokens.length - 1]]) {
        tokens.pop();
      }
      if (!tokens.length) { paint(); return; }
      var expression = tokens.join(' ');
      try {
        var value = evaluate(tokens);
        resultRaw = tidy(value).replace(/,/g, '');
        lastExpression = expression + ' =';
        tokens = [];
        evaluated = true;
        paint();
        toast(readout);
      } catch (err) {
        enterError(err.message);
      }
    }

    function evaluate(list) {
      if (list.length % 2 === 0) { throw new Error('Number is out of range'); }
      var numbers = [];
      var operators = [];
      list.forEach(function (token, i) {
        if (i % 2 === 0) {
          var n = parseFloat(token.replace(/,/g, '').replace('−', '-'));
          if (isNaN(n)) { throw new Error('Number is out of range'); }
          numbers.push(n);
        } else {
          operators.push(token);
        }
      });

      var folded = [numbers[0]];
      var additions = [];
      operators.forEach(function (op, i) {
        var next = numbers[i + 1];
        var last = folded.length - 1;
        if (op === '×') {
          folded[last] = folded[last] * next;
        } else if (op === '÷') {
          if (next === 0) { throw new Error('Cannot divide by zero'); }
          folded[last] = folded[last] / next;
        } else {
          additions.push(op);
          folded.push(next);
        }
      });

      var total = folded[0];
      additions.forEach(function (op, i) {
        total = op === '+' ? total + folded[i + 1] : total - folded[i + 1];
      });
      if (!isFinite(total)) { throw new Error('Number is out of range'); }
      return total;
    }

    function clearError() {
      if (errorReason) {
        errorReason = '';
        reset();
      }
    }

    function reset() {
      tokens = [];
      operand = '';
      resultRaw = null;
      lastExpression = '';
      evaluated = false;
    }

    function enterError(reason) {
      errorReason = reason;
      reset();
      paint();
      toast(readout);
    }

    function paint() {
      var big;
      if (errorReason) {
        big = 'Error';
      } else if (operand) {
        big = groupThousands(operand);
      } else if (evaluated && resultRaw !== null) {
        big = groupThousands(resultRaw);
      } else if (tokens.length && !OPS[tokens[tokens.length - 1]]) {
        big = groupThousands(tokens[tokens.length - 1]);
      } else {
        big = resultRaw ? groupThousands(resultRaw) : '0';
      }

      var line;
      if (errorReason) { line = errorReason; }
      else if (evaluated) { line = lastExpression; }
      else if (tokens.length) { line = tokens.join(' '); }
      else { line = 'Ready'; }

      readout.textContent = big;
      tape.textContent = line;
    }

    Array.prototype.forEach.call(root.querySelectorAll('[data-key]'), function (button) {
      button.addEventListener('click', function () {
        var key = button.getAttribute('data-key');
        if (/^[0-9]$/.test(key)) { pushDigit(key); }
        else if (key === '.') { pushDot(); }
        else if (key === '=') { pushEquals(); }
        else if (key === 'C') { reset(); clearError(); paint(); }
        else if (key === 'del') { backspace(); }
        else if (key === 'sign') { flipSign(); }
        else if (key === '%') { percent(); }
        else { pushOperator(key); }
      });
    });

    function backspace() {
      if (errorReason || evaluated) {
        reset();
        errorReason = '';
        paint();
        return;
      }
      if (operand) {
        operand = operand.slice(0, -1);
        if (operand === '-') { operand = ''; }
      } else if (tokens.length) {
        var removed = tokens.pop();
        if (!OPS[removed]) {
          operand = removed.length > 1 ? removed.slice(0, -1) : '';
        }
      }
      paint();
    }

    function flipSign() {
      clearError();
      if (evaluated) {
        if (resultRaw === null) { return; }
        resultRaw = resultRaw.charAt(0) === '-' ? resultRaw.slice(1) : '-' + resultRaw;
        paint();
        return;
      }
      if (!operand) { return; }
      operand = operand.charAt(0) === '-' ? operand.slice(1) : '-' + operand;
      paint();
    }

    function percent() {
      clearError();
      var source = operand || (evaluated ? resultRaw : null);
      if (!source) { return; }
      var value = parseFloat(source);
      if (isNaN(value)) { return; }
      operand = String(value / 100);
      evaluated = false;
      paint();
    }

    paint();
  }

  /* ====================================================================== quiz */

  var QUESTIONS = [
    {
      prompt: 'Which planet has the shortest day?',
      options: ['Jupiter', 'Mercury', 'Mars', 'Venus'],
      correct: 0,
      note: 'Jupiter turns once in just under ten hours, the fastest of the eight planets.'
    },
    {
      prompt: 'What is the capital of Portugal?',
      options: ['Porto', 'Lisbon', 'Coimbra', 'Braga'],
      correct: 1,
      note: 'Lisbon has been the capital since the thirteenth century.'
    },
    {
      prompt: 'Which gas makes up most of the air we breathe?',
      options: ['Oxygen', 'Carbon dioxide', 'Nitrogen', 'Argon'],
      correct: 2,
      note: 'Nitrogen is about 78 percent of the atmosphere. Oxygen is roughly 21.'
    },
    {
      prompt: 'Who wrote the play "A Midsummer Night\'s Dream"?',
      options: ['Christopher Marlowe', 'William Shakespeare', 'Ben Jonson', 'John Webster'],
      correct: 1,
      note: 'Shakespeare wrote it around 1595 and it was printed in 1600.'
    },
    {
      prompt: 'How many strings does a standard violin have?',
      options: ['Four', 'Five', 'Six', 'Seven'],
      correct: 0,
      note: 'G, D, A and E, tuned in fifths.'
    },
    {
      prompt: 'What does the chemical symbol Fe stand for?',
      options: ['Fluorine', 'Iron', 'Lead', 'Francium'],
      correct: 1,
      note: 'Fe comes from ferrum, the Latin word for iron.'
    },
    {
      prompt: 'Which ocean is the largest by area?',
      options: ['Atlantic', 'Indian', 'Pacific', 'Arctic'],
      correct: 2,
      note: 'The Pacific covers about a third of the planet on its own.'
    },
    {
      prompt: 'In which year did the first iPhone go on sale?',
      options: ['2005', '2006', '2007', '2008'],
      correct: 2,
      note: 'It went on sale in June 2007, after being announced in January.'
    },
    {
      prompt: 'What is the longest river in Africa?',
      options: ['Congo', 'Niger', 'Zambezi', 'Nile'],
      correct: 3,
      note: 'The Nile runs about 6,650 kilometres from source to sea.'
    },
    {
      prompt: 'Which shape has eight sides?',
      options: ['Hexagon', 'Heptagon', 'Octagon', 'Nonagon'],
      correct: 2,
      note: 'Octo is eight in Latin. A hexagon has six sides.'
    },
    {
      prompt: 'Who painted "The Starry Night"?',
      options: ['Claude Monet', 'Vincent van Gogh', 'Paul Cezanne', 'Edvard Munch'],
      correct: 1,
      note: 'Van Gogh painted it in 1889 while staying at Saint-Remy.'
    },
    {
      prompt: 'How many bits are in one byte?',
      options: ['4', '8', '16', '32'],
      correct: 1,
      note: 'A byte is eight bits, which gives 256 possible values.'
    }
  ];

  function mountQuiz(root) {
    var deck = [];
    var index = 0;
    var answers = [];
    var view = 'welcome';

    root.innerHTML = '<div class="quiz-body" data-stage></div>';

    function deal() {
      var pool = QUESTIONS.slice();
      for (var i = pool.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var held = pool[i];
        pool[i] = pool[j];
        pool[j] = held;
      }
      deck = pool.slice(0, 10).map(function (question) {
        var order = question.options.map(function (text, i) { return { text: text, i: i }; });
        for (var k = order.length - 1; k > 0; k--) {
          var m = Math.floor(Math.random() * (k + 1));
          var swap = order[k];
          order[k] = order[m];
          order[m] = swap;
        }
        return {
          prompt: question.prompt,
          options: order.map(function (o) { return o.text; }),
          correct: order.map(function (o) { return o.i; }).indexOf(question.correct),
          note: question.note
        };
      });
      index = 0;
      answers = [];
    }

    function renderWelcome() {
      view = 'welcome';
      root.innerHTML =
        '<div class="screen-views" style="justify-content:center">' +
          '<span class="tile" style="width:58px;height:58px">' + ICON.quiz + '</span>' +
          '<h1 class="auth-title">Ten questions</h1>' +
          '<p class="auth-sub" style="max-width:30ch">General knowledge, one card at a time. ' +
            'You see the answer the moment you pick.</p>' +
          '<div class="card" style="margin-top:16px;display:grid;gap:12px">' +
            ['10 questions', '4 choices each', 'Feedback straight away'].map(function (line) {
              return '<div style="display:flex;align-items:center;gap:10px;font-size:13px">' +
                '<span style="color:var(--accent-ink);display:grid;place-items:center;width:18px">' +
                '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" ' +
                'stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M4.8 12.4l4.6 4.6L19.2 7.2"/></svg>' +
                '</span>' + line + '</div>';
            }).join('') +
          '</div>' +
          '<div class="dock">' +
            '<button class="pill pill--primary pill--block" type="button" data-start>Start the quiz</button>' +
            '<p class="label" style="text-align:center;text-transform:none;letter-spacing:.02em">No timer. Take your time.</p>' +
          '</div>' +
        '</div>';

      cash(root, '[data-start]').addEventListener('click', function () {
        deal();
        renderQuestion();
      });
    }

    function renderQuestion() {
      view = 'question';
      var card = deck[index];
      var answered = answers[index] !== undefined;
      var chosen = answers[index];

      var progress = ((index + (answered ? 1 : 0)) / deck.length) * 100;

      root.innerHTML =
        '<div class="screen-views">' +
          '<div class="quiz-top">' +
            '<div style="display:flex;align-items:center">' +
              '<p class="label" style="flex:1;color:var(--accent-ink)">Question ' + (index + 1) +
                ' of ' + deck.length + '</p>' +
              '<button class="iconbtn" type="button" data-quit aria-label="Leave the quiz">' + ICON.close + '</button>' +
            '</div>' +
            '<div class="quiz-progress"><span style="width:' + progress + '%"></span></div>' +
          '</div>' +
          '<div class="quiz-body">' +
            '<div class="quiz-question">' + card.prompt + '</div>' +
            '<div class="options">' + card.options.map(function (text, i) {
              var cls = 'option';
              var disabled = '';
              if (answered) {
                disabled = ' disabled';
                if (i === card.correct) { cls += ' is-correct'; }
                else if (i === chosen) { cls += ' is-wrong'; }
                else { cls += ' is-muted'; }
              }
              return '<button class="' + cls + '" type="button" data-option="' + i + '"' + disabled + '>' +
                text + '</button>';
            }).join('') + '</div>' +
            '<p class="quiz-note" data-note>' + (answered ? 'Why: ' + card.note : 'Pick one answer') + '</p>' +
          '</div>' +
          '<div class="dock">' +
            '<button class="pill pill--primary pill--block" type="button" data-next' +
              (answered ? '' : ' disabled') + '>' +
              (index === deck.length - 1 ? 'See results' : 'Next question') + '</button>' +
          '</div>' +
        '</div>';

      Array.prototype.forEach.call(root.querySelectorAll('[data-option]'), function (button) {
        button.addEventListener('click', function () {
          if (answers[index] !== undefined) { return; }
          answers[index] = Number(button.getAttribute('data-option'));
          renderQuestion();
        });
      });

      cash(root, '[data-next]').addEventListener('click', function () {
        if (answers[index] === undefined) { return; }
        if (index < deck.length - 1) {
          index++;
          renderQuestion();
        } else {
          renderResult();
        }
      });

      cash(root, '[data-quit]').addEventListener('click', function () {
        renderWelcome();
      });
    }

    function renderResult() {
      view = 'result';
      var correct = deck.reduce(function (sum, card, i) {
        return sum + (answers[i] === card.correct ? 1 : 0);
      }, 0);
      var total = deck.length;
      var percent = Math.round((correct / total) * 100);
      var verdict = correct === total ? 'All ten. Nothing left to fix.' :
        correct / total >= 0.8 ? 'Strong round. A miss or two at most.' :
        correct / total >= 0.55 ? 'About half. The notes are worth a second read.' :
        correct / total >= 0.3 ? 'A few landed. Read the notes and run it again.' :
        'Rough round. Another pass will help.';

      root.innerHTML =
        '<div class="screen-views" style="justify-content:center">' +
          '<span class="tile" style="width:54px;height:54px">' + ICON.quiz + '</span>' +
          '<p class="label" style="margin-top:14px">Your result</p>' +
          '<div class="score-hero"><span class="score-number">' + correct +
            '</span><span class="score-of">of ' + total + '</span>' +
            '<p class="verdict">' + verdict + '</p></div>' +
          '<div class="card breakdown">' +
            '<div><div class="n n--good">' + correct + '</div><div class="k">Correct</div></div>' +
            '<div><div class="n n--bad">' + (total - correct) + '</div><div class="k">Incorrect</div></div>' +
            '<div><div class="n">' + percent + '%</div><div class="k">Accuracy</div></div>' +
          '</div>' +
          '<div class="dock">' +
            '<button class="pill pill--primary pill--block" type="button" data-again>Play again</button>' +
            '<button class="pill pill--ghost pill--block" type="button" data-home>Back to start</button>' +
          '</div>' +
        '</div>';

      cash(root, '[data-again]').addEventListener('click', function () {
        deal();
        renderQuestion();
      });
      cash(root, '[data-home]').addEventListener('click', renderWelcome);
    }

    renderWelcome();
  }

  /* ================================================================= stopwatch */

  function mountStopwatch(root) {
    var state = {
      accumulated: 0,
      startedAt: 0,
      running: false,
      laps: []
    };
    var frame = null;

    root.innerHTML =
      '<header class="app-head">' +
        '<span class="tile">' + ICON.chrono + '</span>' +
        '<div><div class="app-title">Stopwatch</div>' +
        '<div class="app-sub">Hundredths, kept across app switches</div></div>' +
      '</header>' +
      '<div class="card watch-card">' +
        '<div class="watch-state"><span class="dot"></span><span data-state>Ready</span></div>' +
        '<div class="watch-time" data-time>00:00<span class="frac">.00</span></div>' +
        '<div class="watch-last" data-last>No laps yet</div>' +
      '</div>' +
      '<div class="lap-head"><p class="label">Laps</p><p class="label" data-count></p></div>' +
      '<div class="laps" data-laps><p class="laps-empty">Laps you record will line up here.</p></div>' +
      '<div class="watch-controls">' +
        '<div class="line">' +
          '<button class="pill pill--primary" type="button" data-start>Start</button>' +
          '<button class="pill pill--glass" type="button" data-pause disabled>Pause</button>' +
        '</div>' +
        '<div class="line">' +
          '<button class="pill pill--glass" type="button" data-lap disabled>Lap</button>' +
          '<button class="pill pill--ghost" type="button" data-reset disabled>Reset</button>' +
        '</div>' +
      '</div>';

    var timeView = cash(root, '[data-time]');
    var stateView = cash(root, '[data-state]');
    var lastView = cash(root, '[data-last]');
    var countView = cash(root, '[data-count]');
    var lapsView = cash(root, '[data-laps]');
    var startButton = cash(root, '[data-start]');
    var pauseButton = cash(root, '[data-pause]');
    var lapButton = cash(root, '[data-lap]');
    var resetButton = cash(root, '[data-reset]');
    var card = cash(root, '.watch-card');

    function elapsed() {
      return state.accumulated + (state.running ? performance.now() - state.startedAt : 0);
    }

    function clockText(ms) {
      var safe = Math.max(0, ms);
      var totalSeconds = Math.floor(safe / 1000);
      var hours = Math.floor(totalSeconds / 3600);
      var minutes = Math.floor((totalSeconds % 3600) / 60);
      var seconds = totalSeconds % 60;
      var hundredths = Math.floor((safe % 1000) / 10);
      var main = hours > 0
        ? hours + ':' + pad2(minutes) + ':' + pad2(seconds)
        : pad2(minutes) + ':' + pad2(seconds);
      return main + '.' + pad2(hundredths);
    }

    function shortClock(ms) {
      var safe = Math.max(0, ms);
      var totalSeconds = Math.floor(safe / 1000);
      var hours = Math.floor(totalSeconds / 3600);
      var minutes = Math.floor((totalSeconds % 3600) / 60);
      var seconds = totalSeconds % 60;
      var hundredths = Math.floor((safe % 1000) / 10);
      var main = hours > 0
        ? hours + ':' + pad2(minutes) + ':' + pad2(seconds)
        : pad2(minutes) + ':' + pad2(seconds);
      return main + '.' + pad2(hundredths);
    }

    function paintTime() {
      var text = clockText(elapsed());
      var cut = text.lastIndexOf('.');
      timeView.innerHTML = text.slice(0, cut) + '<span class="frac">' + text.slice(cut) + '</span>';
    }

    function paintLaps() {
      countView.textContent = state.laps.length === 1 ? '1 lap' : state.laps.length + ' laps';

      if (!state.laps.length) {
        lapsView.innerHTML = '<p class="laps-empty">Laps you record will line up here.</p>';
        lastView.textContent = 'No laps yet';
        return;
      }

      var chronological = state.laps.slice();
      var best = -1;
      var worst = -1;
      if (chronological.length > 1) {
        var splits = chronological.map(function (lap) { return lap.split; });
        best = splits.indexOf(Math.min.apply(null, splits));
        worst = splits.indexOf(Math.max.apply(null, splits));
        if (best === worst) { worst = -1; }
      }

      var newestFirst = chronological.map(function (lap, i) {
        return { lap: lap, index: i };
      }).reverse();

      lapsView.innerHTML = newestFirst.map(function (entry) {
        var tag = '';
        if (entry.index === best) { tag = '<span class="tag tag--best">Best</span>'; }
        else if (entry.index === worst) { tag = '<span class="tag tag--worst">Slowest</span>'; }
        return '<div class="lap">' +
          '<span class="n">L' + (entry.index + 1) + '</span>' +
          '<span class="split">' + shortClock(entry.lap.split) + '</span>' +
          tag +
          '<span class="total">' + shortClock(entry.lap.total) + '</span>' +
        '</div>';
      }).join('');

      var last = chronological[chronological.length - 1];
      lastView.textContent = 'Last lap ' + shortClock(last.split) + ', total ' + shortClock(last.total);
    }

    function paintControls() {
      var running = state.running;
      var total = elapsed();

      card.classList.toggle('is-running', running);
      stateView.textContent = running ? 'Running' : (total > 0 ? 'Paused' : 'Ready');

      startButton.disabled = running;
      startButton.textContent = total > 0 ? 'Resume' : 'Start';
      pauseButton.disabled = !running;
      lapButton.disabled = !running;
      resetButton.disabled = total === 0 && !state.laps.length;
    }

    function tick() {
      paintTime();
      frame = requestAnimationFrame(tick);
    }

    function start() {
      if (state.running) { return; }
      state.running = true;
      state.startedAt = performance.now();
      paintControls();
      frame = requestAnimationFrame(tick);
    }

    function pause() {
      if (!state.running) { return; }
      state.accumulated = elapsed();
      state.running = false;
      cancelAnimationFrame(frame);
      paintTime();
      paintControls();
    }

    function reset() {
      cancelAnimationFrame(frame);
      state.running = false;
      state.accumulated = 0;
      state.startedAt = 0;
      state.laps = [];
      paintTime();
      paintLaps();
      paintControls();
    }

    function lap() {
      if (!state.running) { return; }
      var total = elapsed();
      var previous = state.laps.length ? state.laps[state.laps.length - 1].total : 0;
      state.laps.push({ split: total - previous, total: total });
      paintLaps();
      paintControls();
    }

    startButton.addEventListener('click', start);
    pauseButton.addEventListener('click', pause);
    resetButton.addEventListener('click', reset);
    lapButton.addEventListener('click', lap);

    paintTime();
    paintLaps();
    paintControls();
  }

  /* ================================================================= page bits */

  var toggle = document.getElementById('themeToggle');
  if (toggle) {
    toggle.addEventListener('click', function () {
      var next = document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', next);
      try { localStorage.setItem('oibsip-theme', next); } catch (err) { /* private mode */ }
    });
    try {
      var saved = localStorage.getItem('oibsip-theme');
      if (saved === 'light' || saved === 'dark') {
        document.documentElement.setAttribute('data-theme', saved);
      }
    } catch (err) { /* nothing to restore */ }
  }

  bootPhones();

  var revealables = document.querySelectorAll('[data-reveal]');
  if ('IntersectionObserver' in window) {
    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-in');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
    revealables.forEach(function (node) { observer.observe(node); });
  } else {
    revealables.forEach(function (node) { node.classList.add('is-in'); });
  }
})();
