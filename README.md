# TN Guard Tricare Helper

A guided, "spoon-fed" app for Tennessee Army/Air National Guard soldiers and
Airmen to (1) enroll in federal TRICARE Reserve Select via milConnect/BWE, and
(2) enroll in the TN Medical Readiness Act premium reimbursement program —
including filling and sharing the required forms.

## Status

- **iOS**: Milestone 1 in progress — SwiftUI app with the full feature set
  (Home dashboard, TRS flow, TN Reimbursement flow with in-app PDF form fill,
  Resources/FAQ, Settings, disclaimer). Uses a synthesized placeholder PDF
  template for the attestation/W-4 forms until the official tn.gov PDFs are
  bundled (see Risks in the plan / `docs/CONTENT_UPDATE_GUIDE.md`).
- **Android**: not started (Milestone 2, planned as a Kotlin/Jetpack Compose
  port of the iOS app once the iOS reference implementation is verified).

## Repo layout

```
content/
  content.schema.json   documents every content field
  content.json           single source of truth: links, premiums, deadlines, copy, form fields
  design-tokens.json     coyote-brown color palette, spacing, type scale
ios/
  project.yml             xcodegen spec — run `xcodegen generate` to (re)produce the .xcodeproj
  TNGuardTricare.xcodeproj (generated, not hand-edited)
  TNGuardTricare/          SwiftUI source
android/                 (Milestone 2)
docs/
  CONTENT_UPDATE_GUIDE.md how to edit content.json and host it for OTA updates
```

## Working on the iOS app

Requires Xcode 15+ and [XcodeGen](https://github.com/yonaskolb/XcodeGen)
(`brew install xcodegen`). The `.xcodeproj` is generated, not checked in by
hand — after editing `ios/project.yml` or adding/removing files, regenerate:

```bash
cd ios && xcodegen generate
```

Then open `ios/TNGuardTricare.xcodeproj` in Xcode, or build headlessly with
`xcodebuild`.

## Design

Coyote brown (`#8A6E52`) primary, warm sand background, muted olive/slate
accent — a clean modern benefits-app look with earth tones, deliberately
avoiding camo patterns, stencil fonts, or high-contrast tactical UI. Full
palette and type scale in `content/design-tokens.json`.

## Privacy

The app is fully local — no accounts, no backend. Sensitive fields (SSN, bank
routing/account numbers) are stored in the iOS Keychain; everything else in a
plain file in the app's sandbox. Nothing is transmitted anywhere except when
the user explicitly shares a generated PDF (e.g. via Mail).
