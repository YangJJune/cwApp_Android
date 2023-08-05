package com.ysj.cwclanapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.util.rangeTo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.ysj.cwclanapp.databinding.FragmentCwtubeBinding
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.internal.userAgent
import okhttp3.internal.wait
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CWTubeFragment:Fragment(){

    lateinit var binding: FragmentCwtubeBinding
    lateinit var mainActivity: MainActivity
    lateinit var videoAdapter:cwTubeAdapter
    private lateinit var vList:ArrayList<videoData>
    private val apiKey = "AIzaSyB_ydPsqRAGzJUIvNA8bVkvQtwOfCZU2yc"
    val scope = CoroutineScope(Dispatchers.IO)
    private val zebraLink = URL("https://www.youtube.com/@makingsignature5609/videos")
    private val doomiLink ="https://bj.afreecatv.com/taijizoom/vods"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCwtubeBinding.inflate(inflater, container, false)
        mainActivity = context as MainActivity
        vList = arrayListOf()
        getZebra()
        getDoomi()
        return binding.root
    }

    fun initRView(){
        //val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        //binding.videoView.addItemDecoration(decoration)
        val vList2 = vList.sortedByDescending {
            it.date
        }
        vList.clear()
        vList.addAll(vList2)
        binding.videoView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)
        videoAdapter = cwTubeAdapter(vList, mainActivity)
        videoAdapter.itemClickListener = object:cwTubeAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int, code: String) {
                Log.d("test","aaa")
            }
        }
        mainActivity.runOnUiThread {
            binding.videoView.adapter = videoAdapter
            videoAdapter.notifyDataSetChanged()
        }
    }
    fun getZebra(){
        scope.launch {
            Log.d("test",Jsoup.connect("http://cwclan.net/ent2/80043")
                .cookie("user-agent","15c2f6f9416d00cec8b4f729460293c0")
                .cookie("PHPSESSID","a3ddb676c0e68b8bc8922fa7913e3b89")
                .cookie("mobie","false")
                .cookie("xe_looged","true")
                .get()
                .toString())
            val url = URL(" https://youtube.googleapis.com/youtube/v3/playlistItems?part=contentDetails&maxResults=100&playlistId=UUvGSU8_jV2ruQUyjyTnqBmA&key="+apiKey)
            val client = OkHttpClient()
            var req = Request.Builder().url(url).build()
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.IO).async{
                    client.newCall(req).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e?.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                return
                            }

                            val json = JSONObject(response.body!!.string())
                            val jArray = json.getJSONArray("items")

                            for (i in 0 until jArray.length()) {
                                val y = jArray.getJSONObject(i)
                                val z = y.getJSONObject("contentDetails")
                                val url2 =
                                    "https://youtube.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&id=" + z.getString(
                                        "videoId"
                                    ) + "&key=" + apiKey
                                val client2 = OkHttpClient()
                                req = Request.Builder().url(url2).build()
                                client2.newCall(req).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        e?.printStackTrace()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        if (!response.isSuccessful) {
                                            Log.d("test", "Response Error")
                                            return
                                        }
                                        val json2 = JSONObject(response.body!!.string())


                                        val jarr2 = json2.getJSONArray("items")

                                        val tempJson = jarr2.getJSONObject(0).getJSONObject("snippet")
                                        val title = tempJson.getString("title")
                                        val img = tempJson.getJSONObject("thumbnails")
                                            .getJSONObject("high").getString("url")
                                        vList.add(
                                            videoData(
                                                z.getString("videoId"),
                                                img,
                                                "CW동물원",
                                                title,
                                                "https://youtu.be/"+z.getString("videoId"),
                                                z.getString("videoPublishedAt").substring(0,9)
                                            )
                                        )
                                        mainActivity.runOnUiThread { initRView() }
                                    }
                                })
                            }
                        }
                    })
                }.await()
            }
        }
    }
    fun getDoomi(){
        lateinit var doc:String
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).async {
                val web = OkHttpClient()
                val req = Request.Builder().url("https://bjapi.afreecatv.com/api/taijizoom/vods/all?page=1&per_page=100&orderby=reg_date&field=title%2Ccontents&created=false&catchCreated=true&keyword=CPL&months=").build()

                web.newCall(req).execute().use{response->
                    if (response.body != null) {
                        doc = response.body!!.string()
                    }
                }
            }.await()
            val json1 = JSONObject(doc)
            val jArr1 = json1.getJSONArray("data")
            for(i in 0 until jArr1.length()){
                val json2 = jArr1.getJSONObject(i)
                val title = json2.getString("title_name")
                val json3 = json2.getJSONObject("ucc")
                val img = "https:"+json3.getString("thumb")
                val id = json2.getString("title_no")
                val link = "https://vod.afreecatv.com/player/"+id
                vList.add(videoData(id,img,"두미★둠",title,link,json2.getString("reg_date").substring(0,9)))
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).async {
                val web = OkHttpClient()
                val req = Request.Builder().url("https://bjapi.afreecatv.com/api/taijizoom/vods/all?page=1&per_page=100&orderby=reg_date&field=title%2Ccontents&created=false&catchCreated=true&keyword=CW&months=").build()

                web.newCall(req).execute().use{response->
                    if (response.body != null) {
                        doc = response.body!!.string()
                    }
                }
            }.await()
            val json1 = JSONObject(doc)
            val jArr1 = json1.getJSONArray("data")
            for(i in 0 until jArr1.length()){
                val json2 = jArr1.getJSONObject(i)
                val title = json2.getString("title_name")
                val json3 = json2.getJSONObject("ucc")
                val img = "https:"+json3.getString("thumb")
                val id = json2.getString("title_no")
                val link = "https://vod.afreecatv.com/player/"+id
                vList.add(videoData(id,img,"두미★둠",title,link,json2.getString("reg_date").substring(0,9)))
            }
        }
    }
}