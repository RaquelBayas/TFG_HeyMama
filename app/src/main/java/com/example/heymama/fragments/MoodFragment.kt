package com.example.heymama.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.heymama.R
import com.example.heymama.models.Mood
import com.example.heymama.models.MoodType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : DialogFragment() {

    lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        var rootView : View = inflater.inflate(R.layout.fragment_mood,container,false)

        rootView.findViewById<Button>(R.id.cancel_mood).setOnClickListener { dismiss() }

        rootView.findViewById<Button>(R.id.submit_mood).setOnClickListener {
            var radio_group_mood : RadioGroup = rootView.findViewById(R.id.radio_group_mood)
            var selectedItem = radio_group_mood.checkedRadioButtonId
            var radioButton = rootView.findViewById<RadioButton>(selectedItem)

            setMood(radioButton.id)

        }

        return rootView
    }

    fun setMood(view : Int) {
        var moodtype : MoodType? = null

        /*when(moodtype) {

            MoodType.BIEN -> {

                print("")
            }
            MoodType.FELIZ -> print("")
            MoodType.MAL -> print("")
            MoodType.REGULAR -> print("")
            MoodType.TRISTE -> print("")

        }*/

        when(view) {
            R.id.btn_mood_regular -> {
                Toast.makeText(context, MoodType.REGULAR.name, Toast.LENGTH_SHORT).show()
                moodtype = MoodType.REGULAR
            }
            R.id.btn_mood_feliz -> {
                Toast.makeText(context, MoodType.FELIZ.name, Toast.LENGTH_SHORT).show()
                moodtype = MoodType.FELIZ
            }
            R.id.btn_mood_bien -> {
                Toast.makeText(context, MoodType.BIEN.name, Toast.LENGTH_SHORT).show()
                moodtype = MoodType.BIEN
            }
            R.id.btn_mood_mal -> {
                Toast.makeText(context, MoodType.MAL.name, Toast.LENGTH_SHORT).show()
                moodtype = MoodType.MAL
            }
            R.id.btn_mood_triste -> {
                Toast.makeText(context, MoodType.TRISTE.name, Toast.LENGTH_SHORT).show()
                moodtype = MoodType.TRISTE
            }
        }

        var mood = Mood(moodtype!!.ordinal.toString(), moodtype!!.name, Date())
        var date = Date().time
        var simpleDateFormat = SimpleDateFormat("dd MM yyyy")
        var dateString = simpleDateFormat.format(date)
        Toast.makeText(context,dateString,Toast.LENGTH_SHORT).show()

        firestore.collection("Mood").document(auth.uid.toString()).collection("Historial").document(
            dateString)
            .set(mood)

        Toast.makeText(context,"Registro guardado correctamente",Toast.LENGTH_SHORT).show()
        dismiss()

    }

}