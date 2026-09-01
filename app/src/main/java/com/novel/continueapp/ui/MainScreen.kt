package com.novel.continueapp.ui

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novel.continueapp.model.Book
import com.novel.continueapp.service.ScreenCaptureService
import com.novel.continueapp.ui.theme.Primary
import com.novel.continueapp.ui.theme.Secondary
import com.novel.continueapp.ui.theme.Tertiary
import java.text.SimpleDateFormat
import java.util.*

// ════════════════════════════════════════════════
//  主框架
// ════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GradientTopBar(
                title = "小说续写",
                subtitle = if (viewModel.currentBook != null) "当前：${viewModel.currentBookTitle}" else "小说识别与续写助手"
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.CenterFocusStrong, contentDescription = "捕获") },
                    label = { Text("捕获", fontSize = 11.sp) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Book, contentDescription = "小说") },
                    label = { Text("小说", fontSize = 11.sp) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = "续写") },
                    label = { Text("续写", fontSize = 11.sp) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "设置") },
                    label = { Text("设置", fontSize = 11.sp) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> CaptureTab(viewModel)
                1 -> BookTab(viewModel)
                2 -> ContinueTab(viewModel)
                3 -> SettingsTab(viewModel)
            }
        }
    }
}

@Composable
private fun GradientTopBar(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Primary, Secondary, Tertiary)
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ════════════════════════════════════════════════
//  通用小组件
// ════════════════════════════════════════════════

@Composable
private fun SectionTitle(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun StatusChip(text: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ════════════════════════════════════════════════
//  Tab 1: 捕获
// ════════════════════════════════════════════════

@Composable
private fun CaptureTab(viewModel: MainViewModel) {
    val context = LocalContext.current

    DisposableEffect(viewModel) {
        ScreenCaptureService.onCaptured = { bitmap ->
            if (bitmap != null) {
                viewModel.doOcrOnBitmap(bitmap)
            } else {
                viewModel.errorMessage = "截图失败，请重试"
            }
        }
        onDispose { ScreenCaptureService.onCaptured = null }
    }

    val screenshotLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            viewModel.errorMessage = "未获得截图授权"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 服务状态卡
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (viewModel.isServiceRunning)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (viewModel.isServiceRunning) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = if (viewModel.isServiceRunning)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (viewModel.isServiceRunning) "无障碍服务运行中" else "无障碍服务未启动",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (viewModel.isServiceRunning) {
                        Text(
                            text = "当前应用：${viewModel.currentPackage.ifEmpty { "未知" }}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (!viewModel.isServiceRunning) {
                    Button(onClick = { viewModel.openAccessibilitySettings() }) {
                        Text("去开启")
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 悬浮窗控制卡
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PictureInPictureAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("悬浮窗控制", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = viewModel.isFloatWindowShowing,
                        onCheckedChange = { viewModel.toggleFloatWindow(context) }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when {
                        viewModel.isFloatWindowShowing && viewModel.isFloatCapturing ->
                            "识别中：屏幕文字会自动捕获并保存到当前小说"
                        viewModel.isFloatWindowShowing ->
                            "已暂停：点击悬浮窗按钮继续识别"
                        else ->
                            "开启后在屏幕边缘显示控制按钮，可在任何界面启动/暂停识别"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 原文编辑区
        SectionTitle("捕获的原文", Icons.Outlined.Description)
        OutlinedTextField(
            value = viewModel.editableSource,
            onValueChange = { viewModel.updateSourceText(it) },
            label = { Text("识别到的文字会自动追加到这里") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            maxLines = Int.MAX_VALUE,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(Modifier.height(12.dp))

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = { viewModel.openAccessibilitySettings() },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("刷新捕获")
            }
            FilledTonalButton(
                onClick = {
                    if (!viewModel.ocrFallback) {
                        viewModel.errorMessage = "截图 OCR 兜底已关闭，请先在设置中开启"
                        return@FilledTonalButton
                    }
                    try {
                        val mpm = context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        val intent = mpm.createScreenCaptureIntent()
                        screenshotLauncher.launch(intent)
                    } catch (e: Exception) {
                        viewModel.errorMessage = "截图启动失败: ${e.message}"
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("截图 OCR")
            }
        }

        if (viewModel.errorMessage.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = viewModel.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(14.dp))

        // 使用说明
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("使用说明", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "1. 开启无障碍服务，打开悬浮窗开关\n" +
                            "2. 在小说 App 里翻页，文字会自动捕获并保存\n" +
                            "3. 读不到时点「截图 OCR」手动捕获\n" +
                            "4. 确认文字无误后，切到「续写」标签续写",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Tab 2: 小说管理
// ════════════════════════════════════════════════

@Composable
private fun BookTab(viewModel: MainViewModel) {
    var showNewBookDialog by remember { mutableStateOf(false) }
    var newBookTitle by remember { mutableStateOf("") }

    if (showNewBookDialog) {
        AlertDialog(
            onDismissRequest = { showNewBookDialog = false },
            title = { Text("新建小说", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newBookTitle,
                    onValueChange = { newBookTitle = it },
                    label = { Text("小说名称") },
                    placeholder = { Text("留空则使用「未命名小说」") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createBook(newBookTitle.ifBlank { "未命名小说" })
                    newBookTitle = ""
                    showNewBookDialog = false
                }) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showNewBookDialog = false }) { Text("取消") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 当前小说信息卡
        if (viewModel.currentBook != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Primary, Secondary)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = viewModel.currentBookTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusChip("原文 ${viewModel.currentBook?.sourceLength ?: 0} 字", true)
                        StatusChip("续写 ${viewModel.currentBook?.continueLength ?: 0} 字", true)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.currentBookTitle,
                onValueChange = { viewModel.updateBookTitle(it) },
                label = { Text("修改书名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
        } else {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "📚 还没有小说，点击下方按钮新建一本",
                    modifier = Modifier.padding(18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 新建按钮
        Button(
            onClick = { showNewBookDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Outlined.Book, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("新建小说", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        // 小说列表
        if (viewModel.bookList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有小说，点击上方按钮新建", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            SectionTitle("全部小说")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(viewModel.bookList, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        isSelected = viewModel.currentBook?.id == book.id,
                        onClick = { viewModel.selectBook(book) },
                        onDelete = { viewModel.deleteBook(book) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookCard(book: Book, isSelected: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Book,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = book.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    StatusChip("当前", true)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("原文 ${book.sourceLength} 字", false)
                StatusChip("续写 ${book.continueLength} 字", false)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = book.capturedText.take(80).replace("\n", " ") +
                        if (book.capturedText.length > 80) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(book.updatedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Tab 3: 续写
// ════════════════════════════════════════════════

@Composable
private fun ContinueTab(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 当前小说信息
        if (viewModel.currentBook != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Secondary, Tertiary)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 ${viewModel.currentBookTitle}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "原文 ${viewModel.currentBook?.sourceLength ?: 0} 字",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // 原文预览
        SectionTitle("原文", Icons.Outlined.Description)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = viewModel.editableSource.ifEmpty { "(暂无文本，请先在捕获页面获取或粘贴原文)" },
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (viewModel.editableSource.isBlank())
                    MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(14.dp))

        // 续写按钮
        Button(
            onClick = { viewModel.doContinue() },
            enabled = !viewModel.isContinuing && viewModel.editableSource.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            if (viewModel.isContinuing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(10.dp))
            } else {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(if (viewModel.isContinuing) "续写中…" else "开始续写", fontSize = 16.sp)
        }

        if (viewModel.errorMessage.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = viewModel.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 续写结果
        if (viewModel.continueResult.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("续写结果", Icons.Outlined.AutoAwesome)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = viewModel.continueResult,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Tab 4: 设置
// ════════════════════════════════════════════════

@Composable
private fun SettingsTab(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionTitle("LLM 接口配置", Icons.Outlined.Language)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = viewModel.baseUrl,
                    onValueChange = { viewModel.baseUrl = it },
                    label = { Text("接口地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.apiKey,
                    onValueChange = { viewModel.apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.model,
                    onValueChange = { viewModel.model = it },
                    label = { Text("模型名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionTitle("续写参数", Icons.Outlined.Tune)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("温度", style = MaterialTheme.typography.bodyMedium)
                    StatusChip("${viewModel.temperature}", false)
                }
                Slider(
                    value = viewModel.temperature,
                    onValueChange = { viewModel.temperature = it },
                    valueRange = 0f..2f,
                    steps = 19
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "续写长度与输出上限：不限",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "不限制字数与 token，按需输出全部内容。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.styleHint,
                    onValueChange = { viewModel.styleHint = it },
                    label = { Text("风格要求（可选）") },
                    placeholder = { Text("如：保持文风，多写心理描写") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionTitle("功能开关", Icons.Outlined.ToggleOn)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                SwitchSettingRow(
                    title = "自动捕获屏幕文字",
                    checked = viewModel.autoCapture,
                    onChange = { viewModel.autoCapture = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                SwitchSettingRow(
                    title = "截图 OCR 兜底",
                    checked = viewModel.ocrFallback,
                    onChange = { viewModel.ocrFallback = it }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.targetPackage,
            onValueChange = { viewModel.targetPackage = it },
            label = { Text("仅监听指定包名（可选）") },
            placeholder = { Text("如 com.example.reader，留空则监听所有 App") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.saveSettings() },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("保存设置", fontSize = 16.sp)
        }
    }
}

@Composable
private fun SwitchSettingRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.SmartToy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}