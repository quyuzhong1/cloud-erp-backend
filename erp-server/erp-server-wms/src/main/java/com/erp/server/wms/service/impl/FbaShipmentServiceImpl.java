package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.enums.FbaDeliveryStatusEnum;
import com.erp.model.wms.enums.FbaPlatformShipmentStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.wms.convert.FbaShipmentConverter;
import com.erp.server.wms.mapper.FbaShipmentMapper;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.erp.server.wms.service.FbaShipmentService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentDTO;
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

    private void fillList(List<FbaShipmentDTO.ListDTO> records) {
        for (FbaShipmentDTO.ListDTO record : records) {
            //设置发货状态中文
            record.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(record.getDeliveryStatus()));
            //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总 TODO
            record.setDeliveryQty(0);
            //签收数量 QuantityReceived TODO
            record.setReceiveQty(0);
            //在途数量 QuantityReceived-发货数量，不为0时显示红色 TODO
            record.setTransportQty(0);
        }
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

        return null;
    }

    @Override
    public List<FbaShipmentDTO.ShipmentStatusRecordView> listShipmentStatusRecord(String code) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.ReceiveRecordView> listReceiveRecord(PagingDTO<FbaShipmentDTO.ReceiveRecordParam> dto) {
        return null;
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

        Map<String, List<FbaShipmentDTO.GenerateDeliverView>> map = list.stream().collect(Collectors.groupingBy(FbaShipmentDTO.GenerateDeliverView::getMainId));
        List<FbaShipmentDTO.AddDTO> addList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<FbaShipmentDTO.GenerateDeliverView>> entry : map.entrySet()) {

        }
        return null;
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
}
