package com.example.apichoice

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var apiList: ArrayAdapter<String, *>
    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Spinner with API options
        val apiOptions = listOf("https://jsonplaceholder.typicode.com", "https://fakestoreapi.com")
        apiList = ArrayAdapter(this, android.R.layout.simple_spinner_item, apiOptions)
        findViewById<android.widget.Spinner>(R.id.apiSpinner).adapter = apiList

        // Setup Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(apiList.getItem(0)) // default API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(ApiService::class.java)

        // On API change, update Retrofit base URL
        findViewById<android.widget.Spinner>(R.id.apiSpinner).onItemSelectedListener = object :
            android.widget.Spinner.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.Spinner?, view: android.widget.View?, position: Int, id: Long) {
                val newBaseUrl = apiList.getItem(position)
                retrofit = retrofit.newBuilder()
                    .baseUrl(newBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                service = retrofit.create(ApiService::class.java)
            }
        }

        // Fetch data on button click
        findViewById<android.widget.Button>(R.id.fetchButton).setOnClickListener {
            fetchData()
        }
    }

    private fun fetchData() {
        val api = service.getPosts()
        api.enqueue(object : Call.Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()
                    Toast.makeText(this@MainActivity, "Fetched ${posts.size} posts", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    interface ApiService {
        @GET("posts")
        fun getPosts(): Call<List<Post>>
    }

    data class Post(
        val id: Int,
        val title: String,
        val body: String
    )
}
