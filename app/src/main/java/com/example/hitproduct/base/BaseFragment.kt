package com.example.hitproduct.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    protected var _binding: VB? = null
    protected val binding: VB
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateLayout(inflater, container)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize view elements (e.g., RecyclerView, TextView setup)
        initView()
        // 2. Setup listeners for UI components (e.g., click, text change)
        initListener()
        // 3. Load or prepare data sources (e.g., fetch from ViewModel or arguments)
        initData()
        // 4. Observe LiveData/Flows or handle events
        handleEvent()
        // 5. Bind loaded data to UI components
        bindData()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun initView()

    /**
     * Set up listeners for UI elements (e.g., click listeners)
     * Called second in onViewCreated
     */
    protected abstract fun initListener()

    /**
     * Load or initialize data sources (e.g., fetch from ViewModel or arguments)
     * Called third in onViewCreated
     */
    protected abstract fun initData()

    /**
     * Handle events or observe LiveData/Flows
     * Called fourth in onViewCreated
     */
    protected abstract fun handleEvent()

    /**
     * Bind loaded data to UI components
     * Called last in onViewCreated
     */
    protected abstract fun bindData()

    abstract fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): VB

}