package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

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

    /**
     * 添加样品台账流水
     * @param addDTO 台账流水新增参数
     * @return 是否成功
     */
    @PostMapping("/feign/sampleLedger/addSampleLedgerFlow")
    Boolean addSampleLedgerFlow(@RequestBody SampleLedgerFlowDTO.AddFlowDTO addDTO);

    /**
     * 批量查询样品台账数量
     * @param sampleLedgerIds 样品台账ID列表
     * @return 台账ID到数量的映射 Map<sampleLedgerId, qty>
     */
    @PostMapping("/feign/sampleLedger/getLedgerQtyMap")
    Map<String, Integer> getLedgerQtyMap(@RequestBody List<String> sampleLedgerIds);

}
