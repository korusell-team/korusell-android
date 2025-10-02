package net.alienminds.ethnogram.service.data

import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.utils.FirestoreProvider

class DataRepository internal constructor(
    private val firestoreProvider: FirestoreProvider
): BaseRepository() {

    private val categoriesCollection
        get() = firestoreProvider.get().collection("cats")

    private val citiesCollection
        get() = firestoreProvider.get().collection("cities")

    private var isCategoriesSync = false
    private var isCitiesSync = false

    fun getCategoriesFlow(): Flow<List<Category>> = flow{
        runCatching {
            categoriesCollection
                .get(Source.CACHE)
                .await()
                .documents
                .mapNotNull { Category(it) }
        }.onSuccess { emit(it) }
            .onFailure{ emit(emptyList()) }

        if (isCategoriesSync.not()) {
            runCatching {
                categoriesCollection
                    .get()
                    .await()
                    .documents
                    .mapNotNull { Category(it) }
            }.onSuccess {
                isCategoriesSync = true
                emit(it)
            }.onFailure{ emit(emptyList()) }
        }
    }

    fun getCitiesFlow(): Flow<List<City>> = flow{
        runCatching {
            citiesCollection
                .get(Source.CACHE)
                .await()
                .documents
                .mapNotNull { City(it) }
        }.onSuccess { emit(it) }.onFailure{ emit(emptyList()) }

        if (isCitiesSync.not()) {
            runCatching {
                citiesCollection
                    .get()
                    .await()
                    .documents
                    .mapNotNull { City(it) }
            }.onSuccess {
                isCitiesSync = true
                emit(it)
            }.onFailure{ emit(emptyList()) }
        }
    }

}