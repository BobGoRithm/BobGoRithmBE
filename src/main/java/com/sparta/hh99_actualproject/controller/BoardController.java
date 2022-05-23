package com.sparta.hh99_actualproject.controller;

import com.sparta.hh99_actualproject.dto.*;
import com.sparta.hh99_actualproject.exception.PrivateResponseBody;
import com.sparta.hh99_actualproject.exception.StatusCode;
import com.sparta.hh99_actualproject.repository.MemberRepository;
import com.sparta.hh99_actualproject.service.AwsS3Service;
import com.sparta.hh99_actualproject.service.BoardService;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor //초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class BoardController {
    private final BoardService boardService;

    //게시글 전체조회
    @GetMapping("/anonypost")
    public ResponseEntity<PrivateResponseBody> getAllBoard(@RequestParam(value = "page") Integer page,
                                                           @RequestParam(value = "category" , required = false) String category ) {
        return new ResponseEntity<>(new PrivateResponseBody(StatusCode.OK, boardService.getAllBoard(page,category)), HttpStatus.OK);

    }

    //게시글 상세 조회
    @ApiResponse(code = 200, message = "응답 실패", response = BoardResponseDto.DetailResponse.class)
    @GetMapping("/anonypost/board/{boardPostId}")
    public ResponseEntity<PrivateResponseBody> getBoardDetails(@PathVariable(value = "boardPostId") Long boardPostId){
        return new ResponseEntity<>(new PrivateResponseBody(StatusCode.OK, boardService.getBoardDetails(boardPostId)), HttpStatus.OK);
    }

    //게시글 작성 2205071800 변경
    @PostMapping("/anonypost/board")
    public ResponseEntity<PrivateResponseBody> createBoard(@ModelAttribute BoardRequestDto.SaveRequest boardRequestDto) { 
        BoardResponseDto.DetailResponse detailResponse = boardService.createBoard(boardRequestDto);

        return new ResponseEntity<>(new PrivateResponseBody(StatusCode.OK, detailResponse), HttpStatus.OK);
    }

    //게시글 수정 2205071800 변경
    @PutMapping("/anonypost/board/{boardPostId}")
    public ResponseEntity<PrivateResponseBody> updateBoard(@PathVariable("boardPostId") Long boardPostId, @ModelAttribute BoardRequestDto.SaveRequest boardRequestDto){
        //TODO
        // 1. 수정 필요
        BoardResponseDto.DetailResponse detailResponse = boardService.updateBoard(boardPostId, boardRequestDto);
        return new ResponseEntity<>(new PrivateResponseBody(StatusCode.OK,detailResponse), HttpStatus.OK);
    }

    //게시글 삭제
    @DeleteMapping("/anonypost/board/{boardpostId}")
    public ResponseEntity<PrivateResponseBody> deleteBoard(@PathVariable Long boardpostId) {
        boardService.deleteBoard(boardpostId);
        return new ResponseEntity<>(new PrivateResponseBody(StatusCode.OK), HttpStatus.OK);
    }

    @PostMapping("/anonypost/board/{boardPostId}/postLikes")
    public ResponseEntity<PrivateResponseBody> updatePostLikes(@PathVariable("boardPostId") Long boardPostId, @RequestParam boolean likes){
        LikesResponseDto LikesResponseDto = boardService.updatePostLikes(boardPostId, likes);
        return new ResponseEntity<>(new PrivateResponseBody(StatusCode.OK,LikesResponseDto), HttpStatus.OK);
    }
}
