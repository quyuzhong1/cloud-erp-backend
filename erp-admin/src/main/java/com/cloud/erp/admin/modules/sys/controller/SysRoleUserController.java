package com.cloud.erp.admin.modules.sys.controller;


import com.cloud.erp.admin.modules.sys.entity.SysRoleUserEntity;
import com.cloud.erp.admin.modules.sys.service.SysRoleUserService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.cloud.erp.common.common.ApiResult;
import com.cloud.erp.common.common.BaseController;
import com.cloud.erp.common.common.dto.BaseSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;


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
    public ApiResult batchSave(@RequestBody Set<SysRoleUserEntity> list){
        boolean flag= sysRoleUserService.saveBatch(list);
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
