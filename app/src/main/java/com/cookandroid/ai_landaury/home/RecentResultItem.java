package com.cookandroid.ai_landaury.home;

public class RecentResultItem {
    private String name;
    private String date;
    private String imageUri;
    private int imgResId;

    // 상세 정보를 위한 필드 추가
    private String material;
    private String color;
    private String washingMethod;
    private String cautions;

    // 생성자 업데이트
    public RecentResultItem(String name, String date, String imageUri, int imgResId,
                            String material, String color, String washingMethod, String cautions) {
        this.name = name;
        this.date = date;
        this.imageUri = imageUri;
        this.imgResId = imgResId;
        this.material = material;
        this.color = color;
        this.washingMethod = washingMethod;
        this.cautions = cautions;
    }

    // Getter 메서드들
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getImageUri() { return imageUri; }
    public int getImgResId() { return imgResId; }

    // 추가된 Getter
    public String getMaterial() { return material; }
    public String getColor() { return color; }
    public String getWashingMethod() { return washingMethod; }
    public String getCautions() { return cautions; }
}