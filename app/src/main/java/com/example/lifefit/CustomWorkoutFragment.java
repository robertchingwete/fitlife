package com.example.lifefit;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomWorkoutFragment extends Fragment {

    // Views
    private TextInputEditText etWorkoutName;
    private TextInputEditText etDuration;
    private TextInputEditText etExerciseCount;
    private TextInputEditText etEquipment;
    private TextInputEditText etInstructions;
    private MaterialCardView cvImagePicker;
    private ImageView ivWorkoutImage;
    private LinearLayout llImagePlaceholder;
    private MaterialButton btnCancel;
    private MaterialButton btnCreateWorkout;
    private ProgressBar pbUpload;

    // Image upload variables
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;
    private static final int PERMISSION_REQUEST_CAMERA = 300;
    private static final int PERMISSION_REQUEST_STORAGE = 400;

    private Uri imageUri;
    private String currentPhotoPath;

    // Firebase Storage
    private StorageReference storageRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize views
        initViews(view);

        // Setup listeners
        setupListeners();
    }

    private void initViews(View view) {
        etWorkoutName = view.findViewById(R.id.etWorkoutName);
        etDuration = view.findViewById(R.id.etDuration);
        etExerciseCount = view.findViewById(R.id.etExerciseCount);
        etEquipment = view.findViewById(R.id.etEquipment);
        etInstructions = view.findViewById(R.id.etInstructions);
        cvImagePicker = view.findViewById(R.id.cvImagePicker);
        ivWorkoutImage = view.findViewById(R.id.ivWorkoutImage);
        llImagePlaceholder = view.findViewById(R.id.llImagePlaceholder);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnCreateWorkout = view.findViewById(R.id.btnCreateWorkout);

        // Initialize progress bar
        pbUpload = view.findViewById(R.id.pbUpload);
        if (pbUpload != null) {
            pbUpload.setVisibility(View.GONE);
        }



    }

    private void setupListeners() {
        // Image picker
        cvImagePicker.setOnClickListener(v -> {
            showImageSourceDialog();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            clearForm();
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateToHomeTab();
            }
        });

        // Create workout button
        btnCreateWorkout.setOnClickListener(v -> {
            createWorkout();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Add Workout Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Take photo
                if (checkCameraPermission()) {
                    openCamera();
                }
            } else if (which == 1) {
                // Choose from gallery
                if (checkStoragePermission()) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses different permission
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_STORAGE);
                return false;
            }
        } else {
            // For Android < 13
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure there's a camera activity to handle the intent
        if (cameraIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                // Camera photo taken
                if (imageUri != null) {
                    displayImage(imageUri);
                }
            } else if (requestCode == GALLERY_REQUEST) {
                // Gallery image selected
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    displayImage(imageUri);
                }
            }
        }
    }

    private void displayImage(Uri uri) {
        try {
            Bitmap bitmap;
            ContentResolver contentResolver = requireContext().getContentResolver();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            }

            // Set image to ImageView
            ivWorkoutImage.setImageBitmap(bitmap);
            ivWorkoutImage.setVisibility(View.VISIBLE);
            llImagePlaceholder.setVisibility(View.GONE);

            Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void createWorkout() {
        // Get input values
        String name = etWorkoutName.getText() != null ?
                etWorkoutName.getText().toString().trim() : "";
        String durationStr = etDuration.getText() != null ?
                etDuration.getText().toString().trim() : "";
        String exerciseCountStr = etExerciseCount.getText() != null ?
                etExerciseCount.getText().toString().trim() : "";
        String equipmentStr = etEquipment.getText() != null ?
                etEquipment.getText().toString().trim() : "";
        String instructions = etInstructions.getText() != null ?
                etInstructions.getText().toString().trim() : "";

        // Validate inputs
        if (!validateInputs(name, durationStr, exerciseCountStr, equipmentStr)) {
            return;
        }

        // Parse values
        int duration = Integer.parseInt(durationStr);
        int exerciseCount = Integer.parseInt(exerciseCountStr);

        // Parse equipment list
        List<String> equipmentList = new ArrayList<>();
        if (!equipmentStr.isEmpty()) {
            String[] items = equipmentStr.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    equipmentList.add(trimmed);
                }
            }
        }

        // Disable button and show loading
        btnCreateWorkout.setEnabled(false);
        btnCreateWorkout.setText("Creating...");
        if (pbUpload != null) {
            pbUpload.setVisibility(View.VISIBLE);
        }

        // If image is selected, upload it first
        if (imageUri != null) {
            uploadImageAndCreateWorkout(name, duration, exerciseCount, equipmentList, instructions);
        } else {
            // Create workout without image
            createWorkoutInFirebase(name, duration, exerciseCount, equipmentList, instructions, null);
        }
    }

    private void uploadImageAndCreateWorkout(String name, int duration, int exerciseCount,
                                             List<String> equipmentList, String instructions) {

        // Create a unique filename for the image
        String timestamp = String.valueOf(System.currentTimeMillis());
        String userId = FirebaseAuthHelper.getUserId();
        String filename = "workout_" + userId + "_" + timestamp + ".jpg";

        // Create reference to Firebase Storage
        StorageReference imageRef = storageRef.child("workout_images/" + filename);

        // Upload image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Create workout with image URL
                                String imageUrl = downloadUri.toString();
                                createWorkoutInFirebase(name, duration, exerciseCount,
                                        equipmentList, instructions, imageUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Use placeholder if can't get URL
                                createWorkoutInFirebase(name, duration, exerciseCount,
                                        equipmentList, instructions, null);
                                Toast.makeText(getContext(),
                                        "Image uploaded but couldn't get URL",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Create workout without image
                        createWorkoutInFirebase(name, duration, exerciseCount,
                                equipmentList, instructions, null);
                        Toast.makeText(getContext(),
                                "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createWorkoutInFirebase(String name, int duration, int exerciseCount,
                                         List<String> equipmentList, String instructions,
                                         String imageUrl) {

        // Create workout object
        Workout workout = new Workout(
                "", // ID will be set by FirebaseWorkoutHelper
                name,
                duration,
                exerciseCount,
                equipmentList,
                instructions,
                false
        );

        // If you've added imageUrl to Workout model, uncomment this:
         workout.setImageUrl(imageUrl);

        // Save to Firebase
        FirebaseWorkoutHelper.createWorkout(workout, new FirebaseWorkoutHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Success
                btnCreateWorkout.setEnabled(true);
                btnCreateWorkout.setText("Create Workout");
                if (pbUpload != null) {
                    pbUpload.setVisibility(View.GONE);
                }

                Toast.makeText(getContext(),
                        "Workout '" + name + "' created successfully!",
                        Toast.LENGTH_LONG).show();

                // Clear form
                clearForm();

                // Navigate to Workouts tab
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).navigateToWorkoutsTab();
                }
            }

            @Override
            public void onFailure(String error) {
                // Failure
                btnCreateWorkout.setEnabled(true);
                btnCreateWorkout.setText("Create Workout");
                if (pbUpload != null) {
                    pbUpload.setVisibility(View.GONE);
                }

                Toast.makeText(getContext(),
                        "Failed to create workout: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String name, String durationStr,
                                   String exerciseCountStr, String equipmentStr) {
        // Validate workout name
        if (name.isEmpty()) {
            etWorkoutName.setError("Please enter workout name");
            etWorkoutName.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            etWorkoutName.setError("Workout name must be at least 3 characters");
            etWorkoutName.requestFocus();
            return false;
        }

        // Validate duration
        if (durationStr.isEmpty()) {
            etDuration.setError("Please enter duration");
            etDuration.requestFocus();
            return false;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                etDuration.setError("Duration must be greater than 0");
                etDuration.requestFocus();
                return false;
            }
            if (duration > 300) {
                etDuration.setError("Duration seems too long (max 300 minutes)");
                etDuration.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etDuration.setError("Please enter a valid number");
            etDuration.requestFocus();
            return false;
        }

        // Validate exercise count
        if (exerciseCountStr.isEmpty()) {
            etExerciseCount.setError("Please enter number of exercises");
            etExerciseCount.requestFocus();
            return false;
        }

        try {
            int exerciseCount = Integer.parseInt(exerciseCountStr);
            if (exerciseCount <= 0) {
                etExerciseCount.setError("Must have at least 1 exercise");
                etExerciseCount.requestFocus();
                return false;
            }
            if (exerciseCount > 50) {
                etExerciseCount.setError("Too many exercises (max 50)");
                etExerciseCount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etExerciseCount.setError("Please enter a valid number");
            etExerciseCount.requestFocus();
            return false;
        }

        // Validate equipment
        if (equipmentStr.isEmpty()) {
            etEquipment.setError("Please enter required equipment");
            etEquipment.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        if (etWorkoutName != null) etWorkoutName.setText("");
        if (etDuration != null) etDuration.setText("");
        if (etExerciseCount != null) etExerciseCount.setText("");
        if (etEquipment != null) etEquipment.setText("");
        if (etInstructions != null) etInstructions.setText("");

        // Clear any errors
        if (etWorkoutName != null) etWorkoutName.setError(null);
        if (etDuration != null) etDuration.setError(null);
        if (etExerciseCount != null) etExerciseCount.setError(null);
        if (etEquipment != null) etEquipment.setError(null);

        // Reset image picker
        if (ivWorkoutImage != null) {
            ivWorkoutImage.setVisibility(View.GONE);
            ivWorkoutImage.setImageBitmap(null);
        }
        if (llImagePlaceholder != null) llImagePlaceholder.setVisibility(View.VISIBLE);

        // Clear image variables
        imageUri = null;
        currentPhotoPath = null;
    }
}