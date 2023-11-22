import SwiftUI
import WebKit

//웹뷰를 SwiftUI에 사용할 수 있도록 하는 UIViewRepresnetable 프로토콜 적용
struct WebView: UIViewRepresentable {
    let urlString: String

    //UIView를 생성하는 함수
    func makeUIView(context: Context) -> WKWebView {
        return WKWebView()
    }

    //UIView가 업데이트 될 때 호출되는 함수
    func updateUIView(_ uiView: WKWebView, context: Context) {
        //전달 받은 URL 객체로 변환
        if let url = URL(string: urlString) {
            //URL를 이용하여 URLRequest  생성 및 웹뷰 로드
            let request = URLRequest(url: url)
            uiView.load(request)
        }
    }
}

struct Tap3: View {
    let urlString = "https://m.land.naver.com/"

    var body: some View {
        //웹뷰를 이용하여 해당 URL 표시
        WebView(urlString: urlString)
            .edgesIgnoringSafeArea(.all)
    }
}

struct Tap3_Previews: PreviewProvider {
    static var previews: some View {
        Tap3()
    }
}
