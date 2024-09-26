package com.example.assignment1

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.assignment1.databinding.FragmentSecondBinding
import java.util.ArrayList


const val GROUP_PREFIX = "Group#:"

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val specialOffers = resources.getStringArray(R.array.interest_rates__special_offers)
        val fixedRateMortgages = resources.getStringArray(R.array.interest_rates__fixed_rate_mortgages)
        val variableRateMortgages = resources.getStringArray(R.array.interest_rates__variable_rate_mortgages)

        val interestRates = ArrayList<String>()
        interestRates.add(GROUP_PREFIX + "Special Offers")
        interestRates.addAll(specialOffers.toList())

        interestRates.add(GROUP_PREFIX + "Fixed Rate Mortgages")
        interestRates.addAll(fixedRateMortgages.toList())

        interestRates.add(GROUP_PREFIX + "Variable Rate Mortgages")
        interestRates.addAll(variableRateMortgages.toList())

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, interestRates) {
            override fun isEnabled(position: Int): Boolean {
                val item = interestRates[position]
                // Disable items that contain the GROUP_PREFIX
                return !item.contains(GROUP_PREFIX)
            }

            // Override getView to remove the group prefix from the selected item displayed
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Inflate the default view for selected item
                val textView = super.getView(position, convertView, parent) as TextView

                var item = interestRates[position]

                // Remove the GROUP_PREFIX before displaying the selected item
                if (item.contains(GROUP_PREFIX)) {
                    item = item.replace(GROUP_PREFIX, "")
                }

                // Set the modified text in the TextView for the selected item
                textView.text = item.trim() // Trim to remove any leading/trailing spaces after removing the prefix
                return textView
            }

            // Override getDropDownView to customize the appearance of group headers
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Inflate the default dropdown view
                val textView = super.getDropDownView(position, convertView, parent) as TextView

                var item = interestRates[position]
                if (item.contains(GROUP_PREFIX)) {
                    item = item.replace(GROUP_PREFIX, "")

                    // Set appearance for group headers
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.setTextColor(Color.BLUE)
                } else {
                    textView.setTypeface(null, Typeface.NORMAL)
                    textView.setTextColor(Color.BLACK)
                }

                // Set the modified item text in the TextView
                textView.text = item.trim() // Trim to remove any leading/trailing spaces after removing the prefix

                return textView
            }
        }

        // Set the dropdown view resource and assign the adapter to the spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.interestRateDropdown.adapter = adapter


        // Set the default selection to the first non-group item
        val defaultPosition = interestRates.indexOfFirst { !it.contains(GROUP_PREFIX) }
        if (defaultPosition != -1) {
            binding.interestRateDropdown.setSelection(defaultPosition)
        }

        binding.button.setOnClickListener {
            val googleMapIntent = Uri.parse("google.streetview:cbll=46.434382,10.0113988")
            val intent = Intent(Intent.ACTION_VIEW, googleMapIntent)
            intent.setPackage("com.google.android.apps.maps")

            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}