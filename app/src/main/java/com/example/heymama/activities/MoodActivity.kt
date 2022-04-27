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
        var count: Float = 0.0f

        var ref = firestore.collection("Mood").document(auth.uid.toString())
        ref.collection("Historial").addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            var docs = value!!.documents
            for(doc in docs) {
                when(doc["id"].toString()) {
                    "0" -> {
                        count_feliz++
                        count = (count_feliz * 100) / docs.size
                        pieEntries.add(PieEntry(count))
                    }
                    "1" -> {
                        count_bien++
                        count = (count_bien * 100) / docs.size
                        pieEntries.add(PieEntry(count))
                    }
                    "2" -> {
                        count_regular++
                        count = (count_regular * 100) / docs.size
                        pieEntries.add(PieEntry(count))
                    }
                    "3" -> {
                        count_mal++
                        count = (count_mal * 100) / docs.size
                        pieEntries.add(PieEntry(count))
                    }
                    "4" -> {
                        count_triste++
                        count = (count_triste * 100) / docs.size
                        pieEntries.add(PieEntry(count))
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