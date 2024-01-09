package com.erp.server.scm.controller.feign;

import cn.hutool.core.lang.Assert;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.server.scm.service.SupplierUserService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName SupplierUserFeignController
 * @description: TODO
 * @date 2024年01月09日
 * @version: 1.0
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/feign/supplierUser")
public class SupplierUserFeignController extends BaseController {
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改供应商协同用户")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        Assert.notEmpty(sysUserInfoDTO.getUid(), "用户ID不能为空");
        supplierUserService.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 用户信息
     */
    @LogViewService
    @GetMapping("/info/{uid}")
    public SupplierUserInfoVO info(@PathVariable("uid") String uid) {
        return supplierUserService.getById(uid);
    }


    /**
     * 删除
     */
    @PostMapping("/remove")
    public ApiResult delete(@RequestBody String uid) {
        return supplierUserService.deleteById(uid);
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
        Boolean result = supplierUserService.importFile(excelFile, response, false);
        return result == true ? success() : failure();
    }

    /**
     * 供应商协作用户导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出供应商协作用户")
    @PostMapping("/export")
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
