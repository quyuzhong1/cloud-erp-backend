package com.cloud.erp.admin.modules.sys.controller;


import com.cloud.erp.admin.modules.sys.dto.*;
import com.cloud.erp.admin.modules.sys.entity.SysUserInfoEntity;
import com.cloud.erp.admin.modules.sys.service.SysUserInfoService;
import com.cloud.erp.admin.modules.sys.service.SysUserThirdService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import com.erp.common.dto.PagingDTO;
import com.erp.common.modules.sys.dto.SysUserThirdDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-07 18:28:29
 */
@RestController
@RequestMapping("sys/user")
public class SysUserInfoController extends BaseController {

    @Autowired
    private SysUserInfoService sysUserInfoService;

    @Autowired
    private SysUserThirdService sysUserThirdService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public ApiResult list(@RequestBody SysSearchUserDTO dto) {
        List<SysUserVO> list = sysUserInfoService.findList(dto);
        return success(list);
    }


    @RequestMapping("/paging")
    public ApiResult list(@RequestBody @Validated PagingDTO<SysUserPagingSearchDTO> dto) {
        PagingVO pagingVO = sysUserInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{uid}")
    public ApiResult info(@PathVariable("uid") Long uid) {
        SysUserInfoEntity sysUserInfo = sysUserInfoService.getById(uid);

        return success(sysUserInfo);
    }

    /**
     * 添加用户
     */
    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoService.add(sysUserInfoDTO);
        return success();
    }

    /**
     * 添加用户
     */
    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoService.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 删除
     */
    @RequestMapping("/remove")
    public ApiResult delete(@RequestBody List<String> uids) {
        sysUserInfoService.removeByIds(uids);
        return success();
    }

    @RequestMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        sysUserInfoService.updateState(stateDTO);
        return success();
    }

    @RequestMapping("/bindingThirdParty")
    public ApiResult binding(@RequestBody SysUserThirdDTO dto) {
        sysUserInfoService.bindingThirdParty(dto);
        return success();
    }


    @RequestMapping("/myCenter")
    public ApiResult myCenter() {
        LoginUser loginUser= sysUserInfoService.myCenter();
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



}
