package com.example.qryde;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Float.parseFloat;

/**
 * List View class that allows drivers to view their Ride history offline
 * Data stored on cloud too
 */
public class RideHistoryList extends AppCompatActivity {
    ListView rideHistoryList;
    private FirebaseFirestore db;

    String driver;

    String TAG = "RideHistoryList";


    //initializing datalist and its objects
    ArrayList<RideInformation> rideInfoDataList;
    private ArrayList<RideInformation> rideInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history_list);

        db = FirebaseFirestore.getInstance();

        Bundle incomingData = getIntent().getExtras();
        if (incomingData != null) {
            driver = incomingData.getString("driver");
        }


        RideInformation[] RideInfo = {};


        //loading data
//        loadData();

        //initializing the data-list and the list from the view
        rideInfoDataList = new ArrayList<>();
        rideInfoDataList.addAll(Arrays.asList(RideInfo));
        rideHistoryList = findViewById(R.id.ride_history_list);

        //initializing the custom list adaptor
        final ArrayAdapter rideInfoAdapter = new RideInfoAdapter(rideInfoDataList, this);

        rideHistoryList.setAdapter(rideInfoAdapter);

        final CollectionReference collectionReference = db.collection("RideHistories");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            /**
             * sets the rider name, start location,
             * end location and cost amount to instance of available ride.
             * Adds marker to the map according to coordinates and notifies rideAdapter data has changed
             * @param queryDocumentSnapshots
             * @param e
             */
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e){
                rideInfoDataList.clear();
                rideInfoAdapter.notifyDataSetChanged();
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.getData().get("driver").toString().equals(driver)) {
                        RideInformation temp = new RideInformation(
                                doc.getData().get("datetime").toString(),
                                doc.getData().get("rider").toString(),
                                doc.getData().get("amount").toString(),
                                doc.getData().get("startLocation").toString(),
                                doc.getData().get("endLocation").toString());
                        rideInfoDataList.add(temp);
                        rideInfoAdapter.notifyDataSetChanged();
                        Log.d(TAG, "onCreate: " + temp.getDate() + temp.getRider() + temp.getAmount() + temp.getStart() + temp.getDestination());

                    }
                }
            }
        });
    }
//
//    //method for loading data(called onCreate)
//    private void loadData()
//    {
//        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
//        Gson gson = new Gson();
//        String json = sharedPreferences.getString("task list", null);
//        Type type = new TypeToken<ArrayList<RideInformation>>() {}.getType();
//        rideInfo = gson.fromJson(json, type);
//
//        if(rideInfo == null)
//        {
//            rideInfo = new ArrayList<>();
//        }
//    }
}