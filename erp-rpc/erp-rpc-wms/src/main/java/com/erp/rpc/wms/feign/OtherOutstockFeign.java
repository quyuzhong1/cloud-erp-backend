package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


/**
 * 仓库Feign
 *
 * @author Jim
 * @date 2023/11/29
 */
@FeignClient(name = "erp-wms", contextId = "otherOutstock")
public interface OtherOutstockFeign {

     /**
     * 根据编码查询
     * @author hcg
     */
     @PostMapping("/feign/otherOutstock/listByCodes")
    List<OtherOutstockEntity> listByCodes(List<String> list);

     /**
     * 审核
     * @author hcg
     */
     @PostMapping("/feign/otherOutstock/updateApproveStatus")
    void updateApproveStatus(OtherOutstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}


