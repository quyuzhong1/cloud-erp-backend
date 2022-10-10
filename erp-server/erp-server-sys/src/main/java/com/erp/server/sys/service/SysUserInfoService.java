package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.modules.email.dto.EmailVerifyCodeDTO;
import com.erp.common.modules.sys.dto.*;
import com.erp.common.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;

import java.util.List;

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
}

