package com.example.dam_5.utilities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dam_5.R;
import com.example.dam_5.ui.search.GenericUserActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserSearchAdapter extends ArrayAdapter<User> {
    public UserSearchAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_searchlist, parent, false);
        }
        // Lookup view for data population
        TextView username = (TextView) convertView.findViewById(R.id.title);
        /*TextView tvHome = (TextView) convertView.findViewById(R.id.icon);*/
        ImageView img = (ImageView) convertView.findViewById(R.id.icon);

        username.setText(user.getUsername());
        if(user.isHasProfilePicture()){
            Picasso.get().load(user.getProfilePictureURL()).resize(59,59).into(img);
        }else{
            img.setImageResource(R.drawable.user);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*open generic user page*/

                Intent intent = new Intent(getContext().getApplicationContext(), GenericUserActivity.class);
                intent.putExtra("email", user.getEmail());

                getContext().startActivity(intent);
            }
        });

        // Populate the data into the template view using the data object
/*        tvName.setText(user.name);
        tvHome.setText(user.hometown);*/
        // Return the completed view to render on screen
        return convertView;
    }

}
