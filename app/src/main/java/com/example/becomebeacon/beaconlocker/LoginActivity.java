package com.example.becomebeacon.beaconlocker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{



    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private static FirebaseAuth mAuth;
    private static FirebaseUser mAccount;



    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);


            mAuth = FirebaseAuth.getInstance();


            // Button listeners
            findViewById(R.id.login_google).setOnClickListener(this);


            // [START configure_signin]
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // [END configure_signin]

            // [START build_client]
            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            // [END build_client]
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10400", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.

            FirebaseUser currentUser = mAuth.getCurrentUser();

            mAccount = currentUser;

            updateUI(currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10401", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                if (result.isSuccess()) {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);

                } else {
                    // Google Sign In failed, update UI appropriately
                    // [START_EXCLUDE]
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show();
                    updateUI(null);
                    // [END_EXCLUDE]
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10402", Toast.LENGTH_LONG).show();
            finish();
        }

    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        try {
            FirebaseAuthWithGoogleProgress task = new FirebaseAuthWithGoogleProgress();
            task.execute(acct);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10403", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        try {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10404", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    // [END signin]

    private void signOut() {
        try {
            // Firebase sign out
            mAuth.signOut();

            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            updateUI(null);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10405", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void revokeAccess() {
        try {
            // Firebase sign out
            mAuth.signOut();

            // Google revoke access
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            updateUI(null);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10406", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateUI(FirebaseUser user) {
        try {
            if (user != null) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10407", Toast.LENGTH_LONG).show();
            finish();
        }
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        try {
            int i = v.getId();
            if (i == R.id.login_google) {
                LoginProgress task = new LoginProgress();
                task.execute();
    //            signIn();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10408", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    public static FirebaseAuth getAuth()
    {
        return mAuth;
    }

    public static FirebaseUser getUser()
    {
        return mAccount;
    }



    private class LoginProgress extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            signIn();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }

    private class FirebaseAuthWithGoogleProgress extends AsyncTask<GoogleSignInAccount, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(GoogleSignInAccount... acct) {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct[0].getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                mAccount=user;
                                asyncDialog.dismiss();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                asyncDialog.dismiss();
                                updateUI(null);
                            }

                            // [START_EXCLUDE]

                            // [END_EXCLUDE]
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}