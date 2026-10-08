/**
 * auth.js — Authentication and Registration handler
 * Campus Event Registration System
 */

document.addEventListener('DOMContentLoaded', () => {
  // If already logged in, redirect to appropriate dashboard
  const isAuthPage = window.location.pathname.includes('login.html') || window.location.pathname.includes('register.html');
  if (isAuthPage && Auth.isLoggedIn()) {
    Auth.redirectIfAuth();
    return;
  }

  // Setup Login Form
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    setupLoginForm(loginForm);
  }

  // Setup Register Form
  const registerForm = document.getElementById('register-form');
  if (registerForm) {
    setupRegisterForm(registerForm);
  }
});

function setupLoginForm(form) {
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email')?.value.trim();
    const password = document.getElementById('password')?.value;
    const rememberMe = document.getElementById('remember-me')?.checked || false;
    const submitBtn = form.querySelector('button[type="submit"]');

    if (!email || !password) {
      showToast('Please enter both email and password.', 'warning');
      return;
    }

    try {
      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span class="spinner-sm"></span> Signing In...';

      const res = await api.post('/api/auth/login', { email, password });

      if (res && res.data) {
        const { token, userId, name, email: userEmail, role, department } = res.data;
        Auth.setSession(token, { id: userId, name, email: userEmail, role, department }, rememberMe);

        showToast(`Welcome back, ${name}!`, 'success');

        setTimeout(() => {
          if (role === 'admin') {
            window.location.href = 'admin/index.html';
          } else {
            window.location.href = 'dashboard.html';
          }
        }, 800);
      }
    } catch (err) {
      showToast(err.message || 'Invalid credentials. Please try again.', 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Sign In</span> <span class="btn-arrow">→</span>';
    }
  });

  // Quick Demo Buttons Setup
  window.fillDemoCredentials = (role) => {
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    if (!emailInput || !passwordInput) return;

    if (role === 'admin') {
      emailInput.value = 'admin@campus.edu';
      passwordInput.value = 'Admin@123';
      showToast('Filled Demo Admin Credentials', 'info');
    } else {
      emailInput.value = 'alex.chen@student.campus.edu';
      passwordInput.value = 'Student@123';
      showToast('Filled Demo Student Credentials', 'info');
    }
  };
}

function setupRegisterForm(form) {
  const passwordInput = document.getElementById('password');
  const confirmPasswordInput = document.getElementById('confirm-password');
  const strengthMeter = document.getElementById('password-strength-bar');
  const strengthText = document.getElementById('password-strength-text');

  if (passwordInput && strengthMeter) {
    passwordInput.addEventListener('input', () => {
      const pwd = passwordInput.value;
      const score = evaluatePasswordStrength(pwd);
      updateStrengthUI(score, strengthMeter, strengthText);
    });
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('name')?.value.trim();
    const email = document.getElementById('email')?.value.trim();
    const password = passwordInput?.value;
    const confirmPassword = confirmPasswordInput?.value;
    const department = document.getElementById('department')?.value;
    const year = parseInt(document.getElementById('year')?.value || '1', 10);
    const phone = document.getElementById('phone')?.value.trim();
    const submitBtn = form.querySelector('button[type="submit"]');

    if (!name || !email || !password || !department) {
      showToast('Please fill out all required fields.', 'warning');
      return;
    }

    if (password.length < 6) {
      showToast('Password must be at least 6 characters long.', 'warning');
      return;
    }

    if (password !== confirmPassword) {
      showToast('Passwords do not match.', 'error');
      return;
    }

    try {
      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span class="spinner-sm"></span> Creating Account...';

      const res = await api.post('/api/auth/register', {
        name,
        email,
        password,
        department,
        year,
        phone
      });

      if (res && res.data) {
        const { token, userId, name: userName, role } = res.data;
        Auth.setSession(token, { id: userId, name: userName, email, role, department }, false);

        showToast('Account created successfully! Welcome to Campus Events.', 'success');
        setTimeout(() => {
          window.location.href = 'dashboard.html';
        }, 1000);
      }
    } catch (err) {
      showToast(err.message || 'Registration failed. Email might already be registered.', 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Create Account</span> <span class="btn-arrow">→</span>';
    }
  });
}

function evaluatePasswordStrength(pwd) {
  if (!pwd) return 0;
  let score = 0;
  if (pwd.length >= 6) score += 25;
  if (pwd.length >= 10) score += 25;
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 25;
  if (/[0-9]/.test(pwd) || /[^A-Za-z0-9]/.test(pwd)) score += 25;
  return score;
}

function updateStrengthUI(score, bar, textEl) {
  bar.style.width = `${score}%`;
  if (score <= 25) {
    bar.style.backgroundColor = 'var(--color-danger)';
    if (textEl) textEl.textContent = 'Weak';
  } else if (score <= 75) {
    bar.style.backgroundColor = 'var(--color-warning)';
    if (textEl) textEl.textContent = 'Medium';
  } else {
    bar.style.backgroundColor = 'var(--color-success)';
    if (textEl) textEl.textContent = 'Strong';
  }
}
