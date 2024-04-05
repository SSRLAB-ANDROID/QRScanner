package by.ssrlab.qrscanner

import android.Manifest
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.ssrlab.qrscanner.databinding.ActivityMainBinding
import by.ssrlab.qrscanner.databinding.DialogOpenBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        checkPermissions()
        initTextAction()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            initJNI()
        else requestPermissionJNI()
    }

    private fun initTextAction() {
        binding.apply {
            scannedTextRipple.setOnClickListener {
                if (scannedText.text.isNotEmpty()) {
                    initDialog(scannedText.text.toString())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            initJNI()
    }

    private fun initDialog(text: String) {
        val dialog = Dialog(this@MainActivity)
        val dialogBinding = DialogOpenBinding.inflate(LayoutInflater.from(this@MainActivity))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        dialogBinding.apply {
            if (text.matches(Regex("^https?://\\S+\\.[a-zA-Zа-яА-Я]{2,5}.*"))) {
                dialogOpen.visibility = View.VISIBLE
                dialogOpen.setOnClickListener {
                    openWebLinkAction(text)
                    dialog.dismiss()
                }
            }

            dialogCopy.setOnClickListener {
                copyTextAction(text)
                dialog.dismiss()
            }

            dialogText.text = text
        }

        val width = resources.displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = width - (width / 10)
        dialog.window?.attributes = layoutParams

        dialog.show()
    }

    private fun copyTextAction(text: String) {
        val clipBoardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("clipboard_qrscanner", text)
        clipBoardManager.setPrimaryClip(clip)
        currentFocus?.clearFocus()
    }

    private fun openWebLinkAction(text: String) {
        val webPage = Uri.parse(text)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        startActivity(intent)
    }

    fun getBinding() = binding

    private external fun requestPermissionJNI()
    private external fun initJNI()

    companion object {
        // Used to load the 'qrscanner' library on application startup.
        init {
            System.loadLibrary("qrscanner")
        }
    }
}