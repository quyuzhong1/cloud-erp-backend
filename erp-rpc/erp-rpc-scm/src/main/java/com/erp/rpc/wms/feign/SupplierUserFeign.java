package com.erp.rpc.wms.feign;

import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Classname: SupplierFeign
 * @CreateTime: 2023-06-19  19:22
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-scm", contextId = "supplierUser")
public interface SupplierUserFeign {

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/supplierUser/paging")
    ApiResult<PagingVO> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto);


    /**
     * 添加用户
     */
    @PostMapping("/feign/supplierUser/save")
    ApiResult save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO);

    /**
     * 修改用户
     */
    @PostMapping("/feign/supplierUser/update")
    ApiResult update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO);


    /**
     * 用户信息
     */
    @GetMapping("/feign/supplierUser/info/{uid}")
    SupplierUserInfoVO info(@PathVariable("uid") String uid);


    /**
     * 删除
     */
    @PostMapping("/feign/supplierUser/remove")
    ApiResult delete(@RequestBody String uid);

    /**
     * 批量启用/禁用
     *
     * @param stateDTO
     * @return
     */
    @PostMapping("/feign/supplierUser/updateState")
    ApiResult updateState(@RequestBody @Validated UpdateUserStateDTO stateDTO);

    /**
     * 重置密码
     *
     * @param uid
     * @param pwd
     * @return
     */
    @GetMapping("/feign/supplierUser/resetPassword")
    ApiResult resetPassword(@RequestParam("uid") String uid, @RequestParam("pwd") String pwd);

    /**
     * 忘记密码
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/supplierUser/forgotPassword")
    ApiResult forgotPassword(@RequestBody ForgotPasswordDTO dto);

    /**
     * 忘记密码-获取验证码
     *
     * @param userAccount
     * @return
     */
    @GetMapping("/feign/supplierUser/forgotPasswordGetCode")
    ApiResult<Map<String, Object>> forgotPasswordGetCode(@RequestParam("userAccount") String userAccount);


    /**
     * 供应商协作用户导入
     */
    @PostMapping("/feign/supplierUser/import")
    ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response);

    /**
     * 供应商协作用户导出
     */
    @PostMapping("/feign/supplierUser/export")
    ApiResult exportSupplier(@RequestBody @Valid UserPagingSearchDTO dto, HttpServletResponse response);

    /**
     * 下载协作用户模板
     *
     * @return
     */
    @GetMapping("/feign/supplierUser/downloadTemplate")
    ApiResult downloadTemplate(HttpServletResponse response);
}
