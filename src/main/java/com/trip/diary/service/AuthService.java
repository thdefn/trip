package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.dto.SignInForm;
import com.trip.diary.dto.SignUpForm;
import com.trip.diary.dto.TokenDto;
import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.event.dto.MemberRegisterEvent;
import com.trip.diary.exception.CustomException;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.trip.diary.exception.ErrorCode.ID_ALREADY_USED;
import static com.trip.diary.exception.ErrorCode.MOBILE_ALREADY_REGISTERED;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;

    private final TokenProvider tokenProvider;

    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void register(SignUpForm form) {
        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new CustomException(ID_ALREADY_USED);
        }

        if (memberRepository.existsByPhone(form.getPhone())) {
            throw new CustomException(MOBILE_ALREADY_REGISTERED);
        }

        form.setPassword(passwordEncoder.encode(form.getPassword()));

        Member savedMember = memberRepository.save(
                Member.builder()
                        .username(form.getUsername())
                        .password(form.getPassword())
                        .phone(form.getPhone())
                        .nickname(form.getNickname())
                        .build()
        );

        applicationEventPublisher.publishEvent(new MemberRegisterEvent(savedMember));
    }

    public TokenDto authenticate(SignInForm form) {
        Member member = memberRepository.findByUsername(form.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (!passwordEncoder.matches(form.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_UNMATCHED);
        }

        return new TokenDto(tokenProvider.generateToken(form.getUsername()));
    }
}
