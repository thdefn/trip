package com.trip.diary.event.dto;

import com.trip.diary.domain.model.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRegisterEvent {
    private Member member;
}
