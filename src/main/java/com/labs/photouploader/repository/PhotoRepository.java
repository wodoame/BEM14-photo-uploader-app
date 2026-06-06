package com.labs.photouploader.repository;

import com.labs.photouploader.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findAllByOrderByCreatedAtDesc();
}
