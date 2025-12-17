package com.erp.rpc.plm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * plm 远程调用接口
 *
 * @author yl
 * @Classname PlmTaskFeign
 * @Date 2022-10-21 9:06
 *
 */
@FeignClient(name = "erp-plm",contextId = "logisticsProductFeign",configuration = {FeignErrorDecoder.class})
public interface LogisticsProductFeign {

    @PostMapping("feign/logistics/product/listLogisticsProduct")
    List<LogisticsProductDTO.ProductDTO> listLogisticsProduct(@RequestBody List<String> skuIdList);

    @PostMapping("feign/logistics/product/listBySkuNoList")
    List<LogisticsProductDTO.ProductDTO> listBySkuNoList(@RequestBody List<String> skuNoList);
}