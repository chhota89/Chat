package com.bridgelabz.chat.service;

import com.bridgelabz.chat.model.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by bridgeit on 29/8/16.
 */

public class ChatService {

    FirebaseDatabase database=FirebaseDatabase.getInstance();

    public ChatService(){

    }

    public void sendMessageToGroup(String groupName, Message message){
       sendMessage(groupName,message);
    }

    public void sendPersonalMessage(String fromUserId,String toMessageId,Message message){
        if(fromUserId.compareTo(toMessageId) < 0 ){
            // ToMessageId has less alphabetic order then fromUserId
            sendMessage(toMessageId+" "+fromUserId,message);
        }else{
            sendMessage(fromUserId+" "+toMessageId,message);
        }
    }

    private void sendMessage(String nodeName,Message message){
        DatabaseReference myRef;
        message.setDate(dateToString(new Date()));
        message.setTimeStump(System.currentTimeMillis());
        myRef=database.getReference(nodeName);
        myRef.push().setValue(message);
    }

    public void getGroupMessageList(String groupName,MessageListInterface messageListInterface){
        getMessageFor(groupName,messageListInterface);
    }

    private void getMessageFor(String nodeName, final MessageListInterface messageListInterface){
        DatabaseReference myRef;
        myRef=database.getReference(nodeName);
        Query query=myRef.orderByChild("timeStump").limitToLast(50);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message=genrateList(dataSnapshot);
                messageListInterface.messageAdded(message);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                messageListInterface.messageDeleted(genrateList(dataSnapshot));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            public Message genrateList(DataSnapshot dataSnapshot){
                Message message=dataSnapshot.getValue(Message.class);
                return message;
            }
        });
    }

    public interface MessageListInterface{
        void messageAdded(Message message);
        void messageDeleted(Message message);
    }

    public String dateToString(Date date){
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        return format.format(date);
    }
}
