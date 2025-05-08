package ru.crackedglass.cdr_generator.impl.service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.crackedglass.cdr_generator.impl.entity.CallEntity;
import ru.crackedglass.cdr_generator.impl.entity.SubscriberEntity;

@RequiredArgsConstructor
@Service
public class GeneratorService {

    private final Random random = new Random();

    public List<CallEntity> generateCalls(long callCount, List<SubscriberEntity> subscribers, Instant generationPeriodStart, Instant generationPeriodEnd) {
        List<CallEntity> calls = new ArrayList<>();

        if (callCount <= 0 || subscribers == null || subscribers.size() < 2) {
            return calls;
        }
        // Validate generation period
        if (generationPeriodStart == null || generationPeriodEnd == null || generationPeriodStart.isAfter(generationPeriodEnd)) {
            return calls; 
        }

        Instant nextConceptualCallCanStartAfter = generationPeriodStart;

        for (int i = 0; i < callCount; i++) {
            int subscriberCount = subscribers.size();
            int callerIndex = random.nextInt(subscriberCount);
            int calleeIndex;
            do {
                calleeIndex = random.nextInt(subscriberCount);
            } while (callerIndex == calleeIndex);

            SubscriberEntity caller = subscribers.get(callerIndex);
            SubscriberEntity callee = subscribers.get(calleeIndex);

            // Determine start time for the current conceptual call
            Instant currentConceptualCallStartTime = nextConceptualCallCanStartAfter.plusSeconds(random.nextInt(301)); // Add random gap

            if (currentConceptualCallStartTime.isAfter(generationPeriodEnd)) {
                break; // No more calls can start within the period
            }

            // Generate conceptual end time
            Instant currentConceptualCallEndTime = currentConceptualCallStartTime.plusSeconds(60 + random.nextInt(3541));

            // Update for the *next* iteration based on the *unclamped* conceptual end time
            nextConceptualCallCanStartAfter = currentConceptualCallEndTime;

            // Clamp the current conceptual call to the generation period
            Instant effectiveCallStartTime = currentConceptualCallStartTime; // Already >= generationPeriodStart
            Instant effectiveCallEndTime = currentConceptualCallEndTime.isAfter(generationPeriodEnd) ? generationPeriodEnd : currentConceptualCallEndTime;

            if (!effectiveCallStartTime.isBefore(effectiveCallEndTime)) { // If call has zero or negative duration after clamping
                continue;
            }

            // Check if the *effective* call spans midnight (UTC)
            if (effectiveCallStartTime.atZone(ZoneOffset.UTC).toLocalDate().isBefore(effectiveCallEndTime.atZone(ZoneOffset.UTC).toLocalDate())) {
                Instant endOfStartDay = effectiveCallStartTime.atZone(ZoneOffset.UTC).toLocalDate().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                Instant part1EndTime = endOfStartDay.isAfter(effectiveCallEndTime) ? effectiveCallEndTime : endOfStartDay;

                if (effectiveCallStartTime.isBefore(part1EndTime)) {
                    CallEntity callPart1 = CallEntity.builder()
                                            .subscriber1(caller)
                                            .subscriber2(callee)
                                            .start(effectiveCallStartTime)
                                            .end(part1EndTime)
                                            .callType(random.nextBoolean() ? "01" : "02")
                                            .build();
                    calls.add(callPart1);
                }

                Instant startOfNextDay = effectiveCallStartTime.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                if (startOfNextDay.isBefore(effectiveCallEndTime)) {
                    CallEntity callPart2 = CallEntity.builder()
                                            .subscriber1(caller)
                                            .subscriber2(callee)
                                            .start(startOfNextDay)
                                            .end(effectiveCallEndTime)
                                            .callType(random.nextBoolean() ? "01" : "02")
                                            .build();
                    calls.add(callPart2);
                }
            } else {
                // Call does not span midnight
                CallEntity call = CallEntity.builder()
                                        .subscriber1(caller)
                                        .subscriber2(callee)
                                        .start(effectiveCallStartTime)
                                        .end(effectiveCallEndTime)
                                        .callType(random.nextBoolean() ? "01" : "02")
                                        .build();
                calls.add(call);
            }
        }
        return calls;
    }
}
