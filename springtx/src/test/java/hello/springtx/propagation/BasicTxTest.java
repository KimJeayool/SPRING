package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManger;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource ) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        // 트랜잭션 가져오기
        TransactionStatus status = txManger.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManger.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        // 트랜잭션 가져오기
        TransactionStatus status = txManger.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManger.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }


    /**
     * 'conn0' 커낵션을 같이 사용하지만, 커낵션을 다루는 프록시 객체와 주소가 다르다.
     * 첫번째 : HikariProxyConnection@1448428389 wrapping conn0
     * 두번째 : HikariProxyConnection@881578083 wrapping conn0
     * */
    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManger.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManger.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManger.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        txManger.commit(tx2);
    }

    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManger.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManger.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManger.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        txManger.rollback(tx2);
    }


    /**
     * 처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리
     * outer 트랜잭션이 inner 트랜잭션을 관리
     * */
    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManger.getTransaction(new DefaultTransactionAttribute());
        // 새로운 트랜잭션 여부 확인
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction()); // true

        // Option + Commend + M : 메소드 생성
        // Shift + F6 : 메소드 이름 변경
        innerCommit();

        log.info("외부 트랜잭션 커밋");
        txManger.commit(outer); // 커밋 실행 O
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManger.getTransaction(new DefaultTransactionAttribute());
        // 새로운 트랜잭션 여부 확인
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction()); // true

        // Option + Commend + M : 메소드 생성
        // Shift + F6 : 메소드 이름 변경
        innerCommit();

        log.info("외부 트랜잭션 롤백");
        txManger.rollback(outer); // 롤백 실행 O
    }

    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManger.getTransaction(new DefaultTransactionAttribute());
        // 새로운 트랜잭션 여부 확인
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction()); // true

        // Transaction rolled back because it has been marked as rollback-only
        // 내부 트랜잭션을 롤백하면 실제 물리 트랜잭션은 롤백하지 않는다.
        // 트랜잭션 동기화 매니저에 rollbackOnly=true 설정이 필요하다.
        // 스프링에서는 UnexpectedRollbackException 런타임 예외를 던진다.
        innerRollback();

        log.info("외부 트랜잭션 커밋");
        assertThatThrownBy(() -> txManger.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }


    private void innerCommit() {
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManger.getTransaction(new DefaultTransactionAttribute());
        // 새로운 트랜잭션 여부 확인
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction()); // false
        log.info("내부 트랜잭션 커밋");
        txManger.commit(inner); // 커밋 실행 X
    }

    private void innerRollback() {
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManger.getTransaction(new DefaultTransactionAttribute());
        // 새로운 트랜잭션 여부 확인
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction()); // false
        log.info("내부 트랜잭션 롤백");
        txManger.rollback(inner); // 커밋 실행 X
    }
}
