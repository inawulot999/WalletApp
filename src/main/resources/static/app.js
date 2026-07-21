const API_ROOT = window.location.origin;
const DEMO_KYC = {
  bvn: "22790575513",
  nin: "52267738961",
  residentialAddress: "Oyo State, Nigeria"
};
const NETWORKS = {
  TRC20: {
    name: "TRON",
    fee: 1,
    pattern: /^T[1-9A-HJ-NP-Za-km-z]{33}$/
  },
  ERC20: {
    name: "Ethereum",
    fee: 5,
    pattern: /^0x[a-fA-F0-9]{40}$/
  },
  BEP20: {
    name: "BNB Smart Chain",
    fee: 0.3,
    pattern: /^0x[a-fA-F0-9]{40}$/
  },
  POLYGON: {
    name: "Polygon",
    fee: 0.1,
    pattern: /^0x[a-fA-F0-9]{40}$/
  }
};

let currentUser = null;
let balances = [];
let addresses = [];
let pendingSend = null;
let marketSocket = null;
let fallbackPriceTimer = null;
let reconnectTimer = null;
let reconnectAttempts = 0;
let priceEngineStarted = false;
const marketState = new Map();

const money = new Intl.NumberFormat("en-US", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

function $(selector) {
  return document.querySelector(selector);
}

function all(selector) {
  return Array.from(document.querySelectorAll(selector));
}

function paintIcons() {
  if (window.lucide) {
    window.lucide.createIcons();
  }
}

function setButtonLoading(button, loading, label) {
  if (!button) {
    return;
  }
  button.disabled = loading;
  if (loading) {
    button.dataset.originalHtml = button.innerHTML;
    button.innerHTML = `<span class="spinner"></span><span>${label}</span>`;
  } else if (button.dataset.originalHtml) {
    button.innerHTML = button.dataset.originalHtml;
    delete button.dataset.originalHtml;
    paintIcons();
  }
}

function formatUsdt(value) {
  return `${Number(value || 0).toLocaleString("en-US", {
    minimumFractionDigits: 8,
    maximumFractionDigits: 8
  })} USDT`;
}

function formatPrice(value) {
  return `$${Number(value || 0).toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  })}`;
}

function showToast(message, type = "success") {
  const toast = $("#toast");
  toast.textContent = message;
  toast.className = `toast is-${type}`;
  toast.hidden = false;
  window.clearTimeout(showToast.timeout);
  showToast.timeout = window.setTimeout(() => {
    toast.hidden = true;
  }, 4200);
}

async function api(path, options = {}) {
  const response = await fetch(`${API_ROOT}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(payload.message || payload.error || `Request failed (${response.status})`);
  }
  return payload;
}

function switchAuth(mode) {
  all("[data-auth-tab]").forEach((tab) => {
    tab.classList.toggle("is-active", tab.dataset.authTab === mode);
  });
  $("#loginForm").classList.toggle("is-active", mode === "login");
  $("#registerForm").classList.toggle("is-active", mode === "register");
  $("#authMessage").hidden = true;
}

function showAuthMessage(message, type = "error") {
  const element = $("#authMessage");
  element.textContent = message;
  element.className = `form-message is-${type}`;
  element.hidden = false;
}

async function loginUser(event) {
  event.preventDefault();
  const email = $("#loginEmail").value.trim().toLowerCase();
  if (!email) {
    showAuthMessage("Enter your email address.");
    return;
  }
  const button = $("#loginForm .submit-button");
  setButtonLoading(button, true, "Signing in");
  try {
    const users = await api("/api/users");
    const user = users.find((item) => item.email.toLowerCase() === email);
    if (!user) {
      showAuthMessage("No wallet found for that email.");
      return;
    }
    enterApp(user);
  } catch (error) {
    showAuthMessage(error.message);
  } finally {
    setButtonLoading(button, false);
  }
}

async function registerUser(event) {
  event.preventDefault();
  const body = {
    fullName: $("#registerName").value.trim(),
    email: $("#registerEmail").value.trim(),
    phoneNumber: $("#registerPhone").value.trim(),
    country: $("#registerCountry").value
  };
  if (!body.fullName || !body.email || !body.phoneNumber) {
    showAuthMessage("Complete all required fields.");
    return;
  }
  const button = $("#registerForm .submit-button");
  setButtonLoading(button, true, "Creating account");
  try {
    const user = await api("/api/users", {
      method: "POST",
      body: JSON.stringify(body)
    });
    enterApp(user);
  } catch (error) {
    showAuthMessage(error.message);
  } finally {
    setButtonLoading(button, false);
  }
}

function enterApp(user) {
  currentUser = user;
  $("#authScreen").hidden = true;
  $("#appShell").hidden = false;
  updateProfileUI();
  setView("dashboard");
  void refreshUser();
  void loadWallet();
  void loadHistory();
  startPriceEngine();
  paintIcons();
}

function logout() {
  currentUser = null;
  $("#appShell").hidden = true;
  $("#authScreen").hidden = false;
  $("#loginEmail").value = "";
  switchAuth("login");
}

async function refreshUser() {
  if (!currentUser) {
    return;
  }
  try {
    currentUser = await api(`/api/users/${currentUser.id}`);
    updateProfileUI();
  } catch (error) {
    showToast(error.message, "error");
  }
}

function updateProfileUI() {
  if (!currentUser) {
    return;
  }
  const initials = currentUser.fullName
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0] || "")
    .join("")
    .toUpperCase();
  all(".profile-avatar").forEach((item) => {
    item.textContent = initials || "DW";
  });
  all(".profile-name").forEach((item) => {
    item.textContent = currentUser.fullName;
  });
  all(".profile-email").forEach((item) => {
    item.textContent = currentUser.email;
  });
  all(".profile-country").forEach((item) => {
    item.textContent = currentUser.country;
  });
  $("#walletStatus").textContent = currentUser.walletStatus || "ACTIVE";

  const verified = currentUser.kycStatus === "VERIFIED";
  $("#kycBadge").textContent = verified ? "KYC verified" : `KYC ${String(currentUser.kycStatus || "PENDING").toLowerCase()}`;
  $("#kycBadge").className = `pill ${verified ? "is-success" : "is-warning"}`;
  $("#kycScore").textContent = verified ? "100%" : "25%";
  $("#kycTitle").textContent = verified ? "Verification complete" : "Verification required";
  $("#kycCopy").textContent = verified ? "Wallet movement is enabled." : "KYC approval is required before funds move.";
  $("#verifyDemoButton").hidden = verified;
  $("#securityVerifyButton").hidden = verified;
  $("#securityKycStatus").textContent = currentUser.kycStatus || "PENDING";
}

async function verifyDemoKyc() {
  if (!currentUser) {
    return;
  }
  const buttons = [$("#verifyDemoButton"), $("#securityVerifyButton")].filter(Boolean);
  buttons.forEach((button) => setButtonLoading(button, true, "Verifying"));
  try {
    if (currentUser.kycStatus !== "SUBMITTED" && currentUser.kycStatus !== "VERIFIED") {
      currentUser = await api(`/api/users/${currentUser.id}/kyc`, {
        method: "POST",
        body: JSON.stringify(DEMO_KYC)
      });
    }
    currentUser = await api(`/api/users/${currentUser.id}/kyc/approve`, {
      method: "POST"
    });
    updateProfileUI();
    showToast("KYC verified.");
  } catch (error) {
    showToast(error.message, "error");
  } finally {
    buttons.forEach((button) => setButtonLoading(button, false));
  }
}

function setView(view) {
  const labels = {
    dashboard: "Wallet dashboard",
    wallet: "Wallet balances",
    markets: "Live USDT markets",
    activity: "Transaction history",
    security: "Security center"
  };
  all("[data-panel]").forEach((panel) => {
    const visible = panel.dataset.panel === view;
    panel.classList.toggle("is-visible", visible);
    panel.setAttribute("aria-hidden", String(!visible));
    if (visible) {
      panel.removeAttribute("inert");
    } else {
      panel.setAttribute("inert", "");
    }
  });
  all("[data-view]").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === view);
  });
  $("#pageTitle").textContent = labels[view] || labels.dashboard;
}

async function loadWallet() {
  if (!currentUser) {
    return;
  }
  try {
    const payload = await api(`/api/v1/wallet/balance?userId=${currentUser.id}`);
    balances = payload.balances || [];
    addresses = payload.addresses || [];
    renderBalances();
    renderAddresses();
    updateSendEstimate();
  } catch (error) {
    showToast(error.message, "error");
  }
}

function balanceFor(asset) {
  const row = balances.find((item) => item.asset === asset || item.currency === asset);
  return Number(row?.balance || 0);
}

function renderBalances() {
  const usdtBalance = balanceFor("USDT");
  $("#totalBalance").textContent = `$${money.format(usdtBalance)}`;
  $("#balanceSubline").textContent = `${formatUsdt(usdtBalance)} available`;

  const container = $("#assetBalances");
  if (!balances.length) {
    container.innerHTML = `<div class="empty-state">No balances yet.</div>`;
    return;
  }
  container.innerHTML = balances.map((item) => `
    <div class="asset-card">
      <span>${item.asset}</span>
      <strong>${Number(item.balance).toLocaleString("en-US", {
        minimumFractionDigits: item.asset === "USDT" ? 8 : 2,
        maximumFractionDigits: item.asset === "USDT" ? 8 : 2
      })}</strong>
      <small>${item.accountReference}</small>
    </div>
  `).join("");
}

function renderAddresses() {
  const html = addresses.map((item) => `
    <div class="address-item">
      <div>
        <strong>${item.network}</strong>
        <span>${item.networkName}</span>
        <code>${item.address}</code>
      </div>
      <button class="icon-button" type="button" data-copy="${item.address}" aria-label="Copy ${item.network} address">
        <i data-lucide="copy"></i>
      </button>
    </div>
  `).join("") || `<div class="empty-state">No addresses available.</div>`;
  $("#addressList").innerHTML = html;
  $("#receiveAddressList").innerHTML = html;
  paintIcons();
}

async function loadHistory() {
  if (!currentUser) {
    return;
  }
  const status = $("#historyStatusFilter")?.value || "";
  const suffix = status ? `&status=${encodeURIComponent(status)}` : "";
  try {
    const payload = await api(`/api/v1/transactions/history?userId=${currentUser.id}&page=0&size=50${suffix}`);
    renderHistory(payload.transactions || []);
  } catch (error) {
    showToast(error.message, "error");
  }
}

function renderHistory(transactions) {
  const table = $("#historyTable");
  const recent = $("#recentActivity");
  if (!transactions.length) {
    const empty = `<div class="empty-state">No transactions yet.</div>`;
    table.innerHTML = empty;
    recent.innerHTML = empty;
    return;
  }

  const rows = transactions.map((tx) => transactionRow(tx)).join("");
  table.innerHTML = `
    <div class="ledger-row ledger-head">
      <span>Status</span><span>Transaction</span><span>Amount</span><span>Created</span>
    </div>
    ${rows}
  `;
  recent.innerHTML = transactions.slice(0, 5).map((tx) => {
    const debit = tx.status === "FAILED" ? "failed" : "debit";
    return `
      <div class="activity-item">
        <div class="activity-icon ${debit}"><i data-lucide="${tx.status === "COMPLETED" ? "check" : tx.status === "FAILED" ? "x" : "clock"}"></i></div>
        <div>
          <strong>${tx.assetType || tx.sourceCurrency} ${tx.network || tx.transferType}</strong>
          <span>${tx.destinationReference}</span>
        </div>
        <b>${formatUsdt(tx.totalDeduction || tx.sourceAmount)}</b>
      </div>
    `;
  }).join("");
  paintIcons();
}

function transactionRow(tx) {
  const date = new Date(tx.createdAt).toLocaleString([], {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
  const asset = tx.assetType || tx.sourceCurrency;
  const network = tx.network || tx.transferType;
  const address = tx.recipientAddress || tx.destinationReference;
  return `
    <div class="ledger-row">
      <span class="status-badge status-${String(tx.status).toLowerCase()}">${tx.status}</span>
      <span><strong>${asset} ${network}</strong><small>${address}</small></span>
      <span>${formatUsdt(tx.totalDeduction || tx.sourceAmount)}</span>
      <span>${date}</span>
    </div>
  `;
}

function openModal(id) {
  const modal = $(`#${id}`);
  if (modal) {
    modal.hidden = false;
    paintIcons();
  }
}

function closeModal(id) {
  const modal = $(`#${id}`);
  if (modal) {
    modal.hidden = true;
  }
}

function openSendModal() {
  if (!currentUser) {
    return;
  }
  if (currentUser.kycStatus !== "VERIFIED") {
    showToast("Verify KYC before sending.", "error");
    return;
  }
  pendingSend = null;
  $("#sendStepForm").hidden = false;
  $("#sendStepVerify").hidden = true;
  $("#sendVerificationCode").value = "";
  $("#sendValidation").hidden = true;
  updateSendEstimate();
  openModal("sendModal");
}

function updateSendEstimate() {
  const network = $("#sendNetwork")?.value || "TRC20";
  const amount = Number($("#sendAmount")?.value || 0);
  const recipient = $("#sendRecipientAddress")?.value.trim() || "";
  const fee = NETWORKS[network].fee;
  const total = amount + fee;
  const available = balanceFor("USDT");

  $("#sendAvailable").textContent = formatUsdt(available);
  $("#sendFee").textContent = formatUsdt(fee);
  $("#sendTotal").textContent = formatUsdt(amount > 0 ? total : fee);

  let message = "";
  if (currentUser && currentUser.kycStatus !== "VERIFIED") {
    message = "KYC must be verified before sending.";
  } else if (!amount || amount <= 0) {
    message = "Enter a USDT amount greater than zero.";
  } else if (!NETWORKS[network].pattern.test(recipient)) {
    message = `Enter a valid ${network} address.`;
  } else if (total > available) {
    message = "Insufficient USDT balance for amount plus fee.";
  }

  const validation = $("#sendValidation");
  validation.hidden = !message;
  validation.textContent = message;
  validation.className = "form-message is-error";
  $("#reviewSendButton").disabled = Boolean(message);
}

function reviewSend() {
  updateSendEstimate();
  if ($("#reviewSendButton").disabled) {
    return;
  }
  const network = $("#sendNetwork").value;
  const amount = Number($("#sendAmount").value);
  const fee = NETWORKS[network].fee;
  pendingSend = {
    userId: currentUser.id,
    asset: "USDT",
    network,
    recipientAddress: $("#sendRecipientAddress").value.trim(),
    amount,
    recipientLabel: $("#sendRecipientLabel").value.trim(),
    memo: $("#sendMemo").value.trim()
  };
  $("#sendReview").innerHTML = `
    <div><span>Asset</span><strong>USDT</strong></div>
    <div><span>Network</span><strong>${network} - ${NETWORKS[network].name}</strong></div>
    <div><span>Amount</span><strong>${formatUsdt(amount)}</strong></div>
    <div><span>Fee</span><strong>${formatUsdt(fee)}</strong></div>
    <div><span>Total</span><strong>${formatUsdt(amount + fee)}</strong></div>
    <div><span>Recipient</span><strong>${pendingSend.recipientAddress}</strong></div>
  `;
  $("#sendStepForm").hidden = true;
  $("#sendStepVerify").hidden = false;
  $("#sendVerificationCode").focus();
}

async function confirmSend() {
  if (!pendingSend) {
    return;
  }
  const verificationCode = $("#sendVerificationCode").value.trim();
  if (!verificationCode) {
    showToast("Enter your PIN or 2FA code.", "error");
    return;
  }
  const button = $("#confirmSendButton");
  setButtonLoading(button, true, "Sending");
  try {
    const response = await api("/api/v1/wallet/send", {
      method: "POST",
      body: JSON.stringify({
        ...pendingSend,
        verificationCode
      })
    });
    closeModal("sendModal");
    showToast(`Transfer queued: ${response.transactionId}`);
    await loadWallet();
    await loadHistory();
  } catch (error) {
    showToast(error.message, "error");
  } finally {
    setButtonLoading(button, false);
  }
}

function openReceiveModal() {
  if (!addresses.length) {
    void loadWallet();
  }
  openModal("receiveModal");
}

function openDepositModal() {
  if (!currentUser) {
    return;
  }
  if (currentUser.kycStatus !== "VERIFIED") {
    showToast("Verify KYC before depositing.", "error");
    return;
  }
  openModal("depositModal");
}

async function depositFunds() {
  if (!currentUser) {
    return;
  }
  const currency = $("#depositCurrency").value;
  const amount = Number($("#depositAmount").value || 0);
  if (!amount || amount <= 0) {
    showToast("Enter a deposit amount.", "error");
    return;
  }
  const button = $("#depositButton");
  setButtonLoading(button, true, "Depositing");
  try {
    await api(`/api/wallets/${currentUser.id}/fund`, {
      method: "POST",
      body: JSON.stringify({
        currency,
        amount,
        memo: $("#depositMemo").value.trim() || "Demo funding"
      })
    });
    closeModal("depositModal");
    showToast(`${currency} deposit posted.`);
    await loadWallet();
    await loadHistory();
  } catch (error) {
    showToast(error.message, "error");
  } finally {
    setButtonLoading(button, false);
  }
}

function handleQuickAction(action) {
  if (action === "send") {
    openSendModal();
  } else if (action === "receive") {
    openReceiveModal();
  } else if (action === "deposit" || action === "buy") {
    openDepositModal();
  }
}

async function loadPrices() {
  try {
    const payload = await api("/api/v1/prices/usdt");
    (payload.pairs || []).forEach((pair) => {
      upsertMarket(pair.pair, {
        price: Number(pair.price),
        changePercent24h: Number(pair.changePercent24h),
        high24h: Number(pair.high24h),
        low24h: Number(pair.low24h)
      });
    });
    $("#priceSource").textContent = payload.stale ? "REST fallback" : payload.source;
    setConnection(payload.stale ? "REST fallback" : "REST synced", payload.stale ? "Using cached market data" : "Backend prices refreshed");
  } catch (error) {
    setConnection("REST fallback", "Backend price fallback unavailable");
  }
}

function startPriceEngine() {
  if (priceEngineStarted) {
    return;
  }
  priceEngineStarted = true;
  void loadPrices();
  fallbackPriceTimer = window.setInterval(loadPrices, 30000);
  connectMarketStream();
}

function connectMarketStream() {
  if (marketSocket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(marketSocket.readyState)) {
    return;
  }
  try {
    marketSocket = new WebSocket("wss://stream.binance.com:9443/stream?streams=btcusdt@ticker/ethusdt@ticker");
  } catch (error) {
    scheduleReconnect();
    return;
  }
  setConnection("Connecting", "Opening Binance stream");

  marketSocket.addEventListener("open", () => {
    reconnectAttempts = 0;
    setConnection("Live stream", "BTC/USDT and ETH/USDT streaming");
  });

  marketSocket.addEventListener("message", (event) => {
    const payload = JSON.parse(event.data);
    const ticker = payload.data || payload;
    if (!ticker.s || !ticker.c) {
      return;
    }
    const pair = ticker.s === "BTCUSDT" ? "BTC/USDT" : ticker.s === "ETHUSDT" ? "ETH/USDT" : ticker.s;
    upsertMarket(pair, {
      price: Number(ticker.c),
      changePercent24h: Number(ticker.P),
      high24h: Number(ticker.h),
      low24h: Number(ticker.l)
    });
    $("#priceSource").textContent = "Binance WS";
  });

  marketSocket.addEventListener("close", () => {
    setConnection("REST fallback", "Market stream reconnecting");
    scheduleReconnect();
  });

  marketSocket.addEventListener("error", () => {
    marketSocket.close();
  });
}

function scheduleReconnect() {
  window.clearTimeout(reconnectTimer);
  reconnectAttempts += 1;
  const delay = Math.min(30000, 1500 * reconnectAttempts);
  reconnectTimer = window.setTimeout(connectMarketStream, delay);
  void loadPrices();
}

function setConnection(title, copy) {
  $("#connectionTitle").textContent = title;
  $("#connectionCopy").textContent = copy;
}

function upsertMarket(pair, next) {
  const previous = marketState.get(pair) || { history: [] };
  const history = [...previous.history, next.price].slice(-40);
  marketState.set(pair, {
    ...next,
    history
  });
  renderMarkets();
}

function renderMarkets() {
  const orderedPairs = ["USDT/USD", "BTC/USDT", "ETH/USDT"];
  const cards = orderedPairs.map((pair) => marketCard(pair, marketState.get(pair))).join("");
  $("#marketGrid").innerHTML = cards;
  const usdt = marketState.get("USDT/USD");
  if (usdt) {
    $("#usdtPrice").textContent = formatPrice(usdt.price);
    const direction = usdt.changePercent24h > 0 ? "is-up" : usdt.changePercent24h < 0 ? "is-down" : "is-neutral";
    $("#usdtChange").className = `price-indicator ${direction}`;
    $("#usdtChange").innerHTML = `<i data-lucide="${direction === "is-up" ? "trending-up" : direction === "is-down" ? "trending-down" : "minus"}"></i><span>${Math.abs(usdt.changePercent24h).toFixed(2)}%</span>`;
    $("#usdtRange").textContent = `H ${formatPrice(usdt.high24h)} / L ${formatPrice(usdt.low24h)}`;
    $("#usdtSparkline").innerHTML = sparkline(usdt.history, direction === "is-down" ? "#F6465D" : "#0ECB81");
  }
  paintIcons();
}

function marketCard(pair, data) {
  const snapshot = data || {
    price: 0,
    changePercent24h: 0,
    high24h: 0,
    low24h: 0,
    history: [0, 0]
  };
  const isUp = snapshot.changePercent24h >= 0;
  return `
    <article class="market-card">
      <div class="market-card-head">
        <div>
          <span>${pair}</span>
          <strong>${formatPrice(snapshot.price)}</strong>
        </div>
        <b class="${isUp ? "price-up" : "price-down"}">${isUp ? "+" : ""}${snapshot.changePercent24h.toFixed(2)}%</b>
      </div>
      <div class="sparkline-wrap">${sparkline(snapshot.history, isUp ? "#0ECB81" : "#F6465D")}</div>
      <div class="market-range">
        <span>24h high ${formatPrice(snapshot.high24h)}</span>
        <span>24h low ${formatPrice(snapshot.low24h)}</span>
      </div>
    </article>
  `;
}

function sparkline(values, color) {
  const points = values.length > 1 ? values : [values[0] || 0, values[0] || 0];
  const min = Math.min(...points);
  const max = Math.max(...points);
  const range = max - min || 1;
  const width = 180;
  const height = 48;
  const coords = points.map((value, index) => {
    const x = (index / (points.length - 1)) * width;
    const y = height - ((value - min) / range) * (height - 8) - 4;
    return `${x.toFixed(2)},${y.toFixed(2)}`;
  }).join(" ");
  return `<svg class="sparkline" viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" aria-hidden="true"><polyline points="${coords}" fill="none" stroke="${color}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`;
}

function setTheme(theme) {
  const next = theme === "light" ? "light" : "dark";
  document.documentElement.dataset.theme = next;
  localStorage.setItem("wallet-theme", next);
  $(".theme-toggle").innerHTML = `<i data-lucide="${next === "dark" ? "sun" : "moon"}"></i>`;
  paintIcons();
}

function bindEvents() {
  all("[data-auth-tab]").forEach((tab) => {
    tab.addEventListener("click", () => switchAuth(tab.dataset.authTab));
  });
  $("#loginForm").addEventListener("submit", loginUser);
  $("#registerForm").addEventListener("submit", registerUser);
  $("#logoutButton").addEventListener("click", logout);

  all("[data-view]").forEach((button) => {
    button.addEventListener("click", () => setView(button.dataset.view));
  });
  all("[data-action]").forEach((button) => {
    button.addEventListener("click", () => handleQuickAction(button.dataset.action));
  });
  all("[data-close-modal]").forEach((button) => {
    button.addEventListener("click", () => closeModal(button.dataset.closeModal));
  });
  all(".modal-overlay").forEach((overlay) => {
    overlay.addEventListener("click", (event) => {
      if (event.target === overlay) {
        overlay.hidden = true;
      }
    });
  });

  $("#verifyDemoButton").addEventListener("click", verifyDemoKyc);
  $("#securityVerifyButton").addEventListener("click", verifyDemoKyc);
  $("#refreshBalancesButton").addEventListener("click", loadWallet);
  $("#refreshPricesButton").addEventListener("click", loadPrices);
  $("#historyStatusFilter").addEventListener("change", loadHistory);
  $("#depositButton").addEventListener("click", depositFunds);
  $("#reviewSendButton").addEventListener("click", reviewSend);
  $("#backToSendForm").addEventListener("click", () => {
    $("#sendStepForm").hidden = false;
    $("#sendStepVerify").hidden = true;
  });
  $("#confirmSendButton").addEventListener("click", confirmSend);
  ["#sendNetwork", "#sendAmount", "#sendRecipientAddress"].forEach((selector) => {
    $(selector).addEventListener("input", updateSendEstimate);
    $(selector).addEventListener("change", updateSendEstimate);
  });
  $(".theme-toggle").addEventListener("click", () => {
    const current = document.documentElement.dataset.theme || "dark";
    setTheme(current === "dark" ? "light" : "dark");
  });

  document.addEventListener("click", async (event) => {
    const copyButton = event.target.closest("[data-copy]");
    if (!copyButton) {
      return;
    }
    await navigator.clipboard.writeText(copyButton.dataset.copy);
    showToast("Address copied.");
  });
}

function init() {
  bindEvents();
  setTheme(localStorage.getItem("wallet-theme") || "dark");
  renderMarkets();
  paintIcons();
}

window.addEventListener("beforeunload", () => {
  if (fallbackPriceTimer) {
    window.clearInterval(fallbackPriceTimer);
  }
  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
  }
  if (marketSocket) {
    marketSocket.close();
  }
});

document.addEventListener("DOMContentLoaded", init);
