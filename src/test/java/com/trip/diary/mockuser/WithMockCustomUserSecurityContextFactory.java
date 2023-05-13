package com.trip.diary.mockuser;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.type.MemberType;
import com.trip.diary.security.MemberPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    private static final Member member = Member.builder()
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .phone("01011111111")
            .build();

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(new MemberPrincipal(member), "",
                Stream.of(MemberType.USER.name()).map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
        context.setAuthentication(authentication);
        return context;
    }
}
