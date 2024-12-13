package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.model.wms.enums.VirtualDetailMsgStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.VirtualTransFlowDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_TRANS_FLOW_DETAIL;

/**
 * <p>
 * 虚拟仓库存流水明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualTransFlowDetailServiceImpl extends SuperServiceImpl<VirtualTransFlowDetailMapper, VirtualTransFlowDetailEntity> implements VirtualTransFlowDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private VirtualTransFlowService virtualTransFlowService;

    @Autowired
    private VirtualInventoryDetailService virtualInventoryDetailService;

    @Autowired
    private WmsVirtualDetailMsgService wmsVirtualDetailMsgService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchAdd(List<VirtualTransFlowDetailDTO.AddDTO> addDTOList) {
        List<VirtualTransFlowDetailEntity> list = new ArrayList<>();
        BeanMapperUtils.copyList(VirtualTransFlowDetailEntity.class, addDTOList);

        log.info("开始新增虚拟仓库存流水明细");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("虚拟仓库存流水明细保存失败");
        }
        return save;
    }

    @Override
    public PagingVO<VirtualTransFlowDetailDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualTransFlowDetailDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualTransFlowDetailDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("库龄流水", EXPORT_WMS_VIRTUAL_TRANS_FLOW_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = RedisKeyConstant.WMS_VIRTUAL_DETAIL_MSG_KEY,keyName = "msgId",waiteTime = 60)
    public Boolean consumeMessage(VirtualTransFlowEntity virtualTransFlowEntity,String msgId) {
        VirtualTransFlowEntity entity = virtualTransFlowService.getById(virtualTransFlowEntity.getId());
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException("未找到流水数据");
        }
        WmsVirtualDetailMsgEntity virtualDetailMsgEntity = wmsVirtualDetailMsgService.getById(msgId);
        if (ObjectUtil.isEmpty(virtualDetailMsgEntity) || !CharSequenceUtil.equals(virtualDetailMsgEntity.getStatus(), VirtualDetailMsgStatusEnum.DOING.getCode())) {
            throw new ServiceException("非进行中任务不支持消费");
        }
        handleVirtualTransFlow(entity);
        return Boolean.TRUE;
    }

    @Override
    public List<String> listVirtualInventoryDetailIdList(String virtualInventoryId, String virtualWarehouseId, String warehouseId, String skuId, Boolean fromTable) {
        return baseMapper.listVirtualInventoryDetailIdList(virtualInventoryId,virtualWarehouseId,warehouseId,skuId,fromTable);
    }

    @Override
    public void overrideVirtualTransFlowDetail(LocalDate startDate, String virtualInvDetailId) {
        log.info("###VirtualTransFlowDetailServiceImpl:::overrideVirtualTransFlowDetail 库存流水重算开始 virtualInvId={}, start_time={}", virtualInvDetailId, LocalDateTime.now());
        List<VirtualTransFlowDetailEntity> flowList = lambdaQuery()
                .eq(VirtualTransFlowDetailEntity::getVirtualInventoryDetailId, virtualInvDetailId)
                .ge(VirtualTransFlowDetailEntity::getBillDate, startDate)
                .last("for update")
                .list();
        if(CollUtil.isEmpty(flowList)) {
            log.warn("未找到需要重算的库存流水，库存id:{}, ", virtualInvDetailId);
        }
        flowList = flowList.stream().sorted(Comparator.comparing(VirtualTransFlowDetailEntity::getBillDate)
                        .thenComparing(VirtualTransFlowDetailEntity::getTradeTime)
                        .thenComparing(VirtualTransFlowDetailEntity::getId))
                .collect(Collectors.toList());
        Integer virtualDetailQty = this.baseMapper.virtualDetailQty(virtualInvDetailId, startDate);
        // 重算库存流水
        overrideFlowByVirtualInventoryDetailId(flowList,virtualDetailQty);
        log.info("###VirtualTransFlowDetailServiceImpl:::overrideVirtualTransFlowDetail 库存流水重算完成 virtualInvId={}, end_time={}",  virtualInvDetailId, LocalDateTime.now());
    }

    /**
     * 重算库存流水
     * @author will
     * @date 2024/12/12 12:17
     * @param flowList
     */
    private void overrideFlowByVirtualInventoryDetailId(List<VirtualTransFlowDetailEntity> flowList,Integer virtualDetailQty) {
        List<VirtualTransFlowDetailEntity> updateList = new ArrayList<>();
        for (VirtualTransFlowDetailEntity flowEntity : flowList) {
            Integer afterQty = virtualDetailQty + flowEntity.getQty();
            updateList.add(new VirtualTransFlowDetailEntity(flowEntity.getId(), afterQty));
            virtualDetailQty = afterQty;
        }
        updateBatchById(updateList);
    }

    /**
     * 更新库存
     * @author will
     * @date 2024/12/10 16:57
     * @param entity
     */
    private void handleVirtualTransFlow(VirtualTransFlowEntity entity) {
        //入库
        if (entity.getQty() > MathUtil.ZERO) {
            //生成批次库存数据
            VirtualInventoryDetailEntity inventoryDetailEntity =  addVirtualInventoryDetail(entity);
            //入库
            instockVirtualTransFlowDetail(entity,inventoryDetailEntity);
            return;
        }
        //出库
        outstockVirtualTransFlowDetail(entity);
    }

    /**
     * 添加出库流水
     * @author will
     * @date 2024/12/10 16:22
     * @param entity
     */
    private void outstockVirtualTransFlowDetail(VirtualTransFlowEntity entity) {
        //出库按先进先出
        List<VirtualInventoryDetailEntity> list = virtualInventoryDetailService.getByOutParam(entity.getSkuId(), entity.getWarehouseId(), entity.getVirtualWarehouseId());
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(CharSequenceUtil.format("流水id【{}】无可出库龄库存",entity.getId()));
        }
        List<VirtualTransFlowDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        List<VirtualInventoryDetailEntity> updateList = new ArrayList<>();
        Integer notOutQty = entity.getQty();
        for (VirtualInventoryDetailEntity detailEntity : list) {
            //当剩余出库数量为0时无需加流水
            if (MathUtil.compareTo(notOutQty,MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }

            VirtualTransFlowDetailDTO.AddDTO addDTO = new VirtualTransFlowDetailDTO.AddDTO();
            addDTO.setVirtualTransFlowId(entity.getId());
            addDTO.setBillDate(entity.getBillDate());
            addDTO.setTradeTime(entity.getTradeTime());
            addDTO.setVirtualInventoryDetailId(detailEntity.getId());
            //批次库存数量是否大于剩余出库数量
            boolean isOver = detailEntity.getQty() > notOutQty;
            addDTO.setQty(isOver ? notOutQty : detailEntity.getQty());
            addDTO.setCurInventoryQty(detailEntity.getQty() - addDTO.getQty());
            //剩余未出数量
            notOutQty = notOutQty - addDTO.getQty();
            addDTOList.add(addDTO);

            detailEntity.setQty(detailEntity.getQty() - addDTO.getQty());
            updateList.add(detailEntity);
        }
        if (CollUtil.isEmpty(addDTOList)) {
            throw new ServiceException("未找到库龄流水数据");
        }
        this.batchAdd(addDTOList);

        if (CollUtil.isEmpty(updateList)) {
            throw new ServiceException("未找到库龄库存数据");
        }
        virtualInventoryDetailService.updateBatchById(updateList);
    }

    /**
     * 入库批次库存数据
     * @author will
     * @date 2024/12/10 15:23
     * @param entity
     * @return VirtualInventoryDetailEntity
     */
    private VirtualInventoryDetailEntity addVirtualInventoryDetail (VirtualTransFlowEntity entity) {
        VirtualInventoryDetailDTO.UpdateDTO  addOrUpdateDTO = new VirtualInventoryDetailDTO.UpdateDTO();
        BeanMapperUtils.copy(addOrUpdateDTO,entity);
        addOrUpdateDTO.setVirtualTransFlowId(entity.getId());
        addOrUpdateDTO.setLastOutstockDate(LocalDate.now());
        //新增入库批次
        VirtualInventoryDetailEntity inventoryDetailEntity = virtualInventoryDetailService.addOrUpdate(addOrUpdateDTO);
        return inventoryDetailEntity;
    }

    /**
     * 入库流水
     * @author will
     * @date 2024/12/10 16:01
     * @param entity
     * @param inventoryDetailEntity
     * @return VirtualTransFlowDetailEntity
     */
    private void instockVirtualTransFlowDetail (VirtualTransFlowEntity entity,VirtualInventoryDetailEntity inventoryDetailEntity) {
        VirtualTransFlowDetailDTO.AddDTO addDTO = new VirtualTransFlowDetailDTO.AddDTO();
        addDTO.setVirtualTransFlowId(entity.getId());
        addDTO.setVirtualInventoryDetailId(inventoryDetailEntity.getId());
        addDTO.setTradeTime(entity.getTradeTime());
        addDTO.setQty(entity.getQty());
        addDTO.setBillDate(entity.getBillDate());
        addDTO.setCurInventoryQty(inventoryDetailEntity.getQty());
        this.batchAdd(Collections.singletonList(addDTO));
    }


    /**
     * 分页列表处理数据
     */
    private void fillPageData(List<VirtualTransFlowDetailDTO.ListDTO> detailList) {
        // TODO 验证数据 & 数据赋值
    }
}
