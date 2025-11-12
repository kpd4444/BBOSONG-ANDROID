package com.cookandroid.ai_landaury.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cookandroid.ai_landaury.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RecentDetailBottomSheet extends BottomSheetDialogFragment {

    public static RecentDetailBottomSheet newInstance(String name, String date, String imageUri, int imgResId) {
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("date", date);
        args.putString("imageUri", imageUri);
        args.putInt("imgResId", imgResId);

        RecentDetailBottomSheet fragment = new RecentDetailBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_recent_detail_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ImageView img = v.findViewById(R.id.imgDetail);
        TextView tvName = v.findViewById(R.id.tvDetailName);
        TextView tvDate = v.findViewById(R.id.tvDetailDate);

        Bundle args = getArguments();
        if (args == null) return;

        String name = args.getString("name", "의류");
        String date = args.getString("date", "-");
        String imageUri = args.getString("imageUri", "");
        int imgResId = args.getInt("imgResId", R.drawable.ic_clothes_placeholder);

        tvName.setText(name);
        tvDate.setText(date);

        if (imageUri != null && !imageUri.isEmpty()) {
            img.setImageURI(Uri.parse(imageUri));
        } else {
            img.setImageResource(imgResId);
        }
    }
}
