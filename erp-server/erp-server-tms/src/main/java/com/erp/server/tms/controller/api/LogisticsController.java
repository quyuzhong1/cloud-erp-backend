package com.erp.server.tms.controller.api;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<BatchResultDTO> batchResultDTOS = logisticsBaseService.syncLogisticsChannel(platform);
        return success(batchResultDTOS);
    }

    /**
     * 批量更新物流信息
     *
     * @param updateDTOS
     * @return
     */
    @PostMapping("/batchUpdateTrackInfo")
    public ApiResult<List<BatchResultDTO>> batchUpdateTrackInfo(@RequestBody List<LogisticsTrackDTO.UpdateTrackDTO> updateDTOS) {
        if (CollectionUtils.isEmpty(updateDTOS)) return failure("数据不能为空");
        List<BatchResultDTO> resultDTOS = logisticsBaseService.batchUpdateTrackInfo(updateDTOS, LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
