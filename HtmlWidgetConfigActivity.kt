package com.example.htmlwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

/**
 * Configuration activity for the HTML Widget
 * Allows users to input HTML content or URL and set widget dimensions
 */
class HtmlWidgetConfigActivity : Activity() {

    private lateinit var radioHtml: RadioButton
    private lateinit var radioUrl: RadioButton
    private lateinit var editHtml: EditText
    private lateinit var editUrl: EditText
    private lateinit var seekWidth: SeekBar
    private lateinit var seekHeight: SeekBar
    private lateinit var txtWidthValue: TextView
    private lateinit var txtHeightValue: TextView
    private lateinit var btnSave: Button
    private lateinit var btnPreview: Button
    
    private var smartspacerId: String? = null
    
    companion object {
        private const val PREFS_NAME = "html_widget_prefs"
        private const val KEY_HTML_CONTENT = "html_content"
        private const val KEY_HTML_URL = "html_url"
        private const val KEY_USE_URL = "use_url"
        private const val KEY_WIDGET_WIDTH = "widget_width"
        private const val KEY_WIDGET_HEIGHT = "widget_height"
        
        // Widget size constraints
        private const val MIN_WIDTH = 100
        private const val MAX_WIDTH = 1000
        private const val MIN_HEIGHT = 100
        private const val MAX_HEIGHT = 1000
        private const val DEFAULT_WIDTH = 400
        private const val DEFAULT_HEIGHT = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_widget_config)
        
        // Get the smartspacer ID from the intent
        smartspacerId = intent.getStringExtra("smartspacerId")
        if (smartspacerId == null) {
            Toast.makeText(this, "Error: Missing widget ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        initViews()
        
        // Load saved configuration
        loadConfig()
        
        // Set up listeners
        setupListeners()
    }
    
    private fun initViews() {
        radioHtml = findViewById(R.id.radio_html)
        radioUrl = findViewById(R.id.radio_url)
        editHtml = findViewById(R.id.edit_html)
        editUrl = findViewById(R.id.edit_url)
        seekWidth = findViewById(R.id.seek_width)
        seekHeight = findViewById(R.id.seek_height)
        txtWidthValue = findViewById(R.id.txt_width_value)
        txtHeightValue = findViewById(R.id.txt_height_value)
        btnSave = findViewById(R.id.btn_save)
        btnPreview = findViewById(R.id.btn_preview)
        
        // Configure seek bars
        seekWidth.max = MAX_WIDTH - MIN_WIDTH
        seekHeight.max = MAX_HEIGHT - MIN_HEIGHT
    }
    
    private fun loadConfig() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useUrl = prefs.getBoolean(KEY_USE_URL, false)
        val htmlContent = prefs.getString(KEY_HTML_CONTENT, "") ?: ""
        val htmlUrl = prefs.getString(KEY_HTML_URL, "") ?: ""
        val width = prefs.getInt(KEY_WIDGET_WIDTH, DEFAULT_WIDTH)
        val height = prefs.getInt(KEY_WIDGET_HEIGHT, DEFAULT_HEIGHT)
        
        // Set radio buttons
        radioHtml.isChecked = !useUrl
        radioUrl.isChecked = useUrl
        
        // Set content fields
        editHtml.setText(htmlContent)
        editUrl.setText(htmlUrl)
        
        // Update input field visibility
        updateInputVisibility(useUrl)
        
        // Set seek bars
        seekWidth.progress = width - MIN_WIDTH
        seekHeight.progress = height - MIN_HEIGHT
        
        // Update text views
        txtWidthValue.text = "${width}px"
        txtHeightValue.text = "${height}px"
    }
    
    private fun setupListeners() {
        // Radio button listeners
        radioHtml.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateInputVisibility(false)
            }
        }
        
        radioUrl.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateInputVisibility(true)
            }
        }
        
        // Seek bar listeners
        seekWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val width = progress + MIN_WIDTH
                txtWidthValue.text = "${width}px"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        seekHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val height = progress + MIN_HEIGHT
                txtHeightValue.text = "${height}px"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Button listeners
        btnSave.setOnClickListener {
            saveConfig()
        }
        
        btnPreview.setOnClickListener {
            previewHtml()
        }
    }
    
    private fun updateInputVisibility(useUrl: Boolean) {
        if (useUrl) {
            editHtml.visibility = View.GONE
            editUrl.visibility = View.VISIBLE
        } else {
            editHtml.visibility = View.VISIBLE
            editUrl.visibility = View.GONE
        }
    }
    
    private fun saveConfig() {
        val useUrl = radioUrl.isChecked
        val htmlContent = editHtml.text.toString()
        val htmlUrl = editUrl.text.toString()
        val width = seekWidth.progress + MIN_WIDTH
        val height = seekHeight.progress + MIN_HEIGHT
        
        // Validate input
        if (useUrl && htmlUrl.isEmpty()) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
            return
        } else if (!useUrl && htmlContent.isEmpty()) {
            Toast.makeText(this, "Please enter HTML content", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Update widget configuration
        val provider = HtmlWidgetProvider()
        provider.attachContext(this)
        provider.updateWidgetConfig(
            smartspacerId!!, 
            htmlContent, 
            htmlUrl, 
            useUrl, 
            width, 
            height
        )
        
        Toast.makeText(this, "Widget configuration saved", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun previewHtml() {
        val useUrl = radioUrl.isChecked
        val content = if (useUrl) editUrl.text.toString() else editHtml.text.toString()
        
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter content to preview", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Launch preview activity
        val intent = Intent(this, HtmlPreviewActivity::class.java).apply {
            putExtra("useUrl", useUrl)
            putExtra("content", content)
        }
        startActivity(intent)
    }
}