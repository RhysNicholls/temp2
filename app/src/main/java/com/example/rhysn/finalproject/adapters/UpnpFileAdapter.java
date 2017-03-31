package com.example.rhysn.finalproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rhysn.finalproject.R;
import org.fourthline.cling.support.model.item.Item;
import com.example.rhysn.finalproject.utils.ContainerWrapper;

import java.util.List;
import java.util.Objects;

/**
 * Created by rhysn on 28/03/2017.
 */

public class UpnpFileAdapter extends ArrayAdapter<UpnpFileAdapter.ListItem> implements View.OnClickListener{

    private AdapterView.OnItemClickListener clickListener;

    public UpnpFileAdapter(AdapterView.OnItemClickListener listener, Context context, int resource){
        super(context, resource);
        clickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.fragment_browser_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.file_browser_list_item_text);
            view.setTag(viewHolder);
        }

        ViewHolder holder =(ViewHolder) view.getTag();
        ListItem listItem = getItem(position);
        if(listItem != null){
            holder.position = position;
            holder.text.setText(listItem.toString());
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        RelativeLayout parent = (RelativeLayout) v.getParent();
        ViewHolder viewHolder = (ViewHolder) parent.getTag();
        ListItem listItem = getItem(viewHolder.position);

    }

    public static class ListItem{
       public static final ListItem PREVIOUS_CONTAINER_LIST_ITEM = new ListItem(null, null);

        private ContainerWrapper containerWrapper;
        private Item listItem;
        private List<Item> mediaItems;

        private ListItem(ContainerWrapper container, Item item){
            containerWrapper = container;
            listItem = item;

        }

        public ListItem(@NonNull ContainerWrapper container){
            this(container, null);

        }

        public ListItem(@NonNull Item item){
            this(null, item);
        }



        public Item getListItem() {
            return listItem;
        }

        public ContainerWrapper getContainerWrapper() {
            return containerWrapper;
        }

        public void setMediaItems(List<Item> mediaItems) {
            this.mediaItems = mediaItems;
        }

        public List<Item> getMediaItems() {

            return mediaItems;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ListItem){
                ListItem other = (ListItem) obj;
                return Objects.equals(containerWrapper, other.containerWrapper) && Objects.equals(listItem, other.listItem)
                        && Objects.equals(mediaItems, other.mediaItems);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(containerWrapper, listItem, mediaItems);

        }

        @Override
        public String toString() {
            if(isPreviousContainer()){
                return "Back to Previous Folder";
            }
            return hasContainer() ? containerWrapper.getTitle() : listItem.getTitle();
        }

        boolean containsMediaItems(){
            return mediaItems.size() > 0;
        }

        public boolean hasContainer(){
            return containerWrapper != null;
        }

        public boolean isPreviousContainer(){
            return containerWrapper == null && listItem == null;
        }

    }
    private static class ViewHolder {
        int position;
        TextView text;

    }
}
