# Updating app content without a store release

All links, premiums, deadlines, phone numbers, eligibility copy, and form
field definitions live in one file: [`content/content.json`](../content/content.json),
documented by [`content/content.schema.json`](../content/content.schema.json).

OTA hosting is live: the iOS app fetches
[`https://raw.githubusercontent.com/mpperrusquia-ui/tn-guard-tricare/main/content/content.json`](https://raw.githubusercontent.com/mpperrusquia-ui/tn-guard-tricare/main/content/content.json)
in the background on every launch (see `remoteURL` in
`ios/TNGuardTricare/Stores/ContentStore.swift`), falling back silently to the
bundled copy if that's ever unreachable.

## Making an edit

1. Edit `content/content.json`.
2. Bump `contentVersion` by 1 and update `updatedAt` to today's date. The app
   ignores a fetched file whose `contentVersion` is lower than what it already
   has, so this is required for updates to actually apply.
3. Copy the updated file into each platform's bundled-fallback location so a
   fresh install always has current data even before its first network fetch:
   - iOS: `ios/TNGuardTricare/Resources/Content/content.json`
   - Android (not yet built): `android/app/src/main/assets/content.json`
4. Commit and push to `main` on GitHub. That's the whole release — no App
   Store build, no review. Installed apps pick it up next launch, or
   immediately via Settings → "Check for content updates".

## Adding or changing a form field

Each entry in `tnReimbursement.forms[].fields` drives both the in-app input
UI and the PDF fill step. Two fill modes, set per form via `fillMode`:

- **`"acroform"`** (Enrollment, W-4): the bundled PDF has real interactive
  fields. Set each field's `pdfFieldName` to the PDF's actual AcroForm field
  name — `PDFFormFiller.swift` matches it across every page (handles both
  flat names and the fully-qualified dotted names XFA-style forms like the
  W-4 use) and calls the standard PDFKit fill API. `type: "choice"` fields
  need an `options` array whose values are the PDF's exact export strings
  (e.g. Branch's real options are `["ARNG", "ANG", "STATE GUARD"]`) —
  inspect a form's real fields/options with pypdf before wiring one up:
  `PdfReader(path).get_fields()` and `field.get('/Opt')`.
- **`"overlay"`** (Attestation): the bundled PDF page is real but flat/
  non-interactive (just printed text with blank lines) — there's nothing to
  set programmatically, so the app draws entered values as plain text on top
  of the real page. Each field needs `overlays: [{page, x, y}]` (top-left
  origin points; `x`/`y` are where to draw), or for a `"choice"` field,
  `overlayOptions: [{value, page, x, y}]` (draws an "X" for whichever option
  matches). Get exact coordinates with `pdfplumber`:
  `page.extract_words()` returns each blank line's `x0`/`top` directly in
  this same coordinate space — use the blank's own `(x0, top)` as the draw
  anchor.

Set `sensitive: true` on any field that should be Keychain-only (SSN, bank
routing/account numbers) rather than stored in a plain file.

`outputPage` (0-based) trims the shared PDF down to just that one page when
the bundled file has other enclosures/instructions bundled alongside it
(e.g. the TN packet is 3 pages; Enrollment and Attestation each only want
their own page in the file the user actually sends). Leave it unset to keep
the whole bundled document, e.g. the W-4's legitimate IRS instruction pages.

## Adding a new real PDF template

1. Get the official PDF and inspect it with pypdf (see above) to determine
   whether it's a real fillable AcroForm or a flat page.
2. Add it to `Resources/Forms/<name>.pdf` and reference that filename as
   `pdfTemplate` in `content.json`.
3. Wire up `fillMode` and each field's `pdfFieldName` (acroform) or
   `overlays`/`overlayOptions` (overlay) as above.
4. Remove `isPlaceholderTemplate` (or set it `false`) — that flag only
   controls the in-app "this is a placeholder" warning banner.
5. **Verify against Apple's actual PDFKit rendering**, not just that the code
   runs — this project has already hit two rendering bugs (freshly-built
   annotations don't get an appearance stream regenerated; combo/choice
   fields need real AcroForm backing) that only showed up when the output
   was actually opened in Preview, not from the fill code "succeeding."
   A quick way to check without going through the Simulator: a standalone
   `swift` script importing `PDFKit`/`AppKit` on macOS, using
   `PDFPage.thumbnail(of:for:)` to render straight to a PNG.
