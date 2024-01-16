package com.erp.server.scm.controller.api;


import cn.hutool.core.lang.Assert;
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
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.DeleteUserDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.server.scm.service.SupplierUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

/**
 * 供应商协同用户
 *
 * @author zdy
 * @since 2024-01-05
 */
@Slf4j
@RestController
@LogSystemModule("供应商协同用户")
@RequestMapping("/supplierUser")
public class SupplierUserController extends BaseController {

    @Resource
    private SupplierUserService supplierUserService;


    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierUserVO>> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto){
        dto.getParams().setIsSuper(true);
        dto.getParams().setUserType(UserTypeEnum.SRM.code);
        PagingVO<SupplierUserVO> pagingVO = supplierUserService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增供应商协同用户
     * @param sysUserInfoDTO
     * @return
     */
    @PostMapping("/save")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增供应商协同用户")
    public ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoDTO.setIsSuper(true);
        sysUserInfoDTO.setUserType(UserTypeEnum.SRM.code);
        supplierUserService.add(sysUserInfoDTO);
        return success();
    }

    /**
     * 修改供应商协同用户
     * @param sysUserInfoDTO
     * @return
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改供应商协同用户")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        if (StringUtils.isEmpty(sysUserInfoDTO.getUid())) throw new ServiceException("用户ID不能为空");
        sysUserInfoDTO.setIsSuper(true);
        sysUserInfoDTO.setUserType(UserTypeEnum.SRM.code);
        supplierUserService.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 用户信息
     * @param uid
     * @return
     */
    @LogViewService
    @GetMapping("/info/{uid}")
    public ApiResult<SupplierUserInfoVO> info(@PathVariable("uid") String uid) {
        SupplierUserInfoVO supplierUserInfoVO = supplierUserService.getById(uid);
        return success(supplierUserInfoVO);
    }


    /**
     * 删除用户
     * @param dto
     * @return
     */
    @PostMapping("/remove")
    public ApiResult remove(@RequestBody DeleteUserDTO dto) {
        return supplierUserService.deleteById(dto.getUid());
    }

    /**
     * 批量启用/禁用
     * @param stateDTO
     * @return
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        supplierUserService.updateState(stateDTO);
        return success();
    }

    /**
     * 重置密码
     * @param uid
     * @param pwd
     * @return
     */
    @GetMapping("/resetPassword")
    public ApiResult resetPassword(@RequestParam("uid") String uid,@RequestParam("pwd") String pwd) {
        return supplierUserService.resetPassword(uid,pwd);
    }


    /**
     * 供应商协作用户导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入供应商协作用户")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = supplierUserService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 供应商协作用户导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出供应商协作用户")
    @PostMapping("/exportSupplier")
    public ApiResult exportSupplier(@RequestBody UserPagingSearchDTO dto, HttpServletResponse response) {
        dto.setIsSuper(true);
        dto.setUserType(UserTypeEnum.SRM.code);
        supplierUserService.exportSupplierUser(dto, response);
        return success();
    }

    /**
     * 下载协作用户模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载供应商协作用户模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        supplierUserService.downloadTemplate(response);
        return success();
    }
}
