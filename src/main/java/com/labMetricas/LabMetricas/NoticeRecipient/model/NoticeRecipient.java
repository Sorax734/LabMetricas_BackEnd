package com.labMetricas.LabMetricas.NoticeRecipient.model;

import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notice_recipients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRecipient {
    @EmbeddedId
    private NoticeRecipientId id;

    @ManyToOne
    @MapsId("noticeId")
    @JoinColumn(name = "notice_id", columnDefinition = "BINARY(16)")
    private Notice notice;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)")
    private User user;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeRecipientId implements Serializable {
        @Column(columnDefinition = "BINARY(16)")
        private UUID noticeId;

        @Column(columnDefinition = "BINARY(16)")
        private UUID userId;
    }
}