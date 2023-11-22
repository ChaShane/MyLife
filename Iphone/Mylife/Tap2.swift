
import SwiftUI
import DGCharts //DGCharts 라이브리러 추가

//지하철역 정보 데이터 모델
struct StationData: Decodable {
    let currentCount: Int
    let data: [StationInfo]
    let matchCount: Int
    let page: Int
    let perPage: Int
    let totalCount: Int
}

struct StationInfo: Decodable {
    let january: String
    let february: String
    let march: String
    let april: String
    let may: String
    let june: String
    let july: String
    let august: String
    let september: String
    let october: String
    let november: String
    let december: String
    let stationName: String
    let stationNumber: String
    let line: String
    
    private enum CodingKeys: String, CodingKey {
        case january = "1월"
        case february = "2월"
        case march = "3월"
        case april = "4월"
        case may = "5월"
        case june = "6월"
        case july = "7월"
        case august = "8월"
        case september = "9월"
        case october = "10월"
        case november = "11월"
        case december = "12월"
        case stationName = "역명"
        case stationNumber = "역번호"
        case line = "호선"
    }
}


struct Tap2: View {
    @State private var lineChartEntries: [ChartDataEntry]?
    
    var body: some View {
        VStack {
            if let entries = lineChartEntries {
                //DGChart 라이브러리 차트 생성
                LineChart(entries: entries)
            } else {
                VStack {
                    Text("test")
                    ProgressView()
                }
            }
        }
        .onAppear {
            fetchData()
        }
    }
    //API 데이터 가져오기
    private func fetchData() {
        guard let url = URL(string: "https://api.odcloud.kr/api/15044249/v1/uddi:2a73166e-6fde-4c5e-97b4-92f20ffd4282?page=1&perPage=1&serviceKey=API KEY")
        else {
            return
        }
        
        //비동기 방식으로 API 데이터 가져오기
        URLSession.shared.dataTask(with: url) { data, _, error in
            if let data = data {
                do {
                    //JSON 데이터 -> 차트 엔트로로 변환
                    let stationData = try JSONDecoder().decode(StationData.self, from: data)
                    let entries = createLineChartEntries(stationData)
                    // 메인 스레드에서 UI 적용
                    DispatchQueue.main.async {
                        self.lineChartEntries = entries
                    }
                } catch {
                    print("데이터 디코딩 에러: \(error)")
                }
            } else if let error = error {
                print("데이터 가져오기 에러: \(error)")
            }
        }.resume()
    }
    
    //해당 데이터를 차트 적용
    private func createLineChartEntries(_ data: StationData) -> [ChartDataEntry] {
        let months = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"]
        let monthlyData = data.data[0]
        
        let entries: [ChartDataEntry] = months.enumerated().compactMap { (index, month) in
            let value = getValueForMonth(month, in: monthlyData)
            return ChartDataEntry(x: Double(index), y: value)
        }
        
        return entries
    }
    //각 월에 해당하는 데이터 값 가져오기
    private func getValueForMonth(_ month: String, in data: StationInfo) -> Double {
        switch month {
        case "1월": return Double(data.january) ?? 0.0
        case "2월": return Double(data.february) ?? 0.0
        case "3월": return Double(data.march) ?? 0.0
        case "4월": return Double(data.april) ?? 0.0
        case "5월": return Double(data.may) ?? 0.0
        case "6월": return Double(data.june) ?? 0.0
        case "7월": return Double(data.july) ?? 0.0
        case "8월": return Double(data.august) ?? 0.0
        case "9월": return Double(data.september) ?? 0.0
        case "10월": return Double(data.october) ?? 0.0
        case "11월": return Double(data.november) ?? 0.0
        case "12월": return Double(data.december) ?? 0.0
        default: return 0.0
        }
    }
    
    
}

struct  Tap2_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
//UIKit의 차트를 SwiftUI에 사용할 수 있도록 하는 UIViewRepresnetable 프로토콜 적용
struct LineChart: UIViewRepresentable {
    var entries: [ChartDataEntry]
    
    //UIView 생성하는 함수
    func makeUIView(context: Context) -> LineChartView {
        let chartView = LineChartView()
        return chartView
    }
    
    //UIView가 업데이트 될 때 호출되는 함수
    func updateUIView(_ uiView: LineChartView, context: Context) {
        //차트 데이터 셋 생성후 설정
        let dataSet = LineChartDataSet(entries: entries, label: "서울역")
        dataSet.drawValuesEnabled = false
        
        let data = LineChartData(dataSet: dataSet)
        uiView.data = data
        //X축 설정
        configureXAxis(uiView.xAxis)
        //오른쪽 Y축 비활성화
        uiView.rightAxis.enabled = false
    }
    
    //X축 설정 함수
    private func configureXAxis(_ xAxis: XAxis) {
        xAxis.valueFormatter = IndexAxisValueFormatter(values: ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"])
        xAxis.labelPosition = .bottom
        xAxis.labelRotationAngle = -45
    }
}
