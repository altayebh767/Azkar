package com.azkar.counter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String[] NAMES = {
            "سبحان الله",
            "الحمد لله",
            "الله أكبر",
            "لا إله إلا الله",
            "أستغفر الله",
            "اللهم صلِّ وسلِّم على نبينا محمد",
            "سبحان الله وبحمده، سبحان الله العظيم",
            "لا حول ولا قوة إلا بالله"
    };
    private static final int[] TARGETS = {33, 33, 34, 100, 100, 100, 100, 100};

    private static final int GREEN = Color.parseColor("#1B5E4B");
    private static final int BG = Color.parseColor("#F4F1EA");

    private SharedPreferences prefs;
    private Vibrator vibrator;
    private int index;

    private TextView dhikrView;
    private TextView countView;
    private TextView progressText;
    private TextView totalView;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("azkar", MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        index = prefs.getInt("index", 0);
        if (index < 0 || index >= NAMES.length) index = 0;

        buildUi();
        refresh();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(20), dp(36), dp(20), dp(20));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("عداد الأذكار");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(GREEN);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        dhikrView = new TextView(this);
        dhikrView.setTextSize(26);
        dhikrView.setTextColor(Color.parseColor("#222222"));
        dhikrView.setGravity(Gravity.CENTER);
        dhikrView.setPadding(dp(8), dp(24), dp(8), dp(4));
        dhikrView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDhikr();
            }
        });
        root.addView(dhikrView, matchWrap());

        TextView hint = new TextView(this);
        hint.setText("اضغط على الذكر لتغييره");
        hint.setTextSize(12);
        hint.setTextColor(Color.GRAY);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint, matchWrap());

        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams barLp =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(10));
        barLp.topMargin = dp(20);
        root.addView(bar, barLp);

        progressText = new TextView(this);
        progressText.setTextSize(16);
        progressText.setTextColor(GREEN);
        progressText.setGravity(Gravity.CENTER);
        progressText.setPadding(0, dp(6), 0, 0);
        root.addView(progressText, matchWrap());

        // الزر الدائري الكبير للعد
        countView = new TextView(this);
        countView.setTextSize(64);
        countView.setTypeface(Typeface.DEFAULT_BOLD);
        countView.setTextColor(Color.WHITE);
        countView.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(GREEN);
        countView.setBackground(circle);
        countView.setClickable(true);
        countView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });
        LinearLayout.LayoutParams circleLp = new LinearLayout.LayoutParams(dp(230), dp(230));
        circleLp.topMargin = dp(28);
        root.addView(countView, circleLp);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        Button reset = new Button(this);
        reset.setText("تصفير");
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmReset();
            }
        });
        Button target = new Button(this);
        target.setText("تغيير الهدف");
        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTarget();
            }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btnLp.setMargins(dp(6), 0, dp(6), 0);
        row.addView(reset, btnLp);
        row.addView(target, btnLp);
        LinearLayout.LayoutParams rowLp = matchWrap();
        rowLp.topMargin = dp(28);
        root.addView(row, rowLp);

        totalView = new TextView(this);
        totalView.setTextSize(14);
        totalView.setTextColor(Color.DKGRAY);
        totalView.setGravity(Gravity.CENTER);
        totalView.setPadding(0, dp(16), 0, 0);
        root.addView(totalView, matchWrap());

        setContentView(root);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int target() {
        return prefs.getInt("t" + index, TARGETS[index]);
    }

    private int count() {
        return prefs.getInt("c" + index, 0);
    }

    private void increment() {
        int c = count() + 1;
        int t = target();
        prefs.edit()
                .putInt("c" + index, c)
                .putInt("total", prefs.getInt("total", 0) + 1)
                .apply();
        if (t > 0 && c % t == 0) {
            buzz(300);
        } else {
            buzz(20);
        }
        refresh();
    }

    private void buzz(long ms) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(ms);
        }
    }

    private void refresh() {
        int c = count();
        int t = target();
        dhikrView.setText(NAMES[index]);
        countView.setText(String.valueOf(c));
        totalView.setText("إجمالي التسبيحات: " + prefs.getInt("total", 0));

        if (t > 0) {
            int rounds = c / t;
            int inRound = c - rounds * t;
            if (inRound == 0 && c > 0) inRound = t;
            bar.setMax(t);
            bar.setProgress(inRound);
            String s = inRound + " / " + t;
            if (c > t || (c == t)) {
                int done = (c % t == 0) ? c / t : rounds;
                s += "   ·   الدورات المكتملة: " + done;
            }
            progressText.setText(s);
        } else {
            bar.setMax(1);
            bar.setProgress(0);
            progressText.setText("بدون هدف");
        }
    }

    private void chooseDhikr() {
        new AlertDialog.Builder(this)
                .setTitle("اختر الذكر")
                .setItems(NAMES, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int which) {
                        index = which;
                        prefs.edit().putInt("index", which).apply();
                        refresh();
                    }
                })
                .show();
    }

    private void changeTarget() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(target()));
        input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this)
                .setTitle("الهدف (0 = بدون هدف)")
                .setView(input)
                .setPositiveButton("حفظ", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int which) {
                        int t = 0;
                        try {
                            t = Integer.parseInt(input.getText().toString().trim());
                        } catch (NumberFormatException ignored) {
                        }
                        prefs.edit().putInt("t" + index, Math.max(0, t)).apply();
                        refresh();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("تصفير عداد هذا الذكر؟")
                .setPositiveButton("تصفير", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int which) {
                        prefs.edit().putInt("c" + index, 0).apply();
                        refresh();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // العد بأزرار الصوت
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getRepeatCount() == 0) increment();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
