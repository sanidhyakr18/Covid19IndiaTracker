package com.sandystudios.covid_19indiatracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var stateListAdapter: StateListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = resources.getColor(R.color.white)
        val view: View = window.decorView
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)

        list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.list_header, list, false))

        fetchResults()
        swipeToRefresh.setOnRefreshListener {
            fetchResults()
        }
        list.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (list.getChildAt(0) != null) {
                    swipeToRefresh.isEnabled =
                        list.firstVisiblePosition == 0 && list.getChildAt(0).top == 0
                }
            }
        })
    }

    private fun fetchResults() {
        GlobalScope.launch {
            val response = withContext(Dispatchers.IO) { Client.api.clone().execute() }
            if (response.isSuccessful) {
                swipeToRefresh.isRefreshing = false
                val data = Gson().fromJson(response.body?.string(), Response::class.java)
                launch(Dispatchers.Main) {
                    bindCombinedData(data.statewise[0])
                    bindStateWiseData(data.statewise.subList(0, data.statewise.size))
                }
            }
        }
    }

    private fun bindStateWiseData(subList: List<StatewiseItem>) {
        stateListAdapter = StateListAdapter(subList)
        list.adapter = stateListAdapter
    }

    private fun bindCombinedData(data: StatewiseItem) {
        val lastUpdatedTime = data.lastupdatedtime
        val myFormat = "dd/MM/yyyy hh:mm:ss"
        val simpleDateFormat = SimpleDateFormat(myFormat, Locale.getDefault())
        val getTimeAgo = getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))
        lastUpdatedTv.text = "Last Updated\n $getTimeAgo"

        confirmedTv.text = NumberFormat.getIntegerInstance().format(Integer.valueOf(data.confirmed))
        activeTv.text = NumberFormat.getIntegerInstance().format(Integer.valueOf(data.active))
        recoveredTv.text = NumberFormat.getIntegerInstance().format(Integer.valueOf(data.recovered))
        deceasedTv.text = NumberFormat.getIntegerInstance().format(Integer.valueOf(data.deaths))
    }
}

fun getTimeAgo(past: Date): String {
    val now = Date()
    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

    return when {
        seconds < 60 -> {
            "Few seconds ago"
        }
        minutes < 60 -> {
            "$minutes minutes ago"
        }
        hours < 24 -> {
            "$hours hour ${minutes % 60} min ago"
        }
        else -> {
            val myFormat = "dd/MM/yyyy hh:mm:ss"
            SimpleDateFormat(myFormat, Locale.getDefault()).format(past).toString()
        }
    }
}