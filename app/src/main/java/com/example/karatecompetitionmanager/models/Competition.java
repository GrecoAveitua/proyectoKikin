package com.example.karatecompetitionmanager.models;

public class Competition {
    private int id;
    private String categoryFolio;
    private String date;
    private String status; // "IN_PROGRESS", "COMPLETED"
    private String firstPlace;
    private String secondPlace;
    private String thirdPlace;

    public Competition(int id, String categoryFolio, String date, String status,
                       String firstPlace, String secondPlace, String thirdPlace) {
        this.id = id;
        this.categoryFolio = categoryFolio;
        this.date = date;
        this.status = status;
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
        this.thirdPlace = thirdPlace;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategoryFolio() { return categoryFolio; }
    public void setCategoryFolio(String categoryFolio) {
        this.categoryFolio = categoryFolio;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFirstPlace() { return firstPlace; }
    public void setFirstPlace(String firstPlace) { this.firstPlace = firstPlace; }

    public String getSecondPlace() { return secondPlace; }
    public void setSecondPlace(String secondPlace) { this.secondPlace = secondPlace; }

    public String getThirdPlace() { return thirdPlace; }
    public void setThirdPlace(String thirdPlace) { this.thirdPlace = thirdPlace; }
}
