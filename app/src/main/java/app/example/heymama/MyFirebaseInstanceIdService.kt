package app.example.heymama

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceIdService: FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val newToken = FirebaseMessaging.getInstance().token

        if(firebaseUser!=null) {
            updateToken(newToken.toString())
        }
    }

    /**
     * @param newToken String
     */
    private fun updateToken(newToken: String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(newToken)
        ref.child(firebaseUser!!.uid).setValue(token)
    }

}