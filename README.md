# 📖 Story App

Story App adalah aplikasi Android yang memungkinkan pengguna untuk **berbagi cerita dalam bentuk foto dan deskripsi**, dengan opsi **menggunakan lokasi (latitude & longitude) atau tanpa lokasi**.  
Aplikasi ini dibangun menggunakan **API resmi Dicoding Story API** dan menerapkan **arsitektur modern Android**.

🔗 API: https://story-api.dicoding.dev/v1

---

## ✨ Fitur Utama

- 🔐 **Autentikasi**
  - Register
  - Login
  - Logout
  - Session disimpan menggunakan **DataStore**

- 📝 **Story**
  - Menampilkan daftar story
  - Detail story
  - Upload story (foto + deskripsi)
  - Upload story **dengan atau tanpa lokasi**

- 📷 **Media**
  - Ambil foto dari **kamera**
  - Pilih foto dari **galeri**

- 📍 **Lokasi**
  - Mengambil lokasi pengguna (latitude & longitude)
  - Menampilkan **nama lokasi hasil reverse geocoding**
  - Menampilkan lokasi story di **Google Maps**
  - Story tanpa lokasi **tetap dapat ditampilkan**

- 🗺️ **Map**
  - Menampilkan semua story yang memiliki lokasi
  - Marker berdasarkan latitude & longitude user

---

## 🛠️ Teknologi & Library

- **Bahasa**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Dependency Injection**: Hilt
- **State Management**: StateFlow + ViewModel
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Data Storage**: DataStore Preferences
- **Location**:
  - Fused Location Provider
  - Reverse Geocoding (lat & lon → nama daerah)
- **Maps**:
  - Google Maps Compose
  - API Key disimpan menggunakan `local.properties`
---

## 🧱 Arsitektur

Menggunakan pendekatan **MVVM**:

<img src="https://github.com/ardhaniahlan/jetpack-compose-story-app-dicoding/blob/main/storyapp.gif" width="300" alt="Story App">


