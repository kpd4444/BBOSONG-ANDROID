package com.cookandroid.ai_landaury.home;

public class RecentResultItem {
    private String name;
    private String date;
    private String imageUri;  // 사진 경로 (String 형태)
    private int imgResId;     // placeholder용

    public RecentResultItem(String name, String date, String imageUri, int imgResId) {
        this.name = name;
        this.date = date;
        this.imageUri = imageUri;
        this.imgResId = imgResId;
    }

    public String getName() { return name; }
    public String getDate() { return date; }
    public String getImageUri() { return imageUri; }
    public int getImgResId() { return imgResId; }
}