package capstone.project.myapplication.ui.face

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import capstone.project.myapplication.R
import capstone.project.myapplication.auth.LoginActivity
import capstone.project.myapplication.classifier.face.FaceClassifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class FaceDetailActivity : AppCompatActivity() {

    private lateinit var buttonGallery: Button
    private lateinit var buttonAnalyze: Button
    private lateinit var buttonCamera: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var bitmap: Bitmap
    private lateinit var faceClassifier: FaceClassifier
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)

        buttonGallery = findViewById(R.id.btn_gallery_face)
        buttonAnalyze = findViewById(R.id.btn_scanner_face)
        buttonCamera = findViewById(R.id.btn_camera_face)
        imageView = findViewById(R.id.img_face)
        textView = findViewById(R.id.tv_result_gesture)

        faceClassifier = FaceClassifier(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        buttonGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 100)
        }

        buttonAnalyze.setOnClickListener {
            if (::bitmap.isInitialized) {
                analyzeImage()
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        buttonCamera.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                imageView.setImageURI(uri)
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            bitmap = imageBitmap
            analyzeImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun analyzeImage() {
        imageView.drawable?.let { drawable ->
            val bitmap = (drawable as BitmapDrawable).bitmap
            Log.d("expression", "Starting image classification")
            faceClassifier.classify(bitmap) { result ->
                Log.d("expression", "$result")
                textView.text = result
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu -> {
                logout()
                return true
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
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("userId")
        editor.apply()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val CAMERA_PERMISSION_REQUEST_CODE = 102
    }
}


