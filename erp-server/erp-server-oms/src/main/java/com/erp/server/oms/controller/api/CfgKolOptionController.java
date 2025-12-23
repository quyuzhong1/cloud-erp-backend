package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgKolOptionDTO;
import com.erp.server.oms.service.CfgKolOptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * kol类型表
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("kol类型表")
@RequestMapping("/cfgKolOption")
public class CfgKolOptionController extends BaseController {

    @Resource
    private CfgKolOptionService cfgKolOptionService;

    /**
    * 新增
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "kol类型表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgKolOptionDTO.AddDTO dto) {
        return success(cfgKolOptionService.add(dto));
    }

    /**
     * 查询下拉选项
     * @author will
     * @date 2025/12/1 16:16
     * @param type
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/select")
    public ApiResult<List<CfgKolOptionDTO.ViewDTO>> select(@RequestParam(value = "type") String type) {
        return success(cfgKolOptionService.select(type));
    }

}
