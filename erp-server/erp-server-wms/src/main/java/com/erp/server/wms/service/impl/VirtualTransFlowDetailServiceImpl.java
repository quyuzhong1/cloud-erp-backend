package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.model.wms.enums.VirtualDetailMsgStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.VirtualTransFlowDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @Resource
    private VirtualInventoryDetailService virtualInventoryDetailService;

    @Resource
    private WmsVirtualDetailMsgService wmsVirtualDetailMsgService;

    @Resource
    private VirtualInventoryDetailHisService virtualInventoryDetailHisService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchAdd(List<VirtualTransFlowDetailDTO.AddDTO> addDTOList) {
        if (CollUtil.isEmpty(addDTOList)) {
            return Boolean.TRUE;
        }
        List<VirtualTransFlowDetailEntity> list = BeanMapperUtils.copyList(VirtualTransFlowDetailEntity.class, addDTOList);

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
    public void consumeMsgJob(List<WmsVirtualDetailMsgDTO.ListDTO> msgList) {
        if (CollUtil.isEmpty(msgList)) {
            return;
        }
        //按操作时间排序
        List<WmsVirtualDetailMsgDTO.ListDTO> list = msgList.stream().sorted(Comparator.comparing(WmsVirtualDetailMsgDTO.ListDTO::getTradeTime)).collect(Collectors.toList());
        for (WmsVirtualDetailMsgDTO.ListDTO listDTO : list) {
            WmsVirtualDetailMsgEntity entity = BeanUtil.toBean(listDTO, WmsVirtualDetailMsgEntity.class);
            try {
                ApplicationContextUtils.getBean(VirtualTransFlowDetailServiceImpl.class).consumeMessage(listDTO.getBusinessId(),listDTO.getId());
            } catch (Exception e) {
                entity.setRemark("操作失败："+ e.getMessage());
                entity.setStatus(VirtualDetailMsgStatusEnum.FAIL.getCode());
                wmsVirtualDetailMsgService.updateStatus(entity);
                break;
            }
            entity.setRemark("操作成功");
            entity.setStatus(VirtualDetailMsgStatusEnum.SUCCESS.getCode());
            wmsVirtualDetailMsgService.updateStatus(entity);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = RedisKeyConstant.WMS_VIRTUAL_DETAIL_MSG_KEY,keyName = "msgId",waiteTime = 600)
    public Boolean consumeMessage(String businessId,String msgId) {
        VirtualTransFlowEntity entity = virtualTransFlowService.getById(businessId);
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException("未找到流水数据");
        }
        WmsVirtualDetailMsgEntity virtualDetailMsgEntity = wmsVirtualDetailMsgService.getById(msgId);
        if (ObjectUtil.isEmpty(virtualDetailMsgEntity)) {
            log.error("未找到虚拟仓库存流水明细消息，msgId = {}",msgId);
            return Boolean.TRUE;
        }
        if (CharSequenceUtil.equals(virtualDetailMsgEntity.getStatus(), VirtualDetailMsgStatusEnum.SUCCESS.getCode())) {
            log.warn("虚拟仓库存流水明细消息已处理，msgId = {}",msgId);
            return Boolean.TRUE;
        }
        //反审
        if (InventoryOperationModeEnum.UN_APPROVE.getCode().equals(entity.getOperationMode())) {
            handleVirtualTransFlowUnapproved(entity);
        } else {
            //查询流水数据是否已经生成了库龄流水数据
            List<VirtualTransFlowDetailEntity> virtualTransFlowDetailList = this.listByVirtualTransFlowId(entity.getId());
            if (CollUtil.isNotEmpty(virtualTransFlowDetailList)) {
                return Boolean.TRUE;
            }
            updateHandleVirtualTransFlow(entity);
        }
        //再次查询状态查询任务表状态是否是已完成
        WmsVirtualDetailMsgEntity virtualDetailMsgEntityAgain = wmsVirtualDetailMsgService.getById(msgId);
        if (ObjectUtil.isEmpty(virtualDetailMsgEntityAgain) || CharSequenceUtil.equals(virtualDetailMsgEntityAgain.getStatus(), VirtualDetailMsgStatusEnum.SUCCESS.getCode())) {
            throw new ServiceException("任务不存在或已完成不支持消费");
        }
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
        overrideFlowByVirtualInventoryDetailId(flowList,MathUtil.valueOfZero(virtualDetailQty));
        log.info("###VirtualTransFlowDetailServiceImpl:::overrideVirtualTransFlowDetail 库存流水重算完成 virtualInvId={}, end_time={}",  virtualInvDetailId, LocalDateTime.now());
    }

    @Override
    public void handleHisVirtualTransFlowDetail(VirtualTransFlowDetailDTO.HandleDTO dto) {
        List<VirtualTransFlowEntity> virtualTransFlowList = virtualTransFlowService.listApproveFlowDetail(dto);
        if (CollUtil.isEmpty(virtualTransFlowList)) {
            return ;
        }
        for (VirtualTransFlowEntity entity : virtualTransFlowList) {
            WmsVirtualDetailMsgDTO.AddDTO addDTO = new WmsVirtualDetailMsgDTO.AddDTO();
            addDTO.setRemark("虚拟仓库存出入库");
            addDTO.setTradeTime(entity.getTradeTime());
            addDTO.setStatus(VirtualDetailMsgStatusEnum.WAIT_HANDLE.getCode());
            addDTO.setBusinessId(entity.getId());
            wmsVirtualDetailMsgService.add(addDTO);
        }
    }

    @Override
    public List<VirtualTransFlowDetailEntity> listByVirtualTransFlowId(String parentVirtualTransFlowId) {
        return lambdaQuery().eq(VirtualTransFlowDetailEntity::getVirtualTransFlowId,parentVirtualTransFlowId).list();
    }

    @Override
    public void handleAddTransFlowDetail(List<VirtualTransFlowEntity> value) {
        for (VirtualTransFlowEntity entity : value) {
            List<VirtualTransFlowDetailEntity> flowDetailList = this.listByVirtualTransFlowId(entity.getId());
            if (CollUtil.isNotEmpty(flowDetailList)) {
                continue;
            }
            try {
                //反审
                if (InventoryOperationModeEnum.UN_APPROVE.getCode().equals(entity.getOperationMode())) {
                    handleVirtualTransFlowUnapproved(entity);
                } else {
                    //入库
                    if (entity.getQty() > MathUtil.ZERO) {
                        //生成批次库存数据
                        VirtualInventoryDetailEntity inventoryDetailEntity =  addVirtualInventoryDetail(entity);
                        //入库
                        instockVirtualTransFlowDetail(entity,inventoryDetailEntity);
                        continue;
                    }
                    //出库
                    handleOutstockVirtualTransFlowDetail(entity);
                }
            } catch (Exception e) {
                log.error("虚拟仓流水更新失败，id = {}，msg = {}",entity.getId(),e.getMessage());
                virtualTransFlowService.updateRemark(entity.getId(),"虚拟仓流水更新失败");
            }
        }
    }
    /**
     * 数据处理
     * @author will
     * @date 2024/12/27 20:38
     * @param entity
     */
    private void handleOutstockVirtualTransFlowDetail(VirtualTransFlowEntity entity) {
        //出库按先进先出
        List<VirtualInventoryDetailEntity> list = virtualInventoryDetailService.getByOutParam(entity.getSkuId(), entity.getWarehouseId(), entity.getVirtualWarehouseId());
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(CharSequenceUtil.format("流水id【{}】无可出库龄库存",entity.getId()));
        }
        List<VirtualTransFlowDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        List<VirtualInventoryDetailEntity> updateList = new ArrayList<>();
        Integer notOutQty = Math.abs(entity.getQty());
        for (int i = 0;i < list.size();i++ ) {
            VirtualInventoryDetailEntity detailEntity = list.get(i);
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
            addDTO.setQty(isOver ? - notOutQty : - detailEntity.getQty());
            addDTO.setCurInventoryQty(detailEntity.getQty() - Math.abs(addDTO.getQty()));
            //剩余未出数量
            notOutQty = notOutQty - Math.abs(addDTO.getQty());

            //如果最后一条入库后还存在出库则添加在最后一个入库上
            if (i == list.size() - 1 && notOutQty > 0) {
                addDTO.setQty(addDTO.getQty() + notOutQty);
                addDTO.setCurInventoryQty(detailEntity.getQty() - Math.abs(addDTO.getQty()));
            }
            addDTOList.add(addDTO);

            detailEntity.setQty(detailEntity.getQty() - Math.abs(addDTO.getQty()));
            detailEntity.setLastOutstockDate(entity.getBillDate());
            updateList.add(detailEntity);
        }
        this.batchAdd(addDTOList);
        if (CollUtil.isEmpty(updateList)) {
            return;
        }
        virtualInventoryDetailService.updateBatchById(updateList);
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
     * 更新反审核流水数据
     * @author will
     * @date 2024/12/20 11:15
     * @param entity
     */
    private void handleVirtualTransFlowUnapproved(VirtualTransFlowEntity entity) {
        //目前只有出库反审
        if (entity.getQty() < MathUtil.ZERO) {
            throw new ServiceException("暂未入库反审，不支持消费");
        }
        //根据当前流水id查询原流水信息
        VirtualTransFlowEntity oldTransFlowEntity = virtualTransFlowService.getUnApprovedTxnFlowBySource(entity);
        if (ObjUtil.isEmpty(oldTransFlowEntity)) {
            throw new ServiceException("未找到原虚拟仓出库库存流水信息");
        }
        /**
         * 1、删除原有出库流水出库时间后所有的库龄流水信息
         * 2、退回原流水库龄库存
         * 3、重新先进先出（除反审核的单）
         * 4、重算原出库流水时间后的结余
         */
        //删除原有出库库龄流水信息
        List<VirtualTransFlowDetailEntity> oldFlowDetailList = this.listByVirtualTransFlowId(oldTransFlowEntity.getId());
        if (CollUtil.isEmpty(oldFlowDetailList)) {
            throw new ServiceException("未找到原虚拟仓出库库龄流水信息");
        }
        oldFlowDetailList.forEach(obj -> obj.setIsUnapproved(Boolean.TRUE));
        this.updateBatchById(oldFlowDetailList);

        //根据sku、仓库id、虚拟仓id查询被删除的出库流水后的批次流水信息
        List<VirtualTransFlowDetailEntity> flowDetailList = baseMapper.listHisByOldParam(new VirtualTransFlowDetailDTO.ParamDTO(oldTransFlowEntity.getSkuId(), oldTransFlowEntity.getWarehouseId(), oldTransFlowEntity.getVirtualWarehouseId(), oldTransFlowEntity.getTradeTime()));
        if (CollUtil.isNotEmpty(flowDetailList)) {
            List<String> oldVirtualTransFlowIdList = flowDetailList.stream().map(VirtualTransFlowDetailEntity::getVirtualTransFlowId).distinct().collect(Collectors.toList());
            //退回批次流水库龄库存
            returnVirtualInventory(flowDetailList,oldTransFlowEntity.getTradeTime());

            //出库流水，重新先进先出
            List<VirtualTransFlowEntity> virtualTransFlowList = virtualTransFlowService.listApproveByIds(oldVirtualTransFlowIdList);
            if (CollUtil.isEmpty(virtualTransFlowList)) {
                return;
            }
            //更新库存
            for (VirtualTransFlowEntity flowEntity :virtualTransFlowList) {
                updateHandleVirtualTransFlow(flowEntity);
            }
        }
        //重算原出库流水时间后的结余
        virtualInventoryDetailHisService.addVirtualInventoryDetailHis(oldTransFlowEntity);
    }


    /**
     * 退回库龄库存
     * @author will
     * @date 2024/12/20 11:43
     * @param oldFlowDetailList
     */
    private void returnVirtualInventory (List<VirtualTransFlowDetailEntity> oldFlowDetailList, LocalDateTime tradeTime) {
        List<String> virtualInventoryDetailIdList = oldFlowDetailList.stream().map(VirtualTransFlowDetailEntity::getVirtualInventoryDetailId).distinct().collect(Collectors.toList());
        List<VirtualInventoryDetailEntity> virtualInventoryDetailList = virtualInventoryDetailService.listByIds(virtualInventoryDetailIdList);
        if (CollUtil.isEmpty(virtualInventoryDetailList)) {
            throw new ServiceException("未找到库龄库存数据");
        }
        for (VirtualInventoryDetailEntity detailEntity : virtualInventoryDetailList) {
            //合计数量
            Integer totalQty = oldFlowDetailList.stream().filter(obj -> StrUtil.equals(obj.getVirtualInventoryDetailId(), detailEntity.getId()))
                    .map(obj -> Math.abs(obj.getQty()))
                    .reduce(MathUtil.ZERO, Integer::sum);
            detailEntity.setQty(MathUtil.add(detailEntity.getQty(),totalQty));
        }
        virtualInventoryDetailService.updateBatchById(virtualInventoryDetailList);

        //删除流水
        List<String> oldDetailIdList = oldFlowDetailList.stream().map(VirtualTransFlowDetailEntity::getId).distinct().collect(Collectors.toList());
        this.removeByIds(oldDetailIdList);
    }

    /**
     * 更新库存
     * @author will
     * @date 2024/12/10 16:57
     * @param entity
     */
    @Override
    public void updateHandleVirtualTransFlow(VirtualTransFlowEntity entity) {
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

    @Override
    public List<VirtualTransFlowDetailDTO.SendNoticeSkuDTO> listDiffSkuSendNotice() {
        return baseMapper.listDiffSkuSendNotice();
    }

    @Override
    public List<VirtualTransFlowDetailDTO.SendNoticeTotalDTO> listDiffTotalSendNotice() {
        return baseMapper.listDiffTotalSendNotice();
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
        Integer notOutQty = Math.abs(entity.getQty());
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
            addDTO.setQty(isOver ? - notOutQty : - detailEntity.getQty());
            addDTO.setCurInventoryQty(detailEntity.getQty() - Math.abs(addDTO.getQty()));
            //剩余未出数量
            notOutQty = notOutQty - Math.abs(addDTO.getQty());
            addDTOList.add(addDTO);

            detailEntity.setQty(detailEntity.getQty() - Math.abs(addDTO.getQty()));
            detailEntity.setLastOutstockDate(entity.getBillDate());
            updateList.add(detailEntity);
        }
        if (MathUtil.compareTo(notOutQty,MathUtil.ZERO) > MathUtil.ZERO) {
            throw new ServiceException(StrUtil.format("单据【{}】库龄库存扣减失败",entity.getSourceCode()));
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
        BeanMapperUtils.copy(entity,addOrUpdateDTO);
        addOrUpdateDTO.setVirtualTransFlowId(entity.getId());
        addOrUpdateDTO.setLastOutstockDate(entity.getBillDate());
        addOrUpdateDTO.setId(null);
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
      if (CollUtil.isEmpty(detailList)) {
          return;
      }
      List<String> uniqueKeyList = new ArrayList<>();
        for (VirtualTransFlowDetailDTO.ListDTO listDTO : detailList) {
          listDTO.setDictInventoryStatusName(InventoryStatusEnum.getNameByCode(listDTO.getDictInventoryStatus()));
          listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
          LocalDate now = LocalDate.now();
          //库龄
          if ("转结".equals(listDTO.getOperateTypeName())) {
              listDTO.setInventoryAgeDays((int)(listDTO.getDate().toEpochDay() - listDTO.getBillDate().toEpochDay()) + 1);
          } else {
              listDTO.setInventoryAgeDays((int)(listDTO.getDate().toEpochDay() - listDTO.getBillDate().toEpochDay()) + 1);
              //仓储时长
              Integer inStockDays = (int) (listDTO.getTradeTime().toLocalDate().toEpochDay()- listDTO.getBillDate().toEpochDay() + 1);
              listDTO.setInStockDays(inStockDays);
          }
          String uniqueKey = StrUtil.format("{}_{}_{}_{}",listDTO.getSkuId(),listDTO.getWarehouseId(),listDTO.getVirtualWarehouseId(),listDTO.getBatchNo());
          if (uniqueKeyList.contains(uniqueKey)) {
              listDTO.setSkuNo("");
              listDTO.setProductName("");
              listDTO.setWarehouseName("");
              listDTO.setVirtualWarehouseCode("");
              listDTO.setVirtualWarehouseName("");
              listDTO.setTradeTime(null);
          }
      }
    }
}
