package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.DictHsCodeDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 出口申报要素表
 *
 * @author jack
 * @since 2025-07-31
 */
@FeignClient(name = "erp-tms", contextId = "dictHsCode" ,configuration = {FeignErrorDecoder.class})
public interface DictHsCodeFeign {

    /**
     * 分页查询
     * @return
     */
    @PostMapping("feign/dictHsCode/pagingByBR")
    PagingVO<DictHsCodeDTO.ListBRDTO> pagingByBR(@RequestBody @Validated PagingDTO<DictHsCodeDTO.PagingParamDTO> dto);


}
