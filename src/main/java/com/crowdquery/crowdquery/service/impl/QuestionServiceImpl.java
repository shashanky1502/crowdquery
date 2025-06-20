package com.crowdquery.crowdquery.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.crowdquery.crowdquery.dto.QuestionRequestDto;
import com.crowdquery.crowdquery.dto.QuestionResponseDto;
import com.crowdquery.crowdquery.model.Question;
import com.crowdquery.crowdquery.repository.QuestionRepository;
import com.crowdquery.crowdquery.service.QuestionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService{
    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;

    @Override
    public QuestionResponseDto createQuestion(QuestionRequestDto req) {
        Question question = modelMapper.map(req, Question.class);
        Question savedQuestion = questionRepository.save(question);
        return modelMapper.map(savedQuestion, QuestionResponseDto.class);
    }
}
