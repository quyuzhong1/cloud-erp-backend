package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.model.wms.enums.VirtualDetailMsgStatusEnum;
import com.erp.server.wms.mapper.WmsVirtualDetailMsgMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsVirtualDetailMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
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
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private MQProducerService mqProducerService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsVirtualDetailMsgDTO.AddDTO addDTO) {
        WmsVirtualDetailMsgEntity wmsVirtualDetailMsgEntity = new WmsVirtualDetailMsgEntity();
        BeanMapperUtils.copy(addDTO, wmsVirtualDetailMsgEntity);

        // 数据处理
        handleData(wmsVirtualDetailMsgEntity);

        log.info("开始新增wms虚拟仓明细同步单");
        boolean save = super.save(wmsVirtualDetailMsgEntity);
        if(!save) {
            throw new ServiceException("wms虚拟仓明细同步单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "wms虚拟仓明细同步单" , wmsVirtualDetailMsgEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsVirtualDetailMsgEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(wmsVirtualDetailMsgEntity.getId(), wmsVirtualDetailMsgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsVirtualDetailMsgDTO.UpdateDTO updateDTO) {
        WmsVirtualDetailMsgEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "wms虚拟仓明细同步单"));
        WmsVirtualDetailMsgEntity wmsVirtualDetailMsgEntity =  BeanMapperUtils.map(WmsVirtualDetailMsgEntity.class, updateDTO);

        // 数据处理
        handleData(wmsVirtualDetailMsgEntity);
        log.info("编辑 开始修改wms虚拟仓明细同步单数据，id：【{}】", old.getId());
        boolean save = super.updateById(wmsVirtualDetailMsgEntity);
        if(!save) {
            throw new ServiceException("wms虚拟仓明细同步单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录wms虚拟仓明细同步单日志数据，id：【{}】", wmsVirtualDetailMsgEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsVirtualDetailMsgEntity.getId(), "wms虚拟仓明细同步单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsVirtualDetailMsgEntity, null, wmsVirtualDetailMsgEntity.getId(), msg);
        return Boolean.TRUE;
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
