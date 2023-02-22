package com.erp.server.sys.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSavePostUserDTO;
import com.erp.server.sys.service.SysPostUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
       List<SysUserDTO> list= sysPostUserService.findPostUser(dto);
       return success(list);

    }


}
