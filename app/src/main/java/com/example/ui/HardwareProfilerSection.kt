package com.example.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning

@Composable
fun HardwareProfilerSection(
    cpuStatus: String,
    cardBg: Color,
    accentCyan: Color,
    textMuted: Color,
    appLanguage: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (appLanguage == "ar") "ملف تعريف الأجهزة والعتاد" else "Hardware Profiler & Silicon Audit",
                color = accentCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (appLanguage == "ar") "مراقبة وتحليل معمق لعتاد الجهاز المؤثر على استنزاف الميلي أمبير." else "In-depth telemetry of silicon hardware impacting mAh drain.",
                color = textMuted,
                fontSize = 12.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = accentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (appLanguage == "ar") "وحدة المعالجة المركزية (CPU)" else "Processor Matrix (CPU)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = cpuStatus.ifBlank { if (appLanguage == "ar") "جاري تحليل بنية أنوية المعالج وحالة السبات..." else "Aggregating Core Architecture & Sleep States..." },
                        color = accentCyan,
                        fontSize = 13.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha=0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (appLanguage == "ar") "عامل الحرارة الأقصى" else "Max Thermal Coefficient", color = textMuted, fontSize = 12.sp)
                        Text("+0.2°C / min", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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
                        text = if (appLanguage == "ar") "استهلاك لوحة العرض (Display)" else "Display Panel Drain (GPU/Display)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (appLanguage == "ar") "معدل التحديث الديناميكي" else "Dynamic Refresh Rate", color = textMuted, fontSize = 12.sp)
                        Text("Active (1-120Hz)", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (appLanguage == "ar") "الطاقة المقدرة للاستنزاف" else "Estimated Current Draw", color = textMuted, fontSize = 12.sp)
                        Text("~250 mA", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (appLanguage == "ar") "حالة البيكسلات المعتمة" else "Dark Pixel State", color = textMuted, fontSize = 12.sp)
                        Text("Optimized", color = accentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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
                        text = if (appLanguage == "ar") "تشخيص الراديو والشبكات (Baseband)" else "Radio & Baseband Diagnostics",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (appLanguage == "ar") "استقرار إشارة الاتصال" else "Cellular Signal Stability", color = textMuted, fontSize = 12.sp)
                        Text("Optimal (-85 dBm)", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (appLanguage == "ar") "نزيف طاقة الواي فاي" else "Wi-Fi Module Drain", color = textMuted, fontSize = 12.sp)
                        Text("~45 mA", color = accentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Green.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                        Text(if (appLanguage == "ar") "جميع الهوائيات تعمل ضمن نطاق الخمول وتوفر الطاقة." else "All antennas are idling efficiently, preserving power.", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
