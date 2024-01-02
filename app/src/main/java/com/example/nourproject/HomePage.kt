package com.example.nourproject

import LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nourproject.itemssettingup.ItemAdapter_main
import com.example.nourproject.itemssettingup.ItemList_main
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.math.roundToInt

class HomePage : AppCompatActivity() {


    // initializing variables
    lateinit var database: FirebaseDatabase
    lateinit var txtT : TextView
    lateinit var txtG: TextView
    lateinit var txtH: TextView
    lateinit var safeItem : LinearLayout
    private var listView: ListView? = null
    private var itemsAdapters : ItemAdapter_main? = null
    private var arrayList: ArrayList<ItemList_main>? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var locationManager: LocationManager
    private lateinit var locationUtils: LocationUtils
    private  var location_granted = false



    // Request code for location permission
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val CALL_PHONE_PERMISSION_REQUEST_CODE = 123



    fun signOut(view: View) {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        // After signing out, you can navigate to a different activity or update the UI as needed.
        // For example, you can navigate back to the sign-in screen.
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity to prevent the user from returning to the signed-in state.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        txtT = findViewById<TextView>(R.id.txtT) // safety
        txtG = findViewById<TextView>(R.id.txtG) // o2
        txtH = findViewById<TextView>(R.id.txtH) // beat
        safeItem = findViewById(R.id.safe)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationUtils = LocationUtils(this)


        val btn = findViewById<ImageView>(R.id.btn_next)

        btn.setOnClickListener {
            val intent = Intent(this,ChartPage::class.java)
            startActivity(intent)
        }

        Toast.makeText(this,auth.currentUser?.displayName.toString(), Toast.LENGTH_LONG).show()

        var gas : String
        var temp : String
        var hum : String
        var g_value : String
        var t_value : String
        var h_value : String
        var time : String


        var listItem : ArrayList<ItemList_main> = ArrayList()

        var ref_listview = FirebaseDatabase.getInstance().getReference()

        val main_listener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //        declaring the listview
                listView = findViewById(R.id._listview_main)
                arrayList = ArrayList()
                arrayList = listItem
                itemsAdapters = ItemAdapter_main(applicationContext , arrayList!!)
                listView?.adapter = itemsAdapters
                gas = "heart"
                g_value = snapshot.child("beat").value.toString().toDouble().toBigDecimal().setScale(2,RoundingMode.UP).toString() + " b/min"
                temp = "safety"
                t_value = snapshot.child("safety").value.toString().toDouble().toBigDecimal().setScale(2,RoundingMode.UP).toString()
                hum = "o2"
                h_value = snapshot.child("o2").value.toString().toDouble().toBigDecimal().setScale(2,RoundingMode.UP).toString() +" %"
                time = "time recieved : "+ Date().toString().substring(4,20)
                listItem.add(ItemList_main(gas , g_value ,temp, t_value,hum,h_value, time))
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }


        ref_listview.addValueEventListener(main_listener)


//         calling the read function to display data

        ReadData()

        // Check and request location permissions
        checkLocationPermission()
        // location

    }

    private fun ReadData() {
        database = FirebaseDatabase.getInstance()
        val g_reference = database.getReference("beat") // beat
        val t_reference = database.getReference("safety") // blood
        val h_reference = database.getReference("o2") // o2


        // setting up the gaslistener
        val g_listener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val beat_value = dataSnapshot.getValue().toString().toDouble().toBigDecimal().setScale(2,RoundingMode.UP).toString()
//                modify unit
                txtG.text = "$beat_value b/min"
                if(beat_value.toDouble() > 94.00){
                    showNotification("HEART beat alert", "Your heart rate is not safe")
                }
                if(beat_value.toDouble() > 100 || beat_value.toDouble() < 60.00){
                    if(location_granted) getUserLocation()
                    else{
                        checkLocationPermission()
                        getUserLocation()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Toast.makeText(
                    this@HomePage,
                    "couldn't read the  Heart beat status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // add eventlistener to the gas
        g_reference.addValueEventListener(g_listener)

        //setting up blood pressure listenet

        val t_listener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val safety_status = dataSnapshot.getValue().toString().toDouble().toBigDecimal().setScale(2,RoundingMode.UP).toInt()
                if(safety_status == 1){
                    txtT.text = "Safe"
                    safeItem.setBackgroundResource(R.drawable.bg_item_safe)
                }else{
                    txtT.text = "Not Safe"
                    safeItem.setBackgroundResource(R.drawable.bg_item_result)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Toast.makeText(
                    this@HomePage,
                    "couldn't read the safety status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        t_reference.addValueEventListener(t_listener)


//        setting up the o2 listener


        val h_listener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val o2_value = dataSnapshot.getValue().toString().toDouble().toBigDecimal().setScale(2,RoundingMode.UP).toString()
                txtH.text = "$o2_value %"

                if(o2_value.toDouble() < 97){
                    showNotification("O2 alert", "Your O2 rate is not safe")
                }
                if(o2_value.toDouble() < 95){
//                    showNotification("O2 alert", "Your O2 rate is not safe")
                    if(location_granted) getUserLocation()
                    else{
                        checkLocationPermission()
                        getUserLocation()
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Toast.makeText(
                    this@HomePage,
                    "couldn't read the oxygen rate status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        h_reference.addValueEventListener(h_listener)

    }


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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted
            // Call a function to get the user's location here
            // For simplicity, let's call a sample function
//            getUserLocation()
            location_granted = true
            Toast.makeText(this, "User location obtained" , Toast.LENGTH_LONG).show()
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Location permission granted
                    // Call a function to get the user's location here
                    // For simplicity, let's call a sample function
                    getUserLocation()
                } else {
                    // Location permission denied
                    Toast.makeText(
                        this,
                        "Location permission denied. Some features may not work.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            CALL_PHONE_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Phone call permission granted
                    // Call a function to make a phone call
                    makePhoneCall("123456789") // Replace with the actual phone number
                } else {
                    // Phone call permission denied
                    Toast.makeText(
                        this,
                        "Phone call permission denied. Cannot make a phone call.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun getUserLocation() {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Get the user's location
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                5f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        // Now, you can send the location via email
                        sendLocationViaEmail(latitude, longitude)
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    }

                    override fun onProviderEnabled(provider: String) {
                    }

                    override fun onProviderDisabled(provider: String) {
                    }
                }
            )
        } else {
            Toast.makeText(
                this,
                "Location permission denied. Unable to get the user's location.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendLocationViaEmail(latitude: Double, longitude: Double) {
        val useremergecy_mail: DatabaseReference = database.reference.child("users_email").child(auth.currentUser?.uid.toString())
        val user_emer_number: DatabaseReference = database.reference.child("users_emergency").child(auth.currentUser?.uid.toString())
        val user_gurdain_name: DatabaseReference = database.reference.child("users_name").child(auth.currentUser?.uid.toString())
        var mail =""
        var number = ""
        var name = ""
        var patient_name = auth.currentUser?.displayName.toString()
        useremergecy_mail.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mail = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("Firebase", "Error reading user's emergency mail", databaseError.toException())
            }
        })
        user_emer_number.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                number = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("Firebase", "Error reading user emergency number", databaseError.toException())
            }
        })

        user_gurdain_name.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                name = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("Firebase", "Error reading user emergency name", databaseError.toException())
            }
        })
        GlobalScope.launch(Dispatchers.IO) {
            val subject = "User Location"
            val city = locationUtils.getCityFromLocation(latitude, longitude)
            val message = "Dear guardian $name\n The user $patient_name is in health problem,\n Latitude: $latitude\nLongitude: $longitude\n city:$city\n"

            val properties = System.getProperties()
            properties["mail.smtp.host"] = "smtp.gmail.com"
            properties["mail.smtp.port"] = "587"
            properties["mail.smtp.auth"] = "true"
            properties["mail.smtp.starttls.enable"] = "true"

            val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("amra51548@gmail.com", "tmta kgfk hcex gxok")
                }
            })

            try {
                val mimeMessage = MimeMessage(session)
                mimeMessage.setFrom(InternetAddress("amra51548@gmail.com"))
                mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(mail))
                mimeMessage.subject = subject
                mimeMessage.setText(message)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomePage, "Sending email...", Toast.LENGTH_SHORT).show()
                }
                // make the call
                makePhoneCall(number)


                Transport.send(mimeMessage)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomePage, "Location sent via email", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Email", "Error sending email", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomePage, "Error sending location via email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun makePhoneCall(phoneNumber: String) {
        // Check if the CALL_PHONE permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, make the phone call
            val dial = "tel:$phoneNumber"
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PHONE_PERMISSION_REQUEST_CODE
            )
        }
    }




}


