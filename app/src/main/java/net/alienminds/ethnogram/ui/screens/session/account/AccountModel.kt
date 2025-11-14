package net.alienminds.ethnogram.ui.screens.session.account

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.ui.extentions.root
import net.alienminds.ethnogram.ui.screens.auth.AuthScreen
import net.alienminds.ethnogram.utils.AppScreenModel
import net.alienminds.ethnogram.utils.InAppUpdateManager
import net.alienminds.ethnogram.utils.UpdateStatus
import net.alienminds.ethnogram.utils.findActivity
import org.koin.core.component.inject

class AccountModel: AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val updateManager by inject<InAppUpdateManager>()

    val user by userRepo.meFlow.asStateWithLoading(null)
    val isAnonymous
        get() = authRepo.isAnonymous

    fun updateApp(ctx: Context) = launchWithLoading {
        updateManager.getUpdate()
            .onFailure{
                Toast.makeText(ctx, "Не удалось проверить наличие обновлений", Toast.LENGTH_LONG).show()
                Log.e("AccountModel", "Failed check app update", it)
            }
            .onSuccess {
                if (it.isAvailable){
                    when(updateManager.status.value){
                        UpdateStatus.NotStarted -> {
                            val activity = ctx.findActivity()?: return@launchWithLoading
                            updateManager.startImmediateUpdate(it, activity)
                        }
                        is UpdateStatus.ProgressFlexible, UpdateStatus.Progress -> {
                            Toast.makeText(ctx, "Обновление скачаивается, пожалуста дождитесь окончания загрузки", Toast.LENGTH_LONG).show()
                        }
                        UpdateStatus.ReadyToInstall -> {
                            updateManager.confirmInstall()
                        }
                    }
                } else{
                    Toast.makeText(ctx, "Установлена последняя версия приложения", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun logout(navigator: Navigator?) = launchWithLoading{
        authRepo.logout()
        navigator?.root?.replaceAll(AuthScreen())
    }

    fun deleteAccount(navigator: Navigator?) = launchWithLoading{
        userRepo.deleteMyAccount().onSuccess {
            authRepo.logout()
            navigator?.root?.replaceAll(AuthScreen())
        }
    }

}