package com.devops.platform.incident.dto;

import com.devops.platform.incident.model.TimelineEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for timeline entry response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntryResponse {

    private UUID id;
    private TimelineEntryType entryType;
    private String message;
    private String oldValue;
    private String newValue;
    private UUID userId;
    private String userName;
    private Boolean isAutomated;
    private Instant createdAt;
}
