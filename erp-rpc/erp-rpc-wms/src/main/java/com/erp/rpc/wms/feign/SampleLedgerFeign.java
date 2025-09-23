package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.SampleLedgerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description:
 * @author jack
 * @date: 2025-09-02
 */
@FeignClient(name = "erp-wms", contextId = "sampleLedgerFeign" ,configuration = {FeignErrorDecoder.class})
public interface SampleLedgerFeign {

    @PostMapping("/feign/sampleLedger/listLedgerByUserId")
    List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerByUserId(@RequestBody SampleLedgerDTO.SearchDTO dto);


    @PostMapping("/feign/sampleLedger/listLedgerAll")
    List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerAll(@RequestBody SampleLedgerDTO.SearchAllDTO dto);

}
