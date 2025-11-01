package com.cookandroid.ai_landaury.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class LaundryAdviceResponse implements Serializable {

    @SerializedName("소재")
    private String material;

    @SerializedName("색상")
    private String color;

    @SerializedName("세탁방법")
    private String washingMethod;

    @SerializedName("주의사항")
    private String cautions;

    @SerializedName("권장심볼")
    private List<String> recommendedSymbols;

    public String getMaterial() { return material; }
    public String getColor() { return color; }
    public String getWashingMethod() { return washingMethod; }
    public String getCautions() { return cautions; }
    public List<String> getRecommendedSymbols() { return recommendedSymbols; }
}
