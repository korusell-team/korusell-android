package net.alienminds.ethnogram

import android.app.Application
import android.content.Context
import net.alienminds.ethnogram.service.serviceModule
import net.alienminds.ethnogram.utils.AppContextWrapper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EthnogramApp: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EthnogramApp)
            modules(appModule, serviceModule)
        }
    }

//    override fun attachBaseContext(base: Context?) {
//        super.attachBaseContext(AppContextWrapper.wrap(base))
//    }

}