package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 *  우선순위 : Method > Class 순으로 적용
 *  Class 적용시, Method 작용 적용
 *
 *  1. Class Method
 *  2. Class Type
 *  3. Interface Method
 *  4. Interface Type
 *
 * */
@SpringBootTest
public class TxLevelTest {

    @Autowired LevelService levelService;

    @Test
    void orderTest() {
        levelService.write();
        levelService.read();
    }


    @TestConfiguration
    static class TxLevelTestConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }





    @Slf4j
    @Transactional(readOnly = true)  // 읽기전용 트랜젝션 (삽입, 수정 등 못함)
    static class LevelService {

        @Transactional(readOnly = false)
        public void write() {
            log.info("Call write");
            printTxInfo();
        }

        public void read() {
            log.info("Call read");
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
