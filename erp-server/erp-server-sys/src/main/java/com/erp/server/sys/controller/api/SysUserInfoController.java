package com.erp.server.sys.controller.api;


import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 用户管理
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-07 18:28:29
 */
@RestController
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
        List<UserDTO> list = sysUserInfoService.findList(dto);
        return success(list);
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @RequestMapping("/paging")
    public ApiResult list(@RequestBody @Validated PagingDTO<SysUserPagingSearchDTO> dto) {
        PagingVO pagingVO = sysUserInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{uid}")
    public ApiResult info(@PathVariable("uid") Long uid) {
        SysUserInfoEntity sysUserInfo = sysUserInfoService.getById(uid);
        return success(sysUserInfo);
    }

    /**
     * 添加用户
     */
    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoService.add(sysUserInfoDTO);
        return success();
    }

    /**
     * 修改用户
     */
    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoService.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 删除
     */
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
    @GetMapping("/resetPassword")
    public ApiResult resetPassword(@RequestParam("uid") String uid) {
        Boolean flag = sysUserInfoService.resetPassword(uid);
        return flag == true ? success() : failure();
    }

    /**
     * 忘记密码
     * @Author Luo_WG
     * @Date 2023/4/20 11:16
     * @param dto dto
     * @return
     **/
    @PostMapping("/forgotPassword")
    public ApiResult forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        Boolean flag = sysUserInfoService.forgotPassword(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 忘记密码-获取验证码
     * @Author Luo_WG
     * @Date 2023/4/20 11:45
     * @param userAccount userAccount
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/forgotPasswordGetCode")
    public ApiResult<Map<String,Object>> forgotPasswordGetCode(@RequestParam("userAccount") String userAccount) {
        Map<String,Object> map = sysUserInfoService.forgotPasswordGetCode(userAccount);
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
}
