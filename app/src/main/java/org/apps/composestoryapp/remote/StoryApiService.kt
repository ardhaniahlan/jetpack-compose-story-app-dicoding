package org.apps.composestoryapp.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.apps.composestoryapp.model.AddStoryResponse
import org.apps.composestoryapp.model.LoginRequest
import org.apps.composestoryapp.model.LoginResponse
import org.apps.composestoryapp.model.RegisterRequest
import org.apps.composestoryapp.model.RegisterResponse
import org.apps.composestoryapp.model.StoryDetailResponse
import org.apps.composestoryapp.model.StoryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface StoryApiService {
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
    ): AddStoryResponse

    @GET("stories")
    suspend fun getAllStories(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoryResponse

    @GET("stories/{id}")
    suspend fun getStoryDetail(
        @Path("id") id: String
    ): StoryDetailResponse
}