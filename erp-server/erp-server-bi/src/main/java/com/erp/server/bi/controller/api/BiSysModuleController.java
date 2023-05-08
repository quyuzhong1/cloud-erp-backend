package com.erp.server.bi.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.validator.UpdateGroup;
import com.erp.model.bi.dto.ModuleSysConfigurationDTO;
import com.erp.model.bi.dto.ModuleSysDTO;
import com.erp.server.bi.constant.BiConstant;
import com.erp.server.bi.service.BiSysModuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 系统模块
 * @Description TODO
 * @Date 2022-12-12 16:58
 * @Created by yl
 */
@RestController
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
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated ModuleSysDTO dto) {
        Boolean flag = this.sysModuleService.insert(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增模块
     *
     * @param dto 实体
     * @return 新增结果
     */
    @PostMapping("/update")
    public ApiResult edit(@RequestBody @Validated(value = {UpdateGroup.class}) ModuleSysDTO dto) {
        Boolean flag = this.sysModuleService.updateSysModule(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 获取父级的id
     *
     * @param
     * @return 新增结果
     */
    @GetMapping("/pidList")
    public ApiResult pidList() {
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
    public ApiResult list() {
        List<Map<String, Object>> list = this.sysModuleService.getSysModuleList(0);
        return success(list);
    }

    /**
     * 模块配置
     */
    @PostMapping("/moduleConfiguration")
    public ApiResult moduleConfiguration(@RequestBody @Validated ModuleSysConfigurationDTO dto) {
        Boolean flag = this.sysModuleService.moduleConfiguration(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 分析页面编辑配置查询
     */
    @GetMapping("/getBySysModuleId")
    public ApiResult getBySysModuleId(@RequestParam("sysModuleId") String sysModuleId) {
        ModuleSysConfigurationDTO dto = this.sysModuleService.getBySysModuleId(sysModuleId);
        return success(dto);
    }

}
