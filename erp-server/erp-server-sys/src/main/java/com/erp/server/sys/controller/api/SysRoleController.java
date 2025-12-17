package com.erp.server.sys.controller.api;


import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.CopyRoleDTO;
import com.erp.model.sys.entity.SysRoleEntity;
import com.erp.server.sys.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 角色表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@RestController
@LogSystemModule("角色管理")
@RequestMapping("role")
public class SysRoleController extends BaseController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public ApiResult list(){
        List<SysRoleEntity> list = sysRoleService.list();
        return success(list);
    }


    /**
     * 信息
     */
    @LogViewService
    @RequestMapping("/info/{id}")
    public ApiResult info(@PathVariable("id") String id){
		SysRoleEntity sysRole = sysRoleService.getById(id);
        return  success(sysRole);
    }

    /**
     * 保存
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "保存角色")
    @RequestMapping("/save")
    public ApiResult save(@RequestBody SysRoleEntity sysRole){
        boolean flag=sysRoleService.saveRoleEntity(sysRole);
        if(flag){
            return success();
        }else{
            return  failure(ApiError.ERROR_1002);
        }

    }

    /**
     * 修改
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改角色")
    @RequestMapping("/update")
    public ApiResult update(@RequestBody SysRoleEntity sysRole){
		sysRoleService.updateById(sysRole);
        return success();
    }

    /**
     * 删除
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除角色")
    @RequestMapping("/delete")
    public ApiResult delete(@RequestBody List<String> ids){
        List<BatchResultDTO> resultDTOList =sysRoleService.removeRoleById(ids);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "复制角色:id={id}")
    @PostMapping("/copy")
   public ApiResult copy(@RequestBody @Validated CopyRoleDTO dto){
        sysRoleService.copyRole(dto.getRoleId());
        return success();
    }

}
