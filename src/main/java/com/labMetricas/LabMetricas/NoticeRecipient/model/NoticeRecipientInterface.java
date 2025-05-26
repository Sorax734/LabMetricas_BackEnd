package com.labMetricas.LabMetricas.NoticeRecipient.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRecipientInterface extends JpaRepository<NoticeRecipient, NoticeRecipient.NoticeRecipientId> {

}