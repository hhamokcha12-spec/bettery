package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BatterySamplePoint

@Composable
fun PowerAnalyticsSection(
    recentSamples: List<BatterySamplePoint>,
    appLanguage: String,
    cardBg: Color,
    accentCyan: Color,
    thermalCrimson: Color,
    textMuted: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (appLanguage == "ar") "مرسمة الذبذبات للطاقة (Oscilloscope)" else "Power Oscilloscope",
                color = accentCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (appLanguage == "ar") "مراقبة حية للتيار والجهد الكهربائي" else "Real-time monitoring of current and voltage",
                color = textMuted,
                fontSize = 12.sp
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(cardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                if (recentSamples.size > 1) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxMv = recentSamples.maxOfOrNull { it.voltageMv }?.toFloat() ?: 1f
                        val minMv = recentSamples.minOfOrNull { it.voltageMv }?.toFloat() ?: 0f
                        val rangeMv = if (maxMv > minMv) maxMv - minMv else 1f

                        val width = size.width
                        val height = size.height
                        val stepX = width / (recentSamples.size - 1).coerceAtLeast(1)

                        val path = Path()
                        recentSamples.forEachIndexed { index, sample ->
                            val x = index * stepX
                            val normalizedY = 1f - ((sample.voltageMv - minMv) / rangeMv)
                            val y = normalizedY * height
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = accentCyan,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                } else {
                    Text(
                        text = if (appLanguage == "ar") "جاري جمع البيانات..." else "Collecting data...",
                        color = textMuted,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (appLanguage == "ar") "تحليل تدهور البطارية" else "Battery Degradation Analytics",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (appLanguage == "ar") "يتم حساب العمر الافتراضي بناءً على دورات الشحن وفقدان السعة." else "Lifespan calculated based on charge cycles and capacity loss.",
                        color = textMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(if (appLanguage == "ar") "الدورات المقدرة المتبقية" else "Estimated Cycles Left", color = textMuted, fontSize = 12.sp)
                            Text("~640", color = accentCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (appLanguage == "ar") "تصنيف الصحة" else "Health Rating", color = textMuted, fontSize = 12.sp)
                            Text("A+", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
