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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Unit tests for the implementation of [TaskDetailViewModel]
 */
@RunWith(AndroidJUnit4::class)
class TaskDetailViewModelTest {

    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        taskDetailViewModel = TaskDetailViewModel(ApplicationProvider.getApplicationContext())
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

}
