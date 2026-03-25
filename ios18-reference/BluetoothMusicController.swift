import Foundation
import CoreBluetooth
import MediaPlayer

/// iOS 18 参考实现：与 Android 端使用相同的协议编码。
final class BluetoothMusicController {
    func decode(_ frame: String) -> (command: String, payload: String) {
        let parts = frame.split(separator: "|", maxSplits: 2).map(String.init)
        guard parts.count >= 2 else { return ("LYRICS_SYNC", "") }
        let command = parts[1]
        let payload = parts.count == 3 ? parts[2] : ""
        return (command, payload)
    }

    func apply(command: String, player: MPMusicPlayerController = .systemMusicPlayer) {
        switch command {
        case "PLAY": player.play()
        case "PAUSE": player.pause()
        case "NEXT": player.skipToNextItem()
        case "PREV": player.skipToPreviousItem()
        default: break
        }
    }
}
