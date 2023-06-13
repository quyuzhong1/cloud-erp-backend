package com.erp.server.sys.controller.feign;

import com.common.business.constant.UserStateConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.sys.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname SysAdminUserFeignController
 * @Description TODO
 * @Date 2022-07-08 17:06
 * @Created by yl
 */
@RestController
@RequestMapping("feign/user")
public class SysUserFeignController extends BaseController {

    @Autowired
    private SysUserInfoService sysUserInfoService;

    @Autowired
    private SysRoleUserService sysRoleUserService;

    @Autowired
    private SysUserThirdService sysUserThirdService;

    @Autowired
    private SysDepartmentService sysDepartmentService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SyncKingdeeService syncKingdeeService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private UserKingdeePostService userKingdeePostService;



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
        if (UserStateConstants.USER_DISABLE.equals(info.getUserState())) {
            return failure(ApiError.ERROR_9016, null);
        }
        return success(info);
    }

    @PostMapping("/findList")
    public ApiResult<List<FindUserDTO>> findList(@RequestBody @Validated BaseSearchDTO dto) {
        List<FindUserDTO> list = sysUserInfoService.getUserList(dto);
        return success(list);
    }

    @PostMapping("/findAuthorityList")

    public ApiResult<List<FindUserDTO>> findAuthorityList(@RequestBody @Validated BaseSearchDTO dto) {
        List<FindUserDTO> list = sysUserInfoService.getAuthorityUserList(dto);
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
     * 查询左菜单栏
     * @param roleIds
     * @return
     */
    @PostMapping("/findLeftMenuByRoleIds")
    public List<SysMenuVO> findLeftMenuByRoleIds(@RequestBody List<String> roleIds) {
        return sysRoleMenuService.findLeftMenuByRoleIds(roleIds);
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

    /**
     * 根据第三方平台 获取对应的 UnionId集合
     *
     * @return
     */
    @PostMapping("/getThirdUnionId")
    public List<ThirdUnionDTO> getThirdUnionId(@RequestBody String platform) {
        List<ThirdUnionDTO> thirdUnionIds = sysUserThirdService.getUnionByPlatform(platform);
        return thirdUnionIds;
    }

    /**
     * 根据用户ids获取用户list
     *
     * @return
     */
    @PostMapping("/getUserListByUserIds")
    public List<FindUserDTO> getUserListByUserIds(@RequestBody List<String> userIds) {
        List<FindUserDTO> list = sysUserInfoService.getUserListByUserIds(userIds);
        return list;
    }

    /**
     * 根据用户id获取用户
     *
     * @return
     */
    @PostMapping("/getUserByUserId")
    public FindUserDTO getUserByUserId(@RequestBody String userId) {
        FindUserDTO dto = sysUserInfoService.getUserByUserId(userId);
        return dto;
    }

    /**
     * 根据用户名称获取用户
     *
     * @return
     */
    @PostMapping("/getUserByUserName")
    public FindUserDTO getUserByUserName(@RequestBody String userName) {
        FindUserDTO dto = sysUserInfoService.getUserByUserName(userName);
        return dto;
    }

    /**
     * 根据用户名称获取用户
     *
     * @return
     */
    @PostMapping("/listUserByUserNames")
    public List<FindUserDTO> listUserByUserNames(@RequestBody List<String> userNames) {
        List<FindUserDTO> list = sysUserInfoService.listUserByUserNames(userNames);
        return list;
    }

    /**
     * 获取所有用户所在的部门
     * @Author Luo_WG
     * @Date 2022/12/13 17:12
     * @return java.util.List<com.erp.model.sys.dto.SysUserDeptDTO>
     **/
    @PostMapping("/getUserDeptList")
    public List<SysUserDeptDTO> getUserDeptList() {
        return sysUserInfoService.getUserDeptList();
    }

    /**
     * @description: 根据部门id查询部门
     * @author Will
     * @date: 2022/12/15 16:22
     * @param deptId
     * @return SysDepartmentDTO
     */
    @PostMapping("/getUserDeptById")
    public SysDepartmentDTO getUserDeptById(@RequestBody String deptId) {
        return sysDepartmentService.getDepartmentById(deptId);
    }

    /**
     * @description: 根据用户id查询所有上级用户
     * @author Will
     * @date: 2023/1/9 10:24
     * @param userIds
     * @return List<UserDTO>
     */
    @PostMapping("/listSuperiorByUserIds")
    public List<UserSuperiorDTO> listSuperiorByUserIds(@RequestBody List<String> userIds) {
        return sysUserInfoService.listSuperiorByUserIds(userIds);
    }

    /**
     * @description: 根据角色ids查询名称
     * @author Will
     * @date: 2023/1/9 11:36
     * @param roleIds
     * @return List<String>
     */
    @PostMapping("/listRoleByIds")
    public List<String> listRoleByIds(@RequestBody List<String> roleIds) {
        return sysRoleService.listRoleByIds(roleIds);
    }

    /**
     * @description: 根据用户ids查询角色
     * @author Will
     * @date: 2023/1/9 11:36
     * @param userIds
     * @return List<String>
     */
    @PostMapping("/listRoleByUserIds")
    public List<SysRoleDTO> listRoleByUserIds(@RequestBody List<String> userIds) {
        return sysRoleService.listRoleByUserIds(userIds);
    }


    @PostMapping("/getSysUserById")
    public SysUserDTO getSysUserById(@RequestBody String userId) {
        return sysUserInfoService.getSysUserById(userId);
    }

    /**
     * @param params
     * @description: 更新业务状态
     * @author Will
     * @date: 2023/3/10 15:46
     */
    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params) {
        syncKingdeeService.updateBusinessSyncKingdeeStatus(params);
    }

    /**
     * 根据第三方平台和用户id 获取对应的 UnionId
     *
     * @return
     */
    @PostMapping("/getThirdUnionIdsByUserIds")
    public List<ThirdUnionDTO> getThirdUnionIdsByUserIds(@RequestParam(value = "platform") String platform, @RequestParam(value = "userIds") List<String> userIds) {
        return sysUserThirdService.getUnionByPlatformAndUserIds(platform, userIds);
    }

    /**
     * 批量获取用户基本信息，如手机号码，名字，邮箱（过滤掉禁用的用户）
     *
     * @return
     */
    @PostMapping("/getUserSimpleInfoByIds")
    public List<SysUserSimpleDTO> getUserSimpleInfoByIds(@RequestParam(value = "userIds") List<String> userIds) {
        return sysUserInfoService.getUserSimpleInfoByIds(userIds);
    }

    /**
     * 根据角色id获取对应用户列表
     */
    @PostMapping("/getUserListByRoleIds")
    public List<FindUserDTO> getUserListByRoleIds(@RequestBody SysFeignDTO.ListByRoleIdsDTO dto) {
        return sysUserInfoService.getUserListByRoleIds(dto);
    }


    /**
     * 根据用戶id 获取金蝶的对应岗位code
     * @author yl
     * @date 2023-06-05 10:08
     * @param userId
     * @return com.erp.model.sys.dto.KingdeePostDTO.UserKingdeePostInfoDTO
     */
    @PostMapping("/getUserKingdeePostByUserId")
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePostByUserId(@RequestBody String userId) {
        return userKingdeePostService.getUserKingdeePostByUserId(userId);
    }

    @PostMapping("/listUserKingdeePostByUserIds")
    public List<KingdeePostDTO.UserKingdeePostInfoDTO> listUserKingdeePostByUserIds(@RequestBody List<String> userIds) {
        return userKingdeePostService.listUserKingdeePostByUserIds(userIds);
    }
}
