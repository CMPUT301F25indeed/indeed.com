/**
 * Fragment responsible for scanning QR codes for event check-in.
 *
 * This class launches the ZXing QR scanner immediately upon opening,
 * extracts the event ID from the scanned QR content, and navigates to
 * the EventDetailsFragment using that ID.
 *
 * Behavior:
 * - Automatically opens QR scanner on fragment load
 * - Parses "ID: <eventId>" from the QR data
 * - Navigates to event details if a valid ID is found
 * - Shows an error toast if no event ID exists in the QR code
 */
package com.example.indeedgambling;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRFragment extends Fragment {

    /**
     * Inflates the UI layout for the QR scanner screen.
     *
     * @return inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false);
    }

    /**
     * Called after the view is created.
     * Immediately begins QR scanning on fragment load.
     *
     * @param view inflated fragment view
     * @param savedInstanceState previous state if exists
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        startQRScanner();
    }

    /**
     * Launches the ZXing QR scanner with appropriate settings.
     */
    private void startQRScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setPrompt("Scan event QR code");
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    /**
     * Handles the QR scan result, extracts the event ID,
     * and navigates to the event details screen.
     *
     * Expected QR format contains a line beginning with:
     *   "ID: <eventId>"
     *
     * @param requestCode callback request code
     * @param resultCode result status from scanner
     * @param data scanned QR data
     */
    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        IntentResult result = IntentIntegrator.parseActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (result != null && result.getContents() != null) {

            String scanned = result.getContents();
            String eventId = null;

            String[] lines = scanned.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("ID:")) {
                    eventId = line.replace("ID:", "").trim();
                    break;
                }
            }

            if (eventId == null) {
                Toast.makeText(getContext(),
                        "QR does not contain event ID!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(
                            R.id.action_scanQRFragment_to_eventDetailsFragment,
                            bundle
                    );
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
