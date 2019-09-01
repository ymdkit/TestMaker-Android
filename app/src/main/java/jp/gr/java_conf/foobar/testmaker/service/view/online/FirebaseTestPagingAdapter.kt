package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.view.share.ImageTextButton


open class FirebaseTestPagingAdapter(private val context: Context,options: FirestorePagingOptions<FirebaseTest>) : FirestorePagingAdapter<FirebaseTest, FirebaseTestPagingAdapter.ViewHolder>(options) {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var download = { _: String -> }
    var showInfo = { _: FirebaseTest -> }
    var startLoading ={}
    var finishLoading = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_test_online, parent, false))

    }

    override fun onLoadingStateChanged(state: LoadingState) {

        when(state){
            LoadingState.LOADING_INITIAL,LoadingState.LOADING_MORE->{
                startLoading()
            }
            LoadingState.LOADED,LoadingState.ERROR->{
                finishLoading()
            }
            else -> {
                finishLoading()
            }
        }

        super.onLoadingStateChanged(state)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, data: FirebaseTest) {

        holder.title.text = data.name
        holder.num.text = context.getString(R.string.num_questions,data.size)
        holder.play.setOnClickListener {
            download(data.documentId)

        }

        holder.information.setOnClickListener{

            showInfo(data)

        }

        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle,null) as GradientDrawable
        drawable.setColor(context.resources.getIntArray(R.array.color_list)[Math.min(Math.abs(data.color), 7)])
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