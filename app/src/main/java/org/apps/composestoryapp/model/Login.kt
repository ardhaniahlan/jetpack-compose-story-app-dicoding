package org.apps.composestoryapp.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult? = null
)

data class LoginResult(
    val userId: String,
    val name: String,
    val token: String
)