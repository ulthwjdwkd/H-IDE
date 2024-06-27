package org.example.backend.codeeditor.controller;


import org.example.backend.codeeditor.dto.FileDto;
import org.example.backend.codeeditor.entity.FileEntity;
import org.example.backend.codeeditor.exception.BadRequestException;
import org.example.backend.codeeditor.exception.ConflictException;
import org.example.backend.codeeditor.exception.ResourceNotFoundException;
import org.example.backend.codeeditor.service.FileService;
import org.example.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.backend.user.entity.User;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/projects/{usersId}/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<FileDto> createFile(@PathVariable("usersId") int usersId, @RequestParam("file_name") String name, @RequestParam("file_type") String fileType) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("File name cannot be null or empty"); // 파일 이름 없음
        }
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new BadRequestException("File type cannot be null or empty"); // 파일 확장자(타입) 없음
        }

        Optional<User> userOptional = userService.getUserByUsersId(usersId);
        if (userOptional.isPresent()) {
            if (fileService.isFileExists(name, fileType, usersId)) {
                throw new ConflictException("A file with the same name and type already exists."); // 같은 이름과 타입의 파일 존재
            }
            FileDto file = fileService.createFile(name, "", fileType, userOptional.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(file);
        } else {
            throw new ResourceNotFoundException("User not found with id: " + usersId); // 해당하는 사용자 ID 없음
        }
    }

    // 파일 ID와 사용자 ID로 조회
    @GetMapping("/{fileId}")
    public ResponseEntity<FileDto> getFileByIdAndUsersId(@PathVariable("usersId") int usersId, @PathVariable("fileId") Long fileId) {
        if (!userService.getUserByUsersId(usersId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + usersId); // 해당하는 사용자 id 없음
        }

        Optional<FileDto> file = fileService.getFileByIdAndUsersId(fileId, usersId);
        if (!file.isPresent()) {
            throw new ResourceNotFoundException("File not found with id: " + fileId + " for user with id: " + usersId); // 사용자 내에 해당하는 파일 id 없음
        }

        return ResponseEntity.ok(file.get());
    }

    // 사용자 ID로 파일 목록 조회
    @GetMapping
    public ResponseEntity<List<FileDto>> getFilesByUsersId(@PathVariable("usersId") int usersId) {
        List<FileDto> files = fileService.getFilesByUsersId(usersId);
        return ResponseEntity.ok(files);
    }

    // 파일 ID와 사용자 ID로 파일 업데이트
    @PutMapping("/{fileId}")
    public ResponseEntity<FileDto> updateFile(@PathVariable("usersId") int usersId, @PathVariable("fileId") Long fileId, @RequestBody FileDto fileDto) {
        if (!userService.getUserByUsersId(usersId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + usersId); // 사용자 id 일치하지 않을 시 예외처리
        }

        Optional<FileEntity> fileEntityOptional = fileService.getFileEntityByIdAndUsersId(fileId, usersId);
        if (!fileEntityOptional.isPresent()) {
            throw new ResourceNotFoundException("File not found with id: " + fileId + " for user with id: " + usersId); // 해당 사용자 내에 파일 id 없을 시 예외처리
        }

        FileEntity fileEntity = fileEntityOptional.get();

        // 파일 이름이나 확장자가 업데이트되려는 경우 같은 사용자 내에서 " 파일 이름 + 확장자 " 가 같을 경우 예외처리
        boolean isNameChanged = fileDto.getName() != null && !fileDto.getName().trim().isEmpty() && !fileEntity.getName().equals(fileDto.getName());
        boolean isFileTypeChanged = fileDto.getFileType() != null && !fileDto.getFileType().trim().isEmpty() && !fileEntity.getFileType().equals(fileDto.getFileType());

        if (isNameChanged || isFileTypeChanged) {
            String newName = isNameChanged ? fileDto.getName() : fileEntity.getName();
            String newFileType = isFileTypeChanged ? fileDto.getFileType() : fileEntity.getFileType();

            if (fileService.isFileExists(newName, newFileType, usersId)) {
                throw new ConflictException("A file with the same name and type already exists."); // 같은 이름과 타입의 파일이 이미 존재할 경우 예외 처리
            }

            if (isNameChanged) {
                fileEntity.setName(fileDto.getName());
            }
            if (isFileTypeChanged) {
                fileEntity.setFileType(fileDto.getFileType());
            }
        }

        // 파일 내용 업데이트
        if (fileDto.getContent() != null) {
            fileEntity.setContent(fileDto.getContent());
        }

        FileDto updatedFile = fileService.saveFile(fileEntity);
        return ResponseEntity.ok(updatedFile);
    }

    // 파일 삭제
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable("usersId") int usersId, @PathVariable("fileId") Long fileId) {
        if (!userService.getUserByUsersId(usersId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + usersId); //사용자 id 존재 안함
        }

        Optional<FileDto> file = fileService.getFileByIdAndUsersId(fileId, usersId);
        if (!file.isPresent()) {
            throw new ResourceNotFoundException("File not found with id: " + fileId + " for user with id: " + usersId); // 사용자 내에 해당하는 파일id 없음
        }

        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    // 파일 실행
    @PostMapping("/{fileId}/run")
    public ResponseEntity<String> executeFile(@PathVariable("usersId") int usersId, @PathVariable("fileId") Long fileId) {
        try {
            String result = fileService.executeFile(fileId, usersId);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error executing file: " + e.getMessage());
        }
    }
}
