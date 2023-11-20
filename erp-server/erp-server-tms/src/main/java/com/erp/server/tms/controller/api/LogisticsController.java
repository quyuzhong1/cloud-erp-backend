package com.erp.server.tms.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    @PostMapping("/createOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流新增订单")
    public ApiResult createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsService service = logisticsRegistry.getHandler("");
        ApiResult<LogisticsOrderResponseVO> order = service.createOrder(logisticsOrderVO);
        return success();
    }

    /**
     * 确认订单
     *
     * @param logisticsQueryVO
     * @return
     */
    @PostMapping("/confirmOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单确认")
    public ApiResult confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {
        return success();
    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    @PostMapping("/cancelOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单取消")
    public ApiResult cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        return success();
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    @PostMapping("/interceptOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单拦截")
    public ApiResult interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        return success();
    }

    /**
     * 更新订单
     *
     * @param logisticsOrderVOS
     * @return
     */
    @PostMapping("/updateOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流订单更新")
    public ApiResult updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {
        return success();
    }

    /**
     * 查询订单信息
     *
     * @param logisticsQueryVOList
     * @return
     */
    @PostMapping("/queryOrderList")
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> list = logisticsBaseService.queryOrderList(logisticsQueryVOList);
        return success(list);
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    @PostMapping("/getLabelList")
    public ApiResult getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) {
        return success();
    }

    /**
     * 获取物流信息
     *
     * @param logisticsQueryBaseVOS
     * @return
     */
    @PostMapping("/getTrack")
    public ApiResult getTrack(List<LogisticsQueryBaseVO> logisticsQueryBaseVOS) {
        return success();
    }

    /**
     * 同步渠道
     *
     * @param platform
     * @return
     */
    @PostMapping("/getChannel")
    public ApiResult getChannel(@RequestParam(value = "platform") String platform) {
        return logisticsBaseService.syncLogisticsChannel(platform);
    }

    /**
     * 批量更新物流信息
     *
     * @param logisticsBillDetailEntities
     * @return
     */
    @PostMapping("/batchUpdateTrackInfo")
    public ApiResult batchUpdateTrackInfo(@RequestBody List<LogisticsBillDetailEntity> logisticsBillDetailEntities) {
        if (CollectionUtils.isEmpty(logisticsBillDetailEntities)) return failure("数据不能为空");
        logisticsBaseService.batchUpdateTrackInfo(logisticsBillDetailEntities);
        return success();
    }
}
