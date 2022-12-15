package com.haystackevents.app.`in`.network

import okhttp3.Interceptor
import okhttp3.Response

class BearerAuthenticationInterceptor(private val canCache: Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //val authKey = SessionManager.instance.getAuthKey()
        //val clientService = SessionManager.instance.getClientService()
        //Log.e("Token: ",authKey)
        val original = chain.request()
        val requestBuilder = original.newBuilder()
                .header("Client-Service", "clientService")
                .header("Auth-Key", "authKey")
                .header("Content-Type", "application/json")
                .addHeader("Cache-control", if (canCache) "max-stale=86400" else "no-cache")
                .method(original.method(), original.body())
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}