package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Channel;
import com.crowdquery.crowdquery.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByChannel(Channel channel);
}
