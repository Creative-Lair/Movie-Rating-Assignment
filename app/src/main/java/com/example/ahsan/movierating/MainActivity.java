package com.example.ahsan.movierating;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Displays movie name, rating and votes. A custom icon is generated based on movie name and rating.
 * @author rvasa
 *
 */

public class MainActivity extends AppCompatActivity
{
    private ArrayList<Movie> movies = new ArrayList<Movie>();
    private LayoutInflater mInflater;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
    }

    private void initializeUI()
    {
        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        InputStream inputStream = getResources().openRawResource(	R.raw.ratings);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        new AsyncTask<InputStream, Void, ArrayList<Movie>>() {
            @Override
            protected void onPreExecute() {

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading");
                progressDialog.show();

            }

            @Override
            protected ArrayList<Movie> doInBackground(InputStream... params) {
                movies = Movie.loadFromFile(params[0]);
                return movies;
            }

            @Override
            protected void onPostExecute(ArrayList<Movie> movies) {
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(new RowIconAdapter(movies));
                if(progressDialog.isShowing()){
                    progressDialog.hide();
                }
            }
        }.execute(inputStream);


    }

    /** Custom row adatper -- that displays an icon next to the movie name */
    class RowIconAdapter extends RecyclerView.Adapter<RowIconAdapter.MyViewHolder>
    {
        private ArrayList<Movie> movies;

        public RowIconAdapter(ArrayList<Movie> moviesList) {
            this.movies = moviesList;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listrow, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final Movie currMovie = movies.get(position);

            if (currMovie != null) {
                new AsyncTask<MyViewHolder, Void, Bitmap>() {
                    MyViewHolder holder;
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected Bitmap doInBackground(MyViewHolder... params) {
                        holder = params[0];
                        return  getMovieIcon(currMovie.getName(), currMovie.getRating());
                    }



                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        holder.title.setText(currMovie.getName());
                        String votesStr = currMovie.getVotes() + " votes";
                        holder.votes.setText(votesStr);
                        holder.icon.setImageBitmap(bitmap);
                        Log.w("MVMVMVMVMVMV", "Creating row view at position " + position + " movie " + currMovie.getName());
                    }
                }.execute(holder);
              }
        }

        @Override
        public int getItemCount() {
            return movies.size();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, votes;
            public ImageView icon;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.row_label);
                votes = (TextView) view.findViewById(R.id.row_subtext);
                icon = (ImageView) view.findViewById(R.id.row_icon);
            }
        }


    }

    /** Creates a unique movie icon based on name and rating */
    private Bitmap getMovieIcon(String movieName, String movieRating)
    {
        int bgColor = getColor(movieName);
        Bitmap b = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        b.eraseColor(bgColor); // fill bitmap with the color
        Canvas c = new Canvas(b);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(getTextColor(bgColor));
        p.setTextSize(24.0f);
        c.drawText(movieRating, 8, 32, p);
        return b;
    }

    /** Construct a color from a movie name */
    private int getColor(String name)
    {
        String hex = toHexString(name);
        String red = "#"+hex.substring(0,2);
        String green = "#"+hex.substring(2,4);
        String blue = "#"+hex.substring(4,6);
        String alpha = "#"+hex.substring(6,8);
        int color = Color.argb(Integer.decode(alpha), Integer.decode(red),
                Integer.decode(green), Integer.decode(blue));
        return color;
    }

    /** Given a movie name -- generate a hex value from its hashcode */
    private String toHexString(String name)
    {
        int hc = name.hashCode();
        String hex = Integer.toHexString(hc);
        if (hex.length() < 8)
        {
            hex = hex+hex+hex;
            hex = hex.substring(0,8); // use default color value
        }
        return hex;
    }

    /** Crude optimization to obtain a contrasting color -- does not work well yet */
    private int getTextColor(int bg)
    {

        int r = Color.red(bg);
        int g = Color.green(bg);
        int b = Color.blue(bg);
        String hex = Integer.toHexString(r)+Integer.toHexString(g);
        hex += Integer.toHexString(b);

        int cDec = Integer.decode("#"+hex);
        if (cDec > 0xFFFFFF/2)  // go dark for lighter shades
            return Color.rgb(0, 0, 0);
        else
        {
            r = (r+128)%256;
            g = (g+128)%256;
            b = (b+128)%256;
            return Color.rgb(r,g,b);
        }
    }
}