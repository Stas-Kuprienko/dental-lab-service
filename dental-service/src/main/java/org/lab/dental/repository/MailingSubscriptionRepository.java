package org.lab.dental.repository;

import org.lab.dental.entity.MailingSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MailingSubscriptionRepository extends JpaRepository<MailingSubscription, UUID> {}
