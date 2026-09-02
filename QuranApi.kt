package com.example.data.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class SurahListResponse(
    val code: Int = 200,
    val status: String = "",
    val data: List<Surah> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Surah(
    val number: Int = 1,
    val name: String = "",
    val englishName: String = "",
    val englishNameTranslation: String = "",
    val numberOfAyahs: Int = 0,
    val revelationType: String = ""
)

@JsonClass(generateAdapter = true)
data class SurahEditionsResponse(
    val code: Int = 200,
    val status: String = "",
    val data: List<SurahEdition> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SurahEdition(
    val number: Int = 1,
    val name: String = "",
    val englishName: String = "",
    val englishNameTranslation: String = "",
    val revelationType: String = "",
    val numberOfAyahs: Int = 0,
    val ayahs: List<Ayah> = emptyList(),
    val edition: Edition? = null
)

@JsonClass(generateAdapter = true)
data class Edition(
    val identifier: String = "",
    val language: String? = null,
    val name: String? = null,
    val englishName: String? = null,
    val format: String? = null,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class AyahEditionsResponse(
    val code: Int = 200,
    val status: String = "",
    val data: List<AyahEdition> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AyahEdition(
    val number: Int = 1,
    val text: String = "",
    val numberInSurah: Int = 1,
    val juz: Int? = null,
    val manzil: Int? = null,
    val page: Int? = null,
    val ruku: Int? = null,
    val hizbQuarter: Int? = null,
    val audio: String? = null,
    val edition: Edition? = null,
    val surah: Surah? = null
)

@JsonClass(generateAdapter = true)
data class Ayah(
    val number: Int = 1,
    val text: String = "",
    val numberInSurah: Int = 1,
    val juz: Int? = null,
    val manzil: Int? = null,
    val page: Int? = null,
    val ruku: Int? = null,
    val hizbQuarter: Int? = null,
    val audio: String? = null,
    val surah: Surah? = null
)

@JsonClass(generateAdapter = true)
data class PageResponse(
    val code: Int = 200,
    val status: String = "",
    val data: PageData? = null
)

@JsonClass(generateAdapter = true)
data class PageData(
    val number: Int = 1,
    val ayahs: List<Ayah> = emptyList(),
    val surahs: Map<String, Surah>? = null
)

interface QuranApiService {
    @GET("v1/surah")
    suspend fun getSurahs(): SurahListResponse

    // Get multiple editions (e.g., quran-uthmani, ur.jalandhry, ar.alafasy, and ur.khan)
    @GET("v1/surah/{surahNumber}/editions/quran-uthmani,ur.jalandhry,ar.alafasy,ur.khan")
    suspend fun getSurahWithTranslation(@Path("surahNumber") surahNumber: Int): SurahEditionsResponse

    @GET("v1/ayah/{ayahNumber}/editions/quran-uthmani,ur.jalandhry,ar.alafasy,ur.khan")
    suspend fun getAyahWithTranslation(@Path("ayahNumber") ayahNumber: Int): AyahEditionsResponse
    @GET("v1/page/{pageNumber}/quran-uthmani")
    suspend fun getPage(@Path("pageNumber") pageNumber: Int): PageResponse
}
object QuranApi {
    private const val BASE_URL = "https://api.alquran.cloud/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: QuranApiService by lazy {
        retrofit.create(QuranApiService::class.java)
    }
}
