package com.erp.server.sys.controller.api;



import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSaveRoleUserDTO;
import com.erp.server.sys.service.SysRoleUserService;
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
@RequestMapping("roleUser")
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
        List<SysUserDTO> list= sysRoleUserService.findRoleUser(dto);
        return success(list);

    }






}
