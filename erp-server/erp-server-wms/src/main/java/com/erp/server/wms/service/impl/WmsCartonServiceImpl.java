package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FirstMileCartonMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货单箱规信息 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonServiceImpl extends SuperServiceImpl<FirstMileCartonMapper, WmsCartonEntity> implements WmsCartonService {

    @Autowired
    private WmsCartonDetailService wmsCartonDetailService;
    @Autowired
    private WmsCartonBillService wmsCartonBillService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private SoOutstockDetailService soOutstockDetailService;
    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WmsCartonDTO.AddDTO addDTO, String sourceId, String sourceType) {
        WmsCartonEntity wmsCartonEntity = new WmsCartonEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonEntity);
        packQtyCheck(sourceId);


        // 数据处理
        handleData(wmsCartonEntity, sourceId, sourceType);

        log.info("开始新增发货单箱规信息");
        boolean save = super.saveOrUpdate(wmsCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        //新增详情信息
        wmsCartonDetailService.add(addDTO, wmsCartonEntity.getId(), sourceId, sourceType);
    }

    @Override
    public List<WmsCartonEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonEntity::getSourceId, sourceIds)
                .orderByAsc(WmsCartonEntity::getCreateTime, WmsCartonEntity::getId)
                .list();
    }

    @Override
    public List<WmsCartonDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId, Integer boxSpecNo) {
        return baseMapper.listPackingQtyByMainId(mainId, boxSpecNo);
    }

    @Override
    public Boolean deleteBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getSourceId, sourceIds).remove();
    }

    @Override
    public List<WmsCartonDTO.PackDateDTO> listPackDateBySourceId(String sourceId) {
        if (StringUtils.isBlank(sourceId)) {
            return Collections.emptyList();
        }
        return baseMapper.listPackDateBySourceId(sourceId);
    }

    @Override
    public Boolean packQtyCheck(String sourceId) {
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonDTO.PackDateDTO> packDateDTOS = this.listPackDateBySourceId(sourceId);
        for (WmsCartonDTO.PackDateDTO packDateDTO : packDateDTOS) {
            //待装箱数量=发货数量-所有已装箱数量
            int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(req -> req.getBoxQty() * req.getPackQty()).sum();
            if (packDateDTO.getDeliveryQty() < packQtySum) {
                throw new ServiceException(ApiError.PACKING_QTY_NOT_GT_WAIT_PACKING_QTY, packDateDTO.getBoxSpecNo(), packDateDTO.getSkuNo());
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public WmsCartonDTO.WmsCartonView getCartonViewBySourceId(String sourceId) {
        WmsCartonDTO.WmsCartonView view = new WmsCartonDTO.WmsCartonView();
        //查询箱规信息
        List<WmsCartonEntity> wmsCartonEntities = this.listBySourceIds(Arrays.asList(sourceId));
        List<WmsCartonDTO.ViewDTO> wmsCartonList = BeanMapper.copyList(wmsCartonEntities, WmsCartonDTO.ViewDTO.class);
        view.setWmsCartonList(wmsCartonList);

        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(Arrays.asList(sourceId));
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(sourceId));


        //查询箱规包含的产品信息
        for (WmsCartonDTO.ViewDTO viewDTO : wmsCartonList) {
            //根据主表id分组sku查询发货及待装箱数
            List<WmsCartonDTO.PackDateDTO> packDateDTOS = this.listPackDateBySourceId(sourceId);

            List<WmsCartonDTO.PackDateDTO> packDateDTOList = packDateDTOS.stream().filter(req -> req.getBoxSpecNo().equals(viewDTO.getBoxSpecNo())).collect(Collectors.toList());

            //查询产品信息
            List<String> skuIdList = packDateDTOList.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

            List<WmsCartonDetailDTO.ViewDTO> detailList = BeanMapper.copyList(packDateDTOList, WmsCartonDetailDTO.ViewDTO.class);
            for (WmsCartonDetailDTO.ViewDTO dto : detailList) {
                Integer deliveryQty = 0;
                if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(dto.getSourceType())) {
                    deliveryQty = soOutstockDetailEntities.stream().mapToInt(req -> req.getActualQty()).sum();
                }
                if (SourceTypeEnum.FIRST_MILE_DELIVERY.getCode().equals(dto.getSourceType())) {
                    deliveryQty = firstMileDeliveryDetailEntities.stream().mapToInt(req -> req.getDeliveryQty()).sum();
                }
                dto.setDeliveryQty(deliveryQty);
                //待装箱数量=发货数量-所有已装箱数量
                int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).mapToInt(req -> req.getBoxQty() * req.getPackQty()).sum();
                dto.setWaitPackQty(deliveryQty - packQtySum);

                //匹配产品信息，设置中文名
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(skuVO)) {
                    dto.setProductName(skuVO.getSkuName());
                }
            }
            viewDTO.setDetailList(detailList);
        }
        return view;
    }


    /**
     * 删除原装箱信息
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCarton(String id) {
        List<WmsCartonEntity> firstMileCartonEntities = this.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(firstMileCartonEntities)) {
            //删除箱子明细信息
            wmsCartonBillService.deleteBySourceIds(Arrays.asList(id));
            //删除原箱包装信息
            wmsCartonDetailService.deleteBySourceIds(Arrays.asList(id));
            //删除原箱信息
            this.deleteBySourceIds(Arrays.asList(id));
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonEntity wmsCartonEntity, String sourceId, String sourceType) {
        wmsCartonEntity.setSourceId(sourceId);
        wmsCartonEntity.setSourceType(sourceType);
    }
}
