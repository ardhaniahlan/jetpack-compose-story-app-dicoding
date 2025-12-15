package org.apps.composestoryapp.presentation.auth

import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.LoginResult
import org.apps.composestoryapp.model.RegisterResponse

data class AuthState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    var passwordVisible: Boolean = false,

    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,

    val authState: ViewState<LoginResult> = ViewState.Idle,
    val regisState: ViewState<RegisterResponse> = ViewState.Idle
)
