/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MessageAdapter.media {
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity";
    private static final int RC_PHOTO_PICKER = 222;
    final String Massage = "massage";
    final String photo = "chat_photos";
    ImageButton mPhotoPickerButton;
    FirebaseDatabase mFireBase;
    List<AuthUI.IdpConfig> providers;
    ImageButton record;
    FirebaseStorage mFirebaseStorage;
    StorageReference mStorageReference;
    StorageReference mStorgeRef_Audio;
    boolean recording = false;
    String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private ChildEventListener childEventListener;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private Button mSendButton;
    private DatabaseReference mReference;
    private String mUsername;
    private FirebaseAuth mFirebaseAuth;
    private  FirebaseAuth.AuthStateListener mAuthStateListener;
    private MediaRecorder myRecorder;
    private String outputFile = null;
    private MediaPlayer media;
    private Handler mHandler = new Handler();
    private SeekBar seekBar;
    Runnable updateSeekBarTime = new Runnable() {
        public void run() {

            //get current position
            int timeElapsed = media
                    .getCurrentPosition();

            //set seekbar progress using time played
            seekBar.setProgress(timeElapsed / 1000);
            Log.d("run", "run: ");
            mHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        record = (ImageButton)findViewById(R.id.record);


        mFireBase = FirebaseDatabase.getInstance();
        mFireBase.setPersistenceEnabled(true);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child(photo);
        mStorgeRef_Audio = mFirebaseStorage.getReference().child("audio");
        mReference = mFireBase.getReference().child(Massage);
        mReference.keepSynced(true);
        providers =  new ArrayList<>();

        mUsername = ANONYMOUS;

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(MainActivity.this, R.layout.item_message, friendlyMessages, this);
        mMessageListView.setAdapter(mMessageAdapter);


        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null,null);

                mReference.push().setValue(friendlyMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
             FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user !=null){

                    onSignIn(user.getDisplayName());

                }else {
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
                    startActivityForResult(
                            // Get an instance of AuthUI based on the default app
                            AuthUI.getInstance().createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers).build(),
                            RC_SIGN_IN);
                    onSignOut();
                }
            }
        };
       record.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (recording){
                   stoprecord();
                   record.setImageResource(R.drawable.microphone);
                   StorageReference storageReference = mStorgeRef_Audio.child(outputFile);

                   UploadTask uploadTask = storageReference.putFile(Uri.fromFile(new File(getFilesDir() + outputFile)));

// Register observers to listen for when the download is done or if it fails
                   uploadTask.addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception exception) {
                           // Handle unsuccessful uploads
                           Log.e(TAG, "onFailure: ", exception);
                           Toast.makeText(getApplicationContext(), "error Uploading", Toast.LENGTH_LONG).show();

                       }
                   }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                           Uri downloadUrl = taskSnapshot.getDownloadUrl();
                           FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, null, downloadUrl.toString());
                           mReference.push().setValue(friendlyMessage);

                       }
                   });


                   recording = false;
                   //Log.d(TAG, "onClick: true");
               }else {
                   StringBuilder s = new StringBuilder();
                   Random random = new Random();
                   for (int i = 0; i < candidateChars.length(); i++)
                       s.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
                   outputFile = "/" + s.toString() + ".3gpp";
                   myRecorder = new MediaRecorder();
                   myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                   myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                   myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                   Log.d(TAG, "onClick: "+outputFile);
                   myRecorder.setOutputFile(getFilesDir() + outputFile);
                   recorrd();
                   record.setImageResource(R.drawable.stop);
                   //Log.d(TAG, "onClick: false");
                   recording = true;
               }
           }
       });

    }
    public void recorrd (){
        try {
            myRecorder.prepare();
            myRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stoprecord(){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder = null;

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.sign_out_menu)
        {AuthUI.getInstance().signOut(this)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        onSignOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    private void onSignIn(String userName){
        mUsername = userName;
        if (childEventListener == null){
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                mMessageAdapter.add( dataSnapshot.getValue(FriendlyMessage.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.addChildEventListener(childEventListener);
        }


    }
    private void onSignOut(){
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        if (childEventListener !=null){
        mReference.removeEventListener(childEventListener);
        childEventListener =null;}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN)
        {
            if (resultCode == RESULT_OK){

            }else if(resultCode == RESULT_CANCELED){
                finish();
            }
        }
        else if (requestCode == RC_PHOTO_PICKER){

            Uri i = data.getData();
            StorageReference storageReference = mStorageReference.child(i.getLastPathSegment());

            UploadTask uploadTask = storageReference.putFile(i);

// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FriendlyMessage friendlyMessage = new FriendlyMessage(null,mUsername,downloadUrl.toString(),null);
                    mReference.push().setValue(friendlyMessage);

                }
            });

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onlClick(String path, SeekBar seekbar, TextView textview, final ImageButton play) {
        if (media == null)
            media = new MediaPlayer();
        if (media.isPlaying()) {
            media.stop();
            media.release();
            mHandler.removeCallbacks(updateSeekBarTime);
            play.setImageResource(R.drawable.play);
            media = new MediaPlayer();
        } else {
            try {
                media.setDataSource(path);
                media.prepare();
                int i = media.getDuration();

                seekbar.setMax(i / 1000);


                long second = (i / 1000) % 60;
                long minute = (i / (1000 * 60)) % 60;


                String time = String.format("%02d:%02d", minute, second);


                textview.setText(String.valueOf(time));
                seekBar = seekbar;
                mHandler.post(updateSeekBarTime);
                updateSeekBarTime.run();
                play.setImageResource(R.drawable.stop);
                media.start();
                media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            mHandler.removeCallbacks(updateSeekBarTime);
                            media.stop();
                            media.release();
                            media = null;
                            play.setImageResource(R.drawable.play);

                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
