package com.example.fyp1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class EditRecycleLocationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView locationImage;
    private EditText locationName;
    private EditText companyName;
    private Spinner stateSpinner;
    private Spinner provinceSpinner;
    private EditText postcode;
    private Button saveButton;

    private List<String> statesList;
    private Map<String, List<String>> provincesMap;
    private Map<String, String> postcodeRanges;

    private Uri imageUri;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private String existingLocationId;
    private boolean isNewImageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recycle_location);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Initialize views
        locationImage = findViewById(R.id.locationImage);
        locationName = findViewById(R.id.locationName);
        companyName = findViewById(R.id.CompanyName);
        stateSpinner = findViewById(R.id.stateSpinner);
        provinceSpinner = findViewById(R.id.provinceSpinner);
        postcode = findViewById(R.id.postcode);
        saveButton = findViewById(R.id.saveButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),   // Keep original left padding
                    topInset,                // Set top padding to adjust for status bar or notch
                    view.getPaddingRight(),  // Keep original right padding
                    view.getPaddingBottom()  // Keep original bottom padding
            );

            return insets;
        });

        // Initialize data
        initializeStatesProvincesAndPostcodes();

        // Set up spinners
        setupStateSpinner();
        setupProvinceSpinner();

        locationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validatePostcode()) {

                    saveLocationData();
                }
            }

        });

        // Retrieve and display existing data
        loadExistingLocationData();
    }

    private void initializeStatesProvincesAndPostcodes() {
        statesList = new ArrayList<>();
        provincesMap = new HashMap<>();
        postcodeRanges = new HashMap<>();

        // Add your states, provinces, and postcode ranges here
        // Add states, their provinces, and postcode ranges
        addStateProvincesAndPostcodes("Select State", new String[]{"Select Province"}, "");
        addStateProvincesAndPostcodes("Johor", new String[]{"Batu Pahat", "Johor Bahru", "Kluang", "Kota Tinggi", "Kulai", "Mersing", "Muar", "Pontian", "Segamat", "Tangkak"}, "79000-86999");
        addStateProvincesAndPostcodes("Kedah", new String[]{"Baling", "Bandar Baharu", "Kota Setar", "Kuala Muda", "Kubang Pasu", "Kulim", "Langkawi", "Padang Terap", "Pendang", "Pokok Sena", "Sik", "Yan"}, "05000-09810");
        addStateProvincesAndPostcodes("Kelantan", new String[]{"Bachok", "Gua Musang", "Jeli", "Kota Bharu", "Kuala Krai", "Machang", "Pasir Mas", "Pasir Puteh", "Tanah Merah", "Tumpat"}, "15000-18500");
        addStateProvincesAndPostcodes("Melaka", new String[]{"Alor Gajah", "Jasin", "Melaka Tengah"}, "75000-78000");
        addStateProvincesAndPostcodes("Negeri Sembilan", new String[]{"Jelebu", "Jempol", "Kuala Pilah", "Port Dickson", "Rembau", "Seremban", "Tampin"}, "70000-73509");
        addStateProvincesAndPostcodes("Pahang", new String[]{"Bentong", "Bera", "Cameron Highlands", "Jerantut", "Kuantan", "Lipis", "Maran", "Pekan", "Raub", "Rompin", "Temerloh"}, "25000-28800, 39000-39200, 49000-49100");
        addStateProvincesAndPostcodes("Perak", new String[]{"Bagan Datuk", "Batang Padang", "Kerian", "Kinta", "Kuala Kangsar", "Larut, Matang dan Selama", "Manjung", "Perak Tengah", "Hulu Perak", "Kampar", "Hilir Perak", "Muallim"}, "30000-36810");
        addStateProvincesAndPostcodes("Perlis", new String[]{"Perlis"}, "01000-02800");
        addStateProvincesAndPostcodes("Pulau Pinang", new String[]{"Barat Daya", "Seberang Perai Selatan", "Seberang Perai Tengah", "Seberang Perai Utara", "Timur Laut"}, "10000-14400");
        addStateProvincesAndPostcodes("Sabah", new String[]{"Beaufort", "Beluran", "Keningau", "Kinabatangan", "Kota Belud", "Kota Kinabalu", "Kuala Penyu", "Kudat", "Kunak", "Lahad Datu", "Nabawan", "Papar", "Penampang", "Pitas", "Putatan", "Ranau", "Sandakan", "Semporna", "Sipitang", "Tambunan", "Tawau", "Telupid", "Tengah", "Tenom", "Tongod", "Tuaran"}, "88000-91309");
        addStateProvincesAndPostcodes("Sarawak", new String[]{"Betong", "Bintulu", "Kapit", "Kuching", "Limbang", "Miri", "Mukah", "Samarahan", "Sarikei", "Serian", "Sibu", "Sri Aman"}, "93000-98859");
        addStateProvincesAndPostcodes("Selangor", new String[]{"Gombak", "Hulu Langat", "Hulu Selangor", "Klang", "Kuala Langat", "Kuala Selangor", "Petaling", "Sabak Bernam", "Sepang"}, "40000-48300, 63000-68100");
        addStateProvincesAndPostcodes("Terengganu", new String[]{"Besut", "Dungun", "Hulu Terengganu", "Kemaman", "Kuala Nerus", "Kuala Terengganu", "Marang", "Setiu"}, "20000-24300");
        addStateProvincesAndPostcodes("Kuala Lumpur", new String[]{"Kuala Lumpur"}, "50000-60000");
        addStateProvincesAndPostcodes("Labuan", new String[]{"Labuan"}, "87000-87033");
        addStateProvincesAndPostcodes("Putrajaya", new String[]{"Putrajaya"}, "62000-62988");
        // Add more states as needed
    }

    private void addStateProvincesAndPostcodes(String state, String[] provinces, String postcodeRange) {
        statesList.add(state);
        provincesMap.put(state, new ArrayList<>(Arrays.asList(provinces)));
        postcodeRanges.put(state, postcodeRange);
    }

    private void setupStateSpinner() {
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statesList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = statesList.get(position);
                updateProvinceSpinner(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupProvinceSpinner() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceAdapter);
    }

    private void updateProvinceSpinner(String selectedState) {
        if (provincesMap == null || provincesMap.isEmpty() || !provincesMap.containsKey(selectedState)) {
            // Handle the case where provincesMap is not initialized or does not contain the selected state
            MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                    "Map Not Initialized",
                    "Provinces map is not initialized for selected state",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        List<String> provinces = provincesMap.get(selectedState);
        ArrayAdapter<String> provinceAdapter = (ArrayAdapter<String>) provinceSpinner.getAdapter();
        provinceAdapter.clear();

        // Add provinces to adapter
        if (provinces != null && !provinces.isEmpty()) {
            provinceAdapter.addAll(provinces);
        } else {
            MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                    "No Provinces Available",
                    "No provinces found for selected state",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        }
        provinceAdapter.notifyDataSetChanged();
    }


    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            locationImage.setImageURI(imageUri);
            isNewImageSelected = true;
        }
    }

    private boolean validatePostcode() {
        String selectedState = stateSpinner.getSelectedItem().toString();
        String postcodeStr = postcode.getText().toString().trim();

        if (!postcodeStr.isEmpty() && postcodeStr.matches("\\d+")) {
            int postcodeNum = Integer.parseInt(postcodeStr);
            String[] postcodeRangesForState = postcodeRanges.get(selectedState).split(", ");
            for (String range : postcodeRangesForState) {
                String[] bounds = range.split("-");
                int lowerBound = Integer.parseInt(bounds[0]);
                int upperBound = Integer.parseInt(bounds[1]);
                if (postcodeNum >= lowerBound && postcodeNum <= upperBound) {
                    return true;
                }
            }
        }
        MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                "Invalid Postcode",
                "Invalid postcode for the selected state",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

        return false;
    }

    private void saveLocationData() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                    "Error",
                    "No user logged in",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

            return;
        }
        MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                "Saving data...",
                "Data saved",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

        final String email = currentUser.getEmail();
        final String companyNameStr = companyName.getText().toString().trim();
        final String locationNameStr = locationName.getText().toString().trim();
        final String stateStr = stateSpinner.getSelectedItem().toString();
        final String provinceStr = provinceSpinner.getSelectedItem().toString();
        final String postcodeStr = postcode.getText().toString().trim();

        if (locationNameStr.isEmpty() || stateStr.equals("Select State") || provinceStr.equals("Select Province") || postcodeStr.isEmpty() || companyNameStr.isEmpty()) {
            MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                    "Complete All Fields",
                    "Please fill all fields",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        if (existingLocationId != null) {
            // Update existing location
            if (isNewImageSelected) {
                // Upload new image and update location
                uploadNewImageAndUpdateLocation(existingLocationId, email, locationNameStr, stateStr, provinceStr, postcodeStr, companyNameStr);
            } else {
                // Update location without changing the image
                updateLocationInDatabase(existingLocationId, email, locationNameStr, stateStr, provinceStr, postcodeStr, imageUri.toString(), companyNameStr);
            }
        } else {
            // Create new location
            if (imageUri == null) {
                MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                        "Image Selection Required",
                        "Please select an images",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                return;
            }
            final String locationId = UUID.randomUUID().toString();
            uploadNewImageAndUpdateLocation(locationId, email, locationNameStr, stateStr, provinceStr, postcodeStr, companyNameStr);
        }
    }

    private void uploadNewImageAndUpdateLocation(final String locationId, final String email, final String locationName, final String state, final String province, final String postcode, final String companyName) {
        StorageReference fileReference = mStorage.child("location_images/" + locationId);
        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                updateLocationInDatabase(locationId, email, locationName, state, province, postcode, imageUrl, companyName);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                                "Image Upload Error",
                                "Failed to upload image",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                });
    }

    private void updateLocationInDatabase(String locationId, String email, String locationName, String state, String province, String postcode, String imageUrl, String companyName) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("locationID", locationId);
        locationData.put("email", email);
        locationData.put("location", locationName);
        locationData.put("state", state);
        locationData.put("province", province);
        locationData.put("postcode", postcode);
        locationData.put("imageUrl", imageUrl);
        locationData.put("companyName", companyName);

        mDatabase.child("RecycleLocation").child(locationId).updateChildren(locationData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                                "Location Updated",
                                "Location updated successfully",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        finish(); // Close the activity after updating
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                                "Location Update Failed",
                                "Failed to update location",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                    }
                });
    }

    private void loadExistingLocationData() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                    "Error",
                    "No user logged in",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return;
        }

        String email = currentUser.getEmail();
        mDatabase.child("RecycleLocation").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                existingLocationId = locationSnapshot.child("locationID").getValue(String.class);
                                String locationNameStr = locationSnapshot.child("location").getValue(String.class);
                                String companyNameStr = locationSnapshot.child("companyName").getValue(String.class);
                                String stateStr = locationSnapshot.child("state").getValue(String.class);
                                String provinceStr = locationSnapshot.child("province").getValue(String.class);
                                String postcodeStr = locationSnapshot.child("postcode").getValue(String.class);
                                String imageUrl = locationSnapshot.child("imageUrl").getValue(String.class);

                                // Set the retrieved values to the respective fields
                                locationName.setText(locationNameStr);
                                companyName.setText(companyNameStr);

                                // Check if statesList is initialized before using it
                                if (statesList != null && !statesList.isEmpty()) {
                                    stateSpinner.setSelection(statesList.indexOf(stateStr));
                                    updateProvinceSpinner(stateStr);
                                } else {
                                    // Handle statesList not initialized scenario
                                    // You may want to retry loading data or display an error message
                                    MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                                            "Error",
                                            "States list not initialized" + stateStr,
                                            MotionToastStyle.ERROR,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                                    return;
                                }

                                // Ensure provincesMap is initialized before using it
                                if (provincesMap != null && provincesMap.containsKey(stateStr)) {
                                    provinceSpinner.setSelection(provincesMap.get(stateStr).indexOf(provinceStr));
                                } else {
                                    // Handle provincesMap not initialized scenario
                                    // You may want to retry loading data or display an error message
                                    MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                                            "Error",
                                            "Provinces map not initialized for state: " + stateStr,
                                            MotionToastStyle.ERROR,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                                    return;
                                }

                                postcode.setText(postcodeStr);
                                Picasso.get().load(imageUrl).into(locationImage);

                                // Set imageUri to the retrieved imageUrl for future updates
                                imageUri = Uri.parse(imageUrl);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        MotionToast.Companion.darkToast(EditRecycleLocationActivity.this,
                                "Error",
                                "Failed to load location data",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(EditRecycleLocationActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                    }
                });
    }
}
