package com.erp.server.oms.service.address;

import com.erp.model.oms.dto.AddressParseDTO;

/**
 * Shared address parse service.
 */
public interface AddressParseService {

    /**
     * Parse raw address text and return structured result.
     */
    AddressParseDTO.ParseResultDTO parse(AddressParseDTO.ParseRequestDTO requestDTO);
}
