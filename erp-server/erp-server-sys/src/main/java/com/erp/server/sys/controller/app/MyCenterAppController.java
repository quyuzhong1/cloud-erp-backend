package com.erp.server.sys.controller.app;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.erp.model.sys.dto.SysUserBaseDTO;
import com.erp.model.sys.dto.SysUserThirdDTO;
import com.erp.model.sys.dto.UpdatePasswordDTO;
import com.erp.model.sys.dto.UserBaseDTO;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 个人中心app端
 *
 * @Classname

 * @Date 2025-09-12
 * @Created jack
 */
@RestController
@LogSystemModule("个人中心app端")
@RequestMapping("personalCenter")
public class MyCenterAppController extends BaseController {

    @Autowired
    private SysUserThirdService sysUserThirdService;


    @Autowired
    private SysUserInfoService sysUserInfoService;


    @RequestMapping("/myCenter")
    public ApiResult myCenter() {
        UserBaseDTO loginUser = sysUserInfoService.myCenter();
        return success(loginUser);
    }

    /**
     * 个人中心 上传头像【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "上传头像")
    @PostMapping("/uploadHeadPhoto")
    public ApiResult uploadHeadPhoto(@RequestParam(value = "headPhotoFile") MultipartFile headPhotoFile) {
        Boolean result = sysUserInfoService.uploadHeadPhoto(headPhotoFile);
        return result?success():failure();
    }


}
