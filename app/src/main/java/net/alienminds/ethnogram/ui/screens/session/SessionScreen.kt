package net.alienminds.ethnogram.ui.screens.session

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.ui.extentions.transitions.SlidePageTransition
import net.alienminds.ethnogram.ui.screens.session.constacts.ContactsScreen

class SessionScreen: Screen {

    @Composable
    override fun Content() {
        Navigator(ContactsScreen()){
            SlidePageTransition(it)
        }
    }

}