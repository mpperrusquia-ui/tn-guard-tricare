# App Store Connect listing — draft copy

Paste these into App Store Connect when creating the app record. Nothing
here is final — edit freely before submitting.

## App Information

- **Name**: TN Guard Tricare Helper
- **Subtitle** (30 chars max): TRICARE enrollment, spoon-fed
- **Bundle ID**: org.tnguardtricare.app
- **Primary category**: Medical
- **Secondary category** (optional): Finance
- **Privacy Policy URL**: https://mpperrusquia-ui.github.io/tn-guard-tricare/privacy.html
- **Support URL**: https://github.com/mpperrusquia-ui/tn-guard-tricare/issues *(or your own support page/email — GitHub Issues works if you don't have another one yet)*

## Promotional text (170 chars, editable without a new build)

> Everything a TN Guard soldier needs to enroll in TRICARE Reserve Select
> and get the state to pay back the premium — links, forms, and instructions
> in one place.

## Description

```
TN Guard Tricare Helper walks Tennessee Army and Air National Guard members
through two things, step by step:

1. ENROLL IN TRICARE RESERVE SELECT (TRS)
Eligibility checklist, current premium amounts, and a guided link straight
into the official milConnect Beneficiary Web Enrollment portal — with
regional contractor phone numbers if you'd rather call it in.

2. GET REIMBURSED BY THE STATE
The Tennessee Medical Readiness Act reimburses your individual TRS/dental
premium. This app fills the real state Enrollment form and Attestation form
for you — plus the federal W-4 — right on your phone, so you can review and
email the completed package straight to tntricare@tn.gov. A monthly
reminder keeps you from missing the resubmission deadline.

Everything you type stays on your device. Sensitive fields (SSN, bank
account info) are stored in the iOS Keychain. Nothing is transmitted
anywhere unless you personally choose to share a filled-out form.

This is an unofficial helper tool built for TN Guard members and is not
affiliated with or endorsed by the Department of Defense, TRICARE, or the
Tennessee Department of Military. Always verify current details on the
official sites linked in the app.
```

## Keywords (100 chars, comma-separated, no spaces after commas)

```
tricare,national guard,tennessee,trs,reserve select,military,reimbursement,milconnect,w4,benefits
```

## What's New (this version)

```
Initial release: TRS enrollment guide, TN Medical Readiness Act reimbursement
walkthrough, real-form PDF filling for the Enrollment, Attestation, and W-4
forms, and a monthly submission reminder.
```

## Age Rating questionnaire

Answer "None"/"No" to all content categories (violence, mature themes,
gambling, etc.) — this app has none of it. Expected result: **4+**.

## App Privacy (data collection) questionnaire

This is the one that matters most given the app touches SSN/bank fields —
answer accurately, not defensively:

- **Does this app collect data?** No — select **"Data Not Collected"** for
  every category (Financial Info, Contact Info, Identifiers, etc.). The app
  has no backend and never transmits what you enter. It's stored locally
  (Keychain for sensitive fields) and only leaves the device if *you*
  explicitly use the share sheet — Apple's guidance treats developer-side
  transmission as the "collection" trigger, and this app has none.
- One nuance: the app does make a network request on launch to fetch
  `content.json` from GitHub for the OTA content-update mechanism. That
  request carries no user data (no query params, no device identifiers,
  just a static file fetch) — it doesn't change the "Data Not Collected"
  answer, but if Apple's review asks, this is the explanation.

## Screenshots

`docs/screenshots/` has 4 ready to upload, captured on the iPhone 17 Pro Max
simulator (6.9" class, 1320×2868 — Apple's current largest-required size):

1. `01-home.png` — Home dashboard, the two-step overview
2. `02-trs.png` — TRS Enrollment tab: premiums, eligibility, steps
3. `03-reimbursement.png` — Reimbursement tab: program eligibility and steps
4. `04-form-fill.png` — the real Enrollment form's in-app fill screen

These are minimum-viable — worth adding 2-3 more (dark mode, Resources tab,
a filled PDF preview) before submitting, since more screenshots generally
help conversion. To generate more: boot the iPhone 17 Pro Max simulator,
run the app, `xcrun simctl io <udid> screenshot <path>.png`.

## Review notes (for Apple's reviewer)

```
This app is an unofficial helper for Tennessee National Guard members
navigating TRICARE Reserve Select enrollment (a real DoD benefit) and the
Tennessee Medical Readiness Act state reimbursement program. It has no
backend, no accounts, and no login — all functionality is available
immediately on launch. The "Open milConnect" button in the TRS tab opens
the real DoD Beneficiary Web Enrollment site in an in-app browser; a valid
DoD credential (CAC/DS Logon) is required to sign in there, which the
reviewer likely won't have — this is expected, the button itself and the
rest of the app's functionality (checklists, PDF form filling, resources)
work fully without it.
```
