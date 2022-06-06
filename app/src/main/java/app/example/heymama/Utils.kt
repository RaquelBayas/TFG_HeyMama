package app.example.heymama

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.coroutineContext

object Utils {

    fun showErrorToast(context: Context) {
        Toast.makeText(context,"Se ha producido un error.",Toast.LENGTH_SHORT).show()
    }
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getUID(): String {
        return FirebaseAuth.getInstance().uid.toString()
    }

    fun updateStatus(status: String) {
        val reference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Usuarios").child(getUID())
        val map = HashMap<String, Any>()
        map["status"] = status
        reference.updateChildren(map)
    }

    fun alertDialogInternet(context: Context) {
        val dialog = AlertDialog.Builder(context,R.style.AlertDialogTheme)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Conexión a Internet no disponible")
            .setMessage("Comprueba tu conexión a Internet")
            .setPositiveButton("Cerrar") { dialogInterface, i ->  }
        dialog.show()
    }
     fun isNetworkAvailable(context: Context): Boolean
     {
         val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
         if (connectivityManager != null) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                 val capabilities =
                     connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                 if (capabilities != null) {
                     when {
                         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                             return true
                         }
                         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                             return true
                         }
                         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                             return true
                         }
                     }
                 }
             }
         }
         return false
     }
}