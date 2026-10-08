package com.agriconnect.farm.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.farm.dto.FarmRequest;
import com.agriconnect.farm.dto.FarmResponse;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.repository.FarmRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private FarmService farmService;

    @BeforeEach
    void authenticateAsOwner() {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        com.agriconnect.tenant.TenantContext.setTenantId(10L);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        com.agriconnect.tenant.TenantContext.clear();
    }

    @Test
    void create_asOwner_setsTenantIdAndSaves() {
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> {
            Farm f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        FarmResponse response = farmService.create(new FarmRequest("Green Valley", "Lagos, NG", 12.5));

        assertThat(response.getName()).isEqualTo("Green Valley");
        assertThat(response.getLocation()).isEqualTo("Lagos, NG");
    }

    @Test
    void create_asBuyer_isRejected() {
        SecurityContextHolder.clearContext();
        var buyerAuth = new UsernamePasswordAuthenticationToken(2L, null, List.of(new SimpleGrantedAuthority("ROLE_BUYER")));
        SecurityContextHolder.getContext().setAuthentication(buyerAuth);

        assertThatThrownBy(() -> farmService.create(new FarmRequest("Some Farm", "Abuja, NG", 5.0)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findById_whenFarmDoesNotExist_throwsNotFound() {
        when(farmRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
