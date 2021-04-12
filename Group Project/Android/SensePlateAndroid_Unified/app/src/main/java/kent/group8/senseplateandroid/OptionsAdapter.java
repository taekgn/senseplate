package kent.group8.senseplateandroid;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {

    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        public View view;
        private LineChartActivity graph;

        ViewHolder(View v) {
            super(v);
            graph = new LineChartActivity();
            imageView = (ImageView)itemView.findViewById(R.id.image);
            view = v;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    graph.setData(optionsList.get(pos).option, context);
                }
            });
        }
    }

    List<OptionsStore> optionsList;

    OptionsAdapter(List<OptionsStore> optionsList, Context context){
        this.optionsList = optionsList;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        int id = context.getResources().getIdentifier(optionsList.get(i).image, "drawable", context.getPackageName());
        viewHolder.imageView.setImageResource(id);

    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }
}