package com.emarketplace.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
class ProfileImage {
    @Id private String username;
    @Lob private byte[] image;
    protected ProfileImage() { }
    ProfileImage(String username, byte[] image) { this.username = username; this.image = image; }
    byte[] getImage() { return image; }
    void setImage(byte[] image) { this.image = image; }
}
