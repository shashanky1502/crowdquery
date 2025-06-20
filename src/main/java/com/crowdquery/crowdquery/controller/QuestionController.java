package com.crowdquery.crowdquery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crowdquery.crowdquery.dto.QuestionRequestDto;
import com.crowdquery.crowdquery.dto.QuestionResponseDto;
import com.crowdquery.crowdquery.service.QuestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponseDto> create(@RequestBody QuestionRequestDto req) {
        QuestionResponseDto question = questionService.createQuestion(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }
}
