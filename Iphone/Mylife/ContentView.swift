import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            // 첫 번째 탭
            NavigationView {
                Tap1()
            }
            .tabItem {
                Label("날씨 및 뉴스", systemImage: "1.circle")
            }
            .tag(1)

            // 두 번째 탭
            NavigationView {
                Tap2()
            }
            .tabItem {
                Label("서울역 탑승수", systemImage: "2.circle")
            }
            .tag(2)

            // 세 번째 탭
            NavigationView {
                Tap3()
            }
            .tabItem {
                Label("부동산", systemImage: "3.circle")
            }
            .tag(3)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
