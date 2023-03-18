package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    // Shift + Option + Command + L  :  자동정렬
    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCall() {
        // tx active=true
        callService.internal();
    }

    @Test
    void externalCall() {
        // tx active=false
        callService.external();
    }


    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService() {
            return new CallService();
        }
    }


    static class CallService {

        public void external() {
            log.info("Call external");
            printTxInfo();

            // 트랜젝션 적용이 안 됨
            // 프록시를 거치지 않고, this.internal() 호
            internal();
        }

        @Transactional
        public void internal() {
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
