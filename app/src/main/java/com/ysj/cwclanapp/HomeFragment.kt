package com.ysj.cwclanapp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ysj.cwclanapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup

class HomeFragment : Fragment(){
    lateinit var mainActivity:MainActivity
    lateinit var binding:FragmentHomeBinding
    lateinit var dataSnapshot: DataSnapshot
    lateinit var db: FirebaseDatabase
    lateinit var noticeArr: ArrayList<String>
    lateinit var schedArr: ArrayList<String>
    lateinit var adapter:textAdapter

    override fun onResume() {
        adapter = textAdapter(noticeArr, mainActivity)
        if(adapter!=null) {
            adapter.notifyDataSetChanged()
        }
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        mainActivity = context as MainActivity
        db = Firebase.database
        isLiveCheck()
        initNotice()
        initButtonListener()
        return binding.root
    }
    fun initButtonListener(){
        binding.doomiLiveImg.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW,Uri.parse("https://bj.afreecatv.com/taijizoom"))
            mainActivity.startActivity(intent)
        }
        binding.CWTubeImg.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/@cwclan4866"))
            mainActivity.startActivity(intent)
        }
        binding.zerbraTubeImg.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@makingsignature5609"))
            mainActivity.startActivity(intent)
        }
    }
    fun initRView(){
        adapter = textAdapter(noticeArr, mainActivity)
        val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        mainActivity.runOnUiThread {
            binding.recyclerView.addItemDecoration(decoration)
            binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)
            binding.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
            adapter.itemClickListener = object: textAdapter.OnItemClickListener {
                override fun OnItemClick(position: Int, code: String) {

                }
            }
        }
    }
    fun initSched(){
        noticeArr = arrayListOf<String>()
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).async {
                dataSnapshot = db.getReference("noticeList").get().await()
            }.await()
            dataSnapshot.children
                .forEach {
                    noticeArr.add(it.value.toString())
                    Log.d("test",it.value.toString())
                }
            initRView()
        }
    }
    fun initNotice(){
        noticeArr = arrayListOf<String>()
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).async {
                dataSnapshot = db.getReference("noticeList").get().await()
            }.await()
            dataSnapshot.children
                .forEach {
                    val tmpStr = it.child("content").value.toString()
                noticeArr.add(tmpStr)
            }
            initRView()
        }
    }
    fun isLiveCheck(){
        CoroutineScope(Dispatchers.IO).launch {
            val doc = Jsoup.connect("https://bjapi.afreecatv.com/api/taijizoom/station").ignoreContentType(true).get().body()
            val json1 = JSONObject(doc.text())
            var json2: JSONObject? = null
            try{
                json2 = json1.getJSONObject("broad")
                mainActivity.runOnUiThread {
                    binding.liveDoomi.imageTintList= ColorStateList
                        .valueOf(Color.parseColor("#FFFFFF"))
                }
            }catch(e:java.lang.Exception){
                if(json2 == null){
                    mainActivity.runOnUiThread {
                        binding.liveDoomi.imageTintList= ColorStateList
                            .valueOf(Color.parseColor("#453D3D"))
                    }
                }
            }
        }
    }
}