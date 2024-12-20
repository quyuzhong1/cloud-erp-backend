package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoB2cProcessingMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2C_PROCESSING;

/**
 * <p>
 * B2C虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class SoB2cProcessingServiceImpl extends SuperServiceImpl<SoB2cProcessingMapper, SoB2cProcessingEntity> implements SoB2cProcessingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private TransferInfoDetailService transferInfoDetailService;

    @Autowired
    private SoOutstockDetailService soOutstockDetailService;

    @Autowired
    private VirtualTransFlowService virtualTransFlowService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addOrUpdate(List<SoB2cProcessingDTO.AddOrUpdateDTO> list) {
        if (CollUtil.isEmpty(list)) {
            lambdaUpdate().remove();
            return Boolean.TRUE;
        }
        // 数据处理
        List<SoB2cProcessingEntity> soB2cProcessingList =  handleData(list);
        boolean save = super.saveOrUpdateBatch(soB2cProcessingList);
        if(!save) {
            throw new ServiceException("B2C虚拟仓订单跟踪保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2cProcessingDTO.ListDTO> paging(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<SoB2cProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(SoB2cProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2C虚拟仓列表信息", EXPORT_WMS_SO_B2C_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void autoUpdateSoB2cProcessing(LocalDate startDate) {
          List<SoB2cProcessingEntity> list = baseMapper.listSoB2cProcessing(startDate);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(SoB2cProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        /**
         * 1、b2b非组合品订单中转走直接调拨单出库，非中转走销售出库单出库
         * 2、b2b组合品走加工单出库
         */
        //查询加工单数据
        List<String> deliveryDetailIdList = list.stream().map(SoB2cProcessingEntity::getDeliveryId).distinct().collect(Collectors.toList());
        //查询发货单出库流水
        List<VirtualTransFlowEntity> virtualTransFlowList = virtualTransFlowService.listBySourceDetailIdList(deliveryDetailIdList);

        //直接调拨单数据
        List<SoB2bProcessingDTO.ResponseDTO> transferList = transferInfoDetailService.listTransferBySourceDetailIdList(deliveryDetailIdList);

        //销售出库单数据
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockList = soOutstockDetailService.listSoOutstockBySourceDetailIdList(deliveryDetailIdList);

        List<SoB2cProcessingDTO.AddOrUpdateDTO> addList = new ArrayList<>();
        for (SoB2cProcessingEntity entity :list) {

            //直接调拨单
            SoB2bProcessingDTO.ResponseDTO transferResponseDTO = transferList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryDetailId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(transferResponseDTO)) {
                handleOutstock (entity,transferResponseDTO, SourceTypeEnum.TRANSFER_INFO.getCode());
            }
            //销售出库单
            SoB2bProcessingDTO.ResponseDTO soOutstockResponseDTO = soOutstockList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryDetailId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soOutstockResponseDTO)) {
                handleOutstock (entity,soOutstockResponseDTO, SourceTypeEnum.SO_OUTSTOCK.getCode());
            }

            //发货单出库流水
            long count = virtualTransFlowList.stream().filter(obj -> StrUtil.equals(obj.getSourceDetailId(), entity.getDeliveryDetailId())).count();
            if (count > 0) {
                entity.setOutstockOrderId(entity.getId());
                entity.setOutstockOrderStatus(entity.getDeliveryStatus());
                entity.setOutstockQty(entity.getDeliveryQty());
                entity.setOutstockOrderCode(entity.getDeliveryCode());
                entity.setOutstockOrderTime(entity.getDeliveryTime());
                entity.setOutstockOrderType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
            }

            //bom信息
            List<BomChildrenSkuDTO> childList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), entity.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                SoB2cProcessingDTO.AddOrUpdateDTO addDTO = new SoB2cProcessingDTO.AddOrUpdateDTO();
                BeanMapperUtils.copy(entity,addDTO);
                addList.add(addDTO);
                continue;
            }
            for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                SoB2cProcessingDTO.AddOrUpdateDTO addDTO = new SoB2cProcessingDTO.AddOrUpdateDTO();
                BeanMapperUtils.copy(entity,addDTO);
                addDTO.setSkuId(childrenSkuDTO.getSkuId());
                addDTO.setParentSkuId(childrenSkuDTO.getParentSkuId());
                addDTO.setOutstockQty(addDTO.getOutstockQty() * childrenSkuDTO.getQuantity());
                addDTO.setFrozenQty(addDTO.getFrozenQty() * childrenSkuDTO.getQuantity());
                addDTO.setDeliveryQty(addDTO.getDeliveryQty() * childrenSkuDTO.getQuantity());
                addList.add(addDTO);
            }
        }
        this.addOrUpdate(addList);
    }


    /**
     * 出库字段赋值
     * @author will
     * @date 2024/12/19 20:56
     * @param entity
     * @param responseDTO
     * @param sourceType
     */
    private void handleOutstock (SoB2cProcessingEntity entity, SoB2bProcessingDTO.ResponseDTO responseDTO,String sourceType) {
        entity.setOutstockOrderId(responseDTO.getId());
        entity.setOutstockOrderStatus(responseDTO.getApproveStatus());
        entity.setOutstockQty(responseDTO.getQty());
        entity.setOutstockOrderCode(responseDTO.getCode());
        entity.setOutstockOrderTime(responseDTO.getApproveTime());
        entity.setOutstockOrderType(sourceType);
    }

    /**
    * 新增修改处理数据
    */
    private List<SoB2cProcessingEntity> handleData(List<SoB2cProcessingDTO.AddOrUpdateDTO> list) {
        List<String> deliveryDetailIdList = list.stream().map(SoB2cProcessingDTO.AddOrUpdateDTO::getDeliveryDetailId).distinct().collect(Collectors.toList());
        List<SoB2cProcessingEntity> oldList = this.listByDeliveryDetailIdList(deliveryDetailIdList);
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollUtil.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        List<SoB2cProcessingEntity> newList = new ArrayList<>();
        for (SoB2cProcessingDTO.AddOrUpdateDTO addOrUpdateDTO :list) {
            SoB2cProcessingEntity entity = new SoB2cProcessingEntity();
            BeanMapperUtils.copy(addOrUpdateDTO,entity);
            //旧数据
            SoB2cProcessingEntity old = oldList.stream().filter(obj -> StrUtil.equals(obj.getDeliveryDetailId(), addOrUpdateDTO.getDeliveryDetailId())).findFirst().orElse(null);
            entity.setId(old.getId());
            newList.add(entity);
        }
        return newList;
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SoB2cProcessingDTO.AddOrUpdateDTO> newList, List<SoB2cProcessingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoB2cProcessingDTO.AddOrUpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoB2cProcessingEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据发货单明细id查询
     * @author will
     * @date 2024/12/20 10:17
     * @param deliveryDetailIdList
     * @return List<SoB2bProcessingEntity>
     */
    private List<SoB2cProcessingEntity> listByDeliveryDetailIdList (List<String> deliveryDetailIdList) {
        if (CollUtil.isEmpty(deliveryDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cProcessingEntity::getDeliveryDetailId,deliveryDetailIdList).list();
    }


    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:24
     * @param list
     */
    private void fillPageData(List<SoB2cProcessingDTO.ListDTO> list) {
        // TODO 验证数据 & 数据赋值
    }
}
