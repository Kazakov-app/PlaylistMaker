package com.example.playlistmaker.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity() {

    private val iTunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var trackList = ArrayList<Track>()
    private lateinit var nothingPlaceHolder: TextView
    private lateinit var noConnectionPlaceholder: LinearLayout
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var buttonUpdate: Button
    private lateinit var recyclerView: RecyclerView

    private val iTunesService = retrofit.create(ITunesApiService::class.java)

    private var sText: String = ""
    private lateinit var etSearch: EditText

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activitySearch)) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }
        etSearch = findViewById<EditText>(R.id.etSearch)
        val iwClear = findViewById<ImageView>(R.id.iwClear)

        nothingPlaceHolder = findViewById(R.id.placeholder_nothing)
        noConnectionPlaceholder = findViewById(R.id.placeholder_no_connection)
        recyclerView = findViewById(R.id.recycler_view)

        trackAdapter = TrackAdapter(trackList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        buttonUpdate = findViewById(R.id.update)
        buttonUpdate.setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isNotEmpty()) {
                searchTracks(query)
            }
        }


        iwClear.setOnClickListener {
            etSearch.text.clear()
            val view: View? = this.currentFocus
            val iMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            iMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
            nothingPlaceHolder.isVisible = false
            noConnectionPlaceholder.isVisible = false
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            recyclerView.visibility = View.GONE
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iwClear.isVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let { sText = s.toString() }
            }
        }

        etSearch.addTextChangedListener(watcher)

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = etSearch.text.toString()
                if (query.isNotEmpty()) {
                    searchTracks(query)
                }
                true
            } else false
        }
    }


    private fun clearVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(ID_SEARCH_QUERY, sText)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        sText = savedInstanceState.getString(ID_SEARCH_QUERY).toString()
        etSearch.setText(sText)
    }

    companion object {
        private const val ID_SEARCH_QUERY = "ID_SEARCH_QUERY"
    }


    fun searchTracks(term: String) {
        iTunesService.searchTracks(term)
            .enqueue(object : Callback<TracksResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<TracksResponse>,
                    response: Response<TracksResponse>
                ) {
                    val tracksResponse = response.body()
                    if (tracksResponse != null && tracksResponse.results.isNotEmpty()) {
                        trackList.clear()
                        trackList.addAll(tracksResponse.results)
                        trackAdapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                        nothingPlaceHolder.visibility = View.GONE
                        noConnectionPlaceholder.visibility = View.GONE
                    } else {
                        trackList.clear()
                        trackAdapter.notifyDataSetChanged()
                        recyclerView.visibility = View.GONE
                        nothingPlaceHolder.visibility = View.VISIBLE
                        noConnectionPlaceholder.visibility = View.GONE
                    }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                    trackList.clear()
                    trackAdapter.notifyDataSetChanged()
                    recyclerView.visibility = View.GONE
                    nothingPlaceHolder.visibility = View.GONE
                    noConnectionPlaceholder.visibility = View.VISIBLE
                }
            })
    }
}