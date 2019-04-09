package com.example.emojify_kotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

class Emojifier{
    private val EMOJI_SCALE_FACTOR = .9f
    private val SMILING_PROB_THRESHOLD = .15
    private val EYE_OPEN_PROB_THRESHOLD = .5

    fun detectFacesandOverlayEmoji(context: Context, picture: Bitmap) : Bitmap{
        var faceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(true)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build()

        var frame = Frame.Builder().setBitmap(picture).build()

        var faces = faceDetector.detect(frame)

        Log.d(Emojifier::class.java.simpleName, "detectedFaces: number of faces $faces")

        var resultBitmap = picture

        if (faces.size() == 0) run {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show()
        } else {
            for (i in 1..faces.size()) {
                var face = faces[i]

                var emojiBitmap: Bitmap? = null

                when(whichEmoji(face)){
                    Emoji.SMILE -> emojiBitmap = BitmapFactory.decodeResource(context.resources,R.drawable.smile)
                }

                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap!!, face)
            }
        }

        faceDetector.release()

        return resultBitmap
    }

    private fun whichEmoji(face: Face): Emoji {

        val smiling = face.isSmilingProbability > SMILING_PROB_THRESHOLD

        val leftEyeClosed = face.isLeftEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD
        val rightEyeClosed = face.isRightEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD

        val emoji: Emoji

        if (smiling) {
            emoji = if (leftEyeClosed && !rightEyeClosed) {
                Emoji.LEFT_WINK
            } else if (rightEyeClosed && !leftEyeClosed) {
                Emoji.RIGHT_WINK
            } else if (leftEyeClosed) {
                Emoji.CLOSED_EYE_SMILE
            } else {
                Emoji.SMILE
            }
        } else {
            emoji = if (leftEyeClosed && !rightEyeClosed) {
                Emoji.LEFT_WINK_FROWN
            } else if (rightEyeClosed && !leftEyeClosed) {
                Emoji.RIGHT_WINK_FROWN
            } else if (leftEyeClosed) {
                Emoji.CLOSED_EYE_FROWN
            } else {
                Emoji.FROWN
            }
        }

        return emoji
    }

    fun addBitmapToFace(backgroundBitmap: Bitmap, emojiBitmap: Bitmap, face: Face): Bitmap {
        var emojiBitmap = emojiBitmap

        val resultBitmap = Bitmap.createBitmap(
            backgroundBitmap.width,
            backgroundBitmap.height, backgroundBitmap.config
        )

        val scaleFactor = EMOJI_SCALE_FACTOR

        val newEmojiWidth = (face.width * scaleFactor).toInt()
        val newEmojiHeight = (emojiBitmap.height * newEmojiWidth / emojiBitmap.width * scaleFactor).toInt()

        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false)

        val emojiPositionX = face.position.x + face.width / 2 - emojiBitmap.width / 2
        val emojiPositionY = face.position.y + face.height / 2 - emojiBitmap.height / 3

        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null)

        return resultBitmap
    }
}