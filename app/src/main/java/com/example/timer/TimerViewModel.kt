package com.example.timer

import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Duration
import java.util.concurrent.TimeUnit

private const val TIMER_INTERVAL_MS = 1000L // 1 second
private const val TIME_FORMAT = "%02d:%02d:%02d"

class TimerViewModel : ViewModel() {

    private val _timerState = MutableLiveData(TimerState.NOT_STARTED)
    val timerState: LiveData<TimerState> get() = _timerState

    private val _timeRemaining = MutableLiveData<String>()
    val timeRemaining: LiveData<String> get() = _timeRemaining

    private var countDownTimer: CountDownTimer? = null

    private var initialDurationMillis = 0L
    private var currentDurationMillis = 0L
    private var timeRemainingMillis = 0L

    fun handlePrimaryAction() {
        when (timerState.value) {
            TimerState.NOT_STARTED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
            TimerState.PAUSED -> startTimer()
            TimerState.FINISHED -> restartTimer()
            null -> {}
        }
    }

    fun handleSecondaryAction() {
        cancelTimer()
    }

    fun setDuration(hours: Long, minutes: Long, seconds: Long) {
        // Adding an extra second so that the timer appears to start from input duration.
        currentDurationMillis = getTimeInMillis(hours, minutes, seconds) + TIMER_INTERVAL_MS
        initialDurationMillis = currentDurationMillis
    }

    private fun startTimer() {
        countDownTimer = getCountDownTimer()
        countDownTimer?.start()
        setTimerState(TimerState.RUNNING)
    }

    private fun restartTimer() {
        currentDurationMillis = initialDurationMillis
        startTimer()
    }

    private fun pauseTimer() {
        currentDurationMillis = timeRemainingMillis
        countDownTimer?.cancel()
        setTimerState(TimerState.PAUSED)
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        setTimerState(TimerState.NOT_STARTED)
    }

    private fun setTimerState(state: TimerState) {
        _timerState.value = state
    }

    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(currentDurationMillis, TIMER_INTERVAL_MS) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMillis = millisUntilFinished

                val formattedTime = getFormattedTime(millisUntilFinished)
                _timeRemaining.value = formattedTime
            }

            override fun onFinish() {
                setTimerState(TimerState.FINISHED)
            }
        }
    }

    private fun getTimeInMillis(hours: Long, minutes: Long, seconds: Long) =
        TimeUnit.HOURS.toMillis(hours) +
                TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFormattedTime(milliSeconds: Long): String {
        val duration = Duration.ofMillis(milliSeconds)

        return String.format(
            TIME_FORMAT,
            duration.toHours(),
            duration.toMinutes() % 60,
            duration.seconds % 60
        )
    }
}
