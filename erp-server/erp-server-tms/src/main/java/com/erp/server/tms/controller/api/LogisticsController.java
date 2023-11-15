package com.erp.server.tms.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsController
 * @description: 物流接口
 * @date 2023年11月14日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("物流渠道")
@RequestMapping("/logistics")
public class LogisticsController extends BaseController {
    @Resource
    LogisticsBaseService logisticsBaseService;
    @Resource
    LogisticsRegistry logisticsRegistry;

    @PostMapping("/createOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流新增订单")
    public void createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsService service = logisticsRegistry.getHandler("");
        ApiResult<LogisticsOrderResponseVO> order = service.createOrder(logisticsOrderVO);

    }

    @PostMapping("/confirmOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单确认")
    public void confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {

    }

    @PostMapping("/cancelOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单取消")
    public void cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {

    }

    @PostMapping("/interceptOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单拦截")
    public void interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {

    }

    @PostMapping("/updateOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单更新")
    public void updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {

    }

    @PostMapping("/queryOrderList")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单查询")
    public void queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {

    }

    @PostMapping("/getLabelList")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流面单查询")
    public void getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) {

    }

    @PostMapping("/getTrack")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流轨迹查询")
    public void getTrack(List<LogisticsQueryBaseVO> logisticsQueryBaseVOS) {

    }

    @PostMapping("/getChannel")
//    @LogAction(value = LogActionEnum.INSERT, desc = "物流渠道同步")
    public ApiResult getChannel(@RequestParam(value = "platform") String platform) {
        return logisticsBaseService.syncLogisticsChannel(platform);
    }
}
