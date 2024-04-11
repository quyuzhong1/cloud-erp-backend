package com.erp.server.tms.controller.api;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
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
     * 创建订单
     *
     * @param
     * @return
     */
    @PostMapping("/createOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流新增订单")
    public ApiResult createOrder() {
        Map<String, String> authMap = new HashMap<>();
        authMap.put("clientId","dcfe81e2059c1f0e6e6263dbcb764885");
        authMap.put("clientSecret","dcfe81e2059c1f0e6e6263dbcb7648850d0c1386bae3caf82229e7cf472d7b53");

        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setAddressFirst("address");
        senderInfo.setContact("contact");
        senderInfo.setCityName("newyork");
        senderInfo.setCompanyName("componeny");
        senderInfo.setName("name");
        senderInfo.setProvinceName("shenzhen");
        senderInfo.setTelNumber("12345678");
        senderInfo.setEmail("321546");
        senderInfo.setCountry("CN");
        senderInfo.setZipCode("515800");
        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setPrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
//        logisticsSaleChannel.setCode("MX1001");
        logisticsSaleChannel.setCode("MX100100");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("155");

        LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
//                .channelCode("MX1001")
//                .channelId("155")
                .orderSource("ERP")
                .deliveryNo("wj12345167721")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("123@q.con")
                        .city("shenz")
                        .name("mark")
                        .companyName("componey")
                        .contact("mark")
                        .country("MX")
                        .zipCode("11510")
                        .province("state")
                        .telNumber("123456789")
                        .build())
                .senderInfo(senderInfo)
                .parceInfoVO(ParceInfoVO.builder()
                        .currency("USD")
                        .height(1)
                        .hasBattery(true)
                        .totalPrice(new BigDecimal("123"))
                        .totalQuantity(12)
                        .totalWeight(1999)
                        .length(1)
                        .totalWeight(123)
                        .width(123)
                        .build())
                .logisticsSaleChannel(logisticsSaleChannel)
                .logisticsProductVOList(Arrays.asList(
                        logisticsProductVO
                ))
                .build();

        LogisticsService service = logisticsRegistry.getHandler(LogisticsPlatformEnum.WEI_SHI.getCode());
        ApiResult<LogisticsOrderResponseVO> order = service.createOrder(logisticsOrderVO);
        return success(order);
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
        List<BatchResultDTO> resultDTOS = logisticsBaseService.batchUpdateTrackInfo(updateDTOS);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
