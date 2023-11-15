package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.service.LogisticsBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsFeignController
 * @description: TODO
 * @date 2023年11月03日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("物流feign接口")
@RequestMapping("/feign/logistics")
public class LogisticsFeignController {

    @Resource
    private LogisticsBaseService logisticsBaseService;
    @PostMapping("/queryOrderList")
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(@RequestBody List<LogisticsQueryBaseVO> logisticsQueryVOList){
        return logisticsBaseService.queryOrderList(logisticsQueryVOList);
    }
}
