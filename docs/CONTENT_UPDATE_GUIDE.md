# Updating app content without a store release

All links, premiums, deadlines, phone numbers, eligibility copy, and form
field definitions live in one file: [`content/content.json`](../content/content.json),
documented by [`content/content.schema.json`](../content/content.schema.json).

## Making an edit

1. Edit `content/content.json`.
2. Bump `contentVersion` by 1 and update `updatedAt` to today's date. The app
   ignores a fetched file whose `contentVersion` is lower than what it already
   has, so this is required for updates to actually apply.
3. Copy the updated file into each platform's bundled-fallback location so a
   fresh install always has current data even before its first network fetch:
   - iOS: `ios/TNGuardTricare/Resources/Content/content.json`
   - Android (Milestone 2): `android/app/src/main/assets/content.json`
4. If you host `content.json` remotely (recommended — see below), upload the
   updated file there too. That's what makes the update reach installed apps
   without an App Store/Play Store release.

## Hosting content.json for over-the-air updates

The simplest option: put `content/content.json` in a public GitHub repo and
reference its raw URL, e.g.
`https://raw.githubusercontent.com/<org>/<repo>/main/content/content.json`.
Any commit to that file updates the content the next time an installed app
fetches it (on launch, or via Settings → "Check for content updates").

To wire this up:
- iOS: set `remoteURL` in `ios/TNGuardTricare/Stores/ContentStore.swift` to
  the hosted URL (currently `nil`, so the app only uses the bundled copy).
- Android (Milestone 2): equivalent constant in the Kotlin `ContentRepository`.

A small static host (S3, Firebase Hosting, GitHub Pages) works just as well
and avoids depending on GitHub's raw-content CDN for production traffic.

## Adding or changing a form field

Each entry in `tnReimbursement.forms[].fields` drives both the in-app input
UI and the PDF fill step — add a field there (with a unique `id` and the PDF's
real AcroForm field name as `pdfFieldName`) and it will automatically appear
in the form screen and get written into the generated PDF. Set `sensitive:
true` for anything that should be Keychain-only (SSN, bank routing/account
numbers, DoD ID) rather than stored in a plain file.

## Swapping in the real official PDF templates

`tnReimbursement.forms[].pdfTemplate` names a file expected at
`ios/TNGuardTricare/Resources/Forms/<pdfTemplate>` (and the Android
equivalent in Milestone 2). Until the real fillable PDFs from tn.gov are
added there, the app synthesizes an equivalent placeholder template on-device
(see `PDFFormFiller.swift`) so the fill → preview → share flow works today.
To switch to the real form:

1. Download the official PDF (attestation / W-4) from the links in
   `tnReimbursement.links`.
2. Confirm it's a fillable AcroForm (open in Preview/Acrobat and check for
   fillable fields). If it isn't, it'll need OCR/redesign work — flag that
   rather than silently keeping the placeholder.
3. Rename its form fields (or note their existing names) so each one matches
   the `pdfFieldName` values in `content.json`, or update `pdfFieldName` to
   match the real PDF's field names instead.
4. Add the PDF file to `Resources/Forms/` under the exact `pdfTemplate` name.
5. Set `isPlaceholderTemplate` to `false` (or remove it) for that form in
   `content.json` so the in-app "placeholder" warning banner goes away.
