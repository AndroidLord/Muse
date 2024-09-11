package com.example.muse.ui.muse.HelperUI

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.muse.data.local.model.Audio
import com.example.muse.ui.muse.UIEvents


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onPlaylistCreateClick: (String, Long) -> Unit,
    trackId: Long = -1
) {

    var text by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .border(2.dp, Color.Gray, shape = RoundedCornerShape(15.dp))
        ) {
            Column {
                Text(
                    text = "New Playlist",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Red,
                        unfocusedIndicatorColor = Color.Red
                    ),
                    singleLine = true,
                    maxLines = 1,
                    placeholder = { Text(text = "Enter Playlist Name") },
                    trailingIcon = {
                        Icon(imageVector = Icons.Filled.Cancel, contentDescription = null,
                            modifier = Modifier.clickable { text = "" }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {


                    Text(
                        text = "Cancel",
                        textAlign = TextAlign.Center,
                        color = Color.Red,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onDismiss() }
                    )

                    Canvas(modifier = Modifier.size(1.dp, 30.dp), onDraw = {
                        drawLine(
                            start = Offset(0f, 0f),
                            end = Offset(0f, 40f),
                            color = Color.Gray
                        )
                    })
                    Text(
                        text = "Create",
                        textAlign = TextAlign.Center,
                        color = Color.Red,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onPlaylistCreateClick(text, trackId) }
                    )

                }
            }
        }
    }


}

@Composable
fun MusicColumnList(
    audioList: List<Audio>,
    currentPlayingAudio: Audio,
    onMoreInfoClick: (id: Long) -> Unit,
    onMusicItemClick: () -> Unit,
    onUiEvents: (UIEvents) -> Unit,
    startService: () -> Unit
) {
    val context = LocalContext.current

    var currentPlayingAudioState by remember { mutableStateOf(currentPlayingAudio) }

    // Update currentPlayingAudioState when currentPlayingAudio changes
    LaunchedEffect(key1 = currentPlayingAudio) {
        currentPlayingAudioState = currentPlayingAudio
    }


    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {



        Log.d("MusicCompose", "Audio Item Outside: ${audioList.size}")

        itemsIndexed(audioList) { index, audio ->

            AudioItem(
                audio = audio,
                currentPlayingAudio = (audio.uri == currentPlayingAudioState.uri),
                onMoreInfoClick = onMoreInfoClick,
                onItemClick = {
                    onMusicItemClick()
                    onUiEvents(UIEvents.SelectAudioChange(index))
                    startService()
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioItem(
    audio: Audio,
    currentPlayingAudio: Boolean = false,
    onItemClick: () -> Unit,
    onMoreInfoClick: (id: Long) -> Unit
) {

    Log.d("MusicCompose", "Audio Item: ${audio.displayName}")


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                onItemClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    brush = when {
                        currentPlayingAudio -> Brush.horizontalGradient(GradientColors)
                        else -> SolidColor(Color.Unspecified)
                    }
                )
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = when{
                        currentPlayingAudio -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = when {
                        currentPlayingAudio -> Modifier.basicMarquee(velocity = 15.dp)
                        else -> Modifier
                    }
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier.clickable {
                    onMoreInfoClick.invoke(audio.id)
                }
            )
            Spacer(modifier = Modifier.size(8.dp))

        }

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarPlayer(
    modifier: Modifier = Modifier,
    isAudioPlaying: Boolean,
    progress: Float,
    onBottomClick: () -> Unit = {},
    onUiEvents: (UIEvents) -> Unit
) {

    BottomAppBar(
        modifier = modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onBottomClick() }
    ) {
        Column(
            modifier = Modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ArtistInfo(audio = audio)
                MediaPlayerController(
                    isAudioPlaying = isAudioPlaying,
                    onStart = { onUiEvents(UIEvents.PlayPause) },
                    onNext = { onUiEvents(UIEvents.SeekToNext) }
                )
                Log.d("MusicCompose", "Outside Slider--------------------->>>")
                AudioProgressSlider(
                    progress = progress,
                    onProgressChanged = { onUiEvents(UIEvents.SeekTo(it)) }
                )

            }

        }
    }

}

@Composable
fun AudioProgressSlider(
    progress: Float,
    onProgressChanged: (Float) -> Unit
) {

    Log.d("MusicCompose", "Inside Slider $progress<<<--------------------")

    var sliderProgress by remember { mutableFloatStateOf(progress) }

    LaunchedEffect(key1 = progress) {
        sliderProgress = progress
    }

    Slider(
        value = sliderProgress,
        onValueChange = { onProgressChanged(it) },
        valueRange = 0f..100f
    )
}

@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        PlayerIconItem(
            icon = when {
                isAudioPlaying -> Icons.Default.Pause
                else -> Icons.Default.PlayArrow
            }
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(4.dp))
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = null,
            modifier = Modifier.clickable { onNext() }
        )
    }

}

@Composable
fun ArtistInfo(
    modifier: Modifier = Modifier,
    audio: Audio
) {

    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(
            icon = Icons.Default.MusicNote,
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.size(4.dp))
        Column {
            Text(
                text = audio.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Clip,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = audio.artist,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Clip,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }

    }

}

@Composable
fun PlayerIconItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {


    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }

}

@Composable
fun NoAudioFoundScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
            )
            Text(text = "Loading...")
        }
    }
}
