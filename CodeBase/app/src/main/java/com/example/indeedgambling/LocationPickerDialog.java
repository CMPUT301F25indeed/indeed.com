package com.example.indeedgambling;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationPickerDialog extends DialogFragment {

    private OnLocationPickedListener listener;
    private GeoPoint selectedPoint;

    public interface OnLocationPickedListener {
        void onLocationPicked(double lat, double lon);
    }

    public LocationPickerDialog(OnLocationPickedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_location_picker);

        MapView map = dialog.findViewById(R.id.mapView);
        Button confirm = dialog.findViewById(R.id.btnConfirm);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);


        //Set to user location. Nicety, not needed.
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(51.05, -114.07)); // Default example

        Marker marker = new Marker(map);
        marker.setTitle("Selected Location");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);

        map.setOnTouchListener((v, event) -> {
            GeoPoint p = (GeoPoint) map.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY()
            );

            marker.setPosition(p);
            selectedPoint = p;
            map.invalidate();

            return false;
        });

        confirm.setOnClickListener(v -> {
            if (selectedPoint != null) {
                listener.onLocationPicked(
                        selectedPoint.getLatitude(),
                        selectedPoint.getLongitude()
                );
            }
            dismiss();
        });

        return dialog;
    }
}

