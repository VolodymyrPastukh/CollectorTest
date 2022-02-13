package com.example.collectortest.database

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Database {

    private var _database: FirebaseDatabase? = null
    private val database: FirebaseDatabase
        get() = checkNotNull(_database)

    private lateinit var databaseRootRef: DatabaseReference

    init {
        _database = FirebaseDatabase.getInstance()
        databaseRootRef = database.reference
    }

    fun getDataReference() = databaseRootRef.child("data")

    //    fun getTimeRef(timestamp: String) = getDataReference().child(timestamp)
    fun getAccelerometerReference(timestamp: String) =
        getDataReference().child("accelerometer").child(timestamp)

    fun getGyroscopeReference(timestamp: String) =
        getDataReference().child("gyroscope").child(timestamp)


}