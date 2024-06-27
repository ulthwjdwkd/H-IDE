package org.example.backend.codeeditor.repository;

import org.example.backend.codeeditor.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT f FROM FileEntity f WHERE f.user.usersId = :usersId")
    List<FileEntity> findByUsersId(@Param("usersId") int usersId);

    @Query("SELECT f FROM FileEntity f WHERE f.id = :id AND f.user.usersId = :usersId")
    Optional<FileEntity> findByIdAndUsersId(@Param("id") Long id, @Param("usersId") int usersId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FileEntity f WHERE f.name = :name AND f.fileType = :fileType AND f.user.usersId = :usersId")
    boolean existsByNameAndFileTypeAndUsersId(@Param("name") String name, @Param("fileType") String fileType, @Param("usersId") int usersId);
}
