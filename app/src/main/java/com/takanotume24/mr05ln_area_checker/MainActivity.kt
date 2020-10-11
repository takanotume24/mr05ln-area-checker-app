package com.takanotume24.mr05ln_area_checker

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val data_store = getSharedPreferences("data_store", Context.MODE_PRIVATE)
        val address = findViewById<EditText>(R.id.router_address)
        val user_name = findViewById<EditText>(R.id.router_username)
        val password = findViewById<EditText>(R.id.router_password)
        address.setText(data_store.getString("aterm_address", "aterm.me"))
        user_name.setText(data_store.getString("aterm_user_name", "admin"))
        password.setText(data_store.getString("aterm_password", ""))
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
            val user_name = findViewById<EditText>(R.id.router_username)
            val password = findViewById<EditText>(R.id.router_password)
            val get_area_text = findViewById<TextView>(R.id.get_area_text)
            val get_time_text = findViewById<TextView>(R.id.get_time_text)
            val editor = getSharedPreferences("data_store", Context.MODE_PRIVATE).edit()

            editor.putString("aterm_address", address.text.toString())
            editor.putString("aterm_user_name", user_name.text.toString())
            editor.putString("aterm_password", password.text.toString())
            editor.apply()

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
                            val dialog = AlertDialog.Builder(this)
                                .setTitle("取得失敗")
                                .setPositiveButton("OK") { dialog, which -> }
                                .create()

                            when (response.statusCode) {
                                401 -> dialog.setMessage("認証に失敗しました． \nAtermのユーザー名もしくはパスワードが間違っていませんか？")
                                else -> dialog.setMessage(ex.localizedMessage)
                            }
                            dialog.show()
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
                            val time =
                                Regex("""[+-]?\d+-[+-]?\d+-[+-]?\d+ [+-]?\d+:[+-]?\d+:[+-]?\d+""").find(
                                    data.first()
                                )?.value
                            get_time_text.text = time

                            when (earfcn) {
                                "1500" -> {
                                    get_area_text.text = "楽天エリア"
                                }
                                "5900" -> {
                                    get_area_text.text = "パートナーエリア"
                                }
                                else -> {
                                    get_area_text.text = "判別不能 earfcn=${earfcn}"
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