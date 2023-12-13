package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductAccessoriesDTO;
import com.erp.server.plm.service.ProductAccessoriesService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 产品包装辅料信息
 * @author Will
 * @version 1.0
 * @date 2023/8/24 17:56
 */
@RestController
@RequestMapping("product/pack")
public class ProductAccessoriesController extends BaseController {
    @Resource
    private ProductAccessoriesService productAccessoriesService;

    /**
     * 查询包装辅料
     * @author Will
     * @date: 2023/8/24 18:37
     * @param dto
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/listAccessories")
    public ApiResult<List<ProductAccessoriesDTO.ListDTO>> listAccessories(@RequestBody @Validated ProductAccessoriesDTO.ParamDTO dto) {
        List<ProductAccessoriesDTO.ListDTO> list = productAccessoriesService.listAccessories(dto);
        return success(list);
    }
}
