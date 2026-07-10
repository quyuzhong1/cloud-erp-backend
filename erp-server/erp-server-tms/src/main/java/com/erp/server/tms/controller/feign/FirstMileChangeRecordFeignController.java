package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("头程调整记录")
@RequestMapping("/feign/firstMileChangeRecord")
public class FirstMileChangeRecordFeignController {
    @Resource
    private FirstMileChangeRecordService firstMileChangeRecordService;

    /**
     * 新增
     * @author zdy
     * @date:  2025-05-12
     * @param dtoList
     * @return ApiResult<String>
     */
    @PostMapping("/batchAdd")
    public List<BaseResultDTO.AddDTO> add(@RequestBody @Validated List<FirstMileChangeRecordDTO.AddDTO> dtoList) {
        List<BaseResultDTO.AddDTO> addDTOS = new ArrayList<>();
        for (FirstMileChangeRecordDTO.AddDTO dto : dtoList) {
            try {
                BaseResultDTO.AddDTO add = firstMileChangeRecordService.add(dto);
                addDTOS.add(add);
            }catch (Exception e){
                log.error("头程调整记录新增失败",e);
            }
        }
        return addDTOS;
    }
}
