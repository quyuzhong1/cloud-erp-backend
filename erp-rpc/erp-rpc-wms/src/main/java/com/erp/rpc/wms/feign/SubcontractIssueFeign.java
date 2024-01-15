package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @description: 委外发料单feign
 * @author Will
 * @date: 2024/1/12 14:16
 */
@FeignClient(name = "erp-wms", contextId = "subcontractIssue", path = "/feign/subcontractIssue")
public interface SubcontractIssueFeign {

    /**
     * 新增委外发料单
     */
    @PostMapping("add")
    String add(@RequestBody @Validated SubcontractIssueDTO.AddDTO dto);
}


