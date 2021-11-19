package com.ravisharma.playbackmusic.utils.alert;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ravisharma.playbackmusic.R;

public class PlaylistAlert {

    private final Context context;
    private final AlertClickListener listener;

    public PlaylistAlert(Context context, AlertClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void showCreateListAlert() {
        View v = LayoutInflater.from(context).inflate(R.layout.alert_create_playlist, null);

        final EditText edPlayListName = v.findViewById(R.id.edPlaylistName);
        TextView tvCancel = v.findViewById(R.id.tvCancel);
        TextView tvOk = v.findViewById(R.id.tvOk);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(v);
        dialog.setCancelable(false);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        edPlayListName.requestFocus();

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playlistName = edPlayListName.getText().toString().trim();
                if (playlistName.length() > 0) {
                    if (playlistName.equalsIgnoreCase("My Favorites") ||
                            playlistName.equalsIgnoreCase("Recent Added") ||
                            playlistName.equalsIgnoreCase("Last Played") ||
                            playlistName.equalsIgnoreCase("Most Played")){

                        Toast.makeText(context, "Named Playlist Already Exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] words = playlistName.split("\\s+");
                    StringBuilder capitalizeWord = new StringBuilder();
                    for (String w : words) {
                        String first = w.substring(0, 1);
                        String second = w.substring(1);
                        capitalizeWord.append(first.toUpperCase()).append(second.toLowerCase()).append(" ");
                    }
                    playlistName = capitalizeWord.toString().trim();
                    listener.OnOkClicked(playlistName);
                }
                edPlayListName.setText("");
                alertDialog.dismiss();
            }
        });
    }

    public void showUpdateListAlert(final String oldPlaylistName) {
        View v = LayoutInflater.from(context).inflate(R.layout.alert_create_playlist, null);

        final EditText edPlayListName = v.findViewById(R.id.edPlaylistName);
        TextView dialogTitle = v.findViewById(R.id.dialogTitle);
        TextView tvCancel = v.findViewById(R.id.tvCancel);
        TextView tvOk = v.findViewById(R.id.tvOk);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(v);
        dialog.setCancelable(false);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();

        dialogTitle.setText("Rename Playlist");
        edPlayListName.setText(oldPlaylistName);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        edPlayListName.requestFocus();
        tvOk.setText("Rename");

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playlistName = edPlayListName.getText().toString().trim();
                if (playlistName.length() > 0) {
                    if (playlistName.equalsIgnoreCase("My Favorites") ||
                            playlistName.equalsIgnoreCase("Recent Added") ||
                            playlistName.equalsIgnoreCase("Last Played") ||
                            playlistName.equalsIgnoreCase("Most Played")){

                        Toast.makeText(context, "Named Playlist Already Exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] words = playlistName.split("\\s+");
                    StringBuilder capitalizeWord = new StringBuilder();
                    for (String w : words) {
                        String first = w.substring(0, 1);
                        String second = w.substring(1);
                        capitalizeWord.append(first.toUpperCase()).append(second.toLowerCase()).append(" ");
                    }
                    playlistName = capitalizeWord.toString().trim();
                    listener.OnOkClicked(playlistName);
                }
                edPlayListName.setText("");
                alertDialog.dismiss();
            }
        });
    }
}
