package com.dicoding.habitapp.ui.countdown

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.databinding.ActivityCountDownBinding
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE

class CountDownActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCountDownBinding
    private lateinit var workManager: WorkManager
    private lateinit var oneTimeWorkRequest: OneTimeWorkRequest
    private var startCountDown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountDownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Count Down"
        workManager = WorkManager.getInstance(this)

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit
        Log.d("log habit parcelable", " log : $habit")

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this)[CountDownViewModel::class.java]

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this) {
            binding.tvCountDown.text = it
        }

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
        viewModel.eventCountDownFinish.observe(this) { isFinished ->
            updateButtonState(!isFinished)
            // jika timer selesai, waktu akan kembali ke focus minute semula dan button yg nyala adalah start
            if (isFinished && startCountDown) {
                startOneTimeTask(habit)
            }
        }

        //
        binding.btnStart.setOnClickListener {
            viewModel.startTimer()
            Log.d("button start", "button is started")
            Toast.makeText(applicationContext, "Habit countdown is starting", Toast.LENGTH_SHORT).show()
            startCountDown = true
        }

        binding.btnStop.setOnClickListener {
            viewModel.resetTimer()
            Toast.makeText(applicationContext, "Habit countdown has stopped", Toast.LENGTH_SHORT).show()
            startCountDown = false
            cancelOneTimeTask()
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        binding.btnStart.isEnabled = !isRunning
        binding.btnStop.isEnabled = isRunning
    }

    private fun startOneTimeTask(habit: Habit) {
        val data = Data.Builder()
            .putInt(HABIT_ID, habit.id)
            .putString(HABIT_TITLE, habit.title)
            .build()

        oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(oneTimeWorkRequest)
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(this) { workInfo ->
                val status = workInfo.state.name
                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                    Log.d(TAG, "Notification is queued. Status : $status")
                } else if (workInfo.state == WorkInfo.State.CANCELLED) {
                    Log.d(TAG, "Notification is cancelled")
                }
            }
    }

    private fun cancelOneTimeTask() {
        workManager.cancelWorkById(oneTimeWorkRequest.id)
        Log.d(TAG, "Cancelling WorkManager successful!")
    }
}