package cvv.udacity.sunshine;

import android.net.Uri;

public interface Callback {
    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onItemSelected(Uri dateUri);
}