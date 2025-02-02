package com.example.bai1

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.example.bai1.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

const val EXTRA_TITLE = "extra_title"
const val EXTRA_CONTENT = "extra_content"
const val EXTRA_EDIT_TIME = "extra_edit_time"
const val EXTRA_ID = "extra_id"


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteDao: NoteDao
    private val selectedItems = mutableListOf<Note>() // Danh sách các mục đã chọn
    private var showCheckbox = false // Biến để kiểm tra khi nào hiển thị checkbox
    private var listNote = mutableListOf<Note>()
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var database: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.tbMain)
        // Hiển thị dữ liệu
        displayData()

        // Xử lí sự kiện khi mà nhấn add Note
        binding.btAddNote.setOnClickListener {
            val intent = Intent(this@MainActivity, AddActivity::class.java)
            addNoteLauncher.launch(intent) // Chuyển sang add note
        }

        // Xử lí sự kiện khi mà nhấn close
        binding.ivClose.setOnClickListener {
            // Ẩn đi tb_second và hiện tb_main
            binding.btAddNote.visibility = View.VISIBLE
            binding.tbMain.visibility = View.VISIBLE
            binding.tbSecond.visibility = View.GONE
            selectedItems.clear()
            itemAdapter.showCheckbox = false
            itemAdapter.notifyDataSetChanged()

        }

        // Xử lí khi mà bỏ chọn tất cả
        binding.ivClearSelection.setOnClickListener {
            selectedItems.clear()
            binding.tvSelection.text = "Đã chọn 0"
            itemAdapter.notifyDataSetChanged()
        }

        // Khi nhấn icon delete để xóa các item đã chọn
        binding.ivDelete.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Xóa trong cơ sở dữ liệu
                    // Trong launch thì code vẫn là chạy kiểu tuần tự
                    // Các launch thì chạy song song
                    noteDao.delete(*selectedItems.toTypedArray())

                    // Sau khi xóa trong DB, quay lại UI chính
                    withContext(Dispatchers.Main) {
                        // Xóa các item trong danh sách hiển thị
                        binding.btAddNote.visibility = View.VISIBLE
                        listNote.removeAll(selectedItems)
                        selectedItems.clear()

                        // Cập nhật lại giao diện
                        binding.tbMain.visibility = View.VISIBLE
                        binding.tbSecond.visibility = View.GONE
                        itemAdapter.showCheckbox = false
                        itemAdapter.notifyDataSetChanged()

                    }
                } catch (e: Exception) {
                    // Xử lý lỗi nếu có
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Lỗi khi xóa dữ liệu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun displayData() {
        // khởi tạo cơ sở dữ liệu
        database = AppDatabase.getInstance(this)
        noteDao = database.noteDao()


        // Chú ý đoạn này cần khởi tạo biến itemAdapter bên trong launch
        lifecycleScope.launch {
            // Lấy dữ liệu từ cơ sở dữ liệu trên IO thread
            val notesFromDb = withContext(Dispatchers.IO) {
                noteDao.getAll() // Giả sử getAll() trả về danh sách các Note
            }

            // Cập nhật listNote trên Main thread
            listNote.clear()
            listNote.addAll(notesFromDb)

            // Sau khi dữ liệu được lấy xong, cập nhật RecyclerView
            itemAdapter = ItemAdapter(listNote, object : OnItemClickListener {
                override fun onItemClick(note: Note) {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra(EXTRA_TITLE, note.title)
                    intent.putExtra(EXTRA_EDIT_TIME, note.editTime)
                    intent.putExtra(EXTRA_CONTENT, note.content)
                    intent.putExtra(EXTRA_ID, note.id)
                    editNoteLauncher.launch(intent)  // Chuyển sang DetailActivity
                }

                override fun onItemLongClick() {
                    binding.btAddNote.visibility = View.GONE
                    binding.tbMain.visibility = View.GONE
                    binding.tbSecond.visibility = View.VISIBLE
                    showCheckbox = true
                    itemAdapter.showCheckbox = true
                    itemAdapter.notifyDataSetChanged()
                }
            },selectedItems,binding.tvSelection,showCheckbox)

            // Gán adapter vào RecyclerView sau khi lấy dữ liệu
            binding.rvMain.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.rvMain.adapter = itemAdapter

        }
    }


    private val editNoteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Cập nhật vào trong cơ sở dữ liệu
                val data = result.data
                val title = data?.getStringExtra(EXTRA_TITLE)
                val content = data?.getStringExtra(EXTRA_CONTENT)
                val editTime = data?.getStringExtra(EXTRA_EDIT_TIME)
                val id = data?.getIntExtra(EXTRA_ID, 0)

                Log.d("kiem tra", "$id")
                // Tìm Note tương ứng với id để sửa
                val index = listNote.indexOfFirst { it.id == id }
                if (index != -1) {
                    // Cập nhật thông tin của Note đó
                    listNote[index].title = title
                    listNote[index].content = content
                    listNote[index].editTime = editTime
                }
                lifecycleScope.launch {
                    noteDao.update(listNote[index])
                }
                // Thông báo cập nhật lên trên RecyclerView
                // Cập nhật cái này thì chỉ cần thay đổi cái đấy
                itemAdapter.notifyItemChanged(index)
            }
        }


    private val addNoteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val title = data?.getStringExtra(EXTRA_TITLE)
                val content = data?.getStringExtra(EXTRA_CONTENT)
                val editTime = data?.getStringExtra(EXTRA_EDIT_TIME)
                val note = Note(0, title, content, editTime)
                listNote.add(note)
                // Thông báo cập nhật lên trên RecyclerView
                // Chỉ cần thêm vào cuối thôi
                itemAdapter.notifyItemInserted(listNote.size-1)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Nạp menu từ tệp XML vào menu của Activity
        menuInflater.inflate(R.menu.menu_item, menu)

        // Tìm SearchView
        val searchItem = menu?.findItem(R.id.search)
        // Cần phải ép kiểu chuẩn
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Nhập tiêu đề"

        Log.d("test", "dc")
        // Khi mà tìm thấy SearchView thiết lập Listener
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val listCopy = listNote.toMutableList()
                val query = newText.toString().trim()

                if (query.isNotEmpty()) {
                    // Nếu mà không trống tìm kiếm theo title không phần biệt theo chữ hoa và chữ thường
                    val filteredList = listCopy.filter {
                        it.title!!.contains(
                            query,
                            ignoreCase = true
                        )
                    }

                    if (filteredList.isNotEmpty()) {
                        itemAdapter.list = filteredList.toMutableList()
                        itemAdapter.notifyDataSetChanged()
                    }
                } else {
                    // Nếu từ khóa tìm kiếm là rỗng
                    itemAdapter.list = listNote
                    itemAdapter.notifyDataSetChanged()
                }
                return true
            }
        })


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                true
            }

            R.id.edit_time_new -> {
                // Dùng để chuyển đổi thành đối tượng Date để so sánh
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                // sắp xếp thời gian mới nhất
                listNote = listNote.sortedByDescending {
                    dateFormat.parse(it.editTime.toString())
                }.toMutableList()
                // cập nhật lên RecyclerView
                itemAdapter.list = listNote
                itemAdapter.notifyDataSetChanged()
                true
            }

            R.id.edit_time_old -> {
                // Dùng để chuyển đổi thành đối tượng Date để so sánh
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                // Sắp xếp theo thời gian cũ nhất trước
                listNote = listNote.sortedBy {
                    dateFormat.parse(it.editTime.toString())
                }.toMutableList()

                // Cập nhật lên RecyclerView
                itemAdapter.list = listNote
                itemAdapter.notifyDataSetChanged()
                true
            }

            R.id.title_a_z -> {
                // Dùng để sắp xếp theo title từ A -> Z
                listNote = listNote.sortedBy { it.title }.toMutableList()

                // Cập nhật lên RecyclerView
                itemAdapter.list = listNote
                itemAdapter.notifyDataSetChanged()

                true
            }

            R.id.title_z_a -> {
                // Dùng để sắp xếp theo title từ Z -> A
                listNote = listNote.sortedByDescending { it.title }.toMutableList()

                // Cập nhật lên RecyclerView
                itemAdapter.list = listNote
                itemAdapter.notifyDataSetChanged()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
