import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";
import "./interactions.css";

const icon =
  (symbol) =>
  ({ size = 18 }) => (
    <span className="ui-icon" style={{ fontSize: size }}>
      {symbol}
    </span>
  );
const Bell = icon("◌"),
  Search = icon("⌕"),
  Eye = icon("◉"),
  EyeOff = icon("⊘"),
  ArrowDown = icon("⇩"),
  ArrowUp = icon("⇧"),
  Swap = icon("⇄"),
  Send = icon("➤"),
  Users = icon("◉"),
  Home = icon("⌂"),
  Markets = icon("⌁"),
  Wallet = icon("▣"),
  User = icon("◯"),
  Chevron = icon("⌄"),
  Shield = icon("✓"),
  Lock = icon("⌑"),
  Check = icon("✓"),
  Help = icon("?");

const API_BASE = "https://wallet-api-7dom.onrender.com/api/v1";
const DEFAULT_USD_TO_NGN_RATE = 1600;
const ROUTES = ["home", "markets", "convert", "wallet", "profile", "notifications", "search", "action", "asset"];
const assets = {
  BTC: {
    name: "Bitcoin",
    price: 65000,
    icon: "₿",
    color: "#f7931a",
    change: 2.84,
    data: "2,13 10,16 18,8 26,12 35,6 43,9 52,3 61,7 70,1",
  },
  ETH: {
    name: "Ethereum",
    price: 3540,
    icon: "♦",
    color: "#627eea",
    change: 1.26,
    data: "2,16 10,10 18,14 26,7 35,11 43,4 52,9 61,2 70,5",
  },
  SOL: {
    name: "Solana",
    price: 142.8,
    icon: "≋",
    color: "#9a6bff",
    change: -0.74,
    data: "2,4 10,8 18,5 26,13 35,9 43,15 52,12 61,17 70,19",
  },
  USDT: {
    name: "Tether USD",
    price: 1,
    icon: "₮",
    color: "#26a17b",
    change: 0.01,
    data: "2,11 10,11 18,10 26,11 35,10 43,11 52,10 61,11 70,10",
  },
  USD: {
    name: "US Dollar",
    price: 1,
    icon: "$",
    color: "#6e7d90",
    change: 0,
    data: "2,11 70,11",
  },
  NGN: {
    name: "Nigerian Naira",
    price: 1 / DEFAULT_USD_TO_NGN_RATE,
    icon: "₦",
    color: "#119a60",
    change: 0,
    data: "2,11 70,11",
  },
};
const fmt = (value, decimals = 2) =>
  new Intl.NumberFormat("en-US", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value || 0);
const initialRoute = () =>
  ROUTES.includes(window.location.hash.slice(1))
    ? window.location.hash.slice(1)
    : "home";
function AssetIcon({ symbol }) {
  const asset = assets[symbol];
  return (
    <span className="asset-icon" style={{ background: asset.color }}>
      {asset.icon}
    </span>
  );
}
function Spark({ symbol }) {
  const asset = assets[symbol];
  return (
    <svg className="spark" viewBox="0 0 72 22" preserveAspectRatio="none">
      <polyline
        points={asset.data}
        fill="none"
        stroke={asset.change < 0 ? "#f6465d" : "#0ecb81"}
        strokeWidth="1.7"
      />
    </svg>
  );
}
function Screen({ children, active, name }) {
  return active ? (
    <section id={`view-${name}`} className="screen-view">
      {children}
    </section>
  ) : null;
}

function App() {
  const [route, setRoute] = useState(initialRoute);
  const [market, setMarket] = useState("Hot");
  const [visible, setVisible] = useState(true);
  const [currency, setCurrency] = useState("USD");
  const [from, setFrom] = useState("USDT");
  const [to, setTo] = useState("NGN");
  const [amount, setAmount] = useState("100");
  const [verificationCode, setVerificationCode] = useState("");
  const [usdToNgnRate] = useState(DEFAULT_USD_TO_NGN_RATE);
  const [toast, setToast] = useState("");
  const [session, setSession] = useState(() =>
    JSON.parse(localStorage.getItem("wallet_session") || "null"),
  );
  const [balances, setBalances] = useState([]);
  const [addresses, setAddresses] = useState([]);
  const [history, setHistory] = useState([]);
  const [refresh, setRefresh] = useState(0);
  const [action, setAction] = useState("Wallet action");
  const [selectedAsset, setSelectedAsset] = useState("BTC");
  const user = session?.user;
  const assetUsdValue = (symbol) =>
    symbol === "NGN" ? 1 / usdToNgnRate : assets[symbol].price;
  const total = balances.length
    ? balances.reduce(
        (sum, item) =>
          sum +
          (Number(item.balance) || 0) *
            (assets[item.asset] ? assetUsdValue(item.asset) : 0),
        0,
      )
    : user
      ? 0
      : 2450;
  const converted = useMemo(
    () => (Number(amount || 0) * assetUsdValue(from)) / assetUsdValue(to),
    [amount, from, to, usdToNgnRate],
  );
  const money = (value) =>
    currency === "NGN" ? `₦${fmt(value * usdToNgnRate)}` : `$${fmt(value)}`;
  const go = (next) => {
    window.location.hash = next;
  };
  const notify = (message) => {
    setToast(message);
    window.setTimeout(() => setToast(""), 2800);
  };
  const openAction = (title) => {
    setAction(title);
    go("action");
  };
  const openAsset = (symbol) => {
    setSelectedAsset(symbol);
    go("asset");
  };

  useEffect(() => {
    const onHash = () => setRoute(initialRoute());
    window.addEventListener("hashchange", onHash);
    if (!window.location.hash) window.location.hash = "home";
    return () => window.removeEventListener("hashchange", onHash);
  }, []);
  useEffect(() => {
    if (!session) return;
    const headers = { Authorization: `Bearer ${session.accessToken}` };
    Promise.all([
      fetch(`${API_BASE}/wallet/balance?userId=${session.user.id}`, {
        headers,
      }),
      fetch(`${API_BASE}/transactions/history?userId=${session.user.id}`, {
        headers,
      }),
    ])
      .then(async ([wallet, transactions]) => {
        if (wallet.ok) {
          const data = await wallet.json();
          setBalances(data.balances || []);
          setAddresses(data.addresses || []);
        }
        if (transactions.ok)
          setHistory((await transactions.json()).transactions || []);
      })
      .catch(() => notify("Unable to refresh wallet data"));
  }, [session, refresh]);

  const completeAuth = (next) => {
    localStorage.setItem("wallet_session", JSON.stringify(next));
    setSession(next);
    notify(`Welcome, ${next.user.fullName.split(" ")[0]}`);
    go("home");
  };
  const signOut = () => {
    localStorage.removeItem("wallet_session");
    setSession(null);
    setBalances([]);
    setHistory([]);
    notify("Signed out");
    go("home");
  };
  const convert = async () => {
    if (!session) {
      notify("Sign in to convert");
      go("profile");
      return;
    }
    if (from === to) return notify("Choose two different assets");
    if (!verificationCode) return notify("Enter your trading PIN or 2FA code");
    try {
      const response = await fetch(`${API_BASE}/wallet/convert`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${session.accessToken}`,
        },
        body: JSON.stringify({
          sourceAsset: from,
          targetAsset: to,
          sourceAmount: Number(amount),
          verificationCode,
        }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Conversion failed");
      setVerificationCode("");
      setRefresh((value) => value + 1);
      notify(`Converted ${data.sourceAmount} ${data.sourceCurrency}`);
    } catch (error) {
      notify(error.message || "Conversion failed");
    }
  };

  const passwordResetToken = new URLSearchParams(window.location.search).get("resetToken");
  const forgotPassword = new URLSearchParams(window.location.search).has("forgotPassword");
  if (!session || passwordResetToken) return passwordResetToken ? <PasswordResetPage token={passwordResetToken} /> : forgotPassword ? <ForgotPasswordPage /> : <AuthGateway onAuth={completeAuth} />;

  return (
    <main className="app-shell">
      <header>
        <button className="avatar" onClick={() => go("profile")}>
          {user?.fullName
            ?.split(" ")
            .map((part) => part[0])
            .join("")
            .slice(0, 2) || "AO"}
        </button>
        <div className="kyc">
          <Shield size={13} /> {user ? user.kycStatus : "Demo mode"}
        </div>
        <button className="search" onClick={() => go("search")}>
          <Search size={17} />
          <span>Search assets</span>
        </button>
        <button className="icon-button" onClick={() => go("notifications")} aria-label="Open notifications">
          <Bell size={20} />
          <i />
        </button>
      </header>

      <div className="content-scroll">
        <Screen name="home" active={route === "home"}>
          <section className="balance-card">
            <div className="balance-head">
              <span>Total assets</span>
              <button onClick={() => setVisible(!visible)}>
                {visible ? <Eye size={17} /> : <EyeOff size={17} />}
              </button>
            </div>
            <div className="balance-value">
              {visible ? money(total) : "••••••••"}
            </div>
            <div className="naira-line">
              {visible
                ? currency === "USD"
                  ? `≈ ₦${fmt(total * usdToNgnRate)}`
                  : `≈ $${fmt(total)}`
                : "••••••••"}
            </div>
            <div className="pnl">
              <span>24h P&L</span>
              <b>
                +$42.60 <em>+1.77%</em>
              </b>
            </div>
            <div className="card-footer">
              <span>Spot account</span>
              <span onClick={() => go("wallet")}>
                View assets <Chevron size={14} />
              </span>
            </div>
          </section>
          <section className="actions">
            {[
              [ArrowDown, "Deposit"],
              [ArrowUp, "Withdraw"],
              [Swap, "Convert"],
              [Send, "Transfer"],
              [Users, "P2P"],
            ].map(([Icon, label]) => (
              <button
                key={label}
                onClick={() => label === "Convert" ? go("convert") : openAction(label)}
              >
                <span>
                  <Icon size={19} />
                </span>
                {label}
              </button>
            ))}
          </section>
          <SectionTitle
            title="Markets"
            subtitle="Top movers right now"
            action="View all"
            onAction={() => go("markets")}
          />
          <MarketList market="Hot" rate={usdToNgnRate} limit={4} onSelect={openAsset} />
          <RecentActivity history={history} />
        </Screen>

        <Screen name="markets" active={route === "markets"}>
          <SectionTitle
            title="Markets"
            subtitle="Live simulated market overview"
          />
          <div className="market-tabs">
            {["Hot", "Gainers", "Losers", "24h Volume"].map((tab) => (
              <button
                className={market === tab ? "active" : ""}
                onClick={() => setMarket(tab)}
                key={tab}
              >
                {tab}
              </button>
            ))}
          </div>
          <MarketList market={market} rate={usdToNgnRate} onSelect={openAsset} />
        </Screen>

        <Screen name="convert" active={route === "convert"}>
          <section className="converter standalone">
            <div className="converter-title">
              <div>
                <span className="amber-dot" /> Instant convert
              </div>
              <button onClick={() => openAction("Conversion help")} aria-label="Conversion help"><Help size={18} /></button>
            </div>
            <p>Zero-fee simulated conversion at the current displayed rate.</p>
            <div className="convert-box">
              <label>From</label>
              <div className="input-row">
                <select
                  value={from}
                  onChange={(event) => setFrom(event.target.value)}
                >
                  {Object.keys(assets).map((asset) => (
                    <option key={asset}>{asset}</option>
                  ))}
                </select>
                <input
                  value={amount}
                  onChange={(event) =>
                    setAmount(event.target.value.replace(/[^0-9.]/g, ""))
                  }
                  inputMode="decimal"
                />
                <span className="unit">{from}</span>
              </div>
            </div>
            <button
              className="swap"
              onClick={() => {
                const current = from;
                setFrom(to);
                setTo(current);
              }}
            >
              <Swap size={18} />
            </button>
            <div className="convert-box">
              <label>To</label>
              <div className="input-row">
                <select
                  value={to}
                  onChange={(event) => setTo(event.target.value)}
                >
                  {Object.keys(assets).map((asset) => (
                    <option key={asset}>{asset}</option>
                  ))}
                </select>
                <div className="output">
                  {fmt(converted, converted < 1 ? 6 : 2)}
                </div>
                <span className="unit">{to}</span>
              </div>
            </div>
            <div className="rate">
              <span>Live exchange rate</span>
              <b>
                1 {from} ={" "}
                {fmt(
                  assetUsdValue(from) / assetUsdValue(to),
                  to === "NGN" ? 2 : 6,
                )}{" "}
                {to}
              </b>
            </div>
            {session && (
              <input
                className="convert-pin"
                type="password"
                maxLength="16"
                value={verificationCode}
                onChange={(event) =>
                  setVerificationCode(event.target.value.replace(/\s/g, ""))
                }
                placeholder="Trading PIN or 2FA code"
                inputMode="numeric"
              />
            )}
            <button className="convert-btn" onClick={convert}>
              Convert instantly <Swap size={18} />
            </button>
          </section>
        </Screen>

        <Screen name="wallet" active={route === "wallet"}>
          <SectionTitle
            title="My wallet"
            subtitle="Spot, funding and futures balances"
          />
          <div className="wallet-summary">
            <span>Spot</span>
            <b>{money(total)}</b>
            <small>
              Funding and Futures are currently zero in this simulated wallet.
            </small>
          </div>
          <section className="watchlist">
            {(balances.length
              ? balances
              : ["BTC", "ETH", "SOL", "USDT", "NGN"].map((asset) => ({
                  asset,
                  balance: 0,
                }))
            ).map((item) => (
              <button className="market-row" key={item.asset} onClick={() => openAsset(item.asset)}>
                <AssetIcon symbol={item.asset} />
                <div className="coin-name">
                  <b>{item.asset}</b>
                  <small>{assets[item.asset].name}</small>
                </div>
                <div className="price">
                  <b>
                    {fmt(Number(item.balance), item.asset === "BTC" ? 8 : 2)}
                  </b>
                  <small>
                    {money(Number(item.balance) * assetUsdValue(item.asset))}
                  </small>
                </div>
              </button>
            ))}
          </section>
          <section className="address-list">
            <SectionTitle
              title="Deposit addresses"
              subtitle="Generated for your account"
            />
            {addresses.length ? (
              addresses.map((address) => (
                <div className="address-row" key={address.network}>
                  <b>{address.networkName}</b>
                  <small>{address.address}</small>
                </div>
              ))
            ) : (
              <p className="empty-copy">
                Sign in to generate your wallet addresses.
              </p>
            )}
          </section>
          <RecentActivity history={history} />
        </Screen>

        <Screen name="profile" active={route === "profile"}>
          {user ? (
            <section className="profile-panel">
              <div className="profile-top">
                <div className="avatar big">
                  {user.fullName
                    .split(" ")
                    .map((part) => part[0])
                    .join("")
                    .slice(0, 2)}
                </div>
                <div>
                  <h3>{user.fullName}</h3>
                  <p>{user.email}</p>
                </div>
                <button onClick={() => openAction("Account profile")}><Chevron size={18} /></button>
              </div>
              <div className="profile-status">
                <button onClick={() => openAction("Identity verification")}>
                  <Shield />
                  <span>
                    <b>{user.kycStatus} verification</b>
                    <small>Approval is handled by a compliance reviewer.</small>
                  </span>
                  <Check size={17} />
                </button>
                <button onClick={() => openAction("Security centre")}>
                  <Lock />
                  <span>
                    <b>Security score: strong</b>
                    <small>JWT session and trading verification enabled</small>
                  </span>
                  <Check size={17} />
                </button>
              </div>
              <div className="pref">
                <span>Display currency</span>
                <button
                  onClick={() =>
                    setCurrency(currency === "USD" ? "NGN" : "USD")
                  }
                >
                  {currency === "USD" ? "$ USD" : "₦ NGN"} <Chevron size={15} />
                </button>
              </div>
              <div className="settings-list">
                <button
                  onClick={() => openAction("API keys")}
                >
                  API keys <Chevron />
                </button>
                <button
                  onClick={() => go("notifications")}
                >
                  Notifications <Chevron />
                </button>
              </div>
              <button className="signout" onClick={signOut}>
                Sign out
              </button>
            </section>
          ) : (
            <AuthPanel onAuth={completeAuth} />
          )}
        </Screen>

        <Screen name="notifications" active={route === "notifications"}>
          <SectionTitle title="Notifications" subtitle="Wallet and market updates" action="Back" onAction={() => go("home")} />
          <section className="notification-list">
            <button onClick={() => openAction("Security alert")}><b>Security check complete</b><small>Your sign-in session is protected with JWT security.</small><time>Just now</time></button>
            <button onClick={() => go("markets")}><b>BTC is up 2.84%</b><small>Tap to view the live market overview.</small><time>Today</time></button>
            <button onClick={() => go("wallet")}><b>Your wallet is ready</b><small>View balances, deposit addresses and transaction history.</small><time>Today</time></button>
          </section>
        </Screen>

        <Screen name="search" active={route === "search"}>
          <SectionTitle title="Search assets" subtitle="Select an asset to view its details" action="Back" onAction={() => go("home")} />
          <section className="watchlist">{Object.keys(assets).map((symbol) => <button className="market-row" key={symbol} onClick={() => openAsset(symbol)}><AssetIcon symbol={symbol} /><div className="coin-name"><b>{symbol}</b><small>{assets[symbol].name}</small></div><Chevron size={16} /></button>)}</section>
        </Screen>

        <Screen name="asset" active={route === "asset"}>
          <SectionTitle title={`${selectedAsset} details`} subtitle={assets[selectedAsset].name} action="Back" onAction={() => go("markets")} />
          <section className="detail-card"><AssetIcon symbol={selectedAsset} /><h3>{assets[selectedAsset].name} ({selectedAsset})</h3><b>${fmt(assets[selectedAsset].price, assets[selectedAsset].price < 10 ? 4 : 2)}</b><small>≈ ₦{fmt(assets[selectedAsset].price * usdToNgnRate)}</small><Spark symbol={selectedAsset} /><button className="primary" onClick={() => go("convert")}>Convert {selectedAsset}</button></section>
        </Screen>

        <Screen name="action" active={route === "action"}>
          <SectionTitle title={action} subtitle="Complete this securely in your wallet" action="Back" onAction={() => go("home")} />
          <section className="detail-card"><div className="modal-icon"><Lock /></div><h3>{action}</h3><p>Continue from this dedicated screen. Transaction protections will be applied before any money movement.</p><button className="primary" onClick={() => action === "Deposit" || action === "Withdraw" ? go("wallet") : action === "P2P" ? go("markets") : go("profile")}>{action === "Deposit" || action === "Withdraw" ? "Open wallet" : "Continue"}</button></section>
        </Screen>
      </div>

      <nav>
        {[
          [Home, "home", "Home"],
          [Markets, "markets", "Markets"],
          [Swap, "convert", "Convert"],
          [Wallet, "wallet", "Wallet"],
          [User, "profile", "Profile"],
        ].map(([Icon, name, label]) => (
          <button
            className={route === name ? "selected" : ""}
            onClick={() => go(name)}
            key={name}
          >
            <Icon size={21} />
            <span>{label}</span>
          </button>
        ))}
      </nav>
      {toast && (
        <div className="toast">
          <Check size={17} />
          {toast}
        </div>
      )}
      <style>{`html,body,#root{width:100%;height:100%;overflow:hidden}body{min-height:100dvh}.app-shell{width:100%;height:100dvh;min-height:0;margin:0 auto;padding:0;display:flex;flex-direction:column;overflow:hidden}.app-shell header{flex:none;padding:max(12px,env(safe-area-inset-top)) 18px 12px}.content-scroll{flex:1;min-height:0;overflow-y:auto;-webkit-overflow-scrolling:touch;overscroll-behavior:contain;padding:0 18px calc(24px + env(safe-area-inset-bottom));scrollbar-width:none}.content-scroll::-webkit-scrollbar{display:none}.screen-view{min-height:100%;padding-bottom:8px}nav{position:static;left:auto;bottom:auto;transform:none;flex:none;max-width:none;width:100%;height:auto;min-height:73px;padding:10px 13px max(12px,env(safe-area-inset-bottom));box-sizing:content-box}@media(max-width:480px){.app-shell{width:100vw;height:100dvh;border-radius:0;box-shadow:none}}@media(min-width:481px){body{display:grid;place-items:center;background:#060708}#root{display:contents}.app-shell{width:min(430px,calc(100vw - 32px));height:min(900px,calc(100dvh - 32px));min-height:0;border-radius:28px;box-shadow:0 24px 80px #000;overflow:hidden}}`}</style>
    </main>
  );
}

function SectionTitle({ title, subtitle, action, onAction }) {
  return (
    <section className="section-title">
      <div>
        <h2>{title}</h2>
        <p>{subtitle}</p>
      </div>
      {action && <button onClick={onAction}>{action}</button>}
    </section>
  );
}
function MarketList({ market, rate, limit, onSelect }) {
  const entries = Object.keys(assets)
    .filter((symbol) => !["USD", "NGN"].includes(symbol))
    .slice(0, limit || 4);
  return (
    <section className="watchlist">
      {entries.map((symbol, index) => {
        const asset = assets[symbol];
        const change =
          market === "Losers"
            ? -Math.abs(asset.change)
            : market === "Gainers"
              ? Math.abs(asset.change)
              : asset.change;
        return (
          <button className="market-row" key={symbol} onClick={() => onSelect?.(symbol)}>
            <AssetIcon symbol={symbol} />
            <div className="coin-name">
              <b>{symbol}</b>
              <small>{asset.name}</small>
            </div>
            <div className="price">
              <b>${fmt(asset.price, asset.price < 10 ? 4 : 2)}</b>
              <small>
                ₦{fmt(asset.price * rate, asset.price < 10 ? 2 : 0)}
              </small>
            </div>
            <Spark symbol={symbol} />
            <span className={`change ${change < 0 ? "down" : ""}`}>
              {change > 0 ? "+" : ""}
              {fmt(change)}%
            </span>
          </button>
        );
      })}
    </section>
  );
}
function RecentActivity({ history }) {
  return (
    <section className="activity">
      <SectionTitle
        title="Recent activity"
        subtitle={
          history.length
            ? "Your latest wallet movements"
            : "No transactions yet"
        }
      />
      {history.slice(0, 3).map((item) => (
        <div className="activity-row" key={item.id}>
          <span className="activity-dot" />
          <div>
            <b>
              {item.sourceCurrency} → {item.targetCurrency}
            </b>
            <small>{item.statusMessage || item.status}</small>
          </div>
          <strong>{item.status}</strong>
        </div>
      ))}
    </section>
  );
}
function PasswordSupportPage({ title, text, children }) { return <main className="auth-shell"><section className="auth-card"><div className="auth-brand"><div className="auth-logo">W</div><span>WalletApp</span></div><div className="auth-copy"><h1>{title}</h1><p>{text}</p></div>{children}<p className="auth-switch"><button onClick={() => window.location.assign(window.location.pathname)}>Back to login</button></p></section><style>{`html,body,#root{width:100%;height:100%;overflow:auto}.auth-shell{min-height:100dvh;display:grid;place-items:center;padding:max(24px,env(safe-area-inset-top)) 18px max(24px,env(safe-area-inset-bottom));background:radial-gradient(ellipse at 80% 0,#30230c 0,transparent 36%),#0d0f12}.auth-card{width:min(100%,430px);padding:28px 24px;background:#17191e;border:1px solid #2b2e34;border-radius:20px;box-shadow:0 22px 70px #0008}.auth-brand{display:flex;align-items:center;gap:9px;font-weight:700;font-size:17px}.auth-logo{display:grid;place-items:center;width:32px;height:32px;border-radius:10px;background:#f7a600;color:#17120a;font-weight:800}.auth-copy h1{font:700 28px 'Space Grotesk';margin:29px 0 7px}.auth-copy p{color:#8f96a3;font-size:12px;line-height:1.5}.gateway-form{display:grid;gap:13px;margin-top:24px}.gateway-form label{display:grid;gap:6px;color:#bac0c8;font-size:11px}.gateway-form input{width:100%;padding:13px 12px;border:1px solid #30343b;border-radius:9px;background:#101216;color:#f5f5f5;outline:0}.gateway-form input:focus{border-color:#f7a600}.gateway-submit{margin-top:4px;width:100%;padding:13px;border:0;border-radius:9px;background:#f7a600;color:#1b150a;font-weight:700}.auth-error{color:#f88b9a;font-size:11px;margin:0}.auth-switch{text-align:center;margin:23px 0 0;color:#9ba1ab;font-size:12px}.auth-switch button{border:0;background:none;color:#f7a600;font-weight:700}`}</style></main>; }
function ForgotPasswordPage() { const [email,setEmail]=useState(""); const [message,setMessage]=useState(""); const [loading,setLoading]=useState(false); const submit=async(event)=>{event.preventDefault();setLoading(true);setMessage("");try{const response=await fetch(`${API_BASE}/auth/password-reset/request`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({email})});const data=await response.json();if(!response.ok)throw new Error(data.message||"Unable to request a reset link");setMessage(data.message)}catch(error){setMessage(error.message||"Network error")}finally{setLoading(false)}};return <PasswordSupportPage title="Reset your password" text="Enter your account email and we will send a secure reset link."><form className="gateway-form" onSubmit={submit}><label>Email<input type="email" value={email} onChange={(event)=>setEmail(event.target.value)} placeholder="Enter your email" required /></label>{message&&<p className="auth-error">{message}</p>}<button className="gateway-submit" disabled={loading}>{loading?"Sending…":"Send reset link"}</button></form></PasswordSupportPage>; }
function PasswordResetPage({token}) { const [password,setPassword]=useState(""); const [message,setMessage]=useState(""); const [loading,setLoading]=useState(false); const submit=async(event)=>{event.preventDefault();setLoading(true);setMessage("");try{const response=await fetch(`${API_BASE}/auth/password-reset/confirm`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({token,newPassword:password})});const data=await response.json();if(!response.ok)throw new Error(data.message||"Unable to reset password");setMessage(data.message);window.history.replaceState({},"",window.location.pathname)}catch(error){setMessage(error.message||"Network error")}finally{setLoading(false)}};return <PasswordSupportPage title="Choose a new password" text="Use at least 12 characters. This reset link expires after 15 minutes."><form className="gateway-form" onSubmit={submit}><label>New password<input type="password" value={password} onChange={(event)=>setPassword(event.target.value)} minLength="12" placeholder="Enter a new password" required /></label>{message&&<p className="auth-error">{message}</p>}<button className="gateway-submit" disabled={loading}>{loading?"Updating…":"Reset password"}</button></form></PasswordSupportPage>; }
function AuthGateway({ onAuth }) {
  const [mode, setMode] = useState("login");
  const [authMethod, setAuthMethod] = useState("email");
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    country: "NG",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const update = (event) =>
    setForm({ ...form, [event.target.name]: event.target.value });
  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const body =
        mode === "login"
          ? { email: form.email, password: form.password }
          : form;
      const response = await fetch(
        `${API_BASE}/auth/${mode === "login" ? "login" : "register"}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(body),
        },
      );
      const data = await response.json();
      if (!response.ok)
        throw new Error(data.message || "Unable to authenticate");
      onAuth(data);
    } catch (problem) {
      setError(problem.message || "Network error");
    } finally {
      setLoading(false);
    }
  };
  return (
    <main className="auth-shell">
      <section className="auth-card">
        <div className="auth-brand">
          <div className="auth-logo">W</div>
          <span>WalletApp</span>
        </div>
        <div className="auth-copy">
          <h1>{authMethod === "phone" ? "Sign in with phone" : mode === "login" ? "Welcome back" : "Create your account"}</h1>
          <p>
            {authMethod === "phone"
              ? "We will send a one-time code to your registered phone number."
              : mode === "login"
              ? "Log in to manage your digital assets securely."
              : "Set up your secure wallet in a few moments."}
          </p>
        </div>
        <div className="auth-methods">
          <button className={`method ${authMethod === "email" ? "active" : ""}`} type="button" onClick={() => { setAuthMethod("email"); setError(""); }}>Email</button>
          <button
            className={`method ${authMethod === "phone" ? "active" : ""}`}
            type="button"
            onClick={() => { setAuthMethod("phone"); setMode("login"); setError(""); }}
          >
            Phone
          </button>
        </div>
        {authMethod === "phone" ? <PhoneOtpForm onAuth={onAuth} /> : <>
        <form className="gateway-form" onSubmit={submit}>
          {mode === "register" && (
            <>
              <label>
                Full name
                <input
                  name="fullName"
                  placeholder="Enter your full name"
                  value={form.fullName}
                  onChange={update}
                  required
                />
              </label>
              <label>
                Phone number
                <input
                  name="phoneNumber"
                  placeholder="+234 801 234 5678"
                  value={form.phoneNumber}
                  onChange={update}
                  required
                />
              </label>
            </>
          )}
          <label>
            Email
            <input
              name="email"
              type="email"
              placeholder="Enter your email"
              value={form.email}
              onChange={update}
              required
            />
          </label>
          <label>
            Password
            <input
              name="password"
              type="password"
              placeholder="Enter your password"
              minLength="12"
              value={form.password}
              onChange={update}
              required
            />
          </label>
          {mode === "login" && (
            <button
              type="button"
              className="forgot"
              onClick={() => window.location.assign(`${window.location.pathname}?forgotPassword=1`)}
            >
              Forgot password?
            </button>
          )}
          {error && <p className="auth-error">{error}</p>}
          <button className="gateway-submit" disabled={loading}>
            {loading
              ? "Please wait…"
              : mode === "login"
                ? "Log in"
                : "Create account"}
          </button>
        </form>
        <div className="divider">
          <span>or continue with</span>
        </div>
        <div className="social-row">
          <button
            type="button"
            onClick={() => setError("Google sign-in is not connected yet.")}
          >
            G <span>Google</span>
          </button>
          <button
            type="button"
            onClick={() => setError("Apple sign-in is not connected yet.")}
          >
            ● <span>Apple</span>
          </button>
        </div>
        <p className="auth-switch">
          {mode === "login" ? "New to WalletApp?" : "Already have an account?"}{" "}
          <button
            onClick={() => {
              setMode(mode === "login" ? "register" : "login");
              setError("");
            }}
          >
            {mode === "login" ? "Sign up" : "Log in"}
          </button>
        </p>
        <p className="auth-legal">
          By continuing, you agree to the WalletApp Terms of Service and Privacy
          Policy.
        </p>
        </>}
      </section>
      <style>{`html,body,#root{width:100%;height:100%;overflow:auto}.auth-shell{min-height:100dvh;display:grid;place-items:center;padding:max(24px,env(safe-area-inset-top)) 18px max(24px,env(safe-area-inset-bottom));background:radial-gradient(ellipse at 80% 0,#30230c 0,transparent 36%),#0d0f12}.auth-card{width:min(100%,430px);padding:28px 24px;background:#17191e;border:1px solid #2b2e34;border-radius:20px;box-shadow:0 22px 70px #0008}.auth-brand{display:flex;align-items:center;gap:9px;font-weight:700;font-size:17px}.auth-logo{display:grid;place-items:center;width:32px;height:32px;border-radius:10px;background:#f7a600;color:#17120a;font-weight:800}.auth-copy h1{font:700 28px 'Space Grotesk';margin:29px 0 7px}.auth-copy p,.auth-legal{color:#8f96a3;font-size:12px;line-height:1.5}.auth-methods{display:grid;grid-template-columns:1fr 1fr;gap:8px;margin:24px 0 17px}.method{padding:10px;border-radius:8px;background:#111317;color:#9ba1ab;border:1px solid #292d34;font-size:12px}.method.active{color:#f7a600;border-color:#f7a600;background:#2a210e}.gateway-form{display:grid;gap:13px}.gateway-form label{display:grid;gap:6px;color:#bac0c8;font-size:11px}.gateway-form input{width:100%;padding:13px 12px;border:1px solid #30343b;border-radius:9px;background:#101216;color:#f5f5f5;outline:0}.gateway-form input:focus{border-color:#f7a600}.forgot{justify-self:end;color:#f7a600;font-size:11px;margin-top:-4px}.gateway-submit{margin-top:4px;width:100%;padding:13px;border-radius:9px;background:#f7a600;color:#1b150a;font-weight:700}.gateway-submit:disabled{opacity:.5}.divider{display:flex;align-items:center;gap:10px;margin:21px 0;color:#737b86;font-size:10px}.divider:before,.divider:after{content:'';height:1px;flex:1;background:#2c3036}.social-row{display:grid;grid-template-columns:1fr 1fr;gap:10px}.social-row button{padding:11px;border:1px solid #30343b;border-radius:9px;background:#1b1d22;color:#eef0f2;font-size:12px}.auth-switch{text-align:center;margin:23px 0 13px;color:#9ba1ab;font-size:12px}.auth-switch button{color:#f7a600;font-weight:700}.auth-legal{text-align:center;margin:0;font-size:10px}@media(min-width:481px){.auth-card{border-radius:24px}}`}</style>
    </main>
  );
}
function PhoneOtpForm({ onAuth }) {
  const [phoneNumber, setPhoneNumber] = useState("");
  const [code, setCode] = useState("");
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const requestCode = async (event) => {
    event.preventDefault(); setLoading(true); setError("");
    try { const response = await fetch(`${API_BASE}/auth/phone-otp/request`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ phoneNumber }) }); const data = await response.json(); if (!response.ok) throw new Error(data.message || "Unable to send a code"); setSent(true); } catch (problem) { setError(problem.message || "Network error"); } finally { setLoading(false); }
  };
  const verifyCode = async (event) => {
    event.preventDefault(); setLoading(true); setError("");
    try { const response = await fetch(`${API_BASE}/auth/phone-otp/verify`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ phoneNumber, code }) }); const data = await response.json(); if (!response.ok) throw new Error(data.message || "Unable to verify the code"); onAuth(data); } catch (problem) { setError(problem.message || "Network error"); } finally { setLoading(false); }
  };
  return <form className="gateway-form" onSubmit={sent ? verifyCode : requestCode}><label>Phone number<input type="tel" value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} placeholder="+234 801 234 5678" autoComplete="tel" required /></label>{sent && <label>6-digit code<input inputMode="numeric" maxLength="6" value={code} onChange={(event) => setCode(event.target.value.replace(/\D/g, ""))} placeholder="123456" autoComplete="one-time-code" required /></label>}{sent && <button className="forgot" type="button" onClick={requestCode} disabled={loading}>Send a new code</button>}{error && <p className="auth-error">{error}</p>}<button className="gateway-submit" disabled={loading}>{loading ? "Please wait…" : sent ? "Verify and sign in" : "Send OTP"}</button></form>;
}
function AuthPanel({ onAuth }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    country: "NG",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const update = (event) =>
    setForm({ ...form, [event.target.name]: event.target.value });
  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const body =
        mode === "login"
          ? { email: form.email, password: form.password }
          : form;
      const response = await fetch(
        `${API_BASE}/auth/${mode === "login" ? "login" : "register"}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(body),
        },
      );
      const data = await response.json();
      if (!response.ok)
        throw new Error(data.message || "Unable to authenticate");
      onAuth(data);
    } catch (problem) {
      setError(problem.message || "Network error");
    } finally {
      setLoading(false);
    }
  };
  return (
    <section className="profile-panel auth-panel">
      <div className="auth-heading">
        <div className="modal-icon">
          <Lock />
        </div>
        <h3>{mode === "login" ? "Welcome back" : "Create your wallet"}</h3>
        <p>Sign in to view your protected wallet information.</p>
      </div>
      <form onSubmit={submit}>
        {mode === "register" && (
          <>
            <input
              name="fullName"
              placeholder="Full name"
              value={form.fullName}
              onChange={update}
              required
            />
            <input
              name="phoneNumber"
              placeholder="Phone number"
              value={form.phoneNumber}
              onChange={update}
              required
            />
          </>
        )}
        <input
          name="email"
          type="email"
          placeholder="Email address"
          value={form.email}
          onChange={update}
          required
        />
        <input
          name="password"
          type="password"
          placeholder="Password (12+ characters)"
          minLength="12"
          value={form.password}
          onChange={update}
          required
        />
        {error && <p className="auth-error">{error}</p>}
        <button className="primary" disabled={loading}>
          {loading
            ? "Please wait…"
            : mode === "login"
              ? "Sign in"
              : "Create account"}
        </button>
      </form>
      <button
        className="auth-switch"
        onClick={() => {
          setMode(mode === "login" ? "register" : "login");
          setError("");
        }}
      >
        {mode === "login"
          ? "New here? Create an account"
          : "Already have an account? Sign in"}
      </button>
    </section>
  );
}

createRoot(document.getElementById("root")).render(<App />);
