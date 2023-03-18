package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.transaction.Transactional;

/**
 * RuntimeException 발생하면 자동으로 Rollback
 * CheckedException 발생하면 자동으로 Commit
 * @Transactional(rollbackOn = MyException.class) 사용하여 특정 Exception Rollback 실행
 *
 * */
@SpringBootTest
public class RollbackTest {

    @Autowired RollbackService rollbackService;

    @Test
    void runtimeException() {
        // 예외 발생 시, RuntimeException 발생
        // Rollback 실행
        Assertions.assertThatThrownBy(() -> rollbackService.runtimeException())
                        .isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkedException() {
        // 예외 발생 시, MyException 발생
        // Commit 실행
        Assertions.assertThatThrownBy(() -> rollbackService.checkedException())
                .isInstanceOf(MyException.class);
    }


    @Test
    void rollbackFor() {
        // 예외 발생 시, MyException 발생
        // Rollback 실행
        Assertions.assertThatThrownBy(() -> rollbackService.rollbackFor())
                .isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class RollbackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    @Slf4j
    static class RollbackService {

        // CASE 1: 런타임 예외 발생 >>> 롤백
        @Transactional
        public void runtimeException() {
            log.info("Call runtimeException");
            throw new RuntimeException();
        }

        // CASE 2: 체크 예외 발생 >>> 커밋
        @Transactional
        public void checkedException() throws MyException {
            log.info("Call checkedException");
            throw new MyException();
        }

        // CASE 3: 체크 예외 rollbackFor 지정 >>> 롤백
        @Transactional(rollbackOn = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("Call checkedException");
            throw new MyException();
        }

    }




    static class MyException extends Exception {

    }
}
