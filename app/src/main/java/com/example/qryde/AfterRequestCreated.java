package com.example.qryde;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

/**
 * class for functions after request has been created, includes cancel button, ride status listener,
 * confirm button, decline driver button, and active ride converter, linked to firebase
 */
public class AfterRequestCreated extends AppCompatActivity {
    private String TAG = "temp";
    private FirebaseFirestore db;

    private String user;

    private ImageView findingBox;
    private TextView findingText;

    private ImageView driverFoundBox;
    private TextView driverName;
    private TextView driverRating;
    private TextView email;
    private TextView phoneNumber;

    private Button confirm;
    private Button cancel;
    private float amount;

    private boolean isCancelDriver = false;

    private String driver;

    private int animationDuration = 100;
    private static final int REQUEST_CALL = 1;


    ObjectAnimator findingBoxAnimationDown;
    ObjectAnimator findingTextAnimationDown;
    ObjectAnimator driverNameAnimationDown;
    ObjectAnimator driverRatingAnimationDown;
    ObjectAnimator confirmAnimationDown;
    ObjectAnimator cancelAnimationDown;

    ObjectAnimator findingBoxAnimationUp;
    ObjectAnimator findingTextAnimationUp;
    ObjectAnimator driverNameAnimationUp;
    ObjectAnimator driverRatingAnimationUp;
    ObjectAnimator confirmAnimationUp;
    ObjectAnimator cancelAnimationUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_after_request_created);

        findingBox = findViewById(R.id.findingDriverBox);
        findingText = findViewById(R.id.findingText);
        phoneNumber = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);

        driverFoundBox = findViewById(R.id.driverFoundBox);
        driverName = findViewById(R.id.driverName);
        driverRating = findViewById(R.id.driverRating);

        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);

        findingBoxAnimationDown = ObjectAnimator.ofFloat(findingBox, "translationY", 1000f);
        findingBoxAnimationDown.setDuration(animationDuration);

        findingTextAnimationDown = ObjectAnimator.ofFloat(findingText, "translationY", 1000f);
        findingTextAnimationDown.setDuration(animationDuration);

        driverNameAnimationDown = ObjectAnimator.ofFloat(driverName, "translationY", 1000f);
        driverNameAnimationDown.setDuration(animationDuration);

        driverRatingAnimationDown = ObjectAnimator.ofFloat(driverRating, "translationY", 1000f);
        driverRatingAnimationDown.setDuration(animationDuration);

        confirmAnimationDown = ObjectAnimator.ofFloat(confirm, "translationY", 1000f);
        confirmAnimationDown.setDuration(animationDuration);

        cancelAnimationDown = ObjectAnimator.ofFloat(cancel, "translationY", 1000f);
        cancelAnimationDown.setDuration(animationDuration);

        findingBoxAnimationUp = ObjectAnimator.ofFloat(findingBox, "translationY", 0f);
        findingBoxAnimationUp.setDuration(animationDuration);

        findingTextAnimationUp = ObjectAnimator.ofFloat(findingText, "translationY", 0f);
        findingTextAnimationUp.setDuration(animationDuration);

        driverNameAnimationUp = ObjectAnimator.ofFloat(driverName, "translationY", 0f);
        driverNameAnimationUp.setDuration(animationDuration);

        driverRatingAnimationUp = ObjectAnimator.ofFloat(driverRating, "translationY", 0f);
        driverRatingAnimationUp.setDuration(animationDuration);

        confirmAnimationUp = ObjectAnimator.ofFloat(confirm, "translationY", 0f);
        confirmAnimationUp.setDuration(animationDuration);

        cancelAnimationUp = ObjectAnimator.ofFloat(cancel, "translationY", 0f);
        cancelAnimationUp.setDuration(animationDuration);

        confirmAnimationDown.start();

        Bundle incomingData = getIntent().getExtras();
        if (incomingData != null) {
            user = incomingData.getString("username");
        }

        db = FirebaseFirestore.getInstance();
        phoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        driverName.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getUserInfo();
            }
        });


//        db.collection("AvailableRides")
//                .whereEqualTo("rider", user)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
////                                startLocation.setText(document.getData().get("startLocation").toString());
////                                endLocation.setText(document.getData().get("endLocation").toString());
//
//                            }
//                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
        setWindowSize();
        rideStatusListener();
        declineDriverButton();
        cancelButton();
        confirmButton();
        activeRideConverter();
    }


    private void activeRideConverter() {
        // listening for when activeRideRequest is changed to true
        db.collection("ActiveRides").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            /**
             * This method calls the GenerateQRCode class when the status of a ride is changed to true
             */
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    if (documentSnapshot.getData().get("status").toString().equals("true")) {
                        Intent intent = new Intent(getApplicationContext(), GenerateQRCode.class);
                        String driverUserName = documentSnapshot.getData().get("driver").toString();
                        intent.putExtra("rider", user);
                        intent.putExtra("driver", driverName.getText().toString());
                        intent.putExtra("driver_user_name", driverUserName);
                        intent.putExtra("amount", amount);

                        startActivity(intent);
                        finish();


                    }
                }
            }
        });
    }


    private void rideStatusListener() {
        db.collection("AvailableRides").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            /**
             * This method listens for when the status of a ride changes
             */
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    if (documentSnapshot.getData().get("status").toString().equals("true")) {
                        findingBoxAnimationDown.start();
                        findingTextAnimationDown.start();
                        driverNameAnimationDown.start();
                        driverRatingAnimationDown.start();
                        cancelAnimationDown.start();

                        //Querying AvailableRides collection with the rider's name
                        db.collection("AvailableRides").whereEqualTo("rider", user).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                driver = document.getData().get("driver").toString();

                                                Log.d(TAG, driver);

                                                db.collection("Users").whereEqualTo("username", driver)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            /**
                                                             * listener to when ride is complete
                                                             * @param task
                                                             */
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                                        //Set the TextViews with the info retrieved from the queried document
                                                                        float likes = parseFloat(document.getData().get("thumbsUp").toString());
                                                                        float dislikes = parseFloat(document.getData().get("thumbsDown").toString());
                                                                        float rating = (likes/(dislikes+likes)*100);
                                                                        DecimalFormat df = new DecimalFormat("#.#");
                                                                        //setting
                                                                        driverName.setText(document.getData().get("name").toString());
                                                                        driverRating.setText("Rating: " + df.format(likes / (dislikes+likes) * 100)  + "%");
                                                                        findingText.setText("Driver found!");
                                                                        cancel.setText(" DECLINE ");
                                                                        phoneNumber.setText(document.getData().get("phoneNumber").toString());
                                                                        email.setText(document.getData().get("email").toString());

                                                                        isCancelDriver = true;

                                                                        findingBoxAnimationUp.start();
                                                                        findingTextAnimationUp.start();
                                                                        driverNameAnimationUp.start();
                                                                        driverRatingAnimationUp.start();
                                                                        confirmAnimationUp.start();
                                                                        cancelAnimationUp.start();


                                                                    }
                                                                } else {
                                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                            }

                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });


                    }
                }
            }
        });
    }


    private void declineDriverButton() {
        View.OnClickListener declineDriverOnClickListener = new View.OnClickListener() {
            /**
             * This method listens for when the decline button is clicked and deletes the driver value
             * and converts the status of the ride to true in the AvailableRides document
             * @param v This is the view to be pressed
             */
            @Override
            public void onClick(View v) {
                email.setVisibility(View.GONE);
                phoneNumber.setVisibility(View.GONE);
                db.collection("AvailableRides").document(user)
                        .update("driver", "")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Successfully deleted document");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to delete document");
                            }
                        });

                db.collection("AvailableRides").document(user)
                        .update("status", true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Successfully deleted document");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to delete document");
                            }
                        });

            }
        };
    }

    private void cancelButton() {
//        View.OnClickListener cancelOnClickListener = new View.OnClickListener() {
        cancel.setOnClickListener(new View.OnClickListener() {
            /**
             * This method listens for when the cancel button is pressed and deletes the ride from the
             * AvailableRides document when the button is pressed
             * @param v This is the view to be clicked
             */
            @Override
            public void onClick(View v) {

                db.collection("ActiveRides").document(user)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });

                if (!isCancelDriver) {
                    db.collection("AvailableRides").document(user)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: Successfully deleted document");
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Failed to delete document");
                                }
                            });
                } else {
                    db.collection("AvailableRides").document(user)
                            .update("driver", "")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: Successfully deleted document");

                                    db.collection("AvailableRides").document(user)
                                            .update("status", false)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess: Successfully deleted document");

                                                    findingBoxAnimationDown.start();
                                                    findingTextAnimationDown.start();
                                                    driverNameAnimationDown.start();
                                                    driverRatingAnimationDown.start();
                                                    cancelAnimationDown.start();
                                                    confirmAnimationDown.start();


                                                    driverName.setText("");
                                                    driverRating.setText("");
                                                    phoneNumber.setText("");
                                                    email.setText("");
                                                    findingText.setText("Finding you a driver ...");
                                                    cancel.setText("Cancel");

                                                    isCancelDriver = false;

                                                    findingBoxAnimationUp.start();
                                                    findingTextAnimationUp.start();
                                                    driverNameAnimationUp.start();
                                                    driverRatingAnimationUp.start();
                                                    cancelAnimationUp.start();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: Failed to delete document");
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Failed to delete document");
                                }
                            });

                }

            }
        });
    }


    private void confirmButton() {
        confirm.setOnClickListener(new View.OnClickListener() {
            /**
             * This method listens for when the the confirm button is pressed and
             * sets the status of the ride to true in the ActiveRides document in firebase
             * @param v This is the button view to be pressed
             */
            @Override
            public void onClick(View v) {
                // get all of the information from AvailableRides to migrate to ActiveRides
                if (confirm.getText().toString().equals("confirm")) {
                    db.collection("AvailableRides")
                            .whereEqualTo("rider", user)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String old_amount = document.getData().get("amount").toString();
                                            String old_datetime = document.getData().get("datetime").toString();
                                            String old_driverName = document.getData().get("driver").toString();
                                            String old_endLocation = document.getData().get("endLocation").toString();
                                            String old_startLocation = document.getData().get("startLocation").toString();
                                            String old_rider = document.getData().get("rider").toString();

                                            Map<String, Object> data = new HashMap<>();
                                            data.put("amount", Float.parseFloat(old_amount));
                                            data.put("datetime", old_datetime);
                                            data.put("driver", old_driverName);
                                            data.put("endLocation", old_endLocation);
                                            data.put("startLocation", old_startLocation);
                                            data.put("rider", old_rider);
                                            data.put("status", false);
                                            db.collection("ActiveRides").document(user).set(data);

                                            amount = Float.parseFloat(old_amount);

                                            // now change the text to ride in progress
                                            findingBoxAnimationDown.start();
                                            findingTextAnimationDown.start();
                                            driverNameAnimationDown.start();
                                            driverRatingAnimationDown.start();
                                            cancelAnimationDown.start();
                                            confirmAnimationDown.start();


                                            findingText.setText("Ride is currently in progress");
                                            cancel.setText("Cancel");
                                            confirm.setText("Ride Complete");

                                            isCancelDriver = false;

                                            findingBoxAnimationUp.start();
                                            findingTextAnimationUp.start();
                                            driverNameAnimationUp.start();
                                            driverRatingAnimationUp.start();
                                            cancelAnimationUp.start();
                                            confirmAnimationUp.start();

                                            // delete the document in AvailableRides
                                            db.collection("AvailableRides").document(user)
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "onSuccess: Successfully deleted document");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: Failed to delete document");
                                                        }
                                                    });

                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                } else if (confirm.getText().toString().equals("Ride Complete")) {
                    db.collection("ActiveRides").document(user)
                            .update("status", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: Successfully deleted document");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Failed to delete document");
                                }
                            });

                }

            }
        });
    }

    /**
     * This method sets the display dimensions
     */
    private void setWindowSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout(width, (height/9)*4);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    /**
     * This method allows the user to send emails to an email address attached to the
     * driver's account
     */
    private void sendEmail(){
        //Sending an email via an intent
        //citation: Coding in Flow, How to Send an Email via Intent - Android Studio Tutorial, https://www.youtube.com/watch?v=tZ2YEw6SoBU&feature=emb_title
        String[] recipients = {email.getText().toString()};
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an email client"));
    }

    /**
     * This method takes transfers the drivers information to the UserInfo class
     */
    private void getUserInfo(){
        //passes the driver's name to UserInfo class
        Intent intent = new Intent(getApplicationContext(), UserInfo.class);
        intent.putExtra("name", driver);
        startActivity(intent);

    }

    /**
     * This method allows the user dial a phone number attached to a drivers' account
     */
    private void makePhoneCall(){
        //makes a phone call using the number in the phoneNumber TextView
        //citation: How to Make a Phone Call from Your App (+ Permission Request) - Android Studio Tutorial, https://www.youtube.com/watch?v=UDwj5j4tBYg&
        String number = phoneNumber.getText().toString();
        if (number.trim().length() > 0) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }

        } else {
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The app uses this method to ask for permission to use the phone
     * @param requestCode The request code to be passed
     * @param permissions The permission to be granted. Not null
     * @param grantResults This grants results to corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //citation: How to Make a Phone Call from Your App (+ Permission Request) - Android Studio Tutorial, https://www.youtube.com/watch?v=UDwj5j4tBYg&
        //Requests the phone permission if needed
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
