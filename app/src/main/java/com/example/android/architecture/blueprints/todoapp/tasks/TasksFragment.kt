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

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.todoapp.EventObserver
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.setupSnackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

/**
 * Display a grid of [Task]s. User can choose to view all, active or completed tasks.
 */
class TasksFragment : Fragment() {

    private val viewModel by viewModels<TasksViewModel> { getViewModelFactory() }

    private val args: TasksFragmentArgs by navArgs()

    private lateinit var listAdapter: TasksAdapter

    private lateinit var swipeRefreshLayout: ScrollChildSwipeRefreshLayout
    private lateinit var tasksLinearLayout: LinearLayout
    private lateinit var filteringText: TextView
    private lateinit var taskListRecyclerView: RecyclerView

    private lateinit var noTasksLayout: LinearLayout
    private lateinit var noTasksIcon: ImageView
    private lateinit var noTasksTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.tasks_frag, container, false)

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout)
        tasksLinearLayout = root.findViewById(R.id.tasks_linear_layout)
        filteringText = root.findViewById(R.id.filtering_text)
        taskListRecyclerView = root.findViewById(R.id.tasks_list)
        noTasksLayout = root.findViewById(R.id.no_tasks_layout)
        noTasksIcon = root.findViewById(R.id.no_tasks_icon)
        noTasksTextView = root.findViewById(R.id.no_tasks_text)

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_clear -> {
                viewModel.clearCompletedTasks()
                true
            }
            R.id.menu_filter -> {
                showFilteringPopUpMenu()
                true
            }
            R.id.menu_refresh -> {
                viewModel.loadTasks(true)
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set the lifecycle owner to the lifecycle of the view
        setupSnackbar()
        setupListAdapter()
        setupRefreshLayout(swipeRefreshLayout, taskListRecyclerView)
        setupNavigation()
        setupFab()
        setupViews()
    }

    private fun setupViews() {
        viewModel.empty.observe(this, Observer { isEmpty ->
            if (isEmpty) {
                tasksLinearLayout.visibility = View.GONE
                noTasksLayout.visibility = View.VISIBLE
            } else {
                tasksLinearLayout.visibility = View.VISIBLE
                noTasksLayout.visibility = View.GONE
            }
        })

        viewModel.currentFilteringLabel.observe(this, Observer { filterLabelResource ->
            filteringText.text = getString(filterLabelResource)
        })

        viewModel.items.observe(this, Observer { items ->
            items?.let {
                (taskListRecyclerView.adapter as TasksAdapter).submitList(items)
            }
        })

        viewModel.noTaskIconRes.observe(this, Observer {icon ->
            noTasksIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), icon))
        })

        viewModel.noTasksLabel.observe(this, Observer {label ->
            noTasksTextView.text = getString(label)
        })

        viewModel.dataLoading.observe(this, Observer {isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        })

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

    }

    private fun setupNavigation() {
        viewModel.openTaskEvent.observe(this, EventObserver {
            openTaskDetails(it)
        })
        viewModel.newTaskEvent.observe(this, EventObserver {
            navigateToAddNewTask()
        })
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_tasks, menu)

            setOnMenuItemClickListener {
                viewModel.setFiltering(
                    when (it.itemId) {
                        R.id.active -> TasksFilterType.ACTIVE_TASKS
                        R.id.completed -> TasksFilterType.COMPLETED_TASKS
                        else -> TasksFilterType.ALL_TASKS
                    }
                )
                true
            }
            show()
        }
    }

    private fun setupFab() {
        activity?.findViewById<FloatingActionButton>(R.id.add_task_fab)?.let {
            it.setOnClickListener {
                navigateToAddNewTask()
            }
        }
    }

    private fun navigateToAddNewTask() {
        val action = TasksFragmentDirections
            .actionTasksFragmentToAddEditTaskFragment(
                null,
                resources.getString(R.string.add_task)
            )
        findNavController().navigate(action)
    }

    private fun openTaskDetails(taskId: String) {
        val action = TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment(taskId)
        findNavController().navigate(action)
    }

    private fun setupListAdapter() {
        listAdapter = TasksAdapter(viewModel)
        taskListRecyclerView.adapter = listAdapter
    }
}
