package com.tanasi.streamflix.activities.launcher

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.tanasi.streamflix.R
import com.tanasi.streamflix.databinding.ActivityLauncherBinding
import com.tanasi.streamflix.ui.UpdateDialog
import com.tanasi.streamflix.utils.UserPreferences

class LauncherActivity : FragmentActivity() {

    private var _binding: ActivityLauncherBinding? = null
    private val binding: ActivityLauncherBinding get() = _binding!!

    private val viewModel by viewModels<LauncherViewModel>()

    private lateinit var updateDialog: UpdateDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Base)
        super.onCreate(savedInstanceState)
        _binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserPreferences.setup(this)

        viewModel.state.observe(this) { state ->
            when (state) {
                LauncherViewModel.State.CheckingUpdate -> {}
                is LauncherViewModel.State.SuccessCheckingUpdate -> {
                    val asset = state.release?.assets
                        ?.find { it.contentType == "application/vnd.android.package-archive" }
                    if (asset != null) {
                        updateDialog = UpdateDialog(this).also {
                            it.release = state.release
                            it.setOnUpdateClickListener { _ ->
                                if (!it.isLoading) viewModel.downloadUpdate(this, asset)
                            }
                            it.show()
                        }
                    }
                }

                LauncherViewModel.State.DownloadingUpdate -> updateDialog.isLoading = true
                is LauncherViewModel.State.SuccessDownloadingUpdate -> {
                    viewModel.installUpdate(this, state.apk)
                    updateDialog.hide()
                }

                LauncherViewModel.State.InstallingUpdate -> updateDialog.isLoading = true

                is LauncherViewModel.State.FailedUpdate -> {
                    Toast.makeText(
                        this,
                        state.error.message ?: "",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}