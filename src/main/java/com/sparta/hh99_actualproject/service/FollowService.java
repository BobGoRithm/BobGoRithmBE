package com.sparta.hh99_actualproject.service;

import com.sparta.hh99_actualproject.dto.FollowRequestDto;
import com.sparta.hh99_actualproject.dto.FollowResponseDto;
import com.sparta.hh99_actualproject.exception.PrivateException;
import com.sparta.hh99_actualproject.exception.StatusCode;
import com.sparta.hh99_actualproject.model.Follow;
import com.sparta.hh99_actualproject.model.Member;
import com.sparta.hh99_actualproject.repository.FollowRepository;
import com.sparta.hh99_actualproject.repository.MemberRepository;
import com.sparta.hh99_actualproject.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FollowService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    public FollowResponseDto followMember(String followMemberId, FollowRequestDto followRequestDto) {
        //Follow 하려는 Member가 존재하는지 확인하기
        if (!memberRepository.existsByMemberId(followMemberId)) {
            throw new PrivateException(StatusCode.NOT_FOUND_MEMBER); //FollowMemberId가 존재하지 않음
        }

        //Member 가져오기
        String memberId = SecurityUtil.getCurrentMemberId();
        Member findedMember = memberRepository.findByMemberId(memberId)
                .orElseThrow(()-> new PrivateException(StatusCode.NOT_FOUND_MEMBER)); //JWT 사용자 MemberId가 존재하지 않음

        //Follow Entity에서 중복체크 필요. 이미 되어있으면 처리되면 X
        Follow findedFollow = followRepository.findByMemberAndFollowMemberId(findedMember, followMemberId)
                .orElse(null);

        FollowResponseDto followResponseDto = new FollowResponseDto();

        // 1. Follow = true , findedFollow = 존재   : 아무 처리 X
        // 2. Follow = false , findedFollow = null : 아무 처리 X
        // 3. Follow = true , findedFollow = null  :  추가
        // 4. Follow = false , findedFollow = 존재  :  삭제

        //없으면 추가하기
        if(followRequestDto.isFollow() && findedFollow != null){ //1.
            followResponseDto.setFollow(followRequestDto.isFollow());
        }else if(!followRequestDto.isFollow() && findedFollow == null){ //2.
            followResponseDto.setFollow(followRequestDto.isFollow());
        }else if(followRequestDto.isFollow() && findedFollow == null){ //3.
            //Follow Table 에 추가하기
            followRepository.save(Follow.builder()
                    .member(findedMember)
                    .followMemberId(followMemberId)
                    .build());
            followResponseDto.setFollow(true);
        }else if(!followRequestDto.isFollow() && findedFollow != null){ //4.
            //Follow Table 에서 삭제
            followRepository.deleteById(findedFollow.getFollowId());
            followResponseDto.setFollow(false);
        }

        return followResponseDto;
    }
}