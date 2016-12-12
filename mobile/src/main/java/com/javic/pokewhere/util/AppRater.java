package com.javic.pokewhere.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.javic.pokewhere.R;

/**
 * Created by vagprogrammer on 11/12/16.
 */

public class AppRater {

    private final static String APP_TITLE = "PokeEASY";// App Name
    private final static String APP_PNAME = "com.javic.pokewhere.util";// Package Name

    private final static int DAYS_UNTIL_PROMPT = 1;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 5;//Min number of launches


    public static void app_launched(Context mContext) {

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_POKEWHERE, mContext.MODE_PRIVATE);

        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);

        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {

                if (prefs.getBoolean("showenjoy", true)) {
                    showEnjoyDialog(mContext, editor);
                } else {
                    showRateDialog(mContext, editor);
                }

            }
        }

        editor.apply();
    }

    private static void showEnjoyDialog(final Context mContext, final SharedPreferences.Editor editor) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        builder.cancelable(false);

        builder.title(mContext.getString(R.string.title_categoria_rate));
        builder.titleGravity(GravityEnum.CENTER);
        builder.positiveText(mContext.getString(R.string.txt_dialog_rate_btn_positive));
        builder.negativeText(mContext.getString(R.string.txt_dialog_rate_btn_negative));
        builder.negativeColorRes(R.color.color_negro_degradado_neutral);
        builder.checkBoxPromptRes(R.string.txt_dialog_rate_btn_dont_ask_again, false, null);

        builder.onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.POSITIVE) {

                    if (editor != null) {
                        editor.putBoolean("showenjoy", false);
                        editor.commit();
                    }

                    showRateDialog(mContext, editor);
                } else if (which == DialogAction.NEGATIVE) {

                    if (dialog.isPromptCheckBoxChecked()) {
                        if (editor != null) {
                            editor.putBoolean("dontshowagain", true);
                            editor.commit();
                        }
                    } else {
                        if (editor != null) {
                            editor.putLong("launch_count", 0);
                            editor.commit();
                        }
                    }

                }
            }
        });

        MaterialDialog dialog = builder.build();
        dialog.show();

    }


    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        builder.stackingBehavior(StackingBehavior.ADAPTIVE);
        builder.cancelable(false);

        builder.title(mContext.getString(R.string.txt_rate_tit_pref));
        builder.titleGravity(GravityEnum.CENTER);
        builder.content(mContext.getString(R.string.txt_rate_summ_pref));
        builder.contentColorRes(R.color.color_background_stroke_user_profile);
        builder.positiveText(mContext.getString(R.string.txt_dialog_btn_positive));

        builder.negativeText(mContext.getString(R.string.txt_dialog_btn_neutral));
        builder.negativeColorRes(R.color.color_background_stroke_user_profile);

        builder.neutralText(mContext.getString(R.string.txt_dialog_btn_negative));
        builder.neutralColorRes(R.color.color_negro_degradado_neutral);

        builder.onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.POSITIVE) {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }

                    final String packageName = Constants.PACKAGE_NAME;
                    String url = "";

                    try {
                        //Check whether Google Play store is installed or not:
                        mContext.getPackageManager().getPackageInfo("com.android.vending", 0);

                        url = "market://details?id=" + packageName;
                    } catch (final Exception e) {
                        url = "https://play.google.com/store/apps/details?id=" + packageName;
                    }


                    //Open the app page in Google Play store:
                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    mContext.startActivity(intent);

                } else if (which == DialogAction.NEGATIVE) {
                    if (editor != null) {
                        editor.putLong("launch_count", 0);
                        editor.commit();
                    }
                } else if (which == DialogAction.NEUTRAL) {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                }
            }
        });

        MaterialDialog dialog = builder.build();
        dialog.show();

    }
}