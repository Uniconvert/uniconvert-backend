package com.uniconvert.backend.domain.pot.repository.projection;

import java.math.BigDecimal;

public interface PotAmountProjection {

    Long getPotId();

    BigDecimal getAmount();
}
