package com.cloud.erp.admin.modules.sys.controller;


import com.cloud.erp.admin.modules.sys.dto.BatchSaveRoleUserDTO;
import com.cloud.erp.admin.modules.sys.service.SysRoleUserService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-08 11:23:00
 */
@RestController
@RequestMapping("sys/roleUser")
public class SysRoleUserController  extends BaseController {

    @Autowired
    private SysRoleUserService sysRoleUserService;



    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody BatchSaveRoleUserDTO dto){
        boolean flag= sysRoleUserService.saveBatchRoleUser(dto);
        return flag==true?success():failure();
    }


    @RequestMapping("/remove")
    public ApiResult remove(@RequestBody List<String> ids){
        boolean flag= sysRoleUserService.removeByIds(ids);
        return flag==true?success():failure();
    }

    @RequestMapping("/list")
    public ApiResult list(@RequestBody BaseSearchDTO dto){
        List<SysUserVO>  list= sysRoleUserService.findRoleUser(dto);
        return success(list);

    }






}
