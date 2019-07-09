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

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Details screen.
 */
class TaskDetailViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {


    private val _params = MutableLiveData<Pair<String, Boolean>>()

    private val _task = _params.switchMap { (taskId, forceUpdate) ->
        if (forceUpdate) {
            _dataLoading.value = true
            viewModelScope.launch {
                tasksRepository.refreshTasks()
                _dataLoading.value = false
            }
        }
        // TODO I think all this needs to be is a map as opposed to a switchMap - then computeResult
        // simply returns a Result instead of a LiveData result
        tasksRepository.observeTask(taskId).switchMap { computeResult(it) }

    }
    val task: LiveData<Task> = _task

    // TODO isDataAvailable seems like a mapping of whether or not _task has an Result.Error
    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editTaskCommand = MutableLiveData<Event<Unit>>()
    val editTaskCommand: LiveData<Event<Unit>> = _editTaskCommand

    private val _deleteTaskCommand = MutableLiveData<Event<Unit>>()
    val deleteTaskCommand: LiveData<Event<Unit>> = _deleteTaskCommand

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    // This LiveData depends on another so we can use a transformation.
    // TODO Use LiveData.map extension function
    val completed: LiveData<Boolean> = Transformations.map(_task) { input: Task? ->
        input?.isCompleted ?: false
    }

    fun deleteTask() = viewModelScope.launch {
        _params.value?.first?.let {
            tasksRepository.deleteTask(it)
            _deleteTaskCommand.value = Event(Unit)
        }
    }

    fun editTask() {
        _editTaskCommand.value = Event(Unit)
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val task = _task.value ?: return@launch
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?) {
        // If we're already loading or already loaded, return (might be a config change)
        if (_dataLoading.value == true || taskId == _params.value?.first) {
            return
        }
        if (taskId == null) {
            // TODO this seems like it should cause Tasks to be a Result.Error -- this logic
            // could be moved and handled in the params switchmap
            _isDataAvailable.value = false
            return
        }

        _params.value = Pair(taskId, false)
    }

    private fun computeResult(taskResult: Result<Task>): LiveData<Task> {

        // TODO: This is a good case for liveData builder. Replace when stable.
        val result = MutableLiveData<Task>()

        if (taskResult is Success) {
            result.value = taskResult.data
            _isDataAvailable.value = true
        } else {
            result.value = null
            showSnackbarMessage(R.string.loading_tasks_error)
            _isDataAvailable.value = false
        }

        return result
    }


    // TODO confused about forceUpdate - only seems to be true on explicit refresh from menu
    // in this method, couldn't you just call tasksRepository.refreshTasks() here?
    // tasksRepository.observeTask has a task that will be updated by calling the repository refresh
    // taks, removing, I believe, the need for forceUpdate
    fun refresh() {
        // Recreate the parameters to force a new data load.
        _params.value = _params.value?.copy(second = true)
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
