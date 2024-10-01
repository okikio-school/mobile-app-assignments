package com.example.assignment1

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.assignment1.databinding.FragmentSecondBinding
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.ArrayList
import java.util.Locale
import kotlin.math.pow


const val GROUP_PREFIX = "Group#:"

data class MortgageInterest(val year: Double, val interest: Double)

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

        val specialOffers = resources.getStringArray(R.array.interest_rates__special_offers)
        val fixedRateMortgages =
            resources.getStringArray(R.array.interest_rates__fixed_rate_mortgages)
        val variableRateMortgages =
            resources.getStringArray(R.array.interest_rates__variable_rate_mortgages)

        val interestRates = ArrayList<String>()
        interestRates.add(GROUP_PREFIX + "Special Offers")
        interestRates.addAll(specialOffers.toList())

        interestRates.add(GROUP_PREFIX + "Fixed Rate Mortgages")
        interestRates.addAll(fixedRateMortgages.toList())

        interestRates.add(GROUP_PREFIX + "Variable Rate Mortgages")
        interestRates.addAll(variableRateMortgages.toList())

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            interestRates
        ) {
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
                textView.text =
                    item.trim() // Trim to remove any leading/trailing spaces after removing the prefix
                return textView
            }

            // Override getDropDownView to customize the appearance of group headers
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
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
                textView.text =
                    item.trim() // Trim to remove any leading/trailing spaces after removing the prefix

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

        binding.calculateMortgagePayment.setOnClickListener {
            val (_, interest) = parseMortgageInterestAndYears(
                binding.interestRateDropdown.selectedItem.toString()
            )

            val principal = binding.mortgagePrincipalAmount.text.toString().toDoubleOrNull() ?: 0.0
            val amortizationPeriod = Integer.parseInt(binding.amortizationPeriod.text.toString())
            val result = calculateEMI(principal, interest, amortizationPeriod)

            println("Principal: $result")
            binding.monthlyMortgagePayments.text = String.format(Locale.getDefault(), "%.2f", result)
        }
    }

    fun convertUnitNumberToDouble(input: String): Double {
        // Extract the number from the input string
        val numberPattern = "\\d+(\\.\\d+)?".toRegex();
        val matchResult = numberPattern.find(input)

        // Convert the extracted number to a Double
        return matchResult?.value?.toDouble() ?: 0.0
    }

    fun parseMortgageInterestAndYears(input: String): MortgageInterest {
        val split = input.split("|");
        val arr = split.toTypedArray();

        val year = convertUnitNumberToDouble(arr[0].trim());
        val interest = convertUnitNumberToDouble(arr[1].trim());

        return MortgageInterest(year, interest)
    }

    /**
     * Function to calculate EMI (Equated Monthly Installment)
     *
     * @param principal The loan amount or principal (P)
     * @param annualInterestRate The annual interest rate (in percentage) for the mortgage
     * @param amortizationPeriod The amortization period in years
     * @return The EMI (Equated Monthly Installment) value
     */
    fun calculateEMI(
        principal: Double,
        annualInterestRate: Double,
        amortizationPeriod: Int
    ): Double {

        println("Principal: $principal")
        println("Principal: $annualInterestRate")
        println("Principal: $amortizationPeriod")

        // Convert annual interest rate to a monthly rate and round to 8 decimal places
        val monthlyInterestRate = BigDecimal((annualInterestRate / 100) / 12)
            .setScale(8, RoundingMode.HALF_UP)
            .toDouble()

        // Calculate the number of months for the loan period
        val numberOfPayments = amortizationPeriod * 12

        // Calculate the EMI using the formula
        val emi = if (monthlyInterestRate > 0) {
            // Calculate the compounding factor for the formula (1 + r)^n
            val compoundingFactor = BigDecimal(
                (1 + monthlyInterestRate).pow(numberOfPayments.toDouble())
            )
                .setScale(8, RoundingMode.HALF_UP)
                .toDouble()

            // Formula for EMI: P * r * (1 + r)^n / ((1 + r)^n - 1)
            (principal * monthlyInterestRate * compoundingFactor) / (compoundingFactor - 1)
        } else {
            // If interest rate is 0, EMI is simply principal divided by number of payments
            principal / numberOfPayments
        }

        // Return the EMI rounded to 2 decimal places
        return BigDecimal(emi).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}