package com.erp.server.sys.controller.api;


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
 * 个人中心
 *
 * @Classname

 * @Date 2022-08-02 14:42
 * @Created by yl
 */
@RestController
@LogSystemModule("个人中心")
@RequestMapping("personalCenter")
public class MyCenterController extends BaseController {

    @Autowired
    private SysUserThirdService sysUserThirdService;


    @Autowired
    private SysUserInfoService sysUserInfoService;


    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "账号绑定第三方平台：平台={bindingPlatform}")
    @RequestMapping("/bindingThirdParty")
    public ApiResult binding(@RequestBody SysUserThirdDTO dto) {
        sysUserInfoService.bindingThirdParty(dto);
        return success();
    }


    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改基础信息：真实名={realName}")
    @RequestMapping("/updateBase")
    public ApiResult updateBase(@Valid @RequestBody SysUserBaseDTO dto) {
        sysUserInfoService.updateBase(dto);
        return success();
    }


    @RequestMapping("/myCenter")
    public ApiResult myCenter() {
        UserBaseDTO loginUser = sysUserInfoService.myCenter();
        return success(loginUser);
    }

//    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "账号移除绑定第三方平台：平台={bindingThird}")
    @RequestMapping("/removeThirdParty")
    public ApiResult removeThirdParty(@RequestParam(value = "bindingThird") String bindingThird) {
        boolean flag = sysUserThirdService.removeThirdParty(bindingThird);
        return flag == true ? success() : failure();
    }


    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "账号更新密码")
    @RequestMapping("/updatePassword")
    public ApiResult updatePassword(@RequestBody @Validated UpdatePasswordDTO updatePasswordDTO) {
        sysUserInfoService.updatePassword(updatePasswordDTO);
        return success();
    }


    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "账号绑定邮箱:邮箱={email}")
    @RequestMapping("/bindingEmail")
    public ApiResult bindingEmail(@RequestBody EmailVerifyCodeDTO dto) {
        sysUserInfoService.bindingEmail(dto);
        return success();
    }

    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "账号发送邮箱:邮箱={email}")
    @RequestMapping("/sedEmail")
    public ApiResult sedEmail(@RequestBody EmailVerifyCodeDTO dto) {
        sysUserInfoService.sendEmail(dto);
        return success();
    }

    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "账号移除邮箱:邮箱={email}")
    @RequestMapping("/removeEmail")
    public ApiResult removeEmail() {
        sysUserInfoService.removeEmail();
        return success();
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
