package swin.examples;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Displays movie name, rating and votes. A custom icon is generated based on movie name and rating.
 * @author rvasa
 *
 */
public class MovieRatingsActivity extends ListActivity
{
	private ArrayList<Movie> movies = new ArrayList<Movie>();
	private LayoutInflater mInflater;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initializeUI();
	}
	
	private void initializeUI()
	{
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		InputStream inputStream = getResources().openRawResource(	R.raw.ratings);
		movies = Movie.loadFromFile(inputStream);		
		setListAdapter(new RowIconAdapter(this, R.layout.listrow, R.id.row_label, movies));
	}
	
	/** Custom row adatper -- that displays an icon next to the movie name */
	class RowIconAdapter extends ArrayAdapter<Movie> 
	{
		private ArrayList<Movie> movies;		
		public RowIconAdapter(Context c, int rowResourceId, int textViewResourceId, 
				ArrayList<Movie> items)
		{
			super(c, rowResourceId, textViewResourceId, items);
			movies  = items;
		}
		
		public View getView(int pos, View convertView, ViewGroup parent)
		{
			View row = mInflater.inflate(R.layout.listrow, parent, false);
			Movie currMovie = movies.get(pos);
			
			if (currMovie != null)
			{
				ImageView icon = (ImageView) row.findViewById(R.id.row_icon);
				TextView movieText = (TextView) row.findViewById(R.id.row_label);
				TextView votesText = (TextView) row.findViewById(R.id.row_subtext);
				movieText.setText(currMovie.getName());
				String votesStr = currMovie.getVotes()+" votes";
				votesText.setText(votesStr);
				Bitmap movieIcon = getMovieIcon(currMovie.getName(), currMovie.getRating());
				icon.setImageBitmap(movieIcon);
				Log.w("MVMVMVMVMVMV", "Creating row view at position "+pos+" movie "+currMovie.getName());
			}
			return row;			
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