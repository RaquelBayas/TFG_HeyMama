package app.example.heymama.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.adapters.MoodAdapter
import app.example.heymama.databinding.FragmentRegistroMoodBinding
import app.example.heymama.models.Mood
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegistroMoodFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerViewMood: RecyclerView
    private lateinit var moodArraylist: ArrayList<Mood>
    private lateinit var adapterMood: MoodAdapter
    private var _binding : FragmentRegistroMoodBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentRegistroMoodBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initRecycler()
        getMoodRegister()
        return binding.root
    }

    /**
     * Este método inicializa el recyclerview, adapter y arraylist
     */
    private fun initRecycler() {
        moodArraylist = arrayListOf()
        adapterMood = MoodAdapter(requireContext(), moodArraylist)
        recyclerViewMood = binding.recyclerViewMood
        recyclerViewMood.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        recyclerViewMood.adapter = adapterMood
    }

    private fun getMoodRegister() {
        moodArraylist.clear()
        val ref = firestore.collection("Mood").document(auth.uid.toString()).collection("Historial")
        ref.addSnapshotListener { value, error ->
            if(error != null){
                Log.e("RegistroMoodFragment",error.toString())
                return@addSnapshotListener
            }
            value!!.documents.iterator().forEach {
                val mood = it.toObject(Mood::class.java)
                moodArraylist.add(mood!!)
            }
            adapterMood.notifyDataSetChanged()
            if(moodArraylist.size>1) {
                moodArraylist.sort()
            }
        }
    }
}