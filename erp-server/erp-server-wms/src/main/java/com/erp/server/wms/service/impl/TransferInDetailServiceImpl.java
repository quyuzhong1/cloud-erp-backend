package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.server.wms.mapper.TransferInDetailMapper;
import com.erp.server.wms.service.TransferInDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分布式调入单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferInDetailServiceImpl extends SuperServiceImpl<TransferInDetailMapper, TransferInDetailEntity> implements TransferInDetailService {


    /**
     * 添加明细
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-26 15:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String mainId, List<TransferInDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferInDetailEntity> addDetailList = BeanMapper.copyList(detailList, TransferInDetailEntity.class);
        addDetailList.stream().forEach(a -> a.setMainId(mainId));
        this.saveBatch(addDetailList);
        
    }
}
