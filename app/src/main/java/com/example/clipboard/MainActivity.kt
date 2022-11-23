package com.example.clipboard

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var floating_button: FloatingActionButton
    private lateinit var join_floating_button : FloatingActionButton
    private lateinit var clipboard: ClipboardManager
    private lateinit var firebase_database :  FirebaseDatabase
    private lateinit var firebase_firestore : FirebaseFirestore
    private var connection_name=""
    private lateinit var sharedPref: SharedPreferences
    private var user_name="default"
    private lateinit var button: Button
    private lateinit var recyclerview : RecyclerView
    private lateinit var my_adapter:_adapter
    private var listner_registration : ListenerRegistration? =null
    private lateinit var active_conn_textview : TextView
    private lateinit var active_name_textview:TextView
    private lateinit var linearlayout : LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        floating_button=findViewById(R.id.floating_btn)
        join_floating_button=findViewById(R.id.floatingActionButton_join)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        firebase_database=Firebase.database
        firebase_firestore=Firebase.firestore
        sharedPref = getSharedPreferences("shared_pref",Context.MODE_PRIVATE)
        active_conn_textview=findViewById(R.id.active_textView)
        linearlayout=findViewById(R.id.linear_layout_2)
        recyclerview=findViewById(R.id.recycler_view)
        active_name_textview=findViewById(R.id.active_name_textView)

        if(sharedPref.contains("user_name")){
            Log.i("!@!","yoo")
            init_recyclerview()
        }

        linearlayout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Remove the active connection ?")
                .setPositiveButton("Yes",
                    DialogInterface.OnClickListener { dialog, id ->
                        listner_registration?.remove()
                        active_conn_textview.visibility=View.INVISIBLE
                        linearlayout.visibility=View.INVISIBLE
                    })
                .setNegativeButton("cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // show AlertDialog
            builder.show()
        }

        if(!sharedPref.contains("user_name")){
            show_username_dialog()
        }

        floating_button.setOnClickListener {

            if(!sharedPref.contains("user_name")){
                Toast.makeText(this,"please enter username first",Toast.LENGTH_LONG).show()
                show_username_dialog()
                return@setOnClickListener
            }
            set_up_dialog()
        }

        join_floating_button.setOnClickListener {
            if(!sharedPref.contains("user_name")){
                Toast.makeText(this,"please enter username first",Toast.LENGTH_LONG).show()
                show_username_dialog()
                return@setOnClickListener
            }
            set_up_join_dialog()
        }

    }
    private fun init_recyclerview(){

        val list=ArrayList<Connection_data>()
        firebase_firestore.collection(sharedPref.getString("user_name","")!!).get()
            .addOnSuccessListener {
                for(document in it){
                    Log.i("!@!","${document.id}")
                    list.add(Connection_data(document.id,document.getString("password")!!))
                }
                my_adapter= _adapter(list)
                recyclerview.adapter = my_adapter
                my_adapter.onItemClick={
                    Log.i("!@!","$it")
                    val user_name=sharedPref.getString("user_name","")
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Connect to this connection ?")
                        .setPositiveButton("Yes",
                            DialogInterface.OnClickListener { dialog, id ->
                                remove_listner()
                                join_connection(user_name!!,it)

                            })
                        .setNegativeButton("cancel",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User cancelled the dialog
                            })
                    // show AlertDialog
                    builder.show()
                }

            }

        recyclerview.addItemDecoration(
            DividerItemDecoration(
                recyclerview.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        //swipe items functionality
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {

                //Remove swiped item from list and notify the RecyclerView
                val position = viewHolder.adapterPosition
                firebase_firestore.collection(sharedPref.getString("user_name","")!!)
                    .get()
                    .addOnSuccessListener {
                        for(document in it){
                            if(my_adapter.con_List.get(position).name==document.id){
                                Log.i("!@!","hmm ${my_adapter.con_List.get(position).name}")
                                my_adapter.remove_item(position)
                                document.reference.delete()
                                Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                break
                            }

                        }
                    }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerview)
    }

    private fun join_connection(connection_username: String, connectionName: String) {
        firebase_firestore.collection(connection_username)
            .document(connectionName)
            .get()
            .addOnSuccessListener {
                with(sharedPref.edit()){

                    putString("con_username",connection_username)
                    putString("con_name",connectionName)
                    putString("con_password","")
                }.apply()
                Toast.makeText(this,"joined ${connectionName} connection",Toast.LENGTH_SHORT).show()
                active_name_textview.text=connectionName
                active_conn_textview.visibility=View.VISIBLE
                linearlayout.visibility=View.VISIBLE
                add_listner()
            }

    }

    private fun create_connection(name:String,password:String){
        if(name.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"please fill the fields to create connection",Toast.LENGTH_SHORT).show()
            return
        }
        Log.i("!@!","${sharedPref.getString("user_name","")}")

        user_name=sharedPref.getString("user_name","").toString()
        Log.i("!@!","yoo ${user_name}")

        firebase_firestore.collection(user_name).document(name).set(Connection_data("",password))
            .addOnSuccessListener {
                Log.i("!@!","success")
                my_adapter.submitlist(Connection_data(name,password))
            }
            .addOnFailureListener {
                Log.i("!@!","failure")
            }
    }

    private fun set_up_dialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Create connection")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val titleBox = EditText(this)
        titleBox.hint = "Name"
        layout.addView(titleBox) // Notice this is an add method

        val password_box = EditText(this)
        password_box.hint = "Password"
        layout.addView(password_box) // Another add method

        builder.setView(layout)

        // Set up the buttons

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                connection_name = titleBox.text.toString()
                val connection_password=password_box.text.toString()
                create_connection(connection_name,connection_password)
            })

        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun set_up_join_dialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Join Connection")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val name_Box = EditText(this)
        name_Box.hint = "user name"
        layout.addView(name_Box) // Notice this is an add method

        val connection_box = EditText(this)
        connection_box.hint = "connection name"
        layout.addView(connection_box)

        val password_box = EditText(this)
        password_box.hint = "Password"
        layout.addView(password_box) // Another add method

        builder.setView(layout)

        // Set up the buttons

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                user_name=name_Box.text.toString()
                connection_name = connection_box.text.toString()
                val connection_password=password_box.text.toString()
                remove_listner()
                join_connection(user_name,connection_name,connection_password)
            })

        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun join_connection(
        connection_username: String,
        connectionName: String,
        connectionPassword: String
    ) {
        firebase_firestore.collection(user_name)
            .document(connection_name)
            .get()
            .addOnSuccessListener {
                if(it.get("password")!=connectionPassword){
                    Toast.makeText(this,"wrong connection details",Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                with(sharedPref.edit()){
                    putString("con_username",connection_username)
                    putString("con_name",connection_name)
                    putString("con_password",connectionPassword)
                }.apply()
                Toast.makeText(this,"joined ${connectionName} connection",Toast.LENGTH_SHORT).show()
                active_name_textview.text=connectionName
                active_conn_textview.visibility=View.VISIBLE
                linearlayout.visibility=View.VISIBLE
                add_listner()
            }
    }
    private fun add_listner(){
        val con_un=sharedPref.getString("con_username","")
        val con_name=sharedPref.getString("con_name","")
        listner_registration=firebase_firestore.collection(con_un!!).document(con_name!!)
            .addSnapshotListener { snapshot, error ->
                if(error!=null){
                    Toast.makeText(this,"some error occured",Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d("!@!", "Current data: ${snapshot.data}")
                    val selectedText=snapshot.data?.get("data").toString()
                    val clip = ClipData.newPlainText("label",selectedText)
                    clipboard.setPrimaryClip(clip)
                }
            }
    }
    private fun remove_listner(){
        listner_registration?.let {
            it.remove()
        }
    }


    private fun show_username_dialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Username")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val titleBox = EditText(this)
        titleBox.hint = "Type"
        layout.addView(titleBox) // Notice this is an add method

        builder.setView(layout)

        // Set up the buttons

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                user_name = titleBox.text.toString()
                sharedPref.edit().putString("user_name",user_name).apply()
                init_recyclerview()
            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        remove_listner()
    }
}