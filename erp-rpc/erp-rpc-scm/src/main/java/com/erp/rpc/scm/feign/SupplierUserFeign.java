package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * @Classname: SupplierFeign
 * @CreateTime: 2023-06-19  19:22
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-scm", contextId = "supplierUser",configuration = {FeignErrorDecoder.class})
public interface SupplierUserFeign {

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/supplierUser/paging")
    ApiResult<PagingVO<SupplierUserVO>> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto);

    /**
     * 查询供应商列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/supplierUser/list")
    List<SupplierUserVO> list(@RequestBody @Validated UserPagingSearchDTO dto);

    /**
     * 添加用户
     */
    @PostMapping("/feign/supplierUser/saveSrm")
    ApiResult saveSrm(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO);

    /**
     * 修改用户
     */
    @PostMapping("/feign/supplierUser/updateSrm")
    ApiResult updateSrm(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO);


    /**
     * 用户信息
     */
    @GetMapping("/feign/supplierUser/info/{uid}")
    SupplierUserInfoVO info(@PathVariable("uid") String uid);


    /**
     * 删除
     */
    @PostMapping("/feign/supplierUser/remove")
    ApiResult remove(@RequestParam("uid") String uid);

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
    @GetMapping("/feign/supplierUser/changePassword")
    ApiResult changePassword(@RequestParam("uid") String uid, @RequestParam("pwd") String pwd);

    /**
     * 保存用户关系
     *
     * @param refUserEntity
     * @return
     */
    @PostMapping("/feign/supplierUser/saveRef")
    Boolean saveRef(@RequestBody @Validated SupplierRefUserEntity refUserEntity);

    /**
     * 根据用户获取供应商关联信息
     * @param uids
     * @return
     */
    @PostMapping("/feign/supplierUser/getSupplierRefBySupplierIds")
    List<SupplierRefUserVO> getSupplierRefBySupplierIds(@RequestBody List<String> uids);
}
