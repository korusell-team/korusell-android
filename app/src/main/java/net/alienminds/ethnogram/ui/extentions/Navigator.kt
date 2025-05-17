package net.alienminds.ethnogram.ui.extentions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.alienminds.ethnogram.utils.UserState
import net.alienminds.ethnogram.utils.getScreen

val ProvidableCompositionLocal<Navigator?>.root: Navigator?
    @Composable get() = current?.root

val ProvidableCompositionLocal<Navigator?>.rootOrThrow: Navigator
    @Composable get() = currentOrThrow.root

val Navigator.root: Navigator
    get(){
        var current = this
        while (current.level > 0){
            current = current.parent?: break
        }
        return current
    }

fun Navigator.navigateByUserState(
    userState: UserState
) = root.replaceAll(userState.getScreen())