package capstone.project.myapplication.classifier.face

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.core.IOException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class FaceClassifier(private val context: Context) {

    private val client = OkHttpClient()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

    fun classify(bitmap: Bitmap, callback: (String) -> Unit) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
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

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    (context as Activity).runOnUiThread {
                        Log.e("FaceClassifier", "API call failed: ${e.message}", e)
                        callback("Error: API call failed - ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    (context as Activity).runOnUiThread {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            Log.d("FaceClassifier", "API response: $responseBody")
                            val detectedExpression = parseDetectedExpression(responseBody)
                            callback(detectedExpression ?: "Error: Empty response from server")
                        } else {
                            val errorBody = response.body?.string()
                            Log.e("FaceClassifier", "Failed with HTTP code ${response.code} and message: ${response.message}, error body: $errorBody")
                            callback("Error: Server encountered an issue. Please try again later.")
                        }
                    }
                }
            })
        }
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
        firebaseUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                callback(token ?: "")
            } else {
                Log.e("TOKEN_ERROR", "Failed to get token", task.exception)
                callback("")
            }
        }
    }
}