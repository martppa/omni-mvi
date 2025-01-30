package net.asere.omni.mvi.sample.list.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.asere.omni.mvi.effect
import net.asere.omni.mvi.evaluate
import net.asere.omni.mvi.exactly
import net.asere.omni.mvi.sample.list.domain.model.PagedRepos
import net.asere.omni.mvi.sample.list.domain.GetRepositories
import net.asere.omni.mvi.sample.list.domain.SearchRepositories
import net.asere.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.sample.shared.domain.model.Repo
import net.asere.omni.mvi.sample.shared.presentation.model.asPresentation
import net.asere.omni.mvi.state
import net.asere.omni.mvi.take
import net.asere.omni.mvi.testConstructor
import net.asere.omni.mvi.testIntent
import net.asere.omni.mvi.testOn
import net.asere.omni.mvi.times
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    private val repo1 = Repo(
        id = 1L,
        name = "repo1",
        description = "desc1",
        owner = "owner1",
        ownerAvatar = "avatar1",
        fork = false,
    )
    private val repo2 = repo1.copy(id = 2L)
    private val repo3 = repo1.copy(id = 3L)

    private val fakePagedRepos = PagedRepos(1, listOf(
        repo1,
        repo2,
        repo3
    ))

    private val getRepositories: GetRepositories = mockk(relaxed = true)
    private val exceptionHandler: ExceptionHandler = mockk(relaxed = true)
    private val searchRepositories: SearchRepositories = mockk(relaxed = true)

    private fun createViewModel() = ListViewModel(
        savedStateHandle = SavedStateHandle(),
        getRepositories = getRepositories,
        searchRepositories = searchRepositories,
        exceptionHandler = exceptionHandler
    )

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        coEvery { getRepositories(any()) } returns fakePagedRepos
    }

    @Test
    fun `On creation request first page to repository and`() = runTest {
        testConstructor { createViewModel() }.evaluate(relaxed = true) {
            coVerify { getRepositories(1) }
            Assert.assertEquals(2, emittedStates.size)
            nextState { previous, current ->
                Assert.assertEquals(current, previous.copy(
                    currentPage = 1,
                    loading = true
                ))
            }
            nextState { previous, current ->
                Assert.assertEquals(current, previous.copy(
                    loading = false,
                    currentPage = fakePagedRepos.currentPage,
                    items = fakePagedRepos.items.map { it.asPresentation() }
                ))
            }
            nextEffect {
                Assert.assertEquals(it, ListEffect.ShowMessage("Fetched"))
            }
        }
    }

    @Test
    fun `On NextPage action called should request next page to repository`() = runTest {
        val nextPage = 2
        createViewModel().testOn(ListAction.NextPage).evaluate(relaxed = true) {
            coVerify { getRepositories(nextPage) }
            Assert.assertEquals(3, emittedStates.size)
            Assert.assertEquals(emittedStates.first().currentPage, nextPage)
        }
    }

    @Test
    fun `On NextPage intent called should request next page to repository`() = runTest {
        createViewModel().testIntent(withState = ListState()) { nextPage() }.evaluate {
            expectState { copy(currentPage = 2) }
            expectState { copy(loading = true, error = String.empty()) }
            expectEffect(ListEffect.ShowMessage("Fetched"))
            expectState {
                copy(
                    loading = false,
                    currentPage = fakePagedRepos.currentPage,
                    items = fakePagedRepos.items.map { it.asPresentation() })
            }
        }
    }

    @Test
    fun `On continues emit intent called should take first 9 states`() = runTest {
        createViewModel().testIntent(
            withState = ListState(currentPage = 10),
            take = 9 times state
        ) { continuesEmit() }.evaluate(relaxed = true) {
            Assert.assertEquals(9, emittedStates.size)
        }
    }

    @Test
    fun `On continues post intent called should take first 15 effects `() = runTest {
        createViewModel().testIntent(take exactly 15 times effect) {
            continuesPost()
        }.evaluate(relaxed = true) {
            Assert.assertEquals(15, emittedEffects.size)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}