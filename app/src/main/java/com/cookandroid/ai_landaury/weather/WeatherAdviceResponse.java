package com.cookandroid.ai_landaury.weather;

import java.util.List;

public class WeatherAdviceResponse {
    private double temperature;
    private int humidity;
    private int rainProbability;
    private int sky;
    private int rainType;
    private double windSpeed;
    private Advice advice;

    public double getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public int getRainProbability() { return rainProbability; }
    public int getSky() { return sky; }
    public int getRainType() { return rainType; }
    public double getWindSpeed() { return windSpeed; }
    public Advice getAdvice() { return advice; }

    // 내부 클래스: advice 필드 구조
    public static class Advice {
        private String summary;
        private List<String> adviceList;

        public String getSummary() { return summary; }
        public List<String> getAdviceList() { return adviceList; }
    }
}