package com.bridgelabz.chat.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by bridgeit on 30/8/16.
 */

public class ImageService {

    private static final String TAG=ImageService.class.getSimpleName();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public void uploadImageIngroup(String groupName, Uri uri,ImageCallback callback){
        uploadImage(groupName,uri,callback);
    }

    private void uploadImage(String node, Uri uri, final ImageCallback callback){

        StorageReference myRef=storage.getReference();
        myRef=myRef.child(node+"/"+uri.getLastPathSegment());
        UploadTask uploadTask= myRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                callback.onFailure(exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                callback.onSuccess(downloadUrl);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.i(TAG, "onProgress: "+progress);
                callback.onProgress(progress);
            }
        });
    }

    public interface ImageCallback{
        void onSuccess(Uri downloadUri);
        void onFailure(Exception exception);
        void onProgress(double progress);
    }

    public void getImageBitmap(String folder, String path, final DownloadImageInterface callback){
        storage.getReference().child(folder).child(path).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                callback.onDownloadComplete(byteArrayToBitmap(bytes));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                callback.onDownloadFailure(exception);
            }
        });
    }

    private Bitmap byteArrayToBitmap(byte[] byteArray){
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public interface DownloadImageInterface{
        void onDownloadComplete(Bitmap bitmap);
        void onDownloadFailure(Exception exception);
    }
}
