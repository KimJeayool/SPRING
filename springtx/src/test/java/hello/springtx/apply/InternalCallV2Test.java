package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * 트랜젝션이 아닌 Class, Method 안에서 사용 시, 트랜젝션 클래스를 별도로 생성하여 사용
 * @Transcational 꼭 public 함수에서만 사용
 *
 * */
@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    // Shift + Option + Command + L  :  자동정렬
    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void externalCallV2() {
        // tx active=false
        callService.external();
    }


    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }


    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("Call external");
            printTxInfo();

            // 외부호출로 변경
            internalService.internal();
        }

        private void printTxInfo() {
            // 트랜젝션 수행 여부
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);

            // 트랜젝션 읽기전용 여부
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly={}", readOnly);
        }
    }


    // 실무에서 보통 별도의 클래스로 분리하여 사용
    static class InternalService {

        @Transactional  // "public" Method 적용 가능
        public void internal() {
            // 외부호출로 변경
            // tx active=true
            log.info("Call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            // 트랜젝션 수행 여부
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);

            // 트랜젝션 읽기전용 여부
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly={}", readOnly);
        }

    }
}
