package net.alienminds.ethnogram.ui.screens.session.contacts.delete

import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.ui.extentions.root
import net.alienminds.ethnogram.ui.screens.auth.AuthScreen
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject
import kotlin.getValue

class DeleteAccountModel: AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()

    fun deleteAccount(navigator: Navigator?) = launchWithLoading{
        userRepo.deleteMyAccount().onSuccess {
            authRepo.logout()
            navigator?.root?.replaceAll(AuthScreen())
        }
    }

}