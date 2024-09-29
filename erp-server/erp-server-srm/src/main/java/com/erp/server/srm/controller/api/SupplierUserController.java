package com.erp.server.srm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.UserTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.scm.feign.SupplierUserFeign;
import com.erp.server.srm.query.SupplierUserQueryHandler;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private SupplierUserFeign supplierUserFeign;
    @Resource
    private UserService userService;
    @Resource
    private SupplierFeign supplierFeign;

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = SupplierUserQueryHandler.class)
    public ApiResult<PagingVO<SupplierUserVO>> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto){
        dto.getParams().setUserType(UserTypeEnum.SRM.code);
        dto.getParams().setSupplierId(userService.getSupplierId());
        dto.getParams().setIsSuper(Boolean.FALSE);
        List<SupplierRefUserVO> supplierRefUserVOS = supplierUserFeign.getSupplierRefBySupplierIds(Collections.singletonList(dto.getParams().getSupplierId()));
        if (CollectionUtils.isEmpty(supplierRefUserVOS)) {
            return success(new PagingVO<>());
        }
        List<String> userIds = supplierRefUserVOS.stream().filter(e-> e.getSupplierId().equals(dto.getParams().getSupplierId())).map(SupplierRefUserVO::getUid).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return success(new PagingVO<>());
        }
        dto.getParams().setUserIds(userIds);
        return supplierUserFeign.page(dto);
    }


    /**
     * 添加用户
     */
    @PostMapping("/save")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增供应商协同用户")
    public ApiResult saveSrm(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        sysUserInfoDTO.setIsSuper(false);
        sysUserInfoDTO.setUserType(UserTypeEnum.SRM.code);
        sysUserInfoDTO.setSupplierId(userService.getSupplierId());
        sysUserInfoDTO.setCreatePasswordType(1);
        sysUserInfoDTO.setConfirmPassword(sysUserInfoDTO.getPassword());
        sysUserInfoDTO.setNeedChangePwd(false);
        sysUserInfoDTO.setRealName(sysUserInfoDTO.getUserName());
        try {
            return supplierUserFeign.saveSrm(sysUserInfoDTO);
        }catch (Exception e){
            throw new ServiceException(e.getMessage());
        }

    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改供应商协同用户")
    public ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getIsSupper()) || !userInfo.getIsSupper()){
            throw new ServiceException(ApiError.NO_PERMISSION);
        }
        supplierUserFeign.updateSrm(sysUserInfoDTO);
        return success();
    }


    /**
     * 用户信息
     */
    @LogViewService
    @GetMapping("/info/{uid}")
    public ApiResult<SupplierUserInfoVO> info(@PathVariable("uid") String uid) {
        SupplierUserInfoVO info = supplierUserFeign.info(uid);
        return success(info);
    }

    /**
     * 获取供应商信息
     */
    @LogViewService
    @GetMapping("/getSupplier")
    public ApiResult<SupplierEntity> getSupplier() {
        SupplierEntity supplier = supplierFeign.getSupplierById(userService.getSupplierId());
        return success(supplier);
    }
    /**
     * 删除
     */
    @PostMapping("/remove")
    public ApiResult remove(@RequestParam("uid") String uid) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getIsSupper()) || !userInfo.getIsSupper()){
            throw new ServiceException(ApiError.NO_PERMISSION);
        }
        return supplierUserFeign.remove(uid);
    }

    /**
     * 批量启用/禁用
     * @param stateDTO
     * @return
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getIsSupper()) || !userInfo.getIsSupper()){
            throw new ServiceException(ApiError.NO_PERMISSION);
        }
        return supplierUserFeign.updateState(stateDTO);
    }

    /**
     * 重置密码
     * @param uid
     * @param pwd
     * @return
     */
    @GetMapping("/changePassword")
    public ApiResult changePassword(@RequestParam("uid") String uid,@RequestParam("pwd") String pwd) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getIsSupper()) || !userInfo.getIsSupper()){
            throw new ServiceException(ApiError.NO_PERMISSION);
        }
        return supplierUserFeign.changePassword(uid,pwd);
    }
    /**
     * 供应商协作用户导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出SRM用户")
    @PostMapping("/export")
    @WebAdvanceQuery(handler = SupplierUserQueryHandler.class)
    public ApiResult exportSupplier(@RequestBody UserPagingSearchDTO dto, HttpServletResponse response) {
        dto.setUserType(UserTypeEnum.SRM.code);
        dto.setSupplierId(userService.getSupplierId());
        List<SupplierRefUserVO> supplierRefUserVOS = supplierUserFeign.getSupplierRefBySupplierIds(Collections.singletonList(dto.getSupplierId()));
        if (CollectionUtils.isEmpty(supplierRefUserVOS)) {
            dto.setUserIds(Collections.singletonList("-1"));
        }else {
            List<String> userIds = supplierRefUserVOS.stream().filter(e-> e.getSupplierId().equals(dto.getSupplierId())).map(SupplierRefUserVO::getUid).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userIds)) {
                dto.setUserIds(Collections.singletonList("-1"));
            }else {
                dto.setUserIds(userIds);
            }
        }
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
