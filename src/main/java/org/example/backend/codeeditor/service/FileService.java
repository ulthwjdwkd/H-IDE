package org.example.backend.codeeditor.service;


import org.example.backend.codeeditor.dto.FileDto;
import org.example.backend.codeeditor.entity.FileEntity;
import org.example.backend.codeeditor.exception.ResourceNotFoundException;
import org.example.backend.codeeditor.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.backend.user.entity.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    // 파일 생성
    public FileDto createFile(String name, String content, String fileType, User user) {
        FileEntity file = new FileEntity(name, content, fileType, user);
        FileEntity savedFile = fileRepository.save(file);
        return toDto(savedFile);
    }

    // 파일 id와 사용자 id로 파일 찾기 (Dto)
    public Optional<FileDto> getFileByIdAndUsersId(Long fileId, int usersId) {
        return fileRepository.findByIdAndUsersId(fileId, usersId)
                .map(this::toDto);
    }

    // 파일 id와 사용자 id로 파일 찾기 (Entity)
    public Optional<FileEntity> getFileEntityByIdAndUsersId(Long fileId, int usersId) {
        return fileRepository.findByIdAndUsersId(fileId, usersId);
    }

    // 사용자 id로 파일 찾기
    public List<FileDto> getFilesByUsersId(int usersId) {
        return fileRepository.findByUsersId(usersId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 사용자 내 같은 이름의 파일 존재 여부 확인
    public boolean isFileExists(String name, String fileType, int usersId) {
        return fileRepository.existsByNameAndFileTypeAndUsersId(name, fileType, usersId);
    }

    // 파일 id로 파일 삭제
    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    // 파일 저장 (업데이트)
    public FileDto saveFile(FileEntity fileEntity) {
        FileEntity updatedFile = fileRepository.save(fileEntity);
        return toDto(updatedFile);
    }

    // 파일 실행
    public String executeFile(Long fileId, int usersId) throws Exception {
        // 파일 ID와 사용자 ID로 파일을 찾음
        Optional<FileEntity> fileEntityOptional = getFileEntityByIdAndUsersId(fileId, usersId);

        // 파일이 존재하지 않으면 예외를 발생시킴
        if (!fileEntityOptional.isPresent()) {
            throw new ResourceNotFoundException("File not found with id: " + fileId + " for user with id: " + usersId);
        }

        // 파일 정보를 가져옴
        FileEntity fileEntity = fileEntityOptional.get();
        String fileName = fileEntity.getName();
        String fileType = fileEntity.getFileType(); // 파일 확장자
        String content = fileEntity.getContent();   // 파일 내용

        // 모든 파일 이름에 확장자가 없다고 가정하고 파일 타입에 따라 확장자를 추가
        if (fileType == null || fileType.isEmpty()) {
            throw new UnsupportedOperationException("File type is missing");
        }

        switch (fileType) {
            case "java":
                fileName += ".java";
                break;
            case "py":
                fileName += ".py";
                break;
            case "js":
                fileName += ".js";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }

        // 임시 디렉토리 생성 (JVM 종료 시 자동 삭제)
        File tempDir = Files.createTempDirectory("codeexecutor").toFile();
        tempDir.deleteOnExit();

        // 임시 파일 생성 (JVM 종료 시 자동 삭제)
        File tempFile = new File(tempDir, fileName);
        Files.write(tempFile.toPath(), content.getBytes());
        tempFile.deleteOnExit();

        // 파일 타입에 따라 실행할 명령어를 결정
        String command;
        Process process;
        switch (fileType) {
            case "java":
                // Java 파일의 경우, 컴파일
                String className = fileName.replace(".java", "");
                command = "javac -d " + tempDir.getAbsolutePath() + " " + tempFile.getAbsolutePath();
                process = Runtime.getRuntime().exec(command);
                process.waitFor();

                // 컴파일된 Java 파일 실행
                command = "java -cp " + tempDir.getAbsolutePath() + " " + className;
                process = Runtime.getRuntime().exec(command);
                break;
            case "py":
                // Python 파일의 경우, 직접 실행
                command = "python3 " + tempFile.getAbsolutePath();
                process = Runtime.getRuntime().exec(command);
                break;
            case "js":
                // JavaScript 파일의 경우, Node.js로 실행
                command = "node " + tempFile.getAbsolutePath();
                process = Runtime.getRuntime().exec(command);
                break;
            default:
                // 지원되지 않는 파일 타입의 경우 예외 발생
                throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }

        // 프로세스의 표준 출력을 읽음
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // 프로세스의 오류 출력을 읽음
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder output = new StringBuilder();
        String line;

        // 표준 출력을 읽어서 StringBuilder에 추가
        while ((line = stdInput.readLine()) != null) {
            output.append(line).append("\n");
        }

        // 오류 출력을 읽어서 StringBuilder에 추가
        while ((line = stdError.readLine()) != null) {
            output.append(line).append("\n");
        }

        process.waitFor();

        // 임시 파일과 디렉토리를 삭제 (JVM 종료 시 자동 삭제가 설정되어 있으므로 생략 가능)
        tempFile.delete();
        tempDir.delete();

        // 실행 결과를 반환
        return output.toString();
    }

    // FileEntity를 FileDto로 변환
    private FileDto toDto(FileEntity fileEntity) {
        FileDto fileDto = new FileDto();
        fileDto.setId(fileEntity.getId());
        fileDto.setName(fileEntity.getName());
        fileDto.setContent(fileEntity.getContent());
        fileDto.setFileType(fileEntity.getFileType());
        fileDto.setCreatedAt(fileEntity.getCreatedAt());
        fileDto.setUpdatedAt(fileEntity.getUpdatedAt());
        fileDto.setUsersId(fileEntity.getUser().getUsersId()); // 변경
        return fileDto;
    }
}
