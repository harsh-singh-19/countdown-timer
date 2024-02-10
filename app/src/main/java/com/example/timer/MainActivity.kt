package com.example.timer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.timer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: TimerViewModel by viewModels()

    private val inputTextWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.primaryTimerButton.isEnabled = isValidInput()
            }

            override fun afterTextChanged(p0: Editable?) {
                // do nothing
            }
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        initViews()
        initObservers()
    }

    private fun isValidInput(): Boolean {
        val hoursInput = binding.hoursInput.text.toString()
        val minutesInput = binding.minutesInput.text.toString()
        val secondsInput = binding.secondsInput.text.toString()

        val hours = if (hoursInput.isNotEmpty()) hoursInput.toLong() else 0L
        val minutes = if (minutesInput.isNotEmpty()) minutesInput.toLong() else 0L
        val seconds = if (secondsInput.isNotEmpty()) secondsInput.toLong() else 0L

        val isValid = hours > 0 || minutes > 0 || seconds > 0

        if (isValid) {
            viewModel.setDuration(hours, minutes, seconds)
        }

        return isValid
    }

    private fun initViews() {
        binding.primaryTimerButton.setOnClickListener {
            viewModel.handlePrimaryAction()
        }

        binding.secondaryTimerButton.setOnClickListener {
            viewModel.handleSecondaryAction()
        }
    }

    private fun initObservers() {
        viewModel.timeRemaining.observe(this) { binding.timerText.text = it }

        viewModel.timerState.observe(this) { handleTimerState(it) }
    }

    private fun handleTimerState(timerState: TimerState) {
        when (timerState) {
            TimerState.NOT_STARTED -> {
                updateTimerTextVisibility(View.GONE)
                updateTimerInputs(shouldShowTimerInput = true)

                updatePrimaryButton(
                    label = getString(R.string.timer_btn_label_start),
                    isEnabled = isValidInput()
                )
                updateSecondaryButton(visibility = View.GONE)
            }

            TimerState.RUNNING -> {
                updateTimerTextVisibility()
                updateTimerInputs()

                updatePrimaryButton(label = getString(R.string.timer_btn_label_pause))
                updateSecondaryButton()
            }

            TimerState.PAUSED -> {
                updateTimerTextVisibility()
                updateTimerInputs()

                updatePrimaryButton(label = getString(R.string.timer_btn_label_resume))
                updateSecondaryButton()
            }

            TimerState.FINISHED -> {
                updateTimerTextVisibility()
                updateTimerInputs()

                updatePrimaryButton(label = getString(R.string.timer_btn_label_restart))
                updateSecondaryButton(label = getString(R.string.timer_btn_label_dismiss))
            }
        }
    }

    private fun updateTimerTextVisibility(visibility: Int = View.VISIBLE) {
        binding.timerText.visibility = visibility
    }

    private fun updateTimerInputs(shouldShowTimerInput: Boolean = false) {
        enableTimerInputEditText(shouldShowTimerInput)
        binding.timerInputContainer.visibility = if (shouldShowTimerInput) {
            View.VISIBLE
        } else View.GONE
    }

    private fun enableTimerInputEditText(isEnabled: Boolean) {
        if (isEnabled) {
            binding.apply {
                hoursInput.addTextChangedListener(inputTextWatcher)
                minutesInput.addTextChangedListener(inputTextWatcher)
                secondsInput.addTextChangedListener(inputTextWatcher)
            }
        }

        binding.apply {
            hoursInput.isEnabled = isEnabled
            minutesInput.isEnabled = isEnabled
            secondsInput.isEnabled = isEnabled
        }
    }

    private fun updatePrimaryButton(label: String, isEnabled: Boolean = true) {
        binding.primaryTimerButton.apply {
            this.isEnabled = isEnabled
            this.text = label
        }
    }

    private fun updateSecondaryButton(
        label: String = getString(R.string.timer_btn_label_cancel),
        visibility: Int = View.VISIBLE
    ) {
        binding.secondaryTimerButton.apply {
            this.visibility = visibility
            this.text = label
        }
    }
}
