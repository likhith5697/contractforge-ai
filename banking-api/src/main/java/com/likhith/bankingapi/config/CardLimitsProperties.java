package com.likhith.bankingapi.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "banking.card-limits")
public class CardLimitsProperties {

    private BigDecimal dailyAtmMax = BigDecimal.valueOf(100000);
    private BigDecimal dailyPosMax = BigDecimal.valueOf(200000);
    private BigDecimal dailyOnlineMax = BigDecimal.valueOf(150000);
    private BigDecimal monthlyTotalMax = BigDecimal.valueOf(1000000);
}
