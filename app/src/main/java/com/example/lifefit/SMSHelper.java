package com.example.lifefit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class SMSHelper {
    public static final int SMS_PERMISSION_CODE = 100;

    // Check if SMS permission is granted
    public static boolean hasSMSPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request SMS permission
    public static void requestSMSPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
    }

    // Handle permission result
    public static void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                  int[] grantResults, Context context) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "SMS permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "SMS permission denied. Using SMS app instead.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Format workout equipment list for SMS
    public static String formatEquipmentList(Workout workout) {
        StringBuilder message = new StringBuilder();
        message.append("🏋️ Equipment needed for: ").append(workout.getName()).append("\n\n");

        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            for (int i = 0; i < workout.getEquipment().size(); i++) {
                message.append("✓ ").append(workout.getEquipment().get(i));
                if (i < workout.getEquipment().size() - 1) {
                    message.append("\n");
                }
            }
        } else {
            message.append("No equipment needed");
        }

        message.append("\n\n📱 Sent from FitLife App");
        return message.toString();
    }

    // Format full workout details for SMS
    public static String formatWorkoutDetails(Workout workout) {
        StringBuilder message = new StringBuilder();
        message.append("🏋️ Workout: ").append(workout.getName()).append("\n\n");
        message.append("⏱️ Duration: ").append(workout.getDuration()).append(" min\n");
        message.append("💪 Exercises: ").append(workout.getExerciseCount()).append("\n\n");

        message.append("Equipment needed:\n");
        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            for (String equip : workout.getEquipment()) {
                message.append("✓ ").append(equip).append("\n");
            }
        } else {
            message.append("✓ No equipment needed\n");
        }

        if (workout.getInstructions() != null && !workout.getInstructions().isEmpty()) {
            message.append("\n📝 Instructions:\n").append(workout.getInstructions());
        }

        message.append("\n\n📱 Sent from FitLife App");
        return message.toString();
    }

    // Format workout reminder for SMS
    public static String formatWorkoutReminder(Workout workout, String reminderText) {
        StringBuilder message = new StringBuilder();
        message.append("⏰ Workout Reminder!\n\n");
        message.append("🏋️ ").append(workout.getName()).append("\n");
        message.append("⏱️ ").append(workout.getDuration()).append(" minutes\n\n");

        if (reminderText != null && !reminderText.isEmpty()) {
            message.append(reminderText).append("\n\n");
        }

        message.append("Equipment to bring:\n");
        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            for (String equip : workout.getEquipment()) {
                message.append("✓ ").append(equip).append("\n");
            }
        } else {
            message.append("✓ No equipment needed\n");
        }

        message.append("\n📱 Sent from FitLife App");
        return message.toString();
    }

    // Send SMS using default SMS app (recommended approach)
    public static void sendSMSViaIntent(Context context, String phoneNumber, String message) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("sms:" + phoneNumber));
            smsIntent.putExtra("sms_body", message);

            // Verify that there's an app to handle this intent
            if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(smsIntent);
            } else {
                Toast.makeText(context, "No SMS app found", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Failed to open SMS app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Send SMS directly (requires runtime permission)
    public static void sendSMSDirect(Context context, String phoneNumber, String message) {
        if (!hasSMSPermission(context)) {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();

            // If message is too long, divide it
            if (message.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }

            Toast.makeText(context, "Message sent successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Validate phone number
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[+]?[0-9]{10,13}$");
    }
}