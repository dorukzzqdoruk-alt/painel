package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Data class representing a celestial node in the galaxy canvas.
 */
data class GalaxyNode(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val baseAlpha: Float,
    val color: Color
)

/**
 * Canvas that renders an animated galaxy network with floating interconnected points
 * that never cluster together and continuously drift smoothly across space.
 */
@Composable
fun GalaxyConstellationCanvas(
    modifier: Modifier = Modifier,
    nodeCount: Int = 48,
    maxDistanceDp: Float = 110f
) {
    // Generate initial nodes
    val nodes = remember {
        val list = mutableListOf<GalaxyNode>()
        val rnd = Random(42)
        val palette = listOf(
            Color(0xFF00E5FF), // Neon Cyan
            Color(0xFF38BDF8), // Electric Blue
            Color(0xFF818CF8), // Soft Violet Blue
            Color(0xFF0284C7), // Deep Ocean Blue
            Color(0xFFE0F2FE)  // Stellar White
        )
        for (i in 0 until nodeCount) {
            val angle = rnd.nextFloat() * 2 * Math.PI.toFloat()
            val speed = 0.35f + rnd.nextFloat() * 0.75f
            list.add(
                GalaxyNode(
                    x = rnd.nextFloat() * 1000f,
                    y = rnd.nextFloat() * 1800f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = 1.8f + rnd.nextFloat() * 2.6f,
                    baseAlpha = 0.45f + rnd.nextFloat() * 0.5f,
                    color = palette[rnd.nextInt(palette.size)]
                )
            )
        }
        list
    }

    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    frameTime = time
                }
                lastTime = time
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background Cosmic Deep Gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0D1D3A), // Center subtle blue nebula
                    Color(0xFF060B18), // Mid cosmic navy
                    Color(0xFF020611)  // Dark deep edge
                ),
                center = Offset(width * 0.5f, height * 0.35f),
                radius = maxOf(width, height) * 0.85f
            )
        )

        // Draw secondary subtle ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1A00E5FF), Color.Transparent),
                center = Offset(width * 0.8f, height * 0.75f),
                radius = width * 0.6f
            ),
            radius = width * 0.6f,
            center = Offset(width * 0.8f, height * 0.75f)
        )

        val maxDistPx = maxDistanceDp * density
        val maxDistSq = maxDistPx * maxDistPx
        val minSeparation = 32f * density
        val minSeparationSq = minSeparation * minSeparation

        // Update positions with anti-clustering physics and boundary bouncing
        for (i in nodes.indices) {
            val n1 = nodes[i]

            // Anti-clustering repulsion from other nearby nodes
            for (j in i + 1 until nodes.size) {
                val n2 = nodes[j]
                val dx = n2.x - n1.x
                val dy = n2.y - n1.y
                val distSq = dx * dx + dy * dy

                if (distSq in 0.001f..minSeparationSq) {
                    val dist = sqrt(distSq)
                    val force = (minSeparation - dist) / minSeparation * 0.04f
                    val nx = dx / dist
                    val ny = dy / dist
                    n1.vx -= nx * force
                    n1.vy -= ny * force
                    n2.vx += nx * force
                    n2.vy += ny * force
                }
            }

            // Cap velocity
            val currentSpeedSq = n1.vx * n1.vx + n1.vy * n1.vy
            val maxSpeed = 1.6f
            if (currentSpeedSq > maxSpeed * maxSpeed) {
                val s = sqrt(currentSpeedSq)
                n1.vx = (n1.vx / s) * maxSpeed
                n1.vy = (n1.vy / s) * maxSpeed
            }

            // Move
            n1.x += n1.vx
            n1.y += n1.vy

            // Bounce off edges with margin
            val pad = 10f
            if (n1.x < pad) {
                n1.x = pad
                n1.vx = kotlin.math.abs(n1.vx)
            } else if (n1.x > width - pad) {
                n1.x = width - pad
                n1.vx = -kotlin.math.abs(n1.vx)
            }

            if (n1.y < pad) {
                n1.y = pad
                n1.vy = kotlin.math.abs(n1.vy)
            } else if (n1.y > height - pad) {
                n1.y = height - pad
                n1.vy = -kotlin.math.abs(n1.vy)
            }
        }

        // Draw connections between nearby nodes
        for (i in nodes.indices) {
            val p1 = nodes[i]
            for (j in i + 1 until nodes.size) {
                val p2 = nodes[j]
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val distSq = dx * dx + dy * dy

                if (distSq < maxDistSq) {
                    val dist = sqrt(distSq)
                    val proximity = 1f - (dist / maxDistPx)
                    val lineAlpha = (proximity * 0.45f).coerceIn(0f, 1f)

                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = lineAlpha),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = (1.1f * proximity * density).coerceAtLeast(0.8f)
                    )
                }
            }
        }

        // Draw nodes
        for (node in nodes) {
            // Glow aura
            drawCircle(
                color = node.color.copy(alpha = node.baseAlpha * 0.3f),
                radius = node.radius * 2.8f,
                center = Offset(node.x, node.y)
            )
            // Core star
            drawCircle(
                color = node.color.copy(alpha = node.baseAlpha),
                radius = node.radius,
                center = Offset(node.x, node.y)
            )
        }
    }
}
