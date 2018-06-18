package com.poop.rumi.rumi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
{
    SignInButton gButton;
    FirebaseAuth mAuth;
    private final static int RC_SIGN_IN = 2;
    FirebaseAuth.AuthStateListener mAuthListener;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        gButton = findViewById(R.id.sign_in_button);
//        setGooglePlusButtonText(gButton, "Sign in with Google");

        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_preferences_key), Context.MODE_PRIVATE);
        String currSessionToken = preferences.getString(getString(R.string.current_user_token), "");
        String currSessionUserJson = preferences.getString(getString(R.string.current_user_json_to_string), "");

        // If the user isn't logged in, take them to the login page
        if(!currSessionToken.isEmpty() && !currSessionUserJson.isEmpty()) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();

            return;
        }

        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                //.requestIdToken(getString(R.string.default_client_id))
//                .requestEmail()
//                .requestProfile()
//                .build();

//        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this,gso);
//
//        gButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//            }
//        });

    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }

        //Tried signing out here aswell.
//        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();

//        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
//                if (firebaseAuth.getCurrentUser() != null) {
//                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
//                }
//
//            }
//        };
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
//
//        if(account != null)
//        {
//            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
//            //user already signed in launch app!
//        }
    }

    //Trying a different way to signout.
    public void onStop()
    {
        super.onStop();
//        FirebaseAuth.getInstance().signOut();
    }


//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        try {
//            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//            // Signed in successfully, show authenticated UI.
//            //updateUI(account);
//            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
//
//        } catch (ApiException e) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w("FIREBASE", "signInResult:failed code" + e.getStatusCode());
//            //updateUI(null);
//        }
//    }



//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
//        }
//    }

}
