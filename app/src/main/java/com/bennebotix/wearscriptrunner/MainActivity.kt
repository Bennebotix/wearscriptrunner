package com.bennebotix.wearscriptrunner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var scrollView: ScrollView
    private lateinit var refreshReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.bennebotix.SCRIPTS_REFRESH") {
                    refreshScripts()
                }
            }
        }

        registerReceiver(refreshReceiver, IntentFilter("com.bennebotix.SCRIPTS_REFRESH"))

        scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val buttonsLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        scrollView.addView(buttonsLayout)
        setContentView(scrollView)

        if (!isRootAvailable()) {
            val button = MaterialButton(this).apply {
                text = "Root required"
                textSize = 12f
                setTextColor(getColor(android.R.color.white))
                setBackgroundColor(getColor(R.color.button_blue))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(32, 16, 32, 16)
                }
                gravity = Gravity.CENTER
                setOnClickListener {
                    val intent = Intent(this@MainActivity, OutputActivity::class.java).apply {
                        putExtra(OutputActivity.EXTRA_TITLE, "Root required")
                        putExtra(
                            OutputActivity.EXTRA_OUTPUT,
                            "This app needs root (Magisk is recomended). Please grant it when prompted."
                        )
                    }
                    startActivity(intent)
                }
            }
            buttonsLayout.addView(button)
            return
        } else {
            refreshScripts()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(refreshReceiver)
    }

    private fun listScriptsAsRoot(path: String): List<String> {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls -1 $path"))
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out.lines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun refreshScripts() {
        val prefs = getSharedPreferences("ScriptRunnerPrefs", Context.MODE_PRIVATE)
        val scriptDir = prefs.getString("script_path", "/data/local/scripts") ?: "/data/local/scripts"

        val buttonsLayout = (scrollView.getChildAt(0) as LinearLayout)
        buttonsLayout.removeAllViews()

        val settingsButton = MaterialButton(this).apply {
            text = "⚙ Settings"
            textSize = 14f
            isAllCaps = false
            cornerRadius = 0
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        
        buttonsLayout.addView(settingsButton)

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

        val scripts = listScriptsAsRoot(scriptDir)
        val horizontalMargin = (resources.displayMetrics.density * 25).toInt()

        buttonsLayout.addView(topSpacer)
        for (script in scripts) {
            val button = MaterialButton(this).apply {
                text = script
                isAllCaps = false
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(horizontalMargin, 8, horizontalMargin, 8)
                }
                gravity = Gravity.CENTER
                setOnClickListener {
                    val intent = Intent(this@MainActivity, OutputActivity::class.java).apply {
                        putExtra(OutputActivity.EXTRA_SCRIPT_NAME, script)
                    }
                    startActivity(intent)
                }
            }
            buttonsLayout.addView(button)
        }
        buttonsLayout.addView(bottomSpacer)
    }


    private fun isRootAvailable(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL) {
            val v = -event.getAxisValue(MotionEvent.AXIS_SCROLL)
            val dy = (v * 140).toInt()
            scrollView.smoothScrollBy(0, dy)
            return true
        }
        return super.onGenericMotionEvent(event)
    }
}
