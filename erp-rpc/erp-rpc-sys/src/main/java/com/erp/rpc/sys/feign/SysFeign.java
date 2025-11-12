package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictPartitionDTO;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.sys.vo.SysDeptDropDownVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Sys 服务 Feign 接口
 * @author wuhaotian
 * @since 2025-09-24
 */
@FeignClient(name = "erp-sys", contextId = "sysFeign", configuration = {FeignErrorDecoder.class})
public interface SysFeign {

    /**
     * 公司列表
     */
    @GetMapping("/feign/company/list")
    ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyList();

    /**
     * 国家字典列表
     */
    @GetMapping("/feign/dict/country/list")
    ApiResult<List<DictCountryDTO.ListDTO>> countryList();

    /**
     * 分区下拉列表
     */
    @PostMapping("/feign/dictPartition/drop/down")
    ApiResult<List<DictPartitionDTO.DictDTO>> dictPartitionDropDown(@RequestBody @Validated DictPartitionDTO.SelectDTO dto);

    /**
     * 部门下拉列表
     */
    @GetMapping("/feign/department/drop/down")
    ApiResult<List<SysDeptDropDownVO>> departmentDropDown();
}
