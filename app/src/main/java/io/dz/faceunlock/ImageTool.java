

package io.dz.faceunlock;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageTool {
    static ObjectAnimator invisToVis;
    static ObjectAnimator visToInvis;

    public ImageTool() {
    }

    public static int dp2px(Context context,float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int px2dp(Context context,float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5F);
    }

    public static int sp2px(Context context,float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(spValue * fontScale + 0.5F);
    }

    public static int px2sp(Context context,float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(pxValue / fontScale + 0.5F);
    }


    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024];

        int read;
        while((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }

    }

    public static int getColorByInt(int colorInt) {
        return colorInt | -16777216;
    }

    public static int changeColorAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static float getAlphaPercent(int argb) {
        return (float)Color.alpha(argb) / 255.0F;
    }

    public static int alphaValueAsInt(float alpha) {
        return Math.round(alpha * 255.0F);
    }

    public static int adjustAlpha(float alpha, int color) {
        return alphaValueAsInt(alpha) << 24 | 16777215 & color;
    }

    public static int colorAtLightness(int color, float lightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = lightness;
        return Color.HSVToColor(hsv);
    }

    public static float lightnessOfColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[2];
    }

    public static String getHexString(int color, boolean showAlpha) {
        int base = showAlpha?-1:16777215;
        String format = showAlpha?"#%08X":"#%06X";
        return String.format(format, new Object[]{Integer.valueOf(base & color)}).toUpperCase();
    }

    public static byte[] bitmap2Bytes(Bitmap bitmap, CompressFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return bytes.length != 0?BitmapFactory.decodeByteArray(bytes, 0, bytes.length):null;
    }


    public static Drawable bitmap2Drawable(Resources res, Bitmap bitmap) {
        return new BitmapDrawable(res, bitmap);
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }


    public static Drawable bytes2Drawable(Resources res, byte[] bytes) {
        Bitmap bitmap = bytes2Bitmap(bytes);
        Drawable drawable = bitmap2Drawable(res, bitmap);
        return drawable;
    }

    public static Drawable bytes2Drawable(byte[] bytes) {
        Bitmap bitmap = bytes2Bitmap(bytes);
        Drawable drawable = bitmap2Drawable(bitmap);
        return drawable;
    }

    private static int calculateInSampleSize(Options options, int maxWidth, int maxHeight) {
        if(maxWidth != 0 && maxHeight != 0) {
            int height = options.outHeight;
            int width = options.outWidth;

            int inSampleSize;
            for(inSampleSize = 1; (height >>= 1) >= maxHeight && (width >>= 1) >= maxWidth; inSampleSize <<= 1) {
                ;
            }

            return inSampleSize;
        } else {
            return 1;
        }
    }

    public static Bitmap getBitmap(File file) {
        if(file == null) {
            return null;
        } else {
            BufferedInputStream is = null;

            Object var3;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
                Bitmap var2 = BitmapFactory.decodeStream(is);
                return var2;
            } catch (FileNotFoundException var7) {
                var7.printStackTrace();
                var3 = null;
            } finally {
                FileTool.closeIO(new Closeable[]{is});
            }

            return (Bitmap)var3;
        }
    }

    public static Bitmap getBitmap(File file, int maxWidth, int maxHeight) {
        if(file == null) {
            return null;
        } else {
            BufferedInputStream is = null;

            Bitmap var5;
            try {
                Options options = new Options();
                options.inJustDecodeBounds = true;
                is = new BufferedInputStream(new FileInputStream(file));
                BitmapFactory.decodeStream(is, (Rect)null, options);
                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
                options.inJustDecodeBounds = false;
                var5 = BitmapFactory.decodeStream(is, (Rect)null, options);
                return var5;
            } catch (FileNotFoundException var9) {
                var9.printStackTrace();
                var5 = null;
            } finally {
                FileTool.closeIO(new Closeable[]{is});
            }

            return var5;
        }
    }

    public static Bitmap getBitmap(InputStream is, int maxWidth, int maxHeight) {
        if(is == null) {
            return null;
        } else {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, (Rect)null, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(is, (Rect)null, options);
        }
    }

    public static Bitmap getBitmap(byte[] data, int offset, int maxWidth, int maxHeight) {
        if(data.length == 0) {
            return null;
        } else {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, offset, data.length, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(data, offset, data.length, options);
        }
    }

    public static Bitmap getBitmap(Resources res, int id) {
        return res == null?null:BitmapFactory.decodeResource(res, id);
    }

    public static Bitmap getBitmap(Resources res, int id, int maxWidth, int maxHeight) {
        if(res == null) {
            return null;
        } else {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, id, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, id, options);
        }
    }

    public static Bitmap getBitmap(FileDescriptor fd) {
        return fd == null?null:BitmapFactory.decodeFileDescriptor(fd);
    }

    public static Bitmap getBitmap(FileDescriptor fd, int maxWidth, int maxHeight) {
        if(fd == null) {
            return null;
        } else {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, (Rect)null, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFileDescriptor(fd, (Rect)null, options);
        }
    }

    public static Bitmap scale(Bitmap src, int newWidth, int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    public static Bitmap scale(Bitmap src, int newWidth, int newHeight, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Bitmap ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap scale(Bitmap src, float scaleWidth, float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    public static Bitmap scale(Bitmap src, float scaleWidth, float scaleHeight, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Matrix matrix = new Matrix();
            matrix.setScale(scaleWidth, scaleHeight);
            Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap clip(Bitmap src, int x, int y, int width, int height) {
        return clip(src, x, y, width, height, false);
    }

    public static Bitmap clip(Bitmap src, int x, int y, int width, int height, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Bitmap ret = Bitmap.createBitmap(src, x, y, width, height);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap skew(Bitmap src, float kx, float ky) {
        return skew(src, kx, ky, 0.0F, 0.0F, false);
    }

    public static Bitmap skew(Bitmap src, float kx, float ky, boolean recycle) {
        return skew(src, kx, ky, 0.0F, 0.0F, recycle);
    }

    public static Bitmap skew(Bitmap src, float kx, float ky, float px, float py) {
        return skew(src, kx, ky, 0.0F, 0.0F, false);
    }

    public static Bitmap skew(Bitmap src, float kx, float ky, float px, float py, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Matrix matrix = new Matrix();
            matrix.setSkew(kx, ky, px, py);
            Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap rotate(Bitmap src, int degrees, float px, float py) {
        return rotate(src, degrees, px, py, false);
    }

    public static Bitmap rotate(Bitmap src, int degrees, float px, float py, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else if(degrees == 0) {
            return src;
        } else {
            Matrix matrix = new Matrix();
            matrix.setRotate((float)degrees, px, py);
            Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static int getRotateDegree(String filePath) {
        short degree = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt("Orientation", 1);
            switch(orientation) {
            case 3:
                degree = 180;
                break;
            case 6:
            default:
                degree = 90;
                break;
            case 8:
                degree = 270;
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return degree;
    }

    public static Bitmap toRound(Bitmap src) {
        return toRound(src, false);
    }

    public static Bitmap toRound(Bitmap src, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            int width = src.getWidth();
            int height = src.getHeight();
            int radius = Math.min(width, height) >> 1;
            Bitmap ret = src.copy(src.getConfig(), true);
            Paint paint = new Paint();
            Canvas canvas = new Canvas(ret);
            Rect rect = new Rect(0, 0, width, height);
            paint.setAntiAlias(true);
            paint.setColor(0);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle((float)(width >> 1), (float)(height >> 1), (float)radius, paint);
            canvas.drawBitmap(src, rect, rect, paint);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap toRoundCorner(Bitmap src, float radius) {
        return toRoundCorner(src, radius, false);
    }

    public static Bitmap toRoundCorner(Bitmap src, float radius, boolean recycle) {
        if(null == src) {
            return null;
        } else {
            int width = src.getWidth();
            int height = src.getHeight();
            Bitmap ret = src.copy(src.getConfig(), true);
            BitmapShader bitmapShader = new BitmapShader(src, TileMode.CLAMP, TileMode.CLAMP);
            Paint paint = new Paint();
            Canvas canvas = new Canvas(ret);
            RectF rectf = new RectF(0.0F, 0.0F, (float)width, (float)height);
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
            canvas.drawRoundRect(rectf, radius, radius, paint);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap stackBlur(Bitmap src, int radius, boolean recycle) {
        Bitmap ret;
        if(recycle) {
            ret = src;
        } else {
            ret = src.copy(src.getConfig(), true);
        }

        if(radius < 1) {
            return null;
        } else {
            int w = ret.getWidth();
            int h = ret.getHeight();
            int[] pix = new int[w * h];
            ret.getPixels(pix, 0, w, 0, 0, w, h);
            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;
            int[] r = new int[wh];
            int[] g = new int[wh];
            int[] b = new int[wh];
            int[] vmin = new int[Math.max(w, h)];
            int divsum = div + 1 >> 1;
            divsum *= divsum;
            int[] dv = new int[256 * divsum];

            int i;
            for(i = 0; i < 256 * divsum; ++i) {
                dv[i] = i / divsum;
            }

            int yi = 0;
            int yw = 0;
            int[][] stack = new int[div][3];
            int r1 = radius + 1;

            int rsum;
            int gsum;
            int bsum;
            int x;
            int y;
            int p;
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int routsum;
            int goutsum;
            int boutsum;
            int rinsum;
            int ginsum;
            int binsum;
            for(y = 0; y < h; ++y) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;

                for(i = -radius; i <= radius; ++i) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 16711680) >> 16;
                    sir[1] = (p & '\uff00') >> 8;
                    sir[2] = p & 255;
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if(i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }

                stackpointer = radius;

                for(x = 0; x < w; ++x) {
                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if(y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }

                    p = pix[yw + vmin[x]];
                    sir[0] = (p & 16711680) >> 16;
                    sir[1] = (p & '\uff00') >> 8;
                    sir[2] = p & 255;
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer % div];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    ++yi;
                }

                yw += w;
            }

            for(x = 0; x < w; ++x) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;
                int yp = -radius * w;

                for(i = -radius; i <= radius; ++i) {
                    yi = Math.max(0, yp) + x;
                    sir = stack[i + radius];
                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];
                    rbs = r1 - Math.abs(i);
                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;
                    if(i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if(i < hm) {
                        yp += w;
                    }
                }

                yi = x;
                stackpointer = radius;

                for(y = 0; y < h; ++y) {
                    pix[yi] = -16777216 & pix[yi] | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if(x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }

                    p = x + vmin[y];
                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi += w;
                }
            }

            ret.setPixels(pix, 0, w, 0, 0, w, h);
            return ret;
        }
    }

    public static Bitmap addFrame(Bitmap src, int borderWidth, int color) {
        return addFrame(src, borderWidth, color);
    }

    public static Bitmap addFrame(Bitmap src, int borderWidth, int color, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            int newWidth = src.getWidth() + borderWidth >> 1;
            int newHeight = src.getHeight() + borderWidth >> 1;
            Bitmap ret = Bitmap.createBitmap(newWidth, newHeight, src.getConfig());
            Canvas canvas = new Canvas(ret);
            Rect rec = canvas.getClipBounds();
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth((float)borderWidth);
            canvas.drawRect(rec, paint);
            canvas.drawBitmap(src, (float)borderWidth, (float)borderWidth, (Paint)null);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap addTextWatermark(Bitmap src, String content, int textSize, int color, int alpha, float x, float y) {
        return addTextWatermark(src, content, textSize, color, alpha, x, y, false);
    }

    public static Bitmap addTextWatermark(Bitmap src, String content, int textSize, int color, int alpha, float x, float y, boolean recycle) {
        if(!isEmptyBitmap(src) && content != null) {
            Bitmap ret = src.copy(src.getConfig(), true);
            Paint paint = new Paint(1);
            Canvas canvas = new Canvas(ret);
            paint.setAlpha(alpha);
            paint.setColor(color);
            paint.setTextSize((float)textSize);
            Rect bounds = new Rect();
            paint.getTextBounds(content, 0, content.length(), bounds);
            canvas.drawText(content, x, y, paint);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        } else {
            return null;
        }
    }

    public static Bitmap addImageWatermark(Bitmap src, Bitmap watermark, int x, int y, int alpha) {
        return addImageWatermark(src, watermark, x, y, alpha, false);
    }

    public static Bitmap addImageWatermark(Bitmap src, Bitmap watermark, int x, int y, int alpha, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Bitmap ret = src.copy(src.getConfig(), true);
            if(!isEmptyBitmap(watermark)) {
                Paint paint = new Paint(1);
                Canvas canvas = new Canvas(ret);
                paint.setAlpha(alpha);
                canvas.drawBitmap(watermark, (float)x, (float)y, paint);
            }

            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    public static Bitmap toAlpha(Bitmap src) {
        return toAlpha(src);
    }

    public static Bitmap toAlpha(Bitmap src, Boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Bitmap ret = src.extractAlpha();
            if(recycle.booleanValue() && !src.isRecycled()) {
                src.recycle();
            }

            return ret;
        }
    }

    private static Bitmap getDropShadow(ImageView iv, Bitmap src, float radius, int color) {
        Paint paint = new Paint(1);
        paint.setColor(color);
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap dest = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        Bitmap alpha = src.extractAlpha();
        canvas.drawBitmap(alpha, 0.0F, 0.0F, paint);
        BlurMaskFilter filter = new BlurMaskFilter(radius, Blur.OUTER);
        paint.setMaskFilter(filter);
        canvas.drawBitmap(alpha, 0.0F, 0.0F, paint);
        iv.setImageBitmap(dest);
        return dest;
    }

    public static Bitmap toGray(Bitmap src) {
        return toGray(src, false);
    }

    public static Bitmap toGray(Bitmap src, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Bitmap grayBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.RGB_565);
            Canvas canvas = new Canvas(grayBitmap);
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0F);
            ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorMatrixColorFilter);
            canvas.drawBitmap(src, 0.0F, 0.0F, paint);
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return grayBitmap;
        }
    }

    public static boolean save(Bitmap src, String filePath, CompressFormat format) {
        return save(src, new File(filePath), format, false);
    }

    public static boolean save(Bitmap src, File file, CompressFormat format) {
        return save(src, file, format, false);
    }

    public static boolean save(Bitmap src, String filePath, CompressFormat format, boolean recycle) {
        return save(src, FileTool.getFileByPath(filePath), format, recycle);
    }

    public static boolean save(Bitmap src, File file, CompressFormat format, boolean recycle) {
        if(!isEmptyBitmap(src) && FileTool.createOrExistsFile(file)) {
            System.out.println(src.getWidth() + ", " + src.getHeight());
            OutputStream os = null;
            boolean ret = false;

            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                ret = src.compress(format, 100, os);
                if(recycle && !src.isRecycled()) {
                    src.recycle();
                }
            } catch (IOException var10) {
                var10.printStackTrace();
            } finally {
                FileTool.closeIO(new Closeable[]{os});
            }

            return ret;
        } else {
            return false;
        }
    }

    public static boolean isImage(File file) {
        return file != null && isImage(file.getPath());
    }

    public static boolean isImage(String filePath) {
        String path = filePath.toUpperCase();
        return path.endsWith(".PNG") || path.endsWith(".JPG") || path.endsWith(".JPEG") || path.endsWith(".BMP") || path.endsWith(".GIF");
    }

    public static String getImageType(String filePath) {
        return getImageType(FileTool.getFileByPath(filePath));
    }

    public static String getImageType(File file) {
        if(file == null) {
            return null;
        } else {
            FileInputStream is = null;

            Object var3;
            try {
                is = new FileInputStream(file);
                String var2 = getImageType((InputStream)is);
                return var2;
            } catch (IOException var7) {
                var7.printStackTrace();
                var3 = null;
            } finally {
                FileTool.closeIO(new Closeable[]{is});
            }

            return (String)var3;
        }
    }

    public static String getImageType(InputStream is) {
        if(is == null) {
            return null;
        } else {
            try {
                byte[] bytes = new byte[8];
                return is.read(bytes, 0, 8) != -1?getImageType(bytes):null;
            } catch (IOException var2) {
                var2.printStackTrace();
                return null;
            }
        }
    }

    public static String getImageType(byte[] bytes) {
        return isJPEG(bytes)?"JPEG":(isGIF(bytes)?"GIF":(isPNG(bytes)?"PNG":(isBMP(bytes)?"BMP":null)));
    }

    private static boolean isJPEG(byte[] b) {
        return b.length >= 2 && b[0] == -1 && b[1] == -40;
    }

    private static boolean isGIF(byte[] b) {
        return b.length >= 6 && b[0] == 71 && b[1] == 73 && b[2] == 70 && b[3] == 56 && (b[4] == 55 || b[4] == 57) && b[5] == 97;
    }

    private static boolean isPNG(byte[] b) {
        return b.length >= 8 && b[0] == -119 && b[1] == 80 && b[2] == 78 && b[3] == 71 && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10;
    }

    private static boolean isBMP(byte[] b) {
        return b.length >= 2 && b[0] == 66 && b[1] == 77;
    }

    private static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    public static Bitmap compressByScale(Bitmap src, int newWidth, int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    public static Bitmap compressByScale(Bitmap src, int newWidth, int newHeight, boolean recycle) {
        return scale(src, newWidth, newHeight, recycle);
    }

    public static Bitmap compressByScale(Bitmap src, float scaleWidth, float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    public static Bitmap compressByScale(Bitmap src, float scaleWidth, float scaleHeight, boolean recycle) {
        return scale(src, scaleWidth, scaleHeight, recycle);
    }

    public static Bitmap compressByQuality(Bitmap src, int quality) {
        return compressByQuality(src, quality, false);
    }

    public static Bitmap compressByQuality(Bitmap src, int quality, boolean recycle) {
        if(!isEmptyBitmap(src) && quality >= 0 && quality <= 100) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.compress(CompressFormat.JPEG, quality, baos);
            byte[] bytes = baos.toByteArray();
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    public static Bitmap compressByQuality(Bitmap src, long maxByteSize) {
        return compressByQuality(src, maxByteSize, false);
    }

    public static Bitmap compressByQuality(Bitmap src, long maxByteSize, boolean recycle) {
        if(!isEmptyBitmap(src) && maxByteSize > 0L) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 100;
            src.compress(CompressFormat.JPEG, quality, baos);

            while((long)baos.toByteArray().length > maxByteSize && quality >= 0) {
                baos.reset();
                quality -= 5;
                src.compress(CompressFormat.JPEG, quality, baos);
            }

            if(quality < 0) {
                return null;
            } else {
                byte[] bytes = baos.toByteArray();
                if(recycle && !src.isRecycled()) {
                    src.recycle();
                }

                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } else {
            return null;
        }
    }

    public static Bitmap compressBySampleSize(Bitmap src, int sampleSize) {
        return compressBySampleSize(src, sampleSize, false);
    }

    public static Bitmap compressBySampleSize(Bitmap src, int sampleSize, boolean recycle) {
        if(isEmptyBitmap(src)) {
            return null;
        } else {
            Options options = new Options();
            options.inSampleSize = sampleSize;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.compress(CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            if(recycle && !src.isRecycled()) {
                src.recycle();
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }
    }

    public static Bitmap getThumb(String filePath, int kind) {
        return ThumbnailUtils.createVideoThumbnail(filePath, kind);
    }

    public static Bitmap getThumb(Bitmap source, int width, int height) {
        return ThumbnailUtils.extractThumbnail(source, width, height);
    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        float width = (float)bgimage.getWidth();
        float height = (float)bgimage.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = (float)newWidth / width;
        float scaleHeight = (float)newHeight / height;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int)width, (int)height, matrix, true);
        Log.e("tag", bitmap.getHeight() + bitmap.getWidth() + "d");
        return bitmap;
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = (float)width / (float)w;
        float scaleHeight = (float)height / (float)h;
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    public Bitmap getBitmap(InputStream is) {
        return is == null?null:BitmapFactory.decodeStream(is);
    }

    public Bitmap getBitmap(byte[] data, int offset) {
        return data.length == 0?null:BitmapFactory.decodeByteArray(data, offset, data.length);
    }
}
