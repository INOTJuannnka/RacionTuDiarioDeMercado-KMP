import SwiftUI
import ComposeApp

/// Envuelve el `UIViewController` que produce Compose Multiplatform.
///
/// Este archivo es lo único específico de iOS: no contiene navegación ni pantallas,
/// solo el puente entre SwiftUI y `MainViewControllerKt.MainViewController()`.
struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
