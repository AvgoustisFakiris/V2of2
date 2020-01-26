package gr.hua.www.v2of2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.util.Patterns;

public class SingupActivity extends BaseActivity {

    private final String TAG = getClass().getName(); // logging purposes
    private String fn, ln, un, em, pw;
    private TextInputLayout textInputEmail, textInputUsername, textInputPassword, textInputFirstname, textInputLastname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        // Send input to API
        final Button submitButton = (Button) findViewById(R.id.singup);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                textInputEmail = findViewById(R.id.label_email);
                textInputUsername = findViewById(R.id.label_username);
                textInputPassword = findViewById(R.id.label_password);
                textInputFirstname = findViewById(R.id.label_firstname);
                textInputLastname = findViewById(R.id.label_lastname);

                EditText editText = (EditText) findViewById(R.id.firstname);
                fn = editText.getText().toString();
                EditText editText2 = (EditText) findViewById(R.id.lastname);
                ln = editText2.getText().toString();
                EditText editText3 = (EditText) findViewById(R.id.username);
                un = editText3.getText().toString();
                EditText editText4 = (EditText) findViewById(R.id.email);
                em = editText4.getText().toString();
                EditText editText5 = (EditText) findViewById(R.id.password);
                pw = editText5.getText().toString();

                // Check if input is valid
                if (validateEmail() && validateUsername() && validatePassword() && validateFirstname() && validateLastname()) {
                    //Post User json
                    createUser();
                }
            }
        });

        //Go to Login Page
        final Button loginButt = (Button) findViewById(R.id.login);
        loginButt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(SingupActivity.this, LoginActivity.class);
                startActivity(i);

            }
        });
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputEmail.setError("Please enter a valid email address");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputUsername.setError("Field can't be empty");
            return false;
        } else if (usernameInput.length() > 255) {
            textInputUsername.setError("Username too long");
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }
    }

    private boolean validateFirstname() {
        String usernameInput = textInputFirstname.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputFirstname.setError("Field can't be empty");
            return false;
        } else if (usernameInput.length() > 255) {
            textInputFirstname.setError("Username too long");
            return false;
        } else {
            textInputFirstname.setError(null);
            return true;
        }
    }

    private boolean validateLastname() {
        String usernameInput = textInputLastname.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputLastname.setError("Field can't be empty");
            return false;
        } else if (usernameInput.length() > 255) {
            textInputLastname.setError("Username too long");
            return false;
        } else {
            textInputLastname.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Field can't be empty");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }


    private void createUser() {


        //POST USER (create user) to database
        // the request
        OkHttpClient client = new OkHttpClient();
        //url to get json data
        uri = "http://test.hua.gr/v2of/users/";
        String jPost = "{" +
                "\"firstname\":\"" + fn + "\"" +
                ",\"lastname\":\"" + ln + "\"" +
                ",\"username\":\"" + un + "\"" +
                ",\"email\":\"" + em + "\"" + ",\"password\":\""
                + pw + "\"" + "}";
        RequestBody body = RequestBody.create(JSON, jPost);
        //Obligatory for post
        String credential = Credentials.basic("test", "1234");
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .addHeader("Authorization", credential)
                .addHeader("cache-control", "no-cache")
                .build();

        //   progress = ProgressDialog.show(SingupActivity.this, "Please wait ...", "Create User...", true);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                String myResponse = response.body().string();
                Log.d(TAG, myResponse);

                Log.d(TAG, "test121" + response.code());

                runOnUiThread(new Runnable() {
                    public void run() {
                        //Check if Posted and inform user
                        if (response.code() != 500) {
                            Toast.makeText(SingupActivity.this,
                                    "SignUp Successful", Toast.LENGTH_LONG).show();
                            //Go to Main Page
                            Intent i = new Intent(SingupActivity.this, BaseActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(SingupActivity.this,
                                    "SignUp Failed. Try Again!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }

}
