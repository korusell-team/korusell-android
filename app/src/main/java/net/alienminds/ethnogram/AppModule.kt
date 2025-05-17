package net.alienminds.ethnogram

import net.alienminds.ethnogram.utils.UserStateProvider
import org.koin.dsl.module

val appModule = module {

    single { UserStateProvider(get(), get(), get()) }

}