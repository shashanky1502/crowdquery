package com.crowdquery.crowdquery.controller;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.QuestionDto.QuestionRequestDto;
import com.crowdquery.crowdquery.dto.QuestionDto.QuestionResponseDto;
import com.crowdquery.crowdquery.service.QuestionService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // Single endpoint - handles both text-only and text+images
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionResponseDto> createQuestion(
            @RequestParam("text") String text,
            @RequestParam("channelCode") String channelCode,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        QuestionRequestDto request = new QuestionRequestDto();
        request.setText(text);
        request.setChannelCode(channelCode);
        request.setImages(images);
        
        return ResponseEntity.ok(questionService.createQuestionWithImages(request));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<QuestionResponseDto>> getQuestions(
            @RequestParam String channelCode,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(questionService.getQuestionsByChannel(channelCode, limit, offset, sortBy, sortDir));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDto> getQuestion(@PathVariable String questionId) {
        return ResponseEntity.ok(questionService.getQuestion(questionId));
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable String questionId,
            @RequestBody QuestionRequestDto request) {
        return ResponseEntity.ok(questionService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}