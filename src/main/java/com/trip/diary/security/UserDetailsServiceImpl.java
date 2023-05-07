package com.trip.diary.security;

import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberPrincipal principal =  new MemberPrincipal(memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.NOT_FOUND_MEMBER.toString())));
        log.error(principal.getAuthorities().toString());
        return principal;
    }
}
