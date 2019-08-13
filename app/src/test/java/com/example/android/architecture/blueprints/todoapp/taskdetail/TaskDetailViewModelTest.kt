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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.*
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeTestRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * Unit tests for the implementation of [TaskDetailViewModel]
 */
@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTestRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val task = Task("Title1", "Description1")

    @Before
    fun setupViewModel() {
        tasksRepository = FakeTestRepository()
        tasksRepository.addTasks(task)

        taskDetailViewModel = TaskDetailViewModel(tasksRepository)
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() {
        taskDetailViewModel.start(task.id)

        // Then verify that the view was notified
        assertThat(taskDetailViewModel.task.getOrAwaitValue()?.title, `is`(task.title))
        assertThat(taskDetailViewModel.task.getOrAwaitValue()?.description, `is`(task.description))
    }

    // TODO might want to change these significantly as well to only check
    // ViewModel
    @Test
    fun completeTask() {
        // Load the ViewModel
        taskDetailViewModel.start(task.id)
        // Start observing to compute transformations
        taskDetailViewModel.task.getOrAwaitValue()

        // Verify that the task was active initially
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(false))

        // When the ViewModel is asked to complete the task
        taskDetailViewModel.setCompleted(true)

        // Then the task is completed and the snackbar shows the correct message
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))
        assertSnackbarMessage(taskDetailViewModel.snackbarText, R.string.task_marked_complete)
    }

    @Test
    fun activateTask() {
        task.isCompleted = true

        // Load the ViewModel
        taskDetailViewModel.start(task.id)

        // Verify that the task was completed initially
        val initialTask = taskDetailViewModel.task.getOrAwaitValue()
        assertThat(initialTask?.isCompleted, `is`(true))

        // When the ViewModel is asked to complete the task
        taskDetailViewModel.setCompleted(false)

        val newTask = taskDetailViewModel.task.getOrAwaitValue()

        // Then the task is not completed and the snackbar shows the correct message
        assertThat(newTask?.isCompleted, `is`(false))
        assertSnackbarMessage(taskDetailViewModel.snackbarText, R.string.task_marked_active)
    }

    @Test
    fun taskDetailViewModel_repositoryError() {
        // Given a repository that returns errors
        tasksRepository.setReturnError(true)

        // Given an initialized ViewModel with an active task
        taskDetailViewModel.start(task.id)

        // Get the computed LiveData value
        assertThat(taskDetailViewModel.isDataAvailable.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        val snackbarText = this.taskDetailViewModel.snackbarText.value

        // Check that the value is null
        assertThat(
            snackbarText, nullValue()
        )
    }

    @Test
    fun clickOnEditTask_SetsEvent() {
        // When opening a new task
        this.taskDetailViewModel.editTask()

        // Then the event is triggered
        val value = this.taskDetailViewModel.editTaskEvent.getOrAwaitValue()
        assertThat(
            value.getContentIfNotHandled(), (not(nullValue()))
        )
    }

    @Test
    fun deleteTask() {
        assertThat(tasksRepository.tasksServiceData.containsValue(task), `is`(true))
        taskDetailViewModel.start(task.id)

        // When the deletion of a task is requested
        taskDetailViewModel.deleteTask()

        assertThat(tasksRepository.tasksServiceData.containsValue(task), `is`(false))
    }

    @Test
    fun loadTask_loading() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Load the task in the viewmodel
        taskDetailViewModel.start(task.id)
        // Start observing to compute transformations
        taskDetailViewModel.task.observeForTesting {
            // Force a refresh to show the loading indicator
            taskDetailViewModel.refresh()

            // Then progress indicator is shown
            assertThat(taskDetailViewModel.dataLoading.getOrAwaitValue(), `is`(true))

            // Execute pending coroutines actions
            mainCoroutineRule.resumeDispatcher()

            // Then progress indicator is hidden
            assertThat(taskDetailViewModel.dataLoading.getOrAwaitValue(), `is`(false))
        }
    }
}
