package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupListBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import java.util.*

class GroupListFragment : Fragment() {

    private val controller: GroupListController by lazy { GroupListController() }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FragmentGroupListBinding>(inflater, R.layout.fragment_group_list, container, false).apply {

            recyclerView.adapter = controller.adapter

            controller.groups = listOf(Group("a", "test", "adada", Timestamp(Date())))

        }.root
    }
}