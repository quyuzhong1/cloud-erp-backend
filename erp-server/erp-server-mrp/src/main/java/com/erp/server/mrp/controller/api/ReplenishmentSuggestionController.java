package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 补货建议主表 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@RestController
@RequestMapping("/replenishment")
public class ReplenishmentSuggestionController extends BaseController {



    public ApiResult paging(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        return null;
    }

}
