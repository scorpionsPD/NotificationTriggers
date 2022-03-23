package com.notificationsdk

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.notificationbuilder.NotificationBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            NotificationBuilder.with(this).header {
                headerText = "gaurav"
            }.content {
                text = "testing notification"
                title = "Notification"
                largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_baseline_brightness_auto_24)
            }.show()
        }
    }
}