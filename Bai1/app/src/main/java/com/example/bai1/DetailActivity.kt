package com.example.bai1

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bai1.databinding.ActivityDetailBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DetailActivity : AppCompatActivity() {
    private lateinit var dataBase: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var binding: ActivityDetailBinding

    // Cờ để kiểm tra xem cái nào thay đổi trước để cập nhật thời gian
    private var titleChanged = false
    private var contentChanged = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        dataBase = AppDatabase.getInstance(this)
        noteDao = dataBase.noteDao()

        display()

        // lắng nghe xem người dùng có sửa note không
        setupListener()

        binding.ivConfirm.setOnClickListener {
            if (binding.etTitle.text.isEmpty() || binding.etContent.text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // truyền dữ liệu sang bên MainActivity
                val id = intent.getIntExtra(EXTRA_ID,0)
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_TITLE, binding.etTitle.text.toString())
                resultIntent.putExtra(EXTRA_EDIT_TIME, binding.tvEditTime.text.toString())
                resultIntent.putExtra(EXTRA_CONTENT, binding.etContent.text.toString())
                resultIntent.putExtra(EXTRA_ID,id)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        // khi mà ấn back arrow
        binding.ivBackArrow.setOnClickListener {
            finish()
        }
    }


    private fun display() {
        val title = intent.getStringExtra(EXTRA_TITLE)
        val content = intent.getStringExtra(EXTRA_CONTENT)
        val editTime = intent.getStringExtra(EXTRA_EDIT_TIME)

        binding.etTitle.text.append(title)
        binding.etContent.text.append(content)
        binding.tvEditTime.text = editTime
    }

    private fun setupListener() {
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (titleChanged == false) {
                    titleChanged = true
                    // tức là lần đầu mà thay đổi title
                    if (contentChanged == false) {
                        // lúc này thì cập nhật thời gian chỉnh sửa
                        binding.tvEditTime.text = getCurrentDateTime()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (contentChanged == false) {
                    contentChanged = true
                    // tức là lần đầu mà thay đổi content
                    if (titleChanged == false) {
                        // lúc này thì cập nhật thời gian chỉnh sửa
                        binding.tvEditTime.text = getCurrentDateTime()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun getCurrentDateTime(): String {
        // Lấy thời gian hiện tại
        val currentDateTime = LocalDateTime.now()

        // Đinh dạng thời gian theo "yyyy-MM-dd HH:mm"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // trả về thời gian đã định dạng
        return currentDateTime.format(formatter)
    }
}