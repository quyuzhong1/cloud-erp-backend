package com.erp.server.mrp.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.entity.ReplenishmentSuggestionDetailEntity;
import com.erp.server.mrp.service.ReplenishmentSuggestionDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 补货建议详细 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@RestController
@RequestMapping("/replenishmentSuggestionDetail")
public class ReplenishmentSuggestionDetailController extends BaseController {

    private ReplenishmentSuggestionDetailService replenishmentSuggestionDetailService;

    /**
     * 根据补货建议id查询
     * @author will
     * @date 2024/11/5 9:56
     * @param id
     * @return ApiResult<ReplenishmentSuggestionDetailEntity>
     */
    @GetMapping("/getByMainId")
    public ApiResult<ReplenishmentSuggestionDetailEntity> getByMainId(@RequestParam String id) {
        ReplenishmentSuggestionDetailEntity entity = replenishmentSuggestionDetailService.getByMainId(id);
        return success(entity);
    }
}
