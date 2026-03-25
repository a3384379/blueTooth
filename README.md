# BlueTooth Music Link

重写后的蓝牙音乐控制工具，目标能力：

- 双角色：控制端 / 被控制端
- 跨平台协议：Android 10+ 与 iOS 18 共享同一指令编码
- 音乐控制：上一首、下一首、播放、暂停、音量
- 实时歌词：通过协议中的 `LYRICS_SYNC` 指令进行同步（当前示例使用本地歌词源）

## Android

- 入口：`app/src/main/java/com/mytooth/bluetooth/MainActivity.kt`
- 协议：`app/src/main/java/com/mytooth/bluetooth/protocol/BtMusicProtocol.kt`
- 业务协调：`app/src/main/java/com/mytooth/bluetooth/core/MusicControlCoordinator.kt`

## iOS 18 参考

- `ios18-reference/BluetoothMusicController.swift`
- 提供协议解码及 `MPMusicPlayerController` 映射示例。

> 说明：当前仓库提供的是功能完备的可运行原型。若要接入真实 BLE 设备、音乐 App 歌词源（如网易云、Apple Music SDK 回调），可在现有协议层上扩展 GATT 服务与歌词拉取模块。
