package com.ista.springboot.web.app.dto;

public class UsuarioCercanoDTO {

    private Long id;
    private String name;
    private Double lat;
    private Double lng;
    private String avatarUrl;

    public UsuarioCercanoDTO() {}

    public UsuarioCercanoDTO(Long id, String name, Double lat, Double lng, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
