package com.example.karatecompetitionmanager.models;

public class Competitor {
    private String folio;
    private String name;
    private String dojo;
    private int age;
    private String belt;
    private boolean participateKata;
    private boolean participateKumite;

    public Competitor(String folio, String name, String dojo, int age, String belt,
                      boolean participateKata, boolean participateKumite) {
        this.folio = folio;
        this.name = name;
        this.dojo = dojo;
        this.age = age;
        this.belt = belt;
        this.participateKata = participateKata;
        this.participateKumite = participateKumite;
    }

    public static String generateFolio(String name, String belt, int age, String type) {
        String namePrefix = name.length() >= 3 ?
                name.substring(0, 3).toUpperCase() : name.toUpperCase();
        String beltCode = belt.substring(0, Math.min(2, belt.length())).toUpperCase();
        return beltCode + "-" + namePrefix + "-" + age + "-" + type;
    }

    // Getters y Setters
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDojo() { return dojo; }
    public void setDojo(String dojo) { this.dojo = dojo; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBelt() { return belt; }
    public void setBelt(String belt) { this.belt = belt; }

    public boolean isParticipateKata() { return participateKata; }
    public void setParticipateKata(boolean participateKata) {
        this.participateKata = participateKata;
    }

    public boolean isParticipateKumite() { return participateKumite; }
    public void setParticipateKumite(boolean participateKumite) {
        this.participateKumite = participateKumite;
    }
}