package com.example.bai1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bai1.databinding.ActivityAddBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var currentTime: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Chỉ lên gọi 1 lần
        database = AppDatabase.getInstance(this)
        noteDao = database.noteDao()


        // Hiển thị thời gian tạo Note
        currentTime = getCurrentDateTime()
        binding.tvEditTime.text = currentTime

        binding.ivConfirm.setOnClickListener {
            confirmAddNode()
        }

        // khi mà ấn back thì quay về
        binding.ivBackArrow.setOnClickListener {
            finish()
        }
    }

    private fun confirmAddNode() {
        // bắt buộc phải nhập tiêu đề và nội dung thì mới có thể xác nhận add Note
        if (binding.etTitle.text.isEmpty() || binding.etContent.text.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT)
                .show()
        } else {
            // khi mà nhập đầy đủ thông tin thì add vào trong db
            val note = Note(
                0,
                binding.etTitle.text.toString(),
                binding.etContent.text.toString(),
                currentTime
            )
            // lưu vào trong db
            lifecycleScope.launch(Dispatchers.IO){
                noteDao.insert(note)
            }
            // Truyền dữ liệu sang bên MainActivity
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_TITLE,binding.etTitle.text.toString())
            resultIntent.putExtra(EXTRA_EDIT_TIME,binding.tvEditTime.text.toString())
            resultIntent.putExtra(EXTRA_CONTENT,binding.etContent.text.toString())
            setResult(RESULT_OK,resultIntent)
            finish()
        }
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