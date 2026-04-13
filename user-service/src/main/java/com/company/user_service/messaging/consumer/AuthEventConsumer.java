package com.company.user_service.messaging.consumer;

import com.company.user_service.config.RabbitMQConfig;
import com.company.user_service.entity.UserProfile;
import com.company.user_service.messaging.event.UserEvent;
import com.company.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final UserProfileRepository profileRepository;

    /**
     * When a new user registers in Auth Service, auto-create their profile here.
     */
    @RabbitListener(queues = RabbitMQConfig.USER_PROFILE_CREATED_QUEUE)
    public void onUserCreated(UserEvent event) {
        log.info("Received USER_CREATED event for userId={}, email={}", event.getUserId(), event.getEmail());

        if (profileRepository.existsById(event.getUserId())) {
            log.warn("Profile already exists for userId={}, skipping", event.getUserId());
            return;
        }

        UserProfile profile = UserProfile.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .active(true)
                .build();

        profileRepository.save(profile);
        log.info("Created UserProfile for userId={}", event.getUserId());
    }

    /**
     * When a user is disabled in Auth Service, mark their profile inactive.
     */
    @RabbitListener(queues = RabbitMQConfig.USER_PROFILE_DISABLED_QUEUE)
    public void onUserDisabled(UserEvent event) {
        log.info("Received USER_DISABLED event for userId={}", event.getUserId());

        profileRepository.findById(event.getUserId()).ifPresent(profile -> {
            profile.setActive(false);
            profileRepository.save(profile);
            log.info("Deactivated UserProfile for userId={}", event.getUserId());
        });
    }
}
