package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.model.wms.entity.SubcontractIssueEntity;
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
@FeignClient(name = "erp-wms", contextId = "subcontractIssue", path = "/feign/subcontractIssue",configuration = {FeignErrorDecoder.class})
public interface SubcontractIssueFeign {

   /**
    * @description: 新增委外发料单
    * @author Will
    * @date: 2024/1/15 10:45
    * @param dto
    * @return String
    */
    @PostMapping("/add")
    String add(@RequestBody @Validated SubcontractIssueDTO.AutoAddDTO dto);


   /**
    * @description: 根据来源id查询委外发料单
    * @author Will
    * @date: 2024/1/15 10:45
    * @param sourceIdList
    * @return String
    */
    @PostMapping("/listBySourceIdList")
    List<SubcontractIssueEntity> listBySourceIdList(@RequestBody List<String> sourceIdList);
}


