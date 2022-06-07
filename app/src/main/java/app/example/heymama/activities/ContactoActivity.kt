package app.example.heymama.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import app.example.heymama.*
import app.example.heymama.databinding.ActivityContactoBinding
import app.example.heymama.models.Consulta
import app.example.heymama.models.Notification
import com.google.common.io.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ContactoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var spinnerConsultas: Spinner
    private lateinit var temas: Array<String>
    private lateinit var binding: ActivityContactoBinding

    /**
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirebase()
        spinnerConsultas = binding.spinnerConsultas
        temas = resources.getStringArray(R.array.temasConsultas)
        val adapter = ArrayAdapter(this,R.layout.spinner_item,temas)
        spinnerConsultas.adapter = adapter
        initButtons()
    }

    /**
     * Este método permite inicializar los objetos de Firebase
     */
    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initButtons() {
        binding.btnSendConsulta.setOnClickListener {
            sendConsulta()
        }

        binding.btnMisConsultas.setOnClickListener {
            misConsultas()
        }

        binding.txt112.setOnClickListener {
            call(binding.txt112.text.toString())
        }

        binding.txt024.setOnClickListener {
            call(binding.txt024.text.toString())
        }
    }

    /**
     * Este método permite marcar un número telefónico.
     * @param number String : Teléfono.
     */
    private fun call(number: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$number")
        startActivity(dialIntent)
    }

    /**
     * Este método permite acceder a las consultas realizadas.
     * @param input
     */
    private fun misConsultas() {
        val intent = Intent(this, ConsultasActivity::class.java)
        startActivity(intent)
    }

    /**
     * Este método permite enviar la consulta del usuario.
     */
    private fun sendConsulta() {
        val spinnerConsultas : Spinner = binding.spinnerConsultas
        val txt_consulta : EditText = binding.editTextConsulta
        val txt_tema : String = spinnerConsultas.selectedItem.toString()
        val user : String = auth.uid.toString()
        val ref = firestore.collection("Consultas").document(txt_tema).collection("Consultas").document()
        val consulta = Consulta(ref.id,user,txt_tema,txt_consulta.text.toString(),Date())
        if(txt_consulta.text.isNotEmpty() && (txt_tema!= "Elige un tema")) {
            ref.set(consulta)
            val notifRef = database.reference.child("NotificationsConsultas")
            val notification = Notification(user,"",ref.id,txt_consulta.text.toString(),"ha realizado una consulta",Date())
            notifRef.push().setValue(notification)
            Toast.makeText(this,"Consulta enviada correctamente",Toast.LENGTH_SHORT).show()
            txt_consulta.setText("")
        } else if(txt_consulta.text.isEmpty()){
            Toast.makeText(this,"Escribe el contenido de tu consulta.",Toast.LENGTH_SHORT).show()
        } else if(txt_tema == "Elige un tema") {
            Toast.makeText(this, "Selecciona el tema de tu consulta.",Toast.LENGTH_SHORT).show()
        } else {
            Utils.showErrorToast(this)
        }
    }

}