package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;

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
     * 修改用户
     *
     * @param sysUserInfoDTO
     */
    void update(SysUserInfoDTO sysUserInfoDTO);

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

    PagingVO paging(PagingDTO<SysUserPagingSearchDTO> dto);

    void bindingThirdParty(SysUserThirdDTO dto);

    void updateState(UpdateUserStateDTO stateDTO);

    void setLoginIp(SysLoginIpDTO dto);

    void updatePassword(UpdatePasswordDTO updatePasswordDTO);

    SysUserDTO scanCodeLogin(SysUserThirdDTO dto);

    UserBaseDTO myCenter();

    void updateBase(SysUserBaseDTO dto);

    void bindingEmail(EmailVerifyCodeDTO dto);

    void sedEmail(EmailVerifyCodeDTO dto);

    void removeEmail();

    List<FindUserDTO> getUserList(BaseSearchDTO dto);

    List<FindUserDTO> getAllUserList();

    List<UserRequestPermissionsDTO> getRequestPermissionsList(String userId);

    List<String> getDepUserList(String userId);

    List<FindUserDTO> getUserListByUserIds(List<String> userIds);

    FindUserDTO getUserByUserId(String userId);

    FindUserDTO getUserByUserName(String userName);

    List<FindUserDTO> listUserByUserNames(List<String> userNames);

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
     * @param businessIds
     * @param status
     * @param syncKingdeeId
     * @return
     */
    boolean updateSyncKingdeeStatus(List<String> businessIds, String status, String syncKingdeeId);
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
    Boolean resetPassword(String uid);

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
    Map<String,Object> forgotPasswordGetCode(String userAccount);

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

}

