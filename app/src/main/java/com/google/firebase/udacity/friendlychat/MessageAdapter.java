package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    static FirebaseStorage mFirebaseStorage;
    ArrayList<String> paths = new ArrayList<>();
    ArrayList<String> poston = new ArrayList<>();
    MediaPlayer mediaPlayer = null;
    SeekBar seekBar;
    boolean downloaded = false;
    TextView dur;
    ImageButton play;
    int pos;
    private Handler mHandler = new Handler();
    Runnable updateSeekBarTime = new Runnable() {
        public void run() {

            //get current position
            int timeElapsed = mediaPlayer.getCurrentPosition();

            //set seekbar progress using time played
            seekBar.setProgress(timeElapsed / 1000);
            Log.d("run", "run: ");
            mHandler.postDelayed(this, 500);
        }
    };
    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);


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
        play = (ImageButton) convertView.findViewById(R.id.play);
        seekBar = (SeekBar) convertView.findViewById(R.id.seekBar);
        final ProgressBar progressbar = (ProgressBar) convertView.findViewById(R.id.progressBar2);
        dur = (TextView) convertView.findViewById(R.id.durtation);

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
            if (!poston.contains(position + "")) {
                StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl(message.getAudioUrl());
                try {
                    progressbar.setVisibility(View.VISIBLE);
//                play.setVisibility(View.INVISIBLE);
//                seekBar.setVisibility(View.INVISIBLE);
//                dur.setVisibility(View.INVISIBLE);

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
                            play.setVisibility(View.VISIBLE);
                            seekBar.setVisibility(View.VISIBLE);
                            dur.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.INVISIBLE);
                            downloaded = true;
                            paths.add(localFile.getPath());
                            poston.add(position + "");
                            pos = position;
                            done(message);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            play.setVisibility(View.INVISIBLE);
                            progressbar.setVisibility(View.INVISIBLE);
                            seekBar.setVisibility(View.INVISIBLE);
                            dur.setText(R.string.error);
                            dur.setVisibility(View.VISIBLE);
                            downloaded = false;
                            Log.e("tag", "onFailure: ", exception);
                        }
                    });
                } catch (RuntimeException | IOException e) {
                    e.printStackTrace();
                    play.setVisibility(View.INVISIBLE);
                    progressbar.setVisibility(View.INVISIBLE);
                    seekBar.setVisibility(View.INVISIBLE);
                    dur.setVisibility(View.VISIBLE);
                    dur.setText(R.string.error);
                    downloaded = false;
                }
            } else done(message);
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

    void done(FriendlyMessage message) {
        play.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        dur.setVisibility(View.VISIBLE);
        mediaPlayer = new MediaPlayer();
        final String u = paths.get(poston.indexOf(pos + ""));
        Toast.makeText(getContext(), u, Toast.LENGTH_SHORT).show();
        try {
            mediaPlayer.setDataSource(u);
            mediaPlayer.prepare();

            int i = mediaPlayer.getDuration();
            seekBar.setMax(i / 1000);


            long second = (i / 1000) % 60;
            long minute = (i / (1000 * 60)) % 60;


            String time = String.format("%02d:%02d", minute, second);


            dur.setText(String.valueOf(time));
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(u);
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {

                        // mHandler.post(updateSeekBarTime);
                        mediaPlayer.start();
                        //updateSeekBarTime.run();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                try {
                                    mHandler.removeCallbacks(updateSeekBarTime);
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                    play.setImageResource(R.drawable.play);

                                } catch (RuntimeException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        play.setImageResource(R.drawable.stop);



                    }
                    else {
                        try {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mHandler.removeCallbacks(updateSeekBarTime);

                            mediaPlayer = null;
                            play.setImageResource(R.drawable.play);
                        }catch (RuntimeException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface media {
        void onClick(String path);
    }
}
