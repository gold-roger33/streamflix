package com.tanasi.streamflix.fragments.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.tanasi.streamflix.adapters.AppAdapter
import com.tanasi.streamflix.database.AppDatabase
import com.tanasi.streamflix.databinding.FragmentMovieTvBinding
import com.tanasi.streamflix.models.Movie
import com.tanasi.streamflix.utils.viewModelsFactory

class MovieTvFragment : Fragment() {

    private var _binding: FragmentMovieTvBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MovieTvFragmentArgs>()
    private val viewModel by viewModelsFactory { MovieViewModel(args.id) }

    private lateinit var database: AppDatabase

    private val appAdapter = AppAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getInstance(requireContext())

        initializeMovie()

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                MovieViewModel.State.Loading -> binding.isLoading.apply {
                    root.visibility = View.VISIBLE
                    pbIsLoading.visibility = View.VISIBLE
                    gIsLoadingRetry.visibility = View.GONE
                }
                is MovieViewModel.State.SuccessLoading -> {
                    displayMovie(state.movie)
                    binding.isLoading.root.visibility = View.GONE
                }
                is MovieViewModel.State.FailedLoading -> {
                    Toast.makeText(
                        requireContext(),
                        state.error.message ?: "",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.isLoading.apply {
                        pbIsLoading.visibility = View.GONE
                        gIsLoadingRetry.visibility = View.VISIBLE
                        btnIsLoadingRetry.setOnClickListener {
                            viewModel.getMovie(args.id)
                        }
                        btnIsLoadingRetry.requestFocus()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appAdapter.onSaveInstanceState(binding.vgvMovie)
        _binding = null
    }


    private fun initializeMovie() {
        binding.vgvMovie.apply {
            adapter = appAdapter
            setItemSpacing(80)
        }
    }

    private fun displayMovie(movie: Movie) {
        database.movieDao().getById(movie.id)?.let { movieDb ->
            movie.merge(movieDb)
        }
        database.movieDao().insert(movie)

        Glide.with(requireContext())
            .load(movie.banner)
            .into(binding.ivMovieBanner)

        appAdapter.submitList(listOfNotNull(
            movie.apply { itemType = AppAdapter.Type.MOVIE_TV },

            movie.takeIf { it.cast.isNotEmpty() }
                ?.copy()
                ?.apply { itemType = AppAdapter.Type.MOVIE_CASTS_TV },

            movie.takeIf { it.recommendations.isNotEmpty() }
                ?.copy()
                ?.apply { itemType = AppAdapter.Type.MOVIE_RECOMMENDATIONS_TV },
        ))
    }
}