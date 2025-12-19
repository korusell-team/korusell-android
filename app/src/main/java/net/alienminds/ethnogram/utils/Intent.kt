package net.alienminds.ethnogram.utils

import android.content.Context
import android.content.Intent
import android.net.Uri


object IntentActions{

    fun callNumber(
        context: Context,
        phone: String
    ){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.setData(Uri.parse("tel:$phone"))
        context.startActivity(intent)
    }

    fun sendMessage(
        context: Context,
        phone: String,
        body: String? = null
    ){
        val uri = Uri.parse("smsto:$phone")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        body?.let { intent.putExtra("sms_body", it) }
        context.startActivity(intent)
    }

    fun shareText(
        context: Context,
        subject: String,
        text: String
    ){
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                null
            )
        )
    }

    object Navigation{

        data class LocationParams(
            val latitude: Double,
            val longitude: Double,
            val address: String?
        )

        enum class Navigator{
            KAKAO,
            NAVER;
        }

        private fun openKakaoRoute(
            context: Context,
            params: LocationParams
        ){
            val lat = params.latitude
            val lng = params.longitude
            runCatching{
                val uri = Uri.parse("kakaomap://route?ep=$lat,$lng")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }.onFailure {
                it.printStackTrace()
                val webUri = Uri.parse("https://map.kakao.com/link/to/${params.address?: "$lat,$lng"},$lat,$lng")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            }
        }

        private fun openNaverRoute(
            context: Context,
            params: LocationParams
        ){
            val addr = params.address?.let(Uri::encode)?: "${params.latitude},${params.longitude}"
            val lat = params.latitude
            val lng = params.longitude
            runCatching {
                val uri = Uri.parse("nmap://route/car?dlat=$lat&dlng=$lng&dname=$addr")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }.onFailure {
                it.printStackTrace()
                val webUri = Uri.parse("https://m.map.naver.com/directions/car?dlat=$lat&dlng=$lng&dname=$addr")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            }
        }

        fun openRoute(
            context: Context,
            navigator: Navigator?,
            params: LocationParams
        ){
            when(navigator){
                Navigator.KAKAO -> openKakaoRoute(context, params)
                Navigator.NAVER -> openNaverRoute(context, params)
                null -> openMapLocation(context, params)
            }
        }

        fun openMapLocation(
            context: Context,
            params: LocationParams
        ){
            val lat = params.latitude
            val lng = params.longitude
            val addr = params.address?.let(Uri::encode)?: "$lat,$lng"
            val uri = Uri.parse("geo:$lat,$lng?q=$addr")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }


    }

}