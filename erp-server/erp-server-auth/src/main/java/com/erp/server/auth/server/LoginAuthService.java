package com.erp.server.auth.server;

import java.util.Date;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.common.business.vo.LoginUser;
import com.common.core.utils.MessageUtils;
import org.springframework.stereotype.Component;

import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.UserTypeEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.IpUtils;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.AccountLoginDTO;
import com.erp.model.sys.dto.SysFeignDTO;
import com.erp.model.sys.dto.SysLoginIpDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.vo.SysLoginUserVO;
import com.erp.model.sys.vo.SysUserMenuAuthVO;
import com.erp.model.sys.vo.SysUserPermissionAuthVO;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;

import cn.hutool.core.util.StrUtil;

/**
 * 登录认证服务
 * <p>
 * 负责账号登录、Token 校验，以及登录后菜单/按钮权限的独立获取。
 * </p>
 *
 * @Classname LoginAuthService
 * @Date 2022-07-08
 */
@Component
public class LoginAuthService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private AuthTokenService authTokenService;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 处理账号登录
     *
     * @param loginDTO 登录入参
     * @param request  HTTP 请求
     * @return 登录结果（仅包含 Token 和用户基础信息）
     */
    public ApiResult<SysLoginUserVO> processLogin(AccountLoginDTO loginDTO, HttpServletRequest request) {
        String loginErrorKey = StrUtil.format(RedisCacheConstants.LOGIN_ERROR_KEY, loginDTO.getUserType(), loginDTO.getAccount());

        int loginAttempts = Integer.parseInt(redisUtil.get(loginErrorKey) != null ?redisUtil.get(loginErrorKey).toString() : "0");
        if(loginAttempts >= RedisCacheConstants.MAX_LOGIN_ATTEMPTS){
            return ApiResult.error(ApiError.AUTH_LOGIN_LOCKED);
        }

        ApiResult<SysUserDTO> apiResult = sysUserFeign.accountLogin(loginDTO);
        int code = apiResult.getCode();
        if (code != 200) {
            if(code == ApiError.AUTH_CREDENTIALS_INVALID.getCode()){
                loginAttempts++;
                redisUtil.set(loginErrorKey, String.valueOf(loginAttempts),RedisCacheConstants.LOCK_DURATION_MINUTES*60L);
                if(loginAttempts >= RedisCacheConstants.MAX_LOGIN_ATTEMPTS){
                    return ApiResult.error(ApiError.AUTH_LOGIN_LOCKED);
                }
                return ApiResult.error(ApiError.AUTH_LOGIN_RETRY_LEFT.getCode(), MessageUtils.getMessage(ApiError.AUTH_LOGIN_RETRY_LEFT, RedisCacheConstants.MAX_LOGIN_ATTEMPTS - loginAttempts));
            }
            return ApiResult.error(code, apiResult.getMsg());
        } else {
        	return this.dealSuccess(apiResult , loginDTO , request , loginErrorKey);
        }
    }
    
    /**
     * 登录成功后的业务处理
     * <p>
     * 校验 SRM 供应商状态、记录登录 IP、创建精简 Token，并组装不含菜单/权限的登录响应。
     * </p>
     *
     * @param apiResult     用户服务返回结果
     * @param loginDTO      登录入参
     * @param request       HTTP 请求
     * @param loginErrorKey 登录失败次数 Redis Key
     * @return 登录结果
     */
    private ApiResult<SysLoginUserVO> dealSuccess(ApiResult<SysUserDTO> apiResult , AccountLoginDTO loginDTO , HttpServletRequest request , String loginErrorKey){
        SysUserDTO info = apiResult.getData();
        if (Objects.isNull(info)) {
            return ApiResult.error(ApiError.AUTH_LOGIN_FAILED);
        }
        //SRM校验供应商是否启用
        if(loginDTO.getUserType().equals(UserTypeEnum.SRM.getCode())){
            SupplierEntity supplier = supplierFeign.getSupplierByUid(info.getUid());
            if(Objects.isNull(supplier)){
                return ApiResult.error(ApiError.SUPPLIER_REF_NOT_FOUND);
            }
            if(supplier.getDisabled()){
                return ApiResult.error(ApiError.COMMON_LOGIN_COOPERATION_TERMINATED);
            }
            if(supplier.getSrmDisabled()){
                return ApiResult.error(ApiError.COMMON_LOGIN_ACCOUNT_DISABLED);
            }
        }
        String ip = IpUtils.getIpAddress(request);
        info.setLoginIp(ip);
        SysLoginIpDTO ipDTO = new SysLoginIpDTO();
        ipDTO.setIp(ip);
        ipDTO.setDate(new Date());
        ipDTO.setUid(info.getUid());
        sysUserFeign.setLoginIp(ipDTO);
        SysLoginUserVO sysLoginUserVO = new SysLoginUserVO();
        if (loginDTO.getIsTest()) {
            // 创建token（仅缓存用户基础信息，菜单与权限由独立接口获取）
            info.setOverallMenuList(null);
            info.setLeftMenuList(null);
            info.setPermissionList(null);
        } else {
            sysLoginUserVO.setOverallMenuList(info.getOverallMenuList());
            sysLoginUserVO.setPermissionList(info.getPermissionList());
            sysLoginUserVO.setLeftMenuList(info.getLeftMenuList());
        }
        info.setUserType(loginDTO.getUserType());
        String accessToken = authTokenService.createSlimToken(info);
        sysLoginUserVO.setAccessToken(accessToken);
        sysLoginUserVO.setUserName(info.getUserName());
        sysLoginUserVO.setHeadIcon(info.getHeadIcon());
        sysLoginUserVO.setBindingPlatform(info.getBindingPlatform());
        sysLoginUserVO.setBindingState(info.getBindingState());
        sysLoginUserVO.setUserId(info.getUid());
        sysLoginUserVO.setNeedChangePwd(info.getNeedChangePwd());
        sysLoginUserVO.setIsSupper(info.getIsSupper());
        sysLoginUserVO.setDeptId(info.getDeptId());
        sysLoginUserVO.setDeptName(info.getDeptName());

        redisUtil.del(loginErrorKey);
        return ApiResult.success(sysLoginUserVO);
    }

    /**
     * 根据 Token 获取用户信息
     *
     * @param token accessToken
     * @return 用户信息（含菜单与权限，供 getUserByToken 使用）
     */
    public SysLoginUserVO getByToken(String token) {
        SysLoginUserVO result = new SysLoginUserVO();
        LoginUser loginUser = authTokenService.getLoginUser(token);
        if (Objects.isNull(loginUser)) {
            throw new ServiceException(ApiError.HTTP_FORBIDDEN);
        }
        SysUserDTO sysUser = sysUserFeign.getSysUserById(loginUser.getUid());
        if (Objects.isNull(sysUser)) {
            throw new ServiceException(ApiError.AUTH_USER_NOT_FOUND, loginUser.getUid());
        }
        result.setAccessToken(token);
        result.setOverallMenuList(sysUser.getOverallMenuList());
        result.setPermissionList(sysUser.getPermissionList());
        result.setUserName(sysUser.getUserName());
        result.setLeftMenuList(sysUser.getLeftMenuList());
        result.setHeadIcon(sysUser.getHeadIcon());
        result.setBindingPlatform(sysUser.getBindingPlatform());
        result.setBindingState(sysUser.getBindingState());
        result.setUserId(sysUser.getUid());
        return result;
    }

    /**
     * 获取当前登录用户的菜单权限
     * <p>
     * 返回 leftMenuList 与 overallMenuList，供前端渲染导航与路由。
     * </p>
     *
     * @param token accessToken
     * @return 菜单权限数据
     */
    public SysUserMenuAuthVO getUserMenus(String token) {
        LoginUser loginUser = getLoginUserOrThrow(token);
        ApiResult<SysUserMenuAuthVO> apiResult = sysUserFeign.getUserMenuAuth(
                new SysFeignDTO.UserLoginInfoDTO(loginUser.getUid(), loginUser.getUserType()));
        return requireFeignData(apiResult);
    }

    /**
     * 获取当前登录用户的按钮权限编码
     * <p>
     * 返回 permissionList，供前端控制按钮显示/隐藏。
     * </p>
     *
     * @param token accessToken
     * @return 按钮权限编码列表
     */
    public SysUserPermissionAuthVO getUserPermissions(String token) {
        LoginUser loginUser = getLoginUserOrThrow(token);
        ApiResult<SysUserPermissionAuthVO> apiResult = sysUserFeign.getUserPermissionAuth(
                new SysFeignDTO.UserLoginInfoDTO(loginUser.getUid(), loginUser.getUserType()));
        return requireFeignData(apiResult);
    }

    /**
     * 校验 Feign 响应：HTTP 200 且 data 非空
     */
    private <T> T requireFeignData(ApiResult<T> apiResult) {
        if (apiResult.getCode() != 200) {
            throw new ServiceException(apiResult.getCode(), apiResult.getMsg());
        }
        T data = apiResult.getData();
        if (Objects.isNull(data)) {
            throw new ServiceException(ApiError.AUTH_LOGIN_FAILED);
        }
        return data;
    }

    /**
     * 根据 Token 获取登录用户，不存在或缺少 userType（历史 Token）则拒绝访问
     *
     * @param token accessToken
     * @return 登录用户缓存信息
     */
    private LoginUser getLoginUserOrThrow(String token) {
        LoginUser loginUser = authTokenService.getLoginUser(token);
        if (Objects.isNull(loginUser)) {
            throw new ServiceException(ApiError.HTTP_FORBIDDEN);
        }
        if (StrUtil.isBlank(loginUser.getUserType())) {
            throw new ServiceException(ApiError.HTTP_UNAUTHORIZED);
        }
        return loginUser;
    }
}
