package com.example.lab_week_10

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import com.example.lab_week_10.viewmodels.TotalViewModel
import java.util.Date

class MainActivity : AppCompatActivity() {
    // Create an instance of the TotalDatabase
    // by lazy is used to create the database only when it's needed
    private val db by lazy { prepareDatabase() }
    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize the value of the total from the database
        initializeValueFromDatabase()
        // Prepare the ViewModel
        prepareViewModel()

    }
    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }
    private fun prepareViewModel(){
        // Observe the LiveData object
        viewModel.total.observe(this, Observer { total ->
            // Whenever the value of the LiveData object changes
            // the updateText() is called, with the new value as the parameter
            updateText(total)
        })
        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        ).allowMainThreadQueries().build()
    }

    private fun initializeValueFromDatabase() {
        val total = db.totalDao().getTotal(ID)
        if (total.isEmpty()) {
            db.totalDao().insert(
                Total(
                    id = 1,
                    total = TotalObject(
                        value = 0,
                        date = Date().toString()
                    )
                )
            )
        } else {
            viewModel.setTotal(total.first().total.value)
        }
    }

    override fun onPause() {
        super.onPause()
        db.totalDao().update(
            Total(
                id = ID,
                total = TotalObject(
                    value = viewModel.total.value!!,
                    date = Date().toString()
                )
            )
        )
    }

    override fun onStart() {
        super.onStart()

        val result = db.totalDao().getTotal(ID)
        if (result.isNotEmpty()) {
            val lastDate = result.first().total.date
            Toast.makeText(this, "Last updated: $lastDate", Toast.LENGTH_LONG).show()
        }
    }


    companion object {
        const val ID: Long = 1
    }
}


