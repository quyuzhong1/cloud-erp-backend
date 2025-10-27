package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.entity.MoldMonitorRefOrderEntity;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.enums.CfgMoldReturnAlertRuleCountDimEnum;
import com.erp.model.plm.enums.MoldMonitorLifeStatusEnum;
import com.erp.model.plm.enums.MoldMonitorReturnStatusEnum;
import com.erp.model.plm.enums.MoldMonitorStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.server.plm.constant.SourceType;
import com.erp.server.plm.service.*;
import com.erp.model.plm.entity.MoldMonitorEntity;
import com.erp.server.plm.mapper.MoldMonitorMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;

import javax.annotation.Resource;

/**
 * <p>
 * 模具监控 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-22
 */
@Slf4j
@Service
public class MoldMonitorServiceImpl extends SuperServiceImpl<MoldMonitorMapper, MoldMonitorEntity> implements MoldMonitorService {

    @Resource
    private MoldMonitorRefOrderService moldMonitorRefOrderService;

    @Resource
    private CfgMoldReturnAlertRuleService cfgMoldReturnAlertRuleService;
    @Resource
    private CfgMoldAlertRuleService cfgMoldAlertRuleService;

    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private PlmAttachmentService attachmentService;

    @Override
    public List<MoldMonitorDTO.TabListDTO> tabList(MoldMonitorDTO.TabDTO param) {
        MoldMonitorDTO.PagingParamDTO searchParam = new MoldMonitorDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        searchParam.setSourceType(param.getSourceType());
        List<MoldMonitorDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        List<MoldMonitorDTO.TabListDTO> result = new ArrayList<>();
        result.add(new MoldMonitorDTO.TabListDTO("all","全部", 0));

        // 预警状态
        if(Objects.equals(param.getSourceType(), SourceTypeEnum.CFG_MOLD_ALERT_RULE.getCode())){
            List<String> statusList = MoldMonitorLifeStatusEnum.getStatusList();
            for (String status : statusList) {
                MoldMonitorDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldMonitorDTO.TabListDTO(status, "", 0));
                tabListDTO.setTabFlagName(MoldMonitorLifeStatusEnum.getName(status));
                result.add(tabListDTO);
            }
        }else {
            // 返还状态
            List<String> statusList = MoldMonitorReturnStatusEnum.getStatusList();
            for (String status : statusList) {
                MoldMonitorDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldMonitorDTO.TabListDTO(status, "", 0));
                tabListDTO.setTabFlagName(MoldMonitorReturnStatusEnum.getName(status));
                result.add(tabListDTO);
            }
        }
        return result;
    }

    @Override
    public PagingVO<MoldMonitorDTO.ListDTO> paging(PagingDTO<MoldMonitorDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<MoldMonitorDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<MoldMonitorDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        for (MoldMonitorDTO.ListDTO data : list) {
            //获取策略的数据
            jsonToData(data);
            data.setStatusName(MoldMonitorStatusEnum.getName(data.getStatus()));
            data.setReturnStatusName(MoldMonitorReturnStatusEnum.getName(data.getReturnStatus()));
            data.setLifeStatusName(MoldMonitorLifeStatusEnum.getName(data.getLifeStatus()));
        }
    }

    /**
     * 将JSON格式的数据转换并填充到MoldMonitorDTO.ListDTO对象中
     * @param data 需要填充数据的MoldMonitorDTO.ListDTO对象
     */
    private static void jsonToData(MoldMonitorDTO.ListDTO data) {
        String sourceRuleJson = data.getSourceRuleJson();
        if(StringUtils.isNotBlank(sourceRuleJson) && !Objects.equals("{}",sourceRuleJson)){
            //sourceRuleJson 转实体类MoldMonitorDTO.ViewDTO
            MoldMonitorDTO.ViewDTO map = BeanMapperUtils.map(MoldMonitorDTO.ViewDTO.class, sourceRuleJson);

            data.setMoldId(map.getMoldId());
            data.setMoldCode(map.getMoldCode());
            data.setMoldName(map.getMoldName());
            data.setSupplierId(map.getSupplierId());
            data.setSupplierCode(map.getSupplierCode());
            data.setSupplierName(map.getSupplierName());
            data.setStartDate(map.getStartDate());
            data.setEndDate(map.getEndDate());
            data.setCountDim(map.getCountDim());
            data.setDisabled(map.getDisabled());
            data.setReturnQtyLimit(map.getReturnQtyLimit());
            data.setReturnPrice(map.getReturnPrice());
            data.setLifeQty(map.getLifeQty());
            data.setAlertLifeQty(map.getAlertLifeQty());
            data.setAlertLifeRate(map.getAlertLifeRate());

            data.setCountDimName(CfgMoldReturnAlertRuleCountDimEnum.getName(data.getCountDim()));
            data.setDisabledName(DisabledEnum.getName(data.getDisabled()));
        }
    }

    @Override
    public MoldMonitorDTO.ViewDTO view(String id) {
        MoldMonitorEntity moldMonitorEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具监控数据"));
        MoldMonitorDTO.ViewDTO data = BeanMapperUtils.map(MoldMonitorDTO.ViewDTO.class, moldMonitorEntity);
        // 数据填充处理
        fillOne(data);

        // 查询相关的附件信息
        List<PlmAttachmentEntity> attachmentList = attachmentService.listByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            // 分别提取附件名称和URL列表设置到返回对象中
            data.setAttachmentNameList(attachmentList.stream().map(PlmAttachmentEntity::getAttachName).collect(Collectors.toList()));
            data.setAttachmentUrlList(attachmentList.stream().map(PlmAttachmentEntity::getAttachUrl).collect(Collectors.toList()));
        }
        return data;
    }

    private void fillOne(MoldMonitorDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //获取策略的数据
        jsonToData(data);

        data.setStatusName(MoldMonitorStatusEnum.getName(data.getStatus()));
        data.setReturnStatusName(MoldMonitorReturnStatusEnum.getName(data.getReturnStatus()));
        data.setLifeStatusName(MoldMonitorLifeStatusEnum.getName(data.getLifeStatus()));
    }



    /**
     * 根据主ID和业务类型查询关联订单信息列表。
     * <p>
     * 该方法根据传入的参数查询与模具监控相关的引用订单数据，并根据不同的业务类型（如采购订单、仓库收货单、采购入库单）
     * 查询对应的主表和明细表数据，组装成统一的返回结构。
     *
     * @param dto 查询参数对象，包含主ID和业务类型等信息
     * @return 返回与主ID和业务类型相关的引用订单信息列表；如果参数为空或不合法，则返回空列表
     */
    @Override
    public List<MoldMonitorDTO.RefOrderDTO> listRefOrderById(MoldMonitorDTO.RefOrderParamsDTO dto) {
        if(Objects.isNull(dto) || StringUtils.isBlank(dto.getId()) || StringUtils.isBlank(dto.getBusinessType())){
            return Collections.emptyList();
        }

        List<MoldMonitorDTO.RefOrderDTO> result = new ArrayList<>();
        String id = dto.getId();
        String businessType = dto.getBusinessType();

        // 查询引用订单实体并按业务ID分组
        Map<String, List<MoldMonitorRefOrderEntity>> listMap = moldMonitorRefOrderService.lambdaQuery()
                .eq(MoldMonitorRefOrderEntity::getMainId, id)
                .eq(MoldMonitorRefOrderEntity::getBusinessType, businessType)
                .list().stream().collect(Collectors.groupingBy(MoldMonitorRefOrderEntity::getBusinessId));

        for (Map.Entry<String, List<MoldMonitorRefOrderEntity>> entry : listMap.entrySet()) {
            String businessId = entry.getKey();
            List<String> businessDetailIds = entry.getValue().stream().map(MoldMonitorRefOrderEntity::getBusinessDetailId).distinct().collect(Collectors.toList());

            // 处理采购订单业务类型
            if(Objects.equals(businessType , CfgMoldReturnAlertRuleCountDimEnum.PURCHASEORDER.getCode())){
                List<PurchaseOrderEntity> main = FeignQuery.create(PurchaseOrderEntity.class).eq(PurchaseOrderEntity::getId, businessId).list();
                List<PurchaseOrderDetailEntity> detail = FeignQuery.create(PurchaseOrderDetailEntity.class).in(PurchaseOrderDetailEntity::getId, businessDetailIds).list();
                if(CollUtil.isNotEmpty(main) && CollUtil.isNotEmpty(detail)){
                    PurchaseOrderEntity entity = main.get(0);
                    String supplierId="";
                    String supplierName="";
                    List<PurchaseOrderSupplierEntity> supplier = FeignQuery.create(PurchaseOrderSupplierEntity.class).eq(PurchaseOrderSupplierEntity::getPurchaseOrderId, businessId).list();
                    if(CollUtil.isNotEmpty(supplier)){
                        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = supplier.get(0);
                        supplierId = purchaseOrderSupplierEntity.getSupplierId();
                        supplierName = purchaseOrderSupplierEntity.getSupplierName();
                    }

                    for (PurchaseOrderDetailEntity detailEntity : detail) {
                        MoldMonitorDTO.RefOrderDTO data = new MoldMonitorDTO.RefOrderDTO();
                        data.setId(entity.getId());
                        data.setCode(entity.getCode());
                        data.setApproveStatus(entity.getApproveStatus());
                        data.setApproveStatusName(ApproveStatusEnum.getName(entity.getApproveStatus()));
                        data.setInvalidStatus(entity.getInvalidStatus());
                        data.setInvalidStatusName(InvalidStatusEnum.getName(entity.getInvalidStatus()));
                        data.setDate(entity.getPurchaseDate());

                        data.setDetailId(detailEntity.getId());
                        data.setSkuId(detailEntity.getSkuId());
                        data.setSkuNo(detailEntity.getSkuNo());
                        data.setProductName(detailEntity.getProductName());
                        data.setQty(detailEntity.getPurchaseQty());

                        data.setSupplierId(supplierId);
                        data.setSupplierName(supplierName);
                        result.add(data);
                    }
                }

                // 处理仓库收货单业务类型
            }else if(Objects.equals(businessType , CfgMoldReturnAlertRuleCountDimEnum.WAREHOUSERECEIVE.getCode())){
                List<WarehouseReceiveEntity> main = FeignQuery.create(WarehouseReceiveEntity.class).eq(WarehouseReceiveEntity::getId, businessId).list();
                List<WarehouseReceiveDetailEntity> detail = FeignQuery.create(WarehouseReceiveDetailEntity.class).in(WarehouseReceiveDetailEntity::getId, businessDetailIds).list();
                if(CollUtil.isNotEmpty(main) && CollUtil.isNotEmpty(detail)){
                    WarehouseReceiveEntity entity = main.get(0);
                    List<String> skuIds = detail.stream().map(WarehouseReceiveDetailEntity::getSkuId).collect(Collectors.toList());
                    // 查询产品信息
                    List<SkuVO> skuVOList = productDetailService.listSkuProductByIds(skuIds);
                    for (WarehouseReceiveDetailEntity detailEntity : detail) {
                        MoldMonitorDTO.RefOrderDTO data = new MoldMonitorDTO.RefOrderDTO();
                        data.setId(entity.getId());
                        data.setCode(entity.getCode());
                        data.setApproveStatus(entity.getApproveStatus());
                        data.setApproveStatusName(ApproveStatusEnum.getName(entity.getApproveStatus()));
                        data.setInvalidStatus(entity.getInvalidStatus());
                        data.setInvalidStatusName(InvalidStatusEnum.getName(entity.getInvalidStatus()));
                        data.setDate(entity.getBillDate());

                        data.setDetailId(detailEntity.getId());
                        data.setSkuId(detailEntity.getSkuId());
                        data.setSkuNo(detailEntity.getSkuNo());
                        SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
                        if(Objects.nonNull(skuVO)){
                            data.setProductName(skuVO.getSkuName());
                        }
                        data.setQty(detailEntity.getReceiveQty());

                        data.setSupplierId(entity.getSupplierId());
                        data.setSupplierName(entity.getSupplierName());
                        result.add(data);
                    }
                }

                // 处理采购入库单业务类型
            }else if(Objects.equals(businessType , CfgMoldReturnAlertRuleCountDimEnum.POINSTOCK.getCode())){
                List<PoInstockEntity> main = FeignQuery.create(PoInstockEntity.class).eq(PoInstockEntity::getId, businessId).list();
                List<PoInstockDetailEntity> detail = FeignQuery.create(PoInstockDetailEntity.class).in(PoInstockDetailEntity::getId, businessDetailIds).list();
                if(CollUtil.isNotEmpty(main) && CollUtil.isNotEmpty(detail)){
                    PoInstockEntity entity = main.get(0);
                    List<String> skuIds = detail.stream().map(PoInstockDetailEntity::getSkuId).collect(Collectors.toList());
                    // 查询产品信息
                    List<SkuVO> skuVOList = productDetailService.listSkuProductByIds(skuIds);

                    for (PoInstockDetailEntity detailEntity : detail) {
                        MoldMonitorDTO.RefOrderDTO data = new MoldMonitorDTO.RefOrderDTO();
                        data.setId(entity.getId());
                        data.setCode(entity.getCode());
                        data.setApproveStatus(entity.getApproveStatus());
                        data.setApproveStatusName(ApproveStatusEnum.getName(entity.getApproveStatus()));
                        data.setInvalidStatus(entity.getInvalidStatus());
                        data.setInvalidStatusName(InvalidStatusEnum.getName(entity.getInvalidStatus()));
                        data.setDate(entity.getStockInDate());

                        data.setDetailId(detailEntity.getId());
                        data.setSkuId(detailEntity.getSkuId());
                        data.setSkuNo(detailEntity.getSkuNo());
                        SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
                        if(Objects.nonNull(skuVO)){
                            data.setProductName(skuVO.getSkuName());
                        }
                        data.setQty(detailEntity.getReceiveQty());

                        data.setSupplierId(entity.getSupplierId());
                        data.setSupplierName(entity.getSupplierName());
                        result.add(data);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public BatchResultDTO updateReturnPriceById(MoldMonitorDTO.UpdateReturnParamsDTO dto) {
        return null;
    }

}
