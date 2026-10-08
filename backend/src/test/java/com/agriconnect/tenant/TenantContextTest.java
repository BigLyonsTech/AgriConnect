package com.agriconnect.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void setAndGetTenantId_returnsTheValueThatWasSet() {
        TenantContext.setTenantId(42L);

        assertThat(TenantContext.getTenantId()).isEqualTo(42L);
    }

    @Test
    void clear_removesTheStoredTenantId() {
        TenantContext.setTenantId(42L);

        TenantContext.clear();

        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void getTenantId_whenNeverSet_returnsNull() {
        assertThat(TenantContext.getTenantId()).isNull();
    }
}
