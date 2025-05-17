package net.alienminds.ethnogram.service

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.prefs.PrefsRepository
import net.alienminds.ethnogram.service.user.UserRepository
import org.koin.dsl.module

val serviceModule = module {

    //Coroutine
    single<CoroutineScope> { CoroutineScope(Dispatchers.IO) }

    //Firebase
    single { Firebase.firestore }
    single { Firebase.storage }

    //Repositories
    single { PrefsRepository(get()) }
    single { AuthRepository() }
    single { DataRepository(get()) }
    single { FeedRepository(get(), get(), get()) }
    single { UserRepository(get(), get(), get(), get()) }


}