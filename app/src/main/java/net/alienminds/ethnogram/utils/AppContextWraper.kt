package net.alienminds.ethnogram.utils

import android.content.Context

class AppContextWrapper(
    context: Context?
): android.content.ContextWrapper(context) {

    companion object{
        fun wrap(context: Context?): AppContextWrapper {
            if(context == null) return AppContextWrapper(context)

            val newContext = setupScreenScale(context)

            return AppContextWrapper(newContext)
        }

        private fun setupScreenScale(
            context: Context
        ): Context? {
            val res = context.resources
            val configuration = res.configuration

            configuration.fontScale = 1f
            return context.createConfigurationContext(configuration)
        }
    }
}