package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ProductRefLabelDTO;
import com.erp.server.plm.service.ProductRefLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 产品便签关系表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("产品管理")
@RequestMapping("/productRefLabel")
public class ProductRefLabelController extends BaseController {

    @Resource
    private ProductRefLabelService productRefLabelService;

    /**
     * 新增产品标签关系
     *
     * @param dtos
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增产品标签关系")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated ProductRefLabelDTO.BatchAddDTO dtos) {
        productRefLabelService.batchAdd(dtos);
        return success();
    }

    /**
     * 删除产品标签关系
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除产品标签关系")
    @PostMapping("/remove")
    public ApiResult delete(@RequestBody @Validated ProductRefLabelDTO.RemoveDTO dto) {
        productRefLabelService.removeProductRef(dto);
        return success();
    }

}
