package com.example.foodreminders

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDate
import java.util.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Button
import java.io.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var mFoodItems = ArrayList<FoodItem>()
    private val filename = "FoodItems.txt"
    private val delimiter = "~"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mFoodItems = getDataFromStorage()
        generateFoodList()

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            // Handler code here.
            val intent = Intent(this, AddFoodActivity::class.java)
            startActivity(intent)
        }
    }

    fun saveToFile(items: ArrayList<FoodItem>){
        var fileContents = ""
        for (item in items){
            fileContents += item.toString(delimiter)
            fileContents += "\n"
        }
        println("To file: "+fileContents)
        val x = openFileOutput(filename, Context.MODE_PRIVATE)
        x.write(fileContents.toByteArray())
        x.close()
    }

    private fun getDataFromStorage(): ArrayList<FoodItem> {
        val items = ArrayList<FoodItem>()
        try {
            val fileInputStream: FileInputStream? = openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); println("here:"+text); text }() != null) {
                stringBuilder.append(text).append("\n")
            }
            for (item in stringBuilder.toString().split("\n")){
                if (item.split(delimiter).size == 5) items.add(FoodItem.fromString(item, delimiter))
                else println(item)
            }
            fileInputStream?.close()
            inputStreamReader.close()
            bufferedReader.close()
        } catch (e: IOException) {
            println("at get "+e.message)
            saveToFile(mFoodItems)
        }
        return items
    }

    fun getDateFrom(time: String):Date{
        val day = Integer.parseInt(time.split("/")[0])
        val month = Integer.parseInt(time.split("/")[1])-1
        val year = Integer.parseInt(time.split("/")[2])

        val c = Calendar.getInstance()
        c.set(year, month, day)
        return c.time
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateFoodList() {
        try {
            val description = intent.getStringExtra("description")
            val quantity = intent.getStringExtra("quantity").toInt()
            val measurement = intent.getStringExtra("measurement")

            val expiry = getDateFrom(intent.getStringExtra("expiry"))
            val purchased = getDateFrom(intent.getStringExtra("purchase"))

            mFoodItems.add(FoodItem(quantity, measurement, expiry, description, purchased))
            saveToFile(mFoodItems)

        } catch (e: Exception) {
            println(e.message)
        }

        val list: LinearLayout = findViewById(R.id.food_list)
        list.removeAllViews()
        for (item in mFoodItems){
            val foodView: LinearLayout = createFoodView(this, item)
            foodView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            list.addView(foodView)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createFoodView(context: Context, item: FoodItem): LinearLayout {
        val block = LinearLayout(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 10, 40, 10)

        val descView = TextView(this)
        descView.layoutParams = params
        descView.text = item.Description
        block.addView(descView)

        val quantView = TextView(this)
        quantView.layoutParams = params
        quantView.text = """${item.Quantity} ${item.Measurement}"""
        block.addView(quantView)

        val expiryView = TextView(this)
        expiryView.layoutParams = params
        println(item.ExpiryDate.toString())
        expiryView.text = android.text.format.DateFormat.format("EEE LLL dd, yyyy", item.ExpiryDate)
        block.addView(expiryView)

        val deleteButton = Button(this)
        deleteButton.layoutParams = params
        deleteButton.setOnClickListener {
            mFoodItems.remove(item)
            saveToFile(mFoodItems)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        deleteButton.text = "Del"
        block.addView(deleteButton)

        return block
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
            R.id.action_settings -> {
                val intent = Intent(this, SettingsMenu::class.java)
                startActivity(intent)
                return true
            }
            R.id.reload -> {
                mFoodItems = getDataFromStorage()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

