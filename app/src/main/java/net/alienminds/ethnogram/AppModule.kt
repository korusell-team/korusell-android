package net.alienminds.ethnogram

import android.app.Activity
import net.alienminds.ethnogram.data.repository.utils.ActivityProvider
import net.alienminds.ethnogram.utils.InAppUpdateManager
import net.alienminds.ethnogram.utils.UserStateProvider
import org.koin.dsl.module

val appModule = module {

    single { UserStateProvider(get(), get(), get()) }
    single { InAppUpdateManager(get()) }

    factory<ActivityProvider> {
        // В Koin можно прокинуть текущую Activity через scope
        ActivityProvider {
            val activity = getKoin().getOrNull<Activity>()
            activity ?: throw IllegalStateException("Activity not available")
        }
    }

}