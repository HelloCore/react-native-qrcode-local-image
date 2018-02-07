package com.remobile.qrcodeLocalImage;

import android.os.Environment;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.SparseArray;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;

import com.google.android.gms.vision.barcode.*;
import com.google.android.gms.vision.Frame;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumSet;
import java.util.Hashtable;

import static java.security.AccessController.getContext;


public class RCTQRCodeLocalImage extends ReactContextBaseJavaModule {

    private ReactApplicationContext mReactContext;

    RCTQRCodeLocalImage(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
    }

    private static final String TAG = "QR";

    @Override
    public String getName() {
        return "RCTQRCodeLocalImage";
    }

    @ReactMethod
    public void decode(String path, Callback callback) {
        try {
            Uri mediaUri = Uri.parse(path);
            String realPath = getRealPathFromUri(mReactContext, mediaUri);

            Bitmap scanBitmap;
            if (path.startsWith("http://")||path.startsWith("https://")) {
                scanBitmap = this.decodeNetworkBitmap(path);
            } else {
                scanBitmap = this.decodeLocalBitmap(realPath);
            }

            if (scanBitmap == null) {
                callback.invoke("cannot load image");
                return;
            }


            Log.d("QR", scanBitmap.getByteCount() + "bytes");
//            Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
////            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
////            hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
//            hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE); // this effective

            int[] intArray = new int[scanBitmap.getWidth()*scanBitmap.getHeight()];

            scanBitmap.getPixels(intArray, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
            LuminanceSource source = new RGBLuminanceSource(scanBitmap.getWidth(), scanBitmap.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new MultiFormatReader();

            try {
                Result result = reader.decode(bitmap);
                Log.d(TAG, "result - " + result);
                callback.invoke(null, result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Errorx - " + e);
                callback.invoke("Decode error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error GENERIC - " + e);
            callback.invoke("Life error");
        }
    }

    private Bitmap decodeNetworkBitmap(String imageUri) {
        Bitmap bitmap;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            bitmap = null;
        } catch (IOException e) {
            e.printStackTrace();
            bitmap = null;
        }

        return bitmap;
    }

    private Bitmap decodeLocalBitmap(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0) {
            sampleSize = 1;
        }
        Log.d(TAG, "sampleSize is " + sampleSize);

        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false; // 获取新的大小

        bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    private String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
