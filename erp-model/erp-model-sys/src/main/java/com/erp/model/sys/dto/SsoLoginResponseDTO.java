package com.erp.model.sys.dto;

import com.erp.model.sys.vo.SysMenuVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 单点登录响应DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
public class SsoLoginResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 权限路径列表
     */
    private String[] pathList;

    /**
     * 签名会话ID（后端生成，用于标识会话密钥存储位置）
     */
    private String signSessionId;

    /**
     * 权限列表（菜单权限编码）
     */
    private List<String> permissionList;

    /**
     * 左侧菜单列表
     */
    private List<SysMenuVO> leftMenuList;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 头像
     */
    private String headIcon;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电话号码
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建成功响应
     */
    public static SsoLoginResponseDTO success(String token, String userId, String appId, String[] pathList, String signSessionId) {
        SsoLoginResponseDTO response = new SsoLoginResponseDTO();
        response.setSuccess(true);
        response.setToken(token);
        response.setUserId(userId);
        response.setAppId(appId);
        response.setPathList(pathList);
        response.setSignSessionId(signSessionId);
        return response;
    }

    /**
     * 创建成功响应（包含权限和菜单）
     */
    public static SsoLoginResponseDTO success(String token, String userId, String appId, String[] pathList, 
                                              String signSessionId, List<String> permissionList, List<SysMenuVO> leftMenuList) {
        SsoLoginResponseDTO response = new SsoLoginResponseDTO();
        response.setSuccess(true);
        response.setToken(token);
        response.setUserId(userId);
        response.setAppId(appId);
        response.setPathList(pathList);
        response.setSignSessionId(signSessionId);
        response.setPermissionList(permissionList);
        response.setLeftMenuList(leftMenuList);
        return response;
    }

    /**
     * 创建成功响应（包含权限、菜单和用户信息）
     */
    public static SsoLoginResponseDTO success(String token, String userId, String appId, String[] pathList, 
                                              String signSessionId, List<String> permissionList, List<SysMenuVO> leftMenuList,
                                              String userName, String headIcon, String realName, String mobile, String email) {
        SsoLoginResponseDTO response = new SsoLoginResponseDTO();
        response.setSuccess(true);
        response.setToken(token);
        response.setUserId(userId);
        response.setAppId(appId);
        response.setPathList(pathList);
        response.setSignSessionId(signSessionId);
        response.setPermissionList(permissionList);
        response.setLeftMenuList(leftMenuList);
        response.setUserName(userName);
        response.setHeadIcon(headIcon);
        response.setRealName(realName);
        response.setMobile(mobile);
        response.setEmail(email);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static SsoLoginResponseDTO error(String errorCode, String errorMessage) {
        SsoLoginResponseDTO response = new SsoLoginResponseDTO();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
