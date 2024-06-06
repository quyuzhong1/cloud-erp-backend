package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.server.wms.mapper.VirtualTransFlowMapper;
import com.erp.server.wms.service.VirtualTransFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 虚拟库存交易流水表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualTransFlowServiceImpl extends SuperServiceImpl<VirtualTransFlowMapper, VirtualTransFlowEntity> implements VirtualTransFlowService {

    @Override
    public PagingVO<VirtualTransFlowDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean add(VirtualTransFlowDTO.AddDTO addDTO, String virtualTansRuleId,InventoryModeEnum inventoryModeEnum) {
        // 记录交易流水
        VirtualTransFlowEntity virtualTransFlowEntity = new VirtualTransFlowEntity();
        BeanMapperUtils.copy(addDTO,virtualTransFlowEntity);

        LoginUser loginUser = UserContext.getDefaultLoginUser();
        virtualTransFlowEntity.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "");
        virtualTransFlowEntity.setTradeTime(LocalDateTime.now());
        virtualTransFlowEntity.setVirtualTransRuleId(StrUtils.null2EmptyWithTrim(virtualTansRuleId));
        Integer qty = addDTO.getQty();
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
            qty = qty * -1;
        }
        virtualTransFlowEntity.setQty(qty);
        boolean save = super.save(virtualTransFlowEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("虚拟库存流水数据保存失败"));
        return save;
    }

    @Override
    public Boolean add(VirtualTransFlowEntity param, Integer afterInventoryQty) {
        // 记录交易流水
        LoginUser loginUser = UserContext.getDefaultLoginUser();

        // 复制所有参数
        VirtualTransFlowEntity virtualTransFlow = new VirtualTransFlowEntity();
        BeanMapper.copy(param,virtualTransFlow);
        // 更改指定的参数
        virtualTransFlow.setCurInventoryQty(afterInventoryQty);
        virtualTransFlow.setTradeTime(LocalDateTime.now());
        virtualTransFlow.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "0");

        // 个别参数设置空值
        virtualTransFlow.setId(null);
        boolean save = super.save(virtualTransFlow);
        ValidatorUtil.isTrue(save, ()->new ServiceException("虚拟库存数据保存失败"));
        return save;
    }

    @Override
    public List<VirtualTransFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId) {
        List<VirtualTransFlowEntity> txnFlows =  lambdaQuery()
                .eq(VirtualTransFlowEntity::getSourceType, sourceType)
                .eq(VirtualTransFlowEntity::getSourceId, sourceId)
                .eq(VirtualTransFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .eq(VirtualTransFlowEntity::getIsUnapproved, Boolean.FALSE)
                .orderByAsc(VirtualTransFlowEntity::getTradeTime)
                .orderByAsc(VirtualTransFlowEntity::getId)
                .list();

        return txnFlows;
    }

    @Override
    public Boolean updateUnapprovedById(String id, Integer version) {
        LoginUser loginUser =  UserContext.getDefaultLoginUser();
        return baseMapper.updateUnapprovedById(id, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Override
    public PagingVO<VirtualTransFlowDTO.InventoryDetailDTO> detailPaging(PagingDTO<VirtualTransFlowDTO.InventoryDetailParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.InventoryDetailDTO> pageData = this.baseMapper.detailPaging(query, dto.getParams());
        // 填充名称
        fillPageDetailData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/6/3 17:10
     * @param list
     */
    private void fillPageData (List<VirtualTransFlowDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
    }


    /**
     * 虚拟库存分页查询明细数据处理
     * @author will
     * @date 2024/6/3 16:58
     * @param list
     */
    private void fillPageDetailData (List<VirtualTransFlowDTO.InventoryDetailDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (VirtualTransFlowDTO.InventoryDetailDTO inventoryDetailDTO : list) {
            //来源类型名称
            inventoryDetailDTO.setSourceTypeName(SourceTypeEnum.getName(inventoryDetailDTO.getSourceType()));
        }
    }
}
