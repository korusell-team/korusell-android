package net.alienminds.ethnogram.data.firestore

import net.alienminds.ethnogram.data.firestore.utils.FirestoreProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val DataFirestoreModule = module{
    singleOf(::FirestoreProvider)
}