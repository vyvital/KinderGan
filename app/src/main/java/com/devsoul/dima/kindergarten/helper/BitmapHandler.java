package com.devsoul.dima.kindergarten.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import static android.graphics.BitmapFactory.decodeStream;

/**
 * This class takes care of all bitmap functionality.
 * Calculate sample size of bitmap based on target width and height,
 * Decode sampled bitmap from stream,
 * Load decoded bitmap to image view,
 * AsyncTask of BitmapWorker - Background thread.
 */
public class BitmapHandler
{
    private static final String TAG = BitmapHandler.class.getSimpleName();

    private Context _context;   // Context from activity
    private Bitmap bitmap;      // Bitmap of the image

    // Constructor
    public BitmapHandler(Context context)
    {
        this._context = context;
    }

    // Getter
    public Bitmap GetBitmap()
    {
        return this.bitmap;
    }

    /**
     * A method to calculate the sample size value based on a target width and height
     * @param options - decoding options
     * @param reqWidth - the required width size of the image
     * @param reqHeight - the required height size of the image
     * @return sample size of image
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * Load a Scaled Down Version into Memory
     * @param path - Uri path of the image
     * @param reqWidth - the required width size of the image
     * @param reqHeight - the required height size of the image
     * @return down scaled bitmap
     */
    public Bitmap decodeSampledBitmapFromStream(Uri path, int reqWidth, int reqHeight)
    {
        Bitmap bitmap = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try
        {
            decodeStream(this._context.getContentResolver().openInputStream(path), null, options);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try
        {
            bitmap = BitmapFactory.decodeStream(this._context.getContentResolver().openInputStream(path), null, options);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Convert Bitmap to base64 String.
     * @param bmp - Bitmap
     * @return encodedImage
     */
    public String getStringImage(Bitmap bmp)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * To start loading the bitmap asynchronously with AsyncTask
     * @param uri - path of bitmap
     * @param imageView - the image view on which the bitmap will load
     */
    public void loadBitmap(Uri uri, ImageView imageView)
    {
        // Decode the image and set on the image view
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(uri);
    }

    /**
     * A background thread (AsyncTask) to perform the image loading.
     */
    class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private Uri uri;

        // Constructor
        public BitmapWorkerTask(ImageView imageView)
        {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... params)
        {
            uri = params[0];
            return decodeSampledBitmapFromStream(uri, 200, 200);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bmp)
        {
            if (imageViewReference != null && bmp != null)
            {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null)
                {
                    bitmap = bmp;
                    imageView.setImageBitmap(bmp);
                }
            }
        }
    }
}
