package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.server.tms.service.TmsDeclareBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("报关单")
@RequestMapping("/feign/tmsDeclareBill")
public class TmsDeclareBillFeignController {
    @Resource
    private TmsDeclareBillService tmsDeclareBillService;

    /**
     * 根据来源id查询报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param sourceIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/listBySourceIds")
    public List<TmsDeclareBillEntity> listBySourceIds(@RequestBody List<String> sourceIds) {
        return tmsDeclareBillService.listBySourceIds(sourceIds);
    }

    /**
     * 新增报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param addDTO
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/add")
    public Boolean add(@RequestBody TmsDeclareBillDTO.AddDTO addDTO) {
        return tmsDeclareBillService.addFmDeclare(addDTO);
    }
}
