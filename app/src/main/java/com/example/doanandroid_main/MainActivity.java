package com.example.doanandroid_main;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.AssetFileDescriptor;
import android.app.AlertDialog;
import android.content.DialogInterface;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnSkip = findViewById(R.id.btnSkip);
        Button btnPrevious = findViewById(R.id.btnPrevious);
        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnRepeat = findViewById(R.id.btnRepeat);
        tvSongInfo = findViewById(R.id.tvSongInfo);
        tvSongTime = findViewById(R.id.tvSongTime);

        mediaPlayer = MediaPlayer.create(this, musicResources[currentSongIndex]);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToNextSong();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToPreviousSong();
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

        tvSongInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSongListDialog();
            }
        });

        // Cập nhật thời gian bài hát mỗi giây
        mHandler.postDelayed(updateSongTime, 1000);
    }


    private void playMusic() {
        // Sử dụng MediaMetadataRetriever để lấy thông tin từ tệp nhạc
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(musicResources[currentSongIndex]);
            if (afd != null) {
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lấy tên bài hát và nghệ sĩ từ metadata
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        // Kiểm tra xem thông tin có khả dụng hay không
        if (title == null || title.isEmpty()) {
            title = "Unknown Title";
        }

        if (artist == null || artist.isEmpty()) {
            artist = "Unknown Artist";
        }

        // Hiển thị thông tin bài hát và nghệ sĩ
        String songInfo = "Tên Bài Hát: " + title + " | Nghệ Sĩ: " + artist;
        tvSongInfo.setText(songInfo);

        // Tiếp tục phát nhạc
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Bài hát hiện tại đã hoàn thành, chuyển đến bài hát tiếp theo
                if (isRepeat) {
                    // Nếu chế độ lặp lại đang được bật, chơi lại bài hát hiện tại
                    playMusic();
                } else if (isShuffle) {
                    // Nếu chế độ ngẫu nhiên đang được bật, chọn bài hát ngẫu nhiên
                    playRandomSong();
                } else {
                    // Ngược lại, chuyển đến bài hát tiếp theo trong danh sách
                    skipToNextSong();
                }
            }
        });
    }

    private void stopMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    private void skipToNextSong() {
        currentSongIndex++;
        if (currentSongIndex >= musicResources.length) {
            currentSongIndex = 0;  // Quay lại bài hát đầu tiên nếu đã phát hết danh sách
        }

        // Chuyển đến và chuẩn bị bài hát mới
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(MainActivity.this, musicResources[currentSongIndex]);

        // Lấy thông tin bài hát mới
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(musicResources[currentSongIndex]);
            if (afd != null) {
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String newArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        // Kiểm tra xem thông tin có khả dụng hay không
        if (newTitle == null || newTitle.isEmpty()) {
            newTitle = "Unknown Title";
        }

        if (newArtist == null || newArtist.isEmpty()) {
            newArtist = "Unknown Artist";
        }

        // Hiển thị thông tin bài hát và nghệ sĩ mới
        String newSongInfo = "Tên Bài Hát: " + newTitle + " | Nghệ Sĩ: " + newArtist;
        tvSongInfo.setText(newSongInfo);

        playMusic();
    }

    private void skipToPreviousSong() {
        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = musicResources.length - 1;  // Quay lại bài hát cuối cùng nếu đang ở bài hát đầu tiên
        }

        // Chuyển đến và chuẩn bị bài hát mới
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(MainActivity.this, musicResources[currentSongIndex]);

        // Lấy thông tin bài hát mới
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(musicResources[currentSongIndex]);
            if (afd != null) {
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String newArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        // Kiểm tra xem thông tin có khả dụng hay không
        if (newTitle == null || newTitle.isEmpty()) {
            newTitle = "Unknown Title";
        }

        if (newArtist == null || newArtist.isEmpty()) {
            newArtist = "Unknown Artist";
        }

        // Hiển thị thông tin bài hát và nghệ sĩ mới
        String newSongInfo = "Tên Bài Hát: " + newTitle + " | Nghệ Sĩ: " + newArtist;
        tvSongInfo.setText(newSongInfo);

        playMusic();
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {
            // Nếu chế độ ngẫu nhiên được bật, cập nhật giao diện người dùng hoặc thêm logic xử lý
        } else {
            // Ngược lại, cập nhật giao diện người dùng hoặc thêm logic xử lý
        }
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        if (isRepeat) {
            // Nếu chế độ lặp lại được bật, cập nhật giao diện người dùng hoặc thêm logic xử lý
        } else {
            // Ngược lại, cập nhật giao diện người dùng hoặc thêm logic xử lý
        }
    }

    private void playRandomSong() {
        Random random = new Random();
        currentSongIndex = random.nextInt(musicResources.length);

        // Chuyển đến và chuẩn bị bài hát mới
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(MainActivity.this, musicResources[currentSongIndex]);

        // Lấy thông tin bài hát mới
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(musicResources[currentSongIndex]);
            if (afd != null) {
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String newArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        // Kiểm tra xem thông tin có khả dụng hay không
        if (newTitle == null || newTitle.isEmpty()) {
            newTitle = "Unknown Title";
        }

        if (newArtist == null || newArtist.isEmpty()) {
            newArtist = "Unknown Artist";
        }

        // Hiển thị thông tin bài hát và nghệ sĩ mới
        String newSongInfo = "Tên Bài Hát: " + newTitle + " | Nghệ Sĩ: " + newArtist;
        tvSongInfo.setText(newSongInfo);

        playMusic();
    }

    // Runnable để cập nhật thời gian bài hát
    private Runnable updateSongTime = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying()) {
                int currentTime = mediaPlayer.getCurrentPosition();

                // Cập nhật TextView với thời gian bài hát
                tvSongTime.setText(formatTime(currentTime));
            }

            mHandler.postDelayed(this, 1000);
        }
    };

    // Hàm để chuyển đổi thời gian từ milliseconds sang định dạng hh:mm:ss
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
        // Dừng cập nhật thời gian khi ứng dụng bị đóng
        mHandler.removeCallbacks(updateSongTime);
    }

    // Hàm hiển thị Dialog danh sách bài hát
    private void showSongListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Bài Hát");
        builder.setItems(getSongTitles(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Khi người dùng chọn bài hát từ danh sách, chuyển đến bài hát được chọn
                currentSongIndex = which;
                prepareAndPlaySelectedSong();
            }
        });
        builder.show();
    }

    // Hàm trả về danh sách tên bài hát để sử dụng trong Dialog
    private CharSequence[] getSongTitles() {
        CharSequence[] songTitles = new CharSequence[musicResources.length];
        for (int i = 0; i < musicResources.length; i++) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(musicResources[i]);
                if (afd != null) {
                    retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title == null || title.isEmpty()) {
                title = "Unknown Title";
            }
            songTitles[i] = title;
        }
        return songTitles;
    }

    // Hàm chuẩn bị và phát bài hát được chọn
    private void prepareAndPlaySelectedSong() {
        // Chuyển đến và chuẩn bị bài hát mới
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(MainActivity.this, musicResources[currentSongIndex]);

        // Lấy thông tin bài hát mới
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(musicResources[currentSongIndex]);
            if (afd != null) {
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String newArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        // Kiểm tra xem thông tin có khả dụng hay không
        if (newTitle == null || newTitle.isEmpty()) {
            newTitle = "Unknown Title";
        }

        if (newArtist == null || newArtist.isEmpty()) {
            newArtist = "Unknown Artist";
        }

        // Hiển thị thông tin bài hát và nghệ sĩ mới
        String newSongInfo = "Tên Bài Hát: " + newTitle + " | Nghệ Sĩ: " + newArtist;
        tvSongInfo.setText(newSongInfo);

        // Phát nhạc
        playMusic();
    }
}
