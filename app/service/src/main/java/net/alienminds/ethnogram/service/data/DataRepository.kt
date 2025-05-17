package net.alienminds.ethnogram.service.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City

class DataRepository internal constructor(
    firestore: FirebaseFirestore = Firebase.firestore
): BaseRepository() {

    private val categoriesCollection = firestore.collection("cats")
    private val citiesCollection = firestore.collection("cities")

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