package com.example.foodreminders

import java.util.*

class FoodItem(quantity: Int, measurement: String, expiry: Date, description: String, purchased: Date) {

    val Quantity = quantity
    val Measurement = measurement
    val PurchasedDate = purchased
    val ExpiryDate = expiry
    val Description = description

    fun toString(delim: String): String {
        return this.Description + delim + this.Quantity.toString() + delim + this.Measurement +
                delim + this.ExpiryDate.time.toString() + delim + this.PurchasedDate.time.toString()
    }

    companion object {
        fun fromString(string: String, delim: String): FoodItem{
            val split = string.split(delim)
            return FoodItem(split[1].toInt(), split[2], Date(split[3].toLong()), split[0], Date(split[4].toLong()))
        }
    }

}