package app.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.example.heymama.R
import app.example.heymama.databinding.ActivityMoodBinding
import app.example.heymama.fragments.RegistroMoodFragment
import app.example.heymama.models.MoodType
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoodActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var pieEntries: ArrayList<PieEntry>
    private lateinit var binding: ActivityMoodBinding
    /**
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        pieEntries = arrayListOf()
        setPieChartData()

        binding.btnConsultarRegistro.setOnClickListener {
            val fragment = RegistroMoodFragment()
            supportFragmentManager.beginTransaction().replace(R.id.moodActivity,fragment)
                .addToBackStack(null).commit()
        }
    }

    /**
     */
    private fun setPieChartData() {
        pieEntries.clear()
        var count_feliz = 0.0f
        var count_bien = 0.0f
        var count_regular  = 0.0f
        var count_mal = 0.0f
        var count_triste = 0.0f
        var count = 0.0f

        val ref = firestore.collection("Mood").document(auth.uid.toString())
        ref.collection("Historial").addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            val docs = value!!.documents

            for(doc in docs) {
                when(doc["id"].toString()) {
                    "0" -> {
                        count_feliz++
                    }
                    "1" -> {
                        count_bien++
                    }
                    "2" -> {
                        count_regular++
                    }
                    "3" -> {
                        count_mal++
                    }
                    "4" -> {
                        count_triste++
                    }
                }

            }
            count = (count_feliz * 100) / docs.size
            if(count!= (0.0).toFloat()) pieEntries.add(PieEntry(count,MoodType.FELIZ.name))
            count = (count_bien * 100) / docs.size
            if(count!= (0.0).toFloat())pieEntries.add(PieEntry(count,MoodType.BIEN.name))
            count = (count_regular * 100) / docs.size
            if(count!= (0.0).toFloat())pieEntries.add(PieEntry(count,MoodType.REGULAR.name))
            count = (count_mal * 100) / docs.size
            if(count!=(0.0).toFloat())pieEntries.add(PieEntry(count,MoodType.MAL.name))
            count = (count_triste * 100) / docs.size
            if(count!=(0.0).toFloat())pieEntries.add(PieEntry(count,MoodType.TRISTE.name))

            val pieChart = findViewById<PieChart>(R.id.pie_chart)
            pieChart.animateXY(1000,1000)

            val colors = arrayListOf<Int>()
            val pieDataSet = PieDataSet(pieEntries,"Emociones")
            pieDataSet.values.iterator().forEach {
                when(it.label.toString()) {
                    MoodType.FELIZ.name -> colors.add(resources.getColor(R.color.pink2))
                    MoodType.BIEN.name -> colors.add(resources.getColor(R.color.rectangle_purple))
                    MoodType.REGULAR.name -> colors.add(resources.getColor(R.color.rectangle_orange))
                    MoodType.TRISTE.name -> colors.add(resources.getColor(R.color.mood_triste))
                    MoodType.MAL.name -> colors.add(resources.getColor(R.color.mood_mal))
                }
            }

            pieDataSet.colors = colors

            val pieData = PieData(pieDataSet)
            pieData.setValueFormatter(PercentFormatter())
            pieData.setValueTextSize(12f)
            pieChart.centerText = "Mood"
            pieChart.description.isEnabled = false

            pieData.setDrawValues(true)

            val legend = pieChart.legend
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.textSize = 12f

            pieChart.data = pieData
        }
    }

}