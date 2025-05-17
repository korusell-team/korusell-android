package net.alienminds.ethnogram.ui.screens.auth

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.ui.extentions.transitions.SlidePageTransition
import net.alienminds.ethnogram.ui.screens.auth.phone.AuthPhoneScreen

//TODO("Вынести в отдельный модуль")
class AuthScreen: Screen {

    @Composable
    override fun Content() {
        Navigator(AuthPhoneScreen()){ navigator ->
            SlidePageTransition(navigator)
        }
    }

}