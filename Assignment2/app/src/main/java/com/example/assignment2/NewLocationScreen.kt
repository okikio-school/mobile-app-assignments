package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewLocationScreen : AppCompatActivity() {
    private var id = -1
    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_location_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Restore saved state (if any)
        if (savedInstanceState != null) {
            updateNoteData(savedInstanceState)
        }

        val backBtn = findViewById<Button>(R.id.back_btn)
        backBtn.setOnClickListener {
            if (id > 0) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                // Call the launch method to switch to SecondActivity
                launch(MainActivity::class.java)
            }
        }

        val addressEdit = findViewById<EditText>(R.id.addressEdit)
        val longitudeEdit = findViewById<EditText>(R.id.longitudeEdit)
        val latitudeEdit = findViewById<EditText>(R.id.latitudeEdit)

        val saveBtn = findViewById<Button>(R.id.save_btn)
        saveBtn.setOnClickListener { view ->
            val myDB = DatabaseHelper(this@NewLocationScreen)
            val isUpdated = if (id > 0) {
                myDB.updateData(
                    id.toString(),
                    addressEdit.text.toString(),
                    longitudeEdit.text.toString(),
                    latitudeEdit.text.toString(),
                )
            } else false
            val isInserted = if (id <= 0) {
                myDB.insertData(
                    addressEdit.text.toString(),
                    longitudeEdit.text.toString(),
                    latitudeEdit.text.toString(),
                )
            } else -1L

            val toastMsg = if (isInserted > -1L || isUpdated) "Location Inserted" else "Something went Wrong"
            Toast.makeText(view.context, toastMsg, Toast.LENGTH_SHORT).show()

            if (isInserted > -1L || isUpdated) {
                if (isUpdated) {
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    launch(MainActivity::class.java)
                }
            }
        }
    }

    // Save the instance state
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val addressEdit = findViewById<EditText>(R.id.addressEdit)
        val longitudeEdit = findViewById<EditText>(R.id.longitudeEdit)
        val latitudeEdit = findViewById<EditText>(R.id.latitudeEdit)

        // Save the current data to the instance state
        outState.putString("address", addressEdit.text.toString())
        outState.putString("longitude", longitudeEdit.text.toString())
        outState.putString("latitude", latitudeEdit.text.toString())
    }

    // Restore the instance state
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore the saved data
        updateNoteData(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        updateNoteData()
    }

    private fun updateNoteData(instanceState: Bundle? = null) {
        extras = intent.extras
        id = extras?.getInt("id") ?: -1

        val titleText = findViewById<TextView>(R.id.titleText)
        val addressEdit = findViewById<EditText>(R.id.addressEdit)
        val longitudeEdit = findViewById<EditText>(R.id.longitudeEdit)
        val latitudeEdit = findViewById<EditText>(R.id.latitudeEdit)

        if (id > 0) {
            val databaseHelper = DatabaseHelper(this)
            val data = databaseHelper.getData(id.toString())

            val address = data[0]["address"].toString()
            val longitude = data[0]["longitude"].toString()
            val latitude = data[0]["latitude"].toString()

            addressEdit.setText(address)
            longitudeEdit.setText(longitude)
            latitudeEdit.setText(latitude)
            titleText.text = "Edit Location"
        } else if (instanceState != null && instanceState.containsKey("address")) {
            addressEdit.setText(instanceState.getString("address"))
            longitudeEdit.setText(instanceState.getString("longitude"))
            latitudeEdit.setText(instanceState.getString("latitude"))
            titleText.text = "Add Location"
        } else {
            addressEdit.setText("")
            longitudeEdit.setText("")
            latitudeEdit.setText("")
            titleText.text = "Add Location"
        }
    }

    /**
     * Method to switch to a specified activity
     * @param activityClass the activity class to launch
     */
    private fun launch(activityClass: Class<*>) {
        // Create an Intent to start the specified activity
        val intent = Intent(this, activityClass)
        // Start the new activity
        startActivity(intent)
    }
}