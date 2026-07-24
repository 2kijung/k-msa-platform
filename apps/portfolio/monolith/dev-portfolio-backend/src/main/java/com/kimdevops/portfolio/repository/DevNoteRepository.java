package com.kimdevops.portfolio.repository;

import com.kimdevops.portfolio.entity.DevNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevNoteRepository extends JpaRepository<DevNote, Long> {
    List<DevNote> findAllByOrderByDisplayOrderAscIdAsc();
}
