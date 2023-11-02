package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.model.wms.enums.FbaDeliveryStatusEnum;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.model.wms.enums.FbaPlatformShipmentStatusEnum;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.FbaShipmentConverter;
import com.erp.server.wms.mapper.FbaShipmentMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA货件表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaShipmentServiceImpl extends SuperServiceImpl<FbaShipmentMapper, FbaShipmentEntity> implements FbaShipmentService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Autowired
    private FbaShipmentStatusService fbaShipmentStatusService;
    @Autowired
    private FbaDeliveryService fbaDeliveryService;
    @Autowired
    private FbaDeliveryDetailService fbaDeliveryDetailService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private OmsListingInfoFeign omsListingInfoFeign;

    @Override
    public PagingVO<FbaShipmentDTO.ListDTO> paging(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<FbaShipmentDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean skuMapping(PagingDTO<FbaShipmentDTO.skuMappingParamDTO> dto) {

        return null;
    }

    @Override
    public Boolean pullShipment(FbaShipmentDTO.pullShipmentDTO dto) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.DeliverRecordView> listDeliverRecord(String id) {
        List<FbaShipmentDTO.DeliverRecordView> deliverRecordViews = fbaDeliveryService.listDeliveryRecordBySourceIds(Arrays.asList(id));
        return deliverRecordViews;
    }

    @Override
    public List<FbaShipmentDTO.ShipmentStatusRecordView> listShipmentStatusRecord(String id) {
        List<FbaShipmentStatusEntity> fbaShipmentStatusEntities = fbaShipmentStatusService.listByIds(Arrays.asList(id));
        List<FbaShipmentDTO.ShipmentStatusRecordView> list = new ArrayList<>();
        for (FbaShipmentStatusEntity fbaShipmentReceiveEntity : fbaShipmentStatusEntities) {
            //映射字段
            FbaShipmentDTO.ShipmentStatusRecordView shipmentStatusRecordView = FbaShipmentConverter.INSTANCE.fbaShipmentStatusEntityToView(fbaShipmentReceiveEntity);
            list.add(shipmentStatusRecordView);
        }
        return list;
    }

    @Override
    public List<FbaShipmentDTO.ReceiveRecordView> listReceiveRecord(String id) {
        List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(Arrays.asList(id));
        List<FbaShipmentDTO.ReceiveRecordView> list = new ArrayList<>();
        for (FbaShipmentReceiveEntity fbaShipmentReceiveEntity : fbaShipmentReceiveEntities) {
            //映射字段
            FbaShipmentDTO.ReceiveRecordView receiveRecordView = FbaShipmentConverter.INSTANCE.fbaShipmentReceiveEntityToView(fbaShipmentReceiveEntity);
            list.add(receiveRecordView);
        }
        return list;
    }

    @Override
    public FbaShipmentDTO.ViewDTO view(String id) {
        FbaShipmentEntity entity = this.getById(id);
        //映射字段
        FbaShipmentDTO.ViewDTO viewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentToViewDTO(entity);

        //根据主表id查询详情信息
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(id));

        //设置详情信息
        List<FbaShipmentDetailDTO.ViewDTO> detailViewList = new ArrayList<>();
        for (FbaShipmentDetailEntity fbaShipmentDetailEntity : fbaShipmentDetailEntities) {

            //映射详情字段
            FbaShipmentDetailDTO.ViewDTO detailViewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentDetailToViewDTO(fbaShipmentDetailEntity);

            //组装详情信息
            detailViewList.add(detailViewDTO);
        }

        //给产品信息赋值
        viewDTO.setDetailList(detailViewList);
        return viewDTO;
    }

    @Override
    public Boolean finishShipment(BaseIdsDTO.IdsDTO ids) {
        Boolean flag = lambdaUpdate()
                .set(FbaShipmentEntity::getDeliveryStatus, FbaDeliveryStatusEnum.IS_OVER.getCode())
                .in(FbaShipmentEntity::getId, ids).update();
        return flag;
    }

    @Override
    public List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(BaseIdsDTO.IdsDTO ids) {
        //根据id获取货件信息
        List<FbaShipmentDTO.GenerateDeliverView> list = baseMapper.generateDeliverView(ids);

        //获取所有店铺id
        List<String> shopIds = list.stream().map(req -> req.getShopId()).distinct().collect(Collectors.toList());

        //根据店铺id查询店铺信息
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(shopIds);

        for (FbaShipmentDTO.GenerateDeliverView view : list) {
            //处理字段映射
            generateDeliverViewFieldHandle(view, shopInfoEntities);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateDeliverSave(List<FbaShipmentDTO.GenerateDeliverView> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        //校验货件单据是否存在
        List<String> shipmentIds = list.stream().map(FbaShipmentDTO.GenerateDeliverView::getId).collect(Collectors.toList());
        List<FbaShipmentEntity> fbaShipmentEntities = this.listByIds(shipmentIds);
        if (CollectionUtils.isEmpty(fbaShipmentEntities)) {
            throw new ServiceException(ApiError.SHIPMENT_NOT_EXIST);
        }

        //根据仓库id查询仓库信息
        List<String> warehouseIds = list.stream().map(req -> req.getDeliveryWarehouseId()).distinct().collect(Collectors.toList());
        List<String> destWarehouseIds = list.stream().map(req -> req.getDestWarehouseId()).distinct().collect(Collectors.toList());
        warehouseIds.addAll(destWarehouseIds);
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);

        //根据仓库信息获取核算公司
        List<String> orgIds = warehouseEntities.stream().map(req -> req.getOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);

        //获取sku信息
        List<String> skuNoList = list.stream().map(FbaShipmentDTO.GenerateDeliverView::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);

        //获取库存sku信息
        List<SkuMappingDTO.listStockSkuNoByProductSkuNoView> listStockSkuNoByProductSkuNoViews = omsListingInfoFeign.listStockSkuNoByProductSkuNo(skuNoList);

        //根据货件单分组一个货件单生成一个发货单
        Map<String, List<FbaShipmentDTO.GenerateDeliverView>> map = list.stream().collect(Collectors.groupingBy(FbaShipmentDTO.GenerateDeliverView::getMainId));

        for (Map.Entry<String, List<FbaShipmentDTO.GenerateDeliverView>> entry : map.entrySet()) {
            List<FbaShipmentDTO.GenerateDeliverView> shipmentList = entry.getValue();
            //映射字段
            FbaDeliveryDTO.AddDTO addDTO = FbaShipmentConverter.INSTANCE.fbaGenerateDeliverViewToDeliveryAdd(shipmentList.get(0));
            addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
            addDTO.setDemandType(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode());

            //设置仓库名称
            String destWarehouseName = warehouseEntities.stream().filter(req -> req.getId().equals(addDTO.getDestWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            addDTO.setDestWarehouseName(destWarehouseName);
            WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(addDTO.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
            addDTO.setDestWarehouseName(warehouseEntity.getName());

            //设置库存组织
            String orgName = accountingCompanyList.stream().filter(d -> d.getId().equals(warehouseEntity.getOrgId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            addDTO.setInventoryOrgId(warehouseEntity.getOrgId());
            addDTO.setInventoryOrgName(orgName);
            //详情信息
            List<FbaDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (FbaShipmentDTO.GenerateDeliverView generateDeliverView : shipmentList) {
                //映射字段
                FbaDeliveryDetailDTO.AddDTO detailAdd = FbaShipmentConverter.INSTANCE.fbaGenerateDeliverViewToDeliveryDetailAdd(generateDeliverView);

                //映射产品信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(generateDeliverView.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
                detailAdd.setProductName(skuVO.getSkuName());
                detailAdd.setNetWeight(skuVO.getNetWeight());
                //拆分产品尺寸
                String productSize = skuVO.getProductSize();
                splitProductSize(detailAdd, productSize);
                String stockSku = listStockSkuNoByProductSkuNoViews.stream().filter(req -> req.getProductSkuNo().equals(generateDeliverView.getSkuNo())).distinct().findFirst()
                        .flatMap(obj -> Optional.ofNullable(obj.getWarehouseSkuNo())).orElse("");
                detailAdd.setStockSku(stockSku);
                detailAddList.add(detailAdd);
            }
            addDTO.setDetailList(detailAddList);
            fbaDeliveryService.add(addDTO);
        }
        return Boolean.TRUE;
    }

    /**
     * 处理列表查询字段
     * @Author Luo_WG
     * @Date 2023/11/2 17:35
     * @param records
     **/
    private void fillList(List<FbaShipmentDTO.ListDTO> records) {
        List<String> ids = records.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<String> detailIds = records.stream().map(req -> req.getDetailId()).distinct().collect(Collectors.toList());
        //根据来源详情id查询发货详情
        List<FbaDeliveryDetailEntity> fbaDeliveryDetailEntities = fbaDeliveryDetailService.listBySourceDetailIds(ids);
        //根据详情id查询收货记录
        List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(detailIds);
        for (FbaShipmentDTO.ListDTO record : records) {
            //设置发货状态中文
            record.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(record.getDeliveryStatus()));
            //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
            Integer deliveryQty = fbaDeliveryDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(record.getDetailId())).mapToInt(FbaDeliveryDetailEntity::getDeliveryQty).sum();
            record.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(record.getDeliveryStatus()));
            record.setDeliveryQty(deliveryQty);
            //签收数量 QuantityReceived
            Integer receiveQty = fbaShipmentReceiveEntities.stream().filter(req -> req.getDetailId().equals(record.getDetailId())).mapToInt(FbaShipmentReceiveEntity::getReceiveQty).sum();
            record.setReceiveQty(receiveQty);
            //在途数量 QuantityReceived-发货数量，不为0时显示红色
            record.setTransportQty(receiveQty - deliveryQty);
        }
    }

    /**
     * 处理下推发货单列表需要映射和配置的字段
     * @Author Luo_WG
     * @Date 2023/11/1 15:19
     * @param view 货件信息
     * @param shopInfoEntities 店铺信息
     * @return com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView
     **/
    private FbaShipmentDTO.GenerateDeliverView generateDeliverViewFieldHandle(FbaShipmentDTO.GenerateDeliverView view, List<ShopInfoEntity> shopInfoEntities) {
        //设置店铺的仓位为目的仓
        ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> view.getShopId().equals(req.getId())).findFirst().orElse(new ShopInfoEntity());
        view.setDestWarehouseId(shopInfoEntity.getWarehouseId());
        view.setDestWarehouseName(shopInfoEntity.getWarehouseName());
        //发货数量默认给申报数量
        view.setDeliveryQty(view.getDeclareQty());
        return view;
    }

    /**
     * 拆分产品尺寸长宽高存入数据集
     * @Author Luo_WG
     * @Date 2023/11/2 17:28
     * @param detailAdd 数据集
     * @param productSize 需要拆分的尺寸
     * @return void
     **/
    private void splitProductSize(FbaDeliveryDetailDTO.AddDTO detailAdd, String productSize) {
        if (StringUtils.isNotBlank(productSize)) {
            String[] productSizes = productSize.split("X");
            //长
            if (productSizes.length > 0) {
                if (StringUtils.isNotBlank(productSizes[0])) {
                    detailAdd.setProductSizeLength(new BigDecimal(productSizes[0]));
                } else {
                    detailAdd.setProductSizeLength(new BigDecimal(BigInteger.ZERO));
                }
            }
            //宽
            if (productSizes.length > 1) {
                if (StringUtils.isNotBlank(productSizes[1])) {
                    detailAdd.setProductSizeWidth(new BigDecimal(productSizes[1]));
                } else {
                    detailAdd.setProductSizeWidth(new BigDecimal(BigInteger.ZERO));
                }
            }
            //高
            if (productSizes.length > 2) {
                if (StringUtils.isNotBlank(productSizes[2])) {
                    detailAdd.setProductSizeHeight(new BigDecimal(productSizes[2]));
                } else {
                    detailAdd.setProductSizeHeight(new BigDecimal(BigInteger.ZERO));
                }
            }
        }
    }
}
