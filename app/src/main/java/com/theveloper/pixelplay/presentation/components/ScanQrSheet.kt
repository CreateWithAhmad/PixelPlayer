package com.theveloper.pixelplay.presentation.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

@Composable
fun ScanQrSheet(
    onImportInvite: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inviteText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var resultText by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                stream?.close()
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                val source = RGBLuminanceSource(width, height, pixels)
                val bitmapBinary = BinaryBitmap(HybridBinarizer(source))
                val res = MultiFormatReader().decode(bitmapBinary)
                resultText = res.text
                onImportInvite(res.text)
            } catch (t: Throwable) {
                resultText = "Unable to decode QR"
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == android.app.Activity.RESULT_OK) {
                val scanned = activityResult.data?.getStringExtra("SCAN_RESULT")
                if (!scanned.isNullOrBlank()) {
                    onImportInvite(scanned)
                    onClose()
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                cameraLauncher.launch(android.content.Intent(context, com.theveloper.pixelplay.QrScannerActivity::class.java))
            } else {
                resultText = "Camera permission denied"
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Scan QR Playlist", style = MaterialTheme.typography.titleLarge)
        Text(text = "Pick an image with a QR code or paste the invite string if you have it.", modifier = Modifier.padding(top = 8.dp))

        Button(onClick = {
            val has = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (has == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(android.content.Intent(context, com.theveloper.pixelplay.QrScannerActivity::class.java))
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "Scan with camera")
        }

        Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Pick image to scan")
        }

        OutlinedTextField(
            value = inviteText,
            onValueChange = { inviteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text(text = "Or paste invite string") }
        )

        Button(onClick = {
            if (inviteText.isNotBlank()) {
                onImportInvite(inviteText.trim())
                onClose()
            }
        }, modifier = Modifier.padding(top = 14.dp)) {
            Text(text = "Import")
        }

        resultText?.let {
            Text(text = it, modifier = Modifier.padding(top = 16.dp).fillMaxWidth())
        }

        Button(onClick = onClose, modifier = Modifier.padding(top = 18.dp)) {
            Text(text = "Close")
        }
    }
}
