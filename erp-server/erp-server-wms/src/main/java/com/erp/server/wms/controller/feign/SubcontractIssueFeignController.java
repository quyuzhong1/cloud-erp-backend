package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.server.wms.service.SubcontractIssueService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    /**
     * 新增
     * @author Will
     * @date: 2024/1/15 10:46
     * @param dto
     * @return String
     */
    @PostMapping("/add")
    public String add(@RequestBody @Validated SubcontractIssueDTO.AutoAddDTO dto) {
        BaseResultDTO.AddDTO add = subcontractIssueService.autoAdd(dto);
        return  add.getId();
    }

    /**
     * 根据来源id查询委外发料单
     * @author Will
     * @date: 2024/1/15 10:51
     * @param sourceIdList
     * @return List<SubcontractIssueEntity>
     */
    @PostMapping("/listBySourceIdList")
    public List<SubcontractIssueEntity> listBySourceIdList(@RequestBody  List<String> sourceIdList) {
        List<SubcontractIssueEntity> list = subcontractIssueService.listBySourceIdList(sourceIdList);
        return list;
    }
}
