package com.kmsa.content.repository;

import com.kmsa.content.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findTopByOrderByIdAsc();
}
