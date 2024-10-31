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
        if (CollectionUtils.isEmpty(updateDTOS)) {
            return failure("数据不能为空");
        }
        List<BatchResultDTO> resultDTOS = logisticsBaseService.batchUpdateTrackInfo(updateDTOS, LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
