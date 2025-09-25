package net.alienminds.ethnogram.utils

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.updatePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InAppUpdateManager(
    private val activity: Activity
){

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)

    var updateState by mutableStateOf<UpdateAppUiState>(UpdateAppUiState.NotInitialize)
        private set

    suspend fun checkUpdate(){
        runCatching{
            withContext(Dispatchers.IO) {
                val updateInfo = appUpdateManager.appUpdateInfo.await()
                if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    launchUpdate(updateInfo)
                }
            }
        }.onFailure {
            updateState = UpdateAppUiState.NotInitialize
            it.printStackTrace()
        }
    }


    private suspend fun launchUpdate(updateInfo: AppUpdateInfo){
        val flexibleAllowed = updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        val immediateAllowed = updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

        when (updateInfo.updatePriority) {
            in 0..1 -> {
                if ((updateInfo.clientVersionStalenessDays?: 0) >= 3 && flexibleAllowed){
                    flexibleUpdate(updateInfo)
                }
            }
            in 2..3 -> if (flexibleAllowed) flexibleUpdate(updateInfo)
            in 4..5 -> when {
                immediateAllowed -> immediateUpdate(updateInfo)
                flexibleAllowed -> flexibleUpdate(updateInfo)
            }
        }
    }

    private suspend fun immediateUpdate(updateInfo: AppUpdateInfo) = appUpdateManager.startUpdateFlow(
        updateInfo,
        activity,
        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
    ).await()

    private suspend fun flexibleUpdate(updateInfo: AppUpdateInfo){
        val listener = InstallStateUpdatedListener { state ->
            val status = state.installStatus()
            when(status){
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    updateState = UpdateAppUiState.Downloading(
                        percentage = bytesDownloaded.toFloat()/totalBytesToDownload
                    )
                }
                InstallStatus.DOWNLOADED -> {
                    updateState = UpdateAppUiState.Ready
                }
                else -> {
                    updateState = UpdateAppUiState.NotInitialize
                }
            }

        }
        appUpdateManager.registerListener(listener)
        appUpdateManager.startUpdateFlow(
            updateInfo,
            activity,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        ).await()
        appUpdateManager.unregisterListener(listener)
    }

    suspend fun completeFlexibleUpdate(){
        if (updateState !is UpdateAppUiState.Ready) return
        runCatching {
            withContext(Dispatchers.IO) {
                appUpdateManager.completeUpdate().await()
            }
        }
    }


    sealed class UpdateAppUiState{

        object NotInitialize: UpdateAppUiState()

        data class Downloading(
            val percentage: Float//0f..1f
        ): UpdateAppUiState()

        object Ready: UpdateAppUiState()

    }


}