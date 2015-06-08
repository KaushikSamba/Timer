package com.kaushiksamba.timer;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends ActionBarActivity {

    private static final String urlstring = "http://spider.nitt.edu/~vishnu/time.php";
    public int timer_length;
    TextView timer_text, fetched;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer_text = (TextView) findViewById(R.id.timer);
        fetched = (TextView) findViewById(R.id.fetched);
        set_timer_length(10);
        Handler handler = new Handler();
        handler.post(repeat);
        Button button = (Button) findViewById(R.id.exit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void set_timer_length(int n)
    {
        timer_length = n;
        timer_text.post(set_timer_length_runnable);
    }
    Runnable set_timer_length_runnable = new Runnable() {
        @Override
        public void run() {
            timer_text.setText(String.valueOf(timer_length));
        }
    };
    private void update_field()
    {
        fetched.post(update_fetched_runnable);
    }

    Runnable update_fetched_runnable = new Runnable() {
        @Override
        public void run() {
            fetched.setText(result_string);
        }
    };

    private int extract_number()
    {
        Long n = Long.parseLong(result_string);
        n=n%10;
        return n.intValue();
    }

    String result_string;
    Runnable scrape_runnable = new Runnable() {
        @Override
        public void run() {
            try {
                result_string = scrape();
            } catch (IOException e) {
                e.printStackTrace();
            }
            update_field();
            set_timer_length(extract_number());

            new Thread()
            {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(repeat);
                }
            }.start();
        }
    };

    Runnable repeat = new Runnable() {
        @Override
        public void run() {
            timer();
            Handler handler = new Handler();
            handler.postDelayed(get_data, timer_length * 1000);
        }
    };

    Runnable get_data = new Runnable() {
        @Override
        public void run() {
            new Thread(scrape_runnable).start();
        }
    };

    public String scrape() throws IOException
    {
        URL url = new URL(urlstring);
        HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection();
        httpurlconnection.setRequestMethod("GET");
        int rc = httpurlconnection.getResponseCode();
        if(httpurlconnection.getResponseCode()==HttpURLConnection.HTTP_OK)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String oneline;
            oneline=reader.readLine();
            while(oneline!=null)
            {
                sb.append(oneline);
                oneline=reader.readLine();
            }
            reader.close();
            httpurlconnection.disconnect();
            return sb.toString();
        }
        return null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Integer time;
    public void timer()
    {
        final Handler handler = new Handler();
        time = timer_length;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                time--;
                timer_text.setText(time.toString());
            }
        };
        for(int x = 1000; x<=timer_length*1000; x+=1000) handler.postDelayed(r,x);
    }
}
