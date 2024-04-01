package com.novikov.faceidentifier.service

import android.os.AsyncTask
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.lang.Exception

class ApiClient {

    public suspend fun sendPicture(file: File) : String{
            val client = OkHttpClient()
            val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)

            Log.i("file: ", file.toString())

            val request = Request.Builder()
                .addHeader("Media-type", "multipart/form-data")
                .url("http://192.168.88.176:8000/upload")
                .post(requestBody)
                .build()

            Log.i("request", request.toString())
//
            client.newCall(request = request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("okhttp", e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.i("okhttp", response.body.toString())
                }

            })

//            Log.i("response", response.message)
//            return response.body.toString()
        return ""
    }

//    override fun doInBackground(vararg params: File?): String {
//
//        val client = OkHttpClient()
//        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), params[0]!!)
//
//        val request = Request.Builder()
//            .url("http://192.168.88.176:8000/upload/")
//            .addHeader("Content-type", "multipart/form/data")
//            .post(requestBody)
//            .build()
//
//        val response = client.newCall(request = request).execute()
//        print(response.body.toString())
//        return response.body.toString()
//
//    }
}