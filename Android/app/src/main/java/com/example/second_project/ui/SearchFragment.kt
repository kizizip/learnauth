package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.CategoryAdapter
import com.example.second_project.adapter.LectureAdapter
import com.example.second_project.databinding.FragmentSearchBinding
import com.example.second_project.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryList = listOf("전체", "데이터", "법률", "생명과학", "체육", "수학")

        // dp -> px로 변환
        val spacing = dpToPx(8)

        val categoryAdapter = CategoryAdapter(categoryList){ position ->
            val selectedCategory = categoryList[position]
        }
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        val lectureAdapter = LectureAdapter { lectureId, lectureTitle ->
            // NavDirections로 이동 (lectureId, lectureTitle 모두 전달)
            val action = SearchFragmentDirections.actionNavSearchToQuizFragment(
                lectureId = lectureId,
                lectureTitle = lectureTitle
            )
            findNavController().navigate(action)
        }

        binding.lectureList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = lectureAdapter
        }

        //예시 데이터 삽입
        val lectureList = List(10) {it}
        lectureAdapter.submitList(lectureList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // dp 값을 px로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
