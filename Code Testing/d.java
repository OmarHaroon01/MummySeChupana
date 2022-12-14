
// encryping

public class d {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }


void addFile(@Nullable Intent data) {

    System.out.println(data.getData());
    // file that is getting uploaded
    Uri fileUri = data.getData();
    // its file type
    String mimeType = getContentResolver().getType(fileUri);
    System.out.println("AAAAAAAAAAAA" + mimeType);
    System.out.println("BBBBBBBB" + FileUtils.getRealPath(this,fileUri));
    System.out.println("BBBBBBBB" + data.getData().getPath());
    // coping it to the internal memory of the phn 
    java.io.File originF = new java.io.File(FileUtils.copyFileToInternal(this, fileUri));

    // key for encryption

    String key = "Mary has one cat";
    java.io.File path;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
        path= (this.getExternalFilesDir(Environment.DIRECTORY_DCIM));
    }
    else
    {
        path= new java.io.File(Environment.getExternalStorageDirectory().toString());
    }
    //    java.io.File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
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
}







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

}

