package net.alienminds.ethnogram.ui.screens.session.map.entities

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import net.alienminds.ethnogram.service.user.entities.User
import kotlin.random.Random

class UserClusterItem(
    val user: User,
    private val location: LatLng,
): ClusterItem{

    override fun getPosition(): LatLng {
        return location
    }

    override fun getTitle(): String {
        return user.fullName
    }

    override fun getSnippet(): String? {
        return user.info
    }

    override fun getZIndex(): Float? {
        return null
    }

    companion object{
        fun fromUserOrNull(user: User): UserClusterItem?{
            return UserClusterItem(
                user = user,
                location = LatLng(
                    user.latitude?: return null,
                    user.longitude?: return null
                )
            )
        }
    }

}

val Cluster<UserClusterItem>.id
    get() = this.items.sortedBy { it.user.uid }.joinToString(separator = "_") { it.user.uid.orEmpty() }