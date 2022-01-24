package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.heymama.R
import com.google.firebase.auth.FirebaseAuth


class RememberPassword : AppCompatActivity() {
    private lateinit var btn_recordarpassword: Button
    private lateinit var txt_recordar_email: TextView
    private lateinit var txt_email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remember_password)

        // TextView de eemail
        txt_recordar_email = findViewById(R.id.txt_recordar_email)

        // Botón Recordar contraseña
        btn_recordarpassword = findViewById(R.id.btn_recordarpassword)
        btn_recordarpassword.setOnClickListener {
            // Si no se ha escrito el email, lanza un mensaje de alerta
            if (txt_recordar_email.text.isEmpty()) {
                Toast.makeText(this, "Introduce el correo de recuperación.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Obtiene el email del TextView
                txt_email = txt_recordar_email.text.toString()

                FirebaseAuth.getInstance().sendPasswordResetEmail(txt_email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Se ha enviado correctamente el email de recuperación de contraseña.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Cierra el activity y vuelve al anterior
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Se ha producido un error.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

}