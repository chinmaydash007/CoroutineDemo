package com.chinmay.coroutinedemo.Imgur

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {

    private const val IMGUR_BASE_URL = "https://api.imgur.com/3/"

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IMGUR_BASE_URL)
            .client(getInteceptor())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getInteceptor(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    val imgurApiService: ImgurApiService = getRetrofit().create(ImgurApiService::class.java)
}