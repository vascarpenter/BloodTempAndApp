package com.hatenablog.gikoha.bloodtempandapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.concurrent.TimeUnit


data class BloodTempPost (
    val apikey: String,
    val temp: String,
    val memo: String,
)

class MainActivity : AppCompatActivity()
{
    private var data: ArrayList<BloodTemp> = ArrayList()
    private var mainact = this
    private lateinit var titleField: TextView
    private lateinit var tempField: TextInputEditText
    private lateinit var memoField: TextInputEditText
    private lateinit var recyclerView: RecyclerView

    private fun loadData()
    {
        val request = Request.Builder().url(BuildConfig.bloodgetapiurl).build()

        buildOkHttp().newCall(request).enqueue(object : Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                titleField.text = e.message
            }

            override fun onResponse(call: Call, response: Response)
            {
                val responseText: String = response.body?.string()?.trimMargin() ?: ""
                try
                {
                    // get array of json to List<BloodTemp>
                    val itemType = object : TypeToken<ArrayList<BloodTemp>>(){}.type
                    val element: ArrayList<BloodTemp> = Gson().fromJson(responseText, itemType)
                    data = element


                } catch (e: JsonSyntaxException)
                {
                    Log.e("JSON", "parse error", e)
                }

                // run in UI thread
                runOnUiThread {
                    val adapter = RecyclerAdapter(data)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                    val layout = recyclerView.layoutManager
                    val count = recyclerView.adapter!!.itemCount
                    layout?.scrollToPosition(count -1 ) // scroll to bottom
                }

            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).orientation)

        recyclerView = findViewById(R.id.listView)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadData()

        titleField = findViewById(R.id.title)
        tempField = findViewById(R.id.tempField)
        memoField = findViewById(R.id.memoField)


        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            closeSoftKeyboard()

            val tempdata = BloodTempPost(BuildConfig.bloodapikey,
                                         tempField.text.toString(),
                                         memoField.text.toString())
            val json = Gson().toJson(tempdata)
            //titleField.text = json

            val mediaTypeJson = "application/json; charset=utf8".toMediaType()

            val request: Request = Request.Builder()
                .url(BuildConfig.bloodpostapiurl)
                .post(json.toRequestBody(mediaTypeJson))
                .build()

            buildOkHttp().newCall(request).enqueue(object : Callback
            {
                override fun onResponse(call: Call, response: Response)
                {
                    val responseBody = response.body?.string().orEmpty()
                    titleField.text = "Post data $responseBody" // will be OK
                    loadData()  // refresh list
                }

                override fun onFailure(call: Call, e: IOException)
                {
                    Log.e("Post Error", e.toString())
                }
            })
        }
    }

    private fun closeSoftKeyboard()
    {
        // close soft keyboard
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(mainact)).windowToken, 0)
    }

    private fun buildOkHttp(): OkHttpClient
    {
        val client = OkHttpClient.Builder()
        client.connectTimeout(20, TimeUnit.SECONDS)
        client.readTimeout(15, TimeUnit.SECONDS)
        client.writeTimeout(15, TimeUnit.SECONDS)
        return client.build()
    }

}