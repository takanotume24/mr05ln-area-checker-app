package com.takanotume24.mr05ln_rakuten_mobile_area_checker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.util.*

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
            val address = findViewById<EditText>(R.id.router_address)
            val result_view = findViewById<TextView>(R.id.area_text)
            val user_name = findViewById<EditText>(R.id.router_username)
            val password = findViewById<EditText>(R.id.router_password)

            area_text.text = Date().toString()

            val url = "http://${address.text}/index.cgi/syslog_call_c.log"

            val http_async = url
                .httpGet()
                .timeout(1000)
                .authentication()
                .basic(username = user_name.text.toString(), password = password.text.toString())
                .responseString { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            result_view.text = ex.localizedMessage
                            println(ex)
                        }
                        is Result.Success -> {
                            val data =
                                result.get()
                                    .lines()
                                    .reversed()
                                    .filter {
                                        it.contains("earfcn")
                                    }
                            val earfcn = Regex("""(?<=earfcn=)[+-]?\d+""").find(data.first())?.value
                            when (earfcn) {
                                "1500" -> {
                                    result_view.text = "楽天エリア"
                                }
                                "5900" -> {
                                    result_view.text = "パートナーエリア"
                                }
                                else -> {
                                    result_view.text = "判別不能 earfcn=${earfcn}"
                                }
                            }
                            println(data.first())
                        }
                    }
                }
            http_async.join()

        } catch (e: Exception) {
            print(e.localizedMessage)
        }
    }

}