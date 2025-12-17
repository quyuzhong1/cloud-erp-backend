package com.erp.server.oms.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.common.core.controller.vo.ApiResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * B2C销售订单物流信息表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cLogistics")
@LogSystemModule("B2C销售订单物流信息")
public class SoB2cLogisticsController extends BaseController {

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private LogisticsFeign logisticsFeign;


    /**
     * 转单预览
     * @param idDTO 订单id集合
     * @return
     */
    @PostMapping("/transferOrderView")
    public ApiResult<List<SoB2cLogisticsDTO.transferOrderDTO>> transferOrderView(@RequestBody @Validated BaseIdsDTO.IdsDTO idDTO) {
        List<String> ids = idDTO.getIds().stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        return success(soB2cLogisticsService.transferOrderView(ids));
    }

    /**
     * 转单保存
     * @param dtos 订单集合
     * @return
     */
    @PostMapping("/transferOrderSave")
    @LogAction(value = LogActionEnum.UPDATE, desc = "转单保存")
    public ApiResult<List<BatchResultDTO>> transferOrderSave(@RequestBody @Validated ValidList<SoB2cLogisticsDTO.transferOrderDTO> dtos) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtos.size());
        List<String> soIds = dtos.stream().map(SoB2cLogisticsDTO.transferOrderDTO::getId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        List<String> channelIds = dtos.stream().map(SoB2cLogisticsDTO.transferOrderDTO::getChannelId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> channelList = logisticsFeign.listChannelInfoById(channelIds);
        for (SoB2cLogisticsDTO.transferOrderDTO dto : dtos) {
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(item -> StrUtil.equals(item.getId(), dto.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNull(soB2cEntity)) {
                resultDTOS.add(new BatchResultDTO(dto.getId(),dto.getCode(),  "销售订单不存在",false));
                continue;
            }
            LogisticsChannelDTO.BaseDTO channel = channelList.stream().filter(item -> StrUtil.equals(item.getId(), dto.getChannelId())).findFirst().orElse(null);
            if (ObjectUtil.isNull(channel)) {
                resultDTOS.add(new BatchResultDTO(dto.getId(),dto.getCode(),  "物流渠道不存在",false));
                continue;
            }
            try {
                resultDTOS.add(soB2cLogisticsService.transferOrderSave(dto, channel,soB2cEntity));
            }catch (Exception e){
                log.error("B2C销售转单异常", e);
                resultDTOS.add(new BatchResultDTO(dto.getId(),dto.getCode(),  e.getMessage(),false));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
