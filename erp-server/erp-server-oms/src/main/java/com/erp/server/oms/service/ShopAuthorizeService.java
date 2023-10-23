package com.erp.server.oms.service;

import com.erp.model.oms.dto.ShopAuthorizeDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ShopAuthorizeService {

    Boolean shopAuthorize(ShopAuthorizeDTO dto);
}
