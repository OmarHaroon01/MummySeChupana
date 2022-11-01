package com.example.mummysechupana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    GoogleSignInClient gsc;
    ProgressDialog p;


    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private RecyclerView mRecyclerView;
    private RecycleAdapter mAdapter;
    private List<File> mList = new ArrayList<>();
    private String accessToken = null;
    Drive service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        p = new ProgressDialog(this);
        p.setTitle("Loading Files and Folders");
        p.setMessage("Loading...");
        p.show();

        String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_FULL))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);
        Intent intent = getIntent();
        String q = intent.getStringExtra("Q");
        String fileName = intent.getStringExtra("folderName");
        if (fileName.isEmpty()) {
            fileName = "WELCOME TO DRIVE";
        }
        setToolbar(fileName);
        gsc.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    String authCode = account.getServerAuthCode();
                    GoogleTokenResponse tokenResponse =
                            new GoogleAuthorizationCodeTokenRequest(
                                    new NetHttpTransport(),
                                    JSON_FACTORY,
                                    "https://oauth2.googleapis.com/token",
                                    "226066860239-ofbto5dillvaiogdjp8m34nv29tmig04.apps.googleusercontent.com",
                                    "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",
                                    authCode, "")
                                    .execute();
                    accessToken = tokenResponse.getAccessToken();
                    GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
                    final NetHttpTransport HTTP_TRANSPORT;
                    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                            .setApplicationName("APPLICATION_NAME")
                            .build();

                    FileList result = service.files().list()
                            .setQ(q)
                            .setPageSize(1000)
                            .setFields("nextPageToken, files(*)")
                            .execute();
                    List<File> files = result.getFiles();
                    if (files == null || files.isEmpty()) {
                        System.out.println("No files found.");
                        p.dismiss();
                    } else {
                        for (File file : files) {
                            mList.add(file);
                        }
                        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
                        mRecyclerView.setLayoutManager(new GridLayoutManager(HomepageActivity.this, 3));
                        mAdapter = new RecycleAdapter(HomepageActivity.this, mList);
                        mRecyclerView.setAdapter(mAdapter);
                        p.dismiss();
                        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @SuppressLint("ResourceType")
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                if (mList.get(position).getMimeType().contains("google-apps.folder")) {
                                    Intent intent = new Intent(HomepageActivity.this, HomepageActivity.class);
                                    intent.putExtra("Q", "'" + mList.get(position).getId()+ "'" +" in parents and trashed=false");
                                    intent.putExtra("folderName", mList.get(position).getName());
                                    startActivity(intent);
                                    return;
                                }

                                PopupMenu popupMenu = new PopupMenu(HomepageActivity.this, view);
                                popupMenu.getMenuInflater().inflate(R.layout.context_menu, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        if (menuItem.getTitle().equals("Download")) {
                                            AsyncForDownload asyncTask=new AsyncForDownload();
                                            asyncTask.execute(new Integer(position));
                                        } else if (menuItem.getTitle().equals("Delete")) {
                                            try {
                                                File newContent = new File();
                                                newContent.setTrashed(true);
                                                service.files().update(mList.get(position).getId(), newContent).execute();
                                                Toast.makeText(HomepageActivity.this, "File moved to trash!",
                                                        Toast.LENGTH_LONG).show();
                                                finish();
                                                startActivity(getIntent());
                                            } catch (IOException e) {
                                                Toast.makeText(HomepageActivity.this, "You dont have permission to do this",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        return true;
                                    }
                                });
                                popupMenu.show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    void setToolbar (String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title_text = (TextView) toolbar.findViewById(R.id.title);
        title_text.setText(name);
        ImageView back_btn = (ImageView) toolbar.findViewById(R.id.back_button);
        if (name.equals("WELCOME TO DRIVE")) {
            back_btn.setVisibility(View.INVISIBLE);
        }
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayout signoutbtn = toolbar.findViewById(R.id.signout_btn);

        signoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(HomepageActivity.this, LoginActivity.class));
            }
        });
    }

    private class AsyncForDownload extends AsyncTask<Integer, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(HomepageActivity.this);
            p.setMessage("Please wait...It is downloading");
            p.setIndeterminate(true);
            p.setCancelable(true);
            p.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            p.show();
        }

        @Override
        protected String doInBackground(Integer... integers) {

            try {
                File d = mList.get(integers[0].intValue());
                HttpResponse f = null;
                OutputStream oOutputStream = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + d.getName());
                if (d.getMimeType().contains("google-apps.document"))
                    service.files().export(d.getId(), "application/vnd.openxmlformats-officedocument.wordprocessingml.document").executeMediaAndDownloadTo(oOutputStream);
                else if (d.getMimeType().contains("google-apps.spreadsheet"))
                    service.files().export(d.getId(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").executeMediaAndDownloadTo(oOutputStream);
                else if (d.getMimeType().contains("google-apps.presentation"))
                    service.files().export(d.getId(), "application/vnd.openxmlformats-officedocument.presentationml.presentation").executeMediaAndDownloadTo(oOutputStream);
                else if (d.getMimeType().contains("google-apps.drawing"))
                    service.files().export(d.getId(), "image/png").executeMediaAndDownloadTo(oOutputStream);
                else
                    service.files().get(d.getId()).executeMediaAndDownloadTo(oOutputStream);



                oOutputStream.flush();
                oOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(String temp) {
            Toast.makeText(HomepageActivity.this, "Downloaded",
                    Toast.LENGTH_LONG).show();
            p.dismiss();

        }


    }
}