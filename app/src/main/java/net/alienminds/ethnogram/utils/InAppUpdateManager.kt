package net.alienminds.ethnogram.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.updatePriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class InAppUpdateManager internal constructor(
    private val appContext: Context
){

    companion object{
        const val LOG_TAG = "InAppUpdateManager"
    }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(appContext) }
    private val stateUpdater = StateUpdater()

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.NotStarted)
    val status = _status.asStateFlow()

    suspend fun getUpdate() = runCatching{
        AppUpdate(appUpdateManager.appUpdateInfo.await())
    }.onSuccess {
        Log.i(LOG_TAG, "App Update Available ${it.versionCode}")
    }.onFailure {
        Log.e(LOG_TAG, "Failed get update app", it)
    }


    suspend fun startUpdate(appUpdate: AppUpdate, activity: Activity){
        runCatching {
            _status.tryEmit(UpdateStatus.Progress)
            val priority = appUpdate.appUpdateInfo.updatePriority
            val updateType = when (priority <= 3) {
                true -> AppUpdateType.FLEXIBLE
                false -> AppUpdateType.IMMEDIATE
            }
            if (updateType == AppUpdateType.FLEXIBLE) {
                appUpdateManager.registerListener(stateUpdater)
            }

            if (appUpdate.appUpdateInfo.isUpdateTypeAllowed(updateType)) {
                val options = AppUpdateOptions.newBuilder(updateType).build()
                val resultCode = appUpdateManager.startUpdateFlow(appUpdate.appUpdateInfo, activity, options).await()
                if (resultCode != RESULT_OK){
                    error("Failed update app, code $resultCode")
                }
            } else {
                error("Update is not allowed, type: $updateType, priority: $priority")
            }
        }.onFailure {
            _status.tryEmit(UpdateStatus.NotStarted)
            Log.e(LOG_TAG, "Failed update app", it)
        }
    }

    suspend fun confirmInstall(){
        runCatching {
            if (_status.value is UpdateStatus.ReadyToInstall) {
                appUpdateManager.completeUpdate().await()
                appUpdateManager.unregisterListener(stateUpdater)
            }
        }.onFailure {
            Log.e(LOG_TAG, "Failed install app", it)
        }
    }

    inner class StateUpdater: InstallStateUpdatedListener {
        override fun onStateUpdate(state: InstallState) {
            when (state.installStatus()) {
                InstallStatus.PENDING,
                InstallStatus.INSTALLING -> UpdateStatus.Progress

                InstallStatus.DOWNLOADING -> UpdateStatus.ProgressFlexible(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytes = state.totalBytesToDownload()
                )

                InstallStatus.DOWNLOADED -> UpdateStatus.ReadyToInstall
                InstallStatus.INSTALLED,
                InstallStatus.FAILED,
                InstallStatus.CANCELED,
                InstallStatus.UNKNOWN -> {
                    appUpdateManager.unregisterListener(this)
                    UpdateStatus.NotStarted
                }

                else -> UpdateStatus.NotStarted
            }.let { updateStatus ->
                _status.tryEmit(updateStatus)
            }

        }
    }

}

sealed class UpdateStatus{
    data object NotStarted: UpdateStatus()
    data object Progress: UpdateStatus()
    data object ReadyToInstall: UpdateStatus()
    data class ProgressFlexible(
        val bytesDownloaded: Long,
        val totalBytes: Long
    ): UpdateStatus()
}

class AppUpdate internal constructor(
    internal val appUpdateInfo: AppUpdateInfo
){

    val isAvailable
        get() = when(appUpdateInfo.updateAvailability()) {
            UpdateAvailability.UPDATE_AVAILABLE,
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> true
            else -> false
        }

    val versionCode
        get() = appUpdateInfo.availableVersionCode()

}