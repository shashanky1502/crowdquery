package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Comment;
import com.crowdquery.crowdquery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByParentContentId(UUID parentContentId);
    List<Comment> findByAuthor(User author);
}
