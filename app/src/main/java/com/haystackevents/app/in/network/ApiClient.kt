package com.haystackevents.app.`in`.network

import com.haystackevents.app.`in`.network.config.AppConfig.BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {




    private var retrofit: Retrofit? = null


    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    val retrofitService: ApiInterface by lazy { getClient()
        .create(ApiInterface::class.java) }

    private fun getClient(): Retrofit{

        val okHttp = OkHttpClient.Builder().addInterceptor(logger)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (retrofit == null){

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttp.build())
                .build()
        }

        return retrofit!!
    }

    fun <S> createBearerAuthService(service: Class<S>, canCache: Boolean): S {
        val token = ""//SessionManager.instance.getToken()
        //Log.e("TAG", "token: $token")

        val bearerAuthInterceptor =
            BearerAuthenticationInterceptor(
                canCache
            )
        val retrofit =
            getRetrofit(bearerAuthInterceptor)
        return retrofit.create(service)
    }

    private fun getRetrofit(interceptor: Interceptor?): Retrofit {

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logger)

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttp.build())
            .build()
    }
}