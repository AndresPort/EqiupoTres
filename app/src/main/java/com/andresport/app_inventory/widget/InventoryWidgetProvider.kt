package com.andresport.app_inventory.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.andresport.app_inventory.R
import com.andresport.app_inventory.model.InventoryRepository
import com.andresport.app_inventory.utils.SessionManager
import com.andresport.app_inventory.view.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class InventoryWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_VISIBILITY = "com.andresport.app_inventory.widget.ACTION_TOGGLE_VISIBILITY"
        private const val ACTION_MANAGE_INVENTORY = "com.andresport.app_inventory.widget.ACTION_MANAGE_INVENTORY"
        const val ACTION_WIDGET_UPDATE = "com.andresport.app_inventory.widget.ACTION_WIDGET_UPDATE"
        private const val PREFS_NAME = "com.andresport.app_inventory.widget.InventoryWidget"
        private const val PREF_IS_VISIBLE = "is_balance_visible_"

        // Constantes públicas para comunicación
        const val EXTRA_LOGIN_ORIGIN = "LOGIN_ORIGIN"
        const val ORIGIN_WIDGET_VISIBILITY = "FROM_WIDGET_VISIBILITY"
        const val ORIGIN_WIDGET_MANAGE = "FROM_WIDGET_MANAGE"
        const val EXTRA_NAVIGATE_TO = "NAVIGATE_TO"
        const val DESTINATION_INVENTORY = "INVENTORY"

        fun setBalanceVisibility(context: Context, appWidgetId: Int, isVisible: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            prefs.edit().putBoolean(PREF_IS_VISIBLE + appWidgetId, isVisible).apply()
        }

        fun requestWidgetUpdate(context: Context, appWidgetId: Int) {
            val intent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        when (intent.action) {
            ACTION_TOGGLE_VISIBILITY -> toggleVisibility(context, appWidgetId)
            ACTION_MANAGE_INVENTORY -> openAppForManagement(context, appWidgetId)
            ACTION_WIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun openAppForManagement(context: Context, appWidgetId: Int) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(context, InventoryWidgetEntryPoint::class.java)
        val sessionManager = hiltEntryPoint.sessionManager()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            if (sessionManager.fetchAuthToken() != null) {
                putExtra(EXTRA_NAVIGATE_TO, DESTINATION_INVENTORY)
            } else {
                putExtra(EXTRA_LOGIN_ORIGIN, ORIGIN_WIDGET_MANAGE)
            }
        }
        context.startActivity(intent)
    }

    private fun toggleVisibility(context: Context, appWidgetId: Int) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(context, InventoryWidgetEntryPoint::class.java)
        val sessionManager = hiltEntryPoint.sessionManager()
        if (sessionManager.fetchAuthToken() != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val isVisible = prefs.getBoolean(PREF_IS_VISIBLE + appWidgetId, false)
            prefs.edit().putBoolean(PREF_IS_VISIBLE + appWidgetId, !isVisible).apply()
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
        } else {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_LOGIN_ORIGIN, ORIGIN_WIDGET_VISIBILITY)
            }
            context.startActivity(intent)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(context, InventoryWidgetEntryPoint::class.java)
        val repository = hiltEntryPoint.inventoryRepository()
        val sessionManager = hiltEntryPoint.sessionManager()
        
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val isBalanceVisible = prefs.getBoolean(PREF_IS_VISIBLE + appWidgetId, false)
        val isLoggedIn = sessionManager.fetchAuthToken() != null

        CoroutineScope(Dispatchers.IO).launch {
            val balance = repository.getTotalInventoryValue()
            val views = RemoteViews(context.packageName, R.layout.inventory_widget)

            if (isLoggedIn && isBalanceVisible) {
                val symbols = DecimalFormatSymbols(Locale("es", "ES"))
                val formatter = DecimalFormat("#,##0.00", symbols)
                views.setTextViewText(R.id.widget_balance_text, "$ ${formatter.format(balance)}")
                views.setImageViewResource(R.id.widget_toggle_visibility, R.drawable.ic_visibility_off)
            } else {
                views.setTextViewText(R.id.widget_balance_text, "$ ****")
                views.setImageViewResource(R.id.widget_toggle_visibility, R.drawable.ic_visibility)
            }

            val toggleIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_VISIBILITY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val togglePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_toggle_visibility, togglePendingIntent)

            val manageIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = ACTION_MANAGE_INVENTORY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val managePendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 1, manageIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_settings_icon, managePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
