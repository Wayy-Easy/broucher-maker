package com.brochurecraft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import com.brochurecraft.app.data.model.DesignCanvasState
import com.brochurecraft.app.data.model.DesignElement
import com.brochurecraft.app.data.model.ElementType
import com.brochurecraft.app.data.model.ShapeKind
import com.brochurecraft.app.ui.theme.VCPrimary
import kotlin.math.max

private fun parseColorSafe(hex: String, fallback: Color = Color.Black): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    fallback
}

@Composable
fun DesignCanvas(
    state: DesignCanvasState,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onDragStart: () -> Unit,
    onElementMoved: (id: String, dxFrac: Float, dyFrac: Float) -> Unit,
    onElementResized: (id: String, dwFrac: Float, dhFrac: Float) -> Unit,
    aspectRatio: Float = 1f / 1.4f,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(parseColorSafe(state.backgroundColorHex, Color.White), RoundedCornerShape(2.dp))
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onSelect(null) })
            }
    ) {
        val sorted = state.elements.sortedBy { it.zIndex }
        for (element in sorted) {
            key(element.id) {
                ElementView(
                    element = element,
                    isSelected = element.id == selectedId,
                    canvasSize = canvasSize,
                    onSelect = { onSelect(element.id) },
                    onDragStart = onDragStart,
                    onMoved = { dx, dy -> onElementMoved(element.id, dx, dy) },
                    onResized = { dw, dh -> onElementResized(element.id, dw, dh) }
                )
            }
        }
    }
}

@Composable
private fun ElementView(
    element: DesignElement,
    isSelected: Boolean,
    canvasSize: IntSize,
    onSelect: () -> Unit,
    onDragStart: () -> Unit,
    onMoved: (Float, Float) -> Unit,
    onResized: (Float, Float) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val widthPx = (element.width * canvasSize.width)
    val heightPx = (element.height * canvasSize.height)
    val leftPx = element.x * canvasSize.width
    val topPx = element.y * canvasSize.height

    with(density) {
        Box(
            modifier = Modifier
                .offset(x = leftPx.toDp(), y = topPx.toDp())
                .size(width = max(widthPx, 4f).toDp(), height = max(heightPx, 4f).toDp())
                .rotate(element.rotation)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isSelected) Modifier.border(2.dp, VCPrimary, RoundedCornerShape(2.dp))
                        else Modifier
                    )
                    .pointerInput(element.id) {
                        detectTapGestures(onTap = { onSelect() })
                    }
                    .pointerInput(element.id) {
                        detectDragGestures(
                            onDragStart = { onSelect(); onDragStart() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (canvasSize.width > 0 && canvasSize.height > 0) {
                                    onMoved(dragAmount.x / canvasSize.width, dragAmount.y / canvasSize.height)
                                }
                            }
                        )
                    }
            ) {
                when (element.type) {
                    ElementType.TEXT -> Text(
                        text = element.text,
                        color = parseColorSafe(element.colorHex),
                        fontSize = element.fontSizeSp.sp,
                        fontWeight = if (element.bold) FontWeight.Bold else FontWeight.Normal,
                        textAlign = when (element.textAlign) {
                            "LEFT" -> TextAlign.Start
                            "RIGHT" -> TextAlign.End
                            else -> TextAlign.Center
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    ElementType.SHAPE -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                parseColorSafe(element.fillColorHex, VCPrimary),
                                if (element.shapeKind == ShapeKind.CIRCLE) CircleShape
                                else RoundedCornerShape(element.cornerRadiusDp.dp)
                            )
                    )
                    ElementType.IMAGE -> if (element.imageUri != null) {
                        AsyncImage(
                            model = element.imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFD8E3FB)))
                    }
                }
            }

            if (isSelected) {
                // Resize handle - bottom-right corner drag changes width/height fraction.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 6.dp)
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, VCPrimary, CircleShape)
                        .pointerInput(element.id) {
                            detectDragGestures(
                                onDragStart = { onDragStart() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                                        onResized(dragAmount.x / canvasSize.width, dragAmount.y / canvasSize.height)
                                    }
                                }
                            )
                        }
                )
            }
        }
    }
}
