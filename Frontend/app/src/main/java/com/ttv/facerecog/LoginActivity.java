package com.ttv.facerecog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ttv.face.FaceEngine;
import com.ttv.face.FaceFeatureInfo;
import com.ttv.face.FaceResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Url;

public class LoginActivity extends AppCompatActivity {

    private DBHelper mydb;
    public static ArrayList<FaceEntity> userLists = new ArrayList<>(0);


    public static final String baseURL = "http://192.168.181.127:8000";


    GoogleSignInClient gsc;
    SignInButton googleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FaceEngine.getInstance(this).setActivation("");
        FaceEngine.getInstance(this).init(2);
        System.out.println("KHELAAA HOI GESE");
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        userLists.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                baseURL + "/auth/get-all-users", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("AISE");
                try {
                    JSONArray allUsers = response.getJSONArray("data");
                    for (int i = 0; i < allUsers.length(); i++) {
                        JSONObject obj = allUsers.getJSONObject(i);
                        int user_id = obj.getInt("userUNID");
                        String email = obj.getString("email");
                        URL url = new URL(baseURL+"/images/" + obj.getString("photo"));
                        Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        List<FaceResult> faceResults = FaceEngine.getInstance(LoginActivity.this).detectFace(bitmap);
                        FaceEngine.getInstance(LoginActivity.this).extractFeature(bitmap, true, faceResults);
                        Rect cropRect =
                                Utils.getBestRect(bitmap.getWidth(), bitmap.getHeight(), faceResults.get(0).rect);
                        Bitmap headImg = Utils.crop(
                                bitmap,
                                cropRect.left,
                                cropRect.top,
                                cropRect.width(),
                                cropRect.height(),
                                120,
                                120
                        );
                        FaceEntity face = new FaceEntity(user_id, email, headImg, faceResults.get(0).feature);
                        LoginActivity.userLists.add(face);
                        System.out.println(userLists.toString());
                        FaceFeatureInfo faceFeatureInfo = new FaceFeatureInfo(
                                user_id,
                                faceResults.get(0).feature
                        );
                        FaceEngine.getInstance(LoginActivity.this).registerFaceFeature(faceFeatureInfo);
                    }

                    googleBtn = findViewById(R.id.google_btn);
                    String serverClientId = getString(R.string.server_client_id);
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(new Scope(Scopes.DRIVE_FULL))
                            .requestServerAuthCode(serverClientId)
                            .requestEmail()
                            .build();
                    gsc = GoogleSignIn.getClient(LoginActivity.this, gso);
                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
                    Log.d("TAG",acct + "ALALALLALA" + "ououou" + userLists.size());
                    if (acct != null) {
                        boolean acctExists = false;
                        System.out.println(acct.getEmail() );
                        for (FaceEntity f : userLists) {
                            Log.d("TAG",f.email + " " + acct.getEmail());
                            if (acct.getEmail().equals(f.email)){
                                acctExists = true;
                                navigateToHomePageActivity();
                                break;
                            }
                        }


                        if (!acctExists) {
                            gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
//                        finish();
//                        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                                }
                            });
                        }
                    }
                    googleBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signIn();
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "DHUKBENA", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(LoginActivity.this).addToRequestQueue(jsonObjectRequest);




    }
    void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            boolean gone = false;
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                for (FaceEntity face : userLists) {
                    System.out.println(face.email + "ASSSSSSSSSSSSSss" + account.getEmail());
                    if (face.email.equals(account.getEmail())) {
                        System.out.println("AISEEEEEEEEEEEEEEEEEE");
                        navigateToHomePageActivity();
                        gone = true;
                    }
                }
                if (!gone)
                    navigateToRegisterActivity(account.getEmail());
            } catch (ApiException e) {
                e.printStackTrace();
            }

        }
    }

    void navigateToRegisterActivity(String email) {
        finish();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    void navigateToHomePageActivity() {
        Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
        intent.putExtra("Q", "trashed=false");
        intent.putExtra("folderName", "");
        startActivity(intent);
        finish();
    }



}