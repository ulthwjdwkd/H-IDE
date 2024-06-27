package org.example.backend.codeeditor.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class FileDto {

    private Long id; // 기본키 id

    @JsonProperty("file_name")
    private String name; // 파일 이름

    private String content; // 파일 내용

    @JsonProperty("file_type")
    private String fileType; // 파일 확장자

    private LocalDateTime createdAt; // 파일 생성 시간
    private LocalDateTime updatedAt; // 마지막으로 수정된 시간

    @JsonProperty("users_id")
    private int usersId; // 사용자 ID (기본키 식별자)

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUsersId() {
        return usersId;
    }

    public void setUsersId(int usersId) {
        this.usersId = usersId;
    }
}
