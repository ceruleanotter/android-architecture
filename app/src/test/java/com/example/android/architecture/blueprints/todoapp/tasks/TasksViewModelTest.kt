/*
 * Copyright 2018, The Android Open Source Project
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

import androidx.test.core.app.ApplicationProvider
import com.example.android.architecture.blueprints.todoapp.awaitNextValue
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import org.hamcrest.CoreMatchers.*

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)

class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var taskList: List<Task>
    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2", true)
    private val task3 = Task("Title3", "Description3", true)

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())

        taskList = listOf(
            task1, task2, task3
        )
    }


    @Test
    fun filterTasks_activeTasks() {

        // Arrange
        val tasksResult = Result.Success(taskList)


        // Act
        val filterLiveData = tasksViewModel.filterTasks(tasksResult, TasksFilterType.ACTIVE_TASKS )
        val currentList = filterLiveData.awaitNextValue()
        val isError = tasksViewModel.isDataLoadingError.awaitNextValue()

        // Assert

        assertThat(isError,`is`(not(true)))
        assertThat(currentList, not(hasItems(task2, task3)))
        assertThat(currentList, hasItem(task1))

    }

    @Test
    fun filterTasks_completeTasks() {

        // Arrange
        val tasksResult = Result.Success(taskList)


        // Act
        val filterLiveData = tasksViewModel.filterTasks(tasksResult, TasksFilterType.COMPLETED_TASKS )
        val currentList = filterLiveData.awaitNextValue()
        val isError = tasksViewModel.isDataLoadingError.awaitNextValue()

        // Assert

        assertThat(isError,`is`(not(true)))
        assertThat(currentList, hasItems(task2, task3))
        assertThat(currentList, not(hasItem(task1)))
    }

    @Test
    fun filterTasks_allTasks() {

        // Arrange
        val tasksResult = Result.Success(taskList)


        // Act
        val filterLiveData = tasksViewModel.filterTasks(tasksResult, TasksFilterType.ALL_TASKS )
        val currentList = filterLiveData.awaitNextValue()
        val isError = tasksViewModel.isDataLoadingError.awaitNextValue()

        // Assert
        assertThat(isError,`is`(not(true)))
        assertThat(currentList, hasItems(task2, task3, task1))
    }


    @Test
    fun filterTasks_error() {

        // Arrange
        val tasksResult = Result.Error(Exception())


        // Act
        val filterLiveData = tasksViewModel.filterTasks(tasksResult, TasksFilterType.ALL_TASKS )
        val currentList = filterLiveData.awaitNextValue()
        val isError = tasksViewModel.isDataLoadingError.awaitNextValue()

        // Assert
        assertThat(isError,`is`(true))
//        assertThat(tasksViewModel.snackbarText, `is`())
//        assertThat(currentList, )
    }

}
