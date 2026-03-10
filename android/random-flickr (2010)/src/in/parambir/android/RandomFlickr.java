package in.parambir.android;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * An activity that loads 10 "interesting" images from flickr
 * website and displays them in a gallery widget. Tapping a
 * photo in the gallery loads it into the ImageViewer widget.
 * Flicking left/right on the screen rotates the image anti-clockwise/
 * clockwise respectively. Images can also be rotated using menu. 
 * Currently displaying image can be set as wallpaper using the menu.
 * The images are cached to the sdcard (to save bandwidth/time). They
 * can be refreshed manually using the menu.
 * 
 * @author param
 *
 */
public class RandomFlickr extends Activity {
	// Flickr API key
	private static String FLICKR_API_KEY = "f..g";

	// IDs for menu items
	private static final int ROTATE_RIGHT = Menu.FIRST;
	private static final int ROTATE_LEFT = Menu.FIRST + 1;
	private static final int SET_WALLPAPER = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;
	
	// Gesture detector to detect left/right swipes
	private GestureDetector gestureDetector;
	
	// Dialog that displays progress bar while images are fetched
	// from the Flickr web site.
	static final int PROGRESS_DIALOG = 0;
	ProgressDialog progressDialog;
	
	// Creates a new FlickrImageLoader with my flickr API key
	FlickrImageLoader loader = new FlickrImageLoader(FLICKR_API_KEY);

	// The Gallery
	private Gallery gallery;

	// The imageView
	private ImageView imageView;
	
	// The drawables shown in the gallery
	Drawable[] drawables = new Drawable[10];

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
        	// Get the progress value from the msg and set
        	// it on the progress bar
            int total = msg.getData().getInt("total");
            progressDialog.setProgress(total);
            
            // if done, dismiss the progress dialog
            if (total >= drawables.length){
                dismissDialog(PROGRESS_DIALOG);
            }
        }
    };
    
    // Another handler for refreshing images from Flickr
    final Handler mHandlerFlickr = new Handler();

	// Create Runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		@Override
		public void run() {
			updateResultsInUI();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Initialize gesture detector with MyGestureListener instance
		gestureDetector = new GestureDetector(this, new MyGestureListener());
		
		// Initialize gallery and imageView widgets
		gallery = (Gallery) findViewById(R.id.gallery);
		imageView = (ImageView) findViewById(R.id.imageView);
		
		// get the images (from sdcard or web)
		getImages();
	}

	
	/**
	 * Loads images into the gallery. Calls loadImages() in a new thread
	 * so that the UI doesn't hang while the images are being loaded. Once
	 * images have been loaded, the results are posted to mHandler which 
	 * in turn calls updateResultsInUI() in another thread.
	 */
	protected void getImages() {
		showDialog(PROGRESS_DIALOG);
		// Fire off a thread to do some work that we shouldn't do directly in
		// the UI thread
		new Thread() {
			public void run() {
				loadImages();
				mHandler.post(mUpdateResults);
			}
		}.start();
	}

	/**
	 * Updates the UI with the new images loaded in the 
	 * drawables array.
	 */
	private void updateResultsInUI() {
		// Refresh the gallery.
		gallery.setAdapter(new ImageAdapter(this));
		
		// when an item is selected in the gallery, create its bitmap
		// and load it in the imageView.
		gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				Bitmap bitMap = ((BitmapDrawable)drawables[position]).getBitmap();
				imageView.setImageBitmap(bitMap);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
		});
		
		// Load first image of the gallery in the imageView
		Bitmap bitMap = ((BitmapDrawable)drawables[0]).getBitmap();
		imageView.setImageBitmap(bitMap);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	}

	/**
	 * Checks if cached images are present on the sdcard. If present, 
	 * images are loaded from the card. Otherwise, they are loaded
	 * from flickr. 
	 */
	private void loadImages() {
		// check if local images are present
		File[] localFiles = loadLocalFiles();
		
		if(localFiles == null) 
			loadImagesFromFlickr();
		else
			loadImagesFromDisk(localFiles);
	}
	
	/**
	 * Creates drawables from sdcard. 
	 * @param files Array of File objects referring to images on the sdcard
	 */
	private void loadImagesFromDisk(File[] files) {
		for(int i = 0; i < drawables.length; i++) {
			drawables[i] = Drawable.createFromPath(files[i].getPath());
		}
	}
		
	/**
	 * Creates an array of File objects that refer to images on the sdcard.
	 * Images are assumed to be located in "randomFlickr" folder on the sdcard
	 * and named imageXX.jpg. If enough images are not present (as required
	 * by the drawables array), it returns null.
	 * 
	 * @return Array of File objects referring to images on the sdcard.
	 */
	private File[] loadLocalFiles() {
		File[] files = new File[drawables.length];
		String sdImageMainDir = Environment.getExternalStorageDirectory() + "/randomFlickr";
		String fileNamePrefix = "image";
		
		for(int i = 0; i < drawables.length; i++) {
			File f = new File(sdImageMainDir + "/" + fileNamePrefix + i + ".jpg");
			if(!f.exists())
				return null;
			else
				files[i] = f;
			
			// Send an update msg to the progress dialog
			Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("total", i+1);
            msg.setData(b);
            mHandler.sendMessage(msg);
		}
		
		return files;
	}
	
	/**
	 * Save the recently downloaded flickr images to sdcard. Images are stored
	 * in the "randomFlickr" directory on the sdcard and named image1.jpg. image2.jpg
	 * ... etc.
	 * @throws IOException 
	 */
	private void saveImagesToCard() {
		String sdImageMainDir = Environment.getExternalStorageDirectory() + "/randomFlickr";
		FileOutputStream fileOutputStream = null;
		String fileNamePrefix = "image";
		
		File dir = new File(sdImageMainDir);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		for(int i = 0; i < drawables.length; i++) {
			try {
				Bitmap bitMap = ((BitmapDrawable)drawables[i]).getBitmap();
				File f = new File(sdImageMainDir + "/" + fileNamePrefix + i + ".jpg");
				if(!f.exists())
					f.createNewFile();
				fileOutputStream = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
				bitMap.compress(CompressFormat.JPEG, 50, bos);
				bos.flush();
				bos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Loads images from the flickr website and populates
	 * drawables array.
	 */
	private void loadImagesFromFlickr() {
		// load image from flickr into corresponding drawable

		// List of image urls from which we try to download images
		List<String> urlQueue = null;
		
		// create 10 drawables (non null)
		for (int i = 0; i < drawables.length; i++) {
			try {
				do {
					if (urlQueue == null || urlQueue.isEmpty()) {	// if url queue is not initialized or is empty, populate it
						urlQueue = getImageURLs();
					}
					
					Log.i("RandomFlickr", "URL Queue Length = " + urlQueue.size());
					
					// Take a url from the queue and try to create a drawable from it.
					String url = urlQueue.remove(0);
					InputStream is = (InputStream) new URL(url).getContent();
					drawables[i] = Drawable.createFromStream(is, "src_name");
				} while (drawables[i] == null);	// sometimes the drawable is not populated from 
												// the url specified. To get around this, we try 
												// to loop till we are able to create a drawable successfully.
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Send an update msg to the progress dialog
			Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("total", i+1);
            msg.setData(b);
            mHandler.sendMessage(msg);
		}
		
		// Once we have downloaded all images, cache them on sdcard.
		saveImagesToCard();
	}

	/**
	 * Gets a list of URLs pointing to "interesting" flickr images.
	 * @return list of image URLs.
	 */
	private List<String> getImageURLs() {
		String[] urls = new String[0];

		do {
			try {
				// Generate a random page number between 1 and 25
				int pageNo = 1 + (int) (25 * Math.random());
				urls = loader.getImagesByInterestingness(25, pageNo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (urls == null || urls.length == 0);

		return new ArrayList<String>(Arrays.asList(urls));
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog = new ProgressDialog(RandomFlickr.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Loading...");
            progressDialog.setMax(drawables.length);
            return progressDialog;
        default:
            return null;
        }
    }
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 super.onCreateOptionsMenu(menu);
		 
		 // add 4 menu items to the menu.
		 menu.add(0, ROTATE_LEFT, 0, R.string.menu_rotateL);
		 menu.add(0, ROTATE_RIGHT, 0, R.string.menu_rotateR);
		 menu.add(0, SET_WALLPAPER, 0, R.string.menu_wallpaper);
		 menu.add(0, REFRESH_ID, 0, R.string.menu_refresh);
		 return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case REFRESH_ID:
        	// show progress dialog
        	showDialog(PROGRESS_DIALOG);
        	
        	// Fire off a thread to load images from flickr website.
    		new Thread() {
    			public void run() {
    				// Reset progress dialog to '0'.
    				Message msg = mHandler.obtainMessage();
    	            Bundle b = new Bundle();
    	            b.putInt("total", 0);
    	            msg.setData(b);
    	            mHandler.sendMessage(msg);
    	            
    				loadImagesFromFlickr();
    				mHandler.post(mUpdateResults);
    			}
    		}.start();
    		return true;
        case ROTATE_LEFT:
        	rotateImage(-90);
            return true;
        case ROTATE_RIGHT:
        	rotateImage(90);
            return true;
        case SET_WALLPAPER:
        	Bitmap b = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
			try {
				WallpaperManager.getInstance(this).setBitmap(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
	
	/**
	 * Rotates the image being displayed in imageView widget by "degrees" degrees.
	 * @param degrees no. of degrees image should be rotated.
	 */
	private void rotateImage(float degrees) {
		Bitmap b = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
		Matrix mat = new Matrix();
        mat.postRotate(degrees);
        imageView.setImageBitmap(Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), mat, true));
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}
	
	/**
	 * Gesture Listener implementation that listens to "flings" (i.e. swipes) on the screen.
	 * Left swipe rotates the image counter clockwise and right swipe rotates the image
	 * clockwise.
	 * @author param
	 */
	public class MyGestureListener extends SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 50;
		private static final int SWIPE_THRESHOLD_VELOCITY = 50;
		
		/* (non-Javadoc)
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			
			Log.i("RandomFlickr", "onFling - e1.getX() = " + e1.getX() + ", e2.getX() = " + e2.getX() + ", velocityX = " + velocityX);
			
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				rotateImage(-90);
				return true;
			}
			else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				rotateImage(90);
				return true;
			}
			
			return false;
		}
	}	
	
    /**
     * ImageAdapter class that is used to populate the gallery widget.
     * @author param
     */
    public class ImageAdapter extends BaseAdapter {
    	private Context mContext;
        private Drawable[] mImageIds = drawables;
    	private int mGalleryItemBackground;
    	
        public ImageAdapter(Context c) {
            mContext = c;
            
            // Set an android provided style as the gallery widget background.
            TypedArray a = c.obtainStyledAttributes(R.styleable.HelloGallery);
    		mGalleryItemBackground = a.getResourceId(R.styleable.HelloGallery_android_galleryItemBackground, 0);
    		a.recycle();
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return drawables[position];
        }

        public long getItemId(int position) {
            return position;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            i.setImageDrawable(mImageIds[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(150, 100));
            i.setBackgroundResource(mGalleryItemBackground);
            return i;
        }

        public float getAlpha(boolean focused, int offset) {
            return Math.max(0, 1.0f - (0.2f * Math.abs(offset)));
        }

        public float getScale(boolean focused, int offset) {
            return Math.max(0, 1.0f - (0.2f * Math.abs(offset)));
        }

        
    }
}