package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.model.wms.enums.VirtualDetailMsgStatusEnum;
import com.erp.server.wms.mapper.WmsVirtualDetailMsgMapper;
import com.erp.server.wms.service.WmsVirtualDetailMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * wms虚拟仓明细同步表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class WmsVirtualDetailMsgServiceImpl extends SuperServiceImpl<WmsVirtualDetailMsgMapper, WmsVirtualDetailMsgEntity> implements WmsVirtualDetailMsgService {

    @Resource
    private MQProducerService mqProducerService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsVirtualDetailMsgDTO.AddDTO addDTO) {
        WmsVirtualDetailMsgEntity wmsVirtualDetailMsgEntity = new WmsVirtualDetailMsgEntity();
        BeanMapperUtils.copy(addDTO, wmsVirtualDetailMsgEntity);

        wmsVirtualDetailMsgEntity.setDataJson(JSONUtil.parseObj(addDTO.getTransFlowEntity()));
        // 数据处理
        handleData(wmsVirtualDetailMsgEntity);

        log.info("开始新增wms虚拟仓明细同步单");
        boolean save = super.save(wmsVirtualDetailMsgEntity);
        if(!save) {
            throw new ServiceException("wms虚拟仓明细同步单保存失败");
        }
        return new BaseResultDTO.AddDTO(wmsVirtualDetailMsgEntity.getId(), wmsVirtualDetailMsgEntity.getId());
    }

    @Override
    public void virtualDetailMsgJob() {
      List<WmsVirtualDetailMsgEntity> list =  listVirtualDetailMsg ();
      if (CollUtil.isEmpty(list)) {
          return;
      }
        // 异步推送到MQ
        List<WmsVirtualDetailMsgEntity> mqList = list.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.WMS_VIRTUAL_DETAIL_MSG_TOPIC, RocketMqTagEnum.WMS_VIRTUAL_DETAIL_MSG_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getDataJson(), msg.getStatus()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("gyyRefund发送数据为：{}" , JSON.toJSONString(mqList));
    }

    /**
     * 查询待处理任务
     * @author will
     * @date 2024/12/10 12:10
     * @return List<WmsVirtualDetailMsgEntity>
     */
    private List<WmsVirtualDetailMsgEntity> listVirtualDetailMsg() {
      return  lambdaQuery()
              .eq(WmsVirtualDetailMsgEntity::getStatus, VirtualDetailMsgStatusEnum.WAIT_HANDLE.getCode())
              .orderByAsc(WmsVirtualDetailMsgEntity::getId)
              .list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsVirtualDetailMsgEntity wmsVirtualDetailMsgEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
