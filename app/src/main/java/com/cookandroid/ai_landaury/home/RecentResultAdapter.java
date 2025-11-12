package com.cookandroid.ai_landaury.home;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.ai_landaury.R;

import java.io.File;
import java.util.List;

public class RecentResultAdapter extends RecyclerView.Adapter<RecentResultAdapter.ViewHolder> {

    public interface OnItemClickListener { void onItemClick(int position); }

    private final Context context;
    private final List<RecentResultItem> list;
    private OnItemClickListener clickListener;

    public RecentResultAdapter(Context context, List<RecentResultItem> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RecentResultItem item = list.get(position);

        h.tvName.setText(item.getName());
        h.tvDate.setText(item.getDate());

        String path = item.getImageUri();
        if (path != null && !path.isEmpty()) {
            // 절대경로 형태 처리
            if (path.startsWith("file://")) path = Uri.parse(path).getPath();
            File f = new File(path);
            if (f.exists()) {
                h.img.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
            } else {
                h.img.setImageResource(item.getImgResId());
            }
        } else {
            h.img.setImageResource(item.getImgResId());
        }

        View.OnClickListener click = v -> {
            if (clickListener != null) clickListener.onItemClick(h.getBindingAdapterPosition());
        };
        h.itemView.setOnClickListener(click);
        h.tvName.setOnClickListener(click);
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgCloth);
            tvName = itemView.findViewById(R.id.tvClothName);
            tvDate = itemView.findViewById(R.id.tvClothDate);
        }
    }
}
