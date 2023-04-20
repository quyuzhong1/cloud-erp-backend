package com.erp.server.sys.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.*;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.service.RedisService;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.Md5Util;
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.dto.email.EmailDTO;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.common.message.service.MailService;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.entity.password.PassEntity;
import com.erp.model.sys.entity.password.PassHandler;
import com.erp.model.sys.utils.RedisKeyUtil;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.rpc.auth.feign.AuthFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.mapper.SysDepartmentMapper;
import com.erp.server.sys.mapper.SysUserInfoMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private SysDepartmentUserService sysDepartmentUserService;

    @Resource
    private SysDepartmentService sysDepartmentService;

    @Resource
    private SyncKingdeeSysUserInfoService syncKingdeeSysUserInfoService;

    @Resource
    private SysCodeService sysCodeService;

    @Resource
    private SysDepartmentMapper sysDepartmentMapper;

    @Resource
    private CommonService commonService;

    private static final String DEFAULT_PASS = "e10adc3949ba59abbe56e057f20f883e";


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserInfoDTO sysUserInfoDTO) {
        String mobile = sysUserInfoDTO.getMobile();
        //验证用户信息
        checkUserInfo(sysUserInfoDTO);
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
        //编号
        String code = sysCodeService.getSeqNo(new SysCodeDTO("", BusinessNoTypeEnum.CODE_USER.getCode()));
        entity.setCode(code);
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
            //同步金蝶员工数据
            syncKingdeeSysUserInfoService.syncDataToKingdee(entity, SyncKingdeeOperateEnum.OPERATE_ADD.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserInfoDTO sysUserInfoDTO) {
        String uid = sysUserInfoDTO.getUid();
        SysUserInfoEntity entity = this.getById(uid);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        //验证用户信息
        checkUserInfo(sysUserInfoDTO);
        entity.setRealName(sysUserInfoDTO.getRealName());
        entity.setMobile(sysUserInfoDTO.getMobile());
        entity.setUserName(sysUserInfoDTO.getUserName());
        List<String> roleIds = sysUserInfoDTO.getRoleIdList();
        boolean updateResult = this.updateById(entity);
        if (updateResult) {
            sysRoleUserService.batchInsertRef(entity.getUid(), roleIds, false);
            //同步金蝶员工数据
            syncKingdeeSysUserInfoService.syncDataToKingdee(entity, SyncKingdeeOperateEnum.OPERATE_UPDATE.getCode());
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
            LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
    @Transactional(rollbackFor = Exception.class)
    public void updateState(UpdateUserStateDTO stateDTO) {
        LambdaUpdateWrapper<SysUserInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysUserInfoEntity::getUserState, stateDTO.getState());
        updateWrapper.in(SysUserInfoEntity::getUid, stateDTO.getIds());
        this.update(updateWrapper);

        List<SysUserInfoEntity> list = this.listByIds(stateDTO.getIds());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (SysUserInfoEntity entity : list) {
            String operate = MathUtil.ZERO.equals(stateDTO.getState()) ? SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode() : SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode();
            syncKingdeeSysUserInfoService.syncDataToKingdee(entity, operate);
        }
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

        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
            CommonInterceptor.threadLocal.remove();

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
        List<SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds);
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds);
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.NO_STATE);
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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
        boolean result = redisService.setNx(email, 1, 1, TimeUnit.MINUTES);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_1014);
        }
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
        LocalDateTime localDate = LocalDateTime.now();
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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
        queryWrapper.eq(SysUserInfoEntity::getUserState, SysConstant.YES_STATE);
        queryWrapper.eq(SysUserInfoEntity::getDeleteState, SysConstant.YES_STATE);
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
    public List<FindUserDTO>  getAuthorityUserList(BaseSearchDTO dto) {
        List<FindUserDTO> resultList = new LinkedList<>();
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
     *
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
        queryWrapper.last("LIMIT 1");
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        return entity;
    }


   /**
    * @description: 用户验证信息
    * @author Will
    * @date: 2023/3/2 10:37
    * @param sysUserInfoDTO
    * @return boolean
    */
    private void checkUserInfo(SysUserInfoDTO sysUserInfoDTO) {
        //验证手机号是否已存在
        LambdaQueryWrapper<SysUserInfoEntity> mobileQueryWrapper = new LambdaQueryWrapper<>();
        mobileQueryWrapper.eq(SysUserInfoEntity::getUserAccount, sysUserInfoDTO.getMobile());
        if (StringUtils.isNotBlank(sysUserInfoDTO.getUid())) {
            mobileQueryWrapper.ne(SysUserInfoEntity::getUid,sysUserInfoDTO.getUid());
        }
        int mobileCount = this.count(mobileQueryWrapper);
        if (mobileCount > 0) {
            throw new ServiceException(ApiError.ERROR_9010);
        }
        //验证用户名是否已存在
        LambdaQueryWrapper<SysUserInfoEntity> userNameQueryWrapper = new LambdaQueryWrapper<>();
        userNameQueryWrapper.eq(SysUserInfoEntity::getUserName,sysUserInfoDTO.getUserName());
        if (StringUtils.isNotBlank(sysUserInfoDTO.getUid())) {
            userNameQueryWrapper.ne(SysUserInfoEntity::getUid,sysUserInfoDTO.getUid());
        }
        int userNameCount = this.count(userNameQueryWrapper);
        if (userNameCount > 0) {
            throw new ServiceException(ApiError.ERROR_9038);
        }
    }

    /**
     * 根据用户id 获取到用户的权限
     *
     * @param userId
     * @return java.util.List<com.erp.common.business.dto.UserRequestPermissionsDTO>
     * @author yl
     * @date 2022-10-15 11:22
     */
    public List<UserRequestPermissionsDTO> getRequestPermissionsList(String userId) {

//        //获取用户角色id
//        List<String> roleIdList = sysRoleUserService.findRoleIdsByUid(userId);
//        //这个是查询角色与对应菜单的关系
//        List<SysRoleMenuEntity> roleRefMenuList = sysRoleMenuService.getMenuRefRoleByRoleIds(roleIdList);
//        //菜单id集合
//        List<String> menuIdList = roleRefMenuList.stream().map(SysRoleMenuEntity::getMenuId).distinct().collect(Collectors.toList());
//
//        List<SysMenuEntity> menuList = sysMenuService.listByIds(menuIdList);
//        List<UserRequestPermissionsDTO> resultList = new ArrayList<>(menuList.size());
//        for (SysMenuEntity menu : menuList) {
//            UserRequestPermissionsDTO result = new UserRequestPermissionsDTO();
//            result.setPermissionsCode(menu.getMenuCode());
//            Integer dataScope = roleRefMenuList.stream().filter(r -> r.getMenuId().equals(menu.getMenuId())).map(SysRoleMenuEntity::getDataScope).max(Integer::compareTo).get();
//            result.setDataScope(dataScope);
//            resultList.add(result);
//        }
        return baseMapper.getRequestPermissionsList(userId);
    }

    /**
     * 根据用户id 获取到所属部门的所有用户id
     *
     * @param userId 用户id
     * @return java.util.List<java.lang.String>
     * @Author Luo_WG
     * @Date 2022/10/19 14:17
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

    @Override
    public List<FindUserDTO> getUserListByUserIds(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.EMPTY_LIST;
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserInfoEntity::getUid, userIds);
        List<FindUserDTO> resultList = new LinkedList<>();
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
    public FindUserDTO getUserByUserId(String userId) {
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getUid, userId);
        queryWrapper.eq(SysUserInfoEntity::getDeleteState, IsConstant.YES);
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        if(!Objects.isNull(entity)){
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(entity.getUid());
            userDTO.setUserName(entity.getUserName());
            userDTO.setIsMyState(0);
            return userDTO;
        }
        return new FindUserDTO();


    }

    @Override
    public FindUserDTO getUserByUserName(String userName) {
        if(StringUtils.isBlank(userName)){
            return new FindUserDTO();
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getUserName, userName);
        queryWrapper.last("limit 1");
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        if(!Objects.isNull(entity)){
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(entity.getUid());
            userDTO.setUserName(entity.getUserName());
            userDTO.setIsMyState(0);
            return userDTO;
        }
        return new FindUserDTO();
    }


    /**
     * 获取所有用户所在的部门
     * @Author Luo_WG
     * @Date 2022/12/13 17:12
     * @return java.util.List<com.erp.model.sys.dto.SysUserDeptDTO>
     **/
    @Override
    public List<SysUserDeptDTO> getUserDeptList(){
        return baseMapper.getUserDeptList();
    }


    @Override
    public List<UserSuperiorDTO> listSuperiorByUserIds(List<String> userIds) {
        List<UserSuperiorDTO> parentList = new ArrayList<>();

        for (String userId: userIds) {
            // 部门负责人
            SysDepartmentUserNumberDTO dto = sysDepartmentUserService.getByUserId(userId);
            if (ObjectUtils.isEmpty(dto) || StringUtils.isBlank(dto.getDepartmentId())) {
                continue;
            }
            SysDepartmentEntity entity = sysDepartmentService.getById(dto.getDepartmentId());
            //部门不存在
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            //查询直接直属上级
            List<SysDepartmentUserEntity> sysDepartmentUserNumberList = sysDepartmentUserService.listSuperiorById(dto.getDepartmentId());
            if (CollectionUtils.isNotEmpty(sysDepartmentUserNumberList)) {
                List<String> parentIds = sysDepartmentUserNumberList.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
                parentList.add(new UserSuperiorDTO().setUserId(StringUtils.join(parentIds, ",")).setSuperiorType("direct_superior"));
                parentList.add(new UserSuperiorDTO().setUserId(StringUtils.join(parentIds, ",")).setSuperiorType("direct_department_charge"));
            }
            //查询二级部门负责人
            if (!"0".equals(entity.getParentId())) {
                SysDepartmentEntity secondDepart = sysDepartmentService.getParentDepartmentById(entity.getParentId());
                if (ObjectUtils.isNotEmpty(secondDepart)) {
                    List<SysDepartmentUserEntity> sysDepartmentUserNumberDTOS = sysDepartmentUserService.listSuperiorById(secondDepart.getId());
                    if (CollectionUtils.isNotEmpty(sysDepartmentUserNumberDTOS)) {
                        List<String> parentIds = sysDepartmentUserNumberDTOS.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
                        parentList.add(new UserSuperiorDTO().setUserId(StringUtils.join(parentIds, ",")).setSuperiorType("second_department_charge"));
                    }
                }
                //查询三级部门负责人
                if (ObjectUtils.isNotEmpty(secondDepart) && "0".equals(secondDepart.getParentId())) {
                    SysDepartmentEntity threeDepart = sysDepartmentService.getParentDepartmentById(secondDepart.getParentId());
                    if (ObjectUtils.isNotEmpty(threeDepart)) {
                        List<SysDepartmentUserEntity> sysDepartmentUserNumberDTOS = sysDepartmentUserService.listSuperiorById(threeDepart.getParentId());
                        if (CollectionUtils.isNotEmpty(sysDepartmentUserNumberDTOS)) {
                            List<String> parentIds = sysDepartmentUserNumberDTOS.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
                            parentList.add(new UserSuperiorDTO().setUserId(StringUtils.join(parentIds, ",")).setSuperiorType("three_department_charge"));
                        }
                    }
                    //查询四级部门负责人
                    if (ObjectUtils.isNotEmpty(threeDepart) && "0".equals(threeDepart.getParentId())) {
                        SysDepartmentEntity fourDepart = sysDepartmentService.getParentDepartmentById(threeDepart.getParentId());
                        if (ObjectUtils.isNotEmpty(fourDepart)) {
                            List<SysDepartmentUserEntity> sysDepartmentUserNumberDTOS = sysDepartmentUserService.listSuperiorById(fourDepart.getParentId());
                            if (CollectionUtils.isNotEmpty(sysDepartmentUserNumberDTOS)) {
                                List<String> parentIds = sysDepartmentUserNumberDTOS.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
                                parentList.add(new UserSuperiorDTO().setUserId(StringUtils.join(parentIds, ",")).setSuperiorType("four_department_charge"));
                            }
                        }
                        //查询五级部门负责人
                        if (ObjectUtils.isNotEmpty(fourDepart) && "0".equals(fourDepart.getParentId())) {
                            SysDepartmentEntity fiveDepart = sysDepartmentService.getParentDepartmentById(fourDepart.getParentId());
                            if (ObjectUtils.isNotEmpty(fiveDepart)) {
                                List<SysDepartmentUserEntity> sysDepartmentUserNumberDTOS = sysDepartmentUserService.listSuperiorById(fiveDepart.getParentId());
                                if (CollectionUtils.isNotEmpty(sysDepartmentUserNumberDTOS)) {
                                    List<String> parentIds = sysDepartmentUserNumberDTOS.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
                                    parentList.add(new UserSuperiorDTO().setUserId(StringUtils.join(parentIds, ",")).setSuperiorType("four_department_charge"));
                                }
                            }
                        }

                    }

                }
            }
        }
            return  parentList;
    }




    /**
     * 根据用户id 获取用户登录的信息
     * 用于 token 获取用户信息内容
     * @author yl
     * @date 2023-01-14 9:45
     * @param userId
     * @return com.erp.model.sys.dto.SysUserDTO
     */
    @Override
    public SysUserDTO getSysUserById(String userId) {
        SysUserInfoEntity entity = this.getById(userId);
        if (Objects.isNull(entity)) {
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

    @Override
    public boolean updateSyncKingdeeStatus(List<String> businessIds, String syncKingdeeStatus, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .in(SysUserInfoEntity::getUid,businessIds)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),SysUserInfoEntity::getSyncKingdeeStatus,syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),SysUserInfoEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId),SysUserInfoEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> uids) {
        List<SysUserInfoEntity> list = this.listByIds(uids);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (SysUserInfoEntity entity : list) {
            //同步金蝶员工数据
            syncKingdeeSysUserInfoService.syncDataToKingdee(entity, SyncKingdeeOperateEnum.OPERATE_DELETE.getCode());
        }
        this.removeByIds(uids);
    }

    /**
     * 重置密码
     * @Author Luo_WG
     * @Date 2023/4/20 9:46
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(String uid) {
        if (StringUtils.isBlank(uid)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        SysUserInfoEntity userInfoEntity = this.getById(uid);
        if (StringUtils.isBlank(userInfoEntity.getEmail())) {
            throw new ServiceException(ApiError.ERROR_9044);
        }
        EmailVerifyCodeDTO emailVerifyCodeDTO = new EmailVerifyCodeDTO();
        emailVerifyCodeDTO.setEmail(userInfoEntity.getEmail());
        String num = RandomStringUtils.randomNumeric(8);
        String password = Md5Util.md5(num);
        SysUserInfoEntity entity = new SysUserInfoEntity();

        PassEntity passEntity = PassHandler.buildPassword(password);
        entity.setPassword(passEntity.getPassword());
        entity.setSalt(passEntity.getSalt());
        boolean flag = lambdaUpdate()
                .set(SysUserInfoEntity::getSalt, passEntity.getSalt())
                .set(SysUserInfoEntity::getPassword, passEntity.getPassword())
                .eq(SysUserInfoEntity::getUid, userInfoEntity.getUid()).update();
        if (flag) {
            boolean emailFlag = ValidatorUtil.isEmail(emailVerifyCodeDTO.getEmail());
            if (!emailFlag) {
                throw new ServiceException(ApiError.ERROR_1008);
            }
            LocalDateTime localDate = LocalDateTime.now();
            emailVerifyCodeDTO.setVerifyCode(num);
            emailVerifyCodeDTO.setDate(DateUtil.getCnDate(localDate));
            Boolean sendResult = sendingEmail(emailVerifyCodeDTO, "重置密码");
            if (!sendResult) {
                throw new ServiceException(ApiError.ERROR_1010);
            }
        }
        return flag;
    }

    /**
     * 忘记密码
     * @Author Luo_WG
     * @Date 2023/4/20 11:18
     * @param forgotPasswordDTO forgotPasswordDTO
     * @return java.lang.Boolean
     **/
    public Boolean forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        SysUserInfoEntity sysUserInfoEntity = lambdaQuery().eq(SysUserInfoEntity::getUserAccount, forgotPasswordDTO.getVerificationCode()).one();
        if (ObjectUtil.isEmpty(sysUserInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_9043);
        }

        if (StringUtils.isBlank(sysUserInfoEntity.getEmail())) {
            throw new ServiceException(ApiError.ERROR_9044);
        }

        String code = redisService.getCacheObject(RedisKeyUtil.getEmailCodeCacheKey(sysUserInfoEntity.getEmail()));
        if (StringUtils.isBlank(code) || !forgotPasswordDTO.getVerificationCode().equals(code)) {
            throw new ServiceException(ApiError.ERROR_1007);
        }
        SysUserInfoEntity entity = new SysUserInfoEntity();

        PassEntity passEntity = PassHandler.buildPassword(forgotPasswordDTO.getPassword());
        entity.setPassword(passEntity.getPassword());
        entity.setSalt(passEntity.getSalt());
        boolean flag = lambdaUpdate()
                .set(SysUserInfoEntity::getSalt, passEntity.getSalt())
                .set(SysUserInfoEntity::getPassword, passEntity.getPassword())
                .eq(SysUserInfoEntity::getUid, sysUserInfoEntity.getUid()).update();
        return flag;
    }

    /**
     * 忘记密码-获取验证码
     * @Author Luo_WG
     * @Date 2023/4/20 11:45
     * @param userAccount userAccount
     * @return com.common.core.controller.vo.ApiResult
     **/
    public Map<String,Object> forgotPasswordGetCode(String userAccount) {
        SysUserInfoEntity sysUserInfoEntity = lambdaQuery().eq(SysUserInfoEntity::getUserAccount, userAccount).one();
        if (ObjectUtil.isEmpty(sysUserInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_9043);
        }

        if (StringUtils.isBlank(sysUserInfoEntity.getEmail())) {
            throw new ServiceException(ApiError.ERROR_9044);
        }
        EmailVerifyCodeDTO dto = new EmailVerifyCodeDTO();
        dto.setEmail(sysUserInfoEntity.getEmail());
        sedEmail(dto);
        Map<String,Object> map = new HashMap<>();
        map.put("msg", String.format("已给<'%s'>成功发送验证码，请在邮箱查看", sysUserInfoEntity.getEmail()));
        return map;
    }

    private Boolean sendingEmail(EmailVerifyCodeDTO dto, String subject) {
        String email = dto.getEmail();
        boolean result = redisService.setNx(email, 1, 1, TimeUnit.MINUTES);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_1014);
        }
        boolean flag = ValidatorUtil.isEmail(email);
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_1008);
        }
        EmailDTO<EmailVerifyCodeDTO> emailDTO = new EmailDTO();
        emailDTO.setData(dto);
        String[] recipients = {email};
        emailDTO.setRecipients(recipients);
        emailDTO.setSubject(subject);
        emailDTO.setTemplate(EmailTemplate.RESETTING_PASSWORD);
        Boolean sendResult = mailService.sedVerifyCode(emailDTO);
        return sendResult;
    }
}