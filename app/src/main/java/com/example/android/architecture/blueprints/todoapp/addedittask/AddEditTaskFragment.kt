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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.architecture.blueprints.todoapp.EventObserver
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.tasks.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.setupSnackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : Fragment() {

    private val args: AddEditTaskFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditTaskViewModel> { getViewModelFactory() }

    private lateinit var swipeRefreshLayout: ScrollChildSwipeRefreshLayout
    private lateinit var addTaskLinearLayout: LinearLayout
    private lateinit var addTaskTitleEditText: EditText
    private lateinit var addTaskDescriptionEditText: EditText
    private lateinit var saveTaskFab: FloatingActionButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.addtask_frag, container, false)

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout)
        addTaskLinearLayout = root.findViewById(R.id.add_task_linear_layout)
        addTaskTitleEditText = root.findViewById(R.id.add_task_title_edit_text)
        addTaskDescriptionEditText = root.findViewById(R.id.add_task_description_edit_text)
        saveTaskFab = root.findViewById(R.id.save_task_fab)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        this.setupRefreshLayout(swipeRefreshLayout)
        setupViews()
        viewModel.start(args.taskId)
    }

    private fun setupViews() {
        viewModel.dataLoading.observe(this, Observer { isLoading ->
            swipeRefreshLayout.isEnabled = isLoading
            swipeRefreshLayout.isRefreshing = isLoading

            if (isLoading) {
                addTaskLinearLayout.visibility = View.GONE
            } else {
                addTaskLinearLayout.visibility = View.VISIBLE
            }
        })

        viewModel.title.observe(this, Observer { title ->
            addTaskTitleEditText.setText(title)
        })

        viewModel.description.observe(this, Observer { description ->
            addTaskDescriptionEditText.setText(description)
        })

        saveTaskFab.setOnClickListener {
            viewModel.saveTask()
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.taskUpdatedEvent.observe(this, EventObserver {
            val action = AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }
}
