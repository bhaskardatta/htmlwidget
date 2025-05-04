package com.example.htmlwidget

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RemoteViews
import java.io.File
import java.net.URL

/**
 * Service responsible for updating the HTML content in the widget's WebView
 */
class HtmlWidgetUpdateService : Service() {

    companion object {
        private const val TAG = "HtmlWidgetUpdateService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_NOT_STICKY
    }

    private fun handleIntent(intent: Intent) {
        val smartspacerId = intent.getStringExtra("smartspacerId") ?: return
        val useUrl = intent.getBooleanExtra("useUrl", false)
        val htmlContent = intent.getStringExtra("htmlContent") ?: ""
        val htmlUrl = intent.getStringExtra("htmlUrl") ?: ""

        try {
            if (useUrl && htmlUrl.isNotEmpty()) {
                // Load content from URL
                loadFromUrl(htmlUrl, smartspacerId)
            } else if (htmlContent.isNotEmpty()) {
                // Load direct HTML content
                loadHtmlContent(htmlContent, smartspacerId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating HTML widget", e)
        } finally {
            stopSelf()
        }
    }

    private fun loadFromUrl(url: String, smartspacerId: String) {
        Thread {
            try {
                val content = URL(url).readText()
                loadHtmlContent(content, smartspacerId)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading HTML from URL: $url", e)
                // Load error message as HTML
                val errorHtml = "<html><body><h2>Error Loading Content</h2><p>Failed to load content from URL: $url</p><p>${e.message}</p></body></html>"
                loadHtmlContent(errorHtml, smartspacerId)
            }
        }.start()
    }

    private fun loadHtmlContent(html: String, smartspacerId: String) {
        // Save HTML to a file that can be loaded by the WebView
        val htmlFile = saveHtmlToFile(html)
        if (htmlFile != null) {
            // Notify the widget provider to update the widget
            val provider = HtmlWidgetProvider()
            provider.attachContext(this)
            provider.notifyChange(smartspacerId)
        }
    }

    private fun saveHtmlToFile(html: String): File? {
        try {
            val cacheDir = cacheDir
            val htmlFile = File(cacheDir, "widget_content_${System.currentTimeMillis()}.html")
            
            // Add viewport meta tag for better scaling if not present
            val modifiedHtml = if (!html.contains("<meta name=\"viewport\"")) {
                "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"></head><body>$html</body></html>"
            } else {
                html
            }
            
            htmlFile.writeText(modifiedHtml)
            return htmlFile
        } catch (e: Exception) {
            Log.e(TAG, "Error saving HTML to file", e)
            return null
        }
    }
}