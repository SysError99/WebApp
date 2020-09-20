package com.nate.webapp

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebSettings
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

const val gameLocation = "file:///android_asset/www/index.html"

const val javaScript = "(function(){events.push(JSON.stringify({res:\"NATIVE_dialog\",name:\""
const val javaScriptTrue = "\",data:true}));})();"
const val javaScriptFalse = "\",data:false}));})();"
const val javaScriptMid = "\",data:\""
const val javaScriptBack = "\"}));})();"

class MainActivity : AppCompatActivity() {
    public val activity: AppCompatActivity = this
    public lateinit var dataStorage: SharedPreferences

    private lateinit var webView: WebView
    private lateinit var webSettings: WebSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataStorage = getSharedPreferences("userData", MODE_PRIVATE)

        webView = findViewById(R.id.webview)

        webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.mediaPlaybackRequiresUserGesture = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.addJavascriptInterface(this, "App")

        //webView.setInitialScale(67)
        webView.loadUrl(gameLocation)
    }

    @JavascriptInterface
    public fun loadData(location: String): String {
        return dataStorage.getString(location, "")!!.replace("\"", "")
    }

    @JavascriptInterface
    public fun saveData(location: String, data: String) {
        dataStorage.edit().putString(location, data.replace("\"", "")).commit()
    }

    @JavascriptInterface
    public fun alert(str: String){
        var alertDialog: AlertDialog = activity.let {
            var builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage(str)
                setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                })
            }
            builder.create()
        }
        alertDialog.show()
    }
    @JavascriptInterface
    public fun confirm(name: String, str: String){
        var alertDialog: AlertDialog = activity.let {
            var builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage(str)
                setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                    webView.post(Runnable {
                        run(){
                            webView.evaluateJavascript(javaScript + name + javaScriptTrue, ValueCallback {  })
                        }
                    })
                })
                setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                    webView.post(Runnable {
                        run(){
                            webView.evaluateJavascript(javaScript + name + javaScriptFalse, ValueCallback {  })
                        }
                    })
                })
            }
            builder.create()
        }
        alertDialog.show()
    }

    @JavascriptInterface
    public fun prompt(name: String, str: String){
        var alertDialog: AlertDialog = activity.let {
            var builder = AlertDialog.Builder(it)
            var input: EditText = EditText(it)
            if(name.contains("password"))
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
            else
                input.setInputType(InputType.TYPE_CLASS_TEXT)
            input.hint = str
            builder.setView(input)
            builder.apply {
                setPositiveButton("Submit", DialogInterface.OnClickListener { dialog, id ->
                    webView.post(Runnable {
                        run(){
                            webView.evaluateJavascript(javaScript +name + javaScriptMid + input.text + javaScriptBack, ValueCallback {  })
                        }
                    })
                })
                setNegativeButton("Cancel",null)
            }
            builder.create()
        }
        alertDialog.show()
    }
}