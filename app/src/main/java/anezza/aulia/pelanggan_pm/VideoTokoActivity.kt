package anezza.aulia.pelanggan_pm

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityVideoTokoBinding

class VideoTokoActivity : AppCompatActivity() {

    private lateinit var b: ActivityVideoTokoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityVideoTokoBinding.inflate(layoutInflater)
        setContentView(b.root)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(b.videoView)
        b.videoView.setMediaController(mediaController)

        val videoUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
        b.videoView.setVideoURI(Uri.parse(videoUrl))

        b.videoView.setOnPreparedListener {
            Toast.makeText(this, "Video siap diputar", Toast.LENGTH_SHORT).show()
        }

        b.videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Video gagal diputar", Toast.LENGTH_SHORT).show()
            true
        }

        b.btnPlay.setOnClickListener {
            b.videoView.start()
        }

        b.btnPause.setOnClickListener {
            if (b.videoView.isPlaying) {
                b.videoView.pause()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        b.videoView.stopPlayback()
    }
}