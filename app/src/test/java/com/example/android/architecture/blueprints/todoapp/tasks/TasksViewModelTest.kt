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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.assertLiveDataEventTriggered
import com.example.android.architecture.blueprints.todoapp.assertSnackbarMessage
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)

class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())
    }


    @Test
    fun addNewTask_setsNewTaskEvent() {
        // When adding a new task
        tasksViewModel.addNewTask()

        // Then the event is triggered
        val value = tasksViewModel.newTaskEvent.getOrAwaitValue()
        assertThat(
            value.getContentIfNotHandled(), (not(nullValue()))
        )
    }

    @Test
    fun openTask_setsOpenTaskEvent() {
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

    @Test
    fun getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Then the "Add task" action is visible
        assertThat(tasksViewModel.tasksAddViewVisible.getOrAwaitValue(), `is`(true))
    }

}
