package com.example.hitproduct.data.model.daily_question.post_answer

import com.google.gson.annotations.SerializedName

data class SaveAnswerResponse (
    val updateLog: UpdateLog
)

data class UpdateLog(
    @SerializedName("_id")              val id:              String,
    @SerializedName("coupleId")        val coupleId:        CoupleId,
    @SerializedName("questionId")      val questionId:      String,
    @SerializedName("date")            val date:            String,
    @SerializedName("isCompleted")     val isCompleted:     Boolean,
    @SerializedName("answerUserA")     val answerUserA:     String,
    @SerializedName("userAAnsweredAt") val userAAnsweredAt: String,
    @SerializedName("answerUserB")     val answerUserB:     String,
    @SerializedName("userBAnsweredAt") val userBAnsweredAt: String
)

data class CoupleId(
    @SerializedName("_id")       val id:      String,
    @SerializedName("userIdA")   val userIdA: String,
    @SerializedName("userIdB")   val userIdB: String
)