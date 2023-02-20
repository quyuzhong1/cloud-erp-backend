package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.erp.model.sys.dto.SysUserThirdDTO;
import com.erp.model.sys.dto.SysUserBaseDTO;
import com.erp.model.sys.dto.UpdatePasswordDTO;
import com.erp.model.sys.dto.UserBaseDTO;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Classname MyCenterController
 * @Description TODO
 * @Date 2022-08-02 14:42
 * @Created by yl
 */
@RestController
@RequestMapping("sys/personalCenter")
public class MyCenterController extends BaseController {

    @Autowired
    private SysUserThirdService sysUserThirdService;




    @Autowired
    private SysUserInfoService sysUserInfoService;





    @RequestMapping("/bindingThirdParty")
    public ApiResult binding(@RequestBody SysUserThirdDTO dto) {
        sysUserInfoService.bindingThirdParty(dto);
        return success();
    }


    @RequestMapping("/updateBase")
    public ApiResult updateBase(@Valid @RequestBody SysUserBaseDTO dto) {
        sysUserInfoService.updateBase(dto);
        return success();
    }


    @RequestMapping("/myCenter")
    public ApiResult myCenter() {
        UserBaseDTO loginUser= sysUserInfoService.myCenter();
        return success(loginUser);
    }

    @RequestMapping("/removeThirdParty")
    public ApiResult removeThirdParty(String bindingThird) {
        boolean flag=sysUserThirdService.removeThirdParty(bindingThird);
        return flag==true?success():failure();
    }


    @RequestMapping("/updatePassword")
    public ApiResult updatePassword(@RequestBody @Validated UpdatePasswordDTO updatePasswordDTO) {
        sysUserInfoService.updatePassword(updatePasswordDTO);
        return success();
    }

    @RequestMapping("/bindingEmail")
    public ApiResult bindingEmail(@RequestBody EmailVerifyCodeDTO dto) {
        sysUserInfoService.bindingEmail(dto);
        return success();
    }

    @RequestMapping("/sedEmail")
    public ApiResult sedEmail(@RequestBody EmailVerifyCodeDTO dto) {
        sysUserInfoService.sedEmail(dto);
        return success();
    }

    @RequestMapping("/removeEmail")
    public ApiResult sedEmail() {
        sysUserInfoService.removeEmail();
        return success();
    }






}
