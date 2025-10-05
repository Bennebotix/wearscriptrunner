package com.bennebotix.wearscriptrunner

import android.util.TypedValue
import android.text.Editable
import android.text.TextWatcher
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("ScriptRunnerPrefs", Context.MODE_PRIVATE)
        val currentPath = prefs.getString("script_path", "/data/local/scripts") ?: "/data/local/scripts"

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(16, 16, 16, 16)
        }

        val topSpacer = TextView(this).apply {
            text = "\n"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val bottomSpacer = TextView(this).apply {
            text = "\n\n"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        editText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }

            setText(currentPath)
            inputType = EditorInfo.TYPE_CLASS_TEXT or
                        EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                        EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imeOptions = EditorInfo.IME_ACTION_DONE
            isSingleLine = true

            setTextColor(getColor(android.R.color.white))
            setHintTextColor(getColor(android.R.color.darker_gray))
            setBackgroundColor(getColor(R.color.background_dark))
            textSize = 16f
            minHeight = (48 * resources.displayMetrics.density).toInt()

            setAutoSizeTextTypeUniformWithConfiguration(
                8, 14, 1, TypedValue.COMPLEX_UNIT_SP
            )
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                val baseSize = 14f
                val minSize = 8f
                val newSize = (baseSize - (length * 0.15f)).coerceAtLeast(minSize)
                editText.textSize = newSize
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val refreshButton = MaterialButton(this).apply {
            text = "Refresh Scripts"
            textSize = 12f
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setOnClickListener {
                val intent = Intent("com.bennebotix.SCRIPTS_REFRESH")
                sendBroadcast(intent)
            }
        }

        val exitButton = MaterialButton(this).apply {
            text = "Exit"
            isAllCaps = false
            textSize = 12f
            setBackgroundColor(getColor(R.color.button_blue))
            setTextColor(getColor(android.R.color.white))
            cornerRadius = 16
            setOnClickListener {
                savePath(editText.text.toString(), prefs)
                finish()
            }
        }
        
        layout.addView(topSpacer)
        layout.addView(editText)
        layout.addView(refreshButton)
        layout.addView(exitButton)
        layout.addView(bottomSpacer)
        scrollView.addView(layout)
        setContentView(scrollView)

        editText.requestFocus()
        editText.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun savePath(path: String, prefs: android.content.SharedPreferences) {
        prefs.edit().putString("script_path", path).apply()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
        editText.clearFocus()
    }
}
