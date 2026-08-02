package com.kmsa.content.repository;

import com.kmsa.content.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findAllByOrderByDisplayOrderAsc();
}
