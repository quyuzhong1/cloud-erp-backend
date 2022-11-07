package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.constant.RedisCacheConstants;
import com.common.core.constant.ThirdConstants;
import com.common.core.constant.UserStateConstants;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.Md5Util;
import com.common.core.utils.RedisKeyUtil;
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.password.PassEntity;
import com.common.core.utils.password.PassHandler;
import com.common.message.service.MailService;
import com.common.web.service.RedisService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.email.dto.EmailDTO;
import com.erp.common.modules.email.dto.EmailVerifyCodeDTO;
import com.erp.common.modules.email.enums.EmailTemplate;
import com.erp.common.modules.sys.dto.*;
import com.erp.common.modules.sys.vo.SysMenuVO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysAdminUserEntity;
import com.erp.model.sys.entity.SysRoleUserEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.rpc.auth.feign.AuthFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.interceptor.SysInterceptor;
import com.erp.server.sys.mapper.SysDepartmentMapper;
import com.erp.server.sys.mapper.SysUserInfoMapper;
import com.erp.server.sys.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class SysUserInfoServiceImpl extends ServiceImpl<SysUserInfoMapper, SysUserInfoEntity> implements SysUserInfoService {

    @Autowired
    private SysRoleUserService sysRoleUserService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserThirdService sysUserThirdService;

    @Resource
    private AuthFeign sysAuthFeign;

    @Resource
    private FsService fsService;

    @Resource
    private RedisService redisService;

    @Resource
    private MailService mailService;

    @Resource
    private SysDepartmentService sysDepartmentService;

    @Resource
    private SysDepartmentMapper sysDepartmentMapper;

    private static final String DEFAULT_PASS = "e10adc3949ba59abbe56e057f20f883e";


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
        entity.setUserName(sysUserInfoDTO.getUserName());
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

        Integer userState = entity.getUserState();
        //表示禁用
        if (UserStateConstants.USER_DISABLE == userState) {
            throw new ServiceException(ApiError.ERROR_1011);
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(entity, vo);

        //判断是否是超级管理员登录
        SysUserDTO sysUserDTO = adminLogin(vo);
        if (sysUserDTO != null) {
            return sysUserDTO;
        }

        //后面还有编写 1580852739573813249
        String uid = entity.getUid();
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        List<SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds);
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds);
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.NO_STATE);
        vo.setPermissionList(permissionList);
        vo.setOverallMenuList(overallMenuList);
        vo.setLeftMenuList(leftMenuList);
        SysUserThirdEntity thirdEntity = sysUserThirdService.findByUserId(uid);
        Integer bindingState = 0;
        String bindingPlatform = "";
        if (!Objects.isNull(thirdEntity)) {
            bindingPlatform = thirdEntity.getThirdPartyType();
            bindingState = 1;
        }
        vo.setBindingPlatform(bindingPlatform);
        vo.setBindingState(bindingState);
        return vo;
    }

    public SysUserDTO adminLogin(SysUserDTO vo) {
        if (!vo.getUserAccount().equals(SysConstant.ADMIN_USER)) {
            return null;
        }
        List<SysMenuVO> menuAll = sysRoleMenuService.findMenuAll();
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuAll();
        List<String> permissionList = sysRoleMenuService.findMenuCodeAll();
        vo.setPermissionList(permissionList);
        vo.setOverallMenuList(menuAll);
        vo.setLeftMenuList(leftMenuList);
        vo.setBindingPlatform("");
        vo.setBindingState(0);
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
    public List<UserDTO> findList(SysSearchUserDTO dto) {
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
        IPage pageData = baseMapper.paging(query, params, params.getRoleIds());
        List<UserManageDTO> list = pageData.getRecords();
        List<String> userIds = list.stream().map(UserManageDTO::getUid).collect(Collectors.toList());
        List<SysRoleUserEntity> roleUserList = sysRoleUserService.findRoleIdsByUidList(userIds);
        for (UserManageDTO vo : list) {
            List<String> roleIdList = roleUserList.stream().filter(r -> r.getUserId().equals(vo.getUid()))
                    .map(SysRoleUserEntity::getRoleId).collect(Collectors.toList());
            vo.setRoleIdList(roleIdList);
        }
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
            FindThirdUserDTO findThirdUserDTO = new FindThirdUserDTO();
            findThirdUserDTO.setCode(code);
            findThirdUserDTO.setThirdType(ThirdConstants.THIRD_BINDING_TYPE);
            Map<String, Object> fsUserMap = fsService.getFsUser(findThirdUserDTO);
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

            boolean ifBinding = sysUserThirdService.checkIfBinding(uid, flagId, bindingPlatform);
            if (ifBinding) {
                throw new ServiceException(ApiError.ERROR_9020);
            }
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
            FindThirdUserDTO findThirdUserDTO = new FindThirdUserDTO();
            findThirdUserDTO.setCode(code);
            findThirdUserDTO.setThirdType(ThirdConstants.THIRD_LOGIN_TYPE);
            Map<String, Object> fsUserMap = fsService.getFsUser(findThirdUserDTO);
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
            throw new ServiceException(ApiError.ERROR_9011);
        }
        Integer userState = userEntity.getUserState();
        //表示禁用
        if (UserStateConstants.USER_DISABLE == userState) {
            throw new ServiceException(ApiError.ERROR_1011);
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(userEntity, vo);
        //后面还有编写
        String uid = userEntity.getUid();
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        //全局菜单
        List<SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds);
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds);
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.FUNCTION_TYPE);
        vo.setPermissionList(permissionList);
        vo.setOverallMenuList(overallMenuList);
        vo.setLeftMenuList(leftMenuList);
        vo.setBindingState(SysConstant.YES_STATE);
        vo.setBindingPlatform(bindingPlatform);
        return vo;
    }

    /**
     * 个人中心
     *
     * @param
     * @return com.cloud.erp.common.common.token.vo.LoginUser
     * @author yl
     * @date 2022-07-26 14:15
     */

    @Override
    public UserBaseDTO myCenter() {
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        UserBaseDTO vo = new UserBaseDTO();
        SysUserThirdEntity sysUserThirdEntity = sysUserThirdService.findByUserId(loginUser.getUid());
        SysUserInfoEntity entity = this.getById(loginUser.getUid());
        String bindingPlatform = "";
        if (!Objects.isNull(sysUserThirdEntity)) {
            bindingPlatform = sysUserThirdEntity.getThirdPartyType();
        }
        BeanMapperUtils.copy(entity, vo);
        vo.setThirdPartyType(bindingPlatform);
        return vo;
    }


    /**
     * 修改基础信息
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-02 14:50
     */

    @Override
    public void updateBase(SysUserBaseDTO dto) {
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        SysUserInfoEntity entity = this.getById(loginUser.getUid());
        if (!Objects.isNull(entity)) {
            entity.setRealName(dto.getRealName());
        }
        this.updateById(entity);
    }


    /**
     * 绑定邮箱
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-02 16:48
     */
    @Override
    public void bindingEmail(EmailVerifyCodeDTO dto) {
        String email = dto.getEmail();
        boolean flag = ValidatorUtil.isEmail(email);
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_1008);
        }
        //检查邮箱是否存在
        checkEmailIfExist(email);
        //检查 验证码是否i正确
        checkMobileCode(dto.getEmail(), dto.getVerifyCode());
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        if (!Objects.isNull(loginUser)) {
            SysUserInfoEntity entity = this.getById(loginUser.getUid());
            entity.setEmail(email);
            this.updateById(entity);
        }

    }

    /**
     * 发送邮箱
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-03 11:14
     */

    @Override
    public void sedEmail(EmailVerifyCodeDTO dto) {
        String email = dto.getEmail();
        boolean flag = ValidatorUtil.isEmail(email);
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_1008);
        }
//        String emailCodeKey = RedisKeyUtil.getEmailCodeCacheKey(email);
//        String redisCode = redisService.getCacheObject(emailCodeKey);
//        if (StringUtils.isNotBlank(redisCode)) {
//            throw new ServiceException(ApiError.ERROR_1009);
//        }
        EmailDTO<EmailVerifyCodeDTO> emailDTO = new EmailDTO();
        String code = RandomStringUtils.randomNumeric(4);
        LocalDate localDate = LocalDate.now();
        dto.setVerifyCode(code);
        dto.setDate(DateUtil.getCnDate(localDate));
        emailDTO.setData(dto);
        String[] recipients = {email};
        emailDTO.setRecipients(recipients);
        emailDTO.setSubject("验证码");
        emailDTO.setTemplate(EmailTemplate.VERIFY_CODE);
        Boolean sendResult = mailService.sedVerifyCode(emailDTO);
        if (sendResult) {
            redisService.setCacheObject(RedisKeyUtil.getEmailCodeCacheKey(email), code, RedisCacheConstants.EMAIL_CODE_EXPIRATION, TimeUnit.MINUTES);
        }
    }


    /**
     * 方法说明
     *
     * @return void
     * @author yl
     * @date 2022-08-03 11:55
     * 解除 邮箱
     */
    @Override
    public void removeEmail() {
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        if (!Objects.isNull(loginUser)) {
            SysUserInfoEntity entity = this.getById(loginUser.getUid());
            entity.setEmail("");
            this.updateById(entity);
        }
    }

    /**
     * 获取所有用户信息
     *
     * @param
     * @return java.util.List<com.erp.model.sys.dto.FindUserDTO>
     * @author yl
     * @date 2022-09-27 15:58
     */
    @Override
    public List<FindUserDTO> getUserList(BaseSearchDTO dto) {
        List<FindUserDTO> resultList = new LinkedList<>();
        //先添加自己
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        Boolean flag = !Objects.isNull(loginUser);
        if (flag) {
            FindUserDTO user = new FindUserDTO();
            user.setIsMyState(1);
            user.setUserId(loginUser.getUid());
            user.setUserName(loginUser.getUserName());
            resultList.add(user);
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysUserInfoEntity::getUid, SysUserInfoEntity::getUserName);
        queryWrapper.eq(SysUserInfoEntity::getUserState,SysConstant.YES_STATE);
        queryWrapper.eq(SysUserInfoEntity::getDeleteState,SysConstant.YES_STATE);
        if (flag) {
            queryWrapper.ne(SysUserInfoEntity::getUid, loginUser.getUid());
        }
        if (StringUtils.isNotBlank(dto.getSearchKeyword())) {
            queryWrapper.like(SysUserInfoEntity::getUserName, dto.getSearchKeyword());
        }
        List<SysUserInfoEntity> list = this.list(queryWrapper);
        for (SysUserInfoEntity item : list) {
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(item.getUid());
            userDTO.setUserName(item.getUserName());
            userDTO.setIsMyState(0);
            resultList.add(userDTO);
        }
        return resultList;
    }

    @Override
    public List<FindUserDTO> getAllUserList() {
        List<FindUserDTO> resultList = new LinkedList<>();
        List<SysUserInfoEntity> list = this.list();
        for (SysUserInfoEntity item : list) {
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(item.getUid());
            userDTO.setUserName(item.getUserName());
            userDTO.setIsMyState(0);
            resultList.add(userDTO);
        }
        return resultList;
    }

    /**
     * 检查邮箱验证码是否正确
     * @param email
     * @param verifyCode
     * @return void
     * @author yl
     * @date 2022-08-02 17:04
     */
    private void checkMobileCode(String email, String verifyCode) {
        String emailCodeKey = RedisKeyUtil.getEmailCodeCacheKey(email);
        String code = redisService.getCacheObject(emailCodeKey);
        if (StringUtils.isBlank(code) || !verifyCode.equals(code)) {
            throw new ServiceException(ApiError.ERROR_1007);
        }
    }

    /**
     * 检查是否存在
     *
     * @param mail
     * @return void
     * @author yl
     * @date 2022-08-02 16:51
     */
    private void checkEmailIfExist(String mail) {
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getEmail, mail);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_9024);
        }
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

    /**
     * 根据用户id 获取到用户的权限
     *
     * @param userId
     * @return java.util.List<com.erp.common.modules.sys.dto.UserRequestPermissionsDTO>
     * @author yl
     * @date 2022-10-15 11:22
     */
    public List<UserRequestPermissionsDTO> getRequestPermissionsList(String userId) {
        return baseMapper.getRequestPermissionsList(userId);
    }

    /**
     * 根据用户id 获取到所属部门的所有用户id
     * @Author Luo_WG
     * @Date 2022/10/19 14:17
     * @param userId 用户id
     * @return java.util.List<java.lang.String>
     **/
    public List<String> getDepUserList(String userId) {
        List<SysDepartmentTreeDTO> treeList = sysDepartmentMapper.findTree();
        List<String> userDepList = baseMapper.getUserDepList(userId);
        List<String> deptList = new LinkedList<>();
        for (String s : userDepList) {
            if (CollectionUtils.isNotEmpty(treeList)) {
                for (SysDepartmentTreeDTO vo : treeList) {
                    if (vo.getPath().contains(s)) {
                        deptList.add(vo.getId());
                    }

                }
            }
        }
        if (deptList.size() <= 0) {
            return new ArrayList<String>();
        }
        return baseMapper.getDepUserList(deptList);
    }


}