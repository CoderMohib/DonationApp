# App Icon Replacement Instructions

## ✅ Completed Steps

1. ✅ Adaptive icon configuration updated
2. ✅ Icon background created (uses app primary color)
3. ✅ Round icon configuration updated

## 📋 Required Action: Add Your Icon Image

### Step 1: Add Your Icon Foreground Image

1. Place your custom app icon image in: `app/src/main/res/drawable/`
2. Name it: `app_icon_foreground.png`
3. Recommended specifications:
   - Size: 1024x1024px (square)
   - Format: PNG with transparency
   - The icon should be centered in the image with some padding (safe zone: keep important content within 66% of the center)

### Step 2: Replace Legacy Icon Files (Optional but Recommended)

For Android versions below 8.0, you need to replace the legacy icon files in these folders:

- `mipmap-mdpi/ic_launcher.webp` → 48x48px
- `mipmap-hdpi/ic_launcher.webp` → 72x72px
- `mipmap-xhdpi/ic_launcher.webp` → 96x96px
- `mipmap-xxhdpi/ic_launcher.webp` → 144x144px
- `mipmap-xxxhdpi/ic_launcher.webp` → 192x192px

Also replace the round variants:
- `mipmap-*/ic_launcher_round.webp` in each folder

**Easiest Method:** Use Android Studio's Image Asset Studio:
1. Right-click `app/src/main/res`
2. Select **New → Image Asset**
3. Choose **Launcher Icons (Adaptive and Legacy)**
4. Select your icon image
5. Android Studio will automatically generate all required sizes

## 🎨 Current Configuration

- **Background:** Uses app primary color (`@color/primary` - Teal/Blue)
- **Foreground:** Will use `app_icon_foreground.png` (you need to add this)
- **Monochrome:** Uses the same foreground for themed icons

## 📝 Notes

- After adding your icon, rebuild the app
- You may need to uninstall and reinstall the app to see the new icon (Android caches icons)
- The adaptive icon system automatically handles different device shapes (square, rounded square, circle, etc.)

