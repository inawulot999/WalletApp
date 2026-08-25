import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Bell, Search, Eye, EyeOff, ArrowDownToLine, ArrowUpFromLine, ArrowLeftRight, Send, UsersRound, Home, ChartNoAxesCombined, WalletCards, UserRound, ChevronDown, ArrowDownUp, ShieldCheck, Fingerprint, Settings, Plus, MoreHorizontal, X, Check, LockKeyhole, ScanFace, Apple, CreditCard, CircleHelp } from 'lucide-react';
import './styles.css';

const rate = 1500;
const assets = {
  BTC:{ name:'Bitcoin', price:65000, icon:'₿', color:'#f7931a', change:2.84, data:'2,13 10,16 18,8 26,12 35,6 43,9 52,3 61,7 70,1' },
  ETH:{ name:'Ethereum', price:3540, icon:'♦', color:'#627eea', change:1.26, data:'2,16 10,10 18,14 26,7 35,11 43,4 52,9 61,2 70,5' },
  SOL:{ name:'Solana', price:142.8, icon:'≋', color:'#9a6bff', change:-0.74, data:'2,4 10,8 18,5 26,13 35,9 43,15 52,12 61,17 70,19' },
  USDT:{ name:'Tether USD', price:1, icon:'₮', color:'#26a17b', change:0.01, data:'2,11 10,11 18,10 26,11 35,10 43,11 52,10 61,11 70,10' },
  USD:{ name:'US Dollar', price:1, icon:'$', color:'#6e7d90', change:0, data:'2,11 70,11'},
  NGN:{ name:'Nigerian Naira', price:1/rate, icon:'₦', color:'#119a60', change:0, data:'2,11 70,11'}
};
const fmt = (num, decimals=2) => new Intl.NumberFormat('en-US',{minimumFractionDigits:decimals,maximumFractionDigits:decimals}).format(num);
const money = (v,currency='USD') => currency==='NGN' ? `₦${fmt(v*rate)}` : `$${fmt(v)}`;
function AssetIcon({symbol, small=false}) { const a=assets[symbol]; return <span className={'asset-icon '+(small?'small':'')} style={{background:a.color}}>{a.icon}</span> }
function Spark({asset}) { let a=assets[asset]; return <svg className="spark" viewBox="0 0 72 22" preserveAspectRatio="none"><polyline points={a.data} fill="none" stroke={a.change<0?'#f6465d':'#0ecb81'} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></svg> }

function App(){
 const [tab,setTab]=useState('Home'), [market,setMarket]=useState('Hot'), [visible,setVisible]=useState(true), [currency,setCurrency]=useState('USD'), [modal,setModal]=useState(null), [from,setFrom]=useState('USDT'), [to,setTo]=useState('NGN'), [amount,setAmount]=useState('100'), [toast,setToast]=useState('');
 const total=2450, display=currency==='NGN'?total*rate:total;
 const converted=useMemo(()=>{ const usd=Number(amount||0)*assets[from].price; return usd/assets[to].price },[amount,from,to]);
 const changeTab = (next) => {setTab(next); if(next==='Convert') setTimeout(()=>document.querySelector('.converter')?.scrollIntoView({behavior:'smooth'}),10)};
 const action=(title)=> { if(title==='Convert') {setTab('Convert'); setTimeout(()=>document.querySelector('.converter')?.scrollIntoView({behavior:'smooth'}),10)} else setModal(title==='Withdraw'?'security':'action') };
 const confirm=()=>{setModal(null);setToast('Conversion request secured and submitted');setTimeout(()=>setToast(''),2800)};
 return <main className="app-shell">
  <div className="status"><span>9:41</span><span>● ● ● &nbsp;▰</span></div>
  <header><div className="avatar">AO</div><div className="kyc"><ShieldCheck size={13}/> KYC Verified</div><button className="search"><Search size={17}/><span>Search assets</span></button><button className="icon-button"><Bell size={20}/><i/></button></header>
  <section className="balance-card"><div className="balance-head"><span>Total assets</span><button onClick={()=>setVisible(!visible)}>{visible?<Eye size={17}/>:<EyeOff size={17}/>}</button><span className="balance-menu"><MoreHorizontal size={19}/></span></div><div className="balance-value">{visible?money(display,currency):'••••••••'}</div><div className="naira-line">{visible ? currency==='USD' ? `≈ ₦${fmt(total*rate)}` : `≈ $${fmt(total)}` : '••••••••'}</div><div className="pnl"><span>24h P&L</span><b>+$42.60 <em>+1.77%</em></b></div><div className="card-footer"><span>Spot account</span><span>View assets <ChevronDown size={14}/></span></div></section>
  <section className="actions">{[[ArrowDownToLine,'Deposit'],[ArrowUpFromLine,'Withdraw'],[ArrowLeftRight,'Convert'],[Send,'Transfer'],[UsersRound,'P2P']].map(([Icon,label])=><button key={label} onClick={()=>action(label)}><span><Icon size={19}/></span>{label}</button>)}</section>
  <section className="section-title"><div><h2>Markets</h2><p>Top movers right now</p></div><button onClick={()=>setTab('Markets')}>View all</button></section>
  <div className="market-tabs">{['Hot','Gainers','Losers','24h Volume'].map(x=><button className={market===x?'active':''} onClick={()=>setMarket(x)} key={x}>{x}</button>)}</div>
  <section className="watchlist">{['BTC','ETH','SOL','USDT'].map((ticker,i)=>{let a=assets[ticker], neg=market==='Losers'?i%2===0: a.change<0; let c=neg?-Math.abs(a.change):Math.abs(a.change);return <div className="market-row" key={ticker}><AssetIcon symbol={ticker}/><div className="coin-name"><b>{ticker}</b><small>{a.name}</small></div><div className="price"><b>${fmt(a.price,a.price<10?4:2)}</b><small>₦{fmt(a.price*rate, a.price<10?2:0)}</small></div><Spark asset={ticker}/><span className={'change '+(c<0?'down':'')}>{c>0?'+':''}{fmt(c)}%</span></div>})}</section>
  <section className="converter" id="convert"><div className="converter-title"><div><span className="amber-dot"/> Instant convert</div><button onClick={()=>setModal('rate')}><CircleHelp size={18}/></button></div><p>Zero-fee conversions at the current rate</p><div className="convert-box"><label>From</label><div className="input-row"><select value={from} onChange={e=>setFrom(e.target.value)}>{Object.keys(assets).map(x=><option key={x}>{x}</option>)}</select><input value={amount} onChange={e=>setAmount(e.target.value.replace(/[^0-9.]/g,''))} inputMode="decimal"/><span className="unit">{from}</span></div></div>
  <button className="swap" onClick={()=>{let x=from;setFrom(to);setTo(x)}}><ArrowDownUp size={18}/></button>
  <div className="convert-box to"><label>To</label><div className="input-row"><select value={to} onChange={e=>setTo(e.target.value)}>{Object.keys(assets).map(x=><option key={x}>{x}</option>)}</select><div className="output">{fmt(converted, converted<1?6:2)}</div><span className="unit">{to}</span></div></div>
  <div className="rate"><span>Live exchange rate</span><b>1 {from} = {fmt(assets[from].price/assets[to].price, to==='NGN'?2:6)} {to}</b></div><button className="convert-btn" onClick={()=>setModal('security')}>Convert instantly <ArrowLeftRight size={18}/></button></section>
  {tab==='Profile'&&<section className="profile-panel"><div className="profile-top"><div className="avatar big">AO</div><div><h3>Amara Okafor</h3><p>amara@vault.ng</p></div><ChevronDown size={18}/></div><div className="profile-status"><div><ShieldCheck/><span><b>Level 2 verified</b><small>Identity & address confirmed</small></span><Check size={17}/></div><div><LockKeyhole/><span><b>Security score: strong</b><small>2FA and biometrics enabled</small></span><Settings size={17}/></div><div><CreditCard/><span><b>2 bank accounts</b><small>Naira payouts available</small></span><ChevronDown size={17}/></div></div><div className="pref"><span>Display currency</span><button onClick={()=>setCurrency(currency==='USD'?'NGN':'USD')}>{currency==='USD'?'$ USD':'₦ NGN'} <ChevronDown size={15}/></button></div></section>}
  <nav>{[[Home,'Home'],[ChartNoAxesCombined,'Markets'],[ArrowLeftRight,'Convert'],[WalletCards,'Wallet'],[UserRound,'Profile']].map(([Icon,name])=><button className={tab===name?'selected':''} onClick={()=>changeTab(name)} key={name}><Icon size={21}/><span>{name}</span></button>)}</nav>
  {toast&&<div className="toast"><Check size={17}/>{toast}</div>}
  {modal&&<Modal type={modal} close={()=>setModal(null)} confirm={confirm}/>} 
 </main>
}
function Modal({type,close,confirm}){const [pin,setPin]=useState('');const security=type==='security';return <div className="overlay"><div className="modal"><button className="close" onClick={close}><X size={20}/></button>{security?<><div className="modal-icon"><LockKeyhole/></div><h3>Confirm with security check</h3><p>Enter your 4-digit trading PIN to continue.</p><input className="pin" maxLength="4" value={pin} onChange={e=>setPin(e.target.value.replace(/\D/g,''))} placeholder="••••" inputMode="numeric"/><button className="primary" disabled={pin.length<4} onClick={confirm}>Continue</button><button className="bio"><Fingerprint size={19}/> Use biometric verification</button></>:<><div className="modal-icon"><ScanFace/></div><h3>{type==='Withdraw'?'Secure withdrawal':'Ready when you are'}</h3><p>{type==='rate'?'Rates refresh continuously. Your final rate is locked at confirmation.':'This action is protected by PIN, two-factor authentication and biometric checks.'}</p><button className="primary" onClick={close}>Got it</button></>}</div></div>}
createRoot(document.getElementById('root')).render(<App/>);
