/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.android.architecture.blueprints.todoapp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*

import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)

class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

//    @ExperimentalCoroutinesApi
//    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setupViewModel() {
        tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())
    }

//    @ExperimentalCoroutinesApi
//    @Before
//    fun setupDispatcher() {
//        Dispatchers.setMain(testDispatcher)
//    }
//
//    @ExperimentalCoroutinesApi
//    @After
//    fun tearDownDispatcher() {
//        Dispatchers.resetMain()
//        testDispatcher.cleanupTestCoroutines()
//    }


    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Trigger loading of tasks
        tasksViewModel.loadTasks(true)
        // Observe the items to keep LiveData emitting
        tasksViewModel.items.observeForever { }

        // Then progress indicator is shown
        assertThat(tasksViewModel.dataLoading.awaitNextValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(tasksViewModel.dataLoading.awaitNextValue(), `is`(false))

        // And data correctly loaded
        //assertThat(tasksViewModel.items.awaitNextValue()).hasSize(3)
    }

    // Attempt to execute without rule
//    @ExperimentalCoroutinesApi
//    @Test
//    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() = runBlockingTest {
//
//        // Pause dispatcher so we can verify initial values
//        pauseDispatcher()
//
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
//
//        // Trigger loading of tasks
//        tasksViewModel.loadTasks(true)
//        // Observe the items to keep LiveData emitting
//        tasksViewModel.items.observeForever { }
//
//        // Then progress indicator is shown
//        assertThat(tasksViewModel.dataLoading.awaitNextValue(), `is`(true))
//
//        // Execute pending coroutines actions
//        resumeDispatcher()
//
//        // Then progress indicator is hidden
//        assertThat(tasksViewModel.dataLoading.awaitNextValue(), `is`(false))
//
//    }

    @Test
    fun clickOnFab_showsAddTaskUi() {
        // When adding a new task
        tasksViewModel.addNewTask()

        // Then the event is triggered
        val value = tasksViewModel.newTaskEvent.awaitNextValue()
        assertThat(
            value.getContentIfNotHandled(), (not(nullValue()))
        )
    }

    @Test
    fun clickOnOpenTask_setsEvent() {
        // When opening a new task
        val taskId = "42"
        tasksViewModel.openTask(taskId)

        // Then the event is triggered
        assertLiveDataEventTriggered(tasksViewModel.openTaskEvent, taskId)
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_saved_task_message
        )
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_added_task_message
        )
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(tasksViewModel.snackbarText, R.string.successfully_deleted_task_message)
    }

}
