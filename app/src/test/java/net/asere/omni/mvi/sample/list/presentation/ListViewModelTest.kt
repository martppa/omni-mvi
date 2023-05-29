package net.asere.omni.mvi.sample.list.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.asere.omni.mvi.evaluate
import net.asere.omni.mvi.sample.list.domain.model.PagedRepos
import net.asere.omni.mvi.sample.list.domain.usecase.GetRepositories
import net.asere.omni.mvi.sample.list.domain.usecase.SearchRepositories
import net.asere.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import net.asere.omni.mvi.testConstructor
import net.asere.omni.mvi.testIntent
import net.asere.omni.mvi.testOn
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ListViewModelTest {

    private val fakePagedRepos = PagedRepos(1, listOf(
        mockk(),
        mockk(),
        mockk()
    ))

    private val getRepositories: GetRepositories = mockk(relaxed = true)
    private val exceptionHandler: ExceptionHandler = mockk(relaxed = true)
    private val searchRepositories: SearchRepositories = mockk(relaxed = true)

    private fun createViewModel() = ListViewModel(
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
        val firstPage = 1
        testConstructor { createViewModel() }.evaluate {
            coVerify { getRepositories(firstPage) }
            Assert.assertEquals(2, emittedStates.size)
            Assert.assertEquals(emittedStates.first().currentPage, firstPage)
        }
    }

    @Test
    fun `On NextPage action called should request next page to repository`() = runTest {
        val nextPage = 2
        createViewModel().testOn(ListAction.NextPage).evaluate {
            coVerify { getRepositories(nextPage) }
            Assert.assertEquals(3, emittedStates.size)
            Assert.assertEquals(emittedStates.first().currentPage, nextPage)
        }
    }

    @Test
    fun `On NextPage intent called should request next page to repository`() = runTest {
        val nextPage = 2
        createViewModel().testIntent { nextPage() }.evaluate {
            coVerify { getRepositories(nextPage) }
            Assert.assertEquals(3, emittedStates.size)
            Assert.assertEquals(emittedStates.first().currentPage, nextPage)
        }
    }

    @Test
    fun `On continues emit intent called should take first 9 states `() = runTest {
        createViewModel().testIntent(takeStates = 9) { continuesEmit() }.evaluate {
            Assert.assertEquals(9, emittedStates.size)
        }
    }

    @Test
    fun `On continues post intent called should take first 15 effects `() = runTest {
        createViewModel().testIntent(takeEffects = 15) { continuesPost() }.evaluate {
            Assert.assertEquals(15, emittedEffects.size)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}