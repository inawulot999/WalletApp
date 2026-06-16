const rates = {
  "NGN:USDT": 0.00060,
  "NGN:USD": 0.00060,
  "NGN:GBP": 0.00048,
  "NGN:GHS": 0.00900,
  "USDT:NGN": 1600.00,
  "USDT:USD": 1.00,
  "USD:NGN": 1600.00,
  "USD:USDT": 1.00,
  "GBP:NGN": 2050.00,
  "GHS:NGN": 105.00
};

const minimumFees = {
  NGN: 500,
  USDT: 1,
  USD: 2,
  GBP: 2,
  GHS: 20
};

const transferModes = {
  CROSS_BORDER: {
    fieldKey: "bank",
    feeRate: 0.015,
    rail: "Bank payout"
  },
  EXCHANGE_WALLET: {
    fieldKey: "exchange",
    feeRate: 0.02,
    rail: "Exchange wallet coming soon",
    comingSoon: true
  },
  DIOR_WALLET_USER: {
    fieldKey: "diorwallet",
    feeRate: 0.005,
    rail: "Dior Wallet UID"
  }
};

let transferType = "CROSS_BORDER";
let appPin = "1234";
let filePickerOpen = false;
let usdtUsdPrice = null;
let usdtTrackerTimer = null;

const USDT_REFRESH_MS = 45000;
const USDT_RETRY_MS = 15000;
const USDT_API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=usd";

const money = new Intl.NumberFormat("en-US", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

function paintIcons() {
  if (window.lucide) {
    window.lucide.createIcons();
  }
}

function formatUsdtPrice(value) {
  return value.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4
  });
}

function setUsdtStatus(message) {
  const statusElement = document.querySelector("#usdtPriceStatus");
  if (statusElement) {
    statusElement.textContent = message;
  }
}

function setUsdtIndicator(direction, label) {
  const indicator = document.querySelector("#usdtPriceIndicator");
  if (!indicator) {
    return;
  }

  const iconName = direction === "up" ? "trending-up" : direction === "down" ? "trending-down" : "minus";
  indicator.classList.remove("is-up", "is-down", "is-neutral");
  indicator.classList.add(direction === "up" ? "is-up" : direction === "down" ? "is-down" : "is-neutral");
  indicator.innerHTML = `<i data-lucide="${iconName}"></i><span>${label}</span>`;
  paintIcons();
}

function setUsdtTimestamp(date = new Date()) {
  const timestampElement = document.querySelector("#usdtLastUpdated");
  if (!timestampElement) {
    return;
  }

  timestampElement.textContent = date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit"
  });
}

function applyUsdtRateToQuotes(price) {
  const usdNgnRate = rates["USD:NGN"] || 1600;
  const usdtNgnRate = usdNgnRate / price;
  rates["USDT:USD"] = price;
  rates["USD:USDT"] = 1 / price;
  rates["USDT:NGN"] = usdtNgnRate;
  rates["NGN:USDT"] = 1 / usdtNgnRate;
  calculateQuote();
  calculateSwap();
}

function updateUsdtPriceView(nextPrice) {
  const priceValue = document.querySelector("#usdtPriceValue");
  if (!priceValue) {
    return;
  }

  priceValue.textContent = `$${formatUsdtPrice(nextPrice)}`;
  if (usdtUsdPrice === null) {
    setUsdtIndicator("neutral", "First live update");
  } else if (nextPrice > usdtUsdPrice) {
    setUsdtIndicator("up", "Price increased");
  } else if (nextPrice < usdtUsdPrice) {
    setUsdtIndicator("down", "Price decreased");
  } else {
    setUsdtIndicator("neutral", "Price unchanged");
  }

  usdtUsdPrice = nextPrice;
  setUsdtTimestamp();
  setUsdtStatus("Live price synced");
  applyUsdtRateToQuotes(nextPrice);
}

async function fetchUsdtPrice() {
  const response = await fetch(USDT_API_URL, {
    headers: {
      accept: "application/json"
    }
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  const payload = await response.json();
  const price = Number(payload?.tether?.usd);
  if (!Number.isFinite(price) || price <= 0) {
    throw new Error("Invalid price payload");
  }

  return price;
}

function scheduleUsdtRefresh(delayMs) {
  if (usdtTrackerTimer) {
    window.clearTimeout(usdtTrackerTimer);
  }

  usdtTrackerTimer = window.setTimeout(() => {
    void refreshUsdtPrice();
  }, delayMs);
}

async function refreshUsdtPrice() {
  try {
    const latestPrice = await fetchUsdtPrice();
    updateUsdtPriceView(latestPrice);
    scheduleUsdtRefresh(USDT_REFRESH_MS);
  } catch {
    setUsdtStatus("Price feed unavailable, retrying...");
    scheduleUsdtRefresh(USDT_RETRY_MS);
  }
}

function startUsdtTracker() {
  setUsdtStatus("Loading latest price...");
  setUsdtIndicator("neutral", "Waiting for update");
  void refreshUsdtPrice();
}

function setTheme(theme) {
  const nextTheme = theme === "light" ? "light" : "dark";
  document.documentElement.dataset.theme = nextTheme;
  localStorage.setItem("diorwallet-theme", nextTheme);

  const themeToggle = document.querySelector(".theme-toggle");
  if (themeToggle) {
    themeToggle.innerHTML = `<i data-lucide="${nextTheme === "dark" ? "sun" : "moon"}"></i>`;
    themeToggle.setAttribute("aria-label", `Switch to ${nextTheme === "dark" ? "light" : "dark"} theme`);
    paintIcons();
  }
}

function updateViewUI(view) {
  document.querySelectorAll("[data-panel]").forEach((panel) => {
    const isVisible = panel.dataset.panel === view;
    panel.classList.toggle("is-visible", isVisible);
    panel.setAttribute("aria-hidden", String(!isVisible));
    if (isVisible) {
      panel.removeAttribute("inert");
    } else {
      panel.setAttribute("inert", "");
    }
  });

  document.querySelectorAll("[data-view], [data-jump]").forEach((button) => {
    const target = button.dataset.view || button.dataset.jump;
    button.classList.toggle("is-active", target === view && button.dataset.view);
  });

  if (window.location.hash.slice(1) !== view) {
    window.history.replaceState(null, "", `#${view}`);
  }
}

function setView(view) {
  const apply = () => updateViewUI(view);

  if (document.startViewTransition && !window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
    document.startViewTransition(apply);
  } else {
    apply();
  }
}

function setTransferType(type) {
  transferType = transferModes[type] ? type : "CROSS_BORDER";
  const mode = transferModes[transferType];

  document.querySelectorAll("[data-transfer-type]").forEach((item) => {
    item.classList.toggle("is-active", item.dataset.transferType === transferType);
  });

  document.querySelectorAll("[data-destination-group]").forEach((group) => {
    group.classList.toggle("is-active", group.dataset.destinationGroup === transferType);
  });

  document.querySelectorAll("[data-destination-field]").forEach((field) => {
    const allowedModes = field.dataset.destinationField.split(" ");
    field.hidden = !allowedModes.includes(mode.fieldKey);
  });

  calculateQuote();
}

function calculateQuote() {
  const sourceAmount = Number(document.querySelector("#sourceAmount").value || 0);
  const sourceCurrency = document.querySelector("#sourceCurrency").value;
  const targetCurrency = document.querySelector("#targetCurrency").value;
  const mode = transferModes[transferType];
  const feeRate = mode.feeRate;
  const minimumFee = minimumFees[sourceCurrency] || 1;
  const fee = Math.max(sourceAmount * feeRate, minimumFee);
  const rate = sourceCurrency === targetCurrency ? 1 : rates[`${sourceCurrency}:${targetCurrency}`] || 0;
  const targetAmount = Math.max(sourceAmount - fee, 0) * rate;

  document.querySelector("#quoteAmount").textContent = `${targetCurrency} ${money.format(targetAmount)}`;
  document.querySelector("#quoteSource").textContent = `${sourceCurrency} ${money.format(sourceAmount)}`;
  document.querySelector("#quoteFee").textContent = `${sourceCurrency} ${money.format(fee)}`;
  document.querySelector("#quoteRate").textContent = rate ? rate.toFixed(sourceCurrency === "NGN" ? 5 : 2) : "Unavailable";
  document.querySelector("#quoteRail").textContent = mode.rail;

  const submitButton = document.querySelector(".transfer-form .submit-button");
  const submitLabel = submitButton.querySelector("span");
  submitButton.disabled = Boolean(mode.comingSoon);
  submitButton.classList.toggle("is-disabled", Boolean(mode.comingSoon));
  submitLabel.textContent = mode.comingSoon ? "Exchange coming soon" : "Simulate transfer";

  document.querySelector("#transferRiskTitle").textContent = mode.comingSoon ? "Coming soon" : "KYC gate passed";
  document.querySelector("#transferRiskCopy").textContent = mode.comingSoon
    ? "Exchange wallet transfers are not active in this sandbox yet."
    : "Transfer remains simulated until licensed rails are connected.";
}

function formatCurrency(amount, currency) {
  return `${currency} ${money.format(amount)}`;
}

function oppositeSwapCurrency(currency) {
  return currency === "NGN" ? "USDT" : "NGN";
}

function syncSwapDirection(changedField) {
  const fromField = document.querySelector("#swapFromCurrency");
  const toField = document.querySelector("#swapToCurrency");

  if (fromField.value === toField.value) {
    if (changedField === "to") {
      fromField.value = oppositeSwapCurrency(toField.value);
    } else {
      toField.value = oppositeSwapCurrency(fromField.value);
    }
  }
}

function calculateSwap() {
  const amount = Number(document.querySelector("#swapAmount").value || 0);
  const fromCurrency = document.querySelector("#swapFromCurrency").value;
  const toCurrency = document.querySelector("#swapToCurrency").value;
  const feeMinimum = fromCurrency === "NGN" ? 100 : 0.5;
  const fee = Math.max(amount * 0.004, feeMinimum);
  const rate = rates[`${fromCurrency}:${toCurrency}`] || 0;
  const receiveAmount = Math.max(amount - fee, 0) * rate;

  document.querySelector("#swapReceiveInput").value = money.format(receiveAmount);
  document.querySelector("#swapReceive").textContent = formatCurrency(receiveAmount, toCurrency);
  document.querySelector("#swapSource").textContent = formatCurrency(amount, fromCurrency);
  document.querySelector("#swapFee").textContent = formatCurrency(fee, fromCurrency);
  document.querySelector("#swapRate").textContent = rate ? rate.toFixed(fromCurrency === "NGN" ? 5 : 2) : "Unavailable";
  document.querySelector("#swapPair").textContent = `${fromCurrency}/${toCurrency}`;
}

function initialsFromName(name) {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0] || "")
    .join("")
    .toUpperCase() || "WA";
}

function saveProfilePreview(showToast = true) {
  const name = document.querySelector("#profileFullName").value.trim() || "Dior Wallet User";
  const username = document.querySelector("#profileUsername").value.trim() || "@user";
  const email = document.querySelector("#profileEmail").value.trim() || "user@example.com";
  const phone = document.querySelector("#profilePhone").value.trim() || "+234";
  const country = document.querySelector("#profileCountry").value;
  const initials = initialsFromName(name);

  document.querySelectorAll(".profile-name").forEach((item) => {
    item.textContent = name;
  });
  document.querySelectorAll(".profile-avatar").forEach((item) => {
    item.textContent = initials;
  });
  document.querySelector(".profile-username").textContent = username;
  document.querySelector(".profile-email").textContent = email;
  document.querySelector(".profile-phone").textContent = phone;
  document.querySelector(".profile-country").textContent = country;

  const toast = document.querySelector(".profile-toast");
  toast.hidden = !showToast;
  paintIcons();
}

function setProfilePhoto(dataUrl) {
  document.querySelectorAll(".profile-avatar").forEach((item) => {
    item.style.backgroundImage = dataUrl ? `url("${dataUrl}")` : "";
    item.classList.toggle("has-image", Boolean(dataUrl));
  });
  if (dataUrl) {
    localStorage.setItem("diorwallet-profile-photo", dataUrl);
  }
}

function securityState() {
  return {
    twoFactor: document.querySelector("#twoFactorToggle").checked,
    fingerprint: document.querySelector("#fingerprintToggle").checked,
    pinLock: document.querySelector("#pinLockToggle").checked,
    immediateLock: document.querySelector("#immediateLockToggle").checked
  };
}

function enabledLabel(value) {
  return value ? "Enabled" : "Off";
}

function saveSecuritySettings() {
  const pin = document.querySelector("#pinInput").value.trim();
  const confirmPin = document.querySelector("#pinConfirmInput").value.trim();
  const securityMessage = document.querySelector("#securityMessage");

  if (pin !== confirmPin || pin.length < 4) {
    securityMessage.textContent = "PIN must match and be at least 4 digits";
    securityMessage.hidden = false;
    return;
  }

  appPin = pin;
  localStorage.setItem("diorwallet-pin", appPin);
  securityMessage.textContent = "Security settings saved";
  securityMessage.hidden = false;
  updateSecurityPreview();
}

function updateSecurityPreview() {
  const state = securityState();
  document.querySelector("#twoFactorStatus").textContent = enabledLabel(state.twoFactor);
  document.querySelector("#fingerprintStatus").textContent = enabledLabel(state.fingerprint);
  document.querySelector("#pinLockStatus").textContent = enabledLabel(state.pinLock);
  document.querySelector("#immediateLockStatus").textContent = enabledLabel(state.immediateLock);

  const enabledCount = Object.values(state).filter(Boolean).length;
  document.querySelector(".security-score").textContent = `${enabledCount}/4`;
}

function lockApp() {
  const overlay = document.querySelector("#lockOverlay");
  document.querySelector("#unlockPin").value = "";
  document.querySelector("#unlockError").hidden = true;
  overlay.hidden = false;
  document.querySelector("#unlockPin").focus();
  paintIcons();
}

function unlockApp() {
  document.querySelector("#lockOverlay").hidden = true;
  document.querySelector("#unlockError").hidden = true;
}

function unlockWithPin() {
  const state = securityState();
  const enteredPin = document.querySelector("#unlockPin").value.trim();
  const error = document.querySelector("#unlockError");

  if (!state.pinLock || enteredPin === appPin) {
    unlockApp();
    return;
  }

  error.textContent = "Incorrect PIN";
  error.hidden = false;
}

function unlockWithFingerprint() {
  const error = document.querySelector("#unlockError");
  if (securityState().fingerprint) {
    unlockApp();
    return;
  }

  error.textContent = "Fingerprint unlock is off";
  error.hidden = false;
}

function animateTabClick(tab) {
  const wrapper = tab.querySelector(".tab-icon-wrapper");
  if (!wrapper || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
    return;
  }

  wrapper.classList.remove("animate-click");
  void wrapper.offsetWidth;
  wrapper.classList.add("animate-click");
}

document.querySelectorAll(".mobile-tab").forEach((tab) => {
  tab.addEventListener("mousedown", () => animateTabClick(tab));
});

document.querySelectorAll("[data-view], [data-jump]").forEach((button) => {
  button.addEventListener("click", () => {
    if (button.dataset.transferJump) {
      setTransferType(button.dataset.transferJump);
    }
    setView(button.dataset.view || button.dataset.jump);
  });
});

document.querySelectorAll("[data-transfer-type]").forEach((button) => {
  button.addEventListener("click", () => {
    setTransferType(button.dataset.transferType);
  });
});

document.querySelectorAll("#sourceAmount, #sourceCurrency, #targetCurrency").forEach((field) => {
  field.addEventListener("input", calculateQuote);
  field.addEventListener("change", calculateQuote);
});

document.querySelector(".transfer-form .submit-button").addEventListener("click", () => {
  setView("activity");
});

document.querySelector(".swap-form .submit-button").addEventListener("click", () => {
  setView("activity");
});

document.querySelector(".swap-switch").addEventListener("click", () => {
  const fromField = document.querySelector("#swapFromCurrency");
  const toField = document.querySelector("#swapToCurrency");
  const oldFrom = fromField.value;
  fromField.value = toField.value;
  toField.value = oldFrom;
  calculateSwap();
});

document.querySelector("#swapAmount").addEventListener("input", calculateSwap);
document.querySelector("#swapFromCurrency").addEventListener("change", () => {
  syncSwapDirection("from");
  calculateSwap();
});
document.querySelector("#swapToCurrency").addEventListener("change", () => {
  syncSwapDirection("to");
  calculateSwap();
});

document.querySelector(".profile-save").addEventListener("click", () => saveProfilePreview(true));

document.querySelector("#profilePhotoInput").addEventListener("click", () => {
  filePickerOpen = true;
});

document.querySelector("#profilePhotoInput").addEventListener("change", (event) => {
  filePickerOpen = false;
  const [file] = event.target.files;
  if (!file) {
    return;
  }

  const reader = new FileReader();
  reader.addEventListener("load", () => setProfilePhoto(reader.result));
  reader.readAsDataURL(file);
});

document.querySelectorAll("#twoFactorToggle, #fingerprintToggle, #pinLockToggle, #immediateLockToggle").forEach((toggle) => {
  toggle.addEventListener("change", updateSecurityPreview);
});

document.querySelector(".security-save").addEventListener("click", saveSecuritySettings);

document.querySelectorAll(".lock-now").forEach((button) => {
  button.addEventListener("click", lockApp);
});

document.querySelector(".unlock-pin").addEventListener("click", unlockWithPin);
document.querySelector(".unlock-fingerprint").addEventListener("click", unlockWithFingerprint);
document.querySelector("#unlockPin").addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    unlockWithPin();
  }
});

window.addEventListener("visibilitychange", () => {
  if (document.hidden && securityState().immediateLock && !filePickerOpen) {
    lockApp();
  }
});

window.addEventListener("focus", () => {
  filePickerOpen = false;
});

document.querySelector(".theme-toggle").addEventListener("click", () => {
  const currentTheme = document.documentElement.dataset.theme || "dark";
  setTheme(currentTheme === "dark" ? "light" : "dark");
});

const startupHash = window.location.hash.slice(1);
const [startupView, startupHashSearch = ""] = startupHash.split("?");
const startupParams = new URLSearchParams(window.location.search || startupHashSearch);
setTheme(startupParams.get("theme") || localStorage.getItem("diorwallet-theme") || "dark");
setView(startupView || "dashboard");
setTransferType(startupParams.get("mode") || "CROSS_BORDER");
calculateQuote();
syncSwapDirection("from");
calculateSwap();
saveProfilePreview(false);
appPin = localStorage.getItem("diorwallet-pin") || appPin;
document.querySelector("#pinInput").value = appPin;
document.querySelector("#pinConfirmInput").value = appPin;
setProfilePhoto(localStorage.getItem("diorwallet-profile-photo"));
updateSecurityPreview();
if (startupParams.get("locked") === "true") {
  lockApp();
}
startUsdtTracker();
paintIcons();

window.addEventListener("beforeunload", () => {
  if (usdtTrackerTimer) {
    window.clearTimeout(usdtTrackerTimer);
  }
});
