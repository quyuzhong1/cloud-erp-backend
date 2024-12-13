package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.erp.server.wms.mapper.VirtualInventoryHisMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualInventoryHisService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟仓库存历史信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-10
 */
@Slf4j
@Service
public class VirtualInventoryHisServiceImpl extends SuperServiceImpl<VirtualInventoryHisMapper, VirtualInventoryHisEntity> implements VirtualInventoryHisService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(VirtualInventoryHisDTO.AddDTO addDTO) {
        VirtualInventoryHisEntity virtualInventoryHisEntity = new VirtualInventoryHisEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryHisEntity);

        // 数据处理
        handleData(virtualInventoryHisEntity);

        log.info("开始新增虚拟仓库存历史信息");
        boolean save = super.saveOrUpdate(virtualInventoryHisEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存历史信息保存失败");
        }

        return new BaseResultDTO.AddDTO(virtualInventoryHisEntity.getId(), virtualInventoryHisEntity.getId());
    }

    @Override
    public List<VirtualInventoryHisDTO.VirtualQtyDTO> listInventoryQty(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList,LocalDate localDate) {
        return baseMapper.listInventoryQty(skuIdList,warehouseIdList,virtualWarehouseIdList,localDate);
    }

    @Override
    public void addVirtualInventoryHis(LocalDate localDate) {
        List<VirtualInventoryHisDTO.AddDTO> list = baseMapper.listVirtualInventoryHis(localDate);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> virtualInventoryIdList = list.stream().map(VirtualInventoryHisDTO.AddDTO::getVirtualInventoryId).distinct().collect(Collectors.toList());
        List<LocalDate> dateList = list.stream().map(VirtualInventoryHisDTO.AddDTO::getDate).distinct().collect(Collectors.toList());
        List<VirtualInventoryHisEntity> oldList =  baseMapper.listByVirtualInventoryIdList(virtualInventoryIdList,dateList);

        for (VirtualInventoryHisDTO.AddDTO addDTO : list) {
            //传入时间减1
            addDTO.setDate(localDate.minusDays(1L));
            //查询是否已存在
            VirtualInventoryHisEntity hisEntity = oldList.stream().distinct().filter(obj ->
                            CharSequenceUtil.equals(obj.getVirtualInventoryId(), addDTO.getVirtualInventoryId())
                                    && obj.getDate().isEqual(addDTO.getDate()))
                    .findFirst().orElse(null);
            if (ObjUtil.isEmpty(hisEntity)) {
                continue;
            }
            addDTO.setId(hisEntity.getId());
            this.addOrUpdate(addDTO);
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryHisEntity virtualInventoryHisEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
