package jp.gr.java_conf.foobar.testmaker.service.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import jp.gr.java_conf.foobar.testmaker.service.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by keita on 2017/03/15.
 */

public class AsyncLoadImage extends AsyncTask<Bitmap, Integer, Bitmap> {

    private final WeakReference<ImageButton> imageViewReference;
    private String url;
    private Context context;
    private int mode;

    public AsyncLoadImage(Context context, ImageButton image, String url, int mode) {
        this.url = url;
        this.context = context;
        this.mode = mode;

        imageViewReference = new WeakReference<>(image);
        imageViewReference.get().setVisibility(View.GONE);
        imageViewReference.get().setImageBitmap(null);
        imageViewReference.get().setImageDrawable(null);

    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        if (isCancelled()) {
            return null;
        }

        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        try {

            int SAVE = 0;

            if (mode == SAVE) {
                FileOutputStream outStream;
                outStream = context.openFileOutput(url, MODE_PRIVATE);
                bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.close();
            }

            InputStream in = context.openFileInput(url);
            Bitmap bm = BitmapFactory.decodeStream(in, null, imageOptions);

            in.close();
            return bm;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (isCancelled()) {
            bitmap.recycle();
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageButton imageButton = imageViewReference.get();
            if (imageButton != null) {
                imageButton.setVisibility(View.VISIBLE);
                imageButton.setImageBitmap(bitmap);
                imageButton.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }
    }
}
