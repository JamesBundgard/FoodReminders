package com.example.foodreminders

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_food.*
import java.util.*


class AddFoodActivity : AppCompatActivity() {

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var datePurchaseView: TextView
    private lateinit var dateExpiryView: TextView

    private val year: Int = calendar.get(Calendar.YEAR)
    private val month: Int = calendar.get(Calendar.MONTH)
    private val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)
        setSupportActionBar(toolbar)

        datePurchaseView = findViewById(R.id.datePurchased)
        dateExpiryView = findViewById(R.id.dateExpiry)

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("description", findViewById<EditText>(R.id.txtDescription).text.toString())
            intent.putExtra("quantity", findViewById<EditText>(R.id.numQuantity).text.toString())
            intent.putExtra("measurement", findViewById<EditText>(R.id.txtMeasurement).text.toString())
            intent.putExtra("expiry", findViewById<TextView>(R.id.dateExpiry).text.toString())
            intent.putExtra("purchase", findViewById<TextView>(R.id.dateExpiry).text.toString())
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun setExpiryDate(view: View) {
        showDialog(1)
        Toast.makeText(
            applicationContext, "ca",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setPurchaseDate(view: View) {
        showDialog(2)
        Toast.makeText(
            applicationContext, "ca",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateDialog(id: Int): Dialog? {
        return when (id) {
            1 -> DatePickerDialog(
                this,
                expiryDateListener, year, month, day
            )
            2 -> DatePickerDialog(
                this,
                purchasedDateListener, year, month, day
            )
            else -> null
        }
    }

    private val expiryDateListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 ->
            showExpiryDate(arg1, arg2 + 1, arg3)
        }

    private val purchasedDateListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 ->
            showPurchasedDate(arg1, arg2 + 1, arg3)
        }

    private fun showExpiryDate(year: Int, month: Int, day: Int) {
        dateExpiryView.text = StringBuilder().append(day).append("/")
            .append(month).append("/").append(year)
    }

    private fun showPurchasedDate(year: Int, month: Int, day: Int) {
        datePurchaseView.text = StringBuilder().append(day).append("/")
            .append(month).append("/").append(year)
    }

}
