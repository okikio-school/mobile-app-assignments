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

        // Get all string-array interest rates and merge them into a larger array list
        val specialOffers = resources.getStringArray(R.array.interest_rates__special_offers)
        val fixedRateMortgages =
            resources.getStringArray(R.array.interest_rates__fixed_rate_mortgages)
        val variableRateMortgages =
            resources.getStringArray(R.array.interest_rates__variable_rate_mortgages)

        // Add prefixes to be able to identify the headers of each interest rate group
        // Create a flat array list for
        val interestRates = ArrayList<String>()
        interestRates.add(GROUP_PREFIX + "Special Offers")
        interestRates.addAll(specialOffers.toList())

        interestRates.add(GROUP_PREFIX + "Fixed Rate Mortgages")
        interestRates.addAll(fixedRateMortgages.toList())

        interestRates.add(GROUP_PREFIX + "Variable Rate Mortgages")
        interestRates.addAll(variableRateMortgages.toList())

        // Array to View Adapter, basically a complex mapper
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            interestRates
        ) {
            // Disable group headers, we use the group prefix to identify the group headers
            override fun isEnabled(index: Int): Boolean {
                // The index of the specific item in question in the dropdown
                val item = interestRates[index]
                // Disable items that contain the GROUP_PREFIX
                return !item.contains(GROUP_PREFIX)
            }

            // Override getView to remove the group prefix from the selected item displayed
            override fun getView(index: Int, view: View?, parent: ViewGroup): View {
                // Inflate the default view for selected item
                val textView = super.getView(index, view, parent) as TextView

                // The index of the specific item in question in the dropdown
                val item = interestRates[index]
                var text = item

                // Remove the GROUP_PREFIX before displaying the selected item
                if (item.contains(GROUP_PREFIX)) {
                    text = item.replace(GROUP_PREFIX, "")
                }

                // Set the modified text in the TextView for the selected item
                textView.text = text.trim() // Trim to remove any leading/trailing spaces after removing the prefix
                return textView
            }

            // Override getDropDownView to customize the appearance of group headers
            override fun getDropDownView(
                index: Int,
                view: View?,
                parent: ViewGroup
            ): View {
                // Inflate the default dropdown view
                val textView = super.getDropDownView(index, view, parent) as TextView

                // The index of the specific item in question in the dropdown
                val item = interestRates[index]
                var text = item

                // If the item contains the group prefix then it's a header,
                // remove the group prefix before displaying the text in the Spinner
                if (item.contains(GROUP_PREFIX)) {
                    text = item.replace(GROUP_PREFIX, "")

                    // Set appearance for group headers
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.setTextColor(Color.BLUE)
                } else {
                    textView.setTypeface(null, Typeface.NORMAL)
                    textView.setTextColor(Color.BLACK)
                }

                // Set the modified item text in the TextView
                textView.text = text.trim() // Trim to remove any leading/trailing spaces after removing the prefix

                return textView
            }
        }

        // Set the dropdown view resource and assign the adapter to the spinner
        // This basically sets the default view resource that should be used for the view dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.interestRateDropdown.adapter = adapter // Use the Adapter to have the Arrays map to View properly

        // Set the default selection to the first non-group item
        val defaultPosition = interestRates.indexOfFirst { !it.contains(GROUP_PREFIX) }
        if (defaultPosition != -1) {
            binding.interestRateDropdown.setSelection(defaultPosition)
        }

        // On clicking the calculate mortgage payment button, calculate the EMI
        binding.calculateMortgagePayment.setOnClickListener {
            // The interest rate Spinner dropdown only gives us a string for the selected item, so
            // we split the the year and interest value by the vertical divider character (`|`)
            // so the string is now an array of 2 items year (with additional text) and
            // interest rate (with additional text).
            // From there we convert the interest rate and year to doubles for calculating the EMI
            val (_, interest) = parseMortgageInterestAndYears(
                binding.interestRateDropdown.selectedItem.toString()
            )

            // The principal amount EditText view is set to only allow numbers,
            // but to avoid errors we fallback to 0
            val principal = binding.mortgagePrincipalAmount.text.toString().toDoubleOrNull() ?: 0.0
            val amortizationPeriod = Integer.parseInt(binding.amortizationPeriod.text.toString())
            val result = calculateEMI(principal, interest, amortizationPeriod)

            println("Principal: $result")

            // Limit the decimal places to a precision of 2 decimal places,
            // making sure to use the default locale
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
        // Split the input by the vertical divider
        val split = input.split("|");
        val arr = split.toTypedArray(); // Convert the list create by `.split` to an array

        // Convert both the interest rate and the year to a double
        val year = convertUnitNumberToDouble(arr[0].trim());
        val interest = convertUnitNumberToDouble(arr[1].trim());

        // Enum meant to represent the year cap and the interest rate
        return MortgageInterest(year, interest)
    }

    /**
     * Function to calculate EMI (Equated Monthly Installment) as precisely as possible
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
        // Debug code
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