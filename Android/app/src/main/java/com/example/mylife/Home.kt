package com.example.mylife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mylife.Home.*
import com.example.mylife.databinding.HomeBinding
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt


//날씨 및 뉴스


/*날씨 API data Model */
data class WeatherResponse(
    val name: String,
    val weather: List<Weather>,
    val main: Main
)

data class Weather(
    val description: String,
)

data class Main(
    val temp: Double,
    val humidity: Int
)

interface WeatherApiService {
    @GET("weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String
    ): Call<WeatherResponse>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.openweathermap.org/data/2.5/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val weatherApiService = retrofit.create(WeatherApiService::class.java)


/* JTBC 뉴스 API Model */
data class RssItem(
    var title: String? = null,
    var link: String? = null,
    var description: String? = null
)


/*리사이클뷰(뉴스라인)  Adapter 이벤트*/
class RssItemAdapter(
    private val rssItems: List<RssItem>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RssItemAdapter.RssItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(rssItem: RssItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news, parent, false)
        return RssItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RssItemViewHolder, position: Int) {
        val rssItem = rssItems[position]
        holder.bind(rssItem)
    }

    override fun getItemCount(): Int {
        return rssItems.size
    }


    inner class RssItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(rssItem: RssItem) {
            titleTextView.text = rssItem.title
            titleTextView.setOnClickListener {
                onItemClickListener.onItemClick(rssItem)
            }
        }
    }


}

class Home : Fragment() {
    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.home, container, false)


        // 날씨 API (Retrofit2를 사용하여 API 호출)
        //API 파라미터 ▼
        val apiKey = "API KEY" // OpenWeather API 키
        val cityName = "Seoul" // 호출할 도시 이름

        val textCityName = view.findViewById<TextView>(R.id.cityNameTextView) //도시이름
        val textStatus = view.findViewById<TextView>(R.id.weatherStatusTextView) //날씨상태
        val textTemperature = view.findViewById<TextView>(R.id.temperatureTextView) //온도


        val call: Call<WeatherResponse> = weatherApiService.getWeather(cityName, apiKey, "kr")
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()

                    if (weatherResponse != null) { // weatherResponse는 API 응답에서 추출한 데이터
                        textCityName.text = cityName
                        textTemperature.text =
                            "온도 : ${(weatherResponse.main.temp - 273.15).roundToInt()} °C"
                        textStatus.text = weatherResponse.weather[0].description
                    }
                } else {
                    Log.d("오류", "API 연동 실패");
                }

                Thread {
                    run(view);
                }.start()

            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // API 호출이 실패한 경우 처리
                Log.d("오류", t.toString());
            }
        })
        return view
    }


    //JTBC 뉴스라인 API 실행 (XML)
    private fun run(a: View) {
        val xmlData = fetchXmlData("https://fs.jtbc.co.kr/RSS/newsflash.xml")

        if (xmlData != null) {
            // XML 파싱
            val rssItems = parseXml(xmlData)

            activity?.runOnUiThread {
                val recyclerView = a.findViewById<RecyclerView>(R.id.newsHeadlineRecyclerView)
                val adapter = RssItemAdapter(rssItems, object : RssItemAdapter.OnItemClickListener {
                    override fun onItemClick(rssItem: RssItem) {
                        // 클릭된 아이템의 링크를 사용하여 웹 페이지이동
                        val link = rssItem.link
                        if (!link.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            context?.startActivity(intent)
                        }
                    }
                })
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())

                val itemDecoration =
                    DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
                val drawable = ContextCompat.getDrawable(recyclerView.context, R.drawable.listline)
                itemDecoration.setDrawable(drawable!!)
                recyclerView.addItemDecoration(itemDecoration)
            }


        } else {
            Log.d("오류", "XML RSS 오류");
        }
    }

    //RSS 연동
    private fun fetchXmlData(urlString: String): InputStream? {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            /*연결 정보*/
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("오류 ", "RSS 연동 실패");
        }
        return null
    }

    //XML 파싱
    private fun parseXml(inputStream: InputStream): List<RssItem> {
        val rssItems = mutableListOf<RssItem>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentItem: RssItem? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "item" -> {
                                currentItem = RssItem()
                            }

                            "title" -> {
                                currentItem?.title = parser.nextText()
                            }

                            "link" -> {
                                currentItem?.link = parser.nextText()
                            }

                            "description" -> {
                                currentItem?.description = parser.nextText()
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && currentItem != null) {
                            rssItems.add(currentItem)
                            currentItem = null
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("오류", "XML 파싱 오류");
        } finally {
            inputStream.close()
        }
        return rssItems
    }
}