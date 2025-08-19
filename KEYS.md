# Android Keystore Setup

1. **Generate keystore**

```bash
keytool -genkeypair -v \
  -keystore ssplite-release.keystore \
  -alias ssplite \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass <STORE_PASS> -keypass <KEY_PASS>
```

2. **Create GitHub secrets**

- `KEYSTORE_BASE64` – base64 of `ssplite-release.keystore`
- `KEY_ALIAS` – `ssplite`
- `KEYSTORE_PASSWORD` – `<STORE_PASS>`
- `KEY_PASSWORD` – `<KEY_PASS>`

3. **Workflow**

The workflow in `.github/workflows/android-release.yml` decodes the keystore and builds a signed APK.
