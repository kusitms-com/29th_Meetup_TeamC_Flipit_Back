package com.web.baebaeBE.presentation.answer;

import com.google.firebase.database.annotations.NotNull;
import com.web.baebaeBE.application.answer.AnswerApplication;
import com.web.baebaeBE.presentation.answer.api.AnswerApi;
import com.web.baebaeBE.presentation.answer.dto.AnswerCreateRequest;
import com.web.baebaeBE.presentation.answer.dto.AnswerDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/answers")
public class AnswerController implements AnswerApi {
    private final AnswerApplication answerApplication;

    @PostMapping(value = "/member/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerDetailResponse> createAnswer(@PathVariable Long memberId,
                                                             @RequestPart(value = "imageFiles") List<MultipartFile> imageFiles,
                                                             @RequestPart(value = "audioFile") MultipartFile audioFile,
                                                             @RequestPart AnswerCreateRequest request) {
        AnswerDetailResponse createdAnswer = answerApplication.createAnswer(request, memberId, imageFiles, audioFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
    }

    @GetMapping(value = "/{answerId}")
    public ResponseEntity<Page<AnswerDetailResponse>> getAllAnswers(@RequestParam Long memberId, Pageable pageable) {
        Page<AnswerDetailResponse> answers = answerApplication.getAllAnswers(memberId, pageable);
        return ResponseEntity.ok(answers);
    }

    @PutMapping(value = "/{answerId}", consumes = "multipart/form-data")
    public ResponseEntity<AnswerDetailResponse> updateAnswer(
            @PathVariable Long answerId,
            @ModelAttribute AnswerCreateRequest request,
            @RequestParam("imageFiles") MultipartFile[] imageFiles,
            @RequestParam("audioFile") MultipartFile audioFile) {
        AnswerDetailResponse updatedAnswer = answerApplication.updateAnswer(answerId, request, imageFiles, audioFile);
        return ResponseEntity.ok(updatedAnswer);
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer( @PathVariable Long answerId) {
        answerApplication.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "반응 알림")
    @PatchMapping("/{answerId}/react")
    public ResponseEntity<Void> updateAnswerReactions(
            @PathVariable Long answerId,
            @RequestParam int heartCount,
            @RequestParam int curiousCount,
            @RequestParam int sadCount
    ) {
        answerApplication.updateReactionCounts(answerId, heartCount, curiousCount, sadCount);
        return ResponseEntity.ok().build();
    }
}