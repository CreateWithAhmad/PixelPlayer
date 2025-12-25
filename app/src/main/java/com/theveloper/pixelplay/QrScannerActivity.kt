package com.theveloper.pixelplay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class QrScannerActivity : AppCompatActivity() {

    private lateinit var scannerLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val out = Intent().apply { putExtra("SCAN_RESULT", result.contents) }
                setResult(Activity.RESULT_OK, out)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }

        val options = ScanOptions().apply {
            setPrompt("Scan playlist QR")
            setBeepEnabled(false)
            setOrientationLocked(false)
        }

        scannerLauncher.launch(options)
    }
}
