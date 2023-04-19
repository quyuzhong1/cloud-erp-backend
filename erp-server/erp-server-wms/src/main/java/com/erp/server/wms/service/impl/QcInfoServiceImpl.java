package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.entity.QcInfoEntity;
import com.erp.server.wms.mapper.QcInfoMapper;
import com.erp.server.wms.service.QcInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcInfoServiceImpl extends SuperServiceImpl<QcInfoMapper, QcInfoEntity> implements QcInfoService {


    /**
     * 质检信息 暂存
     *
     * @param billId
     * @param qcInfo
     * @return void
     * @author yl
     * @date 2023-04-19 10:11
     */
    @Override
    public void draft(String billId, QcInfoDTO.AddDTO qcInfo) {
        QcInfoEntity qcInfoEntity = new QcInfoEntity();
        String id = qcInfo.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }
        BeanMapper.copy(qcInfo, qcInfoEntity);
        qcInfoEntity.setMainId(billId);
        qcInfoEntity.setId(id);
    }
}
