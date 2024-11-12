package com.erp.server.dmp.controller.feign;

import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;
import com.erp.server.dmp.service.DmpLogisticsTrackRegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName DmpLogisticsFeignController
 * @description: 物流feign
 * @date 2024年11月12日
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/feign/trackRegister/")
public class DmpLogisticsFeignController {
    @Resource
    private DmpLogisticsTrackRegisterService dmpLogisticsTrackRegisterService;

    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "注册物流单新增")
    private void batchAdd(@RequestBody List<DmpLogisticsTrackRegisterDTO.AddDTO> addDTOList){
        dmpLogisticsTrackRegisterService.batchAdd(addDTOList);
    }
}
