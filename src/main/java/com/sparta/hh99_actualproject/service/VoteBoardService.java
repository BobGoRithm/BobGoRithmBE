package com.sparta.hh99_actualproject.service;

import com.sparta.hh99_actualproject.dto.VoteBoardRequestDto;
import com.sparta.hh99_actualproject.dto.VoteBoardResponseDto;
import com.sparta.hh99_actualproject.dto.VoteContentResponseDto;
import com.sparta.hh99_actualproject.exception.PrivateException;
import com.sparta.hh99_actualproject.exception.StatusCode;
import com.sparta.hh99_actualproject.model.Member;
import com.sparta.hh99_actualproject.model.Selection;
import com.sparta.hh99_actualproject.model.VoteBoard;
import com.sparta.hh99_actualproject.model.VoteContent;
import com.sparta.hh99_actualproject.repository.MemberRepository;
import com.sparta.hh99_actualproject.repository.VoteBoardRepository;
import com.sparta.hh99_actualproject.repository.VoteContentRepository;
import com.sparta.hh99_actualproject.service.validator.Validator;
import com.sparta.hh99_actualproject.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@AllArgsConstructor
@Service
public class VoteBoardService {
    private final Validator validator;
    private final VoteBoardRepository voteBoardRepository;
    private final VoteContentRepository voteContentRepository;
    private final MemberRepository memberRepository;
    private final SelectionService selectionService;


    @Transactional
    public VoteBoardResponseDto createVoteBoard(VoteBoardRequestDto requestDto, String imgLeftFilePath, String imgRightFilePath) {
        //null Check
        if (validator.hasNullDtoField(requestDto)){
            throw new PrivateException(StatusCode.NULL_INPUT_ERROR);
        }
        
        //Member 가져오기
        String memberId = SecurityUtil.getCurrentMemberId();
        Member findedMember = memberRepository.findByMemberId(memberId)
                .orElseThrow(()-> new PrivateException(StatusCode.NOT_FOUND_MEMBER));
        
        //VoteBoard 제작하기
        VoteBoard savedVoteBoard = voteBoardRepository.save(VoteBoard.of(findedMember,requestDto));
        
        //Dto를 VoteContents Model 로 변경
        VoteContent leftVoteContent = VoteContent.builder()
                .voteBoard(savedVoteBoard)
                .imageUrl(imgLeftFilePath)
                .imageTitle(requestDto.getImgLeftTitle())
                .build();
        leftVoteContent = voteContentRepository.save(leftVoteContent);

        VoteContent rightVoteContent = VoteContent.builder()
                .voteBoard(savedVoteBoard)
                .imageUrl(imgRightFilePath)
                .imageTitle(requestDto.getImgRightTitle())
                .build();
        rightVoteContent = voteContentRepository.save(rightVoteContent);

        //VoteContents 를 만들어서 VoteBoard에 할당 변경
        List<VoteContent> voteContentList = Arrays.asList(leftVoteContent,rightVoteContent);

        savedVoteBoard.setVoteContentList(voteContentList);

        //VoteBoardResponseDto 를 만들기 위해서 List<VoteContentResponseDto>를 만들자
        List<VoteContentResponseDto> voteContentResponseDtoList = Arrays.asList(VoteContentResponseDto.of(leftVoteContent), VoteContentResponseDto.of(rightVoteContent));

        //VoteBoardResponseDto return 해주기
        return VoteBoardResponseDto.builder()
                .postId(savedVoteBoard.getVoteBoardId())
                .memberId(memberId)
                .vote(voteContentResponseDtoList)
                .createdAt(savedVoteBoard.getCreatedAt())
                .title(savedVoteBoard.getTitle())
                .contents(savedVoteBoard.getContents())
                .build();
    }

    //TODO
    //목표 Selection에서 MemberId 의 String을 빼내기. 그리고 그 List를 SelectionList에 붙이기 && selected 여부 확인하기. ==> Stream 으로 만들기
    public VoteBoardResponseDto getVoteBoard(Long postId) {
        //VoteBoard 가져오기
        VoteBoard findedVoteBoard = voteBoardRepository.findById(postId)
                .orElseThrow(() -> new PrivateException(StatusCode.NOT_FOUND_POST));

        //VoteContent 가져오기
        List<VoteContent> voteContentList = findedVoteBoard.getVoteContentList();
        List<VoteContentResponseDto> voteContentResponseDtoList = null;
        if(!voteContentList.isEmpty()){
            VoteContent leftVoteContent = voteContentList.get(0);
            List<String> leftVoteContentMemberIdList = getMemberIdListInVoteContent(leftVoteContent);   //Selection List 확인
            boolean selected = leftVoteContentMemberIdList.contains(SecurityUtil.getCurrentMemberId()); //Selected 확인
            leftVoteContent.setSelected(selected);

            VoteContent rightVoteContent = voteContentList.get(1);
            List<String> rightVoteContentMemberIdList = getMemberIdListInVoteContent(rightVoteContent); //Selection List 확인
            selected = rightVoteContentMemberIdList.contains(SecurityUtil.getCurrentMemberId()); //Selected 확인
            rightVoteContent.setSelected(selected);

            voteContentResponseDtoList = Arrays.asList(VoteContentResponseDto.of(leftVoteContent,leftVoteContentMemberIdList), VoteContentResponseDto.of(rightVoteContent,rightVoteContentMemberIdList));
        }

        return VoteBoardResponseDto.builder()
                .postId(findedVoteBoard.getVoteBoardId())
                .memberId(findedVoteBoard.getMember().getMemberId())
                .vote(voteContentResponseDtoList)
                .createdAt(findedVoteBoard.getCreatedAt())
                .title(findedVoteBoard.getTitle())
                .contents(findedVoteBoard.getContents())
                .build();
    }

    private List<String> getMemberIdListInVoteContent(VoteContent voteContent) {
        List<Selection> voteContentSelections = selectionService.getSelectionList(voteContent);
        voteContent.setSelectionList(voteContentSelections);
        List<String> memberIdList = new ArrayList<String>();
        for (Selection selection : voteContentSelections) {
            memberIdList.add(selection.getMemberId());
        }

        return memberIdList;
    }
}
