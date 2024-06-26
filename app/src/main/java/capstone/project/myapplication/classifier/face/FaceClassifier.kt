package capstone.project.myapplication.classifier.face

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.core.IOException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class FaceClassifier(private val context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private var cachedToken: String? = null

    fun classify(bitmap: Bitmap, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Compress at 80% quality
            val byteArray = byteArrayOutputStream.toByteArray()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
                .build()

            getToken { token ->
                val request = Request.Builder()
                    .url("https://express-app-4s7pae4xgq-et.a.run.app/api/detect-expression")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                Log.d("FaceClassifier", "Sending image to API")

                makeApiCall(request, callback, 3)
            }
        }
    }

    private fun makeApiCall(request: Request, callback: (String) -> Unit, retries: Int) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (retries > 0) {
                    Log.d("FaceClassifier", "Retrying API call, attempts left: $retries")
                    makeApiCall(request, callback, retries - 1)
                } else {
                    (context as Activity).runOnUiThread {
                        Log.e("FaceClassifier", "API call failed: ${e.message}", e)
                        callback("Gagal identifikasi gambar, Silahkan coba lagi")
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                (context as Activity).runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("FaceClassifier", "API response: $responseBody")
                        val detectedExpression = parseDetectedExpression(responseBody)
                        callback(detectedExpression ?: "Gagal identifikasi gambar, Silahkan coba lagi")
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("FaceClassifier", "Failed with HTTP code ${response.code} and message: ${response.message}, error body: $errorBody")
                        callback("Gagal identifikasi gambar, Silahkan coba lagi")
                    }
                }
            }
        })
    }

    private fun parseDetectedExpression(responseBody: String?): String? {
        return try {
            val jsonObject = JSONObject(responseBody)
            jsonObject.getString("detectedExpression")
        } catch (e: Exception) {
            Log.e("FaceClassifier", "Failed to parse JSON", e)
            null
        }
    }

    private fun getToken(callback: (String) -> Unit) {
        if (cachedToken != null) {
            callback(cachedToken!!)
            return
        }

        firebaseUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                cachedToken = token
                callback(token ?: "")
            } else {
                Log.e("TOKEN_ERROR", "Failed to get token", task.exception)
                callback("")
            }
        }
    }
}
