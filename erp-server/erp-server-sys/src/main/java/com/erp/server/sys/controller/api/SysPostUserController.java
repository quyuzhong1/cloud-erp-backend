package com.erp.server.sys.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.core.enums.LogActionEnum;
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

 * @Date 2022-07-12 17:07
 * @Created by yl
 */
@RestController
@LogSystemModule("岗位管理")
@RequestMapping("postUser")
public class SysPostUserController  extends BaseController {

    @Autowired
    private SysPostUserService sysPostUserService;

    @LogAction(value = LogActionEnum.INSERT, desc = "批量添加岗位人员")
    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody BatchSavePostUserDTO dto){
        boolean flag= sysPostUserService.saveBatchPostUser(dto);
        return flag==true?success():failure();
    }

    @LogAction(value = LogActionEnum.DELETE, desc = "批量移除岗位人员")
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
