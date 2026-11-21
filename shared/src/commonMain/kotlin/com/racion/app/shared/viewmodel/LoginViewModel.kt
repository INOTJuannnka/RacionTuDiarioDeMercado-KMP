package com.racion.app.shared.viewmodel

import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.AuthSession
import com.racion.app.shared.network.FirebaseAuthService
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isRegister: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

/**
 * Login (port de `LoginViewModel.swift`): correo + contraseña con Firebase, Google y
 * modo invitado. Al entrar guarda la sesión en [AppStore]; la raíz cambia sola.
 */
class LoginViewModel(
    private val store: AppStore,
    private val auth: FirebaseAuthService
) : RacionViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmChange(value: String) = _state.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun toggleMode() = _state.update {
        it.copy(isRegister = !it.isRegister, confirmPassword = "", errorMessage = null, infoMessage = null)
    }

    fun submit() {
        val current = _state.value
        validate(current)?.let { message ->
            _state.update { it.copy(errorMessage = message) }
            return
        }
        run {
            if (current.isRegister) auth.signUp(current.email, current.password)
            else auth.signIn(current.email, current.password)
        }
    }

    fun signInWithGoogle(idToken: String) = run { auth.signInWithGoogle(idToken) }

    fun continueAsGuest() = store.setSession(AuthSession.ANONYMOUS)

    /** Error del lado de la plataforma (Credential Manager cancelado, sin cuenta, etc.). */
    fun showError(message: String) = _state.update { it.copy(errorMessage = message, isLoading = false) }

    fun sendPasswordReset() {
        val email = _state.value.email.trim()
        if (email.isEmpty() || '@' !in email) {
            _state.update { it.copy(errorMessage = "Escribí tu correo para recuperar la contraseña") }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        scope.launch {
            val result = auth.sendPasswordReset(email)
            _state.update {
                when (result) {
                    is AppResult.Success -> it.copy(
                        isLoading = false,
                        infoMessage = "Te enviamos un correo para restablecer la contraseña."
                    )
                    is AppResult.Failure -> it.copy(isLoading = false, errorMessage = result.error.userMessage)
                }
            }
        }
    }

    private fun run(request: suspend () -> AppResult<AuthSession>) {
        _state.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        scope.launch {
            when (val result = request()) {
                is AppResult.Success -> {
                    _state.update { LoginUiState() }
                    store.setSession(result.value)
                }
                // isLoading se limpia también en el fallo para poder reintentar.
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage)
                }
            }
        }
    }

    companion object {
        /** `null` si el formulario es válido; si no, el mensaje para la persona. */
        fun validate(state: LoginUiState): String? {
            val email = state.email.trim()
            return when {
                email.isEmpty() -> "Ingresá tu correo"
                '@' !in email -> "Ingresá un correo electrónico válido"
                state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                state.isRegister && state.confirmPassword.isEmpty() -> "Confirmá la contraseña"
                state.isRegister && state.confirmPassword != state.password -> "Las contraseñas no coinciden"
                else -> null
            }
        }
    }
}
