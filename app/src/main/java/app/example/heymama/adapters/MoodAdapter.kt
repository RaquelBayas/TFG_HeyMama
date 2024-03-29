package app.example.heymama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.R
import app.example.heymama.models.Mood
import java.text.SimpleDateFormat

class MoodAdapter(private val context: Context, private var moodList: ArrayList<Mood>
) : RecyclerView.Adapter<MoodAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_mood, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MoodAdapter.Holder, position: Int) {
        with(holder) {
            val date = moodList[position].timestamp!!.time
            mood.text = moodList[position].mood.toString()
            val dateFormat = SimpleDateFormat("dd/MM/yy \n  HH:mm")
            fecha.text = dateFormat.format(date)
            when(mood.text.toString()) {
                "FELIZ" -> image.setImageResource(R.drawable.happy)
                "BIEN" -> image.setImageResource(R.drawable.fine)
                "REGULAR" -> image.setImageResource(R.drawable.regular)
                "TRISTE" -> image.setImageResource(R.drawable.sad)
                "MAL" -> image.setImageResource(R.drawable.angry)
            }
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "moodList"
     */
    override fun getItemCount(): Int {
       return moodList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fecha : TextView = itemView.findViewById(R.id.mood_fecha)
        var mood : TextView = itemView.findViewById(R.id.mood_estado)
        var image : ImageView = itemView.findViewById(R.id.mood_image)
    }

}