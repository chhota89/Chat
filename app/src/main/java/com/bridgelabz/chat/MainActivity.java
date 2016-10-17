package com.bridgelabz.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.bridgelabz.chat.adapter.ChatAdapter;
import com.bridgelabz.chat.model.Message;
import com.bridgelabz.chat.service.ChatService;
import com.bridgelabz.chat.service.ImageService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_CODE = 211;
    ChatService chatService;
    private ImageService imageService;
    ArrayList<Message> messageArrayList=new ArrayList<>();
    private ChatAdapter mAdapter;
    private String actualImagepath;
    String selfUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatService=new ChatService();
        imageService=new ImageService();

        selfUserId=Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        //firebaseReference();

        Button btn_send=(Button)findViewById(R.id.btn_send);
        final EditText editText=(EditText)findViewById(R.id.message);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.getText().toString().trim().equals("")) {
                    Message message = new Message(editText.getText().toString(), selfUserId);
                    chatService.sendMessageToGroup("Abc", message);
                    editText.setText("");

                    //hide keyboard
                    View view1 = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                    }
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        mAdapter = new ChatAdapter(MainActivity.this, messageArrayList, selfUserId,"Abc");
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        chatService.getGroupMessageList("Abc", new ChatService.MessageListInterface() {
            @Override
            public void messageAdded(Message message) {
                if(actualImagepath!=null){
                    message.setSync(true);
                    message.setActualImageUrl(actualImagepath);
                    actualImagepath=null;
                }
                messageArrayList.add(message);
                mAdapter.notifyItemInserted(messageArrayList.size()-1);
                layoutManager.scrollToPosition(messageArrayList.size()-1);
            }

            public void messageDeleted(Message message){
                int i=messageArrayList.indexOf(message);
                messageArrayList.remove(message);
                mAdapter.notifyItemRemoved(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.send_image) {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, GALLERY_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_CODE) {
                Uri selectedPic = data.getData();
                actualImagepath=selectedPic.toString();
                Message message=new Message();
                message.setImageUrl(selectedPic.getLastPathSegment());
                message.setUserId(selfUserId);
                chatService.sendMessageToGroup("Abc",message);
            }
        }
    }

}
