package app.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.florent37.viewanimator.ViewAnimator;
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import app.example.heymama.R
import app.example.heymama.databinding.ActivityRespirarBinding
import com.github.florent37.viewanimator.AnimationListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RespirarActivity : AppCompatActivity() {

    private lateinit var txt_exhalar: TextView
    private lateinit var btn_empezar_respirar: Button
    private lateinit var btn_parar_respiracion: Button
    private lateinit var animation: ViewAnimator
    private lateinit var binding: ActivityRespirarBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRespirarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        txt_exhalar = findViewById(R.id.txt_exhalar)

        btn_empezar_respirar = findViewById(R.id.btn_empezar_respirar)
        btn_empezar_respirar.setOnClickListener {
            startBreathing()
        }
        btn_parar_respiracion = findViewById(R.id.btn_parar_respiracion)
        btn_parar_respiracion.setOnClickListener {
            animation.cancel()
        }
    }

    /**
     * Este método sirve para empezar la animación de control de la respiración.
     */
    private fun startBreathing() {
       val img_respirar : ImageView = findViewById(R.id.img_respirar)

        animation = ViewAnimator.animate(img_respirar).alpha(0f, 1f).onStart(object: AnimationListener.Start {
            override fun onStart() {
                txt_exhalar.text = "Inhala... Exhala"
            }
        }).scale(0.02f, 1.5f, 0.02f)
            .rotation(360f)
            .repeatCount(5)
            .duration(6500) // 6.5 segundos
            .onStop(object: AnimationListener.Stop {
                override fun onStop() {
                    txt_exhalar.text = ""
                    img_respirar.scaleX = 1.0f
                    img_respirar.scaleY = 1.0f
                }
            })
            .start()
    }

}