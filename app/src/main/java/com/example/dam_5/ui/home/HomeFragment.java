package com.example.dam_5.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dam_5.MainActivity;
import com.example.dam_5.R;
import com.example.dam_5.databinding.FragmentHomeBinding;
import com.example.dam_5.ui.hunt.HuntFragment;
import com.example.dam_5.utilities.GlobalVariables;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    /*firebase instance*/
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private TextView mainTitle;
    private Button shareCodeButton;
    private Button leaveHuntButton;

    private ProgressBar loadingHomeProgressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        /*firestore*/
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        GlobalVariables.getInstance().setEmail(currentUser.getEmail());

        /*set main title*/
        mainTitle = getView().findViewById(R.id.homeBigTitle);
        shareCodeButton = getView().findViewById(R.id.shareCodeButton);
        leaveHuntButton = getView().findViewById(R.id.leaveCurrentHuntButton);
        loadingHomeProgressBar = getView().findViewById(R.id.progressBarHomeFragment);
        loadingHomeProgressBar.setVisibility(View.VISIBLE);
        loadingHomeProgressBar.setEnabled(true);

        /*ugly code, couldnt find a way to sync HomeFragment and MainActivity to
        * display and act programmatically depending on isOnHunt value retrieve in MainActivity*/



        db.collection("users").document(GlobalVariables.getInstance().getEmail())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    loadingHomeProgressBar.setVisibility(View.GONE);
                    loadingHomeProgressBar.setEnabled(false);


                    boolean isActiveHunt = documentSnapshot.getBoolean("isOnHunt");
                    GlobalVariables.getInstance().setHuntInProgress(isActiveHunt);

                    if (GlobalVariables.getInstance().isHuntInProgress()) {
                        mainTitle.setText(R.string.home_huntInProgress);
                        shareCodeButton.setVisibility(View.VISIBLE);
                        leaveHuntButton.setVisibility(View.VISIBLE);

                    } else {
                        mainTitle.setText(R.string.home_huntNot);
                    }
                }
            }
        });




        shareCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, GlobalVariables.getInstance().getLastHuntCode());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                Intent shareIntent = Intent.createChooser(sendIntent, "Hunt Code");
                startActivity(shareIntent);
            }
        });

        leaveHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*force refresh*/
                GlobalVariables.getInstance().setHuntInProgress(false);
                db.collection("users").document(GlobalVariables.getInstance().getEmail())
                        .update("isOnHunt", false);

                Map<String, Object> removeUserFromArrayMap = new HashMap<>();
                removeUserFromArrayMap.put("participants", FieldValue.arrayRemove(mAuth.getCurrentUser().getEmail()));


                db.collection("hunts").document(GlobalVariables.getInstance().getLastHuntCode())
                        .update(removeUserFromArrayMap);

                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });




    }


        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}