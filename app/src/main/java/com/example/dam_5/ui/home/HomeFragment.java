package com.example.dam_5.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dam_5.R;
import com.example.dam_5.databinding.FragmentHomeBinding;
import com.example.dam_5.ui.hunt.HuntFragment;
import com.example.dam_5.utilities.GlobalVariables;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private TextView mainTitle;
    private Button shareCodeButton;

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

        /*set main title*/
        mainTitle = getView().findViewById(R.id.homeBigTitle);
        shareCodeButton = getView().findViewById(R.id.shareCodeButton);

        if (GlobalVariables.getInstance().isHuntInProgress()) {
            mainTitle.setText(R.string.home_huntInProgress);
            shareCodeButton.setVisibility(View.VISIBLE);

        } else {
            mainTitle.setText(R.string.home_huntNot);
        }

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




    }


        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}