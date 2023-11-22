package net.asere.omni.mvi.sample.list.presentation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.sample.shared.presentation.model.RepoModel

@Parcelize
data class ListState(
    val query: String? = null,
    val currentPage: Int = 1,
    val loading: Boolean = false,
    val items: List<RepoModel> = listOf(),
    val error: String = String.empty()
) : Parcelable

sealed class ListAction {
    object Retry : ListAction()
    object NextPage : ListAction()
    class Query(val value: String) : ListAction()
}

sealed class ListEffect {
    data class ShowMessage(val text: String) : ListEffect()
}