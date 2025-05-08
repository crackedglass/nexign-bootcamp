package ru.crackedglass.cdr_generator.impl.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.crackedglass.cdr_generator.impl.entity.SubscriberEntity;
import ru.crackedglass.cdr_generator.impl.entity.CallEntity;


@SpringBootTest
public class GeneratorServiceMockTest {

    @Autowired
    private GeneratorService generatorService;
    
    @Test
    void shouldGenerateCallsWhenTwoSubscribersExist() {

        var subscribers = List.of(
            SubscriberEntity.builder().id(1L).phoneNumber("1234567890").build(),
            SubscriberEntity.builder().id(2L).phoneNumber("1234567891").build()
        );

        var actual = generatorService.generateCalls(10, subscribers, Instant.now(), Instant.now().plus(Duration.ofDays(1)));

       
        assertThat(actual).hasSize(10);
    }
    
    @Test
    void shouldGenerateCallsWithoutTimeOverlaps() {
        // Given
        var subscribers = List.of(
            SubscriberEntity.builder().id(1L).phoneNumber("1234567890").build(),
            SubscriberEntity.builder().id(2L).phoneNumber("1234567891").build(),
            SubscriberEntity.builder().id(3L).phoneNumber("1234567892").build()
        );
        
        // When
        List<CallEntity> calls = generatorService.generateCalls(20, subscribers, Instant.now(), Instant.now().plus(Duration.ofDays(1)));
        
        // Then
        assertThat(hasNoOverlappingCallsForSameSubscriber(calls)).isTrue();
    }
    
    private boolean hasNoOverlappingCallsForSameSubscriber(List<CallEntity> calls) {
        for (int i = 0; i < calls.size(); i++) {
            CallEntity call1 = calls.get(i);
            long subscriber1Id = call1.getSubscriber1().getId();
            long subscriber2Id = call1.getSubscriber2().getId();
            
            for (int j = i + 1; j < calls.size(); j++) {
                CallEntity call2 = calls.get(j);
                
                // Check if the calls involve the same subscriber
                if (call2.getSubscriber1().getId() == subscriber1Id || 
                    call2.getSubscriber2().getId() == subscriber1Id ||
                    call2.getSubscriber1().getId() == subscriber2Id || 
                    call2.getSubscriber2().getId() == subscriber2Id) {
                    
                    // If there's an overlap for the same subscriber, return false
                    if (!areCallsNotOverlapping(call1, call2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean areCallsNotOverlapping(CallEntity call1, CallEntity call2) {
        return call1.getEnd().isBefore(call2.getStart()) || 
               call2.getEnd().isBefore(call1.getStart());
    }
}
