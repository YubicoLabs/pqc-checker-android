#!/usr/bin/env bash
#
# Sign an unsigned apk
#
# Usage:
#
#   * cd {root of project}
#   * ./gradlew assembleRelease
#   * ./signing/sign.sh
#   * see app-release.apk in root.
#

if command -v apksigner > /dev/null; then
  APK_SIGNER="apksigner"
else
  echo "apksigner not on path, trying ANDROID_HOME (${ANDROID_HOME})"
  APK_SIGNER="$ANDROID_HOME/build-tools/36.0.0/apksigner"
fi

$APK_SIGNER sign \
    --ks signing/standalone.jks \
    --ks-pass env:PQC_KEYSTORE_PASSWORD \
    --next-signer \
    --ks signing/hybrid.jks --ks-key-alias hybrid-classical \
    --ks-pass env:PQC_KEYSTORE_PASSWORD \
    --signer-lineage signing/classical-lineage --hybrid-signer-role classical \
    --next-signer \
    --ks signing/hybrid.jks --ks-key-alias hybrid-pqc \
    --ks-pass env:PQC_KEYSTORE_PASSWORD \
    --signer-lineage signing/pqc-lineage --hybrid-signer-role pqc \
    --out app-release.apk \
    app/build/outputs/apk/release/app-release-unsigned.apk
