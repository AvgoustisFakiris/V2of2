package gr.hua.www.v2of2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import gr.hua.www.v2of2.utils.RestTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by father on 4/8/17.
 */

public class LoginActivity extends BaseActivity {

    private final String TAG = getClass().getName(); // logging purposes

    /* **Google SignIn button**
    private SignInButton mGoogleBtn;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener; */

    /**
     * Any code to access activity fields must be handled in this method.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

/* *** Google-SignIn purposes ***
        mGoogleBtn = (SignInButton) findViewById(R.id.googleBtn);
        mAuth = FirebaseAuth.getInstance();

        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();



        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Toast.makeText(LoginActivity.this, "You Got an Error", Toast.LENGTH_LONG).show();
                }
            })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        }); */

        // Initiate the request to the protected service
        final Button submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                EditText editText = (EditText) getActivity().findViewById(R.id.username);
//                user = editText.getText().toString();
                user = "test";
//                editText = (EditText) getActivity().findViewById(R.id.password);
//                pass = editText.getText().toString();
                pass = "1234";
                uri = URL + "/login/?username=" + user + "&password=" + pass;
                Log.i(TAG, uri);
                getToken();
            }
        });
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    /* *** Google-SignIn purposes ***
    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        //Result returned form launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was succesful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }else{
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                //...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                         //   FirebaseUser user = mAuth.getCurrentUser();
                         //   updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                            //   updateUI(null);
                        }

                        // ...
                    }
                });
    } */


    @Override
    public void onResume() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter(ACTION_FOR_INTENT_CALLBACK));
    }

    @Override
    public void onPause() {
        this.unregisterReceiver(receiver);
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        super.onPause();
    }

    /**
     * Our Broadcast Receiver. We get notified that the data is ready this way.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, new Object() {
            }.getClass().getEnclosingMethod().getName());
            // clear the progress indicator
            if (progress != null) {
                progress.dismiss();
            }
            //String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
            TOKEN = intent.getStringExtra(RestTask.TOKEN);
            Log.d(TAG, "RESPONSE = " + TOKEN);
            try {
                //Call classes for taking database data
                listAllCampaigns();
                vendorsOperator();
                networksOperator();
                osOperator();
                providersOperator();
                statisticsOperator();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void listAllCampaigns() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();

        uri = URL + "/api/campaign/";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dbg) {
                            //Parse the Response to see the result in a Splash Screen
                            Intent i = new Intent(LoginActivity.this, DebugActivity.class);
                            i.putExtra("resp", myResponse);
                            startActivity(i);
                        }
                    }
                });
            }
        });
    }

    //Getting data for VendorsActivity class
    public void vendorsOperator() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = URL + "/api/measurement/vendors/";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        //   progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            //Get Data and fill arrays
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);

                try {
                    JSONArray jsonArray = new JSONArray(myResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String key = object.getString("key");
                        int value = object.getInt("value");
                        vendors.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        });
    }

    //Getting data for NetworksActivity class
    public void networksOperator() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();
        // url to get json data
        uri = URL + "/api/measurement/networkTypes/";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        //   progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);
                //Get Data and fill arrays
                try {
                    JSONArray jsonArray = new JSONArray(myResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String key = object.getString("key");
                        int value = object.getInt("value");
                        networks.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        });
    }

    //Getting data for NetworksActivity class
    public void osOperator() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = URL + "/api/measurement/operatingSystems/";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        //   progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);
                //Get Data and fill arrays
                try {
                    JSONArray jsonArray = new JSONArray(myResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String key = object.getString("key");
                        int value = object.getInt("value");
                        opersyst.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        });
    }

    //Getting data for NetworksActivity class
    public void providersOperator() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = URL + "/api/measurement/allproviders";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        //   progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);
                //Get Data and fill arrays
                try {
                    JSONArray jsonArray = new JSONArray(myResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String key = object.getString("operatorname");
                        int value = object.getInt("value");
                        providers.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        });
    }

    //Getting data for NetworksActivity class
    public void statisticsOperator() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = URL + "/api/measurement/levelStats/";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        //   progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);
                //Get Data and fill arrays
                try {
                    JSONArray jsonArray = new JSONArray(myResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String operator = object.getString("operatorname");
                        int max = object.getInt("max");
                        int min = object.getInt("min");
                        long avg = object.getLong("avg");
                        minstat.add(new BarEntry(i, min));
                        maxstat.add(new BarEntry(i, max));
                        avgstat.add(new BarEntry(i, avg));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        });
    }


}