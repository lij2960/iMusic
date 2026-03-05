//
//  MusicPlayerApp.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import SwiftUI

@main
struct MusicPlayerApp: App {
    // 创建视图模型
    @StateObject private var viewModel = MusicPlayerViewModel()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
                .frame(minWidth: 1000, minHeight: 700)
        }
        .windowStyle(.hiddenTitleBar)
        .commands {
            // 自定义菜单
            CommandGroup(replacing: .newItem) {}
        }
    }
}
