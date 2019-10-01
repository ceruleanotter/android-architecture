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
package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.collection.IsEmptyCollection.empty
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.hamcrest.core.Is.`is`


/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultTasksRepositoryTest {

    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2")
    private val task3 = Task("Title3", "Description3")
    private val newTask = Task("Title new", "Description new")
    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task3).sortedBy { it.id }
    private val newTasks = listOf(task3).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())
        // Get a reference to the class under test
        tasksRepository = DefaultTasksRepository(
            // TODO Dispatchers.Unconfined should be replaced with Dispatchers.Main
            //  this requires understanding more about coroutines + testing
            //  so we will keep this as Unconfined for now.
            tasksRemoteDataSource, tasksLocalDataSource, Dispatchers.Unconfined
        )
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = tasksRepository.getTasks()

        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        val second = tasksRepository.getTasks()

        // Initial and second should match because we didn't force a refresh
        assertThat(second, IsEqual(initial))
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runBlockingTest {
        // When tasks are requested from the tasks repository
        val tasks = tasksRepository.getTasks(true) as Success

        // Then tasks are loaded from the remote data source
        assertThat(tasks.data, IsEqual(remoteTasks))
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = runBlockingTest {
        // Make sure newTask is not in the remote or local datasources
        assertThat(tasksRemoteDataSource.tasks, not(hasItems(newTask)))
        assertThat(tasksLocalDataSource.tasks, not(hasItems(newTask)))

        // When a task is saved to the tasks repository
        tasksRepository.saveTask(newTask)

        // Then the remote and local sources are called
        assertThat(tasksRemoteDataSource.tasks, hasItems(newTask))
        assertThat(tasksLocalDataSource.tasks, hasItems(newTask))
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runBlockingTest {
        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = tasksRepository.getTasks()
        assertThat(cachedTasks, IsEqual(tasks))

        // Now force remote loading
        val refreshedTasks = tasksRepository.getTasks(true) as Success

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks.data, IsEqual(newTasks))
    }

    @Test
    fun getTasks_WithDirtyCache_remoteUnavailable_error() = runBlockingTest {
        // Make remote data source unavailable
        tasksRemoteDataSource.tasks = null

        // Load tasks forcing remote load
        val refreshedTasks = tasksRepository.getTasks(true)

        // Result should be an error
        assertThat(refreshedTasks, instanceOf(Result.Error::class.java))
    }

    @Test
    fun getTasks_WithBothDataSourcesUnavailable_returnsError() = runBlockingTest {
        // When both sources are unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository returns an error
        assertThat(tasksRepository.getTasks(), instanceOf(Result.Error::class.java))
    }


    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Make sure it's active
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isCompleted,`is`(false))

        // Mark is as complete
        tasksRepository.completeTask(newTask.id)

        // Verify it's now completed
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isCompleted,`is`(true))
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)
        tasksRepository.completeTask(newTask.id)

        // Make sure it's completed
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isActive,`is`(false))

        // Mark is as active
        tasksRepository.activateTask(newTask.id)

        // Verify it's now activated
        val result = tasksRepository.getTask(newTask.id) as Success
        assertThat(result.data.isActive,`is`(true))
    }

    @Test
    fun clearCompletedTasks() = runBlockingTest {
        val completedTask = task1.copy().apply { isCompleted = true }
        tasksRemoteDataSource.tasks = mutableListOf(completedTask, task2)
        tasksRepository.clearCompletedTasks()

        val tasks = (tasksRepository.getTasks(true) as? Success)?.data

        assertThat(tasks, hasSize(1))
        assertThat(tasks, hasItems(task2))
        assertThat(tasks, not(hasItems(completedTask)))
    }

    @Test
    fun deleteAllTasks() = runBlockingTest {
        val initialTasks = (tasksRepository.getTasks() as? Success)?.data

        // Delete all tasks
        tasksRepository.deleteAllTasks()

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Success)?.data

        // Verify tasks are empty now
        assertThat(initialTasks, hasSize(greaterThan(0)))
        assertThat(afterDeleteTasks, empty())
    }

    @Test
    fun deleteSingleTask() = runBlockingTest {
        val initialTasks = (tasksRepository.getTasks(true) as? Success)?.data

        // Delete first task
        tasksRepository.deleteTask(task1.id)

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks(true) as? Success)?.data

        // Verify only one task was deleted
        assertThat(afterDeleteTasks?.size, IsEqual(initialTasks!!.size - 1))
        assertThat(afterDeleteTasks, not(hasItems(task1)))
    }
}

