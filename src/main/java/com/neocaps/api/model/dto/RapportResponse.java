package com.neocaps.api.model.dto;

import com.neocaps.api.model.entity.Lot;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportResponse {

    private UUID id;
    private String title;
    private LocalDateTime createdAt;
    private String generatedBy;
    private String filePath;
    private Lot lot;
}
