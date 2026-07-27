package com.erp.server.auth.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.auth.config.OpenApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@OpenApi
@Slf4j
public class PurchaseOrderOpenApi {

    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;

    /**
     * 添加产品数据显示
     *
     * @param dto
     * @return ApiResult<ViewProductDTO>
     * @author Will
     * @date: 2023/4/14 10:21
     */
    @PostMapping(value = "purchaseOrderViewProduct")
    public ApiResult<List<PurchaseOrderDetailDTO.ViewProductDTO>> viewProduct(@Valid PurchaseOrderDetailDTO.ProductSearchParamDTO dto) {
        return purchaseOrderFeign.viewProduct(dto);
    }
}
