package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.heymama.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoodActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

       /* var pieEntries = arrayListOf<PieEntry>()
        pieEntries.add(PieEntry(10.0f))
        pieEntries.add(PieEntry(20.0f))
        pieEntries.add(PieEntry(30.0f))

        var pieChart = findViewById<PieChart>(R.id.pie_chart)
        pieChart.animateXY(1000,1000)

        val pieDataSet = PieDataSet(pieEntries,"PieChart")
        pieDataSet.setColors(
            resources.getColor(R.color.pink),
            resources.getColor(R.color.purple_700),
            resources.getColor(R.color.rectangle_orange)
        )

        val pieData = PieData(pieDataSet)
        pieChart.centerText = "Mood"

        pieChart.legend.isEnabled = false //Ocultamos los tags
        pieData.setDrawValues(true)

        pieChart.data = pieData*/

        setPieChartData()
    }

    /**
     *
     * @param input
     *
     */
    private fun setPieChartData() {
        var pieEntries = arrayListOf<PieEntry>()
        var count_feliz : Float = 0.0f
        var count_bien : Float = 0.0f
        var count_regular : Float = 0.0f
        var count_mal : Float = 0.0f
        var count_triste : Float = 0.0f

        var ref = firestore.collection("Mood").document(auth.uid.toString()).collection("Historial")
        ref.addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            var docs = value!!.documents
            for(doc in docs) {
                Log.i("MOODCHART-0",doc["id"].toString())
                when(doc["id"].toString()) {

                    "0" -> {
                        count_feliz++
                        pieEntries.add(PieEntry(count_feliz))
                    }
                    "1" -> {
                        count_bien++
                        pieEntries.add(PieEntry(count_bien))
                    }
                    "2" -> {
                        count_regular++
                        pieEntries.add(PieEntry(count_regular))
                    }
                    "3" -> {
                        count_mal++
                        pieEntries.add(PieEntry(count_mal))
                    }
                    "4" -> {
                        count_triste++
                        pieEntries.add(PieEntry(count_triste))
                    }
                }
            }

            var pieChart = findViewById<PieChart>(R.id.pie_chart)
            pieChart.animateXY(1000,1000)

            val pieDataSet = PieDataSet(pieEntries,"PieChart")
            pieDataSet.setColors(
                resources.getColor(R.color.mood_feliz),
                resources.getColor(R.color.mood_bien),
                resources.getColor(R.color.mood_regular),
                resources.getColor(R.color.mood_mal),
                resources.getColor(R.color.mood_triste)
            )

            val pieData = PieData(pieDataSet)
            pieChart.centerText = "Mood"

            pieChart.legend.isEnabled = false //Ocultamos los tags
            pieData.setDrawValues(true)

            pieChart.data = pieData
        }

    }
}