package com.erp.server.sys.controller.api;


import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.UserTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.query.SysUserInfoQueryHandler;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 用户管理
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-07 18:28:29
 */
@RestController
@LogSystemModule("用户管理")
@RequestMapping("user")
public class SysUserInfoController extends BaseController {

    @Autowired
    private SysUserInfoService sysUserInfoService;

    @Autowired
    private SysUserThirdService sysUserThirdService;


    /**
     * 列表
     */
    @RequestMapping("/list")
    public ApiResult list(@RequestBody SysSearchUserDTO dto) {
        dto.setUserType(UserTypeEnum.ERP.code);
        List<UserDTO> list = sysUserInfoService.findList(dto);
        return success(list);
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @RequestMapping("/paging")
    public ApiResult<PagingVO<UserManageDTO>> list(@RequestBody @Validated PagingDTO<SysUserPagingSearchDTO> dto) {
        dto.getParams().setUserType(UserTypeEnum.ERP.code);
        PagingVO<UserManageDTO> pagingVO = sysUserInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 信息
     */
    @LogViewService
    @RequestMapping("/info/{uid}")
    public ApiResult info(@PathVariable("uid") String uid) {
        SysUserInfoEntity sysUserInfo = sysUserInfoService.getById(uid);
        return success(sysUserInfo);
    }

    /**
     * 添加用户
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加用户")
    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoDTO.setUserType(UserTypeEnum.ERP.code);
        sysUserInfoService.add(sysUserInfoDTO);
        return success();
    }

    /**
     * 修改用户
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改用户", keyIdName = "uid")
    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoDTO.setUserType(UserTypeEnum.ERP.code);
        sysUserInfoService.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 删除
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除用户")
    @RequestMapping("/remove")
    public ApiResult delete(@RequestBody List<String> uids) {
        sysUserInfoService.deleteByIds(uids);
        sysUserThirdService.deleteByUserIds(uids);
        return success();
    }

    /**
     * 修改状态
     * @param stateDTO
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改用户状态:ids={ids},状态={state}(1=启用,0=未启用)")
    @RequestMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        sysUserInfoService.updateState(stateDTO);
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重置用户密码:用户ID={uid},用户密码={pwd}")
    @GetMapping("/changePassword")
    public ApiResult changePassword(@RequestParam("uid") String uid,@RequestParam("pwd") String pwd) {
        Boolean flag = sysUserInfoService.changePassword(uid,pwd);
        return flag ? success() : failure();
    }

    /**
     * 忘记密码
     * @Author Luo_WG
     * @Date 2023/4/20 11:16
     * @param dto dto
     * @return
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "忘记密码:账号={userAccount}")
    @PostMapping("/forgotPassword")
    public ApiResult forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        dto.setUserType(UserTypeEnum.ERP.code);
        Boolean flag = sysUserInfoService.forgotPassword(dto);
        return flag ? success() : failure();
    }

    /**
     * 忘记密码-获取验证码
     * @Author Luo_WG
     * @Date 2023/4/20 11:45
     * @param userAccount userAccount
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "忘记密码-获取验证码:账号={userAccount}")
    @GetMapping("/forgotPasswordGetCode")
    public ApiResult<Map<String,Object>> forgotPasswordGetCode(@RequestParam("userAccount") String userAccount) {
        Map<String,Object> map = sysUserInfoService.forgotPasswordGetCode(userAccount,UserTypeEnum.ERP.code);
        return success(map);
    }


    /**
     * srm忘记密码
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "忘记密码:账号={userAccount}")
    @PostMapping("/srmForgotPassword")
    public ApiResult srmForgotPassword(@RequestBody ForgotPasswordDTO dto) {
        dto.setUserType(UserTypeEnum.SRM.code);
        Boolean flag = sysUserInfoService.forgotPassword(dto);
        return flag ? success() : failure();
    }

    /**
     * SRM用户忘记密码-获取验证码
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "SRM用户忘记密码-获取验证码:账号={account}")
    @GetMapping("/srmForgotPasswordGetCode")
    public ApiResult<Map<String,Object>> srmForgotPasswordGetCode(@RequestParam("account") String account) {
        Map<String,Object> map = sysUserInfoService.forgotPasswordGetCode(account,UserTypeEnum.SRM.code);
        return success(map);
    }

    /**
     * 获取用户信息
     * @param searchKeyword
     * @return
     */
    @GetMapping("/listBySearchKeyword")
    public ApiResult<List<FindUserDTO>> listBySearchKeyword(@RequestParam(value ="searchKeyword" )String searchKeyword) {
        List<FindUserDTO> list = sysUserInfoService.listBySearchKeyword(searchKeyword);
        return success(list);
    }

    /**
     * 导入用户关联金蝶信息
     * @param file
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入用户关联金蝶信息")
    @PostMapping(value = "importUserKingdee")
    public ApiResult<Void> importUserKingdee(@RequestParam(value = "file") MultipartFile file) throws IOException {
        sysUserInfoService.importUserKingdee(file);
        return success();
    }

    /**
     * 店铺权限设置分页查询
     * @param dto
     * @return
     */
    @RequestMapping("/shopAuthPaging")
    @WebAdvanceQuery(handler = SysUserInfoQueryHandler.class)
    public ApiResult shopAuthPaging(@RequestBody @Validated PagingDTO<SysUserInfoDTO.ShopAuthPagingSearchDTO> dto) {
        PagingVO pagingVO = sysUserInfoService.shopAuthPaging(dto);
        return success(pagingVO);
    }

    /**
     * 飞书人员同步
     * @author jack
     * @date:  2025-05-15
     * @return ApiResult
     */
    @PostMapping("/syncFsUser")
    public void syncFsUser() {
        sysUserInfoService.syncFsUser();
    }
}
