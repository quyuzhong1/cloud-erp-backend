package com.erp.server.plm.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.core.controller.BaseController;
import com.erp.server.plm.service.PilotApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author jack
 * @date: 2024/09/18
 */
@RestController
@RequestMapping("feign/pilotApplication")
public class PilotApplicationFeignController extends BaseController {

    @Resource
    private PilotApplicationService pilotApplicationService;

    /**
     * 更新试产量产明细表的订单状态
     * @author jack
     * @date: 2024/09/18
     * @param map key:pilotApplicationDetailId value:订单状态
     */
    @PostMapping("/updateDetailByPilotApplicationDetailIds")
    public void updateDetailByPilotApplicationDetailIds(@RequestBody Map<String,String> map) {
        pilotApplicationService.updateDetailByPilotApplicationDetailIds(map);
    }

}
