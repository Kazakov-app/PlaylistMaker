package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activitySettings)) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.share_app).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.url_of_Practicum) // Ссылка на курс
                )
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }

        findViewById<TextView>(R.id.write_support).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.myEmail))) // Ваш email
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.theme_support)) // Тема письма
                putExtra(Intent.EXTRA_TEXT, getString(R.string.message_support)) // Текст письма
            }
            startActivity(Intent.createChooser(emailIntent, null))
        }

        findViewById<TextView>(R.id.terms_of_use).setOnClickListener {
            val termsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(getString(R.string.terms_of_use)) // Ссылка на оферту
            }
            startActivity(termsIntent)
        }
    }
}
