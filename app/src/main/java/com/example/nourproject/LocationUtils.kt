import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.util.*

class LocationUtils(private val context: Context) {

    fun getCityFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses.isNotEmpty()) {
                val city = addresses[0].locality
                return city ?: "City not found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "City not found"
    }
}
