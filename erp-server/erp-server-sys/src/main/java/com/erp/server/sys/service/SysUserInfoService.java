package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-07 18:28:29
 */
public interface SysUserInfoService extends IService<SysUserInfoEntity> {




    /**
     * 保存用户
     *
     * @param sysUserInfoDTO
     */
    void add(SysUserInfoDTO sysUserInfoDTO);

    /**
     * 保存SRM用户
     *
     * @param sysUserInfoDTO
     */
    String addSrmUser(SysUserInfoDTO sysUserInfoDTO);
    /**
     * 修改用户
     *
     * @param sysUserInfoDTO
     */
    void update(SysUserInfoDTO sysUserInfoDTO);
    /**
     * 修改用户
     *
     * @param sysUserInfoDTO
     */
    Boolean updateSrmUser(SysUserInfoDTO sysUserInfoDTO);
    /**
     * 账号登录
     *
     * @param dto
     * @return
     */
    SysUserDTO accountLogin(AccountLoginDTO dto);

    /**
     * 根据关键字获取用户列表
     * @author yl
     * @date 2022-07-12 18:13
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysUserVO>
     */

    List<UserDTO> findList(SysSearchUserDTO dto);

    PagingVO<UserManageDTO> paging(PagingDTO<SysUserPagingSearchDTO> dto);

    void bindingThirdParty(SysUserThirdDTO dto);

    void updateState(UpdateUserStateDTO stateDTO);

    void setLoginIp(SysLoginIpDTO dto);

    void updatePassword(UpdatePasswordDTO updatePasswordDTO);

    SysUserDTO scanCodeLogin(SysUserThirdDTO dto);

    UserBaseDTO myCenter();

    void updateBase(SysUserBaseDTO dto);

    void bindingEmail(EmailVerifyCodeDTO dto);

    void sendEmail(EmailVerifyCodeDTO dto);

    void removeEmail();

    List<FindUserDTO> getUserList(BaseSearchDTO dto);

    List<FindUserDTO> getAllUserList();

    List<UserRequestPermissionsDTO> getRequestPermissionsList(String userId);

    List<String> getDepUserList(String userId);

    List<FindUserDTO> getUserListByUserIds(List<String> userIds);

    FindUserDTO  getUserByUserId(String userId);

    FindUserDTO getUserByUserName(String userName,String userType);

    List<FindUserDTO> listUserByUserNames(List<String> userNames,String userType);

    List<FindUserDTO> getAuthorityUserList(BaseSearchDTO dto);

    List<SysUserDeptDTO> getUserDeptList();
    /**
     * @description: 查询人员所有上级
     * @author Will
     * @date: 2023/1/9 9:24
     * @param userIds
     * @return List<UserSuperiorDTO>
     */
    List<UserSuperiorDTO> listSuperiorByUserIds(List<String> userIds);

    /**
     * 获取用户上级部门
     * @param userIds
     * @return
     */
    List<UserSuperiorDTO> listDeptByUserIds(List<String> userIds);

    /**
     * 根据用户id 获取用户信息
     * @author yl
     * @date 2023-01-29 10:25
     * @param userId
     * @return com.erp.model.sys.dto.SysUserDTO
     */
    SysUserDTO getSysUserById(String userId);

    /**
     * @description: 更新金蝶发送状态
     * @author Will
     * @date: 2023/4/10 14:24
     * @param id
     * @param syncKingdeeId
     * @return
     */
    boolean updateSyncKingdeeId(String id, String syncKingdeeId);
    /**
     * @description: 批量删除
     * @author Will
     * @date: 2023/4/11 18:05
     * @param uids
     */
    void deleteByIds(List<String> uids);

    /**
     * 重置密码
     * @Author Luo_WG
     * @Date 2023/4/20 9:46
     * @param uid 用户id
     * @return java.lang.Boolean
     **/
    Boolean changePassword(String uid);
    /**
     * 重置密码
     * @Author Luo_WG
     * @Date 2023/4/20 9:46
     * @param uid 用户id
     * @return java.lang.Boolean
     **/
    Boolean changePassword(String uid,String pwd);
    /**
     * 忘记密码
     * @Author Luo_WG
     * @Date 2023/4/20 11:18
     * @param forgotPasswordDTO forgotPasswordDTO
     * @return java.lang.Boolean
     **/
    Boolean forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    /**
     * 忘记密码-获取验证码
     * @Author Luo_WG
     * @Date 2023/4/20 11:45
     * @param userAccount userAccount
     * @return com.common.core.controller.vo.ApiResult
     **/
    Map<String,Object> forgotPasswordGetCode(String userAccount,String userType);

    /**
     * 通过手机号和用户类型获取验证码
     **/
    Map<String,Object> getCodeByAccountAndType(String phone,String userType);

    /**
     * 批量获取用户基本信息，如手机号码，名字，邮箱（过滤掉禁用的用户）
     * @param userIds
     * @return
     */
    List<SysUserSimpleDTO> getUserSimpleInfoByIds(List<String> userIds);

    /**
     * 根据角色id获取用户列表
     * @param dto
     * @return
     */
    List<FindUserDTO> getUserListByRoleIds(SysFeignDTO.ListByRoleIdsDTO dto);
    /**
     * 根据搜索关键字 获取到用户信息
     * @author yl
     * @date 2023-04-26 18:14
     * @param searchKeyword
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    List<FindUserDTO> listBySearchKeyword(String searchKeyword);


    /**
     * 上传头像
     * @param headPhotoFile
     * @return
     */
    Boolean uploadHeadPhoto(MultipartFile headPhotoFile);

    /**
     * 根据金蝶code获取用户信息
     * @author yl
     * @date 2023-06-27 15:31
     * @param kingdeeCodeList
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    List<FindUserDTO> listUserByKingdeeCode(List<String> kingdeeCodeList);

    /**
     * 导入用户金蝶
     * @param file
     * @throws IOException
     */
    void importUserKingdee(MultipartFile file) throws IOException;

    /**
     * 根据部门名称查询用户
     * @Author Luo_WG
     * @Date 2023/7/20 15:52
     * @param deptName
     * @return java.util.List<com.erp.model.sys.entity.SysUserInfoEntity>
     **/
    List<SysUserInfoEntity> listUserByDept(String deptName);
    /**
     * @description: 店铺权限设置分页查询
     * @author Will
     * @date: 2023/9/4 12:21
     * @param dto
     * @return PagingVO
     */
    PagingVO shopAuthPaging(PagingDTO<SysUserInfoDTO.ShopAuthPagingSearchDTO> dto);
    /**
     * @description: 更新用户更新时间
     * @author Will
     * @date: 2023/9/13 16:59
     * @param userIdList
     */
    void updateSysUserTime(List<String> userIdList);

    /**
     *  供应商协同用户查询
     * @param dto
     * @return
     */
    PagingVO<SupplierUserVO> srmPaging(PagingDTO<UserPagingSearchDTO> dto);

    /**
     * 根据用户类型和手机号获取用户信息
     * @param mobile
     * @param userType
     * @return
     */
    FindUserDTO getUserByMobile(String mobile, String userType);

    /**
     * 列表查询
     * @param dto
     * @return
     */
    List<SupplierUserVO> srmList(UserPagingSearchDTO dto);

    void updateStateSrm(UpdateUserStateDTO stateDTO);

    List<SysUserInfoEntity> listErpUser();
    /**
     * 远程搜索
     *
     * @param dto
     * @return ApiResult
     */
    PagingVO<UserSelectDto.PageSelectDTO> pagingSelect(PagingDTO<UserSelectDto.SelectDTO> dto);

    void syncFsUser();

    /**
     * 通过App-Id获取飞书用户UnionId
     * 通过App-Id从sys_referer_config表获取配置信息，然后调用FsService获取用户unionId
     *
     * @param appId 应用ID
     * @param dto   查找第三方用户DTO
     * @return 用户UnionId
     */
    String getFsUserUnionIdByAppId(String appId, FindThirdUserDTO dto);
}

