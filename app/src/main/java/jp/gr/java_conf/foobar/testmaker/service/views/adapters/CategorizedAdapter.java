package jp.gr.java_conf.foobar.testmaker.service.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;
import net.cattaka.android.adaptertoolbox.adapter.listener.ForwardingListener;
import net.cattaka.android.adaptertoolbox.adapter.listener.ListenerRelay;

import java.util.List;

import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;
import jp.gr.java_conf.foobar.testmaker.service.models.Test;

/**
 * Created by keita on 2017/05/21.
 */

public class CategorizedAdapter extends ScrambleAdapter {

    private RealmController mRealmController;
    private String category;

    public CategorizedAdapter(@NonNull Context context,
                              @NonNull List<Object> items,
                              @Nullable ListenerRelay<ScrambleAdapter<?>,
                                     RecyclerView.ViewHolder> listenerRelay,
                              RealmController realm,
                              @NonNull String category,
                              @NonNull IViewHolderFactory<ScrambleAdapter<?>,
                                     RecyclerView.ViewHolder,
                                     ForwardingListener<ScrambleAdapter<?>, RecyclerView.ViewHolder>,
                                     ?
                                     >... iViewHolderFactories

                             ) {
        super(context, items, listenerRelay, iViewHolderFactories);

        mRealmController = realm;
        this.category = category;
    }

    @Override
    public int getItemCount() {
        return mRealmController.getCategorizedList(category).size();
    }

    @Override
    public Object getItemAt(int position) {

        return mRealmController.getCategorizedList(category).get(position);
    }

    public List<Test> getItems() {

        return mRealmController.getCategorizedList(category);
    }

}
