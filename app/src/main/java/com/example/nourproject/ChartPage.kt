package com.example.nourproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.DropBoxManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class ChartPage : AppCompatActivity() {

    lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var arrayGas = ArrayList<Entry>()
        var arrayTemp = ArrayList<Entry>()

        // charts code
        ChartingData(arrayGas,arrayTemp)


        // navigating to the second chart page

        val nextChart = findViewById<ImageView>(R.id.nextChart)

        nextChart.setOnClickListener {
            startActivity(Intent(this , WhoAreWe::class.java))
        }
    }
    private fun ChartingData(arrayGas:ArrayList<Entry>, arrayTemp:ArrayList<Entry>){
        database = FirebaseDatabase.getInstance()
        val g_reference = database.getReference("beat")
        var counterg = 0



        // setting up the gaslistener
        val g_listener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val g_value = dataSnapshot.getValue().toString()

                //checking  the gas level to send notifications
                if(g_value.toInt() >= 98 || g_value.toInt() < 67){
                    showNotification("Heart rate level Alert", "The heart rate level in the system is not safe")
                }
                // add the reading to the array
                arrayGas.add(Entry(counterg.toFloat(), g_value.toFloat()))
                //increasing counter
                counterg+=1

                // setting linear
                val descriptiong = Description()
                descriptiong.text = "This chart gives the status of the heart rate level in the system"
                val linedataset = LineDataSet(arrayGas, "heart rate")
                linedataset.color = resources.getColor(R.color.bluechart)
                linedataset.circleRadius=0f
                linedataset.setDrawFilled(true)
                linedataset.fillColor = resources.getColor(R.color.shadeg)
                linedataset.fillAlpha = 30

                var data = LineData(linedataset)
                var lineChart = findViewById<LineChart>(R.id.linechart)
                lineChart.description = descriptiong
                lineChart.data= data
                lineChart.setBackgroundColor(resources.getColor(R.color.white))
                lineChart.animateXY(3000,3000)
            }



            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Toast.makeText(
                    this@ChartPage,
                    "couldn't read the rate sensor status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // add eventlistener to the gas
        g_reference.addValueEventListener(g_listener)

    }

//    notification function

    fun showNotification(title: String, message: String) {
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channelID",
                "channelName",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Alerting the user with the system"
            mNotificationManager.createNotificationChannel(channel)
        }

        val mBuilder = NotificationCompat.Builder(applicationContext, "channelID")
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24) // notification icon
            .setContentTitle(title) // title for notification
            .setContentText(message)// message for notification
            .setAutoCancel(true) // clear notification after click

        val intent = Intent(applicationContext, ChartPage::class.java)
        val pi = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        mBuilder.setContentIntent(pi)
        mNotificationManager.notify(0, mBuilder.build())
    }


}