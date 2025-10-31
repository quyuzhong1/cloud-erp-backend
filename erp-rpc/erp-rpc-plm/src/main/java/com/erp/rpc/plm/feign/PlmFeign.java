package com.erp.rpc.plm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductDetailShowDTO;
import com.erp.model.plm.dto.ProductSkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * PLM 服务 Feign 接口
 * @author wuhaotian
 * @since 2025-09-24
 */
@FeignClient(name = "erp-plm", contextId = "plmFeign", configuration = {FeignErrorDecoder.class})
public interface PlmFeign {

    /**
     * 查找用户列表
     */
    @PostMapping("/feign/common/findUserList")
    ApiResult<List<FindUserDTO>> findUserList(@RequestBody BaseSearchDTO dto);

    /**
     * 产品明细列表
     */
    @PostMapping("/feign/productDetail/list")
    ApiResult<PagingVO<ProductDetailShowDTO>> productDetailList(@RequestBody @Valid PagingDTO<ProductSkuDTO> pagingDTO);
}
