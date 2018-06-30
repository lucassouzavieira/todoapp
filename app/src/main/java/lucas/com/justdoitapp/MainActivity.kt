package lucas.com.justdoitapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var listNotes = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            listNotes.add(Task(1, "Casa", "Limpar a casa"))
            listNotes.add(Task(2, "Web", "Terminar trabalho de programação web"))
            listNotes.add(Task(2, "Mobile", "Terminar trabalho de programação mobile"))

            loadQueryAll()
            lvNotes.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
                Toast.makeText(this, "Click on " + listNotes[position].title, Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            Log.i(ex.javaClass.name, ex.message)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.addNote -> {
                    var intent = Intent(this, TaskActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadQueryAll()
    }

    fun loadQueryAll() {

        var dbManager = TaskDbManager(this)
        val cursor = dbManager.queryAll()

        listNotes.clear()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val title = cursor.getString(cursor.getColumnIndex("Title"))
                val content = cursor.getString(cursor.getColumnIndex("Content"))

                listNotes.add(Task(id, title, content))
                print(listNotes.toString())

            } while (cursor.moveToNext())
        }

        var taskAdapter = TaskAdapter(this, listNotes)
        lvNotes.adapter = taskAdapter
    }

    /**
     * Tasks adapter class
     */
    inner class TaskAdapter : BaseAdapter {

        private var taskList = ArrayList<Task>()
        private var context: Context? = null

        constructor(context: Context, taskList: ArrayList<Task>) : super() {
            this.taskList = taskList
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            val view: View?
            val vh: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.task, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
                Log.i("JSA", "set Tag for ViewHolder, position: " + position)
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }

            var mNote = taskList[position]

            vh.tvTitle.text = mNote.title
            vh.tvContent.text = mNote.content

            vh.ivEdit.setOnClickListener {
                updateNote(mNote)
            }

            vh.ivDelete.setOnClickListener {
                var dbManager = TaskDbManager(this.context!!)
                val selectionArgs = arrayOf(mNote.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return taskList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return taskList.size
        }
    }

    private fun updateNote(note: Task) {
        var intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("MainActId", note.id)
        intent.putExtra("MainActTitle", note.title)
        intent.putExtra("MainActContent", note.content)
        startActivity(intent)
    }

    private class ViewHolder(view: View?) {
        val tvTitle: TextView = view?.findViewById(R.id.tvTitle) as TextView
        val tvContent: TextView = view?.findViewById(R.id.tvContent) as TextView
        val ivEdit: ImageView = view?.findViewById(R.id.ivEdit) as ImageView
        val ivDelete: ImageView = view?.findViewById(R.id.ivDelete) as ImageView
    }
}
