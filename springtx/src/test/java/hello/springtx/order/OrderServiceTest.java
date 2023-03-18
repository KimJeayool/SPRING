package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;


    @Test
    void complete() throws NotEnoughMoneyException {
        // Given
        Order order = new Order();
        order.setUsername("정상");
        // When
        orderService.order(order);
        // Then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }


    @Test
    void runtimeException() throws NotEnoughMoneyException{
        // Given
        Order order = new Order();
        order.setUsername("예외");
        // When
        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class);
        // Then
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional.isEmpty()).isTrue();
    }


    @Test
    void bizException() {
        // Given
        Order order = new Order();
        order.setUsername("잔고부족");
        // When
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("잔고부족으로 인하여 고객에게 안내");
        }
        // Then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }
}