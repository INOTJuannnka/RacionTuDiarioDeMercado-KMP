package com.racion.app.shared.auth

import com.racion.app.shared.models.AppError
import com.racion.app.shared.models.AuthProvider
import com.racion.app.shared.models.AuthSession
import com.racion.app.shared.network.mapFirebaseError
import com.racion.app.shared.persistence.RacionStorage
import com.racion.app.shared.state.AppStore
import com.racion.app.shared.viewmodel.LoginUiState
import com.racion.app.shared.viewmodel.LoginViewModel
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthTest {

    @Test
    fun sessionSurvivesRestartAndSignOutClearsIt() {
        val settings = MapSettings()
        val session = AuthSession(AuthProvider.GOOGLE, "uid-1", "ana@example.com", "Ana")

        AppStore(RacionStorage(settings)).setSession(session)
        val restarted = AppStore(RacionStorage(settings))
        assertEquals(session, restarted.state.value.session)

        restarted.signOut()
        assertNull(AppStore(RacionStorage(settings)).state.value.session)
    }

    @Test
    fun validationRules() {
        val ok = LoginUiState(email = "a@b.co", password = "123456")
        assertNull(LoginViewModel.validate(ok))
        assertEquals("Ingresá tu correo", LoginViewModel.validate(ok.copy(email = "  ")))
        assertEquals("Ingresá un correo electrónico válido", LoginViewModel.validate(ok.copy(email = "ab.co")))
        assertEquals("La contraseña debe tener al menos 6 caracteres", LoginViewModel.validate(ok.copy(password = "123")))
        assertEquals("Las contraseñas no coinciden", LoginViewModel.validate(ok.copy(isRegister = true, confirmPassword = "x")))
        assertNull(LoginViewModel.validate(ok.copy(isRegister = true, confirmPassword = "123456")))
    }

    @Test
    fun firebaseErrorsAreTranslated() {
        assertEquals(
            AppError.Unauthorized("Correo o contraseña incorrectos."),
            mapFirebaseError("INVALID_LOGIN_CREDENTIALS")
        )
        assertEquals(
            AppError.Unauthorized("La contraseña es demasiado débil (mínimo 6 caracteres)."),
            mapFirebaseError("WEAK_PASSWORD : Password should be at least 6 characters")
        )
        assertEquals(AppError.RateLimited, mapFirebaseError("TOO_MANY_ATTEMPTS_TRY_LATER"))
    }
}
