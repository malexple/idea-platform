package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    Optional<SystemSetting> findByKey(String key);

    List<SystemSetting> findByGroupOrderByKey(String group);

    List<SystemSetting> findByKeyStartingWith(String prefix);

    boolean existsByKey(String key);
}
