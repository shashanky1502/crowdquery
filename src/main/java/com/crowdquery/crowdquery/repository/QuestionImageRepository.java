package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.QuestionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionImageRepository extends JpaRepository<QuestionImage, UUID> {

    // Find image URLs by question ID in order
    @Query("SELECT qi.imageUrl FROM QuestionImage qi WHERE qi.question.id = :questionId ORDER BY qi.imageOrder ASC")
    List<String> findImageUrlsByQuestionId(@Param("questionId") UUID questionId);

    // Find images by question ID
    List<QuestionImage> findByQuestionIdOrderByImageOrder(UUID questionId);

    // Count images by question
    long countByQuestionId(UUID questionId);

    // Delete by question ID
    void deleteByQuestionId(UUID questionId);
}