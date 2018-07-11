package com.komodaa.app.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.github.kevinsawicki.timeago.TimeAgo;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.LoginActivity;
import com.komodaa.app.activities.SignupActivity;
import com.komodaa.app.models.Attribute;
import com.komodaa.app.models.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by nevercom on 8/22/16.
 */
public class Utils {
    public static final int MODE_DEFAULT = 0;
    public static final int MODE_MINIMAL = 1;
    public static final int MODE_RELATIVE = 2;
    private static String[] monthNames = {
            "فروردین",
            "اردیبهشت",
            "خرداد",
            "تیر",
            "مرداد",
            "شهریور",
            "مهر",
            "آبان",
            "آذر",
            "دی",
            "بهمن",
            "اسفند"
    };

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isEmailValid(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);

            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            Log.e("SET_FONT", "Can not set custom font " + customFontFileNameInAssets + " instead of " + defaultFontNameToOverride);
        }
    }

    public static boolean isPersianCharactersOnly(final String str) {

        return str.matches("^[\\u0600-\\u06FF0-9 ]+$");
    }

    /**
     * Use this method to get Current date and time
     *
     * @return <b>String</b> Current Date and time (yyyy-MM-dd HH:mm:ss)
     */
    public static String getCurrentDate(String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(new Date());

    }

    /**
     * Use this method to get Current date and time
     *
     * @return <b>String</b> Current Date and time (yyyy-MM-dd HH:mm:ss)
     */
    public static String getCurrentDate() {
        return getCurrentDate("yyyy-MM-dd HH:mm:ss");

    }

    public static int getDaysBetween(String startDate, String endDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date d1 = sdf.parse(startDate);
            Date d2 = sdf.parse(endDate);
            if (d2.before(d1)) {
                return 0;
            }
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(d1);
            c2.setTime(d2);

            long milis1 = c1.getTimeInMillis();
            long milis2 = c2.getTimeInMillis();

            // Calculate difference in milliseconds
            long diff = Math.abs(milis2 - milis1);

            return (int) (diff / (24 * 60 * 60 * 1000));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getAndroidVersion() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public static String getDeviceName() {
        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        Log.i("device name", model);
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        final char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * Converts date in <b>Gregorian</b> calendar system to date in
     * <b>Jalali</b> calendar system
     *
     * @param date Gregorian date to be converted to jalali
     * @return Jalali date
     */
    public static String getPersianDateText(String date, boolean includeTime) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        String pattern = includeTime ? "yyyy-MM-dd hh:mm:ss" : "yyyy-MM-dd";
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern,
                Locale.US);
        String dateTime = "";
        try {
            final Date _date = sdf.parse(date);
            final Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(_date);
            final JDF jdf = new JDF(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));
            dateTime = String.format(Locale.US, "%d %s %d", jdf.getIranianDay(), monthNames[jdf.getIranianMonth() - 1], jdf.getIranianYear())
                    + (includeTime ? (" ساعت " + cal.get(Calendar.HOUR_OF_DAY) + ":"
                    + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND))
                    : "");

        } catch (final ParseException e) {
        }
        return dateTime;
    }

    /**
     * Converts date in <b>Gregorian</b> calendar system to date in
     * <b>Jalali</b> calendar system
     *
     * @param date Gregorian date to be converted to jalali
     * @return Jalali date
     */
    public static String getPersianChatTime(String date) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        String pattern = "yyyy-MM-dd hh:mm:ss";
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern,
                Locale.US);
        String dateTime = "";
        try {
            final Date _date = sdf.parse(date);

            final Calendar mnCal = Calendar.getInstance(Locale.US);
            mnCal.set(Calendar.HOUR_OF_DAY, 0);
            mnCal.set(Calendar.MINUTE, 0);
            mnCal.set(Calendar.SECOND, 0);
            mnCal.set(Calendar.MILLISECOND, 0);


            final Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(_date);
            final JDF jdf = new JDF(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));

            String time = cal.get(Calendar.HOUR_OF_DAY) + ":"
                    + cal.get(Calendar.MINUTE);
            if (_date.before(mnCal.getTime())) {
                return String.format(Locale.US, "%d %s %d", jdf.getIranianDay(), monthNames[jdf.getIranianMonth() - 1], jdf.getIranianYear())
                        + (" ساعت " + time);
            }
            return time;

        } catch (final ParseException e) {
        }
        return dateTime;
    }

    /**
     * Checks if the Network is available.
     *
     * @param context Context of Activity
     * @return <b>Boolean</b>: true if available.
     */
    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            @SuppressWarnings("deprecation") final NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (final NetworkInfo element : info) {
                    if (element.getState() == NetworkInfo.State.CONNECTED
                            || element.getState() == NetworkInfo.State.CONNECTING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String formatNumber(int number) {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        final DecimalFormat decimalFormat = new DecimalFormat("###,###,###", symbols);
        return decimalFormat.format(number);
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static Uri resourceToUri(Context context, int resID) {
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID));

        Log.d("URI", uri.toString());
        return uri;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap scaleImage(Context context, Bitmap bitmap, int size) {
        Bitmap scaledBitmap = null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = dpToPx(context, size);

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return scaledBitmap;


    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d("inSampleSize", inSampleSize + "");
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri,
                                                    int reqWidth, int reqHeight) {

        try {
            Log.d("Decode URI", uri.toString());
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
//            AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
//            BitmapFactory.decodeFileDescriptor(
//                    fileDescriptor.getFileDescriptor(), null, options);
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(input, null, options);
            input.close();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("Decode From URI", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String readText(Context context, String fileName) {

        AssetManager am = context.getAssets();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            InputStream inputStream = am.open(fileName);
            int i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString().trim();
    }

    public static void displayLoginErrorDialog(final Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("باید وارد حساب کاربری بشی !")
                .setContentText("اگه قبلا ثبت نام کردی، بزن «ورود» و با ایمیل و رمز حساب ات بپر تو کمدا. اگه نه، جا نمونی! «ثبت نام» کن.")
                .setConfirmText("ورود")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                })
                .setCancelText("ثبت نام")
                .setCancelClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    context.startActivity(new Intent(context, SignupActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                }).show();
    }

    @NonNull public static List<Category> getCategories() {
        String json = App.getPrefs().getString("config", null);

        List<Category> categories = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray cats = new JSONObject(json).getJSONArray("cats");
                for (int i = 0; i < cats.length(); i++) {
                    JSONObject catItem = cats.getJSONObject(i);

                    Category c = new Category();
                    c.setId(catItem.getInt("id"));
                    c.setName(catItem.getString("name"));
                    JSONArray atts = catItem.getJSONArray("attribs");
                    for (int j = 0; j < atts.length(); j++) {
                        JSONObject aObj = atts.getJSONObject(j);
                        Attribute a = new Attribute();
                        a.setId(aObj.getInt("id"));
                        a.setName(aObj.getString("name"));

                        c.addAttribute(a);
                    }
                    categories.add(c);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return categories;
    }

    public static String getCategoryName(int categoryId) {
        List<Category> categories = getCategories();
        for (Category cat : categories) {
            if (cat.getId() == categoryId) {
                return cat.getName();
            }
        }
        return "";
    }

    public static String getCityName(Context c, int cityId) {
        String[] cities = c.getResources().getStringArray(R.array.cities);
        try {
            return cities[cityId];
        } catch (Exception ignored) {

        }
        return "";
    }

    public static Drawable getDrawableFromRes(Context context, @DrawableRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(resId);
        }
        return context.getResources().getDrawable(resId);
    }

    public static int getColorFromResource(Context context, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(color);
        }
        return context.getResources().getColor(color);
    }

    public static SpannableString generateTypeFacedString(Context c, String string) {
        SpannableString s = new SpannableString(string);
        s.setSpan(new TypefaceSpan(c, "IRANSans.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public static int getDisplayContentHeight(Activity c) {
        final WindowManager windowManager = c.getWindowManager();
        final Point size = new Point();
        int screenHeight = 0, navigationBar = 0;

        int contentTop = c.findViewById(android.R.id.content).getTop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(size);
            screenHeight = size.y;
        } else {
            Display d = windowManager.getDefaultDisplay();
            screenHeight = d.getHeight();
        }
        return screenHeight - contentTop - getNavigationBarHeight(c);
    }

    public static int getNavigationBarHeight(Context c) {
        Resources resources = c.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static boolean isActivityVisitedForFirstTime(Context context) {

        SharedPreferences settings = App.getPrefs();
        final boolean ret = settings.getBoolean(context.getClass().getName()
                + ".firstVisit", true);
        if (ret) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(context.getClass().getName() + ".firstVisit", false);
            editor.apply();
        }
        return ret;
    }

    /**
     * Validate the check digit for an the IBAN code.
     *
     * @param code The code to validate
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>
     */
    public static boolean isIBANValid(String code) {
        if (code == null || code.length() < 5) {
            return false;
        }
        try {
            int modulusResult = calculateModulus(code);
            return (modulusResult == 1);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Converts date in <b>Gregorian</b> calendar system to date in
     * <b>Jalali</b> calendar system
     *
     * @param date Gregorian date to be converted to jalali, as UNIX Timestamp
     * @param mode if this parameter is <b>TRUE</b>, converted date is in
     *             format of: "<i>yy-MM-dd</i>", otherwise
     *             "<i>yyyy-MM-dd hh:mm:ss</i>"
     * @return Jalali date
     */
    public static String getPersianDate(String date, int mode) {
        TimeAgo ago = new TimeAgo();
        ago.setSuffixAgo("پیش");
        ago.setSeconds("یک دقیقه");
        ago.setMinute("یک دقیقه");
        ago.setMinutes("{0} دقیقه");
        ago.setHour("یک ساعت");
        ago.setHours("{0} ساعت");
        ago.setDay("یک روز");
        ago.setDays("{0} روز");
        ago.setMonth("یک ماه");
        ago.setMonths("{0} ماه");

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
                Locale.US);

        String dateTime = "";
        try {
            final Date _date = sdf.parse(date);
            final Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(_date);
            final JDF jdf = new JDF(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));
            final String irDate = jdf.getIranianDate();
            switch (mode) {
                case Utils.MODE_RELATIVE:
                    return ago.timeAgo(_date);
                case Utils.MODE_MINIMAL:
                    return irDate.substring(2);
                case Utils.MODE_DEFAULT:
                    return irDate + " (" + cal.get(Calendar.HOUR_OF_DAY) + ":"
                            + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + ")";
                default:
                    return irDate + " (" + cal.get(Calendar.HOUR_OF_DAY) + ":"
                            + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + ")";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    /**
     * Calculate the modulus for a code.
     *
     * @param code The code to calculate the modulus for.
     * @return The modulus value
     * @throws Exception if an error occurs calculating the modulus
     *                   for the specified code
     */
    private static int calculateModulus(String code) throws Exception {
        String reformattedCode = code.substring(4) + code.substring(0, 4);
        long total = 0;
        for (int i = 0; i < reformattedCode.length(); i++) {
            int charValue = Character.getNumericValue(reformattedCode.charAt(i));
            if (charValue < 0 || charValue > 35) {
                throw new Exception("Invalid Character[" +
                        i + "] = '" + charValue + "'");
            }
            total = (charValue > 9 ? total * 100 : total * 10) + charValue;
            if (total > 999999999) {
                total = (total % 97);
            }
        }
        return (int) (total % 97);
    }

    /**
     * Converts date in <b>Gregorian</b> calendar system to date in
     * <b>Jalali</b> calendar system
     *
     * @param date    Gregorian date to be converted to jalali
     * @param minimal if this parameter is <b>TRUE</b>, converted date is in
     *                format of: "<i>yy-MM-dd</i>", otherwise
     *                "<i>yyyy-MM-dd hh:mm:ss</i>"
     * @return Jalali date
     */
    public String getPersianDate(String date, boolean minimal) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
                Locale.US);
        String dateTime = "";
        try {
            final Date _date = sdf.parse(date);
            final Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(_date);
            final JDF jdf = new JDF(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));
            final String irDate = jdf.getIranianDate();
            if (minimal) {
                dateTime = irDate.substring(2);
            } else {
                dateTime = irDate + " (" + cal.get(Calendar.HOUR_OF_DAY) + ":"
                        + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + ")";
            }

        } catch (final ParseException e) {
        }
        return dateTime;
    }
}
