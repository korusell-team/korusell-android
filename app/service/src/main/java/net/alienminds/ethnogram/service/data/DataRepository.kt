package net.alienminds.ethnogram.service.data

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

    private var cachedCategories: List<Category>? = null
    private var cachedCities: List<City>? = null

    suspend fun getCategories() = apiQuery{
        cachedCategories?.let {
            return@apiQuery it
        }
        categoriesCollection
            .get()
            .await()
            .documents
            .mapNotNull { Category(it) }
            .also { cachedCategories = it }
    }

    suspend fun getCities() = apiQuery {
        cachedCities?.let {
            return@apiQuery it
        }
        citiesCollection
            .get()
            .await()
            .documents
            .mapNotNull{ City(it) }
            .also { cachedCities = it }
    }

}