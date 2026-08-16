# Reply to Apple App Review — Guideline 2.1 Information Needed

Paste this into the **Notes** field of App Review Information (or directly
into "Reply to App Review" in the Resolution Center), covering items 2–7 of
Apple's request. Item 1 (screen recording) has to be attached separately —
see the bottom of this doc for how.

---

**2. Devices/OS tested on before submitting:**
Extensively tested on iOS Simulator (iPhone 17 and iPhone 17 Pro Max, iOS
26.5) throughout development, and on a physical iPhone before this
resubmission.

**3. App description — functions, target audience, problem solved:**
TN Tricare Helper is a free guide for Tennessee Army and Air National Guard
members (traditional/drilling status) to (1) understand and complete
enrollment in TRICARE Reserve Select, the federal health plan for Selected
Reserve members, via the official DoD milConnect portal, and (2) apply for
the Tennessee Medical Readiness Act's state premium reimbursement program.
The app provides eligibility checklists, current premium/deadline
information, and in-app tools to fill out the required Enrollment,
Attestation, and IRS W-4 forms using the real official government PDF
templates — completed forms can be reviewed and shared/emailed by the user
to the Tennessee Department of Military. It solves the problem of TN Guard
members having to piece together scattered instructions and paperwork from
multiple state and federal sources by consolidating everything into one
guided workflow.

**4. Setup/access instructions, credentials, or sample files:**
The app requires no login, account creation, or sample files — every
feature is available immediately on first launch after a one-time
disclaimer screen. To review the core flow: launch the app → Home dashboard
→ TRS tab (enrollment info and eligibility) → Reimbursement tab → tap any
of the three "Fill out the forms" cards (Medical Readiness Act Enrollment,
Attestation Form, or Form W-4) to see the in-app PDF form-fill feature →
enter any sample values → tap "Preview & Share PDF" to see a real filled
PDF generated. No test data or credentials are needed anywhere in the app.

**5. External services/tools/platforms used:**
The only external service used is a single unauthenticated GET request on
launch to a public, static JSON file hosted on GitHub
(raw.githubusercontent.com) containing non-personalized program content
(links, premium amounts, deadlines) for over-the-air content updates. No
authentication services, payment processors, analytics SDKs, or AI services
are used. The "Open milConnect" button in the TRS tab opens the real DoD
milConnect Beneficiary Web Enrollment site (milconnect.dmdc.osd.mil) in an
in-app browser — this is an official DoD site, not a service integrated by
the developer, and requires a DoD-issued credential (CAC or DS Logon) to
sign into, which the reviewer will not have. This is expected; the rest of
the app's functionality does not depend on being able to sign into that
site.

**6. Regional differences:**
The app functions identically for all users and does not vary by region.
Its content is specific to Tennessee National Guard members by design —
that is the app's stated purpose, not a regional variation of a broader
feature set.

**7. Protected third-party material / regulated industry documentation:**
The app bundles two categories of PDF documents, both public U.S.
government works with no usage restrictions: (a) the 2026 IRS Form W-4,
published by the Internal Revenue Service and freely distributable as a
U.S. federal government work (not subject to copyright under 17 U.S.C. §
105), and (b) the Tennessee Medical Readiness Act Enrollment and Supporting
Documents packet, published by the Tennessee Department of Military at
tn.gov specifically for members to download, complete, and submit. Neither
requires special authorization to include or distribute. The app does not
operate in a regulated industry itself — it is an informational/utility
tool that helps users complete real government forms which they then
submit through the normal official channels; it does not provide medical,
legal, or financial advice.

---

## Screen recording (item 1) — do this on the physical iPhone

Apple requires this captured on a real device, not the Simulator.

1. **Install the app on your iPhone** (already connected to your Mac, saw
   it as "Marc Perrusquia's iPhone" in Xcode's device list):
   - In Xcode, open the TNGuardTricare project, set the run destination
     (top bar) to your iPhone instead of a simulator, press ▶ Run.
2. **Enable Screen Recording in Control Center** if it's not already there:
   Settings → Control Center → add "Screen Recording".
3. **Start recording**: open Control Center, tap the screen recording
   button, wait for the 3-second countdown.
4. **Walk through the app** — this is the "typical user flow through core
   features" Apple wants to see:
   - Launch the app (record from the very start, including the disclaimer
     screen)
   - Home tab
   - TRS tab — scroll through eligibility and steps
   - Reimbursement tab — scroll through eligibility and steps
   - Tap into the Medical Readiness Act Enrollment form, fill a few fields,
     tap "Preview & Share PDF" to show the real filled government PDF
   - Back out, tap Resources tab
   - Tap Settings tab
5. **Stop recording**: tap the red status bar at the top, or Control Center
   again.
6. The video saves to **Photos**. AirDrop it to your Mac (or just attach it
   straight from Photos if the App Store Connect reply form on your phone's
   browser supports it).
7. In App Store Connect, go to the rejection message → **Reply to App
   Review** → attach the video file + paste the text above into the notes.
