package com.lcp.model;

public class Form {
    private String id;
    private String name;
    private String createdDate;
    private Long userId;

    // Геттеры и сеттеры

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public Long getUser() { return userId; }
    public void setUser(Long userId) { this.userId = userId; }
}