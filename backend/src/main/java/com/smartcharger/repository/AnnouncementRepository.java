package com.smartcharger.repository;

import com.smartcharger.entity.Announcement;
import com.smartcharger.entity.enums.AnnouncementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告数据访问接口
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * 管理端查询：根据状态和关键词查询
     */
    @Query("SELECT a FROM Announcement a WHERE " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:keyword IS NULL OR a.title LIKE %:keyword%)")
    Page<Announcement> findByStatusAndKeyword(@Param("status") AnnouncementStatus status,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);

    /**
     * 车主端查询：查询已发布且在有效期内的公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' AND " +
            "(a.startTime IS NULL OR a.startTime <= :currentTime) AND " +
            "(a.endTime IS NULL OR a.endTime >= :currentTime)")
    Page<Announcement> findPublishedAndValid(@Param("currentTime") LocalDateTime currentTime,
                                              Pageable pageable);

    /**
     * 车主端查询：查询最新的N条已发布且在有效期内的公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' AND " +
            "(a.startTime IS NULL OR a.startTime <= :currentTime) AND " +
            "(a.endTime IS NULL OR a.endTime >= :currentTime) " +
            "ORDER BY a.createdTime DESC")
    List<Announcement> findLatestPublishedAndValid(@Param("currentTime") LocalDateTime currentTime,
                                                     Pageable pageable);
}
