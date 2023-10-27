package edu.uw.ischool.dnh7.tipcalc

import android.app.Activity
import android.content.Context
import android.health.connect.datatypes.units.Percentage
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner: Spinner = findViewById(R.id.spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout.
        val arrayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.tip_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }
        spinner.setSelection(arrayAdapter.getPosition("15%"))


        val amountEditor = findViewById<EditText>(R.id.amount)
        val tipButton = findViewById<Button>(R.id.tip_button)
        val currencyFormatter = CurrencyFormat(amountEditor, tipButton)
        val calc = TipCalc(15.0)
        spinner.onItemSelectedListener = SpinnerActivity(calc)
        amountEditor.addTextChangedListener(
            currencyFormatter
        )

        tipButton.isEnabled = false
        tipButton.setOnClickListener {
            calc.calcTip(amountEditor.text.toString(), this)
        }
    }

   class CurrencyFormat(private val editText: EditText, private val tipButton: Button) : TextWatcher {


       override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
       }

       override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
       }

       override fun afterTextChanged(p0: Editable?) {
           if (p0 != null) {
               if (p0.isNotEmpty()) {
                   tipButton.isEnabled = true
               }
               if (p0.length > 2) {
                   editText.removeTextChangedListener(this)
                   var tmp = p0.toString().replace(".", "")
                   if (tmp.length > 2) {
                       tmp = tmp.slice(0..<tmp.length-2).plus(".".plus(tmp.slice(tmp.length-2..<tmp.length)))
                       editText.setText(tmp)
                       editText.setSelection(tmp.length)
                   }
                   editText.addTextChangedListener(this)
               }
               if (p0.length == 2 && p0.toString().contains(".")) {
                   val tmp = p0.toString().replace(".", "")
                   editText.setText(tmp)
                   editText.setSelection(tmp.length)
               }
           }
       }

   }

    class TipCalc(private var tipPercentage: Double) {

        fun setTipPercentage(percentage: Double) {
            tipPercentage = percentage
        }

        fun calcTip(text: String, context: Context) {
            val money = text.split(".")
            val dollar = money[0].toInt()
            val penny = formatStringToPenny(money[1]).toInt()
            val totalPenny = penny + (dollar * 100)
            // Tipping usually round up, normal Int division round down.
            val tipInPenny = (totalPenny * tipPercentage / 100).roundToInt()
            val tipDollar = tipInPenny / 100
            val tipPenny = tipInPenny - (tipDollar * 100)
            Toast.makeText(context, "$" + tipDollar.toString() + "." + formatIntToPenny(tipPenny), Toast.LENGTH_SHORT).show()
        }

        private fun formatStringToPenny(penny: String) : String {
            if (penny.length == 1) {
                return penny.plus("0")
            }
            if (penny.startsWith("0")) {
                return penny[penny.lastIndex].toString()
            }
            return penny
        }

        private fun formatIntToPenny(penny: Int) : String {
            if (penny < 10) return "0".plus(penny.toString())
            return penny.toString()
        }
    }

    class SpinnerActivity(private val tipCalc: TipCalc) : Activity(), AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            // An item is selected. You can retrieve the selected item using
            Log.d("info", parent.getItemAtPosition(pos).toString())
            val tmp = parent.getItemAtPosition(pos).toString()
            tipCalc.setTipPercentage(tmp.slice(0..<tmp.length-1).toDouble())
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            Log.d("info", "lift nothing")
        }
    }
}