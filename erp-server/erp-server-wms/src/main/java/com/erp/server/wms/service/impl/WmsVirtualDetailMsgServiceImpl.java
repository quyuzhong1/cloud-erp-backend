package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
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
import java.util.Arrays;
import java.util.List;

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
      WmsVirtualDetailMsgEntity entity =  listVirtualDetailMsg();
      if (ObjUtil.isEmpty(entity)) {
          return;
      }
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.WMS_VIRTUAL_DETAIL_MSG_TOPIC, RocketMqTagEnum.WMS_VIRTUAL_DETAIL_MSG_TAG.getName(),
                entity, StrUtil.format("{}_{}", entity.getDataJson(), entity.getStatus()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        //更新状态进行中
        entity.setStatus(VirtualDetailMsgStatusEnum.DOING.getCode());
        this.updateById(entity);
        log.debug("gyyRefund发送数据为：{}" , JSON.toJSONString(entity));
    }

    @Override
    public void updateStatus(WmsVirtualDetailMsgEntity entity) {
        lambdaUpdate().eq(WmsVirtualDetailMsgEntity::getId,entity.getId())
                .set(WmsVirtualDetailMsgEntity::getStatus,entity.getStatus())
                .set(WmsVirtualDetailMsgEntity::getRemark,entity.getRemark())
                .update();
    }

    /**
     * 查询待处理任务
     * @author will
     * @date 2024/12/10 12:10
     * @return WmsVirtualDetailMsgEntity
     */
    private WmsVirtualDetailMsgEntity listVirtualDetailMsg() {
        List<WmsVirtualDetailMsgEntity> doingList = listVirtualDetailMsgDoing();
        if (CollUtil.isEmpty(doingList)) {
            return null;
        }
        return  lambdaQuery()
                .in(WmsVirtualDetailMsgEntity::getStatus, Arrays.asList(VirtualDetailMsgStatusEnum.WAIT_HANDLE.getCode(),VirtualDetailMsgStatusEnum.FAIL.getCode()))
                .orderByAsc(WmsVirtualDetailMsgEntity::getId)
                .last("limit 1")
                .one();
    }

    /**
     * 查询进行中数据
     * @author will
     * @date 2024/12/17 16:16
     * @return List<WmsVirtualDetailMsgEntity>
     */
    private List<WmsVirtualDetailMsgEntity> listVirtualDetailMsgDoing() {
        return  lambdaQuery()
                .eq(WmsVirtualDetailMsgEntity::getStatus,VirtualDetailMsgStatusEnum.DOING.getCode())
                .list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsVirtualDetailMsgEntity wmsVirtualDetailMsgEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
