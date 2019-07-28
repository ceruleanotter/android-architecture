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
package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeTestRepository
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [StatisticsViewModel]
 */
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var statisticsViewModel: StatisticsViewModel

    // Use a fake repository to be injected into the viewmodel
    private val tasksRepository = FakeTestRepository()

    @Before
    fun setupStatisticsViewModel() {
        statisticsViewModel = StatisticsViewModel(tasksRepository)
    }


    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() {
        // Given an initialized StatisticsViewModel with no tasks

        // Then the results are empty
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() {
        // We initialise the tasks to 3, with one active and two completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        val task4 = Task("Title4", "Description4", true)
        tasksRepository.addTasks(task1, task2, task3, task4)

        // Then the results are not empty
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is`(false))
        assertThat(statisticsViewModel.activeTasksPercent.getOrAwaitValue(), `is`(25f))
        assertThat(statisticsViewModel.completedTasksPercent.getOrAwaitValue(), `is`(75f))
    }

}
