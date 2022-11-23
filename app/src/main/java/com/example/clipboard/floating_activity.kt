package com.example.clipboard

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class floating_activity : Activity(){

    private lateinit var sharedPref: SharedPreferences
    private lateinit var clipboard: ClipboardManager
    private lateinit var firebase_firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.float_activity)
        sharedPref = getSharedPreferences("shared_pref",Context.MODE_PRIVATE)
        firebase_firestore=Firebase.firestore

        Log.i("!@!","floating activity oncreate")
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        Log.i("!@!"," yoo ${firebase_firestore}")

        if (intent.action == Intent.ACTION_PROCESS_TEXT) {
            // Get the text from the intent extra.
            val selectedText =  intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: "nope"
            Log.i("!@!", "selectedText is: ${selectedText}")

            val con_un=sharedPref.getString("con_username","")
            val con_name=sharedPref.getString("con_name","")
            val con_pass=sharedPref.getString("con_password","")
            Log.i("!@!","${con_name}$con_pass$con_un")
            firebase_firestore.collection(con_un!!).document(con_name!!)
                .update("data",selectedText)
//            Toast.makeText(this,"$selectedText can be sent to anyone",Toast.LENGTH_LONG).show()

//            val clip = ClipData.newPlainText("label",selectedText)
//            clipboard.setPrimaryClip(clip)
//            finish()


        }


    }

    override fun onResume() {
        super.onResume()
        Log.i("!@!","onresume")

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.i("!@!","focus changed  $hasFocus")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("!@!","floating activity destroyed")
    }
}