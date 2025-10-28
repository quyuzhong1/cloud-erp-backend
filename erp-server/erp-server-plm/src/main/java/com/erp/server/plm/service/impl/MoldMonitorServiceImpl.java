package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.CfgMoldReturnAlertRuleCountDimEnum;
import com.erp.model.plm.enums.MoldMonitorLifeStatusEnum;
import com.erp.model.plm.enums.MoldMonitorReturnStatusEnum;
import com.erp.model.plm.enums.MoldMonitorStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.plm.service.*;
import com.erp.server.plm.mapper.MoldMonitorMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_MONITOR_ALERT;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_MONITOR_RETURN;

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
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private OperateLogService sysLogService;
    @Resource
    private MoldRefSkuService moldRefSkuService;
    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;

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
        if(Objects.isNull(pageData) || CollUtil.isEmpty(pageData.getRecords())) {
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
            data.setStatusName(MoldMonitorStatusEnum.getName(data.getStatus()));
            data.setReturnStatusName(MoldMonitorReturnStatusEnum.getName(data.getReturnStatus()));
            data.setLifeStatusName(MoldMonitorLifeStatusEnum.getName(data.getLifeStatus()));
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
        data.setStatusName(MoldMonitorStatusEnum.getName(data.getStatus()));
        data.setReturnStatusName(MoldMonitorReturnStatusEnum.getName(data.getReturnStatus()));
        data.setLifeStatusName(MoldMonitorLifeStatusEnum.getName(data.getLifeStatus()));
        data.setCountDimName(CfgMoldReturnAlertRuleCountDimEnum.getName(data.getCountDim()));
        data.setDisabledName(DisabledEnum.getName(data.getDisabled()));
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateReturnPriceById(MoldMonitorDTO.UpdateReturnParamsDTO dto) {
        MoldMonitorEntity old = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到模具返还监控数据"));
        String sourceType = old.getSourceType();
        if(!sourceType.equals(SourceTypeEnum.CFG_MOLD_RETURN_ALERT_RULE.getCode())){
            throw new ServiceException("仅支持配置模具返还预警规则生成的模具返还监控数据进行返还确认操作");
        }

        MoldMonitorEntity entity = new MoldMonitorEntity();
        BeanMapper.copy(old,entity);

        SysUserDTO sysUserDTO = sysUserFeign.getSysUserById(dto.getReturnUserId());

        entity.setActualReturnPrice(dto.getActualReturnPrice());
        entity.setReturnUserId(dto.getReturnUserId());
        if(Objects.nonNull(sysUserDTO)){
            entity.setReturnUserName(sysUserDTO.getUserName());
        }
        entity.setRemark(dto.getRemark());
        entity.setReturnDate(dto.getReturnDate());
        updateById(entity);

        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】模具编号【{}】返还数量上限【{}】返还监控", UserContext.getDefaultLoginUser().getUserName(), old.getMoldCode(),old.getReturnQtyLimit());
        sysLogService.addSysLogByUpdate(old,entity,String.valueOf(MoldMonitorEntity.class), entity.getId(), "", msg);

        //附件集合
        List<String> attachmentUrlList = dto.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = dto.getAttachmentNameList();
        List<PlmAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<MoldMonitorEntity> credentialClass = MoldMonitorEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                PlmAttachmentEntity attachment = new PlmAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(entity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                attachmentService.saveBatch(batchAttachmentList);
            }
        }
        return BatchResultDTO.success(old.getId(), old.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO cancelReturnPrice(String id) {
        MoldMonitorEntity old = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具返还监控数据"));
        String sourceType = old.getSourceType();
        if(!sourceType.equals(SourceTypeEnum.CFG_MOLD_RETURN_ALERT_RULE.getCode())){
            throw new ServiceException("仅支持配置模具返还预警规则生成的模具返还监控数据进行返还确认操作");
        }
        MoldMonitorEntity entity = new MoldMonitorEntity();
        BeanMapper.copy(old,entity);
        entity.setActualReturnPrice(BigDecimal.ZERO);
        entity.setReturnUserId("");
        entity.setReturnUserName("");
        entity.setRemark("");
        entity.setReturnDate(null);
        updateById(entity);

        // 日志数据
        String msg = StrUtil.format("用户【{}】模具编号【{}】返还数量上限【{}】取消返还确认", UserContext.getDefaultLoginUser().getUserName(), old.getMoldCode(),old.getReturnQtyLimit());
        sysLogService.addSysLogBySave(msg, "", entity.getId(), "");

        String code = StrUtil.format("模具编号【{}】, 返还数量上限【{}】", entity.getMoldCode(), entity.getReturnQtyLimit());
        return BatchResultDTO.success(old.getId(), code, OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO batchRefresh(MoldMonitorDTO.RefreshParamsDTO dto) {
        return null;
    }


    @Override
    public void exportReturn(MoldMonitorDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具返还监控导出", EXPORT_PLM_MOLD_MONITOR_RETURN.getCode(), param);
    }

    @Override
    public void exportAlert(MoldMonitorDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具预警监控导出", EXPORT_PLM_MOLD_MONITOR_ALERT.getCode(), param);
    }

    @Override
    public List<MoldMonitorEntity> buildMonitor(){
        //预警策略
        List<CfgMoldAlertRuleEntity> cfgMoldAlertRuleEntities = cfgMoldAlertRuleService.lambdaQuery().eq(CfgMoldAlertRuleEntity::getDisabled,false).eq(CfgMoldAlertRuleEntity::getInvalidStatus,false).list();

        //返还策略
        List<CfgMoldReturnAlertRuleDTO.ListDTO> cfgMoldReturnAlertRuleEntities = cfgMoldReturnAlertRuleService.listAll();

        List<MoldMonitorEntity> result = new ArrayList<>(cfgMoldAlertRuleEntities.size() + cfgMoldReturnAlertRuleEntities.size());

        if(CollUtil.isNotEmpty(cfgMoldReturnAlertRuleEntities)){

            List<String> ids = cfgMoldReturnAlertRuleEntities.stream().map(CfgMoldReturnAlertRuleDTO.ListDTO::getId).collect(Collectors.toList());

            List<MoldMonitorEntity> moldMonitorEntities = lambdaQuery().in(MoldMonitorEntity::getSourceId, ids).list();

            Map<String, MoldMonitorEntity> moldMonitorMap = moldMonitorEntities.stream().collect(Collectors.toMap(MoldMonitorEntity::getSourceDetailId, Function.identity()));

            for (CfgMoldReturnAlertRuleDTO.ListDTO entity : cfgMoldReturnAlertRuleEntities) {
                MoldMonitorEntity moldMonitorEntity = moldMonitorMap.getOrDefault(entity.getId(), null);
                if(Objects.isNull(moldMonitorEntity)){
                    moldMonitorEntity = new MoldMonitorEntity();
                }

                moldMonitorEntity.setSourceId(entity.getId());
                moldMonitorEntity.setSourceDetailId(entity.getDetailId());
                moldMonitorEntity.setSourceType(SourceTypeEnum.CFG_MOLD_RETURN_ALERT_RULE.getCode());
                moldMonitorEntity.setMoldId(entity.getMoldId());
                moldMonitorEntity.setMoldCode(entity.getMoldCode());
                moldMonitorEntity.setMoldName(entity.getMoldName());
                moldMonitorEntity.setSupplierId(entity.getSupplierId());
                moldMonitorEntity.setSupplierCode(entity.getSupplierCode());
                moldMonitorEntity.setSupplierName(entity.getSupplierName());
                moldMonitorEntity.setStartDate( entity.getStartDate());
                moldMonitorEntity.setEndDate( entity.getEndDate());
                moldMonitorEntity.setCountDim( entity.getCountDim());
                moldMonitorEntity.setDisabled(entity.getDisabled());
                moldMonitorEntity.setReturnQtyLimit(entity.getReturnQtyLimit());
                moldMonitorEntity.setReturnPrice(entity.getReturnPrice());
                moldMonitorEntity.setStatus(MoldMonitorStatusEnum.COUNTING.getCode());
//                moldMonitorEntity.setReturnStatus(MoldMonitorReturnStatusEnum.UNDERACHIEVED.getCode());
//                moldMonitorEntity.setPurchaseOrderQty(0);
//                moldMonitorEntity.setWarehouseReceiveQty(0);
//                moldMonitorEntity.setPoInstockQty(0);
                result.add(moldMonitorEntity);
            }
        }

        if(CollUtil.isNotEmpty(cfgMoldAlertRuleEntities)){
            List<String> ids = cfgMoldAlertRuleEntities.stream().map(CfgMoldAlertRuleEntity::getId).collect(Collectors.toList());

            List<MoldMonitorEntity> moldMonitorEntities = lambdaQuery().in(MoldMonitorEntity::getSourceId, ids).list();
            Map<String, MoldMonitorEntity> moldMonitorMap = moldMonitorEntities.stream().collect(Collectors.toMap(MoldMonitorEntity::getSourceId, Function.identity()));

            for (CfgMoldAlertRuleEntity entity : cfgMoldAlertRuleEntities) {
                MoldMonitorEntity moldMonitorEntity = moldMonitorMap.getOrDefault(entity.getId(), null);
                if(Objects.isNull(moldMonitorEntity)){
                    moldMonitorEntity = new MoldMonitorEntity();

                }
                moldMonitorEntity.setSourceId(entity.getId());
                moldMonitorEntity.setSourceType(SourceTypeEnum.CFG_MOLD_ALERT_RULE.getCode());

                moldMonitorEntity.setMoldId(entity.getMoldId());
                moldMonitorEntity.setMoldCode(entity.getMoldCode());
                moldMonitorEntity.setMoldName(entity.getMoldName());
                moldMonitorEntity.setSupplierId(entity.getSupplierId());
                moldMonitorEntity.setSupplierCode(entity.getSupplierCode());
                moldMonitorEntity.setSupplierName(entity.getSupplierName());
                moldMonitorEntity.setLifeQty( entity.getLifeQty());
                moldMonitorEntity.setAlertLifeQty( entity.getAlertLifeQty());
                moldMonitorEntity.setAlertLifeRate( entity.getAlertLifeRate());
                moldMonitorEntity.setStartDate( entity.getStartDate());
                moldMonitorEntity.setEndDate( entity.getEndDate());
                moldMonitorEntity.setCountDim( entity.getCountDim());
                moldMonitorEntity.setDisabled(entity.getDisabled());
                moldMonitorEntity.setStatus(MoldMonitorStatusEnum.COUNTING.getCode());
//                moldMonitorEntity.setLifeStatus(MoldMonitorLifeStatusEnum.HEALTHY.getCode());
//                moldMonitorEntity.setPurchaseOrderQty(0);
//                moldMonitorEntity.setWarehouseReceiveQty(0);
//                moldMonitorEntity.setPoInstockQty(0);
                result.add(moldMonitorEntity);
            }
        }
        saveOrUpdateBatch(result);
        return result;
    }

    @Override
    public void calMonitorOrder(List<MoldMonitorEntity> list){
        if(CollUtil.isEmpty(list)){
            return;
        }

        for (MoldMonitorEntity moldMonitorEntity : list) {
            List<MoldRefSkuEntity> moldRefSkuEntities = moldRefSkuService.lambdaQuery()
                    .eq(MoldRefSkuEntity::getMoldId, moldMonitorEntity.getMoldId())
                    .eq(MoldRefSkuEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
                    .list();

            if(CollUtil.isEmpty(moldRefSkuEntities)){
                moldMonitorEntity.setLifeStatus(MoldMonitorLifeStatusEnum.HEALTHY.getCode());
                moldMonitorEntity.setReturnStatus(MoldMonitorReturnStatusEnum.UNDERACHIEVED.getCode());
                moldMonitorEntity.setPurchaseOrderQty(0);
                moldMonitorEntity.setWarehouseReceiveQty(0);
                moldMonitorEntity.setPoInstockQty(0);
                continue;
            }

            List<MoldMonitorRefOrderEntity> moldMonitorRefOrderEntities = new ArrayList<>();

            List<String> skuIds = moldRefSkuEntities.stream().map(MoldRefSkuEntity::getSkuId).distinct().collect(Collectors.toList());

            String countDim = moldMonitorEntity.getCountDim();

            if(Objects.equals(countDim, CfgMoldReturnAlertRuleCountDimEnum.PURCHASEORDER.getCode())){
                PurchaseOrderDTO.PurchaseCalcQtyParamsDTO purchaseCalcQtyParamsDTO = new PurchaseOrderDTO.PurchaseCalcQtyParamsDTO();
                purchaseCalcQtyParamsDTO.setSkuIdList(skuIds);
                List<PurchaseOrderDTO.PurchaseCalcQtyDTO> detail = purchaseOrderFeign.listAllPurchaseBySkuIdAndSupplier(purchaseCalcQtyParamsDTO)
                        .stream()
                        .filter(e -> Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getCode()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(detail)){
                    for (PurchaseOrderDTO.PurchaseCalcQtyDTO purchaseCalcQtyDTO : detail) {
                        MoldMonitorRefOrderEntity moldMonitorRefOrderEntity = new MoldMonitorRefOrderEntity();
                        moldMonitorRefOrderEntity.setMainId(moldMonitorEntity.getId());
                        moldMonitorRefOrderEntity.setBusinessId(purchaseCalcQtyDTO.getId());
                        moldMonitorRefOrderEntity.setBusinessDetailId(purchaseCalcQtyDTO.getPurchaseDetailId());
                        moldMonitorRefOrderEntity.setBusinessType(CfgMoldReturnAlertRuleCountDimEnum.PURCHASEORDER.getCode());
                        moldMonitorRefOrderEntities.add(moldMonitorRefOrderEntity);
                    }
                    moldMonitorRefOrderService.saveBatch(moldMonitorRefOrderEntities);
                }
            }else if(Objects.equals(countDim, CfgMoldReturnAlertRuleCountDimEnum.WAREHOUSERECEIVE.getCode())){
                WarehouseReceiveDTO.ReceiveParamDTO dto = new WarehouseReceiveDTO.ReceiveParamDTO();
                List<WarehouseReceiveDTO.ReceiveInfoDTO> detail = wmsTaskFeign.getReceiveByParams(dto)
                        .stream()
                        .filter(e -> Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getCode()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(detail)){
                    for (WarehouseReceiveDTO.ReceiveInfoDTO receiveInfoDTO : detail) {
                        MoldMonitorRefOrderEntity moldMonitorRefOrderEntity = new MoldMonitorRefOrderEntity();
                        moldMonitorRefOrderEntity.setMainId(moldMonitorEntity.getId());
                        moldMonitorRefOrderEntity.setBusinessId(receiveInfoDTO.getId());
                        moldMonitorRefOrderEntity.setBusinessDetailId(receiveInfoDTO.getDetailId());
                        moldMonitorRefOrderEntity.setBusinessType(CfgMoldReturnAlertRuleCountDimEnum.WAREHOUSERECEIVE.getCode());
                        moldMonitorRefOrderEntities.add(moldMonitorRefOrderEntity);
                    }
                    moldMonitorRefOrderService.saveBatch(moldMonitorRefOrderEntities);
                }
            }else if(Objects.equals(countDim, CfgMoldReturnAlertRuleCountDimEnum.POINSTOCK.getCode())){
                PoInstockDTO.PoInStockParamDTO dto = new PoInstockDTO.PoInStockParamDTO();
                List<PoInstockDTO.PoInStockInfoDTO> detail = wmsTaskFeign.getPoStockInByParams(dto)
                        .stream()
                        .filter(e -> Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getCode()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(detail)){
                    for (PoInstockDTO.PoInStockInfoDTO poInStockInfoDTO : detail) {
                        MoldMonitorRefOrderEntity moldMonitorRefOrderEntity = new MoldMonitorRefOrderEntity();
                        moldMonitorRefOrderEntity.setMainId(moldMonitorEntity.getId());
                        moldMonitorRefOrderEntity.setBusinessId(poInStockInfoDTO.getId());
                        moldMonitorRefOrderEntity.setBusinessDetailId(poInStockInfoDTO.getDetailId());
                        moldMonitorRefOrderEntity.setBusinessType(CfgMoldReturnAlertRuleCountDimEnum.POINSTOCK.getCode());
                        moldMonitorRefOrderEntities.add(moldMonitorRefOrderEntity);
                    }
                    moldMonitorRefOrderService.saveBatch(moldMonitorRefOrderEntities);
                }
            }
        }
    }
}
