package com.example.karatecompetitionmanager.models;

public class Category {
    private String folio;
    private String belt;
    private int minAge;
    private int maxAge;
    private String type; // "kata", "kumite", "both"

    public Category(String folio, String belt, int minAge, int maxAge, String type) {
        this.folio = folio;
        this.belt = belt;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.type = type;
    }

    public static String generateFolio(String belt, int minAge, int maxAge, String type) {
        String beltCode = belt.substring(0, Math.min(2, belt.length())).toUpperCase();
        String typeCode = "";
        if (type.equals("kata")) typeCode = "KA";
        else if (type.equals("kumite")) typeCode = "KU";
        else typeCode = "BOTH";

        return beltCode + "-" + typeCode + "-" + minAge + "A" + maxAge;
    }

    // Getters y Setters
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public String getBelt() { return belt; }
    public void setBelt(String belt) { this.belt = belt; }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
