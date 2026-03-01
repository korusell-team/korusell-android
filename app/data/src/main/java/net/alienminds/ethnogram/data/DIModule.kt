package net.alienminds.ethnogram.data

import net.alienminds.ethnogram.data.firestore.DataFirestoreModule
import net.alienminds.ethnogram.data.firestore.repository.FirestoreAuthRepository
import net.alienminds.ethnogram.data.firestore.repository.FirestoreMessageRepository
import net.alienminds.ethnogram.data.firestore.repository.FirestoreUserRepository
import net.alienminds.ethnogram.data.repository.AuthRepository
import net.alienminds.ethnogram.data.repository.MessageRepository
import net.alienminds.ethnogram.data.repository.UserRepository2
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val DataModule = module {

    singleOf(::FirestoreAuthRepository){ bind<AuthRepository>() }
    singleOf(::FirestoreUserRepository){ bind<UserRepository2>() }
    singleOf(::FirestoreMessageRepository){ bind<MessageRepository>() }

    includes(
        DataFirestoreModule,
        //Add Other
    )
}