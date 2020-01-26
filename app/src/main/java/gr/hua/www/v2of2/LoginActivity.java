package gr.hua.www.v2of2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


import gr.hua.www.v2of2.utils.RestTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by father on 4/8/17.
 */

public class LoginActivity extends BaseActivity {

    private final String TAG = getClass().getName(); // logging purposes

    //Google SignIn button**
    private SignInButton mGoogleBtn;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Refresh Token Timer
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();


    /**
     * Any code to access activity fields must be handled in this method.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Google-SignIn purposes ***
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
        });

        // Initiate the request to the protected service
        final Button submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.username);
                user = "test";
//                editText = (EditText) getActivity().findViewById(R.id.password);
//                pass = editText.getText().toString();
                pass = "1234";
                //    uri = URL + "/login/?username=" + user + "&password=" + pass;
                //    Log.i(TAG, uri);
                getToken();

            }
        });
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

        //Go to Sing Up Page
        final Button singupButton = (Button) findViewById(R.id.singup);
        singupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SingupActivity.class);
                startActivity(i);

            }
        });
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

    }

    //Google-SignIn  ***
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned form launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was succesful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(LoginActivity.this, "Successful Login", Toast.LENGTH_SHORT).show();
            } else {
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
                            //    FirebaseUser user = mAuth.getCurrentUser();
                            //   updateUI(user);
                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                            mUser.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if (task.isSuccessful()) {
                                                String idToken = task.getResult().getToken();
                                                Log.d(TAG, "Token:" + idToken);
                                                // Send token to your backend via HTTPS
                                                // ...
                                            } else {
                                                // Handle error -> task.getException();
                                            }
                                        }
                                    });

                            //Check if is New User
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser) {
                                //Sing-Up
                                // Google Profile Info
                                user = mUser.getEmail();
                                String userName = mUser.getDisplayName();
                                String lastName = "google_user";
                                pass = mUser.getUid();
                                //POST USER (create user) to database
                                // the request
                                OkHttpClient client = new OkHttpClient();
                                //url to get json data
                                uri = "http://test.hua.gr/v2of/users/";
                                String jPost = "{" +
                                        "\"firstname\":\"" + userName + "\"" +
                                        ",\"lastname\":\"" + lastName + "\"" +
                                        ",\"username\":\"" + user + "\"" +
                                        ",\"email\":\"" + user + "\"" + ",\"password\":\""
                                        + pass + "\"" + "}";
                                RequestBody body = RequestBody.create(JSON, jPost);
                                //Obligatory for post
                                String credential = Credentials.basic("test", "1234");
                                Request request = new Request.Builder()
                                        .url(uri)
                                        .post(body)
                                        .addHeader("Authorization", credential)
                                        .addHeader("cache-control", "no-cache")
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        call.cancel();
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                        String myResponse = response.body().string();
                                        Log.d(TAG, myResponse);
                                    }
                                });

                                //Then getToken
                                getToken();

                            } else {
                                //GetToken
                                user = mUser.getEmail();
                                pass = mUser.getUid();
                                getToken();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                            //   updateUI(null);
                        }
                    }
                });
    }


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
            //   TOKEN = intent.getStringExtra(RestTask.TOKEN);
            Log.d(TAG, "RESPONSE = " + TOKEN);
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void getToken() {

        // the request
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = "http://test.hua.gr/v2of/myapp/api-token-auth/";
        String jsonT = "{" + "\"username\":\"" + user + "\"" + ",\"password\":\"" + pass + "\"" + "}";
        RequestBody body = RequestBody.create(JSON, jsonT);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .addHeader("cache-control", "no-cache")
                .build();

        progress = ProgressDialog.show(this, "Please wait ...", "Login...", true);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String myResponse = response.body().string();
                Log.d(TAG, myResponse);
                try {
                    JSONObject myObject = new JSONObject(myResponse);
                    TOKEN = myObject.getString("token");
                    Log.d("Token=", TOKEN);
                    //Operators for Charts
                    vendorsOperator();
                    networksOperator();
                    osOperator();
                    providersOperator();
                    statisticsOperator();
                    //Get Refresh Token Too
                    startTimer();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TOKEN != null) {
                            Toast.makeText(LoginActivity.this,
                                    "Login Successful", Toast.LENGTH_LONG).show();
                            //Go to Main Page
                            Intent i = new Intent(LoginActivity.this, BaseActivity.class);
                            //    startActivity(i);


                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Oops! Try Again", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
    }


    public void listAllCampaigns() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();

        uri = "http://test.hua.gr/v2of/api/myapp/v2ofcampaigns";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("Authorization", "JWT " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        //      progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
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
                Log.i("Campaigns", myResponse);
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
        uri = "http://test.hua.gr/v2of/myapp/vendorStats";
        String credential = Credentials.basic(user, pass);
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("Authorization", "JWT " + TOKEN)
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
                    JSONObject object = new JSONObject(myResponse);
                    JSONArray Jarray = object.getJSONArray("results");

                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject Jsnobject = Jarray.getJSONObject(i);
                        String key = Jsnobject.getString("key");
                        int value = Jsnobject.getInt("value");
                        vendors.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //Check if token expired
                if (response.code() == 401) {
                    //Refresh Token
                    TOKEN = REFRESHTOKEN;
                    //   refreshToken();
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
        uri = "http://test.hua.gr/v2of/myapp/networks";
        String credential = Credentials.basic(user, pass);
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("Authorization", "JWT " + TOKEN)
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
                    JSONObject object = new JSONObject(myResponse);
                    JSONArray Jarray = object.getJSONArray("results");

                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject Jsnobject = Jarray.getJSONObject(i);
                        String key = Jsnobject.getString("key");
                        int value = Jsnobject.getInt("value");
                        networks.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //Check if token expired
                if (response.code() == 401) {
                    //Refresh Token
                    TOKEN = REFRESHTOKEN;
                    //   refreshToken();
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
        uri = "http://test.hua.gr/v2of/myapp/osStats";
        String credential = Credentials.basic(user, pass);
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("Authorization", "JWT " + TOKEN)
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
                    JSONObject object = new JSONObject(myResponse);
                    JSONArray Jarray = object.getJSONArray("results");

                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject Jsnobject = Jarray.getJSONObject(i);
                        String key = Jsnobject.getString("key");
                        int value = Jsnobject.getInt("value");
                        opersyst.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //Check if token expired
                if (response.code() == 401) {
                    //Refresh Token
                    TOKEN = REFRESHTOKEN;
                    //  refreshToken();
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
        uri = "http://test.hua.gr/v2of/myapp/providers";
        String credential = Credentials.basic(user, pass);

        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("Authorization", "JWT " + TOKEN)
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
                    JSONObject object = new JSONObject(myResponse);
                    JSONArray Jarray = object.getJSONArray("results");

                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject Jsnobject = Jarray.getJSONObject(i);
                        String key = Jsnobject.getString("key");
                        int value = Jsnobject.getInt("value");
                        providers.add(new PieEntry(value, key));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //Check if token expired
                if (response.code() == 401) {
                    //Refresh Token
                    TOKEN = REFRESHTOKEN;
                    //  refreshToken();
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
        uri = "http://test.hua.gr/v2of/myapp/" + measu + netw;
        Log.i("uri", uri);
        String credential = Credentials.basic(user, pass);
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("Authorization", "JWT " + TOKEN)
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
                try {
                    JSONObject object = new JSONObject(myResponse);
                    JSONArray Jarray = object.getJSONArray("results");

                    minstat.clear();
                    maxstat.clear();
                    avgstat.clear();
                    prov.clear();

                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject Jsnobject = Jarray.getJSONObject(i);
                        String prvdrs = Jsnobject.getString("key");
                        if (!Jsnobject.isNull("min")) {

                            int min = Jsnobject.getInt("min");
                            int max = Jsnobject.getInt("max");
                            int avg = Jsnobject.getInt("avg");

                            minstat.add(new BarEntry(i, min));
                            maxstat.add(new BarEntry(i, max));
                            avgstat.add(new BarEntry(i, avg));

                        } else {

                        }
                        prov.add(prvdrs);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //Check if token expired
                if (response.code() == 401) {
                    //Refresh Token
                    TOKEN = REFRESHTOKEN;
                    // refreshToken();
                }

            }

        });
    }


    public void refreshToken() {
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = "http://test.hua.gr/v2of/myapp/api-token-refresh/";

        String json = "{" +
                "\"token\":\"" + TOKEN + "\"" + "}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
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
                try {
                    JSONObject myObject = new JSONObject(myResponse);
                    REFRESHTOKEN = myObject.getString("token");
                    Log.i("RefreshToken=", REFRESHTOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });


    }

    //Stop Refresh Token
    protected void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    //Start timer to refresh Token
    protected void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        refreshToken();
                    }
                });
            }
        };
        //Every 5 minutes
        timer.schedule(timerTask, 5000, 200000);
    }


}