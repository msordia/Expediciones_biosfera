package itesm.mx.expediciones_biosfera.behavior.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ir.apend.slider.model.Slide;
import ir.apend.slider.ui.Slider;

import itesm.mx.expediciones_biosfera.R;
import itesm.mx.expediciones_biosfera.entities.models.Destination;
import itesm.mx.expediciones_biosfera.utilities.StringFormatHelper;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class DestinationActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    public static final String DESTINATION_OBJECT = "destination_object";

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    Destination destination;

    ScrollView mainScrollView;
    ImageView transparentImageView;

    TextView tvDescription;
    TextView tvDuration;
    TextView tvPrice;
    TextView tvLocation;

    Button btnReserve;

    Slider sliderImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            destination = (Destination) bundle.getSerializable(DESTINATION_OBJECT);
        }

        configureActionBar();

        findViews();

        configureMap();

        configureSlider();

        setViews();

    }

    private void configureActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String actionBarTitle = destination.getName();
        getSupportActionBar().setTitle(actionBarTitle);
    }

    private void findViews() {
        sliderImages = findViewById(R.id.sliderImages);
        mainScrollView = findViewById(R.id.scroll_view);
        transparentImageView = findViewById(R.id.transparent_image);
        tvDescription = findViewById(R.id.text_description);
        tvDuration = findViewById(R.id.text_duration);
        tvPrice = findViewById(R.id.text_price);
        tvLocation = findViewById(R.id.text_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        btnReserve = findViewById(R.id.btn_rsvp);

    }

    private void configureMap() {
        mapFragment.getMapAsync(this);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    private void configureSlider() {
        if(destination.getImageUrls().size() == 0) {
            sliderImages.setVisibility(View.GONE);
            return;
        }

        List<Slide> slideList = new ArrayList<>();

        for(String imageUrl : destination.getImageUrls()) {
            slideList.add(new Slide(0, imageUrl , getResources().getDimensionPixelSize(R.dimen.slider_image_corner)));

        }

        sliderImages.addSlides(slideList);

    }

    private void setViews() {
        String description = destination.getDescription().replaceAll("\\\\n", "\n\n");
        String duration = String.format(getResources().getString(R.string.duration_text), destination.getDuration());
        String price = StringFormatHelper.getPriceFormat(destination.getPrice(), getResources());

        tvDescription.setText(description);
        tvDuration.setText(duration);
        tvPrice.setText(price);
        tvLocation.setText(destination.getCity() + ", " + destination.getState());

        if(Build.VERSION.SDK_INT >= 26) {
            tvDescription.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

        btnReserve.setOnClickListener(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        LatLng location = new LatLng(destination.getLat(), destination.getLon());

        mMap.addMarker(new MarkerOptions().position(location).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7.0f));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_rsvp:
                startReservation();
                break;
        }
    }

    private void startReservation() {
        Intent intent = new Intent(this, ReservationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ReservationActivity.DESTINATION_OBJECT, destination);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.learn_more_destination, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.google_destination:
                googleDestination();
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void googleDestination() {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, destination.getName()); // query contains search string
        startActivity(intent);
    }

}