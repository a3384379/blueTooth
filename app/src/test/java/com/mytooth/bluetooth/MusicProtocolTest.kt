package com.mytooth.bluetooth

import com.mytooth.bluetooth.core.DeviceRole
import com.mytooth.bluetooth.core.MusicControlCoordinator
import com.mytooth.bluetooth.protocol.BtMusicCommand
import com.mytooth.bluetooth.protocol.BtMusicPacket
import com.mytooth.bluetooth.protocol.ProtocolCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicProtocolTest {

    @Test
    fun codec_roundTrip_success() {
        val encoded = ProtocolCodec.encode(BtMusicPacket(command = BtMusicCommand.NEXT, payload = "idx=1"))
        val decoded = ProtocolCodec.decode(encoded)

        assertEquals(BtMusicCommand.NEXT, decoded.command)
        assertEquals("idx=1", decoded.payload)
    }

    @Test
    fun coordinator_supportsRoleAndPlaybackControl() {
        val coordinator = MusicControlCoordinator()

        coordinator.switchRole(DeviceRole.TARGET)
        coordinator.togglePlayPause()
        coordinator.nextSong()

        val state = coordinator.snapshot()
        assertEquals(DeviceRole.TARGET, state.role)
        assertTrue(state.isPlaying)
        assertTrue(state.trackTitle.isNotBlank())
        assertTrue(state.currentLyrics.isNotEmpty())
    }
}
