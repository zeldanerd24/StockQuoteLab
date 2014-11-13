package com.kevinrothenberger.lab6;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


public class MyActivity extends Activity {

    EditText symbol_input;
    Button load_stock_button;
    TextView name;
    TextView price;
    TextView symbol;
    TextView ts;
    TextView type;
    TextView utc_time;
    TextView volume;

    String stockSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String json = msg.getData().getString("json");
                try {
                    JSONObject stock = new JSONObject(json);
                    JSONObject list = stock.getJSONObject("list");
                    JSONArray resources = list.getJSONArray("resources");
                    JSONObject resourceObject = resources.getJSONObject(0);
                    JSONObject resource = resourceObject.getJSONObject("resource");
                    JSONObject quote = resource.getJSONObject("fields");

                    name.setText(quote.getString("name"));
                    price.setText("$" + quote.getString("price"));
                    symbol.setText(quote.getString("symbol"));
                    ts.setText(quote.getString("ts"));
                    type.setText(quote.getString("type"));
                    utc_time.setText(quote.getString("utctime"));
                    volume.setText(quote.getString("volume"));
                } catch (JSONException e) {
                    //oops
                }
            }
        };

        symbol_input = (EditText) findViewById(R.id.symbol_input);
        load_stock_button = (Button) findViewById(R.id.load_stock_button);
        name = (TextView) findViewById(R.id.name);
        price = (TextView) findViewById(R.id.price);
        symbol = (TextView) findViewById(R.id.symbol);
        ts = (TextView) findViewById(R.id.ts);
        type = (TextView) findViewById(R.id.type);
        utc_time = (TextView) findViewById(R.id.utc_time);
        volume = (TextView) findViewById(R.id.volume);

        load_stock_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stockSymbol = symbol_input.getText().toString();
                double price = -1;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String sym = symbol_input.getText().toString();
                        while(true) {
                            try {
                                //System.out.println(stockSymbol);
                                //System.out.println("Updated Thread 1");

                                if(!sym.equals(stockSymbol)){
                                    break;
                                }

                                URL url = new URL("http://finance.yahoo.com/webservice/v1/symbols/" + sym + "/quote?format=json");
                                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                                String line;
                                StringBuilder json = new StringBuilder();
                                while ((line = in.readLine()) != null) {
                                    json.append(line);
                                    json.append("\n");
                                }
                                in.close();

                                Message msg = handler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putString("json", json.toString());
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                                Thread.sleep(10000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
