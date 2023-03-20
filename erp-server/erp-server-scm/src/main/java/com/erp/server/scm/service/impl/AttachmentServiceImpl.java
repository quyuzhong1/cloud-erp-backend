package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.entity.AttachmentEntity;
import com.erp.server.scm.mapper.AttachmentMapper;
import com.erp.server.scm.service.AttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 公共附件表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class AttachmentServiceImpl extends SuperServiceImpl<AttachmentMapper, AttachmentEntity> implements AttachmentService {


    /**
     * 根据业务表id获取附件信息
     *
     * @param businessIds
     * @return java.util.List<com.erp.model.scm.dto.AttachmentDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:27
     */
    @Override
    public List<AttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        List<AttachmentEntity> list = this.list(businessIds);
        return BeanMapper.copyList(list, AttachmentDTO.UpdateDTO.class);
    }


    /**
     * 根据业务表id 集合删除
     *
     * @param businessIdList
     * @return void
     * @author yl
     * @date 2023-03-20 11:52
     */
    @Override
    public void deleteByBusinessIds(List<String> businessIdList) {
        if (CollectionUtils.isNotEmpty(businessIdList)) {
            LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(AttachmentEntity::getBusinessId, businessIdList);
            this.remove(queryWrapper);
        }
    }


    private List<AttachmentEntity> list(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return new ArrayList<>(1);
        }
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AttachmentEntity::getBusinessId, businessIds);
        return this.list(queryWrapper);
    }
}
