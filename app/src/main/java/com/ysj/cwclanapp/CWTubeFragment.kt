package com.ysj.cwclanapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.rangeTo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.ysj.cwclanapp.databinding.FragmentCwtubeBinding
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
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
    private val doomiLink = URL("https://bj.afreecatv.com/taijizoom/vods")
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
                                Log.d("test", "Response Error")
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
                                            .getJSONObject("default").getString("url")

                                        vList.add(
                                            videoData(
                                                z.getString("videoId"),
                                                img,
                                                "CW동물원",
                                                title
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




            /*BufferedReader(InputStreamReader(con.inputStream)).use { inp->
                var line:String?
                while(inp.readLine().also{
                    line = it
                    }!= null){

                    Log.d("test",line.toString())
                }

            }*/
        }
    }
    fun getDoomi(){

    }

}