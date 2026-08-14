import SwiftUI

struct RootTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }

            TRSFlowView()
                .tabItem { Label("TRS", systemImage: "heart.text.square.fill") }

            ReimbursementFlowView()
                .tabItem { Label("Reimbursement", systemImage: "dollarsign.circle.fill") }

            ResourcesView()
                .tabItem { Label("Resources", systemImage: "book.fill") }

            SettingsView()
                .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
        .tint(Theme.Color.primary)
    }
}
