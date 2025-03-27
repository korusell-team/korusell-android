package net.alienminds.ethnogram

import android.app.Application
import android.content.Context
import net.alienminds.ethnogram.utils.AppContextWrapper

class EthnogramApp: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(AppContextWrapper.wrap(base))
    }

}