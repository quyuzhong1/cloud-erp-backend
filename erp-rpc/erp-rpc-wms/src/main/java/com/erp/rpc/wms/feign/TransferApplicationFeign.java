package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
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
@FeignClient(name = "erp-wms", contextId = "transferApplication")
public interface TransferApplicationFeign {

     /**
     * 根据编码查询
     * @author hcg
     */
     @PostMapping("/feign/transferApplication/listByCodes")
    List<TransferApplicationEntity> listByCodes(List<String> list);

     /**
     * 审核
     * @author hcg
     */
     @PostMapping("/feign/transferApplication/updateApproveStatus")
    void updateApproveStatus(TransferApplicationDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}


