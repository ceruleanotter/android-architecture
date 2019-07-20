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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout

/**
 * Main UI for the statistics screen.
 */
class StatisticsFragment : Fragment() {


    private val viewModel by viewModels<StatisticsViewModel> { getViewModelFactory() }

    private lateinit var swipeRefreshLayout: ScrollChildSwipeRefreshLayout
    private lateinit var statisticsLayout: LinearLayout
    private lateinit var noTasksTextView: TextView
    private lateinit var statActiveText : TextView
    private lateinit var statCompletedText : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.statistics_frag, container, false)

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout)
        statisticsLayout = root.findViewById(R.id.statistics_layout)
        noTasksTextView = root.findViewById(R.id.no_tasks_text)
        statActiveText = root.findViewById(R.id.stats_active_text)
        statCompletedText = root.findViewById(R.id.stats_completed_text)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViews()
        this.setupRefreshLayout(swipeRefreshLayout)
    }

    private fun setupViews() {
        viewModel.dataLoading.observe(this, Observer { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading

            if (isLoading) {
                statisticsLayout.visibility = View.GONE
            } else {
                statisticsLayout.visibility = View.VISIBLE
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.empty.observe(this, Observer { isEmpty ->
            if (isEmpty) {
                noTasksTextView.visibility = View.VISIBLE
                statActiveText.visibility = View.GONE
                statCompletedText.visibility = View.GONE
            } else {
                noTasksTextView.visibility = View.GONE
                statActiveText.visibility = View.VISIBLE
                statCompletedText.visibility = View.VISIBLE
            }
        })

        viewModel.completedTasksPercent.observe(this, Observer {completedPercent ->
            statCompletedText.text = getString(R.string.statistics_completed_tasks,
                completedPercent)
        })

        viewModel.activeTasksPercent.observe(this, Observer {activePercent ->
            statActiveText.text = getString(R.string.statistics_active_tasks,
                activePercent)
        })
    }
}
