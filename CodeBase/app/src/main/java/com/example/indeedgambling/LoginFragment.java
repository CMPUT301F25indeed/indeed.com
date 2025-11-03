package com.example.indeedgambling;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private FirebaseViewModel vm;
    private EntrantViewModel entrantVM;
    private OrganizerViewModel organizerVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText email = view.findViewById(R.id.login_email);
        EditText password = view.findViewById(R.id.login_password);
        Button loginBtn = view.findViewById(R.id.btn_login);

        vm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        FirebaseFirestore db = vm.getDb();

        loginBtn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();

            if(e.isEmpty() || p.isEmpty()) {
                Toast.makeText(getContext(), "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashed = HashUtil.sha256(p);

            db.collection("profiles")
                    .whereEqualTo("email", e)
                    .whereEqualTo("passwordHash", hashed)
                    .get()
                    .addOnSuccessListener(q -> {
                        if(q.isEmpty()) {
                            Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else {
                            Profile pObj = q.getDocuments().get(0).toObject(Profile.class);
                            String role = pObj.getRole();

                            // store in viewmodel
                            if (role.equalsIgnoreCase("Entrant")) {
                                entrantVM.setEntrant(pObj);
                                Navigation.findNavController(view).navigate(R.id.entrantHomeFragment);
                            }
                            else if (role.equalsIgnoreCase("Organizer")) {
                                organizerVM.setOrganizer(pObj);
                                Navigation.findNavController(view).navigate(R.id.organizerHomeFragment);
                            }
                            else {
                                Navigation.findNavController(view).navigate(R.id.adminHomeFragment);
                            }

                            Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(err ->
                            Toast.makeText(getContext(), "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        return view;
    }
}
