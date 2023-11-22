package com.example.mylife

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API 응답을 파싱하기 위한 데이터 클래스들
data class StationData(
    val currentCount: Int,
    val data: List<StationInfo>,
    val matchCount: Int,
    val page: Int,
    val perPage: Int,
    val totalCount: Int
)

data class StationInfo(
    val `1월`: String,
    val `2월`: String,
    val `3월`: String,
    val `4월`: String,
    val `5월`: String,
    val `6월`: String,
    val `7월`: String,
    val `8월`: String,
    val `9월`: String,
    val `10월`: String,
    val `11월`: String,
    val `12월`: String,
    val 역명: String,
    val 역번호: String,
    val 호선: String
)

// Retrofit을 사용하여 API 호출을 정의하는 인터페이스
interface StationDataService {
    @GET("api/15044249/v1/uddi:2a73166e-6fde-4c5e-97b4-92f20ffd4282")
    suspend fun getStationData(
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("serviceKey") serviceKey: String
    ): StationData
}

class Subway : Fragment() {
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.subway, container, false)

        lineChart = view.findViewById(R.id.lineChart)

        runBlocking {
            try {
                // 데이터를 가져와서 차트 적용
                val data = fetchData()
                val chartData = createLineData(data)
                setupLineChart(chartData)
            } catch (e: Exception) {
                // 예외 처리
                Log.d("오류",e.toString());
            }
        }
        return view
    }

    // API에서 데이터 가져오기
    private suspend fun fetchData(): StationData {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.odcloud.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(StationDataService::class.java)
        val page = 1
        val perPage = 1
        val serviceKey = "API KEY" //공공DATA API KEY

        return service.getStationData(page, perPage, serviceKey)
    }

    //해당 데이터를 차트 적용
    private fun createLineData(data: StationData): LineData {
        val entries = mutableListOf<Entry>()

        //월별 데이터
        val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")

        //서울역 월별 데이터
        val monthlyData = data.data[0]

        for ((index, month) in months.withIndex()) {
            val value = when (month) {
                "1월" -> monthlyData.`1월`
                "2월" -> monthlyData.`2월`
                "3월" -> monthlyData.`3월`
                "4월" -> monthlyData.`4월`
                "5월" -> monthlyData.`5월`
                "6월" -> monthlyData.`6월`
                "7월" -> monthlyData.`7월`
                "8월" -> monthlyData.`8월`
                "9월" -> monthlyData.`9월`
                "10월" -> monthlyData.`10월`
                "11월" -> monthlyData.`11월` 
                "12월" -> monthlyData.`12월`
                else -> "0" // 해당 월이 없을 경우 0으로 초기화
            }.toFloatOrNull() ?: 0f
            entries.add(Entry(index.toFloat(), value))
        }
        //차트 데이터셋 생성
        val dataSet = LineDataSet(entries, monthlyData.역명) // 월별 데이터에서 역명을 가져옴
        dataSet.setDrawValues(false)

        return LineData(dataSet)
    }

    //차트 설정
    private fun setupLineChart(data: LineData) {
        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.xAxis.setDrawLabels(true)
        lineChart.axisRight.isEnabled = false
        lineChart.invalidate()

        //X축에 월 표시
        val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
    }
}