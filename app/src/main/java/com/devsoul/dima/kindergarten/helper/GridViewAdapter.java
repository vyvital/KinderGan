package com.devsoul.dima.kindergarten.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.devsoul.dima.kindergarten.R;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;

/**
 * GridView Adapter for the grid layout in Teacher Activity
 * for showing the kid's pictures in grid
 */
public class GridViewAdapter extends BaseAdapter
{
    private Context context;
    private final ArrayList<String> KidsImagesPath;
    private final ArrayList<Integer> KidsPresence;

    // Constructor
    public GridViewAdapter(Context context, ArrayList images, ArrayList presence)
    {
        this.context = context;
        this.KidsImagesPath = images;
        this.KidsPresence = presence;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        // Check to see if we have a view
        if (convertView == null)
        {
            // no view - so create a new one
            gridView = new View(context);

            // get layout from kid.xml
            gridView = inflater.inflate(R.layout.kid, null);

            // set image
            CircleImageView imageView = (CircleImageView) gridView
                    .findViewById(R.id.grid_item_image);

            Picasso.with(context).load(KidsImagesPath.get(position)).error(R.drawable.baby)
                    .resize(300, 300).centerCrop()
                    .into(imageView);

            // set border color
            if (KidsPresence.get(position) == 1)
            // Kid presence in kindergarten
            {
                // Set border color to green
                imageView.setBorderColor(context.getResources().getColor(R.color.green));
            }
            else if (KidsPresence.get(position) == 0)
            // Kid doesn't presence in kindergarten
            {
                // Set border color to red
                imageView.setBorderColor(context.getResources().getColor(R.color.color2));
            }
        }
        else
        {
            // use the recycled view object
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount()
    {
        return KidsImagesPath.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }
}
