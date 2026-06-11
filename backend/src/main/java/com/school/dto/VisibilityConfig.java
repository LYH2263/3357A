package com.school.dto;

import lombok.Data;
import java.util.List;

@Data
public class VisibilityConfig {
    private String visibilityType;
    private List<Integer> classIds;
}
