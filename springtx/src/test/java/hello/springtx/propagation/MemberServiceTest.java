package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired LogRepository logRepository;


    /**
     * memberService        @Transactional : OFF
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON
     * */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // When
        memberService.joinV1(username);

        // When: 모든 데이터가 정상 저장.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService        @Transactional : OFF
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON -> Exception
     * */
    @Test
    void outerTxOff_fail() {
        // given
        String username = "outerTxOff_fail";

        // When
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // When: 로그 데이터만 Rollback.
        assertTrue(memberRepository.find(username).isPresent()); // Commit
        assertTrue(logRepository.find(username).isEmpty());      // Rollback
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : OFF
     * logRepository        @Transactional : OFF
     * */
    @Test
    void singleTx() {
        // given
        String username = "outerTxOff_success";

        // When
        memberService.joinV1(username);

        // When: 모든 데이터가 정상 저장.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON
     * */
    @Test
    void outerTxOn_success() {
        // given
        String username = "outerTxOn_success";

        // When
        memberService.joinV1(username);

        // When: 모든 데이터가 정상 저장.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON -> Exception
     * */
    @Test
    void outerTxOn_fail() {
        // given
        String username = "outerTxOn_fail";

        // When
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // When: 모든 데이터가 정상 Rollback.
        assertTrue(memberRepository.find(username).isEmpty());   // Rollback
        assertTrue(logRepository.find(username).isEmpty());      // Rollback
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON -> Exception
     * */
    @Test
    void recoverException_fail() {
        // given
        String username = "recoverException_fail";

        // When
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // When: 모든 데이터가 정상 Rollback.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON(REQUIRES_NEW) -> Exception
     * */
    @Test
    void recoverException_success() {
        // given
        String username = "recoverException_success";

        // When
        memberService.joinV2(username);

        // When: member Commit, log Rollback
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }
}