package net.alienminds.ethnogram.service

import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.feedback.FeedbackRepository
import net.alienminds.ethnogram.service.prefs.PrefsRepository
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.utils.FirestoreProvider
import org.koin.dsl.module

val serviceModule = module{

    //Coroutine
    single<CoroutineScope> { CoroutineScope(Dispatchers.IO) }

    //Firebase
    single { FirestoreProvider() }
    single { Firebase.storage }

    //Repositories
    single { PrefsRepository(
        context = get()
    ) }

    single { AuthRepository(
        firestoreProvider = get(),
        prefsRepository = get(),
        context = get()
    ) }

    single { DataRepository(
        firestoreProvider = get(),
        authRepository = get(),
        ioScope = get()
    ) }

    single { FeedRepository(
        userRepo = get(),
        authRepo = get(),
        ioScope = get(),
        firestoreProvider = get()
    ) }

    single { UserRepository(
        authRepository = get(),
        ioScope = get(),
        storage = get(),
        firestoreProvider = get()
    ) }

    single { FeedbackRepository(
        firestoreProvider = get(),
        userRepository = get()
    ) }


}