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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.architecture.blueprints.todoapp.EventObserver
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.tasks.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : Fragment() {

    private val args: TaskDetailFragmentArgs by navArgs()

    private val viewModel by viewModels<TaskDetailViewModel> { getViewModelFactory() }

    private lateinit var swipeRefreshLayout: ScrollChildSwipeRefreshLayout
    private lateinit var noTaskLayout: LinearLayout
    private lateinit var noTaskTextView: TextView

    private lateinit var taskLinearLayout: RelativeLayout

    private lateinit var taskCompleteCheckBox : CheckBox
    private lateinit var taskTitleText : TextView
    private lateinit var taskDescriptionText : TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
        this.setupRefreshLayout(swipeRefreshLayout)
        setupViews()
    }

    private fun setupViews() {
        viewModel.isDataAvailable.observe(this, Observer { dataIsAvailable ->
            if (dataIsAvailable) {
                noTaskLayout.visibility = View.GONE
                taskLinearLayout.visibility = View.VISIBLE
            } else {
                noTaskLayout.visibility = View.VISIBLE
                taskLinearLayout.visibility = View.GONE
            }
        })

        viewModel.dataLoading.observe(this, Observer { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading

            if (isLoading) {
                noTaskTextView.visibility = View.GONE
            } else {
                noTaskTextView.visibility = View.VISIBLE
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.completed.observe(this, Observer { checked ->
            taskCompleteCheckBox.isChecked = checked
        })

        viewModel.task.observe(this, Observer { task ->
            taskTitleText.text = task?.title
            taskDescriptionText.text = task?.description
        })

        taskCompleteCheckBox.setOnClickListener { _ ->
            viewModel.setCompleted(taskCompleteCheckBox.isChecked)
        }
    }

    private fun setupNavigation() {
        viewModel.deleteTaskEvent.observe(this, EventObserver {
            val action = TaskDetailFragmentDirections
                .actionTaskDetailFragmentToTasksFragment(DELETE_RESULT_OK)
            findNavController().navigate(action)
        })
        viewModel.editTaskEvent.observe(this, EventObserver {
            val action = TaskDetailFragmentDirections
                .actionTaskDetailFragmentToAddEditTaskFragment(
                    args.taskId,
                    resources.getString(R.string.edit_task)
                )
            findNavController().navigate(action)
        })
    }

    private fun setupFab() {
        activity?.findViewById<View>(R.id.edit_task_fab)?.setOnClickListener {
            viewModel.editTask()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.taskdetail_frag, container, false)

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout)
        noTaskLayout = root.findViewById(R.id.no_task_linear_layout)
        noTaskTextView = root.findViewById(R.id.no_task_text)

        taskLinearLayout = root.findViewById(R.id.task_layout)
        taskCompleteCheckBox = root.findViewById(R.id.task_detail_complete_checkbox)
        taskTitleText = root.findViewById(R.id.task_detail_title_text)
        taskDescriptionText = root.findViewById(R.id.task_detail_description_text)

        viewModel.start(args.taskId)

        setHasOptionsMenu(true)
        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteTask()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
    }
}
