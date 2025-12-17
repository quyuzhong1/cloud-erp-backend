package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soChangeFeign",configuration = {FeignErrorDecoder.class})
public interface SoChangeFeign {

    /**
     * 销售变更审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soChange/approve")
    ApiResult<List<BatchResultDTO>> approve(@RequestBody BaseApproveParamDTO dto);

    /**
     * 根据变更单号查询变更单信息
     * @param list
     */
    @PostMapping("feign/soChange/listByCodes")
    List<SoChangeEntity> listByCodes(List<String> list);


    /**
     *  根据变更单号查询变更单信息
     */
    @PostMapping ("feign/soChange/updateApproveStatus")
    void updateApproveStatus(SoChangeEntity entity);
}
