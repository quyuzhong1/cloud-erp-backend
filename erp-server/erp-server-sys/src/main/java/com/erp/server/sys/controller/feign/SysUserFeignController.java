package com.erp.server.sys.controller.feign;

import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.constant.UserStateConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysRoleMenuEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.sys.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname SysAdminUserFeignController

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
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SyncKingdeeService syncKingdeeService;

    @Resource
    private AuthUserShopService authUserShopService;
    @Resource
    private AuthUserWarehouseService authUserWarehouseService;

    @Resource
    private SysUserWechatService wechatService;

    @Resource
    private UserDatePermissionService userDatePermissionService;
    @Resource
    private RedisService redisService;

    @PostMapping("/accountLogin")
    public ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO dto) {
        SysUserDTO info = sysUserInfoService.accountLogin(dto);
        if (Objects.isNull(info)) {
            return failure(ApiError.ERROR_9012, null);
        }
        if (UserStateConstants.USER_DISABLE.equals(info.getUserState())) {
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

    /**
     * 远程搜索
     *
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<UserSelectDto.PageSelectDTO>> pagingSelect(@RequestBody PagingDTO<UserSelectDto.SelectDTO> dto) {
        return success(sysUserInfoService.pagingSelect(dto));
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
     * 根据用户ID获取用户完整登录信息（包含权限和菜单）
     *
     * @param dto 用户登录信息请求DTO
     * @return 用户信息（包含permissionList和leftMenuList）
     */
    @PostMapping("/getUserLoginInfo")
    public ApiResult<SysUserDTO> getUserLoginInfo(@RequestBody @Validated SysFeignDTO.UserLoginInfoDTO dto) {
        String userId = dto.getUserId();
        String userType = dto.getUserType();
        
        // 1. 获取用户基本信息
        FindUserDTO userByUserId = sysUserInfoService.getUserByUserId(userId);
        if (userByUserId == null) {
            return failure(ApiError.USER_NOT_EXIST, null);
        }
        
        SysUserDTO sysUserDTO = new SysUserDTO();
        org.springframework.beans.BeanUtils.copyProperties(userByUserId, sysUserDTO);
        
        // 2. 获取用户角色ID列表
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(userId);
        
        // 3. 根据角色获取菜单和权限（参考accountLogin方法）
        List<com.erp.model.sys.vo.SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds, userType);
        List<com.erp.model.sys.vo.SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds, MathUtil.ONE, userType);
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.NO_STATE, userType);
        
        // 4. 设置到返回对象
        sysUserDTO.setPermissionList(permissionList);
        sysUserDTO.setOverallMenuList(overallMenuList);
        sysUserDTO.setLeftMenuList(leftMenuList);
        
        return success(sysUserDTO);
    }

    /**
     * 更新用户更新时间
     * @author Will
     * @date: 2023/9/13 16:59
     */
    @PostMapping("/updateSysUserTime")
    public void updateSysUserTime(@RequestBody List<String> userIdList) {
      sysUserInfoService.updateSysUserTime(userIdList);
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
            Integer userState = userEntity.getUserState();
            if (SysConstant.YES_STATE.equals(userState)) {
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
     * 根据用户code集合获取用户list
     * return
     */
    @PostMapping("/listUserByCodeList")
    public List<FindUserDTO> listUserByCodeList(@RequestBody List<String> codeList) {
        List<FindUserDTO> list = sysUserInfoService.listUserByKingdeeCode(codeList);
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
    @GetMapping("/getUserByUserName")
    public FindUserDTO getUserByUserName(@RequestParam("userName") String userName,@RequestParam("userType") String userType) {
        FindUserDTO dto = sysUserInfoService.getUserByUserName(userName,userType);
        return dto;
    }

    /**
     * 根据手机号和用户类型获取用户信息
     * @param mobile
     * @param userType
     * @return
     */
    @GetMapping("/getUserByMobile")
    FindUserDTO getUserByMobile(@RequestParam("mobile") String mobile,@RequestParam("userType") String userType){
        FindUserDTO dto = sysUserInfoService.getUserByMobile(mobile,userType);
        return dto;
    }
    /**
     * 根据用户名称获取用户
     *
     * @return
     */
    @GetMapping("/listUserByUserNames")
    public List<FindUserDTO> listUserByUserNames(@RequestParam("userNames") List<String> userNames,@RequestParam("userType") String userType) {
        List<FindUserDTO> list = sysUserInfoService.listUserByUserNames(userNames,userType);
        return list;
    }

    /**
     * 获取所有用户所在的部门
     *
     * @return java.util.List<com.erp.model.sys.dto.SysUserDeptDTO>
     * @Author Luo_WG
     * @Date 2022/12/13 17:12
     **/
    @PostMapping("/getUserDeptList")
    public List<SysUserDeptDTO> getUserDeptList() {
        return sysUserInfoService.getUserDeptList();
    }

    /**
     * @param deptId
     * @return SysDepartmentDTO
     * @description: 根据部门id查询部门
     * @author Will
     * @date: 2022/12/15 16:22
     */
    @PostMapping("/getUserDeptById")
    public SysDepartmentDTO getUserDeptById(@RequestBody String deptId) {
        return sysDepartmentService.getDepartmentById(deptId);
    }

    /**
     * 根据部门金蝶Code查询部门
     * @Author Luo_WG
     * @Date 2023/6/27 14:12
     * @param code
     * @return com.erp.model.sys.dto.SysDepartmentDTO
     **/
    @PostMapping("/getUserDeptByCode")
    public SysDepartmentDTO getUserDeptByCode(@RequestBody String code) {
        return sysDepartmentService.getUserDeptByCode(code);
    }

    /**
     * @param userIds
     * @return List<UserDTO>
     * @description: 根据用户id查询所有上级用户
     * @author Will
     * @date: 2023/1/9 10:24
     */
    @PostMapping("/listSuperiorByUserIds")
    public List<UserSuperiorDTO> listSuperiorByUserIds(@RequestBody List<String> userIds) {
        return sysUserInfoService.listSuperiorByUserIds(userIds);
    }
    /**
     * @param userIds
     * @return List<UserDTO>
     * @description: 根据用户id查询所有上级用户
     * @author Will
     * @date: 2023/1/9 10:24
     */
    @PostMapping("/listDeptByUserIds")
    public List<UserSuperiorDTO> listDeptByUserIds(@RequestBody List<String> userIds) {
        return sysUserInfoService.listDeptByUserIds(userIds);
    }

    /**
     * @param roleIds
     * @return List<String>
     * @description: 根据角色ids查询名称
     * @author Will
     * @date: 2023/1/9 11:36
     */
    @PostMapping("/listRoleByIds")
    public List<String> listRoleByIds(@RequestBody List<String> roleIds) {
        return sysRoleService.listRoleByIds(roleIds);
    }

    /**
     * @param userIds
     * @return List<String>
     * @description: 根据用户ids查询角色
     * @author Will
     * @date: 2023/1/9 11:36
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
     * 根据第三方平台和用户id获取对应的第三方信息
     * @return
     */
    @PostMapping("/getThirdByUserIds")
    public List<ThirdUnionDTO> getThirdByUserIds(@RequestParam(value = "platform") String platform, @RequestParam(value = "userIds") List<String> userIds) {
        return sysUserThirdService.getThirdByUserIds(platform, userIds);
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
     * 根据用戶ids 获取金蝶的对应岗位code
     *
     * @return
     */
    @PostMapping("/listUserKingdeePostByUserIds")
    public List<KingdeePostDTO.UserKingdeePostInfoDTO> listUserKingdeePostByUserIds(@RequestBody List<String> userIds) {
       // return userKingdeePostService.listUserKingdeePostByUserIds(userIds);
        return null;
    }



    /**
     * 获取到第三方绑定的用户
     *
     * @return
     */
    @GetMapping("/listThirdBindUser")
    public List<FindUserDTO> listThirdBindUser() {
        return sysUserThirdService.listThirdBindUser();
    }

    /**
     * 根据金蝶code 获取到用户信息
     *
     * @return
     */
    @GetMapping("/listUserByKingdeeCode")
    public List<FindUserDTO> listUserByKingdeeCode(@RequestBody List<String> kingdeeCodeList) {
        return sysUserInfoService.listUserByKingdeeCode(kingdeeCodeList);
    }

    /**
     * 根据角色id获取角色菜单
     * @param roleIdList
     * @return
     */
    @PostMapping("/getMenuRefRoleByRoleIds")
    public List<SysRoleMenuEntity> getMenuRefRoleByRoleIds(@RequestBody List<String> roleIdList) {
        return sysRoleMenuService.getMenuRefRoleByRoleIds(roleIdList);
    }

    /**
     * 根据部门名称查询用户
     * @Author Luo_WG
     * @Date 2023/7/20 15:49
     * @param deptName
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.FindUserDTO>>
     **/
    @PostMapping("/listUserByDept")
    public List<SysUserInfoEntity> listUserByDept(@RequestBody String deptName) {
        return sysUserInfoService.listUserByDept(deptName);
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/srmPaging")
    public PagingVO<SupplierUserVO> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto){
        return sysUserInfoService.srmPaging(dto);
    }
    /**
     * 查询
     * @param dto
     * @return
     */
    @PostMapping("/srmList")
    public List<SupplierUserVO> srmList(@RequestBody @Validated UserPagingSearchDTO dto){
        return sysUserInfoService.srmList(dto);
    }

    /**
     * 添加用户
     */
    @PostMapping("/addSrmUser")
    public String addSrmUser(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        return sysUserInfoService.addSrmUser(sysUserInfoDTO);
    }

    /**
     * 修改用户
     */
    @PostMapping("/updateSrmUser")
    public Boolean updateSrmUser(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        return sysUserInfoService.updateSrmUser(sysUserInfoDTO);
    }


    /**
     * 用户信息
     */
    @GetMapping("/info/{uid}")
    public SysUserInfoEntity info(@PathVariable("uid") String uid) {
        return sysUserInfoService.getById(uid);
    }


    /**
     * 删除
     */
    @PostMapping("/deleteSrmUser")
    public ApiResult deleteSrmUser(@RequestBody List<String> uids) {
        sysUserInfoService.removeByIds(uids);
        uids.forEach(uid ->{
            redisService.deleteObject(RedisCacheConstants.LOGIN_TOKEN_KEY + uid);
        });
        return success();
    }

    /**
     * 批量启用/禁用
     * @param stateDTO
     * @return
     */
    @PostMapping("/updateStateSrm")
    public ApiResult updateStateSrm(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        sysUserInfoService.updateStateSrm(stateDTO);
        return success();
    }

    /**
     * 重置密码
     * @Author Luo_WG
     * @Date 2023/4/20 9:43
     * @param
     * @return
     **/
    @DataIdempotent(keyIdName = "uid")
    @GetMapping("/changePassword")
    public ApiResult changePassword(@RequestParam("uid") String uid,@RequestParam("pwd") String pwd) {
        Boolean flag = sysUserInfoService.changePassword(uid,pwd);
        return flag == true ? success() : failure();
    }

    /**
     * 获取用户微信信息
     **/
    @GetMapping("/getWxInfo")
    public SysUserWechatEntity getWxInfo(@RequestParam("uid") String uid) {
        return wechatService.getWxInfo(uid);
    }


    /**
     * 查询用户数据权限,获取到权限sql
     * @Author Luo_WG
     * @Date 2024/3/12 16:45
     * @param tableField 权限过滤字段
     * @param menuCode 菜单编号
     * @return java.lang.String
     **/
    @GetMapping("/getUserDatePermissionSql")
    public String getUserDatePermissionSql(@RequestParam("tableField") String tableField, @RequestParam("menuCode") String menuCode) {
        return userDatePermissionService.getUserDatePermissionSql(tableField, menuCode);
    }
    /**
     * 根据菜单cdoe查询用户数据权限
     * @author hyj
     * @date 2024/5/9 10:43
     * @param menuCode 菜单编号
     * @return Boolean 是否有权限
     **/
    @GetMapping("/getUserDatePermissionByMenuCode")
    public Boolean getUserDatePermissionByMenuCode(@RequestParam("menuCode") String menuCode) {
        return userDatePermissionService.getUserDatePermissionByMenuCode( menuCode);
    }


    /**
     * 根据第三方平台和id查询用户
     **/
    @PostMapping("/getUserByThird")
    public SysUserThirdEntity getUserByThird(@RequestParam(value = "platform") String platform, @RequestParam(value = "thirdId")String thirdId)  {
        return sysUserThirdService.getUserByThird( platform,thirdId);
    }

    /**
     * 根据第三方平台和id查询用户
     **/
    @PostMapping("/getUserByThirdIdList")
    public List<SysUserThirdEntity>  getUserByThirdIdList(@RequestParam(value = "platform") String platform, @RequestParam(value = "thirdIds") ArrayList<String> thirdIds)  {
        return sysUserThirdService.getUserByThirdIdList( platform,thirdIds);
    }

    /**
     * 通过App-Id获取飞书用户UnionId
     * 通过App-Id从sys_referer_config表获取配置信息，然后调用FsService获取用户unionId
     *
     * @param appId 应用ID
     * @param dto   查找第三方用户DTO
     * @return 用户UnionId
     */
    @PostMapping("/getFsUserUnionIdByAppId")
    public ApiResult<String> getFsUserUnionIdByAppId(@RequestParam("appId") String appId, @RequestBody FindThirdUserDTO dto) {
        try {
            String unionId = sysUserInfoService.getFsUserUnionIdByAppId(appId, dto);
            return ApiResult.success(unionId);
        } catch (Exception e) {
            return ApiResult.error(500, "获取飞书用户UnionId失败：" + e.getMessage());
        }
    }
}
