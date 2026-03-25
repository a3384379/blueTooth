package com.mytooth.bluetooth.core

import com.mytooth.bluetooth.protocol.BtMusicCommand
import com.mytooth.bluetooth.protocol.BtMusicPacket
import com.mytooth.bluetooth.protocol.ProtocolCodec
import kotlin.math.max
import kotlin.math.min

class MusicControlCoordinator {

    private var state = MusicUiState()

    private val library = listOf(
        Track("夜空中最亮的星", listOf("夜空中最亮的星", "能否听清", "那仰望的人 心底的孤独和叹息")),
        Track("平凡之路", listOf("我曾经跨过山和大海", "也穿过人山人海", "我曾经拥有着的一切")),
        Track("稻香", listOf("对这个世界如果你有太多的抱怨", "跌倒了就不敢继续往前走", "追不到的梦想换个梦不就得了")),
    )

    private var index = 0

    init {
        refreshTrack()
    }

    fun switchRole(role: DeviceRole) {
        state = state.copy(role = role)
    }

    fun togglePlayPause() {
        applyRemoteCommand(if (state.isPlaying) BtMusicCommand.PAUSE else BtMusicCommand.PLAY)
    }

    fun nextSong() {
        applyRemoteCommand(BtMusicCommand.NEXT)
    }

    fun previousSong() {
        applyRemoteCommand(BtMusicCommand.PREV)
    }

    fun updateVolume(level: Float) {
        applyRemoteCommand(BtMusicCommand.VOLUME)
        state = state.copy(volume = min(1f, max(0f, level)))
    }

    fun snapshot(): MusicUiState = state

    private fun applyRemoteCommand(command: BtMusicCommand) {
        val packet = BtMusicPacket(command = command, payload = "idx=$index;vol=${state.volume}")
        val encoded = ProtocolCodec.encode(packet)
        val decoded = ProtocolCodec.decode(encoded)
        when (decoded.command) {
            BtMusicCommand.PLAY -> state = state.copy(isPlaying = true)
            BtMusicCommand.PAUSE -> state = state.copy(isPlaying = false)
            BtMusicCommand.NEXT -> {
                index = (index + 1) % library.size
                refreshTrack()
            }
            BtMusicCommand.PREV -> {
                index = (index - 1 + library.size) % library.size
                refreshTrack()
            }
            BtMusicCommand.VOLUME -> Unit
            BtMusicCommand.LYRICS_SYNC -> refreshTrack()
        }
    }

    private fun refreshTrack() {
        val track = library[index]
        state = state.copy(trackTitle = track.name, currentLyrics = track.lines)
    }
}

data class Track(
    val name: String,
    val lines: List<String>,
)

enum class DeviceRole(val label: String) {
    CONTROLLER("控制端"),
    TARGET("被控制端"),
}

data class MusicUiState(
    val role: DeviceRole = DeviceRole.CONTROLLER,
    val trackTitle: String = "",
    val isPlaying: Boolean = false,
    val volume: Float = 0.4f,
    val currentLyrics: List<String> = emptyList(),
)
