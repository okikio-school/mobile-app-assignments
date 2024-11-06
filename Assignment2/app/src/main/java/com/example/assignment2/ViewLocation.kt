package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment2.databinding.ActivityViewLocationBinding

class  ViewLocation : AppCompatActivity() {

    private lateinit var binding: ActivityViewLocationBinding

    private var id: Int = -1
    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        // Enable the back button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle back button press
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set fab click listener
        binding.editLocationFab.setOnClickListener { view ->
            val intent = Intent(view.context, NewLocationScreen::class.java)
            val options = Bundle().apply {
                putInt("id", id)
            }

            intent.putExtras(options)
            startActivity(intent)
        }

        binding.deleteLocationFab.setOnClickListener { view ->
            val databaseHelper = DatabaseHelper(this)
            val isDeleted = databaseHelper.deleteData(id.toString())

            val toastMsg = if (isDeleted) "Location deleted" else "Something went Wrong";
            Toast.makeText(view.context, toastMsg, Toast.LENGTH_SHORT).show();

            if (isDeleted) {
                launch(MainActivity::class.java)
                id = -1
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateNoteData()
    }

    private fun updateNoteData() {
        // Retrieve the bundle from the intent's extras
        extras = intent.extras
        id = extras?.getInt("id") ?: -1

        val databaseHelper = DatabaseHelper(this)
        val data = if (id > 0) databaseHelper.getData(id.toString()) else null;

        // Use the values from the bundle
        binding.toolbar.title = (extras?.getString("address") ?: data?.get(0)?.get("address") ?: "Empty Address").toString()

        binding.locationLongitude.text = (extras?.getString("longitude") ?: data?.get(0)?.get("longitude") ?: "Empty Longitude").toString()
        binding.locationLatitude.text = (extras?.getString("latitude") ?: data?.get(0)?.get("latitude") ?: "Empty Latitude").toString()

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