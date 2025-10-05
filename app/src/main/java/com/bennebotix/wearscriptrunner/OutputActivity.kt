package com.bennebotix.wearscriptrunner

import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OutputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCRIPT_NAME = "script_name"
        const val EXTRA_TITLE = "title"
        const val EXTRA_OUTPUT = "output"
    }

    private lateinit var scrollView: ScrollView
    private lateinit var outputTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val horizontalMargin = (resources.displayMetrics.density * 25).toInt()
        scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                marginStart = horizontalMargin
                marginEnd = horizontalMargin
                gravity = Gravity.CENTER
            }
            isFillViewport = false
        }

        scrollView.isVerticalScrollBarEnabled = false

        outputTv = TextView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 11f
            textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            setPadding(8, 8, 8, 8)
        }

        scrollView.addView(outputTv)
        container.addView(scrollView)
        setContentView(container)

        intent.getStringExtra(EXTRA_OUTPUT)?.let {
            outputTv.text = it
        } ?: intent.getStringExtra(EXTRA_TITLE)?.let {
            outputTv.text = it
        }

        intent.getStringExtra(EXTRA_SCRIPT_NAME)?.let { name ->
            runScriptAndShow("/data/local/scripts/$name", name)
        }
    }

    private fun runScriptAndShow(scriptPath: String, name: String) {
        outputTv.text = "\n\n\n▶ $name\n\nRunning..."
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "sh $scriptPath"))
                val out = process.inputStream.bufferedReader().readText()
                val err = process.errorStream.bufferedReader().readText()
                process.waitFor()
                runOnUiThread {
                    val sb = StringBuilder()
                    sb.append("\n\n\n").append("▶ ").append(name).append("\n\n")
                    sb.append(out)
                    if (err.isNotEmpty()) {
                        sb.append("\n\nError:\n").append(err)
                    }
                    sb.append("\n\n\n\n")
                    outputTv.text = sb.toString()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    outputTv.text = "Error running $name:\n${e.message}"
                }
            }
        }.start()
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
