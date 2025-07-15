package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.entity.ApproveTaskDetailEntity;
import com.erp.server.workflow.mapper.ApproveTaskDetailMapper;
import com.erp.server.workflow.service.ApproveTaskDetailService;
import com.erp.server.workflow.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 三方生成查询明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
@Slf4j
@Service
public class ApproveTaskDetailServiceImpl extends SuperServiceImpl<ApproveTaskDetailMapper, ApproveTaskDetailEntity> implements ApproveTaskDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<ApproveTaskDetailDTO.AddDTO> detailList,String mainId) {
        List<ApproveTaskDetailEntity> approveTaskDetailLis  = BeanUtil.copyToList(detailList, ApproveTaskDetailEntity.class);
        for (ApproveTaskDetailEntity detailLi : approveTaskDetailLis) {
            detailLi.setMianId(mainId);
        }
        log.info("开始新增三方生成查询明细");
        boolean save = super.saveBatch(approveTaskDetailLis);
        if(!save) {
            throw new ServiceException("三方生成查询明细保存失败");
        }
        return new BaseResultDTO.AddDTO(mainId,mainId);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<ApproveTaskDetailDTO.UpdateDTO> detailList, String mainId) {
        List<ApproveTaskDetailEntity> approveTaskDetailLis  = BeanUtil.copyToList(detailList, ApproveTaskDetailEntity.class);

        log.info("编辑 开始修改三方生成查询明细数据，主表id：【{}】", mainId);
        boolean save = super.updateBatchById(approveTaskDetailLis);
        if(!save) {
            throw new ServiceException("三方生成查询明细保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<ApproveTaskDetailEntity> listByMainId(String id) {
        return lambdaQuery().eq(ApproveTaskDetailEntity::getMianId,id).list();
    }

    @Override
    public Boolean removeByMainId(String mainId) {
        return lambdaUpdate().eq(ApproveTaskDetailEntity::getMianId,mainId).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ApproveTaskDetailEntity approveTaskDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
