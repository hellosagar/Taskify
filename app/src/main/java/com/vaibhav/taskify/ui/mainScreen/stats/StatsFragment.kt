package com.vaibhav.taskify.ui.mainScreen.stats

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.db.williamchart.ExperimentalFeature
import com.db.williamchart.data.AxisType
import com.vaibhav.taskify.R
import com.vaibhav.taskify.data.models.Bar
import com.vaibhav.taskify.data.models.entity.TaskEntity
import com.vaibhav.taskify.databinding.FragmentStatsBinding
import com.vaibhav.taskify.ui.adapters.TaskAdapter
import com.vaibhav.taskify.util.TaskType
import com.vaibhav.taskify.util.showToast
import com.vaibhav.taskify.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@ExperimentalFeature
@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private val binding by viewBinding(FragmentStatsBinding::bind)
    private val viewModel: StatsViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskAdapter = TaskAdapter {

        }
        binding.allTaskRecycler.apply {
            adapter = taskAdapter
            setHasFixedSize(false)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.barData.collect {
                Timber.d(it.toString())
                configureBarChart(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.donutData.collect {
                Timber.d(it.toString())
                configureDonutChart(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.lastWeekTasks.collect {
                binding.totalTaskCount.text = it.size.toString()
                taskAdapter.submitList(it)
            }
        }

    }


    private fun configureBarChart(data: List<Bar>) {
        val barList = data.map {
            Pair(it.day, it.count.toFloat())
        }
        binding.barChart.apply {
            animation.duration = 1000L
            labelsColor = resources.getColor(R.color.gray3)
            axis = AxisType.X
            labelsFont = ResourcesCompat.getFont(requireContext(), R.font.raleway_medium)
            labelsSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12F, resources.displayMetrics)
            animate(barList)
            onDataPointClickListener = { i: Int, _: Float, _: Float ->
                val message = "${data[i].count} tasks completed on last ${data[i].dayFull}"
                requireContext().showToast(message)
            }
        }
    }

    private fun configureDonutChart(data: Map<TaskType, List<TaskEntity>>) {
        binding.donutChart.apply {
            donutColors = intArrayOf(
                resources.getColor(R.color.home_task_color1),
                resources.getColor(R.color.study_task_color1),
                resources.getColor(R.color.work_task_color1),
                resources.getColor(R.color.gym_task_color1)
            )
            donutTotal = viewModel.lastWeekTasks.value.size.toFloat()
            val dataList = if (donutTotal != 0F) listOf(
                data[TaskType.HOME]?.size?.toFloat() ?: 0F,
                data[TaskType.STUDY]?.size?.toFloat() ?: 0F,
                data[TaskType.WORK]?.size?.toFloat() ?: 0F,
                data[TaskType.GYM]?.size?.toFloat() ?: 0F
            ) else emptyList()
            animation.duration = 1000L
            animate(dataList)
            configureTaskTypeClick(data)
        }
    }

    private fun configureTaskTypeClick(data: Map<TaskType, List<TaskEntity>>) {
        binding.apply {
            homeTv.setOnClickListener {
                showToast(data[TaskType.HOME]?.size ?: 0, TaskType.HOME)
            }
            workTv.setOnClickListener {
                showToast(data[TaskType.WORK]?.size ?: 0, TaskType.WORK)
            }
            studyTv.setOnClickListener {
                showToast(data[TaskType.STUDY]?.size ?: 0, TaskType.STUDY)
            }
            gymTv.setOnClickListener {
                showToast(data[TaskType.GYM]?.size ?: 0, TaskType.GYM)
            }
        }
    }

    private fun showToast(count: Int, type: TaskType) {
        requireContext().showToast("You have completed total $count ${type.name} tasks")
    }
}
