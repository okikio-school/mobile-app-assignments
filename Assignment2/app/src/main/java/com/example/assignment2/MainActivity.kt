package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var initialDataList = mutableListOf<Map<String, Any>>()
    private val adapter = LocationCardAdapter(initialDataList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.addLocationFab.setOnClickListener { view ->
            // Call the launch method to switch to SecondActivity
            launch(NewLocationScreen::class.java)
        }

        // Get reference to the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        // Create an instance of the DatabaseHelper class
        val myDB = DatabaseHelper(this@MainActivity)
        val dataList = myDB.listData()

        // Attach the adapter and a layout manager to the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform search when user submits the query
                if (query != null) performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Debounce search while the user types
                if (newText != null) performSearch(newText)
                return true
            }
        })

        updateRecyclerView(dataList)
    }

    override fun onResume() {
        super.onResume()

        val searchView = findViewById<SearchView>(R.id.searchView)
        val query = searchView.query
        performSearch(query.toString())
    }

    private fun performSearch(query: String) {
        val myDB = DatabaseHelper(this@MainActivity)
        val searchResults = (
            if (query.isEmpty()) myDB.listData()
            else myDB.searchData(query)
        );

        updateRecyclerView(searchResults)
    }

    private fun updateRecyclerView(data: MutableList<Map<String, Any>>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = findViewById<TextView>(R.id.empty_view)

        adapter.updateData(data)

        if (adapter.itemCount <= 0) {
            recyclerView.visibility = RecyclerView.GONE
            emptyView.visibility = TextView.VISIBLE
        } else {
            recyclerView.visibility = RecyclerView.VISIBLE
            emptyView.visibility = TextView.GONE
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}