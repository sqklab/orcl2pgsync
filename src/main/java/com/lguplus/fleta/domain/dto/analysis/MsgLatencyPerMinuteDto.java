package com.lguplus.fleta.domain.dto.analysis;

import org.springframework.beans.factory.annotation.Value;

public interface MsgLatencyPerMinuteDto extends ProcessedMessagePerMinuteDto{

    @Value("#{target.totalLatency}")
    Long getTotalLatency();
}
