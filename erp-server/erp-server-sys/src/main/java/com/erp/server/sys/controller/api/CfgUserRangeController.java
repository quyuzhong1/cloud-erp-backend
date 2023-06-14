package com.erp.server.sys.controller.api;

import com.erp.model.sys.dto.CfgUserRangeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.CfgUserRangeService;
import com.common.core.controller.vo.ApiResult;

import java.util.List;


/**
 * <p>
 * 用户区间配置
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-12
 */
@RestController
@RequestMapping("/cfgUserRange")
public class CfgUserRangeController extends BaseController {

    @Autowired
    private CfgUserRangeService cfgUserRangeService;

    /**
     * 获取用户区间
     * @param type
     * @return
     */
    @GetMapping("/getUserRange")
    public ApiResult<List<CfgUserRangeDTO.UserRangeDataDTO>> getUserRange(@RequestParam(value = "type") String type) {
        return success(cfgUserRangeService.detail(type));
    }

    /**
     * 设置用户区间
     * @param userRangeDTO
     * @return
     */
    @PostMapping("/setUserRange")
    public ApiResult<Void> setUserRange(@RequestBody @Validated CfgUserRangeDTO.SaveDTO userRangeDTO) {
        cfgUserRangeService.save(userRangeDTO);
        return success();
    }




}
