package com.example.dam_5.utilities;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dam_5.R;
import com.squareup.picasso.Picasso;

public class CustomSearchList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] username;
    private final Boolean[] hasProfilePicture;
    private final String[] profilePictureURL;

/*    public MyListAdapter(Activity context, String[] maintitle,String[] subtitle, Integer[] imgid) {
        super(context, R.layout.mylist, maintitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.maintitle=maintitle;
        this.subtitle=subtitle;
        this.imgid=imgid;

    }*/

    public CustomSearchList(Activity context, String[] username, String[] profilePictureURL, Boolean[] hasProfilePicture) {
        super(context, R.layout.custom_searchlist);
        this.context = context;
        this.username = username;
        this.profilePictureURL = profilePictureURL;
        this.hasProfilePicture = hasProfilePicture;
    }

    public View getView(int position, View view, ViewGroup parent) {
        Log.d("CUSTOMVIEW LOG", username[position]);
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.custom_searchlist, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        titleText.setText(username[position]);
        if(hasProfilePicture[position]){
            Picasso.get().load(profilePictureURL[position]).into(imageView);
        }else{
            imageView.setImageResource(R.drawable.user);
        }

        return rowView;

    };
}
