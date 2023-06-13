package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.server.scm.mapper.SubcontractOrderDetailMapper;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 委外订单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractOrderDetailServiceImpl extends SuperServiceImpl<SubcontractOrderDetailMapper, SubcontractOrderDetailEntity> implements SubcontractOrderDetailService {


    @Override
    public void updateArrivalStatusByIds(String arrivalStatus, List<String> ids) {
        lambdaUpdate()
                .in(SubcontractOrderDetailEntity::getId,ids)
                .set(SubcontractOrderDetailEntity::getArrivalStatus,arrivalStatus)
                .set(SubcontractOrderDetailEntity::getArrivalTime, LocalDateTime.now())
                .set(SubcontractOrderDetailEntity::getIsEndReceive,Boolean.TRUE)
                .update();
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate()
                .in(SubcontractOrderDetailEntity::getMainId,mainIds)
                .remove();
    }

    @Override
    public void add(List<SubcontractOrderDetailDTO.AddDTO> detailList, String mainId) {

    }

    @Override
    public void update(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId) {

    }

    @Override
    public void listByMainId(String mainId) {

    }
}
