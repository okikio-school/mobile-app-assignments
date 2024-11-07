package com.example.assignment2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class LocationCardAdapter(private var itemList: MutableList<Map<String, Any>>) : RecyclerView.Adapter<LocationCardAdapter.LocationViewHolder>() {
    // ViewHolder class that holds reference to the item views
    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val address: TextView = itemView.findViewById(R.id.address)
        val longitude: TextView = itemView.findViewById(R.id.longitude)
        val latitude: TextView = itemView.findViewById(R.id.latitude)
        val card: CardView = itemView.findViewById(R.id.location_card)
    }

    // Inflate the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_card_view_design, parent, false)
        return LocationViewHolder(view)
    }

    // Bind data to the item view
    override fun onBindViewHolder(holder: LocationViewHolder, index: Int) {
        println("Binding data for item at index $index: ${itemList[index]}")
        val item = itemList[index]
        holder.address.text = item["address"].toString()
        holder.longitude.text = item["longitude"].toString()
        holder.latitude.text = item["latitude"].toString()

        holder.card.setOnClickListener { v ->
            val intent = Intent(v.context, ViewLocation::class.java)
            val options = Bundle().apply {
                putInt("id", Integer.parseInt(item["id"].toString()))
            }

            // Attach the options to the intent as extras
            intent.putExtras(options)
            v.context.startActivity(intent)
        }

        // Add popup menu to the card
        holder.card.setOnLongClickListener { v ->
            showPopupMenu(v, Integer.parseInt(item["id"].toString()))
            true
        }
    }

    // Method to create and show the popup menu
    private fun showPopupMenu(view: View, id: Int) {
        val popup = PopupMenu(view.context, view)

        // Add menu items programmatically with IDs
        popup.menu.add(0, 1, 0, "Edit")    // Group ID: 0, Item ID: 1, Order: 0, Title: "Edit"
        popup.menu.add(0, 2, 1, "Delete")  // Group ID: 0, Item ID: 2, Order: 1, Title: "Delete"

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            println("Menu item clicked: ${menuItem.itemId}")
            when (menuItem.itemId) {
                1 -> {
                    val intent = Intent(view.context, NewLocationScreen::class.java)
                    val options = Bundle().apply {
                        putInt("id", id)
                    }
                    intent.putExtras(options)
                    view.context.startActivity(intent)

                    Toast.makeText(view.context, "Edit clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                2 -> {
                    val databaseHelper = DatabaseHelper(view.context)
                    val isDeleted = databaseHelper.deleteData(id.toString())

                    val toastMsg = if (isDeleted) "Location deleted" else "Something went Wrong";
                    Toast.makeText(view.context, toastMsg, Toast.LENGTH_SHORT).show();

                    if (isDeleted) {
                        launch(view.context, MainActivity::class.java)
                    }
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // Return the number of items in the data list
    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * Method to switch to a specified activity
     * @param activityClass the activity class to launch
     */
    private fun launch(context: Context, activityClass: Class<*>) {
        // Create an Intent to start the specified activity
        val intent = Intent(context, activityClass)

        // Start the new activity
        context.startActivity(intent)
    }

    // Method to update the dataset
    fun updateData(newItems: MutableList<Map<String, Any>>) {
        itemList = newItems
        notifyDataSetChanged() // This refreshes the RecyclerView with new data
    }
}
