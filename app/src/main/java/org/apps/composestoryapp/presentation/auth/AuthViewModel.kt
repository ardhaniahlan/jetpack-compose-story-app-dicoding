package org.apps.composestoryapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apps.composestoryapp.UiEvent
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.remote.SessionManager
import org.apps.composestoryapp.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
): ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun getProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = ViewState.Loading) }

            repository.getProfile()
                .onSuccess { loginResult ->
                    _uiState.update {
                        it.copy(authState = ViewState.Success(loginResult))
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(authState = ViewState.Error(error.message ?: "Failed to load profile"))
                    }
                }
        }
    }

    fun login(){
        viewModelScope.launch {
            _uiState.update { it.copy(authState = ViewState.Loading) }

            with(_uiState.value) {
                repository.login(email, password)
            }.onSuccess { login ->
                _uiState.update { it.copy(authState = ViewState.Success(login)) }
            }.onFailure { e ->
                _uiState.update { it.copy(authState = ViewState.Error(e.message ?: "Email dan Password tidak tepat")) }
            }
        }
    }

    fun register(){
        viewModelScope.launch {
            _uiState.update { it.copy(regisState = ViewState.Loading) }

            with(_uiState.value) {
                repository.register(name, email, password)
            }.onSuccess { regis ->
                if (regis.error) {
                    _uiState.update {
                        it.copy(regisState = ViewState.Error(regis.message))
                    }
                } else {
                    _uiState.update {
                        it.copy(regisState = ViewState.Success(regis))
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(regisState = ViewState.Error(e.message ?: "Register Gagal")) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _eventFlow.emit(UiEvent.Navigate)
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isNotEmpty() && name.length < 3) {
                    "Nama minimal 3 digit"
                } else null
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = if (email.isNotEmpty() && !isValidGmail(email)) {
                    "Email tidak valid"
                } else null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = if (password.isNotEmpty() && password.length < 8) {
                    "Password minimal 8 karakter"
                } else null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(
                passwordVisible = !it.passwordVisible
            )
        }
    }

    fun isValidGmail(input: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@gmail\\.com$").matches(input)
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                name = "",
                email = "",
                password = "",
            )
        }
    }
}