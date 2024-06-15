package capstone.project.myapplication.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import capstone.project.myapplication.R
import capstone.project.myapplication.auth.LoginActivity
import capstone.project.myapplication.ui.face.FaceActivity
import capstone.project.myapplication.ui.hand.HandActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var buttonface : Button
    private lateinit var buttonhand : Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        buttonface = findViewById(R.id.Btn_face)
        buttonhand = findViewById(R.id.Btn_hands)
        firebaseAuth = FirebaseAuth.getInstance()
        buttonface.setOnClickListener {
            val intent = Intent(this, FaceActivity::class.java)
            startActivity(intent)

        }

        buttonhand.setOnClickListener {
            val intent = Intent(this, HandActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu ->{
                logout()
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        firebaseAuth.signOut()
        clearUserIdFromPreferences()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearUserIdFromPreferences() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("userId")
        editor.apply()
    }
}