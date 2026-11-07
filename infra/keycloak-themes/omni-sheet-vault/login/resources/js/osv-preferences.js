// Applies the web app's theme to the login pages and runs the language menu; both share the app's cookies.
(function () {
  var YEAR = 60 * 60 * 24 * 365;

  function readCookie(name) {
    var match = document.cookie.split('; ').find(function (entry) {
      return entry.indexOf(name + '=') === 0;
    });
    return match ? decodeURIComponent(match.slice(name.length + 1)) : null;
  }

  function writeCookie(name, value, maxAge) {
    document.cookie = name + '=' + encodeURIComponent(value) + '; path=/; max-age=' + maxAge + '; samesite=lax';
  }

  function prefersDark() {
    var chosen = readCookie('osv_theme');
    if (chosen === 'dark' || chosen === 'light') {
      return chosen === 'dark';
    }
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  if (prefersDark()) {
    document.documentElement.setAttribute('data-osv-theme', 'dark');
    document.documentElement.classList.add('pf-v5-theme-dark');
  }

  function setUpLanguageMenu() {
    var toggle = document.getElementById('osv-language-toggle');
    var menu = document.getElementById('osv-language-menu');
    if (!toggle || !menu) {
      return;
    }
    function setOpen(open) {
      menu.hidden = !open;
      toggle.setAttribute('aria-expanded', String(open));
    }
    toggle.addEventListener('click', function () {
      setOpen(menu.hidden);
    });
    document.addEventListener('mousedown', function (event) {
      if (!menu.hidden && !toggle.parentNode.contains(event.target)) {
        setOpen(false);
      }
    });
    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        setOpen(false);
      }
    });
    menu.querySelectorAll('[data-locale]').forEach(function (option) {
      option.addEventListener('click', function () {
        writeCookie('osv_locale', option.getAttribute('data-locale'), YEAR);
        writeCookie('osv_locale_picked', '1', YEAR);
      });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setUpLanguageMenu);
  } else {
    setUpLanguageMenu();
  }
})();
