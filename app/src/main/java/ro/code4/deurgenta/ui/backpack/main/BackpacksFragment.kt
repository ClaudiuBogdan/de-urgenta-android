package ro.code4.deurgenta.ui.backpack.main

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_backpacks.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.parceler.Parcels
import ro.code4.deurgenta.R
import ro.code4.deurgenta.data.model.Backpack
import ro.code4.deurgenta.helper.setToRotateIndefinitely
import ro.code4.deurgenta.ui.base.ViewModelFragment

class BackpacksFragment : ViewModelFragment<BackpacksViewModel>() {
    override val screenName: Int
        get() = R.string.analytics_title_backpack
    override val layout: Int
        get() = R.layout.fragment_backpacks
    override val viewModel: BackpacksViewModel by sharedViewModel(from = { requireParentFragment() })
    private val backpacksAdapter: BackpacksAdapter by lazy {
        BackpacksAdapter(requireContext()) {
            if (it !== Backpack.EMPTY) {
                findNavController().navigate(
                    R.id.action_backpackMainFragment_to_backpackDetailsFragment,
                    bundleOf(BackpackDetailsFragment.KEY_BACKPACK to Parcels.wrap(it))
                )
            }
        }
    }
    private var loadingAnimator: ObjectAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchBackpacks()

        add_new_backpack.setOnClickListener {
            findNavController().navigate(R.id.action_backpackMainFragment_to_newBackpackDialogFragment)
        }

        with(remainder) {
            val underlineAction = SpannableString(getString(R.string.backpack_btn_reminder))
            underlineAction.setSpan(UnderlineSpan(), 0, underlineAction.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            text = underlineAction
            setOnClickListener { findNavController().navigate(R.id.action_backpackMainFragment_to_homeFragment) }
        }
        with(backpacks_list) {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = backpacksAdapter
        }

        viewModel.uiModel.observe(viewLifecycleOwner) {
            when (it) {
                is Error -> TODO()
                Loading -> setAsLoading(true)
                is Success -> {
                    setAsLoading(false)
                    backpacksAdapter.submitList(it.backpacks)
                }
            }
        }
    }

    private fun setAsLoading(isLoading: Boolean) {
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            loadingAnimator?.cancel()
            loadingAnimator = loadingIndicator.setToRotateIndefinitely()
            loadingAnimator?.start()
        }
        backpacks_list.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = getString(R.string.backpacks_listing_title)
    }
}