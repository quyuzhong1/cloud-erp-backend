package com.erp.server.srm.controller.api;


import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.rpc.wms.feign.SupplierUserFeign;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;
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
public class UserController extends BaseController {

    @Resource
    private SupplierUserFeign supplierUserFeign;
    @Resource
    private UserService userService;

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto){
        dto.getParams().setIsSuper(false);
        return supplierUserFeign.page(dto);
    }


    /**
     * 添加用户
     */
    @PostMapping("/save")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增供应商协同用户")
    public ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoDTO.setIsSuper(false);
        supplierUserFeign.save(sysUserInfoDTO);
        return success();
    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改供应商协同用户")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoDTO.setIsSuper(true);
        supplierUserFeign.update(sysUserInfoDTO);
        return success();
    }


    /**
     * 用户信息
     */
    @LogViewService
    @GetMapping("/info/{uid}")
    public ApiResult info(@PathVariable("uid") String uid) {
        SupplierUserInfoVO info = supplierUserFeign.info(uid);
        return success(info);
    }


    /**
     * 删除
     */
    @PostMapping("/remove")
    public ApiResult delete(@RequestBody String uid) {
        return supplierUserFeign.delete(uid);
    }

    /**
     * 批量启用/禁用
     * @param stateDTO
     * @return
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        return supplierUserFeign.updateState(stateDTO);
    }

    /**
     * 重置密码
     * @param uid
     * @param pwd
     * @return
     */
    @GetMapping("/resetPassword")
    public ApiResult resetPassword(@RequestParam("uid") String uid,@RequestParam("pwd") String pwd) {
        return supplierUserFeign.resetPassword(uid,pwd);
    }
    /**
     * 供应商协作用户导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出SRM用户")
    @PostMapping("/export")
    public ApiResult exportSupplier(@RequestBody @Valid UserPagingSearchDTO dto, HttpServletResponse response) {
        dto.setIsSuper(false);
        dto.setSupplierIds(Collections.singletonList(userService.getSupplierId()));
        userService.exportSupplier(dto, response);
        return success();
    }


    /**
     * 供应商协作用户导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入SRM用户")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = userService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载协作用户模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载SRM用户模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        userService.downloadTemplate(response);
        return success();
    }
}
