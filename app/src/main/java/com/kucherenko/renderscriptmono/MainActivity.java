package com.kucherenko.renderscriptmono;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Bitmap mBitmapIn;
    private Bitmap mBitmapOut;

    private RenderScript mRS;
    private Allocation mInAllocation;
    private Allocation mOutAllocation;
    private ScriptC_mono mScript;

    private static final String[] IMAGES = {"960x541", "700x394", "500x281", "300x169"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSpinner();

        final ImageView out = (ImageView) findViewById(R.id.displayOutImageView);
        //out.setImageBitmap(mBitmapOut);

        final TextView resultTextView = (TextView) findViewById(R.id.executionTimeTextView);

        Button renderScriptButton = (Button) findViewById(R.id.renderScriptButton);
        final Button javaButton = (Button) findViewById(R.id.javaButton);

        renderScriptButton.setOnClickListener(v -> {
            long start = android.os.SystemClock.uptimeMillis();
            renderScriptMonoChromeFilter();
            long end = android.os.SystemClock.uptimeMillis();
            long result = end - start;
            out.setImageBitmap(mBitmapOut);
            resultTextView.setText(String.format(getString(R.string.execution_time), result));
        });
        javaButton.setOnClickListener(v -> {
            long start = android.os.SystemClock.uptimeMillis();
            javaMonoChromeFilter();
            long end = android.os.SystemClock.uptimeMillis();
            long result = end - start;
            out.setImageBitmap(mBitmapOut);
            resultTextView.setText(String.format(getString(R.string.execution_time), result));
        });

    }

    private void initSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, IMAGES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                int res = R.drawable.house_960_541;
                switch (position) {
                    case 0: {
                        res = R.drawable.house_960_541;
                        break;
                    }
                    case 1: {
                        res = R.drawable.house_700_394;
                        break;
                    }
                    case 2: {
                        res = R.drawable.house_500_281;
                        break;
                    }
                    case 3: {
                        res = R.drawable.house_300_169;
                        break;
                    }
                }

                mBitmapIn = loadBitmap(res);
                mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(), mBitmapIn.getHeight(),
                        mBitmapIn.getConfig());

                ImageView in = (ImageView) findViewById(R.id.displayInImageView);
                in.setImageBitmap(mBitmapIn);

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void javaMonoChromeFilter() {
        float monoMult[] = {0.299f, 0.587f, 0.114f};
        int inPixels[] = new int[mBitmapIn.getHeight() * mBitmapIn.getWidth()];
        int outPixels[] = new int[mBitmapOut.getHeight() * mBitmapOut.getWidth()];
        mBitmapIn.getPixels(inPixels, 0, mBitmapIn.getWidth(), 0, 0,
                mBitmapIn.getWidth(), mBitmapIn.getHeight());
        for(int i = 0;i < inPixels.length;i++) {
            float r = (float)(inPixels[i] & 0xff);
            float g = (float)((inPixels[i] >> 8) & 0xff);
            float b = (float)((inPixels[i] >> 16) & 0xff);

            int mono = (int)(r * monoMult[0] + g * monoMult[1] + b * monoMult[2]);

            outPixels[i] = mono + (mono << 8) + (mono << 16) + (inPixels[i] & 0xff000000);
        }
        mBitmapOut.setPixels(outPixels, 0, mBitmapOut.getWidth(), 0, 0,
                mBitmapOut.getWidth(), mBitmapOut.getHeight());
    }

    private void renderScriptMonoChromeFilter() {
        mRS = RenderScript.create(this);

        mInAllocation = Allocation.createFromBitmap(mRS, mBitmapIn,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        mOutAllocation = Allocation.createFromBitmap(mRS, mBitmapOut,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);

        mScript = new ScriptC_mono(mRS);

        mScript.forEach_root(mInAllocation, mOutAllocation);
        mOutAllocation.copyTo(mBitmapOut);
    }

    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }
}
