package wanted.backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.Member.MemberPrincipal;
import wanted.backend.Repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberPrincipalService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("cannot find account : " + email));

        return new MemberPrincipal(member);
    }
}
