package com.ysj.cwclanapp

data class videoData(val id:String, val img:String, val author:String, val title:String){
    val link = "https://youtu.be/"+id
}