package com.example.htmlwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.graphics.SizeF
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.RemoteViews
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider.Config
import org.json.JSONObject
import java.io.File

class HtmlWidgetProvider : SmartspacerWidgetProvider() {

    companion object {
        private const val TAG = "HtmlWidgetProvider"
        private const val PREFS_NAME = "html_widget_prefs"
        private const val KEY_HTML_CONTENT = "html_content"
        private const val KEY_HTML_URL = "html_url"
        private const val KEY_USE_URL = "use_url"
        private const val KEY_WIDGET_WIDTH = "widget_width"
        private const val KEY_WIDGET_HEIGHT = "widget_height"
        
        // Default widget dimensions
        private const val DEFAULT_WIDTH = 400
        private const val DEFAULT_HEIGHT = 300
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        // This is called when the widget changes, but we're creating our own widget
        // so we don't need to handle changes from an external widget
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        // We're not binding to an existing widget, so return null
        return null
    }

    override fun getConfig(smartspacerId: String): Config {
        // Get saved dimensions or use defaults
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val width = prefs?.getInt(KEY_WIDGET_WIDTH, DEFAULT_WIDTH) ?: DEFAULT_WIDTH
        val height = prefs?.getInt(KEY_WIDGET_HEIGHT, DEFAULT_HEIGHT) ?: DEFAULT_HEIGHT
        
        return Config(
            width = width,
            height = height
        )
    }

    /**
     * Creates a RemoteViews containing a WebView to display HTML content
     */
    fun createHtmlWidget(smartspacerId: String): RemoteViews {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useUrl = prefs?.getBoolean(KEY_USE_URL, false) ?: false
        val htmlContent = prefs?.getString(KEY_HTML_CONTENT, "") ?: ""
        val htmlUrl = prefs?.getString(KEY_HTML_URL, "") ?: ""
        
        // Create a RemoteViews with a WebView
        val packageName = context?.packageName ?: return RemoteViews("com.example.htmlwidget", android.R.layout.simple_list_item_1)
        
        // We need to create a layout with a WebView
        val remoteViews = RemoteViews(packageName, R.layout.widget_html)
        
        // Set up the WebView through a Service or BroadcastReceiver
        val intent = Intent(context, HtmlWidgetUpdateService::class.java).apply {
            putExtra("smartspacerId", smartspacerId)
            putExtra("useUrl", useUrl)
            putExtra("htmlContent", htmlContent)
            putExtra("htmlUrl", htmlUrl)
        }
        
        context?.startService(intent)
        
        // Add configuration button
        val configIntent = Intent(context, HtmlWidgetConfigActivity::class.java).apply {
            putExtra("smartspacerId", smartspacerId)
        }
        val configPendingIntent = PendingIntent.getActivity(
            context,
            0,
            configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        remoteViews.setOnClickPendingIntent(R.id.btn_configure, configPendingIntent)
        
        return remoteViews
    }
    
    /**
     * Updates the widget configuration
     */
    fun updateWidgetConfig(smartspacerId: String, htmlContent: String?, htmlUrl: String?, useUrl: Boolean, width: Int, height: Int) {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        
        prefs.edit().apply {
            putString(KEY_HTML_CONTENT, htmlContent ?: "")
            putString(KEY_HTML_URL, htmlUrl ?: "")
            putBoolean(KEY_USE_URL, useUrl)
            putInt(KEY_WIDGET_WIDTH, width)
            putInt(KEY_WIDGET_HEIGHT, height)
            apply()
        }
        
        // Notify Smartspacer that the widget has been updated
        notifyChange(smartspacerId)
    }
    
    /**
     * Save HTML content to a temporary file that can be loaded by the WebView
     */
    fun saveHtmlToFile(html: String): Uri? {
        try {
            val cacheDir = context?.cacheDir ?: return null
            val htmlFile = File(cacheDir, "widget_content_${System.currentTimeMillis()}.html")
            
            htmlFile.writeText(html)
            return Uri.fromFile(htmlFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving HTML to file", e)
            return null
        }
    }
}