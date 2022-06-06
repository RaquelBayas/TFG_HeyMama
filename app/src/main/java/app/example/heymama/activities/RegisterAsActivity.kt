package app.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.example.heymama.databinding.ActivityRegisterAsBinding

class RegisterAsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterAsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterAsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUsuario.setOnClickListener { initRegister("Usuario") }
        binding.btnProfesional.setOnClickListener { initRegister("Profesional") }
    }

    /**
     * Este método permite acceder al registro.
     * @param rol String : Rol
     */
    private fun initRegister(rol: String){
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("Rol",rol)
        startActivity(intent)
    }
}