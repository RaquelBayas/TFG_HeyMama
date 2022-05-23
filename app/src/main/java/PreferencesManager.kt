import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    val PRIVATE_MODE = 0

    private val PREF_NAME = "SharedPreferences"
    private val IS_LOGIN = "is_login"
    private val IS_PROTECTED = "is_protected"

    val preferences: SharedPreferences? = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE)
    val editor: SharedPreferences.Editor? = preferences?.edit()

    fun createLoginSession(email: String, password: String, rol: String) {
        editor?.putBoolean(IS_LOGIN,true)
        editor?.putString("email",email)
        editor?.putString("password",password)
        editor?.putString("rol",rol)
        editor?.commit()
    }

    fun switchPrivacidad(protected: Boolean) {
        editor?.putBoolean(IS_PROTECTED,protected)
        editor?.commit()
    }

    fun isLogin() : Boolean {
        return preferences!!.getBoolean(IS_LOGIN,false)
    }

    fun isProtected() : Boolean {
        return preferences!!.getBoolean(IS_PROTECTED,false)
    }


}