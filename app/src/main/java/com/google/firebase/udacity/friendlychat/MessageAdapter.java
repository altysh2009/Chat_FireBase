package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import static android.R.id.message;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    MediaPlayer mediaPlayer = null;

    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        ConstraintLayout audio = (ConstraintLayout)convertView.findViewById(R.id.audio);
        final ImageButton play = (ImageButton)convertView.findViewById(R.id.play);
        final SeekBar seekBar = (SeekBar)convertView.findViewById(R.id.seekBar);
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
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.GONE);
            audio.setVisibility(View.VISIBLE);
            Log.d("getView", "getView: ");
            Log.d("getView", ": "+message.getAudioUrl());

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer == null)
                    {
                        mediaPlayer = new MediaPlayer();
                        Log.d(" cccccc", "onClick: "+message.getAudioUrl());
                        try {
                           // Log.d(" cccccc", "onClick: "+message.getAudioUrl());
                            mediaPlayer.setDataSource(getContext().getFilesDir() + message.getAudioUrl());

                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    try {
                                        mediaPlayer.stop();
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                        play.setImageResource(R.drawable.play);
                                    }catch (RuntimeException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            play.setImageResource(R.drawable.stop);
                           int i = mediaPlayer.getDuration();


                            long second = (i / 1000) % 60;
                            long minute = (i / (1000 * 60)) % 60;


                            String time = String.format("%02d:%02d", minute, second);


                            dur.setText(String.valueOf(time));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                            play.setImageResource(R.drawable.play);
                        }catch (RuntimeException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        }
        else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            audio.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());

        return convertView;
    }
}
