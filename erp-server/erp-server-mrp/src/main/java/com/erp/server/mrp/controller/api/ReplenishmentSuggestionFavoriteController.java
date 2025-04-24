package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.ReplenishmentSuggestionFavoriteDTO;
import com.erp.server.mrp.service.ReplenishmentSuggestionFavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 补货建议关注表
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@RestController
@LogSystemModule("补货建议关注表")
@RequestMapping("/replenishmentSuggestionFavorite")
public class ReplenishmentSuggestionFavoriteController extends BaseController {

    @Resource
    private ReplenishmentSuggestionFavoriteService replenishmentSuggestionFavoriteService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "补货建议关注表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ReplenishmentSuggestionFavoriteDTO.AddDTO dto) {
        return success(replenishmentSuggestionFavoriteService.add(dto));
    }
}
