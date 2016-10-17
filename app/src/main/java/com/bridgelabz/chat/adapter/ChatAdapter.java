package com.bridgelabz.chat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bridgelabz.chat.R;
import com.bridgelabz.chat.model.Message;
import com.bridgelabz.chat.service.ImageService;
import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bridgeit on 29/8/16.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ParentViewHolder> {

    private static String TAG = ChatAdapter.class.getSimpleName();

    private String myUserId,mGroupName;
    private final int SELF_WITH_IMAGE=1,SELF_WITH_OUT_IMAGE=2,OTHER_WITH_IMAGE=3,OTHER_WITH_OUT_IMAGE=4;
    private static String today;
    private Context mContext;
    ImageService imageService;
    LruCache<String,Bitmap> cache;
    private ArrayList<Message> messageArrayList;

    public ChatAdapter(Context context, ArrayList<Message> messageArrayList, String myUserId,String groupName) {
        this.messageArrayList = messageArrayList;
        this.myUserId = myUserId;
        this.mContext = context;
        this.mGroupName=groupName;
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        imageService=new ImageService();

        //Find out maximum memory available to application
        //1024 is used because LruCache constructor takes int in kilobytes
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;
        Log.d(TAG, "max memory " + maxMemory + " cache size " + cacheSize);

        // LruCache takes key-value pair in constructor
        // key is the string to refer bitmap
        // value is the stored bitmap
        cache = new LruCache<String, Bitmap>(cacheSize);
    }

     class ParentViewHolder extends RecyclerView.ViewHolder{
         TextView timestamp;
        public ParentViewHolder(View itemView) {
            super(itemView);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        }
    }

     class ViewHolderWithImage extends ParentViewHolder {

         ImageView imageView;
         ProgressBar progressDialog;

        public ViewHolderWithImage(View view) {
            super(view);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            imageView.setDrawingCacheEnabled(false);
            progressDialog=(ProgressBar) itemView.findViewById(R.id.progress);
        }
    }

    class ViewHolderWithOutImage extends ParentViewHolder{
        TextView message;
        public ViewHolderWithOutImage(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.message);

        }
    }

    @Override
    public ParentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // view type is to identify where to render the chat message
        // left or right
        switch (viewType){
            case SELF_WITH_IMAGE:itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_chat_self_image, parent, false);
                return new ViewHolderWithImage(itemView);

            case SELF_WITH_OUT_IMAGE: itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_chat_self, parent, false);
                return new ViewHolderWithOutImage(itemView);

            case OTHER_WITH_IMAGE: itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_chat_other_image, parent, false);
                return new ViewHolderWithImage(itemView);

            case OTHER_WITH_OUT_IMAGE: itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_chat_other, parent, false);
                return new ViewHolderWithOutImage(itemView);

            default:itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_chat_self, parent, false);
                return new ViewHolderWithOutImage(itemView);
        }



    }


    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getUserId().equals(myUserId)) {
            if(message.getImageUrl()!= null)
                return SELF_WITH_IMAGE;
            else
                return SELF_WITH_OUT_IMAGE;
        }else{
            if(message.getImageUrl()!=null)
                return OTHER_WITH_IMAGE;
            else
                return OTHER_WITH_OUT_IMAGE;
        }
    }

    @Override
    public void onBindViewHolder(final ParentViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        String timestamp = getTimeStamp(message.getDate());
        if (message.getUserId().equals(myUserId)) {
            if(message.getImageUrl()!= null) {
                ViewHolderWithImage viewHolderWithImage= (ViewHolderWithImage) holder;
                if(message.isSync()) {
                    Uri uri=Uri.parse(message.getActualImageUrl());
                    Glide.with(mContext).load(uri).into(viewHolderWithImage.imageView);
                    message.setSync(false);
                    sendImageOnServer(viewHolderWithImage,uri);
                }else
                    setImage(mGroupName,message.getImageUrl(),viewHolderWithImage.imageView);
                //Glide.with(mContext).load(message.getImageUrl()).into(viewHolderWithImage.imageView);
            }
            else{
                ViewHolderWithOutImage viewHolderWithOutImage= (ViewHolderWithOutImage) holder;
                viewHolderWithOutImage.message.setText(message.getMsg());
            }
        }else{
            if(message.getImageUrl()!=null){
                ViewHolderWithImage viewHolderWithImage= (ViewHolderWithImage) holder;
               setImage(mGroupName,message.getImageUrl(),viewHolderWithImage.imageView);
            }
            else{
                ViewHolderWithOutImage viewHolderWithOutImage = (ViewHolderWithOutImage) holder;
                viewHolderWithOutImage.message.setText(message.getMsg());
            }
            timestamp = message.getUserId() + " , " + timestamp;
        }
        holder.timestamp.setText(timestamp);
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    private void sendImageOnServer(final ViewHolderWithImage viewHolderWithImage, Uri uri){
        viewHolderWithImage.progressDialog.setVisibility(View.VISIBLE);

        imageService.uploadImageIngroup("Abc", uri, new ImageService.ImageCallback() {
            @Override
            public void onSuccess(Uri downloadUri) {
                viewHolderWithImage.progressDialog.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception exception) {

            }

            @Override
            public void onProgress(double progress) {

            }
        });
    }

    private void setImage(String groupName, final String imagePath, final ImageView imageView){

        if(cache.get(imagePath) != null){
            imageView.setImageBitmap(cache.get(imagePath));
        }else{
            imageService.getImageBitmap(groupName, imagePath, new ImageService.DownloadImageInterface() {
                @Override
                public void onDownloadComplete(Bitmap bitmap) {
                    cache.put(imagePath,bitmap);
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onDownloadFailure(Exception exception) {

                }
            });
        }
    }
}
