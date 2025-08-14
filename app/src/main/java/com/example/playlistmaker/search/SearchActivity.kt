package com.example.playlistmaker.search

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
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
import com.example.playlistmaker.player.AudioPlayerActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
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
    private lateinit var nothingPlaceHolder: LinearLayout
    private var trackListSearchHistory = ArrayList<Track>()
    private lateinit var noConnectionPlaceholder: LinearLayout
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var trackHistoryAdapter: TrackAdapter
    private lateinit var buttonUpdate: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var searchHistoryLayout: ScrollView
    private lateinit var searchHistoryText: TextView
    private lateinit var searchHistoryRecyclerView: RecyclerView
    private lateinit var searchHistoryClearButton: Button
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var searchHistory: SearchHistory
    private lateinit var listenerSharedPrefs: SharedPreferences.OnSharedPreferenceChangeListener

    private val iTunesService = retrofit.create(ITunesApiService::class.java)

    private var sText: String = ""
    private lateinit var etSearch: EditText

    enum class SearchState {
        LOADING,       // Загрузка
        SUCCESS,       // Успешно
        EMPTY_RESULT,  // Пусто
        ERROR          // Ошибка
    }

    private lateinit var progressBar: ProgressBar

    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable {
        val query = etSearch.text.toString()
        if (query.isNotEmpty()) {
            searchTracks(query)
        }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private var isClickAllowed = true
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listenerSharedPrefs)
    }

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

        etSearch = findViewById(R.id.etSearch)
        val iwClear = findViewById<ImageView>(R.id.iwClear)

        progressBar = findViewById(R.id.progress_bar)
        nothingPlaceHolder = findViewById(R.id.placeholder_nothing)
        noConnectionPlaceholder = findViewById(R.id.placeholder_no_connection)
        recyclerView = findViewById(R.id.recycler_view)
        searchHistoryLayout = findViewById(R.id.search_history_layout)
        searchHistoryText = findViewById(R.id.search_history_text)
        searchHistoryRecyclerView = findViewById(R.id.search_history_recycler_view)
        searchHistoryClearButton = findViewById(R.id.clear_history_button)
        sharedPrefs = getSharedPreferences(SEARCH_HISTORY_SHARED_PREFS, MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)

        trackAdapter = TrackAdapter(trackList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter
        trackHistoryAdapter = TrackAdapter(trackListSearchHistory)
        searchHistoryRecyclerView.adapter = trackHistoryAdapter
        searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)


        trackAdapter.onClickTrack = { track: Track ->
            if (clickDebounce()) {
                searchHistory.addTrack(track)
                updateSearchHistory()
                goToInfo(track)
            }
        }

        trackHistoryAdapter.onClickTrack = { track: Track ->
            if (clickDebounce()) {
                searchHistory.addTrack(track)
                updateSearchHistory()
                goToInfo(track)
            }
        }

        listenerSharedPrefs =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                if (key == SEARCH_HISTORY_KEY) {
                    trackListSearchHistory.clear()
                    trackListSearchHistory.addAll(searchHistory.getHistory())
                    trackHistoryAdapter.notifyDataSetChanged()
                }
            }

        sharedPrefs.registerOnSharedPreferenceChangeListener(listenerSharedPrefs)

        etSearch.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && etSearch.text.isNullOrEmpty()
                && searchHistory.getHistory().isNotEmpty()
            ) {
                searchHistoryLayout.isVisible = true
                trackListSearchHistory.addAll(searchHistory.getHistory())
                trackHistoryAdapter.notifyDataSetChanged()
            } else {
                searchHistoryLayout.isVisible = false
            }
        }

        searchHistoryClearButton.setOnClickListener {
            searchHistory.clearHistory()
            updateSearchHistory()
            searchHistoryLayout.isVisible = false
        }

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
            recyclerView.isVisible = false
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iwClear.isVisible = !s.isNullOrEmpty()
                if (etSearch.hasFocus() && s.isNullOrEmpty()
                    && searchHistory.getHistory().isNotEmpty()
                ) {
                    searchHistoryLayout.isVisible = true
                    updateSearchHistory()
                } else {
                    searchHistoryLayout.isVisible = false
                    trackList.clear()
                    trackAdapter.notifyDataSetChanged()
                }
                if (!s.isNullOrEmpty()) {
                    searchDebounce()
                }
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

    fun goToInfo(track: Track) {
        val audioPlayerIntent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
        audioPlayerIntent.putExtra(TRACK, Gson().toJson(track))
        startActivity(audioPlayerIntent)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchHistory() {
        trackListSearchHistory.clear()
        trackListSearchHistory.addAll(searchHistory.getHistory())
        trackHistoryAdapter.notifyDataSetChanged()
    }

    private fun updateUI(state: SearchState) {
        when (state) {
            SearchState.LOADING -> {
                progressBar.visibility = View.VISIBLE
                recyclerView.isVisible = false
                nothingPlaceHolder.isVisible = false
                noConnectionPlaceholder.isVisible = false
            }

            SearchState.SUCCESS -> {
                progressBar.visibility = View.GONE
                recyclerView.isVisible = true
                nothingPlaceHolder.isVisible = false
                noConnectionPlaceholder.isVisible = false
            }

            SearchState.EMPTY_RESULT -> {
                progressBar.visibility = View.GONE
                recyclerView.isVisible = false
                nothingPlaceHolder.isVisible = true
                noConnectionPlaceholder.isVisible = false
            }

            SearchState.ERROR -> {
                progressBar.visibility = View.GONE
                recyclerView.isVisible = false
                nothingPlaceHolder.isVisible = false
                noConnectionPlaceholder.isVisible = true
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, sText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        sText = savedInstanceState.getString(SEARCH_TEXT_KEY).toString()
        etSearch.setText(sText)
    }

    fun searchTracks(term: String) {
        updateUI(SearchState.LOADING)

        iTunesService.searchTracks(term)
            .enqueue(object : Callback<TracksResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<TracksResponse>,
                    response: Response<TracksResponse>
                ) {
                    if (response.isSuccessful) {
                        val tracksResponse = response.body()
                        if (tracksResponse != null && tracksResponse.results.isNotEmpty()) {
                            trackList.clear()
                            trackList.addAll(tracksResponse.results)
                            trackAdapter.notifyDataSetChanged()
                            updateUI(SearchState.SUCCESS)
                        } else {
                            trackList.clear()
                            trackAdapter.notifyDataSetChanged()
                            updateUI(SearchState.EMPTY_RESULT)
                        }
                    } else {
                        trackList.clear()
                        trackAdapter.notifyDataSetChanged()
                        updateUI(SearchState.ERROR)
                    }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                    trackList.clear()
                    trackAdapter.notifyDataSetChanged()
                    updateUI(SearchState.ERROR)
                }
            })
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "TEXT_KEY"
        private const val SEARCH_HISTORY_SHARED_PREFS = "HISTORY_SP"
        private const val SEARCH_HISTORY_KEY = "HISTORY_KEY"
        private const val TRACK = "TRACK_DATA"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}