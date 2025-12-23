package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.example.playlistmaker.media.ui.adapters.PlaylistBottomSheetAdapter
import com.example.playlistmaker.media.ui.fragments.CreatePlaylistFragment
import com.example.playlistmaker.models.Track
import com.example.playlistmaker.player.domain.AudioPlayerState
import com.example.playlistmaker.player.ui.viewmodel.AudioPlayerViewModel
import com.example.playlistmaker.search.ui.utils.ViewUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding
    private val viewModel: AudioPlayerViewModel by viewModel {
        parametersOf(requireNotNull(intent.getParcelableExtra<Track>(TRACK)))
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var overlay: View
    private lateinit var playlistAdapter: PlaylistBottomSheetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(top = statusBars.top, bottom = navigationBars.bottom)
            insets
        }

        setupObservers()
        setupPlaybackUI()

        playlistAdapter = PlaylistBottomSheetAdapter { playlist ->
            viewModel.onPlaylistClicked(playlist)
        }

        binding.playlistsRecycler.layoutManager = LinearLayoutManager(this)
        binding.playlistsRecycler.adapter = playlistAdapter


        viewModel.playlists.observe(this) { playlists ->
            playlistAdapter.submitList(playlists)
        }

        val bottomSheetContainer = binding.playlistsBottomSheet
        overlay = findViewById(R.id.overlay)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                        overlay.alpha = 0f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.addTrack.setOnClickListener {
            overlay.visibility = View.VISIBLE
            overlay.alpha = 0.6f
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            viewModel.loadPlaylists()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            val hasFragments = supportFragmentManager.backStackEntryCount > 0
            findViewById<FrameLayout>(R.id.fragment_container).visibility =
                if (hasFragments) View.VISIBLE else View.GONE
        }
    }

    private fun setupTrackInfo(track: Track) {
        binding.trackName.text = track.trackName
        binding.trackSinger.text = track.artistName
        binding.timeTrackInfo.text = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(track.trackTimeMillis)
        binding.yearTrackInfo.text = track.releaseDate?.substringBefore("-") ?: RES_NO
        binding.genreTrackInfo.text = if (!track.genreName.isNullOrEmpty()) {
            track.genreName
        } else {
            RES_NO
        }
        binding.countryTrackInfo.text = track.country

        if (track.collectionName.isNullOrEmpty()) {
            binding.albumTrack.isVisible = false
            binding.albumTrackInfo.isVisible = false
        } else {
            binding.albumTrack.isVisible = true
            binding.albumTrackInfo.isVisible = true
            binding.albumTrackInfo.text = track.collectionName
        }
        val imageUrl = track.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder_312)
            .transform(
                RoundedCorners
                    (ViewUtils.dpToPx(8f, this))
            )
            .into(binding.placeholderTrack)
    }

    private fun setupObservers() {
        viewModel.getThemeSettings().observe(this) { state ->
            when (state) {
                is AudioPlayerState.Loading -> {
                    binding.timeTrack.text = TIME_PLAY_TRACK
                    binding.playTrack.setImageResource(R.drawable.ic_play)
                }

                is AudioPlayerState.Content -> {
                    val trackFromState = state.track
                    binding.timeTrack.text = state.currentTime
                    binding.playTrack.setImageResource(
                        if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                    setupTrackInfo(trackFromState)
                }

                is AudioPlayerState.Error -> {
                    binding.timeTrack.text = TIME_PLAY_TRACK
                    binding.playTrack.setImageResource(R.drawable.ic_play)
                }
            }
        }

        viewModel.playlists.observe(this) { playlists ->
            playlistAdapter.submitList(playlists)
            binding.playlistsRecycler.isVisible = playlists.isNotEmpty()
        }

        viewModel.isFavorite().observe(this) { isFavorite ->
            binding.favoriteTrack.setImageResource(
                if (isFavorite) R.drawable.ic_dislike else R.drawable.ic_like
            )
        }

        viewModel.addStatus.observe(this) { status ->
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Добавлено")) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun setupPlaybackUI() {
        binding.playTrack.setOnClickListener {
            viewModel.togglePlayback()
        }
        binding.favoriteTrack.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
        binding.backButton.setNavigationOnClickListener { finish() }

        binding.update.setOnClickListener {
            showCreatePlaylistFragment()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    private fun showCreatePlaylistFragment() {
        overlay.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CreatePlaylistFragment())
            .addToBackStack("create_playlist")
            .commit()

        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
    }

    companion object {
        const val TRACK = "TRACK_DATA"
        private const val TIME_PLAY_TRACK = "0:30"
        private const val RES_NO = "N/A"
    }
}
