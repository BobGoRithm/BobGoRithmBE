package com.sparta.hh99_actualproject.repository;

import com.sparta.hh99_actualproject.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Integer countByMemberIdAndIsRead(String memberId, boolean isRead);
    List<Notification> findAllByMemberIdAndIsRead(String memberId, boolean isRead);
    List<Notification> findAllByMemberId(String memberId);
}
