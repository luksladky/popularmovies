package cz.lukassladky.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import cz.lukassladky.popularmovies.utils.Constants;
import cz.lukassladky.popularmovies.utils.Utility;

public class MainActivity extends AppCompatActivity implements PostersFragment.Callback {


    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mSortingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSortingOrder = Utility.getPreferredSortingOrder(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true; //tablet layout

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(),DETAILFRAGMENT_TAG)
                        .commit();
            }

        } else {
            mTwoPane = false; //phone layout
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(Movie selectedMovie) {
        if (mTwoPane) {

            Bundle arguments = new Bundle();
            arguments.putParcelable(Constants.PARC_MOVIES_KEY,selectedMovie);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container,fragment)
                    .commit();

        } else {
            Intent intent = new Intent(this,DetailActivity.class)
                    .putExtra(Constants.PARC_MOVIES_KEY,selectedMovie);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first
        // Test Network

        String newSortingOrder = Utility.getPreferredSortingOrder(this);

        if (newSortingOrder != null && !newSortingOrder.equals(mSortingOrder)) {
            PostersFragment pf = (PostersFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_posters);
            if (pf != null ) {
                pf.onSortingOrderChanged();
            }
            /*DetailFragment df = (DetailFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null ) {

            }*/

            mSortingOrder = newSortingOrder;
        }
    }

}
