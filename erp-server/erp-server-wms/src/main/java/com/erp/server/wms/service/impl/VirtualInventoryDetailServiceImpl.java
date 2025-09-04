package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.erp.server.wms.mapper.VirtualInventoryDetailMapper;
import com.erp.server.wms.service.VirtualInventoryDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 虚拟仓库明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryDetailServiceImpl extends SuperServiceImpl<VirtualInventoryDetailMapper, VirtualInventoryDetailEntity> implements VirtualInventoryDetailService {

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public VirtualInventoryDetailEntity addOrUpdate(VirtualInventoryDetailDTO.UpdateDTO addOrUpdateDTO) {
        VirtualInventoryDetailEntity entity = new VirtualInventoryDetailEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, entity);

        // 数据处理
        handleData(entity);

        //生成单号
        if (CharSequenceUtil.isBlank(entity.getId())) {
            String batchNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_N);
            entity.setBatchNo(batchNo);
        }
        log.info("开始新增虚拟仓库明细");
        boolean save = super.save(entity);
        if(!save) {
            throw new ServiceException("虚拟仓库明细保存失败");
        }
        return entity;
    }

    @Override
    public List<VirtualInventoryAgeDTO.SendNoticeSkuDTO> listDiffSkuSendNotice() {
        return baseMapper.listDiffSkuSendNotice();
    }

    @Override
    public List<VirtualInventoryAgeDTO.SendNoticeTotalDTO> listDiffTotalSendNotice() {
        return baseMapper.listDiffTotalSendNotice();
    }

    @Override
    public List<VirtualInventoryDetailEntity> getByOutParam(String skuId, String warehouseId, String virtualWarehouseId) {
        return baseMapper.getByOutParam(skuId,warehouseId,virtualWarehouseId);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryDetailEntity virtualInventoryDetailEntity) {

    }
}

