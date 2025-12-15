package org.apps.composestoryapp.repository

import android.util.Log
import androidx.datastore.core.IOException
import kotlinx.coroutines.flow.first
import org.apps.composestoryapp.model.LoginRequest
import org.apps.composestoryapp.model.LoginResult
import org.apps.composestoryapp.model.RegisterRequest
import org.apps.composestoryapp.model.RegisterResponse
import org.apps.composestoryapp.remote.SessionManager
import org.apps.composestoryapp.remote.StoryApiService
import retrofit2.HttpException

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResult>
    suspend fun register(name: String, email: String, password: String): Result<RegisterResponse>
    suspend fun getProfile(): Result<LoginResult>
}

class AuthRepositoryImpl(
    private val api: StoryApiService,
    private val sessionManager: SessionManager
): AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<LoginResult> {
        return try {
            val response = api.login(
                request = LoginRequest(email, password)
            )
            if (!response.error && response.loginResult != null){
                val session = LoginResult(
                    userId = response.loginResult.userId,
                    name = response.loginResult.name,
                    token = response.loginResult.token
                )
                sessionManager.saveSession(session)
                Result.success(session)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception){
            val errorMessage = when (e) {
                is HttpException -> {
                    when (e.code()) {
                        401 -> "Email atau password tidak valid"
                        500 -> "Server error, coba lagi nanti"
                        else -> "Terjadi kesalahan (${e.code()})"
                    }
                }
                is IOException -> "Tidak ada koneksi internet"
                else -> e.message ?: "Register Gagal"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): Result<RegisterResponse> {
        return try {
            val response = api.register(
                request = RegisterRequest(name, email, password)
            )
            if (!response.error){
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception){
            val errorMessage = when (e) {
                is HttpException -> {
                    when (e.code()) {
                        400 -> "Email sudah terdaftar, coba lagi"
                        500 -> "Server error, coba lagi nanti"
                        else -> "Terjadi kesalahan (${e.code()})"
                    }
                }
                is IOException -> "Tidak ada koneksi internet"
                else -> e.message ?: "Register Gagal"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun getProfile(): Result<LoginResult> = runCatching {
        val session = sessionManager.sessionFlow.first()
            ?: throw Exception("Session not found")
        session
    }

}
