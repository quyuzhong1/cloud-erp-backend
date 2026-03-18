package com.erp.server.dmp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpCfgApiDTO;
import com.erp.model.dmp.dto.DmpCfgDbDTO;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.model.dmp.dto.DmpCfgMqDTO;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgDbEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgDbService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 输入输出信息
 *
 */
@Slf4j
@RestController
@LogSystemModule("输入输出信息")
@RequestMapping("/dmpCfgCommon")
public class DmpCfgCommonController extends BaseController {

    @Resource
    private DmpCfgDbService dmpCfgDbService;
    @Resource
    private DmpCfgMqService dmpCfgMqService;
    @Resource
    private DmpCfgApiService dmpCfgApiService;

    /**
     * 前端展示输入输出类型详情
     *
     * @return ApiResult<DmpCfgInputDTO.ViewDTO>>
     */
    @GetMapping("/view")
    public ApiResult<?> view(@RequestParam("id") String id) {
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(id);
        if (null != dmpCfgApiEntity){
            return success(dmpCfgApiService.view(id));
        }
        DmpCfgMqEntity dmpCfgMqEntity = dmpCfgMqService.getById(id);
        if (null != dmpCfgMqEntity){
            return success(dmpCfgMqService.view(id));
        }
        DmpCfgDbEntity dmpCfgDbEntity = dmpCfgDbService.getById(id);
        if (null != dmpCfgDbEntity){
            return success(dmpCfgDbService.view(id));
        }
        return failure("未知类型");
    }

    /**
     * 前端展示api类型详情
     *
     */
    @GetMapping("/viewApi")
    public ApiResult<DmpCfgApiDTO.ViewDTO> viewApi(@RequestParam("id") String id) {
       return success(dmpCfgApiService.view(id));
    }

    /**
     * 前端展示mq类型详情
     *
     */
    @GetMapping("/viewMq")
    public ApiResult<DmpCfgMqDTO.ViewDTO> viewMq(@RequestParam("id") String id) {
        return success(dmpCfgMqService.view(id));
    }

    /**
     * 前端展示mq类型详情
     *
     */
    @GetMapping("/viewDb")
    public ApiResult<DmpCfgDbDTO.ViewDTO> viewDb(@RequestParam("id") String id) {
        return success(dmpCfgDbService.view(id));
    }


}
