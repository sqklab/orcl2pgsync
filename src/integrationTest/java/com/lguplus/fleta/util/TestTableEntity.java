package com.lguplus.fleta.util;

import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TestTableEntity{
    private String bpchar;
    private String varchar;
    private Float float8;
    private Integer int4;
    private Integer numeric;
    private Integer int2;
    private String text;

    public void setRandomData(){
        this.bpchar = RandomStringUtils.randomAlphanumeric(4);
        this.varchar = RandomStringUtils.randomAlphanumeric(4);
        this.float8 = (float) Math.random()*1000;
        this.numeric = (int) (Math.random()*1000);
        this.int2 = (int) (Math.random()*10);
        this.int4 = (int) (Math.random()*10);
        this.text = RandomStringUtils.randomAlphanumeric(30);
    }
}
