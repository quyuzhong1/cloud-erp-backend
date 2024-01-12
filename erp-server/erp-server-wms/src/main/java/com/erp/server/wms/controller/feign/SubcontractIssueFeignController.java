package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.SubcontractIssueService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @description: 委外发料单远程调用控制层
 * @author Will
 * @date: 2024/1/12 14:19
 */
@RestController
@RequestMapping("feign/subcontractIssue")
public class SubcontractIssueFeignController {

    @Resource
    private SubcontractIssueService subcontractIssueService;

    @PostMapping("/add")
    public String add(@RequestBody @Validated SubcontractIssueDTO.AddDTO dto) {
        BaseResultDTO.AddDTO add = subcontractIssueService.add(dto);
        return  add.getId();
    }
}
