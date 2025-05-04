package com.example.htmlwidget

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.net.URL

/**
 * Activity for previewing HTML content before saving to the widget
 */
class HtmlPreviewActivity : Activity() {

    private lateinit var webView: WebView
    
    companion object {
        private const val TAG = "HtmlPreviewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_preview)
        
        // Enable back button in action bar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        
        webView = findViewById(R.id.preview_webview)
        setupWebView()
        
        // Get content from intent
        val useUrl = intent.getBooleanExtra("useUrl", false)
        val content = intent.getStringExtra("content") ?: ""
        
        if (content.isEmpty()) {
            Toast.makeText(this, "No content to preview", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Load content
        if (useUrl) {
            loadFromUrl(content)
        } else {
            loadHtmlContent(content)
        }
    }
    
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Handle URL loading within the WebView
                return false
            }
        }
    }
    
    private fun loadHtmlContent(html: String) {
        // Add viewport meta tag for better scaling if not present
        val modifiedHtml = if (!html.contains("<meta name=\"viewport\"")) {
            "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"></head><body>$html</body></html>"
        } else {
            html
        }
        
        webView.loadDataWithBaseURL(null, modifiedHtml, "text/html", "UTF-8", null)
    }
    
    private fun loadFromUrl(url: String) {
        try {
            webView.loadUrl(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading URL: $url", e)
            Toast.makeText(this, "Error loading URL: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}