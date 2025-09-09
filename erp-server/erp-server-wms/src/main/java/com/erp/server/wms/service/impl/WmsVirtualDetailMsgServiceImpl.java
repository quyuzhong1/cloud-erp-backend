package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.server.wms.convert.WmsVirtualDetailMsgConverter;
import com.erp.server.wms.mapper.WmsVirtualDetailMsgMapper;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import com.erp.server.wms.service.WmsVirtualDetailMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsVirtualDetailMsgDTO.AddDTO addDTO) {
        log.warn("新增wms虚拟仓明细同步单，addDTO参数：{}", JSON.toJSONString(addDTO));

        WmsVirtualDetailMsgEntity wmsVirtualDetailMsgEntity = WmsVirtualDetailMsgConverter.INSTANCE.wmsVirtualDetailMsgToAdd(addDTO);
        //查询是否已存在任务
        WmsVirtualDetailMsgEntity old = getByBusinessId(wmsVirtualDetailMsgEntity.getBusinessId());
        if (ObjUtil.isNotNull(old)) {
            log.warn("业务id = {}，已生成任务",wmsVirtualDetailMsgEntity.getBusinessId());
            return new BaseResultDTO.AddDTO(old.getId(), old.getId());
        }
        log.warn("新增wms虚拟仓明细同步单，wmsVirtualDetailMsgEntity参数：{}", JSON.toJSONString(wmsVirtualDetailMsgEntity));

        boolean save = super.save(wmsVirtualDetailMsgEntity);
        if(!save) {
            throw new ServiceException("wms虚拟仓明细同步单保存失败");
        }
        log.warn("新增wms虚拟仓明细同步单，参数：{}", JSON.toJSONString(wmsVirtualDetailMsgEntity));

        return new BaseResultDTO.AddDTO(wmsVirtualDetailMsgEntity.getId(), wmsVirtualDetailMsgEntity.getId());
    }

    @Override
    public void virtualDetailMsgJob() {
        List<WmsVirtualDetailMsgDTO.ListDTO> wmsVirtualDetailMsgList = baseMapper.listFirstVirtualDetailMsg();
        if (CollUtil.isEmpty(wmsVirtualDetailMsgList)) {
            log.warn("未找到需要同步的数据");
          return;
        }
        //按sku、仓库、虚拟仓分组
        Map<String, List<WmsVirtualDetailMsgDTO.ListDTO>> map = wmsVirtualDetailMsgList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat("|").concat(obj.getWarehouseId()).concat("|").concat(obj.getVirtualWarehouseId())));
        // 多线程更新库存流水
        CompletableFuture<Void> allOf = CompletableFuture.allOf(map.entrySet().stream()
                .map(value -> CompletableFuture.runAsync(() ->
                        virtualTransFlowDetailService.consumeMsgJob(value.getValue()))
                ).toArray(CompletableFuture[]::new));
        allOf.thenRun(() -> log.info("虚拟仓库存流水消费，所有任务执行完毕")).join();
    }

    @Override
    public void updateStatus(WmsVirtualDetailMsgEntity entity) {
        lambdaUpdate().eq(WmsVirtualDetailMsgEntity::getId,entity.getId())
                .set(WmsVirtualDetailMsgEntity::getStatus,entity.getStatus())
                .set(WmsVirtualDetailMsgEntity::getRemark,entity.getRemark())
                .update();
    }

    /**
     * 根据业务id查询
     * @author will
     * @date 2024/12/17 18:05
     * @param businessId
     * @return WmsVirtualDetailMsgEntity
     */
    private WmsVirtualDetailMsgEntity getByBusinessId(String businessId) {
       return lambdaQuery().eq(WmsVirtualDetailMsgEntity::getBusinessId,businessId).last("limit 1").one();
    }
}
