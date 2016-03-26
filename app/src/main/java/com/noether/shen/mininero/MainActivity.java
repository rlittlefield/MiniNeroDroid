package com.noether.shen.mininero;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import org.json.*; //json response handler
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header; //gets pulled in with the loopj stuff

import com.noether.shen.mininero.TweetNaclFast.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SQLiteDatabase sql;
    Cursor c;

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //attempt to load address book..
        loadDB();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }
        if (id == R.id.action_addresses) {
             try {
                loadAddress();
            } catch (Exception dberr) {
                showToast("unknown sqlite db error for loading saved addresses");
            }
        }
        if (id == R.id.action_balance) {
            Context context = getApplicationContext();
            if (isNetworkConnected()) {
                try {
                    JsonRequestGetTime(); //this will be moved to app launch later, so the offset is preloaded
                    JsonRequestGetBalance();
                } catch (Exception e) {
                    Log.d("asdf", "is your server running");
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "server ip error!", duration);
                    toast.show();
                }
            } else {
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "no network!", duration);
                toast.show();
            }
                }
        if (id == R.id.action_my_addr) {
            try {
                JsonRequestGetAddress();
            } catch (Exception addrexc) {
                Log.d("asdf","error in getaddress");
            }
        }
        if (id == R.id.action_account) {
            //JsonRequestGetTransactions
            //display in webview
            //need to add decryption here also..
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
             Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
        }
        if (id == R.id.nav_addresses) {
//            Intent settingIntent = new Intent(this, ItemListActivity.class);
            //startActivity(settingIntent);

            try {
                loadAddress();
            } catch (Exception dberr) {
                showToast("unknown sqlite db error for loading saved addresses");
            }
        }
        if (id == R.id.nav_balance) {
            Context context = getApplicationContext();
            if (isNetworkConnected()) {
                try {
                    JsonRequestGetTime(); //this will be moved to app launch later, so the offset is preloaded
                    JsonRequestGetBalance();
                } catch (Exception e) {
                    Log.d("asdf", "is your server running");
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "server ip error!", duration);
                    toast.show();
                }
            } else {
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "no network!", duration);
                toast.show();
            }
                }
        if (id == R.id.nav_my_addr) {
            try {
                JsonRequestGetAddress();
            } catch (Exception errr){
                Log.d("asdf","didn't get address");
            }
        }
        if (id == R.id.nav_account) {
            //JsonRequestGetTransactions
            //display in webview
            //need to add decryption here also..
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadDB() {
        //can move this around or encrypt it later..
        Context context = getApplicationContext();
        try {
            String fileName = "SavedAddresses.db";
            //String filePath = "/storage/sdcard0/" + Environment.DIRECTORY_DOCUMENTS + File.separator + fileName;
            sql = this.openOrCreateDatabase(fileName, MODE_PRIVATE, null );
        } catch (Exception dberr) {
            Log.d("asdf", "unknown db error");
            showToast("unknown android sqlite error - unable to store or load addresses locally");
        }
    }

    public List<SavedAddress> getAddresses () {
        List<SavedAddress> addressList = new ArrayList<SavedAddress>();
        String selectQuery = "SELECT  * FROM SavedAddress";
        try {
            Cursor cursor = sql.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                Log.d("asdf", "got move to first");
                do {
                    SavedAddress sa = new SavedAddress();
                    sa.Id = Integer.parseInt(cursor.getString(0));
                    sa.address = cursor.getString(1);
                    sa.Name = cursor.getString(2);
                    addressList.add(sa);
                } while (cursor.moveToNext());
            } else {
                //preinitialize with creator address + monero dev address if it's empty
                //user can delete these if they want..
                SavedAddress me = new SavedAddress();
                me.Id = 0;
                me.Name = "shen";
                me.address = "4AjCAP7WoojjdydwkgvEyxRfxHNLhxbBz4FeLug5gW4WLJ13VnhXtrW7uk5fcLKUarTVpJtcWxRheUd7etWG9c8VHwA8gFC";
                SavedAddress xmr = new SavedAddress();
                xmr.Id = 1;
                xmr.Name = "xmr dev donate";
                xmr.address = "44AFFq5kSiGBoZ4NMDwYtN18obc8AemS33DBLWs3H7otXft3XjrpDtQGv7SqSsaBYBb98uNbr2VBBEt7f2wfn3RVGQBEP3A";
                addressList.add(me);
                addressList.add(xmr);
            }
        } catch (Exception dberr) {
                 //preinitialize with creator address + monero dev address if it's empty
                //user can delete these if they want..
                SavedAddress me = new SavedAddress();
                me.Id = 0;
                me.Name = "shen";
                me.address = "4AjCAP7WoojjdydwkgvEyxRfxHNLhxbBz4FeLug5gW4WLJ13VnhXtrW7uk5fcLKUarTVpJtcWxRheUd7etWG9c8VHwA8gFC";
                SavedAddress xmr = new SavedAddress();
                xmr.Id = 1;
                xmr.Name = "xmr dev donate";
                xmr.address = "44AFFq5kSiGBoZ4NMDwYtN18obc8AemS33DBLWs3H7otXft3XjrpDtQGv7SqSsaBYBb98uNbr2VBBEt7f2wfn3RVGQBEP3A";
                addressList.add(me);
                addressList.add(xmr);
        }
        return addressList;
    }

    public void loadAddress() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Load Address");
        final List<SavedAddress> sa = getAddresses();
        String[] names = new String[sa.size()];
        for (int i = 0; i < sa.size(); i++) {
            names[i] = sa.get(i).Name;
        }
        b.setItems(names, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                String dest = sa.get(which).address;
                EditText desttext = (EditText) findViewById(R.id.destination_text);
                desttext.setText(dest);
            }

        });
        b.show();
    }

    public void onScanClick(View view) {
        //new IntentIntegrator(this).initiateScan();
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
        integrator.initiateScan();
    }

    //parses scanning
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                // handle scan result as in mn uwp app...
                String dest = scanResult.getContents().toString();
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, dest, duration);
                toast.show();
                EditText desttext = (EditText) findViewById(R.id.destination_text);
                desttext.setText(dest);
            } else {
                //Context context = getApplicationContext();
                //int duration = Toast.LENGTH_SHORT;
                //Toast toast = Toast.makeText(context, "No valid address found", duration);
                //toast.show();
            }
            // else continue with any other code you need in the method
        } catch (Exception e) {
            Log.d("asdf","scan parsing error");
        }
    }


    /*
   *  Json requesting from mininodo stuff
   *
     */

    public long now() {
        return (long) (System.currentTimeMillis() / 1000);
        //return Convert.ToInt64(DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalSeconds);
    }

    public long computeBackwardsOffset(String timestamp) {
        long last = Long.parseLong(timestamp);   // Convert.ToInt64(timestamp);
        long offset = last - now();
        return offset;
    }

    //attempted rewrite using mininodo client
   public void JsonRequestGetTime() throws Exception {
       final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
       String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
       Log.d("asdf", "requesting:"+mnip);
       MiniNodoClient mnc = new MiniNodoClient(mnip);

       //use this handler if it's just a string, not a json..
       mnc.get("/api/mininero", null, new AsyncHttpResponseHandler() {

           @Override
           public void onSuccess(int statusCode, Header[] headers, byte[] response) {
               // called when response HTTP status is "200 OK"
               Log.d("asdf", "time2success");
               try {
                   String t2 = new String(response, "UTF-8");
                   Log.d("asdf", "response is:"+t2);
                   SharedPreferences.Editor editor = SP.edit();
                   editor.putLong("offsetCB", computeBackwardsOffset(t2) );
                   Log.d("asdf", "computed offset as:"+Long.toString(computeBackwardsOffset(t2)));
                   editor.commit();
                   /*
                   Int64 cb_time = Convert.ToInt64(s.ToString());
                   offsetCB = cb_time - Convert.ToInt64(DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalSeconds);
                   System.Diagnostics.Debug.WriteLine("computed offest as..:" + offsetCB.ToString());
                   Windows.Storage.ApplicationData.Current.LocalSettings.Values["offset"] = offsetCB;
                   */
               } catch (UnsupportedEncodingException ue) {
                   Log.d("asdf", "bad encoding");
               }
           }

           @Override
           public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
               // called when response HTTP status is "4XX" (eg. 401, 403, 404)
               Log.d("asdf", "time2failure");
           }
       });
   }


    public String GenerateSignature(String message) {
        //obviously this is a dummy sk just here for testing
        String sk = "167e5760ac648c09fc965d9f42104c1838e82a0e51ba939032e628b0af2e0a01";
        sk = "abaa0ebb990938a509251f1e75ce69aa0721c8969c577698fc9ea405a8a9bba87ea2d03244b32fb47eca62c02fe89ec6b4c71585a69ae56181e7ab2e3d75feef";

        String rv = "";
        //Signature.KeyPair skpk = Signature.keyPair_fromSeed(TweetNaclFast.hexDecode(sk)); //use if 32 byte
        Signature.KeyPair skpk = Signature.keyPair_fromSecretKey(TweetNaclFast.hexDecode(sk)); //use if 64 byte
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        TweetNaclFast.Signature Ed25519 = new TweetNaclFast.Signature(skpk.getPublicKey(), skpk.getSecretKey());
        try {
            byte[] sigBytes = Ed25519.sign(messageBytes);
            rv = TweetNaclFast.hexEncodeToString(sigBytes).substring(0, 128);
            Log.d("asdf", rv);
            //Log.d("asdf", "sig length:" + Integer.toString(rv.length()));
        } catch (Exception ee) {
            Log.d("asdf","signing error");
        }
        return rv;
    }

    public void JsonRequestGetBalance() throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "balance" + timestamp;
        Log.d("asdf", "message to sign is:"+message);
        String signature = GenerateSignature(message);
        Log.d("asdf", "signature is:"+signature);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("Type", "balance");
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "balance is:"+balanceString);
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "error in get balance:" + errorResponse);
                if (errorResponse.contains("balance")) {
                    //actually not an error...
                     Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, errorResponse, duration);
                    toast.show();
                }
            }

        });

    }

        public void JsonRequestGetAddress() throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "address" + timestamp;
        Log.d("asdf", "message to sign is:"+message);
        String signature = GenerateSignature(message);
        Log.d("asdf", "signature is:"+signature);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("Type", "address");
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:"+balanceString);
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                if (errorResponse.contains("address")) {
                    //actually not an error...
                     Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, errorResponse, duration);
                    toast.show();
                }
            }

        });

    }

    //needs work..
    public void JsonRequestSend() throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String amount = "0d1";
        String destination = "44AFFq5kSiGBoZ4NMDwYtN18obc8AemS33DBLWs3H7otXft3XjrpDtQGv7SqSsaBYBb98uNbr2VBBEt7f2wfn3RVGQBEP3A";
        //obviously change this later

        String timestamp = Long.toString(now() + offsetCB);
        String message = "send" + amount + timestamp + destination;
        Log.d("asdf", "message to sign is:"+message);
        String signature = GenerateSignature(message);
        Log.d("asdf", "signature is:"+signature);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("amount", amount);
        params.put("Type", "send");
        params.put("destination", destination);
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:"+balanceString);
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                if (errorResponse.contains("xmr")) {
                    //actually not an error...
                     Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, errorResponse, duration);
                    toast.show();
                }
            }

        });

    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    //now this is just a demo
    public void onBalanceClick(View view) {

    }

    public void insertAddress(SavedAddress sa) {
        ContentValues data = new ContentValues();
        data.put("Name", sa.Name);
        data.put("address", sa.address);
        sql.insert( "SavedAddress",null, data);
    }

    public void saveButtonClick(View view) {
        //
        final SavedAddress sa = new SavedAddress();
        EditText desttext = (EditText) findViewById(R.id.destination_text);
        sa.address = desttext.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Alias?");
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do something with value!
                sa.Name = input.getText().toString();
                insertAddress(sa);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                Log.d("asdf", "canceled");
            }
        });

        alert.show();
    }

    public void sendButtonClick(View view) {
        //needs to parse whether it's an xmr address or bitcoin first

        //this is currently non-functional, but close enough it can be easily made so..
        try {
            JsonRequestSend();
        } catch (Exception sendEx) {
            Log.d("asdf", "problem in send");
        }
    }
}