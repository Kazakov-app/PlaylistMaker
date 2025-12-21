package com.example.playlistmaker.media.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.media.domain.FavoritesState
import com.example.playlistmaker.media.ui.viewmodel.FavoritesViewModel
import com.example.playlistmaker.models.Track
import com.example.playlistmaker.player.ui.AudioPlayerActivity
import com.example.playlistmaker.search.ui.adapters.TrackAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {
    private val viewModel: FavoritesViewModel by viewModel()
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val trackList = ArrayList<Track>()
    private lateinit var trackAdapter: TrackAdapter
    private var isClickAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackAdapter = TrackAdapter(trackList)
        _binding!!.recyclerViewFavourites.layoutManager = LinearLayoutManager(requireContext())
        _binding!!.recyclerViewFavourites.adapter = trackAdapter
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Content -> {
                    _binding!!.placeholderNofavourites.visibility = View.GONE
                    _binding!!.recyclerViewFavourites.visibility = View.VISIBLE
                    trackList.clear()
                    trackList.addAll(state.tracks)
                    trackAdapter.notifyDataSetChanged()
                }

                is FavoritesState.Empty -> {
                    _binding!!.recyclerViewFavourites.visibility = View.GONE
                    _binding!!.placeholderNofavourites.visibility = View.VISIBLE
                }
            }
        }

        trackAdapter.onClickTrack = { track ->
            if (clickDebounce()) {
                goToAudioPlayer(track)
            }
        }
    }

    private fun goToAudioPlayer(track: Track) {
        val intent = Intent(requireContext(), AudioPlayerActivity::class.java)
        intent.putExtra(TRACK, track)
        startActivity(intent)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TRACK = "TRACK_DATA"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        fun newInstance() = FavoritesFragment()
    }

}
