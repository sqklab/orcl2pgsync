package com.lguplus.fleta.domain.util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableConstraint{
    Boolean isPrimaryKeyConstraint;
    String constraints;
}
