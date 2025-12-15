package org.apps.composestoryapp.repository

import android.util.Log
import androidx.datastore.core.IOException
import kotlinx.coroutines.flow.first
import org.apps.composestoryapp.model.Story
import org.apps.composestoryapp.remote.SessionManager
import org.apps.composestoryapp.remote.StoryApiService
import retrofit2.HttpException

interface StoryRepository {
    suspend fun getAllStories(page: Int = 1, size: Int = 10,): Result<List<Story>>
    suspend fun getStoryDetail(id: String): Result<Story>
}

class StoryRepositoryImpl(
    private val api: StoryApiService
): StoryRepository{
    override suspend fun getAllStories(
        page: Int,
        size: Int,
    ): Result<List<Story>> {
        return try {
            val response = api.getAllStories(
                page = page,
                size = size
            )

            if(!response.error){
                Result.success(response.listStory)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is HttpException -> {
                    when (e.code()) {
                        400 -> "Bad Request - Cek parameter"
                        401 -> "Unauthorized - Token expired"
                        404 -> "Stories not found"
                        500 -> "Server error"
                        else -> "Error (${e.code()})"
                    }
                }

                is IOException -> "No internet connection"
                else -> e.message ?: "Failed to fetch stories"
            }
            Result.failure(Exception(errorMessage))
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
}