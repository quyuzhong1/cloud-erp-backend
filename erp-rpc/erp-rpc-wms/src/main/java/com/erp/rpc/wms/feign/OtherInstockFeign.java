package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.TransferApplicationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


/**
 * 仓库Feign
 *
 * @author Jim
 * @date 2023/11/29
 */
@FeignClient(name = "erp-wms", contextId = "otherInstock" ,configuration = {FeignErrorDecoder.class})
public interface OtherInstockFeign {

     /**
     * 根据编码查询
     * @author hcg
     */
     @PostMapping("/feign/otherInstock/listByCodes")
    List<OtherInstockEntity> listByCodes(List<String> list);

     /**
     * 审核
     * @author hcg
     */
     @PostMapping("/feign/otherInstock/updateApproveStatus")
    void updateApproveStatus(OtherInstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}


