# Dior Wallet MVP

This is a compliance-safe Java/Spring Boot MVP for a Nigeria-focused wallet and remittance product.

The app does not connect to banks, crypto exchanges, payment switches, blockchains, or real remittance rails. All transfers are simulated so you can build and demo the product before applying for licences or signing licensed partners.

## What is included

- User onboarding
- KYC submission and demo approval
- Wallet balances by currency
- Double-entry ledger entries for every simulated movement
- Cross-border transfer quotes and simulated execution
- Exchange-wallet transfer quote preview, marked as coming soon for execution
- Dior Wallet UID transfer setup
- Compliance gate that blocks transfers until the user is KYC verified
- Live crypto price tracker powered by the public CoinGecko API

## Run

You can run this project with your installed JDK 26.x. The Maven compiler target stays on Java 21 bytecode because the current Spring Boot stack used here does not scan Java 26 class files cleanly yet.

```powershell
mvn spring-boot:run
```

The API runs on `http://localhost:8080`.

## Demo flow

Create a user:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/users `
  -ContentType 'application/json' `
  -Body '{"fullName":"Popoola Toluwani Faith","email":"inawulot999@gmail.com","phoneNumber":"+2349012208818","country":"NG"}'
```

Submit KYC:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/users/{userId}/kyc `
  -ContentType 'application/json' `
  -Body '{"bvn":"12345678901","nin":"12345678901","residentialAddress":"Ibadan, Oyo State"}'
```

Approve KYC for demo:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/users/{userId}/kyc/approve
```

Fund wallet with simulated money:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/wallets/{userId}/fund `
  -ContentType 'application/json' `
  -Body '{"currency":"NGN","amount":100000,"memo":"Demo funding"}'
```

Quote a cross-border transfer:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/transfers/quote `
  -ContentType 'application/json' `
  -Body '{"sourceCurrency":"NGN","targetCurrency":"USD","sourceAmount":25000,"transferType":"CROSS_BORDER"}'
```

Simulate transfer:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/transfers/simulate `
  -ContentType 'application/json' `
  -Body '{"sourceUserId":"{userId}","sourceCurrency":"NGN","targetCurrency":"USD","sourceAmount":25000,"transferType":"CROSS_BORDER","recipientName":"Sample Recipient","destinationCountry":"US","destinationReference":"US-BANK-SANDBOX-001"}'
```

Track live crypto prices:

```powershell
Invoke-RestMethod http://localhost:8080/api/crypto/prices?page=1&perPage=100&vsCurrency=usd
```

Look up a single asset:

```powershell
Invoke-RestMethod http://localhost:8080/api/crypto/prices/bitcoin?vsCurrency=usd
```

The tracker paginates through the full CoinGecko market list. Increase `page` to fetch the next batch of assets. Responses are cached for 60 seconds to stay within public API limits.

## Compliance note

Use this MVP for internal testing, partner demos, architecture validation, and regulator-readiness preparation only. Do not use it to hold customer funds, operate remittance, run exchange services, or move virtual assets without the relevant CBN/SEC approval or licensed partner arrangement.
