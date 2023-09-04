package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductBomHistoryDTO;
import com.erp.server.plm.service.ProductBomHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * bom 历史表(ProductBomHistory)表控制层
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
@RestController
@RequestMapping("productBomHistory")
public class ProductBomHistoryController extends BaseController {

    @Resource
    private ProductBomHistoryService productBomHistoryService;

    /**
     * 查询历史版本下拉
     * @author Will
     * @date: 2023/8/28 19:00
     * @param dto
     * @return ApiResult<List<VersionDTO>>
     */
    @PostMapping("/listHistoryVersion")
    public ApiResult<List<ProductBomHistoryDTO.VersionDTO>> listHistoryVersion(@RequestBody @Validated ProductBomHistoryDTO.ParamDTO dto) {
        List<ProductBomHistoryDTO.VersionDTO> list = this.productBomHistoryService.listHistoryVersion(dto);
        return success(list);
    }


}

