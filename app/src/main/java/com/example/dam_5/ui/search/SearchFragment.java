package com.example.dam_5.ui.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dam_5.R;
import com.example.dam_5.databinding.FragmentSearchBinding;
import com.example.dam_5.utilities.CustomSearchList;
import com.example.dam_5.utilities.User;
import com.example.dam_5.utilities.UserSearchAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FirebaseFirestore db;
    private SearchViewModel searchViewModel;
    private FragmentSearchBinding binding;
    private CollectionReference usersRef;
    private ProgressBar progBar;

    private ListView usersList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        progBar = getView().findViewById(R.id.loadingUsersSpinner);
        progBar.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //
        /*manage searches*/


        ImageButton search_button = (ImageButton) getView().findViewById(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*retrieve query input*/
                EditText search_input = (EditText) getView().findViewById(R.id.search_query);
                if (search_input.getText().toString().isEmpty()) {
                    search_input.setError("The value inserted is empty!");
                } else {
                    /*enable progress bar*/
                    progBar.setVisibility(View.VISIBLE);
                    progBar.setEnabled(true);

                    /* Log.d("COOL BRO", "fino a qui ok");*/
                    usersRef = db.collection("users");


                    /*execute firebase query*/
                    usersRef.whereGreaterThanOrEqualTo("username", search_input.getText().toString().toLowerCase()).whereLessThanOrEqualTo("username", search_input.getText().toString().toLowerCase() + '\uf8ff')
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    progBar.setVisibility(View.GONE);
                                    progBar.setEnabled(false);
                                    /*scoped arrays for custom search list*/
                                    ArrayList<User> users = new ArrayList<>();


                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {
                                            Snackbar.make(getView(), "No user found :(", BaseTransientBottomBar.LENGTH_SHORT).show();
                                        }
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("COOL BRO", document.getId() + " => " + document.getData());
                                            users.add(new User(document.getString("username"),
                                                    document.getString("email"),
                                                    document.getBoolean("hasProfilePicture"),
                                                    document.getString("profilePictureURL")));


                                        }

                                        UserSearchAdapter adapter = new UserSearchAdapter(getContext(), users);
                                        ListView lv = getView().findViewById(R.id.search_list);
                                        lv.setAdapter(adapter);

                                    } else {
                                        Log.d("not COOL BRO", "TESA CAZZO");
                                        /*show error message*/
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage(R.string.search_error_message)
                                                .setTitle(R.string.search_error_title);

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FAIL", "sad noises");
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.search_error_message)
                                    .setTitle(R.string.search_error_title);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}