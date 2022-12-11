package com.ttv.facerecog;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.apache.commons.io.FilenameUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomepageActivity extends AppCompatActivity {

    GoogleSignInClient gsc;
    ProgressDialog p;
    FloatingActionButton fab;
    Intent fileIntent ;
    TextView searchBar;
    ImageButton searchButton;

    private int Read_Permission = 1;
    private int getFile = 21;

    String email;



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

        // need face recognition or pattern before this if this is the first time
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
        if (fileName != null){
            if (fileName.isEmpty()) {
                fileName = "WELCOME TO DRIVE";
            }
        }
        System.out.println(fileName + "LAALLALALALALALLALALAL");
        setToolbar(fileName);
        /*
        search

         */
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
                    email = account.getEmail();
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

                                if (mList.get(position).getMimeType().contains("google-apps.folder")) { // if folder then shows that folder's content
                                    Intent intent = new Intent(HomepageActivity.this, HomepageActivity.class);
                                    intent.putExtra("Q", "'" + mList.get(position).getId()+ "'" +" in parents and trashed=false");
                                    intent.putExtra("folderName", mList.get(position).getName());
                                    intent.putExtra("folder_id", mList.get(position).getId());
                                    startActivity(intent);
                                    return;
                                }

                                // if it is a file that show the menu
                                PopupMenu popupMenu = new PopupMenu(HomepageActivity.this, view);
                                popupMenu.getMenuInflater().inflate(R.layout.context_menu, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        if (menuItem.getTitle().equals("View")) {
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



        // floating action button
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("mgss", "error?");
//                Toast.makeText(HomepageActivity.this, "hello", Toast.LENGTH_SHORT).show();
                getPermission();

            }
        });

        searchBar = findViewById(R.id.search);

        searchButton = findViewById(R.id.search_img_btn);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBar.getText().toString().isEmpty()) {
                    Toast.makeText(HomepageActivity.this, "Search Bar is Empty!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(HomepageActivity.this, HomepageActivity.class);
                intent.putExtra("Q", "name contains '" + searchBar.getText().toString() + "' and trashed=false");
                intent.putExtra("folderName", "Search Results");
                startActivity(intent);

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
        // for normal files or can't be viewed
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
                java.io.File dir = new java.io.File(HomepageActivity.this.getCacheDir(), "resources");
                if(!dir.exists()){
                    dir.mkdir();
                }
                dir = new java.io.File(HomepageActivity.this.getCacheDir(), "encrypted");
                if(!dir.exists()){
                    dir.mkdir();
                }
                System.out.println("DIR: " + dir.getAbsolutePath());
                OutputStream oOutputStream = new FileOutputStream(HomepageActivity.this.getCacheDir() + "/resources" + "/" + d.getName());
                String fileName = HomepageActivity.this.getCacheDir() + "/resources" + "/" + d.getName();
                java.io.File file = new java.io.File(fileName);
                if (d.getMimeType().contains("google-apps")) {
                    service.files().export(d.getId(), "application/pdf").executeMediaAndDownloadTo(oOutputStream);
//                    Intent intent = new Intent(HomepageActivity.this, PdfViewerActivity.class);
//                    intent.putExtra("fileName", fileName);
//                    startActivity(intent);
                    Intent intent = new Intent(HomepageActivity.this, CameraActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("type", "pdf");
                    intent.putExtra("email", email);
                    startActivity(intent);

                }
                else {
                    if (d.getAppProperties() != null) {
                        OutputStream encryptedOutputStream = new FileOutputStream(HomepageActivity.this.getCacheDir() + "/encrypted" + "/" + d.getName());
                        fileName = HomepageActivity.this.getCacheDir() + "/encrypted" + "/" + d.getName();
                        java.io.File encryptedFile = new java.io.File(fileName);
                        java.io.File decryptedFile = new java.io.File(HomepageActivity.this.getCacheDir() + "/resources" + "/" + FilenameUtils.removeExtension(d.getName()) + d.getAppProperties().get("extension"));
                        service.files().get(d.getId()).executeMediaAndDownloadTo(encryptedOutputStream);
                        CryptoUtils.decrypt(d.getAppProperties().get("key"), encryptedFile, decryptedFile);
                        System.out.println(decryptedFile.getAbsolutePath());
                        if (d.getAppProperties().get("extension").equals("pdf")){
                            Intent intent = new Intent(HomepageActivity.this, CameraActivity.class);
                            intent.putExtra("fileName", HomepageActivity.this.getCacheDir() + "/resources" + "/" + FilenameUtils.removeExtension(d.getName()) + d.getAppProperties().get("extension"));
                            intent.putExtra("type", "pdf");
                            intent.putExtra("email", email);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(HomepageActivity.this, CameraActivity.class);
                            intent.putExtra("fileName", HomepageActivity.this.getCacheDir() + "/resources" + "/" + FilenameUtils.removeExtension(d.getName()) + d.getAppProperties().get("extension"));
                            intent.putExtra("type", "img");
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                    } else if (d.getFileExtension().equals("pdf")) {
                        service.files().get(d.getId()).executeMediaAndDownloadTo(oOutputStream);
//                        Intent intent = new Intent(HomepageActivity.this, PdfViewerActivity.class);
//                        intent.putExtra("fileName", fileName);
//                        startActivity(intent);
                        Intent intent = new Intent(HomepageActivity.this, CameraActivity.class);
                        intent.putExtra("fileName", fileName);
                        intent.putExtra("type", "pdf");
                        intent.putExtra("email", email);
                        startActivity(intent);


                    } else {
                        service.files().get(d.getId()).executeMediaAndDownloadTo(oOutputStream);
//                        Intent intent = new Intent(HomepageActivity.this, ImageViewerActivity.class);
//                        intent.putExtra("fileName", fileName);
//                        startActivity(intent);
                        Intent intent = new Intent(HomepageActivity.this, CameraActivity.class);
                        intent.putExtra("fileName", fileName);
                        intent.putExtra("type", "img");
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (CryptoException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(String temp) {
            Toast.makeText(HomepageActivity.this, "Viewing File",
                    Toast.LENGTH_LONG).show();
            p.dismiss();

        }


    }

    void selectNewFile(){
        // after getting permissions
        // opens files, folders, pdf, txt files in phone
        // get the location or folder address like where the file is getting uploaded
        fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent,getFile);
    }

    void getPermission() {
        if(ContextCompat.checkSelfPermission(HomepageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            // already granted
            selectNewFile();
        }
       else{
           requestPermission();
        }
    }

    // asks for permission
    private void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(HomepageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            // to explain why we need this permission
            new AlertDialog.Builder(HomepageActivity.this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission needed to add new file.")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(HomepageActivity.this, new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            }, Read_Permission);
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
        else{ // directly asks for permission
            ActivityCompat.requestPermissions(HomepageActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, Read_Permission);
        }
    }

    // after request granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==Read_Permission){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(HomepageActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                selectNewFile();
            }
            else{
                Toast.makeText(HomepageActivity.this, "Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==getFile){
            if(resultCode==RESULT_OK){
                String path = data.getData().getPath();

                Log.d("pl",path); // shows that the file is getting selected
                addFile(data);
            }
        }
    }



    void addFile(@Nullable Intent data) {
        System.out.println(data.getData());
        Uri fileUri = data.getData();
        String mimeType = getContentResolver().getType(fileUri);
        System.out.println("AAAAAAAAAAAA" + mimeType);
//        java.io.File originF = new java.io.File(FileUtils.getRealPath(this,fileUri));
        System.out.println("BBBBBBBB" + FileUtils.getRealPath(this,fileUri));
        System.out.println("BBBBBBBB" + data.getData().getPath());
        java.io.File originF = new java.io.File(FileUtils.copyFileToInternal(this, fileUri));
        String key = "Mary has one cat";
        java.io.File path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            path= (this.getExternalFilesDir(Environment.DIRECTORY_DCIM));
        }
        else
        {
            path= new java.io.File(Environment.getExternalStorageDirectory().toString());
        }
//        java.io.File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        java.io.File encryptedFile = new java.io.File(path, "/" + FilenameUtils.removeExtension(originF.getName()) + ".bin");
        try {
            CryptoUtils.encrypt(key, originF, encryptedFile);
            String filePath = encryptedFile.getPath();
            File fileMetadata = new File();
            fileMetadata.setName(encryptedFile.getName());
            Map<String, String> appProperties = new HashMap<String, String>();
            appProperties.put("appName", "MummySeChupana");
            appProperties.put("key", "Mary has one cat");
            appProperties.put("extension", FilenameUtils.getExtension(originF.getName()));
            fileMetadata.setAppProperties(appProperties);
            Intent intent = getIntent();
            if (intent.hasExtra("folder_id")) {
                if (!intent.getStringExtra("folder_id").isEmpty()) {
                    fileMetadata.setParents(Collections.singletonList(intent.getStringExtra("folder_id")));
                }
            }
            java.io.File fileContent = new java.io.File(filePath);
            FileContent mediaContent =  new FileContent("application/octet-stream", fileContent);
            service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            finish();
            startActivity(getIntent());

            //com.google.api.client.googleapis.json.GoogleJsonResponseException
        } catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            Log.d("Unable to upload file: ", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Unable file: ", e.getMessage());
        }

//        String filePath = originF.getPath();
//
//        File fileMetadata = new File();
//        fileMetadata.setName(originF.getName());
//        Intent intent = getIntent();
//        if (!intent.getStringExtra("folder_id").isEmpty()){
//            fileMetadata.setParents(Collections.singletonList(intent.getStringExtra("folder_id")));
//        }
//        // File's content.
//        java.io.File fileContent = new java.io.File(filePath);
//        FileContent mediaContent = null;
//        if (mimeType.contains("image")) {
//            // Specify media type and file-path for file.
//            mediaContent = new FileContent("image/jpeg", fileContent);
//        } else if (FilenameUtils.getExtension(filePath).equals("doc")
//                    || FilenameUtils.getExtension(filePath).equals("docx")) {
//            fileMetadata.setMimeType("application/vnd.google-apps.document");
//            mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.wordprocessingml.document", fileContent);
//        } else if (FilenameUtils.getExtension(filePath).equals("txt")) {
//            mediaContent = new FileContent("text/plain", fileContent);
//        } else if (FilenameUtils.getExtension(filePath).equals("ppt")
//                || FilenameUtils.getExtension(filePath).equals("pptx")) {
//            fileMetadata.setMimeType("application/vnd.google-apps.presentation");
//            mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.presentationml.presentation", fileContent);
//        } else if (FilenameUtils.getExtension(filePath).equals("pdf")) {
//            mediaContent = new FileContent("application/pdf", fileContent);
//        } else if (FilenameUtils.getExtension(filePath).equals("xls")
//                || FilenameUtils.getExtension(filePath).equals("xlsx")) {
//            fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
//            mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileContent);
//        } else {
//            mediaContent = new FileContent("application/vnd.google-apps.file", fileContent);
//        }
//        try {
//            File file = service.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute();
//            finish();
//            startActivity(getIntent());
//
//            //com.google.api.client.googleapis.json.GoogleJsonResponseException
//        } catch (GoogleJsonResponseException e) {
//            e.printStackTrace();
//            Log.d("Unable to upload file: ", e.getMessage());
//
//        } catch (IOException e) {
//
//            e.printStackTrace();
//            Log.d("Unable file: ", e.getMessage());
//        }
    }


}