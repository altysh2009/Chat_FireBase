package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.udacity.friendlychat.data.DataContract;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    private static FirebaseStorage mFirebaseStorage;
    media m;
    int pos;
    String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private MediaPlayer mediaPlayer = null;
    private boolean downloaded = false;
    private Handler mHandler = new Handler();

    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects, media m) {
        super(context, resource, objects);
        this.m = m;

        if (mFirebaseStorage == null)
            mFirebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        ConstraintLayout audio = (ConstraintLayout)convertView.findViewById(R.id.audio);
        final ImageButton play;
        play = (ImageButton) convertView.findViewById(R.id.play);
        final SeekBar seekBar = (SeekBar) convertView.findViewById(R.id.seekBar);
        final ProgressBar progressbar = (ProgressBar) convertView.findViewById(R.id.progressBar2);
        final TextView dur = (TextView) convertView.findViewById(R.id.durtation);

        final FriendlyMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            audio.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else if (message.getAudioUrl() !=null){
            if (true) {
                StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl(message.getAudioUrl());
                try {
                    progressbar.setVisibility(View.VISIBLE);
//

                    final String u = message.getAudioUrl().replace("-", "").substring(message.getAudioUrl().length() - 33);
                    final File localFile = File.createTempFile(u, ".3gpp");
                    // Toast.makeText(getContext(),localFile.getPath(),Toast.LENGTH_LONG).show();
                    if (!localFile.exists()) {
                        localFile.mkdir();
                    }
                    //Toast.makeText(getContext(),localFile.getPath(),Toast.LENGTH_LONG).show();
                    storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Local temp file has been

                            progressbar.setVisibility(View.INVISIBLE);
                            downloaded = true;
                            ContentValues c = new ContentValues();
                            c.put(DataContract.DataEntry.AudioId, u);
                            c.put(DataContract.DataEntry.AudioPath, localFile.getPath());
                            c.put(DataContract.DataEntry.AudioLink, message.getAudioUrl());
                            getContext().getContentResolver().insert(DataContract.uri, c);
                            Cursor cc = getContext().getContentResolver().query(DataContract.uri, null, DataContract.DataEntry.AudioLink + "=?", new String[]{message.getAudioUrl()}, null);

                            cc.moveToNext();

                            final String u = cc.getString(cc.getColumnIndex(DataContract.DataEntry.AudioPath));

                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), "to  ", Toast.LENGTH_LONG).show();
                                    m.onlClick(u, seekBar, dur, play);

                                }
                            });
                            cc.close();



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors

                            dur.setText(R.string.error);
                            dur.setVisibility(View.VISIBLE);
                            downloaded = false;

                        }
                    });
                } catch (RuntimeException | IOException e) {
                    e.printStackTrace();

                    dur.setVisibility(View.VISIBLE);
                    dur.setText(R.string.error);
                    downloaded = false;
                }
            } else {
                Cursor c = getContext().getContentResolver().query(DataContract.uri, null, DataContract.DataEntry.AudioLink + "=?", new String[]{message.getAudioUrl()}, null);

                c.moveToNext();

                final String u = c.getString(c.getColumnIndex(DataContract.DataEntry.AudioPath));

                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "to  ", Toast.LENGTH_LONG).show();
                        m.onlClick(u, seekBar, dur, play);

                    }
                });
                c.close();
            }
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.GONE);
            audio.setVisibility(View.VISIBLE);


        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            audio.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());

        return convertView;
    }

    private void done(FriendlyMessage message) {




    }

    public interface media {
        void onlClick(String path, SeekBar seekbar, TextView textview, ImageButton play);
    }
}






