package com.example.doanandroid_main;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private TextView tvSongInfo;
    private TextView tvSongTime;
    private int currentSongIndex = 0;
    private int[] musicResources = {R.raw.song1, R.raw.song2, R.raw.song3};
    private Handler mHandler = new Handler();
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ImageButton btnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        ImageButton btnSkip = findViewById(R.id.btnSkip);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);
        ImageButton btnShuffle = findViewById(R.id.btnShuffle);
        ImageButton btnRepeat = findViewById(R.id.btnRepeat);
        tvSongInfo = findViewById(R.id.tvSongInfo);
        tvSongTime = findViewById(R.id.tvSongTime);

        mediaPlayer = new MediaPlayer();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSongListDialog();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseMusic();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.getCurrentPosition() >= mediaPlayer.getDuration()) {
                    skipToNextSong();
                } else {
                    mediaPlayer.stop();
                    skipToNextSong();
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int rewindThreshold = 5000;

                if (currentPosition <= rewindThreshold) {
                    skipToPreviousSong();
                } else {
                    mediaPlayer.seekTo(0);
                }
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShuffle();
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRepeat();
            }
        });

        mHandler.postDelayed(updateSongTime, 1000);
    }

    private void playMusic(int selectedSongIndex) {
        currentSongIndex = selectedSongIndex;
        int musicResource = musicResources[currentSongIndex];
        String songName = getResources().getResourceEntryName(musicResource);
        String songInfo = "Tên Bài Hát: " + songName;
        tvSongInfo.setText(songInfo);

        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), musicResource);
                mediaPlayer.start();
                updatePauseButtonImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isRepeat) {
                    playMusic(currentSongIndex);
                } else if (isShuffle) {
                    playRandomSong();
                } else {
                    skipToNextSong();
                }
            }
        });
    }

    private void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updatePauseButtonImage();
        } else {
            mediaPlayer.start();
            updatePauseButtonImage();
        }
    }

    private void updatePauseButtonImage() {
        if (mediaPlayer.isPlaying()) {
            btnPause.setImageResource(R.drawable.ic_pause);
        } else {
            btnPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void skipToNextSong() {
        currentSongIndex++;
        if (currentSongIndex >= musicResources.length) {
            currentSongIndex = 0;
        }
        playMusic(currentSongIndex);
    }

    private void skipToPreviousSong() {
        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = musicResources.length - 1;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            playMusic(currentSongIndex);
        } else {
            displaySongInfo(currentSongIndex);
        }
    }

    private void displaySongInfo(int songIndex) {
        int musicResource = musicResources[songIndex];
        String songName = getResources().getResourceEntryName(musicResource);
        String songInfo = "Tên Bài Hát: " + songName;
        tvSongInfo.setText(songInfo);
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {
            // Update UI or add logic for shuffle mode
        } else {
            // Update UI or add logic for non-shuffle mode
        }
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        if (isRepeat) {
            // Update UI or add logic for repeat mode
        } else {
            // Update UI or add logic for non-repeat mode
        }
    }

    private void playRandomSong() {
        Random random = new Random();
        currentSongIndex = random.nextInt(musicResources.length);
        playMusic(currentSongIndex);
    }

    private Runnable updateSongTime = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying()) {
                int currentTime = mediaPlayer.getCurrentPosition();
                tvSongTime.setText(formatTime(currentTime));
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        int hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mHandler.removeCallbacks(updateSongTime);
    }

    private void showSongListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Bài Hát");
        builder.setItems(getSongTitles(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playMusic(which);
            }
        });
        builder.show();
    }

    private CharSequence[] getSongTitles() {
        CharSequence[] songTitles = new CharSequence[musicResources.length];
        for (int i = 0; i < musicResources.length; i++) {
            String songName = getResources().getResourceEntryName(musicResources[i]);
            if (songName == null || songName.isEmpty()) {
                songName = "Unknown Title";
            }
            songTitles[i] = songName;
        }
        return songTitles;
    }
}
