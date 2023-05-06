package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.dto.SignUpForm;
import com.trip.diary.exception.CustomException;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.security.MemberPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.trip.diary.exception.ErrorCode.ID_ALREADY_USED;

@Service
@AllArgsConstructor
public class AuthService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new MemberPrincipal(memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.NOT_FOUND_MEMBER.toString())));
    }

    public void register(SignUpForm form) {
        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new CustomException(ID_ALREADY_USED);
        }

        memberRepository.save(
                Member.builder()
                        .username(form.getUsername())
                        .password(passwordEncoder.encode(form.getPassword()))
                        .phone(form.getPhone())
                        .nickname(form.getNickname())
                        .build()
        );
    }
}
