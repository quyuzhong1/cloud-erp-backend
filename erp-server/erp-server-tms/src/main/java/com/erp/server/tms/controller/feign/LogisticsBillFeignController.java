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
     *
     * @param addDTOList
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/11/9 17:59
     **/
    @PostMapping("/logisticsBillBatchSave")
    public Boolean logisticsBillBatchSave(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList) {
        Boolean flag = logisticsBillService.logisticsBillBatchSave(addDTOList);
        return flag;
    }

    /**
     * 添加物流单
     *
     * @return java.lang.Boolean
     * @parms addDTO
     * @author yl
     * @date 2023-11-17
     */
    @PostMapping("/addLogisticsBill")
    public Boolean addLogisticsBill(@RequestBody LogisticsBillDTO.AddDTO addDTO) {
        Boolean flag = logisticsBillService.add(addDTO);
        return flag;
    }

    /**
     * 根据来源id查询物流信息及跟踪号
     *
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     * @Author Luo_WG
     * @Date 2023/11/10 9:06
     **/
    @PostMapping("/listLogisticsBillVoBySourceIds")
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@RequestBody List<String> sourceIdList) {
        List<LogisticsBillDTO.LogisticsBillVo> flag = logisticsBillService.listLogisticsBillVoBySourceIds(sourceIdList);
        return flag;
    }

    /**
     * 生成物流单
     *@parms
     *@return
     *@author yl
     *@date 2023-11-23
     */
    @PostMapping("/generateBill")
    public void  generateBill(@RequestBody LogisticsBillDTO.AddDTO  dto){
        logisticsBillService.generateBill(dto);
    }
}
