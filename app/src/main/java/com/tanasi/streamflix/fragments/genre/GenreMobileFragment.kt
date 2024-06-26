package com.tanasi.streamflix.fragments.genre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.tanasi.streamflix.R
import com.tanasi.streamflix.adapters.AppAdapter
import com.tanasi.streamflix.databinding.FragmentGenreMobileBinding
import com.tanasi.streamflix.models.Genre
import com.tanasi.streamflix.models.Movie
import com.tanasi.streamflix.models.TvShow
import com.tanasi.streamflix.ui.SpacingItemDecoration
import com.tanasi.streamflix.utils.dp
import com.tanasi.streamflix.utils.viewModelsFactory

class GenreMobileFragment : Fragment() {

    private var _binding: FragmentGenreMobileBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<GenreMobileFragmentArgs>()
    private val viewModel by viewModelsFactory { GenreViewModel(args.id) }

    private val appAdapter = AppAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenreMobileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeGenre()

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                GenreViewModel.State.Loading -> binding.isLoading.apply {
                    root.visibility = View.VISIBLE
                    pbIsLoading.visibility = View.VISIBLE
                    gIsLoadingRetry.visibility = View.GONE
                }
                GenreViewModel.State.LoadingMore -> appAdapter.isLoading = true
                is GenreViewModel.State.SuccessLoading -> {
                    displayGenre(state.genre, state.hasMore)
                    appAdapter.isLoading = false
                    binding.isLoading.root.visibility = View.GONE
                }
                is GenreViewModel.State.FailedLoading -> {
                    Toast.makeText(
                        requireContext(),
                        state.error.message ?: "",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (appAdapter.isLoading) {
                        appAdapter.isLoading = false
                    } else {
                        binding.isLoading.apply {
                            pbIsLoading.visibility = View.GONE
                            gIsLoadingRetry.visibility = View.VISIBLE
                            btnIsLoadingRetry.setOnClickListener {
                                viewModel.getGenre(args.id)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initializeGenre() {
        binding.tvGenreName.text = getString(R.string.genre_header_name, args.name)

        binding.rvGenre.apply {
            adapter = appAdapter
            addItemDecoration(
                SpacingItemDecoration(10.dp(requireContext()))
            )
        }
    }

    private fun displayGenre(genre: Genre, hasMore: Boolean) {
        appAdapter.submitList(genre.shows.onEach {
            when (it) {
                is Movie -> it.itemType = AppAdapter.Type.MOVIE_GRID_MOBILE_ITEM
                is TvShow -> it.itemType = AppAdapter.Type.TV_SHOW_GRID_MOBILE_ITEM
            }
        })

        if (hasMore) {
            appAdapter.setOnLoadMoreListener { viewModel.loadMoreGenreShows() }
        } else {
            appAdapter.setOnLoadMoreListener(null)
        }
    }
}