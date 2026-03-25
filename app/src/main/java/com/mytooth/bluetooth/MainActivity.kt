package com.mytooth.bluetooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mytooth.bluetooth.core.DeviceRole
import com.mytooth.bluetooth.core.MusicControlCoordinator
import com.mytooth.bluetooth.core.MusicUiState
import com.mytooth.bluetooth.ui.theme.BlueToothTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(MusicControlCoordinator())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlueToothTheme {
                val state by viewModel.uiState.collectAsState()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        state = state,
                        modifier = Modifier.padding(innerPadding),
                        onRoleChange = viewModel::switchRole,
                        onPlayPause = viewModel::togglePlayPause,
                        onNext = viewModel::nextSong,
                        onPrev = viewModel::previousSong,
                        onVolumeChange = viewModel::setVolume,
                    )
                }
            }
        }
    }
}

class MainViewModel(
    private val coordinator: MusicControlCoordinator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(coordinator.snapshot())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    fun switchRole(role: DeviceRole) = update { coordinator.switchRole(role) }
    fun togglePlayPause() = update { coordinator.togglePlayPause() }
    fun nextSong() = update { coordinator.nextSong() }
    fun previousSong() = update { coordinator.previousSong() }
    fun setVolume(value: Float) = update { coordinator.updateVolume(value) }

    private fun update(action: () -> Unit) {
        viewModelScope.launch {
            action()
            _uiState.value = coordinator.snapshot()
        }
    }

    class Factory(private val coordinator: MusicControlCoordinator) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(coordinator) as T
        }
    }
}

@Composable
private fun MainScreen(
    state: MusicUiState,
    modifier: Modifier = Modifier,
    onRoleChange: (DeviceRole) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onVolumeChange: (Float) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "BlueTooth 音乐控制",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("兼容目标：Android 10+ / iOS 18（同协议）")

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            DeviceRole.entries.forEach { role ->
                SegmentedButton(
                    selected = state.role == role,
                    onClick = { onRoleChange(role) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = role.ordinal,
                        count = DeviceRole.entries.size,
                    ),
                ) {
                    Text(role.label)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("当前歌曲：${state.trackTitle}")
                Text("播放状态：${if (state.isPlaying) "播放中" else "已暂停"}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPrev) { Text("上一首") }
                    Button(onClick = onPlayPause) { Text(if (state.isPlaying) "暂停" else "播放") }
                    Button(onClick = onNext) { Text("下一首") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("音量：${(state.volume * 100).toInt()}%")
                Slider(value = state.volume, onValueChange = onVolumeChange, valueRange = 0f..1f)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("实时歌词")
                LazyColumn {
                    items(state.currentLyrics) { line ->
                        Text("• $line")
                    }
                }
            }
        }
    }
}
