import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    val PRIVATE_MODE = 0

    private val PREF_NAME = "SharedPreferences"
    private val IS_LOGIN = "is_login"

    val preferences: SharedPreferences? = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE)
    val editor: SharedPreferences.Editor? = preferences?.edit()

    fun createLoginSession(email: String, password: String, rol: String) {
        editor?.putBoolean(IS_LOGIN,true)
        editor?.putString("email",email)
        editor?.putString("password",password)
        editor?.putString("rol",rol)
        editor?.commit()
    }

    fun isLogin() : Boolean {
        return preferences!!.getBoolean(IS_LOGIN,false)
    }


}