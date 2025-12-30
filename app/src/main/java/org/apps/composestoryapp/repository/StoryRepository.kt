package org.apps.composestoryapp.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.datastore.core.IOException
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apps.composestoryapp.model.AddStoryResponse
import org.apps.composestoryapp.model.Story
import org.apps.composestoryapp.remote.StoryApiService
import retrofit2.HttpException
import java.io.File
import androidx.core.graphics.scale
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getAllStories(): Flow<PagingData<Story>>
    suspend fun addStory(
        description: String,
        photoUri: Uri,
        lat: Double?,
        lon: Double?
    ): Result<AddStoryResponse>
    suspend fun getStoryDetail(id: String): Result<Story>
    suspend fun getStoriesWithLocation(page: Int = 1, size: Int = 10,): Result<List<Story>>
    suspend fun getStoriesForNotification(): Result<List<Story>>
}

class StoryRepositoryImpl(
    private val api: StoryApiService,
    @ApplicationContext private val context: Context,
): StoryRepository{

    override fun getAllStories(): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource(api)
            }
        ).flow
    }

    override suspend fun getStoriesWithLocation(
        page: Int,
        size: Int,
    ): Result<List<Story>> = runCatching {
        api.getAllStories(
            page = page,
            size = size,
            location = 1
        ).listStory
    }

    override suspend fun getStoriesForNotification(): Result<List<Story>> {
        return try {
            val response = api.getAllStories(
                page = 1,
                size = 10,
                location = null
            )
            Result.success(response.listStory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStoryDetail(id: String): Result<Story> {
        return try{
            val response = api.getStoryDetail(
                id = id
            )
            Result.success(response.story)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun addStory(
        description: String,
        photoUri: Uri,
        lat: Double?,
        lon: Double?,
    ): Result<AddStoryResponse> {
        return try {
            val photoFile = uriToFile(photoUri)
            val descriptionBody = description.toRequestBody("text/plain".toMediaType())

            val requestFile = photoFile.asRequestBody("image/*".toMediaType())
            val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)

            val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())

            val response = api.addStory(
                description = descriptionBody,
                photo = photoPart,
                lat = latBody,
                lon = lonBody,
            )

            if (!response.error) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception){
            val errorMessage = when (e) {
                is HttpException -> {
                    when (e.code()) {
                        400 -> "Bad Request - Cek parameter"
                        401 -> "Unauthorized - Token expired"
                        413 -> "File terlalu besar (max 1MB)"
                        415 -> "File type tidak support"
                        500 -> "Server error"
                        else -> "Error (${e.code()})"
                    }
                }
                is IOException -> "No internet connection"
                else -> e.message ?: "Failed to add story"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    private fun uriToFile(uri: Uri): File {
        val resolver = context.contentResolver
        val maxSize = 1 * 1024 * 1024 // 1MB

        val original = BitmapFactory.decodeStream(
            resolver.openInputStream(uri)
        ) ?: error("Invalid image")

        val resized = resizeKeepRatio(original, 1800)

        var quality = 85
        var file: File

        do {
            file = File(context.cacheDir, "ktp_${System.currentTimeMillis()}.jpg")
            file.outputStream().use {
                resized.compress(Bitmap.CompressFormat.JPEG, quality, it)
            }
            quality -= 5
        } while (file.length() > maxSize && quality >= 60)

        return file
    }

    private fun resizeKeepRatio(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) return bitmap

        val ratio = bitmap.height.toFloat() / bitmap.width
        val height = (maxWidth * ratio).toInt()

        return bitmap.scale(maxWidth, height)
    }
}