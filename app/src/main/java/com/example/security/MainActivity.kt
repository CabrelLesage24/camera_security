import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri

    private val requestCameraPermissionLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data ?: return

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraButton = findViewById(R.id.cameraButton)
        imageView = findViewById(R.id.imageView)

        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                startCameraIntent()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun startCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val file = createImageFile()
            if (file != null) {
                imageUri = FileProvider.getUriForFile(this, packageName+".fileprovider", file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                requestCameraPermissionLauncher.launch(intent)
            }
        }
    }

    private fun createImageFile(): File? {
        val imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (imageDir != null) {
            val imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg"
            val file = File(imageDir, imageFileName)
            return file
        }
        return null
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 100
    }
}
