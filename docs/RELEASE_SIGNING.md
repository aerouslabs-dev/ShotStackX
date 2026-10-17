# Release Signing

This document explains how release signing works for ssJanitor, how to set it up locally, and how CI produces signed APKs.

## Overview

ssJanitor uses a standard Android signing configuration:

- **Local builds**: Read signing credentials from `keystore.properties` at the project root. Falls back to debug signing if absent.
- **CI builds**: Decode a Base64-encoded keystore from GitHub Secrets, create `keystore.properties` at build time, then build a signed release APK.

## Generating a Keystore

### Option A: Use the helper script

```bash
./scripts/generate-keystore.sh
```

This will prompt you for passwords and create `ssjanitor-release.jks` and a `keystore.properties` template.

### Option B: Manual generation

```bash
keytool -genkeypair \
    -v \
    -keystore ssjanitor-release.jks \
    -alias ssjanitor \
    -keyalg RSA \
    -keysize 4096 \
    -validity 10000
```

## Setting Up Local Signing

1. Generate the keystore (see above).

2. Create `keystore.properties` in the project root:

```properties
storeFile=ssjanitor-release.jks
keyAlias=ssjanitor
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
```

3. Build a release APK:

```bash
./gradlew assembleRelease
```

The build script automatically detects `keystore.properties` and signs the APK. If the file is absent, it falls back to debug signing.

## Backing Up Your Keystore

Your keystore is critical. If you lose it, you cannot publish updates to your app.

**Backup checklist:**

- [ ] Copy `ssjanitor-release.jks` to a secure location (password manager, encrypted USB, etc.)
- [ ] Store `keystore.properties` separately from the keystore file
- [ ] Document the passwords in a password manager
- [ ] Consider using a dedicated secrets management tool for team projects

**Never:**
- Commit the keystore to Git
- Share the keystore over unencrypted channels
- Store passwords in plain text in documentation or code

## GitHub Actions Setup

### Required Secrets

Add these secrets to your GitHub repository (Settings → Secrets and variables → Actions):

| Secret | Description | Example |
|--------|-------------|---------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file | `base64 < ssjanitor-release.jks \| tr -d '\n'` |
| `KEYSTORE_PASSWORD` | Keystore password | `mypassword` |
| `KEY_ALIAS` | Key alias in the keystore | `ssjanitor` |
| `KEY_PASSWORD` | Key password | `mypassword` |

### How to encode your keystore

```bash
base64 < ssjanitor-release.jks | tr -d '\n'
```

Copy the output and paste it as the `KEYSTORE_BASE64` secret value.

### How CI works

When you push a tag matching `v*`, the release workflow:

1. Decodes the Base64 keystore from `KEYSTORE_BASE64` secret
2. Creates `keystore.properties` from the other secrets
3. Builds a signed release APK
4. Verifies the APK signature
5. Uploads the signed APK to GitHub Releases
6. Cleans up the keystore file (even if the build fails)

If no signing secrets are configured, the build falls back to debug signing.

## Local Release Build

With `keystore.properties` in place:

```bash
./gradlew assembleRelease
```

The signed APK will be at:

```
app/build/outputs/apk/release/app-release.apk
```

Verify the signature:

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### "Keystore file not found"

Ensure `storeFile` in `keystore.properties` points to an existing file. The path is relative to the `app/` directory.

### "Keystore was tampered with, or password was incorrect"

Double-check `storePassword` and `keyPassword` in `keystore.properties`.

### CI build uses debug signing

Verify that all four GitHub Secrets are set:
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

If any secret is missing, the workflow falls back to debug signing.

### APK not signed in CI

Check the workflow logs for the "Decode keystore" step. If it's skipped, the `KEYSTORE_BASE64` secret is empty or not set.

## F-Droid and IzzyOnDroid

F-Droid and IzzyOnDroid build and sign APKs from source using their own keys. You do not need to provide a signed APK for these repositories — they handle signing themselves.

However, having a signed APK is still important for:

- GitHub Releases
- Obtainium
- Direct distribution
- Verified updates (users can verify the APK is from you)
