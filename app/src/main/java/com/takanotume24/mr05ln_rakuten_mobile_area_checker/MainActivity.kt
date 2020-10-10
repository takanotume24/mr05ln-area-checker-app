package com.takanotume24.mr05ln_rakuten_mobile_area_checker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.rybalkinsd.kohttp.ext.httpGetAsync
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.Response

class MainActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val button = findViewById<Button>(R.id.get)

        button.setOnClickListener {
            scope.launch {
                myTask()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scope.coroutineContext.cancelChildren()
    }

    private suspend fun myTask() {
        try {
            val response: Deferred<Response> = "https://google.com".httpGetAsync()
            val domain_area = findViewById<EditText>(R.id.domainText)
            area_text.text = "now loading..."
            while (!response.isCompleted) {
                Thread.sleep(100)
            }
            val result_view = findViewById<TextView>(R.id.area_text)
            result_view.text = response.getCompleted().body?.string()
        } catch (e: Exception) {
            print(e.localizedMessage)
        }
    }

}