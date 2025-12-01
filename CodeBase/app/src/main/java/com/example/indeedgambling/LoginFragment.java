/**
 * Handles manual login for Entrants, Organizers, and Admins.
 *
 * Responsibilities:
 * - Authenticates user credentials against Firestore.
 * - Determines user role and loads the correct ViewModel.
 * - For Entrants only, saves deviceId for future auto-login.
 *
 * Notes:
 * - Passwords are stored as SHA-256 hashes and matched directly.
 * - Organizer and Admin login does not use persistent device tracking.
 */
package com.example.indeedgambling;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private FirebaseViewModel vm;
    private EntrantViewModel entrantVM;
    private OrganizerViewModel organizerVM;
    private AdminViewModel adminVM;

    /**
     * Inflates the login layout, initializes ViewModels, and sets button listeners.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText email = view.findViewById(R.id.login_email);
        EditText password = view.findViewById(R.id.login_password);
        Button loginBtn = view.findViewById(R.id.btn_login);
        Button backBtn = view.findViewById(R.id.btn_back);

        backBtn.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        vm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        adminVM = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        FirebaseFirestore db = vm.getDb();

        /**
         * Handles login button click:
         * - Validates input
         * - Matches email + hashed password in Firestore
         * - Routes user based on role
         */
        loginBtn.setOnClickListener(v -> {

            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(getContext(), "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashed = HashUtil.sha256(p);

            db.collection("profiles")
                    .whereEqualTo("email", e)
                    .whereEqualTo("passwordHash", hashed)
                    .get()
                    .addOnSuccessListener(q -> {

                        if (q.isEmpty()) {
                            Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Profile pObj = q.getDocuments().get(0).toObject(Profile.class);
                        String role = pObj.getRole();

                        if (role.equalsIgnoreCase("entrant")) {

                            Entrant entrant = q.getDocuments().get(0).toObject(Entrant.class);
                            entrantVM.setEntrant(entrant);

                            String deviceId = Settings.Secure.getString(
                                    requireContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID
                            );

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("deviceId", deviceId);

                            vm.updateProfile(pObj.getProfileId(), updates, null, null);

                            Navigation.findNavController(view)
                                    .navigate(R.id.entrantHomeFragment);
                        }
                        else if (role.equalsIgnoreCase("organizer")) {

                            organizerVM.setOrganizer(pObj);

                            Navigation.findNavController(view)
                                    .navigate(R.id.organizerHomeFragment);
                        }
                        else {

                            Admin admin = q.getDocuments().get(0).toObject(Admin.class);
                            adminVM.setAdmin(admin);

                            Navigation.findNavController(view)
                                    .navigate(R.id.adminHomeFragment);
                        }

                        Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(err ->
                            Toast.makeText(
                                    getContext(),
                                    "Error: " + err.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });

        return view;
    }
}
