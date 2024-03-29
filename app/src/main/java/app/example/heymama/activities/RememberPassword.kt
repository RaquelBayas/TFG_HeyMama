package app.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import app.example.heymama.databinding.ActivityRememberPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class RememberPassword : AppCompatActivity() {
    private lateinit var btn_recordarpassword: Button
    private lateinit var txt_recordar_email: TextView
    private lateinit var txt_email: String
    private lateinit var binding: ActivityRememberPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRememberPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txt_recordar_email = binding.txtRecordarEmail
        btn_recordarpassword = binding.btnRecordarpassword

        btn_recordarpassword.setOnClickListener {
            if (txt_recordar_email.text.isEmpty()) {
                Toast.makeText(this, "Introduce el correo de recuperación.", Toast.LENGTH_SHORT).show()
            } else {
                txt_email = txt_recordar_email.text.toString()
                FirebaseAuth.getInstance().sendPasswordResetEmail(txt_email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Se ha enviado correctamente el email de recuperación de contraseña.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Se ha producido un error.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

}