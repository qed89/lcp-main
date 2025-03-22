package com.lcp.model;

public class Element {
    private String id;
    private String name;
    private String htmlCode;
    private String cssCode;
    private String label;
    private Long userId;

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHtmlCode() { return htmlCode; }
    public void setHtmlCode(String htmlCode) { this.htmlCode = htmlCode; }

    public String getCssCode() { return cssCode; }
    public void setCssCode(String cssCode) { this.cssCode = cssCode; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Long getUser() { return userId; }
    public void setUser(Long userId) { this.userId = userId; }
}