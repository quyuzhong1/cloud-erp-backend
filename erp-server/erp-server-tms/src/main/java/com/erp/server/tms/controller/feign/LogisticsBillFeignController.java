package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.server.tms.service.LogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("物流单feign接口")
@RequestMapping("/feign/logisticsBill")
public class LogisticsBillFeignController {
    @Resource
    private LogisticsBillService logisticsBillService;

    /**
     * 新增物流单
     * @Author Luo_WG
     * @Date 2023/11/9 17:59
     * @param addDTOList
     * @return java.lang.Boolean
     **/
    @PostMapping("/logisticsBillBatchSave")
    public Boolean logisticsBillBatchSave(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList) {
        Boolean flag = logisticsBillService.logisticsBillBatchSave(addDTOList);
        return flag;
    }

    /**
     * 根据来源id查询物流信息及跟踪号
     * @Author Luo_WG
     * @Date 2023/11/10 9:06
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    @PostMapping("/listLogisticsBillVoBySourceIds")
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@RequestBody List<String> sourceIdList) {
        List<LogisticsBillDTO.LogisticsBillVo> flag = logisticsBillService.listLogisticsBillVoBySourceIds(sourceIdList);
        return flag;
    }
}
