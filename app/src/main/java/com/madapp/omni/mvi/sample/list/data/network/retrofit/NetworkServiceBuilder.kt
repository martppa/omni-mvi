package com.madapp.omni.mvi.sample.list.data.network.retrofit

import com.madapp.omni.mvi.sample.BuildConfig
import com.madapp.omni.mvi.sample.list.data.network.Routes
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkServiceBuilder {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Routes.Host)
        .addConverterFactory(buildJsonParser())
        .client(createHttpClient())
        .build()

    private fun createHttpClient(): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            okHttpClientBuilder.addInterceptor(buildLoggingInterceptor())
        }
        return okHttpClientBuilder.build()
    }

    private fun buildJsonParser(): MoshiConverterFactory {
        return MoshiConverterFactory.create()
    }

    private fun buildLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

    fun <T> create(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }
}