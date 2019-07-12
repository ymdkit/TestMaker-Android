package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.nifcloud.mbaas.core.NCMBObject
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton
import java.lang.Math.abs
import java.lang.Math.min

open class FirebaseTestAdapter(private val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<FirebaseTestAdapter.ViewHolder>() {

    var array: List<DocumentSnapshot>  = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var download = { _: DocumentSnapshot -> }
    var showInfo = { _: FirebaseTest -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_test_online, parent, false))

    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = array[holder.adapterPosition].toObject(FirebaseTest::class.java) ?: return

        holder.title.text = data.name
        holder.num.text = context.getString(R.string.num_questions,data.size)
        holder.play.setOnClickListener {

            download(array[holder.adapterPosition])

        }

        holder.information.setOnClickListener{

            showInfo(data)

        }

        val drawable = ResourcesCompat.getDrawable(context.resources,R.drawable.circle,null) as GradientDrawable
        drawable.setColor(context.resources.getIntArray(R.array.color_list)[min(abs(data.color),7)])
        holder.cate.background = drawable

    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        val cate: ImageButton = v.findViewById(R.id.color)
        val title: TextView = v.findViewById(R.id.text_title_category)
        val num: TextView = v.findViewById(R.id.num_questions)
        val num_download: TextView = v.findViewById(R.id.num_download)
        val information: ImageTextButton = v.findViewById(R.id.open)
        val play: ImageTextButton = v.findViewById(R.id.play)

    }

}