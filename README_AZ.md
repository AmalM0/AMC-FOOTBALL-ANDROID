# AMC FOOTBALL — Android APK layihəsi

Bu, 19.0.2 saytını `https://amcfootball.netlify.app` ünvanından təhlükəsiz HTTPS WebView-də açan Android proqramıdır. **ZIP APK deyil**; APK-ni aşağıdakı üsulla yığmaq lazımdır. Android 8.0+.

## Pulsuz build: GitHub Actions
1. GitHub hesabında **yeni boş repository** yarat.
2. `android_apk_project` qovluğunun **içindəki** bütün faylları, `.github/workflows/build-apk.yml` daxil olmaqla, həmin repository-nin kökünə yerləşdir.
3. Repository-yə `main` budağına push etdikdə Actions avtomatik çalışmalıdır (və ya Actions > Build AMC FOOTBALL Android APK > Run workflow).
4. Build yaşıl tamamlandıqda Actions > həmin run > **Artifacts** > `AMC-FOOTBALL-Android-APK` paketini yüklə.
5. Artifaktın içində `app-debug.apk` faylı var; öz cihazında test üçün nəzərdə tutulub.

Alternativ: Android Studio ilə layihəni aç, Gradle Sync et və Build > Build APK(s) seç.

## Necə işləyir?
- APK uzaq Netlify saytını göstərir, ona görə PWA paketini ƏVVƏL Netlify-yə deploy et.
- Online Career üçün internet və Supabase lazım olacaq.
- Saytdakı yeniliklər APK daxilində avtomatik görünəcək, APK-ni hər dəfə yığmaq tələb olunmur.
- Android WebView-in lokal yaddaşı Safari, Chrome və digər tətbiqlərdən ayrıdır; hesabına yenidən daxil olmalı ola bilərsən.
- JSON ehtiyat nüsxəsi `Downloads` qovluğuna saxlanır; import zamanı Android fayl seçicisi açılır.
- `app-debug.apk` test üçündür; Google Play üçün release imzası və ayrıca mağaza prosesləri lazımdır.
- Tətbiqi yalnız güvəndiyin yerdən quraşdır, sistemin təhlükəsizlik xəbərdarlıqlarını nəzərə al.

## Kodda dəyişdiriləcək yer
- Sayt ünvanı: `app/src/main/java/com/amalmcodes/amcfootball/MainActivity.java` > `GAME_URL`.
- Paket adı: `app/build.gradle` > applicationId (store-a çıxanda sabit saxlanmalıdır).
- Versiya: `app/build.gradle` > versionCode/versionName.
