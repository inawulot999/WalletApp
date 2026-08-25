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

## Run

You can run this project with your installed JDK 26.x. The Maven compiler target stays on Java 21 bytecode because the current Spring Boot stack used here does not scan Java 26 class files cleanly yet.

```powershell
mvn spring-boot:run
```

The API runs on `http://localhost:8080`.

Before starting the secured API, set a random JWT signing secret (at least 32 characters):

```powershell
$env:WALLET_JWT_SECRET = "replace-with-a-long-random-secret"
```

Register with `POST /api/v1/auth/register`, then send the returned token on protected requests as `Authorization: Bearer <accessToken>`.

## Render deployment

The repository includes `render.yaml` for a Docker-based Render web service. In Render, choose **New > Blueprint**, select this GitHub repository, and create the `wallet-api` service. Render generates `WALLET_JWT_SECRET` automatically and uses the public GitHub Pages URL as the allowed browser origin.

This first hosted deployment uses the existing H2 demo database, which can be reset when the service restarts. Do not use it for customer balances or real money movement. Provisioning PostgreSQL and Flyway migrations is required before production use.

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

## Compliance note

Use this MVP for internal testing, partner demos, architecture validation, and regulator-readiness preparation only. Do not use it to hold customer funds, operate remittance, run exchange services, or move virtual assets without the relevant CBN/SEC approval or licensed partner arrangement.
