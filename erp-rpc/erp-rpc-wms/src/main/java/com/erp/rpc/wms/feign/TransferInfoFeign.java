package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 仓库Feign
 *
 * @author Jim
 * @date 2023/11/29
 */
@FeignClient(name = "erp-wms", contextId = "transferInfo",configuration = {FeignErrorDecoder.class})
public interface TransferInfoFeign {

    /**
     * 保存并审核
     * @author zdy
     */
    @PostMapping("/feign/transferInfo/addAndApprove")
    String addAndApprove(@RequestBody @Validated TransferInfoDTO.AddDTO dto);

     /**
     * 根据编码查询
     * @author hcg
     */
     @PostMapping("/feign/transferInfo/listByCodes")
    List<TransferInfoEntity> listByCodes(List<String> list);

     /**
     * 审核
     * @author hcg
     */
     @PostMapping("/feign/transferInfo/updateApproveStatus")
    void updateApproveStatus(TransferInfoDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}


