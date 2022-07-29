package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.dto.BatchSavePostUserDTO;
import com.cloud.erp.admin.modules.sys.entity.SysPostUserEntity;
import com.cloud.erp.admin.modules.sys.service.SysPostUserService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import com.erp.common.dto.BaseSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * @Classname SysPostUserController
 * @Description TODO
 * @Date 2022-07-12 17:07
 * @Created by yl
 */
@RestController
@RequestMapping("sys/postUser")
public class SysPostUserController  extends BaseController {

    @Autowired
    private SysPostUserService sysPostUserService;

    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody BatchSavePostUserDTO dto){
        boolean flag= sysPostUserService.saveBatchPostUser(dto);
        return flag==true?success():failure();
    }

    @RequestMapping("/remove")
    public ApiResult remove(@RequestBody List<String> ids){
        boolean flag= sysPostUserService.removeByIds(ids);
        return flag==true?success():failure();
    }

    @RequestMapping("/list")
    public ApiResult list(@RequestBody BaseSearchDTO dto){
       List<SysUserVO>  list= sysPostUserService.findPostUser(dto);
       return success(list);

    }


}
