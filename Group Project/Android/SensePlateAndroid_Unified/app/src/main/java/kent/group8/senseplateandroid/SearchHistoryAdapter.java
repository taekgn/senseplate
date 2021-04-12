package kent.group8.senseplateandroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private List<SearchHistoryItem> historyList;
    private Context context;
    private DatabaseSQLite myDb;

    SearchHistoryAdapter(List<SearchHistoryItem> historyList, Context context, AdapterListener listener) {
        this.historyList = historyList;
        this.context = context;
        onClickListener = listener;
    }

    public AdapterListener onClickListener;

    public interface AdapterListener {
        void iconViewOnClick(View v, int position);
        void iconImageViewOnClick(View v, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodItemTextView;
        ImageView deleteImageView;

        ViewHolder(View itemView) {
            super(itemView);
            foodItemTextView = itemView.findViewById(R.id.text);
            deleteImageView = itemView.findViewById(R.id.iv_delete);
            //search item from historyList
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    String foodItem = historyList.get(pos).foodItem;
                    context.startActivity(new Intent(context, SearchActivity.class).putExtra("foodItem", foodItem).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    onClickListener.iconViewOnClick(v, pos);
                }
            });
            //remove item from history table in database
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    int selectedID = historyList.get(pos).id;
                    myDb = new DatabaseSQLite(context);
                    myDb.removeItemHistory(Integer.toString(selectedID));
                    onClickListener.iconImageViewOnClick(v, pos);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchHistoryItem currentItem = historyList.get(position);
        holder.foodItemTextView.setText(currentItem.foodItem);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}