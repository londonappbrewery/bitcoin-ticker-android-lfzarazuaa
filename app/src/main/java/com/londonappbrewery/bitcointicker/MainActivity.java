package com.londonappbrewery.bitcointicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import com.loopj.android.http.*;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    // Constants:
    // TODO: Create the base URL
    private final String BASE_URL_BTC = "https://apiv2.bitcoinaverage.com/indices/global/ticker/BTCUSD";

    private final String BASE_URL_CURRENCY = "https://free.currconv.com/api/v7/convert";
    private final String BASE_CURRENCY = "USD_";
    private final String COMPACT_CURRENCY = "ultra";
    private final String APP_ID_CURRENCY = "9c5e1a8ae2773a0653f8";

    // Member Variables:
    TextView mPriceTextView;
    private String mUSDtoCurrencyValue = null;
    private String mBTCtoUSDValue = null;
    private double mBTCtoUSDLastValue = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPriceTextView = (TextView) findViewById(R.id.priceLabel);
        Spinner spinner = (Spinner) findViewById(R.id.currency_spinner);

        // Create an ArrayAdapter using the String array and a spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // TODO: Set an OnItemSelected listener on the spinner
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // The position of the view in the adapter.
                // The row id of the item that is selected.
                String selectedCountry = parent.getItemAtPosition(position).toString();
                Log.d("Bitcoin", "The selected country is: " + selectedCountry);
                RequestParams params = new RequestParams();
                params.put("q", BASE_CURRENCY + selectedCountry); // Currency to convert.
                params.put("compact", COMPACT_CURRENCY); // Parameter.
                params.put("apiKey", APP_ID_CURRENCY); // Api Key.
                getCurrencyRequestConversion(params, BASE_CURRENCY + selectedCountry);
                getBitcoinToUsd();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Bitcoin", "Nothing selected");
                Toast.makeText(MainActivity.this, "No Selected component.", Toast.LENGTH_SHORT);
            }
        });
    }



    // TODO: complete the letsDoSomeNetworking() method
    private void getBitcoinToUsd() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(BASE_URL_BTC, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d("Bitcoin", "Bitcoin JSON: " + response.toString());
                try {
                    mBTCtoUSDValue = response.getString("ask");
                    Log.d("Bitcoin", "BTCtoUSDValue: " + mUSDtoCurrencyValue);
                    updateUI();
                }
                catch (JSONException ex){
                    Log.e("Bitcoin", "Exception: " + ex);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Bitcoin", "Request Bitcoin fail! Status code: " + statusCode);
                Log.d("Bitcoin", "Fail JSON response: " + response);
                Log.e("Bitcoin", e.toString());
                Toast.makeText(MainActivity.this, "Bitcoin Request Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,String response, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Bitcoin", "Request Bitcoin fail! Status code: " + statusCode);
                Log.d("Bitcoin", "Fail String response: " + response);
                Log.e("Bitcoin", e.toString());
                Toast.makeText(MainActivity.this, "Bitcoin Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrencyRequestConversion(RequestParams paramaters, String fieldNameParam) {
        AsyncHttpClient client = new AsyncHttpClient();
        final String fieldName = fieldNameParam;
        client.get(BASE_URL_CURRENCY, paramaters, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d("Bitcoin", "Currency JSON: " + statusCode + ", " + response.toString());
                try {
                    mUSDtoCurrencyValue = response.getString(fieldName);
                    Log.d("Bitcoin", "UsdToCurrency: " + mUSDtoCurrencyValue);
                    updateUI();
                }
                catch (JSONException ex){
                    Log.e("Bitcoin", "Exception: " + ex);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Bitcoin", "Request currency fail! Status code: " + statusCode);
                Log.d("Bitcoin", "Fail response: " + response);
                Log.e("Bitcoin", e.toString());
                Toast.makeText(MainActivity.this, "Currency Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUI() {
        Log.d("Bitcoin", String.format("BTC: %s , Currency: %s", mBTCtoUSDValue, mUSDtoCurrencyValue));
        if(mUSDtoCurrencyValue!=null && mBTCtoUSDValue!=null){
            double bitcoinToUSD = Double.parseDouble(mBTCtoUSDValue);
            mBTCtoUSDLastValue = bitcoinToUSD;
            double usdToCurrency = Double.parseDouble(mUSDtoCurrencyValue);
            double bitcoinToCurrency = bitcoinToUSD * usdToCurrency;
            Log.d("Bitcoin", String.format("Result %.2f", bitcoinToCurrency));
            mPriceTextView.setText(String.format("%.2f", bitcoinToCurrency));
            mUSDtoCurrencyValue=null;
            mBTCtoUSDValue=null;
        }
        else if(mBTCtoUSDLastValue>=0 && mUSDtoCurrencyValue!=null){
            double usdToCurrency = Double.parseDouble(mUSDtoCurrencyValue);
            double bitcoinToCurrency = mBTCtoUSDLastValue * usdToCurrency;
            Log.d("Bitcoin", String.format("Result %.2f", bitcoinToCurrency));
            mPriceTextView.setText(String.format("%.2f", bitcoinToCurrency));
            mUSDtoCurrencyValue=null;
            mBTCtoUSDValue=null;
        }
    }



}
