package app.example.heymama.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import app.example.heymama.R
import app.example.heymama.databinding.FragmentPoliticasBinding

class PoliticasFragment : Fragment() {

    private var _binding : FragmentPoliticasBinding? = null
    private val binding get() = _binding!!
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPoliticasBinding.inflate(inflater, container, false)
        policy()
        return binding.root
    }

    private fun policy() {
        webView = binding.webView
        webView.loadUrl("https://sites.google.com/view/heymamaapp/inicio")
        webView.webViewClient = WebViewClient()
    }

}