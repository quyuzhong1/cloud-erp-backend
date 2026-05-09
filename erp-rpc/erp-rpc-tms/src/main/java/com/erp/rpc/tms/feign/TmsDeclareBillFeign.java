package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "tmsDeclareBill" ,configuration = {FeignErrorDecoder.class})
public interface TmsDeclareBillFeign {
    /**
     * 根据来源id查询报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param sourceIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/tmsDeclareBill/listBySourceIds")
    List<TmsDeclareBillEntity> listBySourceIds(@RequestBody List<String> sourceIds);

    /**
     * 删除报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/tmsDeclareBill/delete")
    List<BatchResultDTO> delete(@RequestBody TmsDeclareBillDTO.DeleteDTO dto);

    /**
     * 删除tms发货明细
     * @author will
     * @date 2026/4/24 14:55
     * @param sourceIdList 
     * @return java.lang.Boolean
     */
    @PostMapping("/feign/tmsDeclareBill/deleteDeliveryDeclareDetailMid")
    Boolean deleteDeliveryDeclareDetailMid(@RequestBody List<String> sourceIdList);


    /**
     * 合并报关单预览
     **/
    @PostMapping("/feign/tmsDeclareBill/autoMergeDeclareBillView")
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(@RequestBody TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO);
}
