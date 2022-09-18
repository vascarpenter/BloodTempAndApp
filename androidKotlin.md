# Androidの場合

- TextInputEditText にデータを入力し、ボタンでsubmit
  - その下にListView でデータ一覧を表示するような単純なアプリ

- APIとして server.tk が 
  - `/post` の場合 JSONで `{"key":"APIKEY", "temp":"36.0"}` で post できるものとし、
  - `/get?key=APIKEY` の場合 JSON で `[{"date":"xx","temp":"36.0","memo":""},...] ` を返すものとする
  - APIKEY はいわばpassword

- letsencrypt の問題があるので、 network_security_config.xml は こんな感じで certificateをいれる
  ```
  ...
          <trust-anchors>
            <certificates src="@raw/cert" />
            <certificates src="@raw/isrgrootx1" />
            <certificates src="@raw/letsencryptauthorityx3" />
            <certificates src="system" />
        </trust-anchors>
  ...
  ```
  - AndroidManifest.xml に下記を追加
  ```
  android:networkSecurityConfig="@xml/network_security_config"
  <uses-permission android:name="android.permission.INTERNET" />
  ```
- kotlinコード

```kotlin

data class BloodTemp (
    val date: String,
    val temp: String,
    val memo: String,
)

data class BloodTempPost (
    val key: String,
    val temp: String,
)

class MainActivity : AppCompatActivity()
{
    var data = mutableListOf<Map<String, String>>()
    var mainact = this
    private lateinit var titleField: TextView
    private lateinit var tempField: TextInputEditText
    private lateinit var listView: ListView

    private fun loadData()
    {
        val request = Request.Builder().url("https://server.tk/get?key=APIKEY").build()

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
                    val element: List<BloodTemp> = Gson().fromJson(responseText, Array<BloodTemp>::class.java).toList()

                    // make map list from List<BloodTemp>
                    val newdata = mutableListOf<Map<String, String>>()
                    element.forEach {
                        newdata += mapOf("text1" to (it.date + " " + it.temp), "text2" to it.memo)
                    }
                    data = newdata

                    // run in UI thread
                    Handler(Looper.getMainLooper()).post {
                        val adapter = SimpleAdapter(
                            mainact,
                            data,
                            android.R.layout.simple_list_item_2,
                            arrayOf("text1", "text2"),
                            intArrayOf(android.R.id.text1, android.R.id.text2)
                        )       // 作り直さないと反映されないぽい
                        listView.adapter = adapter
                        listView.setSelection(listView.count - 6) // scroll to bottom
                        adapter.notifyDataSetChanged()
                    }
                } catch (e: JsonSyntaxException)
                {
                    Log.e("JSON", "parse error", e)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        titleField = findViewById(R.id.title)
        tempField = findViewById(R.id.tempField)

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            val tempdata = BloodTempPost("APIKEY", tempField.text.toString())
            val json = Gson().toJson(tempdata)
            titleField.text = json

            val mediaTypeJson = "application/json; charset=utf8".toMediaType()

            val request: Request = Request.Builder()
                .url("https://server.tk/post")
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
        loadData()
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
```
