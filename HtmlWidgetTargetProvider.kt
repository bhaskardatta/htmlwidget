package com.example.htmlwidget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate

/**
 * Target provider for the HTML Widget
 * This integrates with Smartspacer to display the HTML widget as a target
 */
class HtmlWidgetTargetProvider : SmartspacerTargetProvider() {

    companion object {
        private const val TARGET_ID = "html_widget_target"
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        // Create a widget provider to get the HTML content
        val widgetProvider = HtmlWidgetProvider()
        widgetProvider.attachContext(context ?: return emptyList())
        
        // Create the widget RemoteViews
        val remoteViews = widgetProvider.createHtmlWidget(smartspacerId)
        
        // Create a configuration intent
        val configIntent = Intent(context, HtmlWidgetConfigActivity::class.java).apply {
            putExtra("smartspacerId", smartspacerId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create a SmartspaceAction for the configuration
        val configAction = SmartspaceAction.Builder("html_widget_config_action")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_menu_preferences))
            .setTitle("Configure HTML Widget")
            .setSubtitle("Tap to configure")
            .setPendingIntent(pendingIntent)
            .build()
        
        // Create the target with the widget RemoteViews
        val target = TargetTemplate.Widget(
            context ?: return emptyList(),
            TARGET_ID,
            ComponentName(context?.packageName ?: "", HtmlWidgetTargetProvider::class.java.name),
            "HTML Widget",
            remoteViews
        ).setWidget(
            remoteViews
        ).setActionChips(
            listOf(configAction)
        ).build()
        
        return listOf(target)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "HTML Widget",
            description = "Displays custom HTML content in a widget",
            icon = Icon.createWithResource(context, android.R.drawable.ic_menu_view),
            setupActivity = ComponentName(context?.packageName ?: "", HtmlWidgetConfigActivity::class.java.name),
            widgetProvider = true
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        // We don't support dismissal
        return false
    }
}