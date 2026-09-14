/* =====================================================================
 * Q版可爱风 登录/注册
 * 安全说明（务必阅读）：
 *  - 本页为纯前端演示，数据保存在浏览器 localStorage，仅用于展示交互与 UI。
 *  - 密码使用「每用户随机盐 + SHA-256」做哈希后存储，绝不保存明文。
 *  - 真实生产环境必须使用后端：HTTPS 传输、服务端加盐哈希（bcrypt/argon2）、
 *    速率限制、验证码、防暴力破解、JWT/Cookie 会话等。前端哈希不能替代服务端校验。
 *  - 所有用户输入均通过 textContent / 属性赋值输出，避免 XSS。
 * ===================================================================== */

(function () {
  "use strict";

  var USERS_KEY = "aurora_users_v1";
  var SESSION_KEY = "aurora_session_v1";

  /* ---------- 工具 ---------- */
  function $(sel, root) { return (root || document).querySelector(sel); }
  function $all(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function getUsers() {
    try {
      var raw = localStorage.getItem(USERS_KEY);
      var arr = raw ? JSON.parse(raw) : [];
      return Array.isArray(arr) ? arr : [];
    } catch (e) { return []; }
  }
  function saveUsers(users) { localStorage.setItem(USERS_KEY, JSON.stringify(users)); }

  function makeSalt() {
    var bytes;
    if (window.crypto && crypto.getRandomValues) {
      bytes = new Uint8Array(16);
      crypto.getRandomValues(bytes);
    } else {
      bytes = [];
      for (var i = 0; i < 16; i++) bytes.push(Math.floor(Math.random() * 256));
    }
    return Array.prototype.map.call(bytes, function (b) {
      return ("0" + b.toString(16)).slice(-2);
    }).join("");
  }

  /* ---------- 密码哈希 ---------- */
  function derive(salt, password) {
    var s = salt + ":" + password;
    if (window.crypto && crypto.subtle) {
      return crypto.subtle.digest("SHA-256", new TextEncoder().encode(s))
        .then(function (buf) {
          return Array.prototype.map.call(new Uint8Array(buf), function (b) {
            return ("0" + b.toString(16)).padStart(2, "0");
          }).join("");
        });
    }
    return Promise.resolve(fallbackHash(s));
  }
  function fallbackHash(str) {
    var h1 = 0xDEADBEEF, h2 = 0x41C6CE57;
    for (var i = 0; i < str.length; i++) {
      var ch = str.charCodeAt(i);
      h1 = Math.imul(h1 ^ ch, 2654435761);
      h2 = Math.imul(h2 ^ ch, 1597334677);
    }
    h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507) ^ Math.imul(h2 ^ (h2 >>> 13), 3266489909);
    h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507) ^ Math.imul(h1 ^ (h1 >>> 13), 3266489909);
    return (h2 >>> 0).toString(16).padStart(8, "0") + (h1 >>> 0).toString(16).padStart(8, "0");
  }

  /* ---------- 校验 ---------- */
  var RE_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  var RE_USER = /^[A-Za-z0-9_]{3,20}$/;

  function setError(inputEl, msg) {
    var field = inputEl.closest(".field");
    var err = field ? field.querySelector("[data-error]") : null;
    if (field) field.classList.toggle("invalid", !!msg);
    if (err) err.textContent = msg || "";
  }
  function clearFormErrors(form) {
    $all(".field", form).forEach(function (f) { f.classList.remove("invalid"); });
    $all("[data-error]", form).forEach(function (e) { e.textContent = ""; });
  }
  function setMsg(form, text, ok) {
    var el = $("[data-msg]", form);
    if (!el) return;
    el.textContent = text || "";
    el.className = "form-msg" + (text ? (ok ? " ok" : " bad") : "");
  }

  function validateLogin(account, password) {
    if (!account) return "请输入邮箱或用户名";
    if (!password) return "请输入密码";
    if (password.length > 128) return "密码长度异常";
    return "";
  }
  function validateRegister(u, email, pw, confirm) {
    if (!RE_USER.test(u)) return "用户名需为 3-20 位字母、数字或下划线";
    if (!RE_EMAIL.test(email)) return "邮箱格式不正确";
    if (pw.length < 8) return "密码至少 8 位";
    if (!(/[A-Za-z]/.test(pw) && /[0-9]/.test(pw))) return "密码需同时包含字母和数字";
    if (pw.length > 128) return "密码过长";
    if (pw !== confirm) return "两次输入的密码不一致";
    return "";
  }

  /* ---------- 密码强度 ---------- */
  function strengthOf(pw) {
    if (!pw) return 0;
    var score = 0;
    if (pw.length >= 8) score++;
    if (pw.length >= 12) score++;
    if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++;
    if (/[0-9]/.test(pw) && /[^A-Za-z0-9]/.test(pw)) score++;
    return Math.min(score, 4);
  }
  function updateStrength(pw) {
    var box = $("#registerForm .strength");
    if (!box) return;
    var lvl = strengthOf(pw);
    box.className = "strength" + (lvl ? " s" + lvl : "");
    var labels = ["强度", "弱", "一般", "较强", "超强"];
    var lab = box.querySelector(".strength-label");
    if (lab) lab.textContent = labels[lvl];
  }

  /* ---------- 标签页 ---------- */
  var tabs = $(".tabs");
  function switchTab(name) {
    var login = name === "login";
    tabs.setAttribute("data-active", name);
    $all(".tab").forEach(function (t) {
      var active = t.getAttribute("data-tab") === name;
      t.classList.toggle("is-active", active);
      t.setAttribute("aria-selected", active ? "true" : "false");
    });
    $("#loginForm").classList.toggle("is-hidden", !login);
    $("#registerForm").classList.toggle("is-hidden", login);
    $("#brandSub").textContent = login
      ? "登录你的小窝，继续未完成的事"
      : "注册一个新账号，马上出发";
  }
  $all(".tab").forEach(function (t) {
    t.addEventListener("click", function () { switchTab(t.getAttribute("data-tab")); });
  });

  /* ---------- 显示/隐藏密码 ---------- */
  $all(".toggle-pw").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var inp = document.getElementById(btn.getAttribute("data-target"));
      if (!inp) return;
      var show = inp.type === "password";
      inp.type = show ? "text" : "password";
      btn.textContent = show ? "隐藏" : "显示";
      btn.setAttribute("aria-label", show ? "隐藏密码" : "显示密码");
    });
  });

  /* ---------- 吉祥物：输密码时捂眼睛 ---------- */
  var mascot = $("#mascot");
  function updateCover() {
    if (!mascot) return;
    var covering = false;
    ["loginPassword", "regPassword"].forEach(function (id) {
      var inp = document.getElementById(id);
      if (inp && document.activeElement === inp && inp.type === "password") covering = true;
    });
    mascot.classList.toggle("covering", covering);
  }
  ["loginPassword", "regPassword"].forEach(function (id) {
    var inp = document.getElementById(id);
    if (!inp) return;
    inp.addEventListener("focus", updateCover);
    inp.addEventListener("blur", updateCover);
    inp.addEventListener("input", updateCover);
  });

  /* ---------- 彩带庆祝 ---------- */
  var reduced = window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  var Confetti = (function () {
    var canvas, ctx, parts = [], raf = 0;
    var COLORS = ["#ff7eb3", "#ffcf5c", "#5fd4b0", "#7c6cff", "#8fd3ff", "#ff8a5c"];
    function resize() {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    }
    function drawStar(x, y, r, rot) {
      ctx.beginPath();
      for (var i = 0; i < 5; i++) {
        var a = rot + i * 4 * Math.PI / 5 - Math.PI / 2;
        var px = x + Math.cos(a) * r, py = y + Math.sin(a) * r;
        if (i === 0) ctx.moveTo(px, py); else ctx.lineTo(px, py);
      }
      ctx.closePath();
      ctx.fill();
    }
    function tick() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      for (var i = parts.length - 1; i >= 0; i--) {
        var p = parts[i];
        p.vy += 0.12;
        p.vx *= 0.985;
        p.x += p.vx;
        p.y += p.vy;
        p.rot += p.vr;
        p.a -= 0.011;
        if (p.a <= 0 || p.y > canvas.height + 30) { parts.splice(i, 1); continue; }
        ctx.save();
        ctx.globalAlpha = Math.max(p.a, 0);
        ctx.fillStyle = p.color;
        if (p.star) drawStar(p.x, p.y, p.r, p.rot);
        else {
          ctx.beginPath();
          ctx.ellipse(p.x, p.y, p.r, p.r * 0.55, p.rot, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.restore();
      }
      if (parts.length) raf = requestAnimationFrame(tick);
      else { raf = 0; ctx.clearRect(0, 0, canvas.width, canvas.height); }
    }
    return {
      init: function () {
        if (reduced) return;
        canvas = document.getElementById("fx");
        if (!canvas) return;
        ctx = canvas.getContext("2d");
        resize();
        window.addEventListener("resize", resize);
      },
      burst: function (x, y, n) {
        if (reduced || !canvas) return;
        for (var i = 0; i < n; i++) {
          parts.push({
            x: x + (Math.random() - 0.5) * 60,
            y: y + (Math.random() - 0.5) * 30,
            vx: (Math.random() - 0.5) * 7,
            vy: -3 - Math.random() * 4,
            r: 4 + Math.random() * 6,
            rot: Math.random() * Math.PI,
            vr: (Math.random() - 0.5) * 0.25,
            a: 1,
            star: Math.random() < 0.45,
            color: COLORS[(Math.random() * COLORS.length) | 0]
          });
        }
        if (!raf) raf = requestAnimationFrame(tick);
      }
    };
  })();

  function celebrate() {
    var card = document.querySelector(".auth-card:not(.is-hidden)");
    if (!card) return;
    var rect = card.getBoundingClientRect();
    Confetti.burst(rect.left + rect.width / 2, rect.top + rect.height * 0.3, 42);
    card.classList.remove("celebrate");
    void card.offsetWidth;
    card.classList.add("celebrate");
  }

  /* ---------- 会话 ---------- */
  function startSession(username, remember) {
    if (remember) localStorage.setItem(SESSION_KEY, username);
    else sessionStorage.setItem(SESSION_KEY, username);
  }
  function currentUser() {
    var name = localStorage.getItem(SESSION_KEY) || sessionStorage.getItem(SESSION_KEY);
    if (!name) return null;
    var users = getUsers();
    for (var i = 0; i < users.length; i++) {
      if (users[i].username === name || users[i].email === name) return users[i];
    }
    return null;
  }

  function showDashboard(user) {
    $("#authCard").classList.add("is-hidden");
    $("#dashboard").classList.remove("is-hidden");
    var display = user.nickname || user.username;
    $("#dashName").textContent = display;
    $("#dashEmail").textContent = user.email || "";
    $("#dashUser").textContent = user.username;
    $("#dashAvatar").textContent = (display.charAt(0) || "喵").toUpperCase();
    $("#dashSince").textContent = user.createdAt
      ? new Date(user.createdAt).toLocaleDateString("zh-CN") : "—";
  }
  function showAuth() {
    $("#dashboard").classList.add("is-hidden");
    $("#authCard").classList.remove("is-hidden");
    switchTab("login");
  }

  $("#logoutBtn").addEventListener("click", function () {
    localStorage.removeItem(SESSION_KEY);
    sessionStorage.removeItem(SESSION_KEY);
    showAuth();
  });

  /* ---------- 实时强度 ---------- */
  var regPw = $("#regPassword");
  if (regPw) regPw.addEventListener("input", function () { updateStrength(regPw.value); });

  /* ---------- 注册 ---------- */
  $("#registerForm").addEventListener("submit", function (e) {
    e.preventDefault();
    clearFormErrors(this);
    setMsg(this, "", true);

    var u = $("#regUsername").value.trim();
    var email = $("#regEmail").value.trim();
    var pw = $("#regPassword").value;
    var confirm = $("#regConfirm").value;

    var general = validateRegister(u, email, pw, confirm);
    if (general) { setMsg(this, general, false); return; }

    var users = getUsers();
    var lower = email.toLowerCase();
    for (var i = 0; i < users.length; i++) {
      if (users[i].username.toLowerCase() === u.toLowerCase()) {
        setError($("#regUsername"), "这个用户名已经被占啦"); return;
      }
      if (users[i].email.toLowerCase() === lower) {
        setError($("#regEmail"), "这个邮箱已经注册过啦"); return;
      }
    }

    var btn = this.querySelector(".btn-primary");
    btn.disabled = true; btn.textContent = "创建中…";
    var salt = makeSalt();
    derive(salt, pw).then(function (hash) {
      users.push({
        username: u, email: email, salt: salt, pwHash: hash,
        nickname: "", createdAt: Date.now()
      });
      saveUsers(users);
      btn.disabled = false; btn.textContent = "创建账号";
      setMsg(this, "注册成功，快去登录吧！", true);
      this.reset();
      updateStrength("");
      celebrate();
      switchTab("login");
    }.bind(this)).catch(function () {
      btn.disabled = false; btn.textContent = "创建账号";
      setMsg(this, "出了点小状况，请重试", false);
    }.bind(this));
  });

  /* ---------- 登录 ---------- */
  $("#loginForm").addEventListener("submit", function (e) {
    e.preventDefault();
    clearFormErrors(this);
    setMsg(this, "", true);

    var account = $("#loginAccount").value.trim();
    var password = $("#loginPassword").value;
    var general = validateLogin(account, password);
    if (general) { setMsg(this, general, false); return; }

    var users = getUsers();
    var match = null;
    for (var i = 0; i < users.length; i++) {
      if (users[i].username === account ||
          users[i].email.toLowerCase() === account.toLowerCase()) {
        match = users[i]; break;
      }
    }
    if (!match) { setMsg(this, "没有找到这个账号哦", false); return; }

    var btn = this.querySelector(".btn-primary");
    btn.disabled = true; btn.textContent = "登录中…";
    derive(match.salt, password).then(function (hash) {
      btn.disabled = false; btn.textContent = "登录";
      if (hash !== match.pwHash) { setMsg(this, "密码不对，再想想~", false); return; }
      startSession(match.username, $("#rememberMe").checked);
      celebrate();
      showDashboard(match);
    }.bind(this)).catch(function () {
      btn.disabled = false; btn.textContent = "登录";
      setMsg(this, "出了点小状况，请重试", false);
    }.bind(this));
  });

  /* ---------- 初始化 ---------- */
  (function init() {
    Confetti.init();
    var u = currentUser();
    if (u) showDashboard(u);
    else switchTab("login");
  })();
})();
