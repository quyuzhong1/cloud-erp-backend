package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.dto.FirstMileProcessingDetailDTO;
import com.erp.model.wms.entity.FirstMileProcessingDetailEntity;
import com.erp.server.wms.mapper.FirstMileProcessingDetailMapper;
import com.erp.server.wms.service.FirstMileProcessingDetailService;
import com.erp.server.wms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void addFirstMileOrderDetail(List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> allDetailList) {
        if (CollUtil.isEmpty(allDetailList)) {

        }
    }
}
