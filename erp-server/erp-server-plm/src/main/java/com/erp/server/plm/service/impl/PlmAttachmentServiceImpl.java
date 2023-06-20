package com.erp.server.plm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.server.plm.mapper.PlmAttachmentMapper;
import com.erp.server.plm.service.PlmAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class PlmAttachmentServiceImpl extends SuperServiceImpl<PlmAttachmentMapper, PlmAttachmentEntity> implements PlmAttachmentService {


    /**
     * 批量保存附件信息
     *
     * @param attachUrlList
     * @param attachNameList
     * @param type
     * @param businessId
     * @return void
     * @author yl
     * @date 2023-06-20 15:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<String> attachUrlList, List<String> attachNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachNameList) ? attachNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachUrlList)) {
            List<PlmAttachmentEntity> addList = new ArrayList<>(attachUrlList.size());
            for (int i = 0; i < attachUrlList.size(); i++) {
                PlmAttachmentEntity entity = new PlmAttachmentEntity();
                entity.setAttachUrl(attachUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachNameList)) {
                    if (nameSize > i) {
                        entity.setAttachName(attachNameList.get(i));
                    }
                }
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }
}
