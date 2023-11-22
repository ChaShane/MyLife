import SwiftUI
import Foundation

/*
 날씨 및 뉴스 탭
 */
// 날씨 정보를 담는 데이터 모델
struct Weather: Codable {
    let name: String
    let weather: [WeatherInfo]
    let main: MainInfo
}

struct WeatherInfo: Codable {
    let description: String
}

struct MainInfo: Codable {
    let temp: Double
    let humidity: Int
}

//뉴스 정보를 표현하는 데이터 모델
struct RssItem: Identifiable {
    var id: UUID = UUID()
    var title: String?
    var link: String?
    var description: String?
}

//날씨 정보를 가져오고 처리하는 하는 서비스 클래스
class WeatherService {
    static let shared = WeatherService()
    
    private init() {}
    
    func getWeather(cityName: String, apiKey: String, completion: @escaping (Result<Weather, Error>) -> Void) {
        let baseURL = "https://api.openweathermap.org/data/2.5/weather"
        let urlString = "\(baseURL)?q=\(cityName)&appid=\(apiKey)&lang=kr"
        
        if let url = URL(string: urlString) {
            URLSession.shared.dataTask(with: url) { data, response, error in
                if let data = data {
                    do {
                        let weather = try JSONDecoder().decode(Weather.self, from: data)
                        completion(.success(weather))
                    } catch {
                        completion(.failure(error))
                    }
                } else if let error = error {
                    completion(.failure(error))
                }
            }.resume()
        }
    }
}

//뉴스 데이터를 파싱하고 처리하는 클래스
class RssService: NSObject, XMLParserDelegate {
    var rssItems = [RssItem]()
    var currentElement: String?
    var currentRssItem: RssItem?
    
    //XML 데이터 파싱
    static func parseXml(data: Data) -> [RssItem] {
        let rssService = RssService()
        let parser = XMLParser(data: data)
        parser.delegate = rssService
        parser.parse()
        return rssService.rssItems
    }
    //XML 시작 태그 호출되는 메소드
    func parser(_ parser: XMLParser, didStartElement elementName: String, namespaceURI: String?, qualifiedName qName: String?, attributes attributeDict: [String : String] = [:]) {
        currentElement = elementName
        if elementName == "item" {
            currentRssItem = RssItem()
        }
    }
    //XML 종료 태그 호출되는 메소드
    func parser(_ parser: XMLParser, didEndElement elementName: String, namespaceURI: String?, qualifiedName qName: String?) {
        currentElement = nil
        if elementName == "item" {
            if let rssItem = currentRssItem {
                rssItems.append(rssItem)
            }
            currentRssItem = nil
        }
    }
    
    //XML 문자열이 존재할때 호출되는 메소드
    func parser(_ parser: XMLParser, foundCharacters string: String) {
        switch currentElement {
        case "title":
            currentRssItem?.title = string
        case "link":
            currentRssItem?.link = string
        case "description":
            currentRssItem?.description = string
        default:
            break
        }
    }
}





struct Tap1: View {
    @State private var weather: Weather?
    @State private var rssItems: [RssItem] = []
    
    var body: some View {
        VStack {
            HStack {
                // 날씨 이미지와 정보
                Image(systemName: "sun.max.fill")
                    .resizable()
                    .frame(width: 50, height: 50)
                    .foregroundColor(.yellow)
                
                if let weather = weather {
                    Text("City: \(weather.name)\nTemperature: \(String(format: "%.1f", weather.main.temp - 273.15))°C\nHumidity: \(weather.main.humidity)%")
                } else {
                    Text("Loading weather...")
                }
                
                Spacer()
            }
            

            // 뉴스 목록
            List(rssItems) { rssItem in
                Button(action: {
                    openURLInSafari(rssItem.link)
                }) {
                    VStack(alignment: .leading) {
                        Text(rssItem.title ?? "")
                        //Text(rssItem.description ?? "")
                    }
                }
            }
            .onAppear {
                // 날씨 정보 가져오기
                WeatherService.shared.getWeather(cityName: "Seoul", apiKey: "API KEY") { result in
                    switch result {
                    case .success(let weather):
                        self.weather = weather
                    case .failure(let error):
                        print("Error fetching weather: \(error)")
                    }
                }
                
                // 뉴스 정보 가져오기
                if let url = URL(string: "https://fs.jtbc.co.kr/RSS/newsflash.xml") {
                    URLSession.shared.dataTask(with: url) { data, response, error in
                        if let data = data {
                            self.rssItems = RssService.parseXml(data: data)
                        } else if let error = error {
                            print("Error fetching RSS: \(error)")
                        }
                    }.resume()
                }
            }
        }
    }
    
    //뉴스 해당 항목 클릭시 JTBC 링크 URL 열기
    func openURLInSafari(_ urlString: String?) {
        if let urlString = urlString, let url = URL(string: urlString) {
            UIApplication.shared.open(url)
        }
    }
}

struct  Tap1_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

