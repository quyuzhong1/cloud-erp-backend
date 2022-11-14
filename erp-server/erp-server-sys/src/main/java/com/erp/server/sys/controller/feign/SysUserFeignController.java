package com.erp.server.sys.controller.feign;

import com.common.core.constant.UserStateConstants;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.modules.sys.dto.*;
import com.erp.model.sys.dto.UserDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.service.SysRoleService;
import com.erp.server.sys.service.SysRoleUserService;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @Classname SysAdminUserFeignController
 * @Description TODO
 * @Date 2022-07-08 17:06
 * @Created by yl
 */
@RestController
@RequestMapping("sys/feign/user")
public class SysUserFeignController extends BaseController {

    @Autowired
    private SysUserInfoService sysUserInfoService;

    @Autowired
    private SysRoleUserService sysRoleUserService;

    @Autowired
    private SysUserThirdService sysUserThirdService;

    @PostMapping("/accountLogin")
    public ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO dto) {
        SysUserDTO info = sysUserInfoService.accountLogin(dto);
        if (Objects.isNull(info)) {
            return failure(ApiError.ERROR_9012, null);
        }
        if (info.getUserState() == UserStateConstants.USER_DISABLE) {
            return failure(ApiError.ERROR_9016, null);
        }
        return success(info);
    }


    @PostMapping("/setLoginIp")
    public ApiResult<SysUserDTO> setLoginIp(@RequestBody SysLoginIpDTO dto) {
        sysUserInfoService.setLoginIp(dto);
        return success();
    }


    @PostMapping("/scanCodeLogin")
    public ApiResult<SysUserDTO> scanCodeLogin(@RequestBody SysUserThirdDTO dto) {
        SysUserDTO info = sysUserInfoService.scanCodeLogin(dto);
        if (Objects.isNull(info)) {
            return failure(ApiError.ERROR_9012, null);
        }
        if (info.getUserState() == UserStateConstants.USER_DISABLE) {
            return failure(ApiError.ERROR_9016, null);
        }
        return success(info);
    }

    @PostMapping("/findList")
    public ApiResult<List<FindUserDTO>> findList(@RequestBody @Validated BaseSearchDTO dto) {
        List<FindUserDTO> list = sysUserInfoService.getUserList(dto);
        return success(list);
    }

    @GetMapping("/getUserList")
    public List<FindUserDTO> getUserList() {
        List<FindUserDTO> list = sysUserInfoService.getAllUserList();
        return list;
    }


    /**
     * 根据用户id 获取 用户的权限标识
     *
     * @return
     */
    @PostMapping("/getRequestPermissionsList")
    public List<UserRequestPermissionsDTO> getRequestPermissionsList(@RequestBody String userId) {
        List<UserRequestPermissionsDTO> list = sysUserInfoService.getRequestPermissionsList(userId);
        return list;
    }

    /**
     * 根据部门id 获取 所有用户
     *
     * @return
     */
    @PostMapping("/getDepUserList")
    public List<String> getDepUserList(@RequestBody String userId) {
        List<String> depUserList = sysUserInfoService.getDepUserList(userId);
        return depUserList;
    }

    /**
     * 根据用户id 获取用户角色的id
     *
     * @return
     */
    @PostMapping("/getRoleIdList")
    public List<String> getRoleIdList(@RequestBody String userId) {
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(userId);
        return roleIds;
    }


    /**
     * 根据第三方绑定的关系 获取用户信息
     *
     * @return
     */
    @PostMapping("/getUserIdByThird")
    public String getUserIdByThird(@RequestBody FindUserByThirdDTO thirdDTO) {
        SysUserInfoEntity userEntity = sysUserThirdService.getUserIdByThird(thirdDTO);
        if (!Objects.isNull(userEntity)) {
            Integer deleteState = userEntity.getDeleteState();
            Integer userState = userEntity.getUserState();
            if (SysConstant.YES_STATE.equals(deleteState) && SysConstant.YES_STATE.equals(userState)) {
                return userEntity.getUid();
            }
        }
        return "";
    }
}
