package com.example.vivlio.Activities;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.vivlio.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the location activity. It handles two current cases, one where a user is setting
 * the location of where they want a borrower to pick up the book, and one where a borrower is
 * trying to view the location of where the book pickup location is. If the call is from the lender,
 * this function will return an intent containing the longitute and latitude of the selected
 * location when a confirm button is pressed, and if the call is from the borrower, both cancel and
 * confirm buttons will finish the activity
 */
public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private Boolean looker;
    private Button cancelButton;
    private Button confirmButton;
    private Button doneButton;
    private Boolean changed;
    private int checker;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;
    private String isbn;
    private String borrower;

    /**
     * LocationActivity constructor for the borrower
     * @param longitude
     * @param latitude
     */
    public LocationActivity(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
        looker = true;
    }

    /**
     * LocationActivity constructor for the lender with a preset location shown to be
     * West Edmonton Mall
     */
    public LocationActivity(){
        looker = false;
        changed = false;

    }

    /**
     * Handles the creation of LocationActivity, creates a supportmapgragment, and syncs.
     * This also sets the onclicklisteners for the cancel and confirm buttons
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cancelButton = findViewById(R.id.cancel_button);
        confirmButton = findViewById(R.id.confirm_button);
        doneButton = findViewById(R.id.done_button);
        Bundle b = getIntent().getExtras();
        if (b != null){
            checker = b.getInt("check");
            isbn = b.getString("isbn");
            borrower = b.getString("borrower");
            longitude = b.getDouble("long");
            latitude = b.getDouble("lat");
            if (checker == 1){
                looker = true;
                doneButton.setVisibility(View.VISIBLE);
            }else{
                confirmButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        }
        cancelButton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        }));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTask();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     *this function is called for lenders who want to set their location. Each time the user clicks
     * on the map, the screen will zoom in on the selected area and place a marker down, keeping
     * track of where the marker is
     */
    public void getLocation(){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(latLng.latitude + ":" + latLng.longitude);
                longitude = latLng.longitude;
                latitude = latLng.latitude;
                options.draggable(true);
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                mMap.addMarker(options);
                changed = true;
            }
        });
    }

    /**
     *this function checks for 3 cases, if the user is a lender who has not selected a spot on the
     * map (case 2), if the user is a lender who has selected a spot on the map (case 1), and if
     * the user is a borrower, before returning/displaying the respective results, including
     * returning an intent with the latitude and longitude if the user is a lender who has selected
     * a spot on the map
     */
    public void checkTask(){
        if (changed == true){
            Intent intent = new Intent();
            ArrayList<Double> latLong = new ArrayList<>();
            latLong.add(longitude);
            latLong.add(latitude);
            intent.putExtra("latLong arraylist", latLong);
            setResult(RESULT_OK, intent);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser Firebaseuser = mAuth.getCurrentUser();
            final String uid = Firebaseuser.getUid();
            firebaseUser = mAuth.getCurrentUser();



            DocumentReference docRef = db.collection("users")
                    .document(uid + "/owned/" + isbn);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    GeoPoint geo = new GeoPoint(latitude,longitude);

                    db.collection("users").document(uid + "/owned/" + isbn)
                            .update(
                                    "location", geo
                            );
                }

            });


            if (!borrower.equals(uid)) {


                DocumentReference doc = db.collection("users")
                        .document(borrower + "/requested/" + isbn);
                doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        GeoPoint geo = new GeoPoint(latitude, longitude);

                        db.collection("users").document(borrower + "/requested/" + isbn)
                                .update(
                                        "location", geo
                                );
                    }

                });


            }

            finish();


        }else if (!looker){
            Toast.makeText(this,"You have not selected a location yet", Toast.LENGTH_SHORT).show();
        }else{
            finish();
        }
    }

    /**
     *this function is called when the map is ready to be used, and will determine whether the user
     * is a borrower (where a set location will be displayed), or a lender (where getLOcation will
     * be called to allow the user to select a location)
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (looker == false){
            getLocation();
        }else{

            Log.i("lat", String.valueOf(latitude));
            Log.i("long", String.valueOf(longitude));


            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                List<Address> addresses =
                        geocoder.getFromLocation(latitude, longitude, 1);



                String location = addresses.get(0).getLocality()+ ":"
                        + addresses.get(0).getCountryName();
                LatLng latLng = new LatLng(latitude, longitude);

                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.setMaxZoomPreference(20);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));

            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                LatLng latlong = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(latlong));
                mMap.setMaxZoomPreference(20);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 12.0f));
            }
        }
    }
}