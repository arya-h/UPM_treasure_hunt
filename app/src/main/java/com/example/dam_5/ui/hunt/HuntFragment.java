package com.example.dam_5.ui.hunt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dam_5.databinding.FragmentHuntBinding;
import com.example.dam_5.ui.hunt.HuntViewModel;

public class HuntFragment extends Fragment {

    private HuntViewModel huntViewModel;
    private FragmentHuntBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        huntViewModel =
                new ViewModelProvider(this).get(HuntViewModel.class);

        binding = FragmentHuntBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

/*        final TextView textView = binding.textSlideshow;
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}