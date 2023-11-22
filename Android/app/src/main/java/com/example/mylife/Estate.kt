package com.example.mylife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

import androidx.fragment.app.Fragment

//부동산 웹뷰 처리
class Estate : Fragment() {
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // 레이아웃 파일과 연결
        val view = inflater.inflate(R.layout.estate, container, false)

        // WebView 초기화 및 설정
        webView = view.findViewById(R.id.webView)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화

        // 웹 페이지 로드
        webView.loadUrl("https://m.land.naver.com/") // 웹 페이지 URL을 설정

        return view
    }
}