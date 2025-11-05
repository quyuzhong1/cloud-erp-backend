package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.*;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.UserTypeEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.UUID;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.common.message.dto.email.EmailDTO;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.common.message.service.MailService;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysRoleUserEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.sys.entity.password.PassEntity;
import com.erp.model.sys.entity.password.PassHandler;
import com.erp.model.sys.enums.AuthDataTypeEnum;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.sys.enums.ThirdPlatformEnums;
import com.erp.model.sys.utils.RedisKeyUtil;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.rpc.auth.feign.AuthFeign;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopSysUserAuthFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.convert.SysUserConvert;
import com.erp.server.sys.mapper.SysDepartmentMapper;
import com.erp.server.sys.mapper.SysUserInfoMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.*;
import com.lark.oapi.service.acs.v1.enums.UserIdTypeEnum;
import com.lark.oapi.service.contact.v3.model.BatchGetIdUserResp;
import com.lark.oapi.service.contact.v3.model.UserContactInfo;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
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
    private SyncKingdeeSysUserInfoService syncKingdeeSysUserInfoService;

    @Resource
    private SysCodeService sysCodeService;

    @Resource
    private SysDepartmentMapper sysDepartmentMapper;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopSysUserAuthFeign shopSysUserAuthFeign;
    @Resource
    private AuthDataFeign authDataFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private AuthUserShopService authUserShopService;
    @Resource
    private AuthUserWarehouseService authUserWarehouseService;
    @Resource
    private FileFeign filefeign;

    //123456
    private static final String DEFAULT_PASS = "e10adc3949ba59abbe56e057f20f883e";


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void add(SysUserInfoDTO sysUserInfoDTO) {
        String mobile = sysUserInfoDTO.getMobile();
        //验证用户信息
        checkUserInfo(sysUserInfoDTO);
        Integer createPasswordType = sysUserInfoDTO.getCreatePasswordType();
        String randomString = UUID.generateRandomString();
        String password = Md5Util.md5(randomString);
        boolean needChangePwd;
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
            needChangePwd = false;
        }else {
            //自动创建密码 强制登录修改密码
            needChangePwd = true;
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
        entity.setNeedChangePwd(needChangePwd);
        if (Objects.isNull(entity.getIsSuper())){
            entity.setIsSuper(false);
        }
        boolean saveResult = this.save(entity);
        if (needChangePwd){
            sendPwdEmail(entity,randomString);
        }
        //保存成功 就去更新角色表
        if (saveResult) {
            List<String> roleIds = sysUserInfoDTO.getRoleIdList();
            if (CollectionUtils.isNotEmpty(roleIds)) {
                sysRoleUserService.batchInsertRef(entity.getUid(), roleIds, true);
            }
            //店铺授权
            authUserShopService.batchSaveOrUpdate(entity.getUid(),sysUserInfoDTO.getShopIdList(),sysUserInfoDTO.getShopAuthType());
            //仓库权限
            authUserWarehouseService.batchSaveOrUpdate(entity.getUid(),sysUserInfoDTO.getWarehouseIdList(),sysUserInfoDTO.getWarehouseAuthType());
            //同步金蝶员工数据
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSysUserInfoService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }
    }
    @Override


    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public String addSrmUser(SysUserInfoDTO sysUserInfoDTO) {
        String mobile = sysUserInfoDTO.getMobile();
        //验证用户信息
        checkUserInfo(sysUserInfoDTO);
        Integer createPasswordType = sysUserInfoDTO.getCreatePasswordType();
        String randomString = UUID.generateRandomString();
        String password = Md5Util.md5(randomString);
        Boolean needChangePwd = sysUserInfoDTO.getNeedChangePwd();
        //表示自己输入
        if (Objects.nonNull(createPasswordType) && 1 == createPasswordType) {
            password = sysUserInfoDTO.getPassword();
            String confirmPassword = sysUserInfoDTO.getConfirmPassword();
            if (StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
                throw new ServiceException(ApiError.ERROR_9015);
            }
            if (!password.equals(confirmPassword)) {
                throw new ServiceException(ApiError.ERROR_1001);
            }
        }
        SysUserInfoEntity entity = SysUserConvert.INSTANCE.copyDTOtoSysUser(sysUserInfoDTO);
        //编号
        String code = sysCodeService.getSeqNo(new SysCodeDTO("", BusinessNoTypeEnum.CODE_USER.getCode()));
        entity.setCode(code);
        PassEntity passEntity = PassHandler.buildPassword(password);
        entity.setPassword(passEntity.getPassword());
        //账号
        entity.setUserAccount(mobile);
        entity.setSalt(passEntity.getSalt());
        entity.setNeedChangePwd(Objects.nonNull(needChangePwd)? needChangePwd:false);
        entity.setIsSuper(Objects.nonNull(sysUserInfoDTO.getIsSuper())? sysUserInfoDTO.getIsSuper():false);
        this.save(entity);
        //发送email
        if (Objects.nonNull(createPasswordType) && 0 == createPasswordType){
            sendPwdEmail(entity,randomString);
        }
        return entity.getUid();
    }

    private void sendPwdEmail(SysUserInfoEntity entity,String pwd){
        StringBuilder sb = new StringBuilder();
        sb.append("您好：");
        sb.append("\n");
        sb.append("您的用户名【").append(entity.getUserName()).append("】");
        sb.append("\n");
        sb.append("您的账号【").append(entity.getUserAccount()).append("】");
        sb.append("\n");
        sb.append("初始密码为：【").append(pwd).append("】");
        sb.append("\n");
        sb.append("请登录后及时修改密码！");
        mailService.sendSimpleMail(entity.getEmail(),"用户账号创建",sb.toString(),null);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void update(SysUserInfoDTO sysUserInfoDTO) {
        String uid = sysUserInfoDTO.getUid();
        SysUserInfoEntity entity = this.getById(uid);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        //用户名
        String userName = entity.getUserName();
        //验证用户信息
        checkUserInfo(sysUserInfoDTO);
        entity.setRealName(sysUserInfoDTO.getRealName());
        entity.setMobile(sysUserInfoDTO.getMobile());
        entity.setUserAccount(sysUserInfoDTO.getMobile());
        entity.setUserName(sysUserInfoDTO.getUserName());
        entity.setEmail(sysUserInfoDTO.getEmail());
        List<String> roleIds = sysUserInfoDTO.getRoleIdList();
        boolean updateResult = this.updateById(entity);
        if (updateResult) {
            sysRoleUserService.batchInsertRef(entity.getUid(), roleIds, false);
            //店铺授权
            authUserShopService.batchSaveOrUpdate(entity.getUid(),sysUserInfoDTO.getShopIdList(),sysUserInfoDTO.getShopAuthType());
            //仓库权限
            authUserWarehouseService.batchSaveOrUpdate(entity.getUid(),sysUserInfoDTO.getWarehouseIdList(),sysUserInfoDTO.getWarehouseAuthType());
            //同步金蝶员工数据
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSysUserInfoService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }
        //更新plm任务列表任务负责人名称
        if (!StrUtil.equals(sysUserInfoDTO.getUserName(),userName)) {
            plmTaskFeign.updateProjectTaskChargeName(sysUserInfoDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean updateSrmUser(SysUserInfoDTO sysUserInfoDTO) {
        String uid = sysUserInfoDTO.getUid();
        SysUserInfoEntity entity = this.getById(uid);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        //验证用户信息
        checkUserInfo(sysUserInfoDTO);
        entity.setRealName(sysUserInfoDTO.getRealName());
        entity.setMobile(sysUserInfoDTO.getMobile());
        entity.setUserAccount(sysUserInfoDTO.getMobile());
        entity.setUserName(sysUserInfoDTO.getUserName());
        entity.setEmail(sysUserInfoDTO.getEmail());
        if (Objects.nonNull(sysUserInfoDTO.getUserState())){
            entity.setUserState(sysUserInfoDTO.getUserState());
        }
        return this.updateById(entity);
    }

    /**
     * 账号登录
     *
     * @param dto
     * @return
     */
    @Override
    public SysUserDTO accountLogin(AccountLoginDTO dto) {
        log.info("accountLogin：{}", JSONObject.toJSONString(dto));
        SysUserInfoEntity entity = findByAccount(dto.getAccount(),dto.getUserType());
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
        if (UserStateConstants.USER_DISABLE.equals(userState)) {
            throw new ServiceException(ApiError.ERROR_1011);
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(entity, vo);
        vo.setIsSupper(entity.getIsSuper());
        //判断是否是超级管理员登录
        SysUserDTO sysUserDTO = adminLogin(vo,dto.getUserType());
        if (sysUserDTO != null) {
            return sysUserDTO;
        }

        //后面还有编写 1580852739573813249
        String uid = entity.getUid();
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        List<SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds,dto.getUserType());
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds,MathUtil.ONE,dto.getUserType());
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.NO_STATE,dto.getUserType());
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
        // 当前用户所属部门
        SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysDepartmentUserService.getDeptByUserId(uid);
        vo.setDeptId(sysDepartmentUserNumberDTO.getDepartmentId());
        vo.setDeptName(sysDepartmentUserNumberDTO.getDepartmentName());
        return vo;
    }

    public SysUserDTO getAdminLoginData(SysUserDTO vo) {
        if (!vo.getUserAccount().equals(SysConstant.ADMIN_USER)) {
            return null;
        }
        List<SysMenuVO> menuAll = sysRoleMenuService.findMenuAll(vo.getUserType());
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuAll(vo.getUserType());
        List<String> permissionList = sysRoleMenuService.findMenuCodeAll(vo.getUserType());
        vo.setPermissionList(permissionList);
        vo.setOverallMenuList(menuAll);
        vo.setLeftMenuList(leftMenuList);
        vo.setBindingPlatform("");
        vo.setBindingState(0);
        return vo;
    }

    public SysUserDTO adminLogin(SysUserDTO vo,String userType) {
        if (!vo.getUserAccount().equals(SysConstant.ADMIN_USER)) {
            return null;
        }
        List<SysMenuVO> menuAll = sysRoleMenuService.findMenuAll(userType);
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuAll(MathUtil.ONE, userType);
        List<String> permissionList = sysRoleMenuService.findMenuCodeAll(userType);
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
            return baseMapper.findRoleIfExistList(dto);
        }
        //岗位的
        if (sysType == 2) {
            return baseMapper.findPostIfExistList(dto);
        }
        //部门
        if (sysType == 3) {
            return baseMapper.findDepartmentIfExistList(dto);
        }
        return baseMapper.findList(dto);

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
    public PagingVO<UserManageDTO> paging(PagingDTO<SysUserPagingSearchDTO> dto) {
        Page<UserManageDTO> query = new Page<UserManageDTO>(dto.getCurrPage(), dto.getPageSize());
        SysUserPagingSearchDTO params = dto.getParams();
        IPage<UserManageDTO> pageData = baseMapper.paging(query, params, params.getRoleIds());
        List<UserManageDTO> list = pageData.getRecords();
        List<String> userIds = list.stream().map(UserManageDTO::getUid).collect(Collectors.toList());
        //角色
        List<SysRoleUserEntity> roleUserList = sysRoleUserService.findRoleIdsByUidList(userIds);
        Map<String, List<String>> roleMap = new HashMap<>();
        if (CollUtil.isNotEmpty(roleUserList)){
            roleMap = roleUserList.stream().collect(Collectors.groupingBy(SysRoleUserEntity::getUserId, Collectors.mapping(SysRoleUserEntity::getRoleId, Collectors.toList())));
        }
        //店铺
        List<SysUserDTO.ShopDTO> shopDTOList = authUserShopService.listShopIdByUserIds(userIds);
        Map<String, List<String>> shopMap = new HashMap<>();
        Map<String, String> shopAuthTypeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(shopDTOList)){
            shopMap = shopDTOList.stream().collect(Collectors.groupingBy(SysUserDTO.ShopDTO::getUserId, Collectors.mapping(SysUserDTO.ShopDTO::getShopId, Collectors.toList())));
            shopAuthTypeMap = shopDTOList.stream().collect(Collectors.toMap(SysUserDTO.ShopDTO::getUserId, SysUserDTO.ShopDTO::getAuthType,(existing,replacement) -> existing));
        }
        //仓库
        List<SysUserDTO.WarehouseDTO> warehouseDTOList = authUserWarehouseService.listWarehouseIdByUserIds(userIds);
        Map<String, List<String>> warehouseMap = new HashMap<>();
        Map<String, String> warehouseAuthTypeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(warehouseDTOList)){
            warehouseMap = warehouseDTOList.stream().collect(Collectors.groupingBy(SysUserDTO.WarehouseDTO::getUserId, Collectors.mapping(SysUserDTO.WarehouseDTO::getWarehouseId, Collectors.toList())));
            warehouseAuthTypeMap = warehouseDTOList.stream().collect(Collectors.toMap(SysUserDTO.WarehouseDTO::getUserId, SysUserDTO.WarehouseDTO::getAuthType,(existing,replacement) -> existing));
        }
        for (UserManageDTO vo : list) {
            vo.setRoleIdList(roleMap.getOrDefault(vo.getUid(), Collections.emptyList()));
            vo.setShopAuthType(shopAuthTypeMap.getOrDefault(vo.getUid(), AuthDataTypeEnum.ENUM_ALL.getCode()));
            vo.setShopIdList(shopMap.getOrDefault(vo.getUid(),Collections.emptyList()));
            vo.setWarehouseAuthType(warehouseAuthTypeMap.getOrDefault(vo.getUid(), AuthDataTypeEnum.ENUM_ALL.getCode()));
            vo.setWarehouseIdList(warehouseMap.getOrDefault(vo.getUid(),Collections.emptyList()));
        }
        return new PagingVO<>(pageData);
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
        String thirdOpenId = "";
        String thirdUserId = "";
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
            if (fsUserMap != null && fsUserMap.containsKey("open_id")) {
                thirdOpenId = fsUserMap.get("open_id").toString();
            }
            if (fsUserMap != null && fsUserMap.containsKey("user_id")) {
                thirdUserId = fsUserMap.get("user_id").toString();
            }
        }
        //当不为空的时候
        if (StringUtils.isNotBlank(flagId)) {
            LoginUser loginUser = UserContext.getLoginUser();
            String uid = loginUser.getUid();
            boolean ifBinding = sysUserThirdService.checkIfBinding(uid, flagId, bindingPlatform);
            if (ifBinding) {
                throw new ServiceException(ApiError.ERROR_9020);
            }
            sysUserThirdService.bindingThirdParty(uid, flagId,thirdOpenId,thirdUserId, bindingPlatform);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void updateState(UpdateUserStateDTO stateDTO) {
        LambdaUpdateWrapper<SysUserInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysUserInfoEntity::getUserState, stateDTO.getState());
        updateWrapper.in(SysUserInfoEntity::getUid, stateDTO.getIds());
        this.update(updateWrapper);

        //禁用清除redis登录信息
        if (ObjectUtil.isNotEmpty(stateDTO.getState()) && MathUtil.compareTo(stateDTO.getState(),MathUtil.ZERO) == MathUtil.ZERO) {
            stateDTO.getIds().forEach(uid -> redisService.deleteObject(RedisCacheConstants.LOGIN_TOKEN_KEY + uid));
        }

        List<SysUserInfoEntity> list = this.listByIds(stateDTO.getIds());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (SysUserInfoEntity entity : list) {
            String operate = MathUtil.ZERO.equals(stateDTO.getState()) ? SyncOperateEnum.OPERATE_DISABLE.getCode() : SyncOperateEnum.OPERATE_ENABLE.getCode();
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSysUserInfoService.syncDataToKingdee(entity, operate);
            resultList.add(pushTaskEntity);
        }
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void updateStateSrm(UpdateUserStateDTO stateDTO) {
        LambdaUpdateWrapper<SysUserInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysUserInfoEntity::getUserState, stateDTO.getState());
        updateWrapper.in(SysUserInfoEntity::getUid, stateDTO.getIds());
        this.update(updateWrapper);

        //禁用清除redis登录信息
        if (ObjectUtil.isNotEmpty(stateDTO.getState()) && MathUtil.compareTo(stateDTO.getState(),MathUtil.ZERO) == MathUtil.ZERO) {
            stateDTO.getIds().forEach(uid -> redisService.deleteObject(RedisCacheConstants.LOGIN_TOKEN_KEY + uid));
        }
    }

    @Override
    public List<SysUserInfoEntity> listErpUser() {
        return this.lambdaQuery().eq(SysUserInfoEntity::getUserType, UserTypeEnum.ERP.getCode()).list();
    }

    /**
     * 远程搜索
     *
     * @param dto
     * @return ApiResult
     */
    @Override
    public PagingVO<UserSelectDto.PageSelectDTO> pagingSelect(PagingDTO<UserSelectDto.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<UserSelectDto.PageSelectDTO> pageData = this.baseMapper.pagingSelect(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        Integer notState = SysConstant.NO_STATE;
        LoginUser loginUser = UserContext.getLoginUser();

        pageData.getRecords().forEach(item -> {
            Integer userState = item.getUserState();
            item.setDisabled(notState.equals(userState));
            item.setIsMyState(Objects.nonNull(loginUser) && Objects.equals(loginUser.getUid(), item.getUserId()) ? 1 : 0);
        });
        return new PagingVO<>(pageData);
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

        LoginUser loginUser = UserContext.getLoginUser();
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
            infoEntity.setNeedChangePwd(false);
            //更改密码
            this.updateById(infoEntity);
            //退出登录 清除token
            sysAuthFeign.logout(loginUser.getAccessToken());
            UserContext.clear();

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
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        Integer userState = userEntity.getUserState();
        //表示禁用
        if (UserStateConstants.USER_DISABLE.equals(userState)) {
            throw new ServiceException(ApiError.ERROR_1011);
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(userEntity, vo);
        //后面还有编写
        String uid = userEntity.getUid();

        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        List<SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds,userEntity.getUserType());
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds,MathUtil.ONE,userEntity.getUserType());
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.NO_STATE,userEntity.getUserType());
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
        LoginUser loginUser = UserContext.getLoginUser();
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
        LoginUser loginUser = UserContext.getLoginUser();
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
        LoginUser loginUser = UserContext.getLoginUser();
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
    public void sendEmail(EmailVerifyCodeDTO dto) {
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
        Boolean sendResult = mailService.sendVerifyCode(emailDTO);
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
        LoginUser loginUser = UserContext.getLoginUser();
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
        LoginUser loginUser = UserContext.getLoginUser();
        Boolean flag = !Objects.isNull(loginUser);
        if (flag) {
            FindUserDTO user = new FindUserDTO();
            user.setIsMyState(1);
            user.setUserId(loginUser.getUid());
            user.setUserName(loginUser.getUserName());
            user.setDisabled(Boolean.FALSE);
            resultList.add(user);
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysUserInfoEntity::getUid, SysUserInfoEntity::getUserName,SysUserInfoEntity::getUserState);
        queryWrapper.eq(SysUserInfoEntity::getDeleteState, SysConstant.YES_STATE);
        queryWrapper.eq(SysUserInfoEntity::getUserType, UserTypeEnum.ERP.getCode());
        if (flag) {
            queryWrapper.ne(SysUserInfoEntity::getUid, loginUser.getUid());
        }
        if (StringUtils.isNotBlank(dto.getSearchKeyword())) {
            queryWrapper.like(SysUserInfoEntity::getUserName, dto.getSearchKeyword());
        }
        queryWrapper.orderByAsc(SysUserInfoEntity::getUserName);
        List<SysUserInfoEntity> list = this.list(queryWrapper);
        Integer notState=SysConstant.NO_STATE;
        for (SysUserInfoEntity item : list) {
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(item.getUid());
            userDTO.setUserName(item.getUserName());
            Integer userState=item.getUserState();
            userDTO.setDisabled(notState.equals(userState));
            userDTO.setIsMyState(0);
            resultList.add(userDTO);
        }
        return resultList;
    }

    @Override
    public List<FindUserDTO> getAuthorityUserList(BaseSearchDTO dto) {
        List<FindUserDTO> resultList = new LinkedList<>();
        return resultList;
    }


    @Override
    public List<FindUserDTO> getAllUserList() {
        List<FindUserDTO> resultList = new LinkedList<>();
        List<SysUserInfoEntity> list = this.lambdaQuery().eq(SysUserInfoEntity::getUserType, UserTypeEnum.ERP.getCode()).list();;
        for (SysUserInfoEntity item : list) {
            Integer userState = item.getUserState();
            if (userState == 0) {
                continue;
            }
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(item.getUid());
            userDTO.setUserName(item.getUserName());
            userDTO.setRealName(item.getRealName());
            userDTO.setCode(item.getCode());
            userDTO.setSyncKingdeeId(item.getSyncKingdeeId());
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

    public SysUserInfoEntity findByAccount(String account, String userType) {
        //暂时将pda账号重置为erp pda和erp共用用户体系
        if (UserTypeEnum.PDA.code.equals(userType)){
            userType = UserTypeEnum.ERP.code;
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getUserAccount, account)
                .eq(StringUtils.isNotBlank(userType), SysUserInfoEntity::getUserType, userType)
                .eq(SysUserInfoEntity::getDeleteState, 1);
        queryWrapper.last("LIMIT 1");
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        return entity;
    }


    /**
     * @param sysUserInfoDTO
     * @return boolean
     * @description: 用户验证信息
     * @author Will
     * @date: 2023/3/2 10:37
     */
    private void checkUserInfo(SysUserInfoDTO sysUserInfoDTO) {
        //验证手机号是否已存在
        LambdaQueryWrapper<SysUserInfoEntity> mobileQueryWrapper = new LambdaQueryWrapper<>();
        mobileQueryWrapper.eq(SysUserInfoEntity::getUserAccount, sysUserInfoDTO.getMobile());
        mobileQueryWrapper.eq(SysUserInfoEntity::getDeleteState, SysConstant.YES_STATE);
        if (StringUtils.isNotBlank(sysUserInfoDTO.getUid())) {
            mobileQueryWrapper.ne(SysUserInfoEntity::getUid, sysUserInfoDTO.getUid());
        }
        if (StringUtils.isNotEmpty(sysUserInfoDTO.getUserType())){
            mobileQueryWrapper.eq(SysUserInfoEntity::getUserType, sysUserInfoDTO.getUserType());
        }
        int mobileCount = this.count(mobileQueryWrapper);
        if (mobileCount > 0) {
            throw new ServiceException(ApiError.ERROR_9010);
        }
        //验证用户名是否已存在
        LambdaQueryWrapper<SysUserInfoEntity> userNameQueryWrapper = new LambdaQueryWrapper<>();
        userNameQueryWrapper.eq(SysUserInfoEntity::getUserName, sysUserInfoDTO.getUserName());
        userNameQueryWrapper.eq(SysUserInfoEntity::getDeleteState, SysConstant.YES_STATE);
        if (StringUtils.isNotBlank(sysUserInfoDTO.getUid())) {
            userNameQueryWrapper.ne(SysUserInfoEntity::getUid, sysUserInfoDTO.getUid());
        }
        if (StringUtils.isNotEmpty(sysUserInfoDTO.getUserType())){
            userNameQueryWrapper.eq(SysUserInfoEntity::getUserType, sysUserInfoDTO.getUserType());
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
    @Override
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
    @Override
    public List<String> getDepUserList(String userId) {
        List<SysDepartmentTreeDTO> treeList = sysDepartmentMapper.findTree();
        List<String> userDepList = baseMapper.getUserDepList(userId);
        List<String> deptList = new LinkedList<>();
        for (String s : userDepList) {
            if (CollectionUtils.isNotEmpty(treeList)) {
                for (SysDepartmentTreeDTO vo : treeList) {
                    if (StringUtils.isNotBlank(s)&&vo.getPath().contains(s)) {
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
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserInfoEntity::getUid, userIds);
        List<FindUserDTO> resultList = new LinkedList<>();
        List<SysUserInfoEntity> list = this.list(queryWrapper);
        for (SysUserInfoEntity item : list) {
            // 当前用户所属部门
            SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysDepartmentUserService.getDeptByUserId(item.getUid());

            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(item.getUid());
            userDTO.setUserName(item.getUserName());
            userDTO.setCode(item.getCode());
            userDTO.setIsMyState(0);
            userDTO.setDepartmentId(sysDepartmentUserNumberDTO.getDepartmentId());
            userDTO.setDepartmentName(sysDepartmentUserNumberDTO.getDepartmentName());
            resultList.add(userDTO);
        }
        return resultList;
    }

    @Override
    public FindUserDTO getUserByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return new FindUserDTO();
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getUid, userId);
        queryWrapper.eq(SysUserInfoEntity::getDeleteState, IsConstant.YES);
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        if (!Objects.isNull(entity)) {
            SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysDepartmentUserService.getDeptByUserId(userId);
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(entity.getUid());
            userDTO.setUserName(entity.getUserName());
            userDTO.setCode(entity.getCode());
            userDTO.setMobile(entity.getMobile());
            userDTO.setEmail(entity.getEmail());
            userDTO.setRealName(entity.getRealName());
            userDTO.setIsMyState(0);
            if (Objects.nonNull(sysDepartmentUserNumberDTO)) {
                userDTO.setDepartmentId(sysDepartmentUserNumberDTO.getDepartmentId());
                userDTO.setDepartmentName(sysDepartmentUserNumberDTO.getDepartmentName());
            }
            return userDTO;
        }
        return new FindUserDTO();


    }

    @Override
    public FindUserDTO getUserByUserName(String userName,String userType) {
        if (StringUtils.isBlank(userName)|| StringUtils.isEmpty(userType)) {
            return new FindUserDTO();
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getUserName, userName);
        queryWrapper.eq(SysUserInfoEntity::getUserType, userType);
        queryWrapper.last("limit 1");
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        if (!Objects.isNull(entity)) {
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(entity.getUid());
            userDTO.setUserName(entity.getUserName());
            userDTO.setIsMyState(0);
            //部门信息
            SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysDepartmentUserService.getDeptByUserId(entity.getUid());
            if (Objects.nonNull(sysDepartmentUserNumberDTO)) {
                userDTO.setDepartmentId(sysDepartmentUserNumberDTO.getDepartmentId());
                userDTO.setDepartmentName(sysDepartmentUserNumberDTO.getDepartmentName());
            }
            return userDTO;
        }
        return new FindUserDTO();
    }

    @Override
    public List<FindUserDTO> listUserByUserNames(List<String> userNames, String userType) {
        if (CollectionUtils.isEmpty(userNames) || StringUtils.isEmpty(userType)) {
            return Collections.EMPTY_LIST;
        }
        List<SysUserInfoEntity> list = lambdaQuery()
                .eq(SysUserInfoEntity::getUserType,userType)
                .in(SysUserInfoEntity::getUserName, userNames).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<FindUserDTO> resultList = new ArrayList<>();
        for (SysUserInfoEntity entity : list) {
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(entity.getUid());
            userDTO.setUserName(entity.getUserName());
            userDTO.setIsMyState(0);
            resultList.add(userDTO);
        }
        return resultList;
    }


    /**
     * 获取所有用户所在的部门
     *
     * @return java.util.List<com.erp.model.sys.dto.SysUserDeptDTO>
     * @Author Luo_WG
     * @Date 2022/12/13 17:12
     **/
    @Override
    public List<SysUserDeptDTO> getUserDeptList() {
        return baseMapper.getUserDeptList();
    }


    @Override
    public List<UserSuperiorDTO> listSuperiorByUserIds(List<String> userIds) {
        List<UserSuperiorDTO> parentList = new ArrayList<>();

        for (String userId : userIds) {
            List<UserSuperiorDTO> superList = sysDepartmentUserService.listSuperiorByUserId(userId);
            if (CollectionUtils.isEmpty(superList)) {
                continue;
            }
            List<UserSuperiorDTO> collect = superList.stream().map(entity -> entity.setSuperiorType(ChargeSuperiorEnum.getName(entity.getLevel()))).collect(Collectors.toList());

            //添加直属上级
            UserSuperiorDTO newSuperiorDTO = new UserSuperiorDTO();
            UserSuperiorDTO userSuperiorDTO = superList.stream()
                    .sorted(Comparator.comparing(UserSuperiorDTO::getLevel))
                    .filter(superior -> superior.getLevel() >= ChargeSuperiorEnum.DIRECT_SUPERIOR.getCode())
                    .findFirst()
                    .orElse(null);
            BeanMapperUtils.copy(userSuperiorDTO, newSuperiorDTO);
            newSuperiorDTO.setSuperiorType(ChargeSuperiorEnum.DIRECT_SUPERIOR.getName());
            parentList.add(newSuperiorDTO);
            parentList.addAll(collect);
        }
        return parentList;
    }

    @Override
    public List<UserSuperiorDTO> listDeptByUserIds(List<String> userIds) {
        if (CollUtil.isEmpty(userIds)){
            return Collections.emptyList();
        }
        List<UserSuperiorDTO> resultList = new ArrayList<>();
        for (String userId : userIds){
            List<UserSuperiorDTO> userSuperiorDTOS = sysDepartmentUserService.listDeptByUserId(userId);
            if (CollUtil.isEmpty(userSuperiorDTOS)){
                continue;
            }
            resultList.addAll(userSuperiorDTOS);
        }
        return resultList;
    }


    /**
     * 根据用户id 获取用户登录的信息
     * 用于 token 获取用户信息内容
     *
     * @param userId
     * @return com.erp.model.sys.dto.SysUserDTO
     * @author yl
     * @date 2023-01-14 9:45
     */
    @Override
    public SysUserDTO getSysUserById(String userId) {
        if (StringUtils.isBlank(userId)) {
            return new SysUserDTO();
        }
        SysUserInfoEntity entity = this.getById(userId);
        if (Objects.isNull(entity)) {
            return null;
        }
        Integer userState = entity.getUserState();
        //表示禁用
        if (UserStateConstants.USER_DISABLE.equals(userState)) {
            throw new ServiceException(ApiError.ERROR_1011);
        }
        SysUserDTO vo = new SysUserDTO();
        BeanMapperUtils.copy(entity, vo);

        //判断是否是超级管理员登录
        SysUserDTO sysUserDTO = getAdminLoginData(vo);
        if (sysUserDTO != null) {
            return sysUserDTO;
        }

        //后面还有编写 1580852739573813249
        String uid = entity.getUid();
        List<String> roleIds = sysRoleUserService.findRoleIdsByUid(uid);
        List<SysMenuVO> overallMenuList = sysRoleMenuService.findMenuByRoleIds(roleIds, entity.getUserType());
        List<SysMenuVO> leftMenuList = sysRoleMenuService.findLeftMenuByRoleIds(roleIds, entity.getUserType());
        List<String> permissionList = sysRoleMenuService.findMenuCodeByRoleIds(roleIds, SysConstant.NO_STATE,entity.getUserType());
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
    public boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SysUserInfoEntity::getUid, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SysUserInfoEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void deleteByIds(List<String> uids) {
        List<SysUserInfoEntity> list = this.listByIds(uids);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        this.removeByIds(uids);
        //同步金蝶员工数据
        List<DmpPushTaskEntity> restList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSysUserInfoService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode());
            restList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(restList);
            }
        });
    }

    /**
     * 重置密码
     *
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/20 9:46
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changePassword(String uid) {
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
            redisService.deleteObject(RedisCacheConstants.LOGIN_TOKEN_KEY + uid);
        }
        return flag;
    }

    public static void main(String[] args) {
        String admin12345 = Md5Util.md5("admin12345");
        System.out.println(admin12345);
    }

    @Override
    public Boolean changePassword(String uid, String pwd) {
        log.info("changePassword：uid：{}，pwd：{}",uid,pwd);
        if (StringUtils.isBlank(uid)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        if(StringUtils.isEmpty(pwd)){
            throw new ServiceException(ApiError.ERROR_9015);
        }
        SysUserInfoEntity userInfoEntity = this.getById(uid);
        if (Objects.isNull(userInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_9043);
        }
        //强制退出账号

        //产生一个6位数的随机码
        String salt = RandomStringUtils.randomAlphabetic(10);
        //加密后的密码
        String encryptPassword = Md5Util.md5(Md5Util.md5(pwd) + salt);
        boolean flag = lambdaUpdate()
                .set(SysUserInfoEntity::getSalt, salt)
                .set(SysUserInfoEntity::getPassword, encryptPassword)
                .set(SysUserInfoEntity::getNeedChangePwd, Boolean.TRUE)
                .eq(SysUserInfoEntity::getUid, uid).update();
        redisService.deleteObject(RedisCacheConstants.LOGIN_TOKEN_KEY + uid);
        String loginErrorKey = StrUtil.format(RedisCacheConstants.LOGIN_ERROR_KEY, userInfoEntity.getUserType(), userInfoEntity.getUserAccount());
        redisService.deleteObject(loginErrorKey);
        return flag;
    }

    /**
     * 忘记密码
     *
     * @param forgotPasswordDTO forgotPasswordDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/20 11:18
     **/
    @Override
    public Boolean forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        SysUserInfoEntity sysUserInfoEntity = lambdaQuery()
                .eq(SysUserInfoEntity::getUserAccount, forgotPasswordDTO.getUserAccount())
                .eq(SysUserInfoEntity::getUserType,forgotPasswordDTO.getUserType())
                .eq(SysUserInfoEntity::getDeleteState,1)
                .one();
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

        redisService.deleteObject(RedisCacheConstants.LOGIN_TOKEN_KEY + sysUserInfoEntity.getUid());
        String loginErrorKey = StrUtil.format(RedisCacheConstants.LOGIN_ERROR_KEY, forgotPasswordDTO.getUserType(), forgotPasswordDTO.getUserAccount());
        redisService.deleteObject(loginErrorKey);
        return flag;
    }

    /**
     * 忘记密码-获取验证码
     *
     * @param userAccount userAccount
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/20 11:45
     **/
    @Override
    public Map<String, Object> forgotPasswordGetCode(String userAccount,String userType) {
        SysUserInfoEntity sysUserInfoEntity = lambdaQuery().eq(SysUserInfoEntity::getUserAccount, userAccount).eq(SysUserInfoEntity::getUserType,userType)
                .eq(SysUserInfoEntity::getDeleteState,1)
                .one();
        if (ObjectUtil.isEmpty(sysUserInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_9043);
        }

        if (StringUtils.isBlank(sysUserInfoEntity.getEmail())) {
            throw new ServiceException(ApiError.ERROR_9044);
        }
        EmailVerifyCodeDTO dto = new EmailVerifyCodeDTO();
        dto.setEmail(sysUserInfoEntity.getEmail());
        sendEmail(dto);
        Map<String, Object> map = new HashMap<>();
        map.put("msg", String.format("已给<'%s'>成功发送验证码，请在邮箱查看", sysUserInfoEntity.getEmail()));
        return map;
    }

    @Override
    public Map<String, Object> getCodeByAccountAndType(String phone,String userType) {
        SysUserInfoEntity sysUserInfoEntity = lambdaQuery().eq(SysUserInfoEntity::getUserAccount, phone).eq(SysUserInfoEntity::getUserType,userType).one();
        if (ObjectUtil.isEmpty(sysUserInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_9043);
        }

        if (StringUtils.isBlank(sysUserInfoEntity.getEmail())) {
            throw new ServiceException(ApiError.ERROR_9044);
        }
        EmailVerifyCodeDTO dto = new EmailVerifyCodeDTO();
        dto.setEmail(sysUserInfoEntity.getEmail());
        sendEmail(dto);
        Map<String, Object> map = new HashMap<>();
        map.put("msg", String.format("已给<'%s'>成功发送验证码，请在邮箱查看", sysUserInfoEntity.getEmail()));
        return map;
    }

    @Override
    public List<SysUserSimpleDTO> getUserSimpleInfoByIds(List<String> userIds) {
        List<SysUserInfoEntity> users = this.lambdaQuery().eq(SysUserInfoEntity::getUserState, 1).in(SysUserInfoEntity::getUid, userIds).list();
        if (CollUtil.isNotEmpty(users)) {
            return BeanMapperUtils.copyList(SysUserSimpleDTO.class, users);
        }
        return null;
    }

    @Override
    public List<FindUserDTO> getUserListByRoleIds(SysFeignDTO.ListByRoleIdsDTO dto) {
        List<String> roleIds = dto.getRoleIds();
        return baseMapper.getListByRoleIds(roleIds);
    }


    /**
     * 根据搜索关键字 获取到用户信息
     *
     * @param searchKeyword
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     * @author yl
     * @date 2023-04-26 18:14
     */
    @Override
    public List<FindUserDTO> listBySearchKeyword(String searchKeyword) {

        List<FindUserDTO> resultList = new LinkedList<>();
        //先添加自己
        LoginUser loginUser = UserContext.getLoginUser();
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
        if (StringUtils.isNotBlank(searchKeyword)) {
            queryWrapper.like(SysUserInfoEntity::getUserName, searchKeyword);
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


    /**
     * 上传头像
     *
     * @param headPhotoFile
     * @return
     */
    @Override
    public Boolean uploadHeadPhoto(MultipartFile headPhotoFile) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        SysUserInfoEntity userInfo = this.getById(userId);
        if (Objects.isNull(userInfo)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }

        String headPhotoUrl = filefeign.uploadFile(headPhotoFile);
        if (StringUtils.isNotBlank(headPhotoUrl)) {
            userInfo.setHeadIcon(headPhotoUrl);
            return this.updateById(userInfo);
        }
        return Boolean.FALSE;
    }

    /**
     * 根据金蝶code获取用户信息
     *
     * @param kingdeeCodeList
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     * @author yl
     * @date 2023-06-27 15:31
     */
    @Override
    public List<FindUserDTO> listUserByKingdeeCode(List<String> kingdeeCodeList) {
        if (CollectionUtils.isEmpty(kingdeeCodeList)) {
            return Collections.emptyList();
        }
        List<SysUserInfoEntity> userList = this.lambdaQuery().in(SysUserInfoEntity::getCode, kingdeeCodeList).list();
        List<FindUserDTO> resultList = new ArrayList<>(userList.size());
        for (SysUserInfoEntity item : userList) {
            FindUserDTO findUser = new FindUserDTO();
            findUser.setUserId(item.getUid());
            findUser.setUserName(item.getUserName());
            findUser.setRealName(item.getRealName());
            findUser.setCode(item.getCode());
            resultList.add(findUser);
        }
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importUserKingdee(MultipartFile file) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = wb.getSheetAt(0);
        // 读取数据集
        int rows = sheet.getPhysicalNumberOfRows();

        for (int i = 2; i < rows; i++) {
            XSSFRow row = sheet.getRow(i);

            // 用户名称
            String name = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(1)));
            // 金蝶id
            String kingdeeId = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(0)));
            // 金蝶编码
            String code = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(2)));
            LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserInfoEntity::getUserName, name);
            queryWrapper.last("LIMIT 1");
            SysUserInfoEntity sysUserInfoEntity = super.getOne(queryWrapper);
            if (Objects.isNull(sysUserInfoEntity)) {
                log.info("未找到人员【{}】", name);
                continue;
            }
            if (StrUtils.isNotEmpty(sysUserInfoEntity.getSyncKingdeeId())) {
                log.info("人员【{}】已经存在金蝶id，不处理", name);
                continue;
            }
            lambdaUpdate().set(SysUserInfoEntity::getSyncKingdeeId, kingdeeId)
                    .set(SysUserInfoEntity::getCode, code)
                    .eq(SysUserInfoEntity::getUid, sysUserInfoEntity.getUid())
                    .update();
        }
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
        Boolean sendResult = mailService.sendVerifyCode(emailDTO);
        return sendResult;
    }

    @Override
    public List<SysUserInfoEntity> listUserByDept(String deptName) {
        List<SysDepartmentTreeDTO> treeList = baseMapper.listSonDeptAll(deptName);
        if (CollectionUtils.isEmpty(treeList)){
            return Collections.emptyList();
        }
        List<String> deptIds = treeList.stream().map(SysDepartmentTreeDTO::getId).distinct().collect(Collectors.toList());
        return baseMapper.listUserByDept(deptIds);
    }

    @Override
    public PagingVO shopAuthPaging(PagingDTO<SysUserInfoDTO.ShopAuthPagingSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SysUserInfoDTO.ShopAuthPagingSearchDTO params = dto.getParams();
        List<String> userIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(params.getShopIdList())) {
             userIdList = authDataFeign.listUserIdByShopIdList(params.getShopIdList());
             if (CollectionUtils.isEmpty(userIdList)) {
                 return new PagingVO<>(new Page<>());
             }
        }
        IPage<SysUserInfoDTO.ShopAuthPagingDTO> pageData = baseMapper.shopAuthPaging(query, params,userIdList);
        List<SysUserInfoDTO.ShopAuthPagingDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO<>(pageData);
        }
        handleData(records);
        return new PagingVO(pageData);
    }

    @Override
    public void updateSysUserTime(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return;
        }
        lambdaUpdate().in(SysUserInfoEntity::getUid,userIdList)
                .update(new SysUserInfoEntity());
    }

    @Override
    public PagingVO<SupplierUserVO> srmPaging(PagingDTO<UserPagingSearchDTO> dto) {
        Page<SupplierUserVO> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SupplierUserVO> userVOIPage = baseMapper.srmPaging(page, dto.getParams());
        return new PagingVO<>(userVOIPage);
    }

    @Override
    public FindUserDTO getUserByMobile(String mobile, String userType) {
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(userType)) {
            return null;
        }
        LambdaQueryWrapper<SysUserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserInfoEntity::getMobile, mobile);
        queryWrapper.eq(SysUserInfoEntity::getUserType, userType);
        queryWrapper.last("limit 1");
        SysUserInfoEntity entity = this.getOne(queryWrapper);
        if (!Objects.isNull(entity)) {
            FindUserDTO userDTO = new FindUserDTO();
            userDTO.setUserId(entity.getUid());
            userDTO.setUserName(entity.getUserName());
            userDTO.setIsMyState(0);
            return userDTO;
        }
        return new FindUserDTO();
    }

    @Override
    public List<SupplierUserVO> srmList(UserPagingSearchDTO dto) {
        return baseMapper.srmList(dto);
    }

    /**
     * 处理数据
     */
    private void handleData (List<SysUserInfoDTO.ShopAuthPagingDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<String> userIdList = records.stream().map(SysUserInfoDTO.ShopAuthPagingDTO::getUserId).collect(Collectors.toList());
        List<ShopSysUserAuthDTO.ViewDTO> viewList = shopSysUserAuthFeign.listShopSysUserAuthByUserIdList(userIdList);
        if (CollectionUtils.isEmpty(viewList)) {
            return;
        }
        for (SysUserInfoDTO.ShopAuthPagingDTO shopAuthPagingDTO : records) {
            ShopSysUserAuthDTO.ViewDTO viewDTO = viewList.stream().filter(obj -> obj.getUserId().equals(shopAuthPagingDTO.getUserId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(viewDTO) || CollectionUtils.isEmpty(viewDTO.getDetailList())) {
                continue;
            }
            String shopNames = viewDTO.getDetailList().stream().filter(obj -> !obj.getDisabled()).map(ShopSysUserAuthDTO.ViewShopDTO::getShopName).collect(Collectors.joining(","));
            shopAuthPagingDTO.setShopNames(shopNames);
        }
    }


    @Override
    public void syncFsUser() {
        List<SysUserThirdEntity> sysUserThird = sysUserThirdService.lambdaQuery().eq(SysUserThirdEntity::getThirdPartyType, ThirdPlatformEnums.FS.code).list();
        if(CollUtil.isNotEmpty(sysUserThird)){
            Map<String, SysUserThirdEntity> sysUserThirdMap = sysUserThird.stream().collect(Collectors.toMap(SysUserThirdEntity::getUserId, e -> e));

            List<String> uids = sysUserThird.stream().map(SysUserThirdEntity::getUserId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            //获取飞书平台
            List<SysUserInfoEntity> list = lambdaQuery()
                    .in(SysUserInfoEntity::getUid, uids)
                    .eq(SysUserInfoEntity::getUserState, 1)
                    .list();
            list = list.stream().filter(e -> StringUtils.isNotBlank(e.getMobile()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(list)){
                // 分割为每 50 条数据一个子列表
                List<List<SysUserInfoEntity>> partitions = splitList(list, 50);
                List<SysUserThirdEntity> result = new ArrayList<>();
                for (List<SysUserInfoEntity> partition : partitions) {
                    List<String> mobiles = partition.stream().map(SysUserInfoEntity::getMobile).distinct().collect(Collectors.toList());
                    String[] mobileArray = mobiles.toArray(new String[0]);

                    FindThirdUserDTO.UserParamsDTO params = new FindThirdUserDTO.UserParamsDTO();
                    params.setMobiles(mobileArray);
                    params.setUserIdType(UserIdTypeEnum.OPEN_ID.getValue());
                    //根据手机号码查询openId
                    BatchGetIdUserResp batchGetOpenIdResp = fsService.getBatchFsUserByMobileOrEmail(params);
                    UserContactInfo[] thirdOpenId = batchGetOpenIdResp.getData().getUserList();
                    Map<String, String> mobileToOpendIdMap = Arrays.stream(thirdOpenId)
                            .filter(Objects::nonNull) // 过滤掉可能的 null 值
                            .filter(e -> Objects.nonNull(e.getUserId())) // 过滤掉可能的 null 值
                            .collect(Collectors.toMap(
                                    UserContactInfo::getMobile, // Key: mobile
                                    UserContactInfo::getUserId,  // Value: userId
                                    (existing, replacement) -> existing // 遇到重复 key，保留已存在的值
                            ));
                    //根据手机号码查询userId
                    params.setUserIdType(UserIdTypeEnum.USER_ID.getValue());
                    BatchGetIdUserResp batchGetUserIdResp = fsService.getBatchFsUserByMobileOrEmail(params);
                    UserContactInfo[] thirdUserIdList = batchGetUserIdResp.getData().getUserList();
                    Map<String, String> mobileToUserIdMap = Arrays.stream(thirdUserIdList)
                            .filter(Objects::nonNull) // 过滤掉可能的 null 值
                            .filter(e -> Objects.nonNull(e.getUserId())) // 过滤掉可能的 null 值
                            .collect(Collectors.toMap(
                                    UserContactInfo::getMobile, // Key: mobile
                                    UserContactInfo::getUserId,  // Value: userId
                                    (existing, replacement) -> existing // 遇到重复 key，保留已存在的值
                            ));
                    //根据手机号码查询unionId
                    params.setUserIdType(UserIdTypeEnum.UNION_ID.getValue());
                    BatchGetIdUserResp batchGetUnionIdResp = fsService.getBatchFsUserByMobileOrEmail(params);
                    UserContactInfo[] thirdUnionIdList = batchGetUnionIdResp.getData().getUserList();
                    Map<String, String> mobileToUnionIdMap = Arrays.stream(thirdUnionIdList)
                            .filter(Objects::nonNull) // 过滤掉可能的 null 值
                            .filter(e -> Objects.nonNull(e.getUserId())) // 过滤掉可能的 null 值
                            .collect(Collectors.toMap(
                                    UserContactInfo::getMobile, // Key: mobile
                                    UserContactInfo::getUserId,  // Value: userId
                                    (existing, replacement) -> existing // 遇到重复 key，保留已存在的值
                            ));
                    for (SysUserInfoEntity sysUserInfoEntity : partition) {
                        SysUserThirdEntity sysUserThirdEntity = sysUserThirdMap.getOrDefault(sysUserInfoEntity.getUid(),null);
                        if(Objects.isNull(sysUserThirdEntity)){
                            continue;
                        }

                        String unionId = mobileToUnionIdMap.getOrDefault(sysUserInfoEntity.getMobile(), "");
                        if(StringUtils.isNotBlank(unionId) && StringUtils.isBlank(sysUserThirdEntity.getThirdUnionId())){
                            sysUserThirdEntity.setThirdUnionId(unionId);
                        }

                        String openId = mobileToOpendIdMap.getOrDefault(sysUserInfoEntity.getMobile(), "");
                        if(StringUtils.isNotBlank(openId)){
                            sysUserThirdEntity.setThirdOpenId(openId);

                        }
                        String userId = mobileToUserIdMap.getOrDefault(sysUserInfoEntity.getMobile(), "");
                        if(StringUtils.isNotBlank(userId)){
                            sysUserThirdEntity.setThirdUserId(userId);
                        }
                        if(StringUtils.isNotBlank(unionId) || StringUtils.isNotBlank(openId) || StringUtils.isNotBlank(userId)){
                            result.add(sysUserThirdEntity);
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(result)){
                    sysUserThirdService.saveOrUpdateBatch(result);
                }
            }
        }
    }


    public static <T> List<List<T>> splitList(List<T> list, int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        int partitionSize = Math.max(1, size); // 确保分区大小至少为1
        return IntStream.range(0, (list.size() + partitionSize - 1) / partitionSize)
                .mapToObj(i -> list.subList(i * partitionSize, Math.min(list.size(), (i + 1) * partitionSize)))
                .collect(Collectors.toList());
    }
}