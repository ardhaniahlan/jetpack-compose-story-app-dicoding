package org.apps.composestoryapp.remote

import org.apps.composestoryapp.model.LoginRequest
import org.apps.composestoryapp.model.LoginResponse
import org.apps.composestoryapp.model.RegisterRequest
import org.apps.composestoryapp.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface StoryApiService {
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}