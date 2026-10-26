# HugzzBotzX — Consent-first Remote Support UI

Starter Android project (Java + Android Gradle Plugin) with a dark remote-support dashboard, local demo pairing code, consent walkthrough, reset control, and GitHub Actions build workflow.

## What works in this starter

- Android app UI and dashboard layout.
- Generates a six-digit **local demo** pairing code.
- Shows a clear consent walkthrough and status indicator.
- Resets the demo session state.
- GitHub Actions builds a debug APK on pushes to `main` and allows manual runs.

## What is not implemented

This repository is **not yet a real remote-control product**. It does not stream the screen, connect two devices, send touch commands, transfer files, or run a relay server. The pairing code is generated on the current device only. The status labels are demo UI, not proof of a real secure connection.

## Safe next-step architecture

1. Add an authenticated pairing service that uses short-lived one-time codes, TLS, rate limits, device identity, and revocation.
2. Add user-initiated screen sharing using Android `MediaProjection`; request consent for each session and show a persistent foreground-service notification.
3. Add an encrypted WebRTC stream and a properly configured signaling/TURN service. Do not expose an unauthenticated raw screen stream to the internet.
4. If remote touch input is required, make it opt-in and visible. Explain the Accessibility permission before opening Android Settings, limit commands to an active approved session, and provide an always-visible stop control.
5. Add audit events, session expiry, blocking of sensitive screens where practical, and a privacy policy before production distribution.

Never collect passwords/OTPs, hide the app from its owner, silently enable microphone/camera, bypass Android permission prompts, or persist remote access after a user ends a session.

## Build on GitHub

1. Create a new GitHub repository named `HugzzBotzX`.
2. Upload/push this project to the `main` branch.
3. Open **Actions** in the repository and select **Build HugzzBotzX Android APK**. It also runs automatically after a push to `main`.
4. Open the successful workflow run, find **Artifacts**, and download `HugzzBotzX-debug-apk`.
5. Extract the downloaded artifact ZIP; it contains `app-debug.apk`. Transfer it to your Android phone and install it. Android may ask you to allow installs from that file manager/browser.

## Build configuration

- Android Gradle Plugin: 8.7.3
- Gradle: 8.9 (installed by GitHub Actions)
- Java: 17
- Compile SDK: 35
- Minimum Android: API 23

This starter intentionally uses only Android platform UI APIs, so it does not require external UI libraries.
