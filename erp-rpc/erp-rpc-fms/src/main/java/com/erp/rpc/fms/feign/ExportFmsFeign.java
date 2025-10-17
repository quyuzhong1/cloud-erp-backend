package com.erp.rpc.fms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetLocationDTO;
import com.erp.model.fms.dto.AssetAcceptDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * FMS异步导出Feign接口
 * @author wuht
 * @date 2025/10/13
 */
@FeignClient(name = "erp-fms", contextId = "exportFmsFeign", configuration = ExportFeignConfig.class)
public interface ExportFmsFeign {

    /**
     * 导出资产位置
     */
    @PostMapping("/feign/export/getAssetLocationPageData")
    PagingVO<AssetLocationDTO.ListDTO> getAssetLocationPageData(@RequestBody PagingDTO<AssetLocationDTO.ExportDTO> dto);

    /**
     * 导出资产验收表
     */
    @PostMapping("/feign/export/getAssetAcceptPageData")
    PagingVO<AssetAcceptDTO.ListDTO> getAssetAcceptPageData(@RequestBody PagingDTO<AssetAcceptDTO.ExportDTO> dto);

}

