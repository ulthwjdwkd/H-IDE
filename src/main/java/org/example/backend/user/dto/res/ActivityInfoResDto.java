package org.example.backend.user.dto.res;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class ActivityInfoResDto {
    private int level;
    private int point;
    private int alarmCycle;
}
