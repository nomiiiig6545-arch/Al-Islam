package com.example.data.model

import com.example.R

data class Reciter(
    val id: String,
    val name: String,
    val nameArabic: String = "",
    val serverUrl: String,
    val subtext: String = "Rewayat Hafs 'an 'Aasim",
    val photoUrl: String? = null,
    val imageRes: Int? = null,
    val country: String = "Saudi Arabia",
    val isTranslation: Boolean = false
)

val AVAILABLE_RECITERS = listOf(
    Reciter(
        id = "ar.sudais",
        name = "Abdur Rahman Al-Sudais",
        nameArabic = "عبد الرحمن السديس",
        serverUrl = "https://server11.mp3quran.net/sds/",
        subtext = "Imam of Masjid al-Haram",
        photoUrl = "https://static.qurancdn.com/images/reciters/6/abdur-rahman-as-sudais-profile.jpeg",
        imageRes = R.drawable.img_reciter_sudais,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.alafasy",
        name = "Mishary Rashid Alafasy",
        nameArabic = "مشاري راشد العفاسي",
        serverUrl = "https://server8.mp3quran.net/afs/",
        subtext = "Kuwaiti Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/7/mishari-rashid-al-afasy-profile.jpeg",
        imageRes = R.drawable.img_reciter_alafasy,
        country = "Kuwait"
    ),
    Reciter(
        id = "ar.saadghamadi",
        name = "Saad Al-Ghamdi",
        nameArabic = "سعد الغامدي",
        serverUrl = "https://server7.mp3quran.net/s_gmd/",
        subtext = "Saudi Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/5/saad-al-ghamdi-profile.jpeg",
        imageRes = R.drawable.img_reciter_ghamdi,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.maher",
        name = "Maher Al-Muaiqly",
        nameArabic = "ماهر المعيقلي",
        serverUrl = "https://server12.mp3quran.net/maher/",
        subtext = "Imam of Masjid al-Haram",
        photoUrl = "https://static.qurancdn.com/images/reciters/4/maher-al-muaiqly-profile.jpeg",
        imageRes = R.drawable.img_reciter_sudais,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.abdulbasitmurattal",
        name = "Abdul Basit Abdul Samad",
        nameArabic = "عبد الباسط عبد الصمد",
        serverUrl = "https://server7.mp3quran.net/basit/",
        subtext = "Egyptian Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/1/abdul-baset-profile.jpeg",
        imageRes = R.drawable.img_reciter_abdulbasit,
        country = "Egypt"
    ),
    Reciter(
        id = "ar.alajamy",
        name = "Ahmed Ibn Ali Al-Ajmy",
        nameArabic = "أحمد بن علي العجمي",
        serverUrl = "https://server10.mp3quran.net/ajm/",
        subtext = "Saudi Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/3/ahmed-al-ajamy-profile.jpeg",
        imageRes = R.drawable.img_reciter_ghamdi,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.shuraim",
        name = "Saud Al-Shuraim",
        nameArabic = "سعود الشريم",
        serverUrl = "https://server7.mp3quran.net/shur/",
        subtext = "Former Imam of Masjid al-Haram",
        photoUrl = "https://static.qurancdn.com/images/reciters/2/saud-ash-shuraym-profile.jpeg",
        imageRes = R.drawable.img_reciter_sudais,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.husary",
        name = "Mahmoud Khalil Al-Husary",
        nameArabic = "محمود خلیل الحصری",
        serverUrl = "https://server13.mp3quran.net/husr/",
        subtext = "Master of Tajweed",
        photoUrl = "https://static.qurancdn.com/images/reciters/8/mahmoud-khalil-al-hussary-profile.jpeg",
        imageRes = R.drawable.img_reciter_abdulbasit,
        country = "Egypt"
    ),
    Reciter(
        id = "ar.shaatree",
        name = "Abu Bakr Al-Shatri",
        nameArabic = "أبو بكر الشاطري",
        serverUrl = "https://server11.mp3quran.net/shatri/",
        subtext = "Yemeni Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/9/abu-bakr-al-shatri-profile.jpeg",
        imageRes = R.drawable.img_reciter_alafasy,
        country = "Yemen"
    ),
    Reciter(
        id = "ar.dosari",
        name = "Yasser Al-Dosari",
        nameArabic = "ياسر الدوسري",
        serverUrl = "https://server11.mp3quran.net/yasser/",
        subtext = "Imam of Masjid al-Haram",
        photoUrl = "https://static.qurancdn.com/images/reciters/10/yasser-ad-dussary-profile.jpeg",
        imageRes = R.drawable.img_reciter_sudais,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.oosi",
        name = "Abdul Rahman Al-Oosi",
        nameArabic = "عبد الرحمن العوسی",
        serverUrl = "https://server6.mp3quran.net/aloosi/",
        subtext = "Saudi Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/11/abdul-rahman-al-oosi-profile.jpeg",
        imageRes = R.drawable.img_reciter_alafasy,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ar.fares",
        name = "Fares Abbad",
        nameArabic = "فارس عباد",
        serverUrl = "https://server8.mp3quran.net/frs_a/",
        subtext = "Yemeni Reciter",
        photoUrl = "https://static.qurancdn.com/images/reciters/12/fares-abbad-profile.jpeg",
        imageRes = R.drawable.img_reciter_ghamdi,
        country = "Yemen"
    ),
    Reciter(
        id = "ar.baleela",
        name = "Bandar Baleela",
        nameArabic = "بندر بليلة",
        serverUrl = "https://server6.mp3quran.net/balilah/",
        subtext = "Imam of Masjid al-Haram",
        photoUrl = "https://static.qurancdn.com/images/reciters/14/bandar-baleela-profile.jpeg",
        imageRes = R.drawable.img_reciter_sudais,
        country = "Saudi Arabia"
    ),
    Reciter(
        id = "ur.sadaqat",
        name = "Qari Syed Sadaqat Ali",
        nameArabic = "قاری سید صداقت علی",
        serverUrl = "https://download.quranicaudio.com/quran/sadaqat_ali/",
        subtext = "Famous Pakistani Reciter",
        photoUrl = "https://archive.org/services/img/Al_Quran_Qari_Syed_Sadaqat_Ali",
        imageRes = R.drawable.img_reciter_qari,
        country = "Pakistan",
        isTranslation = false
    )
)

val TRANSLATION_RECITERS = listOf(
    Reciter(
        id = "ur.waheed",
        name = "Qari Waheed Zafar Qasmi",
        nameArabic = "قاری وحید ظفر قاسمی",
        serverUrl = "https://archive.org/download/Al_Quran-Urdu_Translation_Qari_Waheed_Zafar_Qasmi-High_Quality_201410/",
        subtext = "تلاوت مع اردو ترجمہ",
        photoUrl = "https://archive.org/services/img/Al_Quran-Urdu_Translation_Qari_Waheed_Zafar_Qasmi-High_Quality_201410",
        imageRes = R.drawable.img_reciter_qari,
        country = "Pakistan",
        isTranslation = true
    ),
    Reciter(
        id = "ur.khan",
        name = "Prof. Shamshad Ali Khan",
        nameArabic = "پروفیسر شمشاد علی خان (مع مشاری العفاسی)",
        serverUrl = "https://cdn.islamic.network/quran/audio/128/ur.khan/",
        subtext = "تلاوت مع اردو ترجمہ (عربی + اردو)",
        photoUrl = "https://archive.org/services/img/UrduTranslationProf.ShamshadAliKhanWithMisharyRashidAlafasy",
        imageRes = R.drawable.img_reciter_qari,
        country = "Pakistan",
        isTranslation = true
    )
)

val ALL_RECITERS = AVAILABLE_RECITERS + TRANSLATION_RECITERS
