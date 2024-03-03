package com.muhammadsayed.fancyvolumestepper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammadsayed.fancyvolumestepper.ui.theme.FancyVolumeStepperTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FancyVolumeStepperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F6FA),
                ) {
                    FancyVolumeStepper()
                }
            }
        }
    }
}

@Composable
fun FancyVolumeStepper() {
    var progress by remember {
        mutableIntStateOf(0)
    }
    var draggingUp by remember {
        mutableStateOf(false)
    }

    var draggingDown by remember {
        mutableStateOf(false)
    }

    var dragEnded by remember {
        mutableStateOf(true)
    }
    Box(contentAlignment = Alignment.Center) {

        OvalShape(
            onDragEnd = {
                dragEnded = true
                draggingDown = false
                draggingUp = false
            },
            progressUp = {
                draggingUp = true
                progress = ((abs(it) + progress).toInt()).coerceIn(0, 100)
            }, progressDown = {
                draggingDown = true
                progress = (progress - (abs(it)).toInt()).coerceIn(0, 100)
            }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .rotate(180f),
                tint = if (draggingUp && dragEnded) Color.Blue
                else Color(0xFFE0E0EE)
            )

            Text(
                text = "$progress",
                fontSize = 45.sp,
                fontWeight = FontWeight.Light,
                color = Color.Gray
            )

            Icon(
                Icons.Filled.ArrowDropDown, contentDescription = null,
                tint = if (draggingDown && dragEnded && progress > 0) {
                    Color.Blue
                } else Color(0xFFE0E0EE)
            )
        }
    }


}


@Composable
fun OvalShape(progressUp: (Float) -> Unit, progressDown: (Float) -> Unit, onDragEnd: () -> Unit) {
    val distance = 80.dp
    var offsetY by remember { mutableStateOf(0.dp) }

    var dragDirection by remember { mutableStateOf("") }

    val animatable by remember {
        derivedStateOf { Animatable(initialValue = offsetY.value) }
    }

    val scope = rememberCoroutineScope()

    Canvas(modifier = Modifier
        .size(300.dp)
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, dragAmount ->
                    change.consume()
                    offsetY = (offsetY + dragAmount.y.dp)
                        .coerceIn(-distance, distance)


                    if (dragAmount.y < 0) {
                        dragDirection = "Up"
                        progressUp((animatable.value / 80).coerceIn(-1f, 0f))
                    } else if (dragAmount.y > 0) {
                        dragDirection = "Down"
                        progressDown((animatable.value / 80).coerceIn(0f, 1f))
                    }
                },
                onDragEnd = {
                    scope.launch {
                        animatable.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = 0.4f,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        offsetY = 0.dp
                    }
                    onDragEnd()

                })


        }) {

        val centerX = (size.width.toDp() / 2).toPx()
        val centerY = (size.height.toDp() / 2).toPx()
        val radius = (size.minDimension.toDp() / 2).toPx()



        drawOval(
            color = Color.White,
            topLeft = Offset(centerX - radius / 2, centerY - radius / 2),
            size = Size(radius, radius),
        )
        if (dragDirection == "Down") {
            drawOval(
                color = Color.White,
                topLeft = Offset(centerX - radius / 2, centerY - radius / 2),
                size = Size(radius, radius + animatable.value),
            )
        } else {
            rotate(180f) {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(
                        centerX - radius / 2,
                        centerY - radius / 2
                    ),
                    size = Size(radius, radius - animatable.value),
                )
            }
        }


    }
}

@Preview
@Composable
fun FancyVolumeStepperPreview() {
    FancyVolumeStepper()
}