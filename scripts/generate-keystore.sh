#!/bin/bash
set -euo pipefail

# ssJanitor Release Keystore Generator
# This script generates a release signing keystore for the ssJanitor Android app.

KEYSTORE_FILE="ssjanitor-release.jks"
KEY_ALIAS="ssjanitor"
KEY_SIZE=4096
VALIDITY=10000

echo "=== ssJanitor Release Keystore Generator ==="
echo ""
echo "This will create a release keystore for signing the app."
echo "You will be prompted to enter a keystore password and key password."
echo ""
echo "IMPORTANT: Back up this keystore file and passwords securely."
echo "          If you lose them, you cannot update your app on app stores."
echo ""

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo "ERROR: keytool not found. Install Java JDK and ensure keytool is in PATH."
    exit 1
fi

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "WARNING: $KEYSTORE_FILE already exists."
    read -p "Overwrite? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 0
    fi
fi

# Generate the keystore
keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize "$KEY_SIZE" \
    -validity "$VALIDITY"

echo ""
echo "Keystore created: $KEYSTORE_FILE"
echo ""

# Create keystore.properties
cat > keystore.properties << EOF
storeFile=$KEYSTORE_FILE
keyAlias=$KEY_ALIAS
storePassword=CHANGE_ME
keyPassword=CHANGE_ME
EOF

echo "Created keystore.properties template."
echo ""
echo "=== Next Steps ==="
echo "1. Edit keystore.properties and replace CHANGE_ME with your actual passwords."
echo "2. Back up ssjanitor-release.jks and keystore.properties securely."
echo "3. Never commit these files to Git (they are in .gitignore)."
echo "4. For GitHub Actions, add these secrets:"
echo "   - KEYSTORE_BASE64: base64 < $KEYSTORE_FILE | tr -d '\\n'"
echo "   - KEYSTORE_PASSWORD: <your store password>"
echo "   - KEY_ALIAS: $KEY_ALIAS"
echo "   - KEY_PASSWORD: <your key password>"
echo ""
echo "Done!"
