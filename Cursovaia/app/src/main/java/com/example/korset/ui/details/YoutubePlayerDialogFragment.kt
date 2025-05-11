package com.example.korset.ui.details // или ui.player

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.example.korset.databinding.DialogYoutubePlayerBinding // ViewBinding

class YoutubePlayerDialogFragment : DialogFragment() {

    private var _binding: DialogYoutubePlayerBinding? = null
    private val binding get() = _binding!!

    private var videoKey: String? = null

    companion object {
        private const val ARG_VIDEO_KEY = "video_key"
        const val TAG = "YoutubePlayerDialog" // Тег для показа диалога

        fun newInstance(youtubeVideoKey: String): YoutubePlayerDialogFragment {
            val fragment = YoutubePlayerDialogFragment()
            val args = Bundle()
            args.putString(ARG_VIDEO_KEY, youtubeVideoKey)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем ключ видео из аргументов
        videoKey = arguments?.getString(ARG_VIDEO_KEY)
        // Стиль диалога, чтобы убрать заголовок и сделать фон прозрачным
        // (фон будет задан макетом)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogYoutubePlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled") // Нужно для YouTube IFrame API
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoKey?.let { key ->
            setupWebView(key)
        } ?: dismiss() // Закрываем диалог, если ключ не передан

        // --- ДОБАВЛЯЕМ ОБРАБОТКУ КНОПКИ ЗАКРЫТЬ ---
        binding.buttonClosePlayer.setOnClickListener {
            dismiss() // Просто закрываем диалоговое окно
        }
        // -----------------------------------------
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(key: String) {
        binding.youtubeWebview.settings.javaScriptEnabled = true // Включаем JavaScript
        // Позволяет видео воспроизводиться внутри WebView
        binding.youtubeWebview.webChromeClient = WebChromeClient()
        // Чтобы ссылки открывались в WebView, а не в браузере
        binding.youtubeWebview.webViewClient = WebViewClient()

        // HTML-код для встраивания YouTube плеера через IFrame API
        // Мы используем 100% ширины/высоты и передаем videoId
        val htmlData = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { margin: 0; overflow: hidden; background-color: black; }
                    iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
                </style>
            </head>
            <body>
                <iframe src="https://www.youtube.com/embed/$key?autoplay=1&fs=0&modestbranding=1"
                        frameborder="0" allow="autoplay; encrypted-media" allowfullscreen>
                </iframe>
            </body>
            </html>
        """.trimIndent()

        // Загружаем наш HTML в WebView
        binding.youtubeWebview.loadData(htmlData, "text/html", "utf-8")
    }


    // Убедимся, что WebView останавливается при закрытии диалога
    override fun onPause() {
        super.onPause()
        binding.youtubeWebview.onPause() // Пауза WebView
    }

    override fun onResume() {
        super.onResume()
        binding.youtubeWebview.onResume() // Возобновление WebView
    }

    override fun onDestroyView() {
        // Важно уничтожить WebView, чтобы остановить видео и освободить ресурсы
        binding.youtubeWebview.destroy()
        _binding = null
        super.onDestroyView()
    }


}