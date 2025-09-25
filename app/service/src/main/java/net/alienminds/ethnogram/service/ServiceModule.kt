package net.alienminds.ethnogram.service

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.prefs.PrefsRepository
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.utils.FirestoreProvider
import org.koin.dsl.module

val serviceModule = module {

    //Coroutine
    single<CoroutineScope> { CoroutineScope(Dispatchers.IO) }

    //Firebase
    single { FirestoreProvider() }
    single { Firebase.storage }

    //Repositories
    single { PrefsRepository(get()) }
    single { AuthRepository(get(), get()) }
    single { DataRepository(get()) }
    single { FeedRepository(get(), get(), get(), get()) }
    single { UserRepository(get(), get(), get(), get()) }


}