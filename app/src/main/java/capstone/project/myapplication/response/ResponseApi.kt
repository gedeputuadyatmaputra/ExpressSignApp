package capstone.project.myapplication.response

import com.google.gson.annotations.SerializedName

data class ResponseApi(

	@field:SerializedName("ResponseApi")
	val responseApi: List<ResponseApiItem>


)

data class ResponseApiItem(

	@field:SerializedName("detectionType")
	val detectionType: String,

	@field:SerializedName("data")
	val data: Data,

	@field:SerializedName("userId")
	val userId: String,

	@field:SerializedName("detectionId")
	val detectionId: String,

	@field:SerializedName("timestamp")
	val timestamp: String
)

data class Data(

	@field:SerializedName("imageUrl")
	val imageUrl: String,

	@field:SerializedName("detectedExpression")
	val detectedExpression: String? = null,

	@field:SerializedName("detectedSignLanguange")
	val detectedSignLanguange: String? = null
)

