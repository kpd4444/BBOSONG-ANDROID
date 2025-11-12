package com.cookandroid.ai_landaury.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookandroid.ai_landaury.R;
import com.cookandroid.ai_landaury.kakaomap.LaundryMapActivity;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // 1) 지도 이미지 뷰 참조
        ImageView mapPreview = v.findViewById(R.id.iv_map_preview);

        // 2) 클릭 시 지도 액티비티 실행
        mapPreview.setOnClickListener(view -> {
            Intent i = new Intent(requireActivity(),
                    LaundryMapActivity.class);
            startActivity(i);
        });

        return v;
    }
}
