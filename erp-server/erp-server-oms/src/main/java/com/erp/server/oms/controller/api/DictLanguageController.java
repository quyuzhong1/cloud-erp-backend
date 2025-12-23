package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.oms.service.DictLanguageService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.DictLanguageDTO;

import java.util.List;

/**
 * ISO 639-1 语言标准
 *
 * @author jack
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("ISO 639-1 语言标准")
@RequestMapping("/dictLanguage")
public class DictLanguageController extends BaseController {

    @Resource
    private DictLanguageService dictLanguageService;
    /**
     * 下拉列表
     * @author jack
     * @date:  2025-12-01
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/drop/down")
    public ApiResult<List<DictLanguageDTO.ListDTO>> dropDown(@RequestBody DictLanguageDTO.SelectDTO dto) {
        return success(dictLanguageService.dropDown(dto));
    }



}
