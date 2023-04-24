package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.server.workflow.mapper.ProcessDefinitionMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class ProcessDefinitionServiceImpl extends SuperServiceImpl<ProcessDefinitionMapper, ProcessDefinitionEntity> implements ProcessDefinitionService {

    @Resource
    private ProcessBusinessService processBusinessService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        // dto转换为 processDefinitionEntity 和 processBusinessEntity 两个实体
        ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity(dto);
        ProcessBusinessEntity processBusinessEntity = new ProcessBusinessEntity(dto);
        // 不存在则新增
        if (null == entity) {
            // 保存 processDefinitionEntity
            if (!(save(processDefinitionEntity) && processBusinessService.save(processBusinessEntity))) {
                throw new ServiceException(ApiError.SAVE_PROCESS_ERROR);
            }
        }else {
            // 存在则更新
            processDefinitionEntity.setId(entity.getId());
            // 更新 processDefinitionEntity
            if (!updateById(processDefinitionEntity)) {
                throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ProcessDefinitionDTO.ListDTO> paging(PagingDTO<ProcessDefinitionDTO.QueryDTO> dto) {
        // 分页查询
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProcessDefinitionDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollectionUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        pageData.getRecords().stream().peek(x -> x.setApproveStatusName(x.getApproveStatusCode().getName())).collect(Collectors.toList());
        return new PagingVO<>(pageData);
    }
}
