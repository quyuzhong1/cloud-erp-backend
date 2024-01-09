package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierRefUserDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.scm.service.SupplierRefUserService;
import com.erp.server.scm.service.SupplierUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
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
    public ApiResult<PagingVO> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto){
        PagingVO<SupplierUserVO> pagingVO = supplierUserService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 添加用户
     */
    @PostMapping("/save")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增供应商协同用户")
    public ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        supplierUserService.add(sysUserInfoDTO);
        return success();
    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplierUser:update",
            serviceClass = SupplierRefUserService.class,
            keyIdName = "refId")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        supplierUserService.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 用户信息
     */
    @LogViewService
    @GetMapping("/info/{uid}")
    public ApiResult info(@PathVariable("uid") String uid) {
        SysUserInfoEntity sysUserInfo = supplierUserService.getById(uid);
        return success(sysUserInfo);
    }


    /**
     * 删除
     */
    @PostMapping("/remove")
    public ApiResult delete(@RequestBody List<String> uids) {
        return supplierUserService.deleteByIds(uids);
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
     * 忘记密码
     * @param dto
     * @return
     */
    @PostMapping("/forgotPassword")
    public ApiResult forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        return supplierUserService.forgotPassword(dto);
    }

    /**
     * 忘记密码-获取验证码
     * @param userAccount
     * @return
     */
    @GetMapping("/forgotPasswordGetCode")
    public ApiResult<Map<String,Object>> forgotPasswordGetCode(@RequestParam("userAccount") String userAccount) {
        return supplierUserService.forgotPasswordGetCode(userAccount);
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "uid",
            menuCode = "scm:supplierUser:paging",
            tableAlias = "supplierUser"
    )
    public ApiResult exportSupplier(@RequestBody @Valid UserPagingSearchDTO dto, HttpServletResponse response) {
        supplierUserService.exportSupplierUser(dto, response);
        return success();
    }

    /**
     * 下载协作用户模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板供应商")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        supplierUserService.downloadTemplate(response);
        return success();
    }
}
