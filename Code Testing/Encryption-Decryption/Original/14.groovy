       storageTask = fileReference.putFile(uri)
               .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       Log.d("image","3");

//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                //mProgressBar.setProgress(0);
//                            }
//                        }, 500);

                       Log.d("image","4");
.
                       ToastmakeText(MainActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                       Upload upload = new Upload(email.getText().toString().trim(),
                               fileReference2.getDownloadUrl().toString());
                       String uploadId = databaseReference.push().getKey();
                       databaseReference.child(uploadId).setValue(upload);
                   }
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });