package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.constant.SysConstant;
import com.cloud.erp.admin.modules.feign.SysAuthFeign;
import com.cloud.erp.admin.modules.feign.ThirdFeign;
import com.cloud.erp.admin.modules.interceptor.SysInterceptor;
import com.cloud.erp.admin.modules.sys.dto.*;
import com.cloud.erp.admin.modules.sys.entity.SysUserInfoEntity;
import com.cloud.erp.admin.modules.sys.entity.SysUserThirdEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysUserInfoMapper;
import com.cloud.erp.admin.modules.sys.service.SysRoleMenuService;
import com.cloud.erp.admin.modules.sys.service.SysRoleUserService;
import com.cloud.erp.admin.modules.sys.service.SysUserInfoService;
import com.cloud.erp.admin.modules.sys.service.SysUserThirdService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.cloud.erp.common.common.ApiError;
import com.cloud.erp.common.common.dto.PagingDTO;
import com.cloud.erp.common.common.exception.ServiceException;
import com.cloud.erp.common.common.token.vo.LoginUser;
import com.cloud.erp.common.common.vo.PagingVO;
import com.cloud.erp.common.constant.ThirdConstants;
import com.cloud.erp.common.dto.AccountLoginDTO;
import com.cloud.erp.common.modules.sys.dto.SysLoginIpDTO;
import com.cloud.erp.common.modules.sys.dto.SysUserDTO;
import com.cloud.erp.common.modules.sys.dto.SysUserThirdDTO;
import com.cloud.erp.common.modules.sys.vo.SysMenuVO;
import com.cloud.erp.common.utils.BeanMapperUtils;
import com.cloud.erp.common.utils.password.PassEntity;
import com.cloud.erp.common.utils.password.PassHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class SysUserInfoServiceImpl extends ServiceImpl<SysUserInfoMapper, SysUserInfoEntity> implements SysUserInfoService {

    @Autowired
    private SysRoleUserService sysRoleUserService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserThirdService sysUserThirdService;

    @Resource
    private SysAuthFeign sysAuthFeign;

    @Resource
    private ThirdFeign thirdFeign;

    private static final String DEFAULT_PASS = "123456";


    @Override
    public void add(SysUserInfoDTO sysUserInfoDTO) {
        String mobile = sysUserInfoDTO.getMobile();
        //当是新增加的时候才保校验用户是否存在
        boolean ifExist = checkMobile(mobile);
        if (ifExist) {
            throw new ServiceException(ApiError.ERROR_9010);
        }
        Integer createPasswordType = sysUserInfoDTO.getCreatePasswordType();
        String password = DEFAULT_PASS;
        //表示自己输入
        if (createPasswordType == 1) {
            password = sysUserInfoDTO.getPassword();
            String confirmPassword = sysUserInfoDTO.getConfirmPassword();
            if (StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
                throw new ServiceException(ApiError.ERROR_9015);
            }
            if (!password.equals(confirmPassword)) {
                throw new ServiceException(ApiError.ERROR_1001);
            }
        }

        SysUserInfoEntity entity = new SysUserInfoEntity();
        //复制属性
        BeanMapperUtils.copy(sysUserInfoDTO, entity);

        PassEntity passEntity = PassHandler.buildPassword(password);
        entity.setPassword(passEntity.getPassword());
        //账号
        entity.setUserAccount(mobile);
        entity.setSalt(passEntity.getSalt());
        boolean saveResult = this.save(entity);
        //保存成功 就去更新角色表
        if (saveResult) {
            List<String> roleIds = sysUserInfoDTO.getRoleIdList();
            if (CollectionUtils.isNotEmpty(roleIds)) {
                sysRoleUserService.batchInsertRef(entity.getUid(), roleIds, true);
            }
        }
    }

    @Override
    @Transactional
    public void update(SysUserInfoDTO sysUserInfoDTO) {
        String uid = sysUserInfoDTO.getUid();
        SysUserInfoEntity entity = this.getById(uid);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        entity.setRealName(sysUserInfoDTO.getRealName());
        entity.setMobile(sysUserInfoDTO.getMobile());
        List<String> roleIds = sysUserInfoDTO.getRoleIdList();
        boolean updateResult = this.updateById(entity);
        if (updateResult) {
            sysRoleUserService.batchInsertRef(entity.getUid(), roleIds, false);
        }

    }


    /**
     * 账号登录
     *
     * @param dto
     * @return
     */
    @Override
    public SysUserDTO accountLogin(AccountLoginDTO dto) {
        SysUserInfoEntity entity = findByAccount(dto.getAccount());
        if (Objects.isNull(entity)) {
            return null;
        }
        //检查密码是否正确
        boolean passwordFlag = PassHandler.checkPass(dto.getPassword(), entity.getSalt(), entity.getPassword());
        if (!passwordFlag) {
            return null;
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(entity, vo);
        //后面还有编写
        String uid = entity.getUid();
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        List<SysMenuVO> menuList = sysRoleMenuService.findMenuByRoleIds(roleIds);
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.FUNCTION_TYPE);
        vo.setPermissionList(permissionList);
        vo.setMenuList(menuList);
        return vo;
    }

    /**
     * 根据关键字获取用户列表
     *
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysUserVO>
     * @author yl
     * @date 2022-07-12 18:13
     */
    @Override
    public List<SysUserVO> findList(SysSearchUserDTO dto) {
        Integer sysType = dto.getSysType();
        //角色的
        if (sysType == 1) {
            return baseMapper.findRoleIfExistList(dto.getSearchKeyWord(), dto.getFlagId());
        }
        //岗位的
        if (sysType == 2) {
            return baseMapper.findPostIfExistList(dto.getSearchKeyWord(), dto.getFlagId());
        }
        //部门
        if (sysType == 3) {
            return baseMapper.findDepartmentIfExistList(dto.getSearchKeyWord(), dto.getFlagId());
        }
        return baseMapper.findList(dto.getSearchKeyWord(), dto.getFlagId());

    }

    /**
     * 分页获取用户信息
     *
     * @param dto
     * @return com.cloud.erp.common.common.vo.PagingVO
     * @author yl
     * @date 2022-07-12 18:30
     */

    @Override
    public PagingVO paging(PagingDTO<SysUserPagingSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SysUserPagingSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }


    /**
     * 账号绑定第三方平台
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-07-13 17:19
     */

    @Override
    public void bindingThirdParty(SysUserThirdDTO dto) {
        //code码
        String code = dto.getCode();
        String bindingPlatform = dto.getBindingPlatform();
        String flagId = "";
        //飞书平台
        if (ThirdConstants.FS_PLATFORM.equals(bindingPlatform)) {
            Map<String, Object> fsUserMap = thirdFeign.getFsUser(code);
            if (fsUserMap != null && fsUserMap.containsKey("union_id")) {
                flagId = fsUserMap.get("union_id").toString();
            } else {
                throw new ServiceException(ApiError.ERROR_9018);
            }

        }

        //当不为空的时候
        if (StringUtils.isNotBlank(flagId)) {
            LoginUser loginUser = SysInterceptor.threadLocal.get();
            String uid = loginUser.getUid();
            sysUserThirdService.bindingThirdParty(uid, flagId, bindingPlatform);
        }


    }

    /**
     * 修改用户状态
     *
     * @param stateDTO
     * @return void
     * @author yl
     * @date 2022-07-14 17:58
     */

    @Override
    public void updateState(UpdateUserStateDTO stateDTO) {
        LambdaUpdateWrapper<SysUserInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysUserInfoEntity::getUserState, stateDTO.getState());
        updateWrapper.in(SysUserInfoEntity::getUid, stateDTO.getIds());
        this.update(updateWrapper);

    }

    /**
     * 设置登录ip
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-07-15 14:05
     */

    @Override
    public void setLoginIp(SysLoginIpDTO dto) {
        baseMapper.setLoginIp(dto);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        String oldPassword = updatePasswordDTO.getOldPassword();
        String newPassword = updatePasswordDTO.getNewPassword();
        //确认密码
        String confirmPassword = updatePasswordDTO.getConfirmPassword();
        if (!newPassword.equals(confirmPassword)) {
            throw new ServiceException(ApiError.ERROR_1001);
        }

        LoginUser loginUser = SysInterceptor.threadLocal.get();
        String uid = loginUser.getUid();
        SysUserInfoEntity infoEntity = this.getById(uid);
        if (!Objects.isNull(infoEntity)) {
            //检查密码是否正确
            boolean passwordFlag = PassHandler.checkPass(oldPassword, infoEntity.getSalt(), infoEntity.getPassword());
            if (!passwordFlag) {
                throw new ServiceException(ApiError.ERROR_9017);
            }
            PassEntity passEntity = PassHandler.buildPassword(newPassword);
            infoEntity.setPassword(passEntity.getPassword());
            infoEntity.setSalt(passEntity.getSalt());
            //更改密码
            this.updateById(infoEntity);
            //退出登录 清除token
            sysAuthFeign.logout(loginUser.getAccessToken());
            SysInterceptor.threadLocal.remove();

        }


    }

    /**
     * 扫码登录
     *
     * @param dto
     * @return com.cloud.erp.common.modules.sys.dto.SysUserDTO
     * @author yl
     * @date 2022-07-21 16:13
     */
    @Override
    public SysUserDTO scanCodeLogin(SysUserThirdDTO dto) {
        //code码
        String code = dto.getCode();
        String bindingPlatform = dto.getBindingPlatform();
        String flagId = "";
        //飞书平台
        if (ThirdConstants.FS_PLATFORM.equals(bindingPlatform)) {
            Map<String, Object> fsUserMap = thirdFeign.getFsUser(code);
            if (fsUserMap != null && fsUserMap.containsKey("union_id")) {
                flagId = fsUserMap.get("union_id").toString();
            } else {
                throw new ServiceException(ApiError.ERROR_1005);
            }
        }

        SysUserThirdEntity entity = sysUserThirdService.findByUnionId(flagId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_9019);
        }
        SysUserInfoEntity userEntity = this.getById(entity.getUserId());
        if (Objects.isNull(userEntity)) {
            return null;
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(userEntity, vo);
        //后面还有编写
        String uid = userEntity.getUid();
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        List<SysMenuVO> menuList = sysRoleMenuService.findMenuByRoleIds(roleIds);
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.FUNCTION_TYPE);
        vo.setPermissionList(permissionList);
        vo.setMenuList(menuList);
        return vo;
    }

    public SysUserInfoEntity findByAccount(String account) {
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getUserAccount, account).
                eq(SysUserInfoEntity::getDeleteState, 1);

        SysUserInfoEntity entity = this.getOne(queryWrapper);
        return entity;

    }


    /**
     * 检查手机号是否存在
     *
     * @param mobile
     */
    private boolean checkMobile(String mobile) {
        QueryWrapper<SysUserInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysUserInfoEntity::getUserAccount, mobile);
        int count = this.count(queryWrapper);
        return count > 0 ? true : false;

    }


}