package com.erp.server.sys.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
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
@RequestMapping("user")
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
        List<UserDTO> list = sysUserInfoService.findList(dto);
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
     * 修改用户
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
        sysUserInfoService.deleteByIds(uids);
        sysUserThirdService.deleteByUserIds(uids);
        return success();
    }

    @RequestMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        sysUserInfoService.updateState(stateDTO);
        return success();
    }


}
