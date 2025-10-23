package com.example.playlistmaker.search.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.models.Track
import com.example.playlistmaker.player.ui.AudioPlayerActivity
import com.example.playlistmaker.search.domain.SearchState
import com.example.playlistmaker.search.ui.adapters.TrackAdapter
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModel { parametersOf(requireContext()) }
    private var trackList = ArrayList<Track>()
    private var trackHistoryList = ArrayList<Track>()

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var trackHistoryAdapter: TrackAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        val query = binding.etSearch.text.toString()
        if (query.isNotEmpty()) {
            viewModel.searchTracks(query)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackAdapter = TrackAdapter(trackList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = trackAdapter

        trackHistoryAdapter = TrackAdapter(trackHistoryList)
        binding.searchHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchHistoryRecyclerView.adapter = trackHistoryAdapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }

        trackAdapter.onClickTrack = { track ->
            if (clickDebounce()) {
                viewModel.addTrackToHistory(track)
                goToAudioPlayer(track)
            }
        }

        trackHistoryAdapter.onClickTrack = { track ->
            if (clickDebounce()) {
                viewModel.addTrackToHistory(track)
                goToAudioPlayer(track)
            }
        }

        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.etSearch.text.isNullOrEmpty()) {
                viewModel.showHistory()
            } else {
                binding.searchHistoryLayout.visibility = View.GONE
            }
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.update.setOnClickListener {
            val query = binding.etSearch.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchTracks(query)
            }
        }
        binding.iwClear.setOnClickListener {
            binding.etSearch.setText("")
            hideKeyboard()
            viewModel.clearSearchResults()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.iwClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (!s.isNullOrEmpty()) {
                    binding.searchHistoryLayout.visibility = View.GONE
                    searchDebounce()
                } else {
                    viewModel.showHistory()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchTracks(query)
                }
                true
            } else false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderState(state: SearchState) {
        when (state) {
            is SearchState.Loading -> {
                showLoading()
            }

            is SearchState.Content -> {
                showContent(state.tracks)
            }

            is SearchState.Empty -> {
                showEmpty()
            }

            is SearchState.History -> {
                showHistory(state.tracks)
            }

            is SearchState.Start -> {
                showStartScreen()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderNothing.visibility = View.GONE
        binding.searchHistoryLayout.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showContent(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.placeholderNothing.visibility = View.GONE
        binding.searchHistoryLayout.visibility = View.GONE

        trackList.clear()
        trackList.addAll(tracks)
        trackAdapter.notifyDataSetChanged()
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderNothing.visibility = View.VISIBLE
        binding.searchHistoryLayout.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showHistory(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            binding.searchHistoryLayout.visibility = View.GONE
            return
        }

        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderNothing.visibility = View.GONE
        binding.searchHistoryLayout.visibility = View.VISIBLE

        trackHistoryList.clear()
        trackHistoryList.addAll(tracks)
        trackHistoryAdapter.notifyDataSetChanged()
    }

    private fun showStartScreen() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderNothing.visibility = View.GONE
        binding.searchHistoryLayout.visibility = View.GONE
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

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun goToAudioPlayer(track: Track) {
        val intent = Intent(requireContext(), AudioPlayerActivity::class.java)
        intent.putExtra(TRACK, track)
        startActivity(intent)
    }

    companion object {
        private const val TRACK = "TRACK_DATA"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}

