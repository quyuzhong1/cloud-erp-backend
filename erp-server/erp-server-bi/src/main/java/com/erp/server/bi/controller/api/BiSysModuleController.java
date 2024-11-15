package com.erp.server.bi.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.validator.UpdateGroup;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.ModuleSysConfigurationDTO;
import com.erp.model.bi.dto.ModuleSysDTO;
import com.erp.model.bi.entity.BiSysModuleEntity;
import com.erp.server.bi.constant.BiConstant;
import com.erp.server.bi.service.BiSysModuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 系统模块

 * @Date 2022-12-12 16:58
 * @Created by yl
 */
@RestController
@LogSystemModule("模块管理")
@RequestMapping("sys/module")
public class BiSysModuleController extends BaseController {


    @Resource
    private BiSysModuleService sysModuleService;


    /**
     * 新增模块
     *
     * @param dto 实体
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增系统模块")
    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody @Validated ModuleSysDTO dto) {
        boolean flag = this.sysModuleService.insert(dto);
        return flag ? success() : failure();
    }

    /**
     * 更新模块
     *
     * @param dto 实体
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新系统模块")
    @PostMapping("/update")
    public ApiResult<Void> edit(@RequestBody @Validated(value = {UpdateGroup.class}) ModuleSysDTO dto) {
        boolean flag = this.sysModuleService.updateSysModule(dto);
        return flag ? success() : failure();
    }

    /**
     * 模块详情
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<BiSysModuleEntity> view(@RequestBody @Validated BaseIdDTO dto) {
        BiSysModuleEntity result = sysModuleService.view(dto.getId());
        return success(result);
    }


    /**
     * 获取父级的id
     *
     * @param
     * @return 新增结果
     */
    @GetMapping("/pidList")
    public ApiResult<List<Map<String, Object>>> pidList() {
        List<Map<String, Object>> list = this.sysModuleService.getPid(BiConstant.PID);
        return success(list);
    }


    /**
     * 获取父级的id
     *
     * @param
     * @return 新增结果
     */
    @GetMapping("/list")
    public ApiResult<List<Map<String, Object>>> list() {
        List<Map<String, Object>> list = this.sysModuleService.getSysModuleList(0);
        return success(list);
    }

    /**
     * 模块配置
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新系统模块配置:id={id},模块名称={name}")
    @PostMapping("/moduleConfiguration")
    public ApiResult<Void> moduleConfiguration(@RequestBody @Validated ModuleSysConfigurationDTO dto) {
        boolean flag = this.sysModuleService.moduleConfiguration(dto);
        return flag ? success() : failure();
    }

    /**
     * 分析页面编辑配置查询
     */
    @GetMapping("/getBySysModuleId")
    public ApiResult<ModuleSysConfigurationDTO> getBySysModuleId(@RequestParam("sysModuleId") String sysModuleId) {
        ModuleSysConfigurationDTO dto = this.sysModuleService.getBySysModuleId(sysModuleId);
        return success(dto);
    }

}
