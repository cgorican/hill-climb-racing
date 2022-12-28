package si.um.feri.hillclimbracing.services

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import si.um.feri.hillclimbracing.R
import java.time.Duration
import java.time.LocalDateTime

class TimerService: Service() {

    private lateinit var timer: CountDownTimer

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Get the time remaining from the intent
        val startTime = LocalDateTime.now()
        val timeRemaining = intent.getLongExtra(getString(R.string.TIMER_SERVICE_VAR), 0)

        // Initialize the timer
        timer = object: CountDownTimer(timeRemaining, 1) {
            override fun onTick(millisUntilFinished: Long) {
                val duration = Duration.between(startTime, LocalDateTime.now())
                // Update the notification with the new time remaining
                val bIntent = Intent(getString(R.string.TIMER_UPDATE_ACTION))
                bIntent.putExtra(getString(R.string.TIMER_VALUE), duration.toMillis())
                sendBroadcast(bIntent)
            }

            override fun onFinish() {
                // The timer has finished, stop the service
                val bIntent = Intent(getString(R.string.TIMER_UPDATE_ACTION))
                bIntent.putExtra(getString(R.string.TIMER_VALUE), -1)
                sendBroadcast(bIntent)
                stopSelf()
            }
        }

        // Start the timer
        timer.start()

        // Return START_STICKY to keep the service running until it is stopped explicitly
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer when the service is destroyed
        timer.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
