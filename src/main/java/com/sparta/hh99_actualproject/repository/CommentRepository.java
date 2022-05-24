package com.sparta.hh99_actualproject.repository;

import com.sparta.hh99_actualproject.model.Board;
import com.sparta.hh99_actualproject.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    Page<Comment> findAllByBoardOrderByCreatedAtDesc(Board board , Pageable pageable);
    List<Comment> findAllByBoard(Board board);

    @Query(value = "SELECT count(board_id) " +
            "FROM comment " +
            "where board_id=:boardPostId"
            , nativeQuery = true)
    Integer countByBoard(Long boardPostId);
}
