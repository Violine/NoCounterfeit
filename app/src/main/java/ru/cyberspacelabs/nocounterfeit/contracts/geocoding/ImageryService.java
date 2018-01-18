package ru.cyberspacelabs.nocounterfeit.contracts.geocoding;

import android.widget.ImageView;

/**
 * Created by mike on 22.03.17.
 */
public interface ImageryService {
	void showOnMap(final double latitude, final double longitude, final ImageView target);
}
