package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictPartitionDTO;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.vo.SysDeptDropDownVO;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictPartitionService;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sys 服务 Feign 控制器
 * @author wuhaotian
 * @since 2025-09-24
 */
@RestController
@RequestMapping("/feign")
public class SysFeignController extends BaseController {

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Resource
    private DictCountryService dictCountryService;

    @Resource
    private DictPartitionService dictPartitionService;

    @Resource
    private SysDepartmentService sysDepartmentService;

    /**
     * 公司列表
     */
    @GetMapping("/company/list")
    public ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyList() {
        List<SysAccountingCompanyDTO.ListDTO> list = sysAccountingCompanyService.getList();
        return success(list);
    }

    /**
     * 国家字典列表
     */
    @GetMapping("/dict/country/list")
    public ApiResult<List<DictCountryDTO.ListDTO>> countryList() {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountry();
        return success(list);
    }

    /**
     * 分区下拉列表
     */
    @PostMapping("/dictPartition/drop/down")
    public ApiResult<List<DictPartitionDTO.DictDTO>> dictPartitionDropDown(@RequestBody DictPartitionDTO.SelectDTO dto) {
        List<DictPartitionDTO.DictDTO> list = dictPartitionService.dropDown(dto);
        return success(list);
    }

    /**
     * 部门下拉列表
     */
    @GetMapping("/department/drop/down")
    public ApiResult<List<SysDeptDropDownVO>> departmentDropDown() {
        List<SysDepartmentEntity> entities = sysDepartmentService.listDept();
        List<SysDeptDropDownVO> resultList = entities.stream()
                .map(x -> new SysDeptDropDownVO(x.getId(), x.getName()))
                .collect(Collectors.toList());
        return success(resultList);
    }
}
