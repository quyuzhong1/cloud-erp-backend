package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.dto.FirstMileProcessingDetailDTO;
import com.erp.model.wms.entity.FirstMileProcessingDetailEntity;
import com.erp.server.wms.convert.FirstMileProcessingDetailConverter;
import com.erp.server.wms.mapper.FirstMileProcessingDetailMapper;
import com.erp.server.wms.service.FirstMileProcessingDetailService;
import com.erp.server.wms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 头程虚拟仓订单跟踪明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-02-25
 */
@Slf4j
@Service
public class FirstMileProcessingDetailServiceImpl extends SuperServiceImpl<FirstMileProcessingDetailMapper, FirstMileProcessingDetailEntity> implements FirstMileProcessingDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Override
    public Boolean addFirstMileOrderDetail(List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> allDetailList) {

        //删除未关联主表的明细数据
        this.baseMapper.deleteUnrelatedDetail();
        //无明细数据无需添加
        if (CollUtil.isEmpty(allDetailList)) {
            return Boolean.TRUE;
        }
        List<FirstMileProcessingDetailEntity> addList = FirstMileProcessingDetailConverter.INSTANCE.addToEntity(allDetailList);
        return this.saveBatch(addList);
    }

    @Override
    public List<FirstMileProcessingDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileProcessingDetailEntity::getMainId, mainIdList).list();
    }
}
