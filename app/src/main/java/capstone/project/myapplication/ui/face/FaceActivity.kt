package capstone.project.myapplication.ui.face

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
import com.google.firebase.auth.FirebaseAuth

import capstone.project.myapplication.ui.hand.HandActivity

class FaceActivity : AppCompatActivity() {

    private lateinit var buttonstartface : Button
    private lateinit var buttonhandstart : Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_face)

        buttonstartface = findViewById(R.id.button_startface)
        buttonhandstart = findViewById(R.id.Btn_hand)
        firebaseAuth = FirebaseAuth.getInstance()
        buttonstartface.setOnClickListener {
            val intent = Intent(this,FaceDetailActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonhandstart.setOnClickListener {
            val intent = Intent(this, HandActivity::class.java)
            startActivity(intent)
            finish()
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