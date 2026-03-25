package com.mytooth.bluetooth.protocol

/**
 * 跨平台 BLE 协议：Android 10+ 与 iOS 18 共享同一帧结构。
 */
data class BtMusicPacket(
    val version: Int = 1,
    val command: BtMusicCommand,
    val payload: String = "",
)

enum class BtMusicCommand {
    PLAY,
    PAUSE,
    NEXT,
    PREV,
    VOLUME,
    LYRICS_SYNC,
}

object ProtocolCodec {
    fun encode(packet: BtMusicPacket): String {
        return "${packet.version}|${packet.command.name}|${packet.payload}"
    }

    fun decode(raw: String): BtMusicPacket {
        val parts = raw.split("|", limit = 3)
        val version = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val command = parts.getOrNull(1)?.let(BtMusicCommand::valueOf) ?: BtMusicCommand.LYRICS_SYNC
        val payload = parts.getOrNull(2).orEmpty()
        return BtMusicPacket(version = version, command = command, payload = payload)
    }
}
