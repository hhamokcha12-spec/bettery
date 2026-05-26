package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BatterySamplePoint
import com.example.data.local.ChargeSession
import kotlinx.coroutines.delay
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// MULTI-LANGUAGE BILINGUAL DICTIONARY IN COGNITIVE ARABIC / ENGLISH
fun getLabel(key: String, lang: String): String {
    val ar = mapOf(
        "title" to "هايبر تشارج - محرك المعايرة",
        "subtitle" to "إصدار المؤسسات والذكاء المطلق",
        "tab_engine" to "محرك الأيونات",
        "tab_schedules" to "ملفات الطاقة",
        "tab_diagnostics" to "المعايرة والنواة",
        "lang_switch" to "English UI",
        "status_charging" to "يتم الشحن الآن بفاعلية قصوى",
        "status_discharging" to "تشغيل على البطارية - غير موصول",
        "limit_shield" to "مدرع حماية الشحن الذكي",
        "limit_desc" to "يوقف تدفق تيار المدخل عند الوصول للنسبة المحددة لتمديد عمر دورات الليثيوم كيميائياً ومنع الانتفاخ الأيوني.",
        "set_limit_btn" to "تعديل الحد الأقصى: ",
        "system_safeguards" to "المثبتات والواقيات النظامية",
        "radio_hibernate" to "إخماد الهوائيات لسرعة الشحن",
        "radio_desc" to "يعطل تقلب موجات شبكات الراديو والواي فاي مؤقتاً عند التوصيل لتقليل المقاومة والحرارة.",
        "thermal_desc" to "يعمل على تبريد نواة السيليكون للمعالج وتأخير سرعة النبض للمحافظة على الخلايا.",
        "ion_preserve" to "الوقاية الأيونية الكاملة",
        "ion_desc" to "يكثف فحص توزيع الأقطاب المتناظرة لمنع الشروخ النانوية الكربودية والأكسدة.",
        "pure_amoled" to "نظام الطيف البصري الفائق (AMOLED)",
        "pure_desc" to "تعتيم مطلق بيكسل صفر لتخفيض السطوع وتيار الاستهلاك وانبعاث الحرارة.",
        "diagnostics_run" to "بدء فحص كيميائي للبطارية",
        "deep_diagnostics" to "الحصيلة الفنية ومصفوفت النواة الحية",
        "audit_section" to "مدقق التماسك الإلكتروليتي الكيميائي",
        "calib_section" to "بوابة معايرة وإصلاح المخططات",
        "current" to "التيار المتدفق",
        "voltage" to "الجهد اللحظي",
        "temp" to "الحرارة الكلية",
        "health" to "الحالة العامة",
        "cycles" to "دورات الشحن المكتملة",
        "wear_level" to "نسبة الاهتراء والضعف",
        "soh" to "كفاءة تخزين الأيونات (SOH)",
        "profile" to "ملف الطاقة النشط بالنواة",
        "profile_eco" to "الملف الاقتصادي الواقي",
        "profile_balanced" to "الملف المتوازن الموصى به",
        "profile_gaming" to "ملف الألعاب الفائق غير المحدود",
        "generate_cert" to "استخراج وثيقة فحص البطارية المعتمدة",
        "cert_result" to "التقرير المعتمد كيميائياً للبطارية",
        "cert_copy" to "نسخ التقرير الفني للذاكرة الحافظة",
        "repair_engine" to "معالج المعايرة والإصلاح",
        "running_repair" to "جاري المعايرة الكيميائية الفورية للأقطاب...",
        "ion_alignment_title" to "محاكي تدفق وبلمرة أيونات الليثيوم",
        "ion_alignment_desc" to "عرض حي لحركة الأيونات بين القطبين. الخلايا التالفة باللون الأحمر تمارس تصحيح الجريان والجهد لتتحول للأخضر.",
        "thermal_alert_title" to "نظام التخفيف الحراري البرمجي نشط",
        "thermal_alert_desc" to "تجاوزت حرارة بطارية الجهاز 38.0°C. تم كبح العمليات الخفية وخفض السطوع لحفظ كفاءة السيليكون والإنود والسيلوليت.",
        "limit_alert_title" to "مدرع كبح الإشباع الأيوني نشط",
        "limit_alert_desc" to "تطابق الشحن الفعلي مع الحد المستهدف ($1%). تم بدء نبضات التيار الدقيق لتأخير شيخوخة الليثيوم.",
        "no_history" to "لا توجد سجلات شحن محفوظة حتى الآن.",
        "generate_test" to "توليد سجل اختبار محاكاة",
        "history_title" to "سجل عمليات الشحن التاريخية",
        "deep_linux_status" to "حالة واختبارات نواة نظام لينكس العميقة",
        "realtime_log_header" to "محاكي وحدة التحكم وأوامر الهايبر تشارج",
        "no_shell_activity" to "لا توجد أنشطة نشطة حالياً. قم بتشغيل المميزات أو موازنة البطارية لدفق الأوامر الفورية..."
    )
    
    val en = mapOf(
        "title" to "HyperCharge Enterprise",
        "subtitle" to "Ultimate AI & Calibration Edition",
        "tab_engine" to "Engine Shield",
        "tab_schedules" to "Power Profiles",
        "tab_diagnostics" to "Core Diagnostics",
        "lang_switch" to "العربية",
        "status_charging" to "Active Fast Charging",
        "status_discharging" to "Unplugged Battery Powered",
        "limit_shield" to "Smart Battery Target Shield",
        "limit_desc" to "Prevents current saturation at a safe specific percentage threshold to preserve chemical lithium life and restrict cell bloating.",
        "set_limit_btn" to "Set Charging Target Limit: ",
        "system_safeguards" to "System Shield Defenses",
        "radio_hibernate" to "Auto Radio Sleep Optimizer",
        "radio_desc" to "Simulates hibernation in hardware wireless and wifi parameters when plugged, avoiding heat buildup.",
        "thermal_desc" to "Throttles background processing speeds and cooling routines if battery thermals breach extreme parameters.",
        "ion_preserve" to "Symmetric Electrochemical Control",
        "ion_desc" to "Regulates ion drift velocity to prevent crystal dendrite buildup in internal anode plates.",
        "pure_amoled" to "Pitch-Black Core AMOLED Mode",
        "pure_desc" to "Forces zero-pixel luminance to reduce continuous display controller drain and screen warmth.",
        "diagnostics_run" to "Run Diagnostics Cycle",
        "deep_diagnostics" to "Status Matrix & Live Activity Shell Streams",
        "audit_section" to "Chemical Ion Integrity Auditor",
        "calib_section" to "Deep Calibration & Repair System",
        "current" to "Current",
        "voltage" to "Voltage",
        "temp" to "Thermals",
        "health" to "Health State",
        "cycles" to "Charge Cycles",
        "wear_level" to "Cellular Wear Level",
        "soh" to "State of Health (SOH)",
        "profile" to "Active Core Power Profile",
        "profile_eco" to "Eco Preservation Mode",
        "profile_balanced" to "Optimal Balanced Mode",
        "profile_gaming" to "Max Gaming Mode (Unrestricted)",
        "generate_cert" to "Issue Battery Status Certificate",
        "cert_result" to "Issued Electrochemical Certificate Log",
        "cert_copy" to "Copy Certificate Text to Clipboard",
        "repair_engine" to "Electro-chemical Ion Realignment Engine",
        "running_repair" to "Actively Calibrating Structural Ions...",
        "ion_alignment_title" to "Lithium Ion Alignment Simulator",
        "ion_alignment_desc" to "Live representation of lithium ions migrating. Degrading cells (red) are rebalanced to stable states (green/cyan).",
        "thermal_alert_title" to "SOFTWARE THERMAL MITIGATION ACTIVE",
        "thermal_alert_desc" to "Battery core temp exceeded 38.0°C. Background throttled; Brightness force reduced to safe state.",
        "limit_alert_title" to "ION SATURATION LIMIT SHIELD ACTIVE",
        "limit_alert_desc" to "Current cap matched target threshold ($1%). Micro-current trickle simulation deployed to maximize cell lifetime.",
        "no_history" to "No recorded charging history yet.",
        "generate_test" to "Generate Test Entry",
        "history_title" to "REGISTRATION CHARGING HISTORY",
        "deep_linux_status" to "Deep Linux Kernel Status & Diagnostics",
        "realtime_log_header" to "HYPERCHARGE SHELL REAL-TIME CONSOLE",
        "no_shell_activity" to "No shell activities logged. Hook system charger or toggle controls to stream live events..."
    )
    
    return if (lang == "ar") ar[key] ?: (en[key] ?: key) else en[key] ?: key
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChargeScreen(viewModel: ChargeViewModel) {
    var activeTab by remember { mutableStateOf(0) }

    // Persistent startup/background crash listener
    val context = LocalContext.current
    var crashLog by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("hypercharge_crash_prefs", Context.MODE_PRIVATE)
        crashLog = prefs.getString("last_crash", null)
    }

    // Collect States
    val batteryPct by viewModel.liveBatteryLevel.collectAsStateWithLifecycle()
    val temp by viewModel.liveTemperature.collectAsStateWithLifecycle()
    val voltageMv by viewModel.liveVoltageMv.collectAsStateWithLifecycle()
    val rawCurrent by viewModel.liveCurrentMa.collectAsStateWithLifecycle()
    val isCharging by viewModel.liveIsCharging.collectAsStateWithLifecycle()
    val pluggedState by viewModel.livePluggedType.collectAsStateWithLifecycle()
    val health by viewModel.liveHealth.collectAsStateWithLifecycle()
    val curveType by viewModel.liveChargeCurveType.collectAsStateWithLifecycle()
    val cpuStatus by viewModel.liveCpuCoreStatus.collectAsStateWithLifecycle()
    val wakelockStatus by viewModel.liveWakelockStatus.collectAsStateWithLifecycle()
    val estFullMinutes by viewModel.liveEstimatedFullMinutes.collectAsStateWithLifecycle()
    val logs by viewModel.liveActivityLogs.collectAsStateWithLifecycle()

    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val recentSamples by viewModel.recentSamples.collectAsStateWithLifecycle()

    val autoDisableRadios by viewModel.autoDisableRadios.collectAsStateWithLifecycle()
    val preventOverheat by viewModel.preventOverheat.collectAsStateWithLifecycle()
    val preserveBatteryHealth by viewModel.preserveBatteryHealth.collectAsStateWithLifecycle()
    val puristAmoledMode by viewModel.puristAmoledMode.collectAsStateWithLifecycle()
    val batteryChargeLimit by viewModel.batteryChargeLimit.collectAsStateWithLifecycle()
    val activeAuditState by viewModel.activeAuditState.collectAsStateWithLifecycle()
    val isRepairing by viewModel.isRepairing.collectAsStateWithLifecycle()
    val repairProgress by viewModel.repairProgress.collectAsStateWithLifecycle()
    val repairStatus by viewModel.repairStatus.collectAsStateWithLifecycle()

    // BILINGUAL & STATS EXTRA INDICES
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val batteryCycles by viewModel.batteryCycles.collectAsStateWithLifecycle()
    val batteryWear by viewModel.batteryWear.collectAsStateWithLifecycle()
    val healthScore by viewModel.healthScore.collectAsStateWithLifecycle()
    val selectedPowerProfile by viewModel.selectedPowerProfile.collectAsStateWithLifecycle()
    val diagnosticReportText by viewModel.diagnosticReportText.collectAsStateWithLifecycle()

    // Interactive custom software cooling triggers simulated
    val softwareCoolingTriggered = preventOverheat && temp >= 38.0f

    // Space Theme Dark colors: Switch to absolute pure bio pitch-black if puristAmoledMode is selected!
    val darkCanvas = if (puristAmoledMode) Color(0xFF000000) else Color(0xFF0C101B)
    val cardBg = if (puristAmoledMode) Color(0xFF0C101B) else Color(0xFF161C2C)
    val accentCyan = Color(0xFF00FFCC)
    val chargerAmber = Color(0xFFFF9F0A)
    val thermalCrimson = Color(0xFFFF453A)
    val textMuted = Color(0xFF8E9EB6)

    if (crashLog != null) {
        AlertDialog(
            onDismissRequest = { /* Modal */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Crash Warning",
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Core Diagnostic Error Report",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "A previous run encountered an uncaught engine or system exception. Please copy details to solve the issue:",
                        color = Color(0xFF8E9EB6),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(Color(0xFF0C101B), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFF453A).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Text(
                            text = crashLog ?: "",
                            color = Color(0xFFFF9F0A),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("HyperCharge Crash Log", crashLog)
                            clipboard.setPrimaryClip(clip)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                ) {
                    Text("Copy Code Log", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val prefs = context.getSharedPreferences("hypercharge_crash_prefs", Context.MODE_PRIVATE)
                        prefs.edit().remove("last_crash").apply()
                        crashLog = null
                    }
                ) {
                    Text("Clear & Dismiss", color = Color(0xFFFF453A))
                }
            },
            containerColor = Color(0xFF161C2C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Fast Charging",
                            tint = accentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = getLabel("title", appLanguage),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = getLabel("subtitle", appLanguage),
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.setAppLanguage(if (appLanguage == "ar") "en" else "ar")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = getLabel("lang_switch", appLanguage),
                            color = accentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkCanvas,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = darkCanvas,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Engine") },
                    label = { Text(getLabel("tab_engine", appLanguage)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentCyan,
                        selectedTextColor = accentCyan,
                        indicatorColor = cardBg,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Schedules") },
                    label = { Text(getLabel("tab_schedules", appLanguage)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentCyan,
                        selectedTextColor = accentCyan,
                        indicatorColor = cardBg,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Kernels") },
                    label = { Text(getLabel("tab_diagnostics", appLanguage)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentCyan,
                        selectedTextColor = accentCyan,
                        indicatorColor = cardBg,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    )
                )
            }
        },
        containerColor = darkCanvas
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(darkCanvas)
                .padding(horizontal = 16.dp)
        ) {
            // Software Cooling Action Banner
            AnimatedVisibility(visible = softwareCoolingTriggered) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = thermalCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Cooling Active",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = getLabel("thermal_alert_title", appLanguage),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = getLabel("thermal_alert_desc", appLanguage),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // EXCLUSIVE: Charging Cap Limit Protection Active Banner
            AnimatedVisibility(visible = isCharging && batteryPct >= batteryChargeLimit) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = accentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Ion Shield engaged",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = getLabel("limit_alert_title", appLanguage),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = getLabel("limit_alert_desc", appLanguage).replace("$1", batteryChargeLimit.toString()),
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            when (activeTab) {
                0 -> EngineDashboard(
                    batteryPct = batteryPct,
                    temp = temp,
                    voltageMv = voltageMv,
                    rawCurrentMa = rawCurrent,
                    isCharging = isCharging,
                    pluggedState = pluggedState,
                    health = health,
                    curveType = curveType,
                    estFullMinutes = estFullMinutes,
                    recentSamples = recentSamples,
                    viewModel = viewModel,
                    cardBg = cardBg,
                    accentCyan = accentCyan,
                    chargerAmber = chargerAmber,
                    thermalCrimson = thermalCrimson,
                    textMuted = textMuted,
                    appLanguage = appLanguage,
                    batteryCycles = batteryCycles,
                    batteryWear = batteryWear,
                    healthScore = healthScore,
                    selectedPowerProfile = selectedPowerProfile
                )
                1 -> PowerAISchedules(
                    autoDisableRadios = autoDisableRadios,
                    preventOverheat = preventOverheat,
                    preserveBatteryHealth = preserveBatteryHealth,
                    puristAmoledMode = puristAmoledMode,
                    batteryChargeLimit = batteryChargeLimit,
                    viewModel = viewModel,
                    sessions = sessions,
                    cardBg = cardBg,
                    accentCyan = accentCyan,
                    textMuted = textMuted,
                    appLanguage = appLanguage,
                    selectedPowerProfile = selectedPowerProfile
                )
                2 -> KernelDiagnosticsConsole(
                    cpuStatus = cpuStatus,
                    wakelockStatus = wakelockStatus,
                    logs = logs,
                    activeAuditState = activeAuditState,
                    isRepairing = isRepairing,
                    repairProgress = repairProgress,
                    repairStatus = repairStatus,
                    viewModel = viewModel,
                    cardBg = cardBg,
                    accentCyan = accentCyan,
                    chargerAmber = chargerAmber,
                    textMuted = textMuted,
                    appLanguage = appLanguage,
                    diagnosticReportText = diagnosticReportText
                )
            }
        }
    }
}

@Composable
fun EngineDashboard(
    batteryPct: Int,
    temp: Float,
    voltageMv: Int,
    rawCurrentMa: Int,
    isCharging: Boolean,
    pluggedState: String,
    health: String,
    curveType: String,
    estFullMinutes: Int,
    recentSamples: List<BatterySamplePoint>,
    viewModel: ChargeViewModel,
    cardBg: Color,
    accentCyan: Color,
    chargerAmber: Color,
    thermalCrimson: Color,
    textMuted: Color,
    appLanguage: String,
    batteryCycles: Int,
    batteryWear: Float,
    healthScore: Float,
    selectedPowerProfile: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            // Main Live Dynamic Liquid Battery Container
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Cosmic Pulse background particles
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = accentCyan.copy(alpha = 0.03f),
                            radius = 400f,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$batteryPct",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCharging) accentCyan else Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                            )
                        }

                        // Charge State Info
                        Text(
                            text = if (isCharging) getLabel("status_charging", appLanguage).uppercase() else getLabel("status_discharging", appLanguage).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCharging) chargerAmber else textMuted,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom animated horizontal battery gauge slider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(22.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(11.dp))
                                .padding(2.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((batteryPct / 100f).coerceIn(0f, 1f))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                accentCyan.copy(alpha = 0.6f),
                                                accentCyan
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Estimated full info
                        if (isCharging && estFullMinutes >= 0) {
                            val estTextEn = "Estimated finished charge time: $estFullMinutes min"
                            val estTextAr = "الوقت المتبقي لاكتمال الشحن: $estFullMinutes دقيقة"
                            Text(
                                text = if (appLanguage == "ar") estTextAr else estTextEn,
                                color = textMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            val plugTextEn = "Plug charger for AI prediction info"
                            val plugTextAr = "قم بتوصيل الشاحن لتفعيل تخمينات الذكاء"
                            Text(
                                text = if (appLanguage == "ar") plugTextAr else plugTextEn,
                                color = textMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // EXCLUSIVE STATS ROW: State of Health (SOH), Cycles, and Cellular Wear
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accentCyan.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getLabel("soh", appLanguage),
                            color = textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$healthScore%",
                            color = accentCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getLabel("wear_level", appLanguage),
                            color = textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$batteryWear%",
                            color = if (batteryWear > 4.0f) thermalCrimson else accentCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getLabel("cycles", appLanguage),
                            color = textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$batteryCycles",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            // Live parameters grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Parameter 1: Temperature
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBg, RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Temperature",
                            tint = if (temp > 38f) thermalCrimson else accentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getLabel("temp", appLanguage), color = textMuted, fontSize = 12.sp)
                        Text(
                            "$temp°C",
                            color = if (temp > 38f) thermalCrimson else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Parameter 2: Instantaneous Milliamperes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBg, RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Power Speed",
                            tint = chargerAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getLabel("current", appLanguage), color = textMuted, fontSize = 12.sp)
                        val currentMa = if (rawCurrentMa != 0) rawCurrentMa else if (isCharging) 3620 else -480
                        Text(
                            "${if (currentMa > 0) "+" else ""}$currentMa mA",
                            color = if (currentMa >= 0) accentCyan else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Parameter 3: Voltage
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBg, RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Voltage",
                            tint = accentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getLabel("voltage", appLanguage), color = textMuted, fontSize = 12.sp)
                        val voltFloat = if (voltageMv > 0) voltageMv / 1000f else 4.12f
                        Text(
                            "%.3f V".format(voltFloat),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Parameter 4: Overall State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBg, RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Plug Connection",
                            tint = textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getLabel("health", appLanguage), color = textMuted, fontSize = 12.sp)
                        val translatedHealth = if (appLanguage == "ar") {
                            when {
                                health.contains("Good") -> "ممتازة ومستقرة"
                                health.contains("Overheat") -> "ارتفاع طارئ للحرارة"
                                health.contains("Dead") -> "تالفة (تتطلب تبديل)"
                                health.contains("Over") -> "جهد زائد مفرط"
                                else -> "سليمة"
                            }
                        } else health
                        
                        Text(
                            translatedHealth,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            // Intelligent Curve details card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        getLabel("profile", appLanguage).uppercase(),
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedPowerProfile == "Eco") getLabel("profile_eco", appLanguage)
                               else if (selectedPowerProfile == "Gaming") getLabel("profile_gaming", appLanguage)
                               else getLabel("profile_balanced", appLanguage),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = getLabel("limit_desc", appLanguage),
                        color = textMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            // Realtime Interactive Charge Wave Graphs (Canvas drawing)
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val graphHeaderAr = "رسم لخط بياني فوري لموجات جهد الشحن"
                    val graphHeaderEn = "LIVE INTEGRATED POWER INTERACTION WAVE"
                    Text(
                        text = if (appLanguage == "ar") graphHeaderAr else graphHeaderEn,
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f).padding(top = 16.dp)
                    ) {
                        val path = Path()
                        val width = size.width
                        val height = size.height

                        path.moveTo(0f, height * 0.7f)
                        if (isCharging) {
                            path.cubicTo(
                                width * 0.25f, height * 0.1f,
                                width * 0.5f, height * 0.9f,
                                width * 0.75f, height * 0.2f
                            )
                            path.lineTo(width, height * 0.4f)
                        } else {
                            path.lineTo(width * 0.3f, height * 0.75f)
                            path.lineTo(width * 0.6f, height * 0.82f)
                            path.lineTo(width, height * 0.85f)
                        }

                        drawPath(
                            path = path,
                            color = accentCyan,
                            style = Stroke(width = 4f)
                        )

                        // Draw background fill gradient
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(accentCyan.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PowerAISchedules(
    autoDisableRadios: Boolean,
    preventOverheat: Boolean,
    preserveBatteryHealth: Boolean,
    puristAmoledMode: Boolean,
    batteryChargeLimit: Int,
    viewModel: ChargeViewModel,
    sessions: List<ChargeSession>,
    cardBg: Color,
    accentCyan: Color,
    textMuted: Color,
    appLanguage: String,
    selectedPowerProfile: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = getLabel("profile", appLanguage),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = getLabel("limit_desc", appLanguage),
                color = textMuted,
                fontSize = 13.sp
            )
        }

        // EXCLUSIVE FEATURE: Smart Silicon Power Profile Selector chips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accentCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val pTitleAr = "تعديل المخطط النشط للنواة"
                    val pTitleEn = "CHIP SILICON GOVERNOR SELECTOR"
                    Text(
                        text = if (appLanguage == "ar") pTitleAr else pTitleEn,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Eco", "Balanced", "Gaming").forEach { profile ->
                            val isSelected = selectedPowerProfile == profile
                            val profileLabel = when (profile) {
                                "Eco" -> getLabel("profile_eco", appLanguage)
                                "Gaming" -> getLabel("profile_gaming", appLanguage)
                                else -> getLabel("profile_balanced", appLanguage)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accentCyan else Color.White.copy(alpha = 0.04f))
                                    .clickable { viewModel.setPowerProfile(profile) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profileLabel,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        getLabel("system_safeguards", appLanguage).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // EXCLUSIVE MODE 1: AMOLED Pure Dark Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    getLabel("pure_amoled", appLanguage),
                                    fontWeight = FontWeight.Bold,
                                    color = accentCyan,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(accentCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("PRO", color = accentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                getLabel("pure_desc", appLanguage),
                                color = textMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = puristAmoledMode,
                            onCheckedChange = { viewModel.setPuristAmoledMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentCyan)
                        )
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // EXCLUSIVE MODE 2: Smart Charge Cap Restrictor Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.85f)) {
                                Text(
                                    getLabel("limit_shield", appLanguage),
                                    fontWeight = FontWeight.Bold,
                                    color = accentCyan,
                                    fontSize = 14.sp
                                )
                                Text(
                                    getLabel("limit_desc", appLanguage),
                                    color = textMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(accentCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$batteryChargeLimit%",
                                    color = accentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = batteryChargeLimit.toFloat(),
                            onValueChange = { viewModel.setBatteryChargeLimit(it.toInt()) },
                            valueRange = 80f..100f,
                            steps = 3, // 80, 85, 90, 95, 100
                            colors = SliderDefaults.colors(
                                thumbColor = accentCyan,
                                activeTrackColor = accentCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Switch 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Text(
                                getLabel("radio_hibernate", appLanguage),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                getLabel("radio_desc", appLanguage),
                                color = textMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = autoDisableRadios,
                            onCheckedChange = { viewModel.setAutoDisableRadios(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentCyan)
                        )
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Switch 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Text(
                                getLabel("thermal_desc", appLanguage),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                getLabel("thermal_desc", appLanguage),
                                color = textMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = preventOverheat,
                            onCheckedChange = { viewModel.setPreventOverheat(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentCyan)
                        )
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Switch 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Text(
                                getLabel("ion_preserve", appLanguage),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                getLabel("ion_desc", appLanguage),
                                color = textMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = preserveBatteryHealth,
                            onCheckedChange = { viewModel.setPreserveBatteryHealth(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentCyan)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    getLabel("history_title", appLanguage).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = getLabel("generate_test", appLanguage),
                    modifier = Modifier
                        .clickable { viewModel.simulateHeavyChargeChargeCycle() }
                        .padding(4.dp),
                    color = accentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty Stats",
                            tint = textMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            getLabel("no_history", appLanguage),
                            color = textMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(sessions) { session ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val sessionLabelAr = "سلسلة شحن رقم #${session.id}"
                            val sessionLabelEn = "Session #${session.id}"
                            Text(
                                text = if (appLanguage == "ar") sessionLabelAr else sessionLabelEn,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val boostLabelAr = "تعزيز المستوى: ${session.startBatteryLevel}% -> ${session.endBatteryLevel}%"
                            val boostLabelEn = "Battery Boosted: ${session.startBatteryLevel}% -> ${session.endBatteryLevel}%"
                            Text(
                                text = if (appLanguage == "ar") boostLabelAr else boostLabelEn,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            val detailsLabelAr = "التسخين الأقصى: ${session.maxTemp}°C | المصدر: ${session.chargerType}"
                            val detailsLabelEn = "Thermal: Max ${session.maxTemp}°C | Cap: ${session.chargerType}"
                            Text(
                                text = if (appLanguage == "ar") detailsLabelAr else detailsLabelEn,
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Safe",
                            tint = accentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KernelDiagnosticsConsole(
    cpuStatus: String,
    wakelockStatus: String,
    logs: List<String>,
    activeAuditState: String,
    isRepairing: Boolean,
    repairProgress: Float,
    repairStatus: String,
    viewModel: ChargeViewModel,
    cardBg: Color,
    accentCyan: Color,
    chargerAmber: Color,
    textMuted: Color,
    appLanguage: String,
    diagnosticReportText: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    getLabel("deep_linux_status", appLanguage),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    getLabel("deep_diagnostics", appLanguage),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = { viewModel.clearHistory() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Status",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // EXCLUSIVE: AI Cell Chemical & Electrolyte Integrity Auditor Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            getLabel("audit_section", appLanguage).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = accentCyan,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            getLabel("ion_preserve", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.runBatteryChemicalAudit() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(getLabel("diagnostics_run", appLanguage).uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Audit Status",
                            tint = chargerAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeAuditState,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // EXCLUSIVE PARTICLE KINETIC MODULE: Interactive Lithium Ion alignment simulator chamber
        IonCalibratorSection(
            isRepairing = isRepairing,
            progress = repairProgress,
            appLanguage = appLanguage,
            accentCyan = accentCyan,
            chargerAmber = chargerAmber,
            cardBg = cardBg,
            textMuted = textMuted
        )

        // EXCLUSIVE: Software Tool to Repair & Calibrate Battery
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, chargerAmber.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            getLabel("calib_section", appLanguage).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = chargerAmber,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            getLabel("repair_engine", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    if (isRepairing) {
                        CircularProgressIndicator(
                            color = chargerAmber,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        val repairNowText = if (appLanguage == "ar") "بدء المعايرة" else "REPAIR NOW"
                        Button(
                            onClick = { viewModel.runBatteryRepair() },
                            colors = ButtonDefaults.buttonColors(containerColor = chargerAmber),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(repairNowText, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isRepairing || repairProgress > 0f) {
                    LinearProgressIndicator(
                        progress = repairProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = chargerAmber,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Repair Status",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = repairStatus,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // EXCLUSIVE ARCHIVE EXPORTER: Issue & Copy Official Calibration Certificate
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            getLabel("cert_result", appLanguage).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = accentCyan,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            getLabel("generate_cert", appLanguage),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { viewModel.generateBatteryCertificate() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        val runCertText = if (appLanguage == "ar") "استخراج" else "EXPORT"
                        Text(runCertText, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                if (diagnosticReportText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, accentCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = diagnosticReportText,
                                color = accentCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("HyperCharge Diagnostics", diagnosticReportText)
                                    clipboard.setPrimaryClip(clip)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(getLabel("cert_copy", appLanguage), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active State Matrices
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val cpuGovernorAr = "حالة مثبت معالج النواة"
                        val cpuGovernorEn = "CPU Core Power Governor"
                        Text(text = if (appLanguage == "ar") cpuGovernorAr else cpuGovernorEn, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        val cpuTranslated = if (appLanguage == "ar") {
                            when {
                                cpuStatus.contains("High") -> "أداء فائق للسرعة"
                                cpuStatus.contains("Restricted") -> "الحد الآمن (مخفض حرارياً)"
                                else -> "متزن ذكي للقرص"
                            }
                        } else cpuStatus
                        Text(cpuTranslated, color = accentCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val wakelockAr = "مدرع حظر عمليات الخلفية"
                        val wakelockEn = "Linux OS Wakelocks Matrix"
                        Text(text = if (appLanguage == "ar") wakelockAr else wakelockEn, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        val wakeTranslated = if (appLanguage == "ar") {
                            when {
                                wakelockStatus.contains("Sleep") -> "سبات كلي مشدد (نشط)"
                                wakelockStatus.contains("Safe") -> "المسار الآمن"
                                else -> "معياري"
                            }
                        } else wakelockStatus
                        Text(wakeTranslated, color = chargerAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Realtime Terminal Logs Shell Simulator
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        getLabel("realtime_log_header", appLanguage).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = accentCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(accentCyan, RoundedCornerShape(5.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color.White.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                getLabel("no_shell_activity", appLanguage),
                                color = textTerminalMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        items(logs) { log ->
                            // Translate common phrases inside shell dynamically
                            var translatedLog = log
                            if (appLanguage == "ar") {
                                translatedLog = translatedLog
                                    .replace("Charger connected! Triggering fast charge optimizations...", "تم توصيل الشاحن! جاري تشغيل محسّنات التيار الأيونية...")
                                    .replace("Charger disconnected. Restoring user system configuration...", "تم فصل الشاحن. جاري فك حظر الخلفية وإلغاء القيود...")
                                    .replace("High Thermal Warning", "⚠️ تحذير حراري مرتفع")
                                    .replace("Suppressing background background load.", "تم خفض عبء المعالجة.")
                                    .replace("Thermal Restricted (Low Power)", "مقيد حرارياً (نمط ترشيد الطاقة)")
                                    .replace("Forced Sleep Active (No Wakelocks)", "سبات إجباري للعمليات (منع التنبيهات)")
                                    .replace("Balanced Charge Governor", "مثبت شحن متزن")
                                    .replace("Simulating Radio Hibernation to lower chipset thermals.", "محاكاة خماد بروتوكولات الراديو لرفع سرعة تدفق الطاقة.")
                                    .replace("Re-aligning 0-100% capacity parameters...", "إعادة معايرة اتزان خلايا الليثيوم كلياً...")
                                    .replace("Halting extreme wakelocks", "حظر العمليات الخفية المستنزفة")
                                    .replace("Purging batterystats.bin", "تنظيف وإعادة كتابة ملف إحصائيات الاندرويد")
                                    .replace("HyperCharge Engine fully initialized!", "محرك شحن الهايبرتشارج جاهز ومكتمل الصلاحيات!")
                            }
                            Text(
                                text = translatedLog,
                                color = if (log.contains("⚠️")) Color.Red else if (log.contains("⚡")) accentCyan else Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IonCalibratorSection(
    isRepairing: Boolean,
    progress: Float,
    appLanguage: String,
    accentCyan: Color,
    chargerAmber: Color,
    cardBg: Color,
    textMuted: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getLabel("ion_alignment_title", appLanguage).uppercase(),
                fontWeight = FontWeight.Bold,
                color = accentCyan,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getLabel("ion_alignment_desc", appLanguage),
                color = textMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "ion_motion")
            val pulseFloat by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw Cathode (-) plate on the left
                    drawRect(
                        color = Color(0xFFFF453A).copy(alpha = 0.2f),
                        topLeft = Offset(0f, 0f),
                        size = Size(15.dp.toPx(), height)
                    )
                    drawLine(
                        color = Color(0xFFFF453A),
                        start = Offset(15.dp.toPx(), 0f),
                        end = Offset(15.dp.toPx(), height),
                        strokeWidth = 2f
                    )

                    // Draw Anode (+) plate on the right
                    drawRect(
                        color = accentCyan.copy(alpha = 0.2f),
                        topLeft = Offset(width - 15.dp.toPx(), 0f),
                        size = Size(15.dp.toPx(), height)
                    )
                    drawLine(
                        color = accentCyan,
                        start = Offset(width - 15.dp.toPx(), 0f),
                        end = Offset(width - 15.dp.toPx(), height),
                        strokeWidth = 2f
                    )

                    // Draw separator in the middle
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(width / 2, 0f),
                        end = Offset(width / 2, height),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    val particles = listOf(
                        Offset(0.15f, 0.25f), Offset(0.25f, 0.65f), Offset(0.35f, 0.45f), Offset(0.42f, 0.75f),
                        Offset(0.55f, 0.15f), Offset(0.65f, 0.82f), Offset(0.72f, 0.35f), Offset(0.85f, 0.60f),
                        Offset(0.20f, 0.12f), Offset(0.32f, 0.88f), Offset(0.48f, 0.38f), Offset(0.60f, 0.52f),
                        Offset(0.78f, 0.70f), Offset(0.88f, 0.20f)
                    )

                    particles.forEachIndexed { index, theoretical ->
                        val driftSpeed = if (isRepairing) 4.0f else 1.0f
                        val offsetRatio = (theoretical.x + pulseFloat * driftSpeed) % 0.8f + 0.1f
                        val xPos = width * offsetRatio
                        val yPos = height * theoretical.y

                        val isFailedNode = index % 3 == 0
                        val isNodeHealed = isRepairing && (progress >= (index.toFloat() / particles.size))
                        val nodeColor = if (isFailedNode && !isNodeHealed) {
                            Color(0xFFFF453A)
                        } else {
                            accentCyan
                        }

                        drawCircle(
                            color = nodeColor.copy(alpha = 0.2f),
                            radius = if (isFailedNode && !isNodeHealed) 10.dp.toPx() else 8.dp.toPx(),
                            center = Offset(xPos, yPos)
                        )

                        drawCircle(
                            color = nodeColor,
                            radius = 3.dp.toPx(),
                            center = Offset(xPos, yPos)
                        )
                    }

                    if (isRepairing) {
                        val waveX = width * progress
                        drawLine(
                            color = chargerAmber.copy(alpha = 0.6f),
                            start = Offset(waveX, 0f),
                            end = Offset(waveX, height),
                            strokeWidth = 4.dp.toPx()
                        )
                        drawRect(
                            color = chargerAmber.copy(alpha = 0.08f),
                            topLeft = Offset(0f, 0f),
                            size = Size(waveX, height)
                        )
                    }
                }

                Text(
                    text = "CAT (-)",
                    color = Color(0xFFFF453A).copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 25.dp, top = 6.dp)
                )

                Text(
                    text = "ANO (+)",
                    color = accentCyan.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 25.dp, top = 6.dp)
                )
            }
        }
    }
}

val textTerminalMuted = Color(0xFFA0AABF)
