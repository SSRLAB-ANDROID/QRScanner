package by.ssrlab.qr_scanner;

import android.content.pm.PackageManager;
import android.media.Image;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.List;
import java.util.concurrent.ExecutionException;
import by.ssrlab.qr_scanner.databinding.ActivityMainBinding;

public class Helper {

    private ListenableFuture<ProcessCameraProvider> cplf; //camera provider listenable feature

    private void requestPermission(MainActivity activity) {
        String[] permissions = { android.Manifest.permission.CAMERA };
        ActivityCompat.requestPermissions(activity, permissions, PackageManager.PERMISSION_GRANTED);
    }

    private void init(MainActivity activity) {
        cplf = ProcessCameraProvider.getInstance(activity);

        cplf.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cplf.get();
                bindImageAnalysis(cameraProvider, activity);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    private void bindImageAnalysis(ProcessCameraProvider cameraProvider, MainActivity activity) {
        ActivityMainBinding binding = activity.getBinding();
        ImageAnalysis ia = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        final boolean[] isAnimationStarted = { false };

        ia.setAnalyzer(ContextCompat.getMainExecutor(activity), new ImageAnalysis.Analyzer() {
            @Override
            @OptIn(markerClass = ExperimentalGetImage.class)
            public void analyze(@NonNull ImageProxy image) {
                Image mediaImage = image.getImage();

                if (mediaImage != null) {
                    InputImage image2 = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                    BarcodeScanner scanner = BarcodeScanning.getClient();

                    Task<List<Barcode>> results = scanner.process(image2);
                    results.addOnSuccessListener(barcodes -> {
                        for (Barcode barcode: barcodes) {
                            final String value = barcode.getRawValue();

                            activity.runOnUiThread(() -> {
                                if (!isAnimationStarted[0]) {
                                    binding.scannedHolder.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scanned_anim));
                                    isAnimationStarted[0] = true;

                                    binding.scannedHolder.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {}
                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            isAnimationStarted[0] = false;
                                        }
                                        @Override
                                        public void onAnimationRepeat(Animation animation) {}
                                    });
                                }

                                binding.scannedText.setText(value);
                            });
                        }

                        image.close();
                        mediaImage.close();
                    });
                }
            }
        });

        Preview preview = new Preview.Builder().build();
        CameraSelector cs = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(binding.scannerPreview.getSurfaceProvider());

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(activity, cs, ia, preview);
    }
}