package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.utils.PdfUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.oms.enums.LabelSourceTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.csource.common.MyException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_DELIVERY_NOTICE;

/**
 * <p>
 * 发货通知单主表明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoDeliveryNoticeServiceImpl extends SuperServiceImpl<SoDeliveryNoticeMapper, SoDeliveryNoticeEntity> implements SoDeliveryNoticeService {
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;


    @Resource
    private InventoryService inventoryService;


    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Lazy
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private CfgRuleOutService cfgRuleOutService;
    @Resource
    private CfgRulePickingStagingService  cfgRulePickingStagingService;

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private PackingTaskService packingTaskService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Lazy
    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Resource
    private SoDeliveryNoticeChangeService soDeliveryNoticeChangeService;

    @Resource
    private MachineInfoService machineInfoService;
    @Resource
    private MachineDetailService machineDetailService;
    @Resource
    private FileTemplateFeign fileTemplateFeign;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;

    @Override
    public PagingVO<SoDeliveryNoticeDTO.PagingView> paging(PagingDTO<SoDeliveryNoticeDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoDeliveryNoticeDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }

        //明细数据
        List<SoDeliveryNoticeDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = records.stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        Map<String,Integer> qtyMap = new HashMap<>();
        //中转仓map
        Map<String, String> warehouseMap =  warehouseService.list().stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                if (CharSequenceUtil.isNotBlank(obj.getPackingStatus())) {
                    obj.setPackingStatusName(PackingTaskStatusEnum.getName(obj.getPackingStatus()));
                }else{
                    obj.setPackingStatus(PackingTaskStatusEnum.WAIT.getCode());
                    obj.setPackingStatusName(PackingTaskStatusEnum.WAIT.getName());
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                if (obj.getDeliveryStatus() != null && obj.getDeliveryStatus()) {
                    obj.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
                } else {
                    obj.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
                }
                obj.setIsPicked(obj.getDeliveryQty().equals(obj.getPickedQty()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                obj.setUnit(productDetailEntity.getUnitName());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                //如果装箱数量大于发货数量，拆分处理
                String key = obj.getId() + obj.getSkuId();
                if(qtyMap.containsKey(key)){
                    Integer reduceQty = qtyMap.get(key);
                    if(reduceQty > obj.getDeliveryQty()){
                        obj.setPackingQty(obj.getDeliveryQty());
                        qtyMap.put(key,reduceQty - obj.getDeliveryQty());
                    }else{
                        obj.setPackingQty(reduceQty);
                        qtyMap.put(key,0);
                    }
                }else{
                    if(obj.getPackingQty() > obj.getDeliveryQty()){
                        qtyMap.put(key,obj.getPackingQty() - obj.getDeliveryQty());
                        obj.setPackingQty(obj.getDeliveryQty());
                    }else{
                        qtyMap.put(key,0);
                        obj.setPackingQty(obj.getPackingQty());
                    }
                }
                //中转仓名称
                if (CharSequenceUtil.isNotBlank(obj.getTransferWarehouseIds())){
                    StringBuilder sb = new StringBuilder();
                    List<String> split = CharSequenceUtil.split(obj.getTransferWarehouseIds(), ",");
                    for (String s : split) {
                        sb.append(warehouseMap.get(s)).append(",");
                    }
                    obj.setTransferWarehouseNames(sb.substring(0, sb.length() - 1));
                }
            });
        }
        Map<String,List<SoDeliveryNoticeDTO.PagingView>> map = records.stream().collect(Collectors.groupingBy(SoDeliveryNoticeDTO.PagingView::getId));
        map.forEach((key,val)->{
            for (SoDeliveryNoticeDTO.PagingView pagingView : val) {
                pagingView.setIsPicked(pagingView.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())&&val.stream().allMatch(SoDeliveryNoticeDTO.PagingView::getIsPicked));
            }
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<SoDeliveryNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        OsDeliveryChangeListTypeEnum[] values = OsDeliveryChangeListTypeEnum.values();
        List<SoDeliveryNoticeDTO.StatusCountDTO> list = new ArrayList<>();
        for (OsDeliveryChangeListTypeEnum item : values) {
            SoDeliveryNoticeDTO.PagingParam pagingParam = new SoDeliveryNoticeDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            SoDeliveryNoticeDTO.StatusCountDTO resultDTO = new SoDeliveryNoticeDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (OsDeliveryChangeListTypeEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.UN_SHIPPED.getCode().equals(item.getCode())) {
                pagingParam.setDeliveryStatus(Boolean.FALSE);
                pagingParam.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (OsDeliveryChangeListTypeEnum.COMPLETE_SHIPMENT.getCode().equals(item.getCode())) {
                pagingParam.setDeliveryStatus(Boolean.TRUE);
                count = this.baseMapper.listCount(pagingParam);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoDeliveryNoticeDTO.Add dto) {
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(dto.getSourceId());
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FHTZ, BusinessNoTypeEnum.CODE_FHTZ.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHTZ);
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        soDeliveryNoticeEntity.setType(soInfoEntity.getOrderType());
        soDeliveryNoticeEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        soDeliveryNoticeEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        soDeliveryNoticeEntity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (CharSequenceUtil.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                soDeliveryNoticeEntity.setSalesDeptName(dept.getName());
            }
        }
        //虚拟仓库
        soDeliveryNoticeEntity.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());

        soDeliveryNoticeEntity.setSellerId(soInfoEntity.getSellerId());
        soDeliveryNoticeEntity.setSellerName(soInfoEntity.getSellerName());
        soDeliveryNoticeEntity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soDeliveryNoticeEntity.setCustomerName(customerInfoEntity.getName());
        soDeliveryNoticeEntity.setReceiverName(soInfoEntity.getReceiverName());
        soDeliveryNoticeEntity.setTelNumber(soInfoEntity.getTelNumber());
        soDeliveryNoticeEntity.setReceiveAddress(soInfoEntity.getReceiveAddress());
        soDeliveryNoticeEntity.setDeliveryModeDict(soInfoEntity.getDeliveryMode());
        soDeliveryNoticeEntity.setRequireDate(soInfoEntity.getRequireDate());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(soInfoEntity.getWarehouseOrgName());
        soDeliveryNoticeEntity.setCode(code);
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setSourceType(dto.getSourceType());
        if (dto.getPlanDeliveryDate() != null) {
            soDeliveryNoticeEntity.setPlanDeliveryDate(dto.getPlanDeliveryDate());
        }
        soDeliveryNoticeEntity.setTrackNo(dto.getTrackNo());
        if (CharSequenceUtil.isNotBlank(dto.getCarrierId())) {
            //获取采购单供应商信息
            SupplierEntity supplierById = scmTaskFeign.getSupplierById(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierName(supplierById.getName());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        soDeliveryNoticeEntity.setWarehouseOrgId(soInfoEntity.getWarehouseOrgId());
        soDeliveryNoticeEntity.setWarehouseOrgName(soInfoEntity.getWarehouseOrgName());
        //匹配中转规则
        matchTransferRule(soDeliveryNoticeEntity,soInfoEntity);
        this.save(soDeliveryNoticeEntity);
        soDeliveryNoticeDetailService.add(dto, soDeliveryNoticeEntity.getId());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个发货通知单【%s】", code), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), soDeliveryNoticeEntity.getId(), "新增操作");
        //生成装箱任务
        packingTaskService.addPackingByB2BDelivery(soDeliveryNoticeEntity);
        return soDeliveryNoticeEntity.getId();
    }

    private void matchTransferRule(SoDeliveryNoticeEntity soDeliveryNoticeEntity, SoInfoEntity soInfoEntity) {
        soDeliveryNoticeEntity.setTransferWarehouseIds(CharSequenceUtil.EMPTY);
        List<SoInfoDTO.CustomerDTO> customerDTOS = soInfoFeign.listSoCustomer(Collections.singletonList(soDeliveryNoticeEntity.getSourceId()));
        SoInfoDTO.CustomerDTO customerDTO = customerDTOS.stream().filter(v -> v.getCustomerId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new SoInfoDTO.CustomerDTO());
        //是否中转
        CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
        ruleDTO.setType(StockOutTransferTypeEnum.B2B.getCode());
        ruleDTO.setReceiveCountry(customerDTO.getCountryId());
        ruleDTO.setFromWarehouse(soDeliveryNoticeEntity.getWarehouseId());
        ruleDTO.setSalesOrgId(soInfoEntity.getSalesOrgId());
        ruleDTO.setDictPlatform("");
        CfgRuleOutDTO.MatchTransferResultDTO matchTransferResultDTO = cfgRuleOutService.matchTransferRule(ruleDTO);
        if (Objects.nonNull(matchTransferResultDTO) && Objects.nonNull(matchTransferResultDTO.getIsTransit()) && matchTransferResultDTO.getIsTransit()){
            if (CollectionUtils.isNotEmpty(matchTransferResultDTO.getTransferWarehouseIdList())){
                soDeliveryNoticeEntity.setTransferWarehouseIds(String.join(",", matchTransferResultDTO.getTransferWarehouseIdList()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        // 判断是否已生成拣货单
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(dto.getId()));
        if (CollectionUtils.isNotEmpty(views)) {
            throw new ServiceException(ApiError.ERROR_99140);
        }
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(dto.getSourceId());
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = new SoDeliveryNoticeEntity();
        soDeliveryNoticeEntity.setType(soInfoEntity.getOrderType());
        soDeliveryNoticeEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        soDeliveryNoticeEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        soDeliveryNoticeEntity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (CharSequenceUtil.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                soDeliveryNoticeEntity.setSalesDeptName(dept.getName());
            }
        }
        soDeliveryNoticeEntity.setSellerId(soInfoEntity.getSellerId());
        soDeliveryNoticeEntity.setSellerName(soInfoEntity.getSellerName());
        soDeliveryNoticeEntity.setCustomerId(soInfoEntity.getCustomerId());
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soDeliveryNoticeEntity.setCustomerName(customerInfoEntity.getName());
        soDeliveryNoticeEntity.setReceiverName(soInfoEntity.getReceiverName());
        soDeliveryNoticeEntity.setTelNumber(soInfoEntity.getTelNumber());
        soDeliveryNoticeEntity.setReceiveAddress(soInfoEntity.getReceiveAddress());
        soDeliveryNoticeEntity.setDeliveryModeDict(soInfoEntity.getDeliveryMode());
        soDeliveryNoticeEntity.setRequireDate(soInfoEntity.getRequireDate());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(soInfoEntity.getWarehouseOrgName());
        soDeliveryNoticeEntity.setId(dto.getId());
        SoDeliveryNoticeEntity entity = this.getById(dto.getId());
        soDeliveryNoticeEntity.setApproveStatus(entity.getApproveStatus());
        soDeliveryNoticeEntity.setSourceId(dto.getSourceId());
        soDeliveryNoticeEntity.setSourceCode(soInfoEntity.getCode());
        soDeliveryNoticeEntity.setTrackNo(dto.getTrackNo());
        if (CharSequenceUtil.isNotBlank(dto.getCarrierId())) {
            //获取采购单供应商信息
            SupplierEntity supplierById = scmTaskFeign.getSupplierById(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierId(dto.getCarrierId());
            soDeliveryNoticeEntity.setCarrierName(supplierById.getName());
        }
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseId(soInfoEntity.getWarehouseId());
        soDeliveryNoticeEntity.setWarehouseName(warehouseEntity.getName());
        soDeliveryNoticeEntity.setWarehouseOrgId(soInfoEntity.getWarehouseOrgId());
        soDeliveryNoticeEntity.setWarehouseOrgName(soInfoEntity.getWarehouseOrgName());
        if (CollUtil.isNotEmpty(dto.getTransferWarehouseIdList())){
            soDeliveryNoticeEntity.setTransferWarehouseIds(String.join(",",dto.getTransferWarehouseIdList()));
        }else {
            soDeliveryNoticeEntity.setTransferWarehouseIds(CharSequenceUtil.EMPTY);
        }
        boolean flag = this.updateById(soDeliveryNoticeEntity);
        //操作日志
        SoDeliveryNoticeEntity byId = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(byId, soDeliveryNoticeEntity, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), soDeliveryNoticeEntity.getId(), "", "");

        //soDeliveryNoticeDetailService.update(dto);
        return flag;
    }

    @Override
    public SoDeliveryNoticeDTO.View view(String id) {
        SoDeliveryNoticeDTO.View viewDTO = new SoDeliveryNoticeDTO.View();
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = this.getById(id);

        //创库保存详情表的集合
        List<SoDeliveryNoticeDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(id);
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soDeliveryNoticeEntity.getSourceId());
        BeanMapperUtils.copy(soInfoEntity, viewDTO);
        BeanMapperUtils.copy(soDeliveryNoticeEntity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soDeliveryNoticeEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        viewDTO.setCustomerName(customerInfoEntity.getName());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setTypeName(BillTypeEnum.getName(viewDTO.getType()));
        if (viewDTO.getDeliveryStatus()) {
            viewDTO.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
        } else {
            viewDTO.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
        }

        viewDTO.setDeliveryModeDictName(DeliveryModeEnum.getName(soInfoEntity.getDeliveryMode()));
        if (CharSequenceUtil.isNotBlank(soDeliveryNoticeEntity.getTransferWarehouseIds())){
            List<String> split = StrUtil.split(soDeliveryNoticeEntity.getTransferWarehouseIds(), ",");
            viewDTO.setTransferWarehouseIdList(split);
        }
        List<CustomerAddressEntity> customerAddressEntities = customerFeign.listCustomerAddressByIds(Collections.singletonList(soInfoEntity.getReceiveAddressId()));
        CustomerAddressEntity customerAddressEntity = customerAddressEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getReceiveAddressId())).findFirst().orElse(new CustomerAddressEntity());
        viewDTO.setReceiveAddress(customerAddressEntity.getAddress());
        for (SoDeliveryNoticeDetailEntity deliveryNoticeDetailEntity : detailEntityList) {
            SoDeliveryNoticeDetailDTO.View detailView = new SoDeliveryNoticeDetailDTO.View();
            BeanMapperUtils.copy(deliveryNoticeDetailEntity, detailView);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(deliveryNoticeDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(deliveryNoticeDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setSalesQty(soDetailEntity.getQty());

            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(deliveryNoticeDetailEntity.getId()));
            List<String> attachmentUrlList = attachmentList.stream().
                    map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().
                    map(WmsAttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            detailView.setAttachUrlList(attachmentUrlList);
            detailView.setAttachNameList(attachmentNameList);
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        List<SoDeliveryNoticeEntity> soDeliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = soDeliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != soDeliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程
        //操作日志
        List<Pair<String, String>> pairList = soDeliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个发货通知单【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoDeliveryNoticeDTO.Add dto) {
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Collections.singletonList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoDeliveryNoticeDTO.Update dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoDeliveryNoticeEntity entity, String type, String comment, Boolean isNeedProcess) {
        //判断是否是审核中的状态
        if (!Boolean.FALSE.equals(entity.getInvalidStatus()) || !ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException("只有未作废和审核中的数据允许审核");
        }
        PackingTaskEntity taskEntity = packingTaskService.getBySourceCode(entity.getCode());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException("未生成装箱任务，不允许审核");
        }
        //检查出库配置，是否需要状态
        CfgRuleOutDTO.CfgOverweightDetailDTO cfgOverweightDetailDTO = cfgRuleOutService.getCfgOverweightDetailDTOByType(taskEntity.getSourceType());
        if(Objects.nonNull(cfgOverweightDetailDTO) && cfgOverweightDetailDTO.isCheckStatusWhenApprove()){
            if(!(taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()) && taskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode()))){
                throw new ServiceException("{已装箱+全部称重}才能审核通过");
            }
        }

        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            List<SoDeliveryNoticeChangeEntity> soDeliveryNoticeChangeEntities = soDeliveryNoticeChangeService.listNoApproveByNoticeId(entity.getId());
            if(CollectionUtils.isNotEmpty(soDeliveryNoticeChangeEntities)){
                throw new ServiceException("存在未审核的变更单，无法审核通知单");
            }
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(entity.getId());


            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoDeliveryNoticeEntity::getApproveUserId, userInfo.getUid())
                    .set(SoDeliveryNoticeEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoDeliveryNoticeEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoDeliveryNoticeEntity::getId, entity.getId())
                    .update();

            //销售通知明细信息
            List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeDetailService.listDetailByMainIds(Collections.singletonList(entity.getId()));
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_99044);
            }

            //针对拣货单进行库存的多退少补
            handleApproveVirtualInventoryQty(entity);
        } else {
            //审核不通过
            lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoDeliveryNoticeEntity::getId, entity.getId())
                    .update();
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个发货通知单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoDeliveryNoticeEntity entity) {
        //已审核支持反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程

        //下推出库单不能反审核
        List<SoOutstockEntity> soOutstockEntityList = soOutstockService.listBySourceId(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(soOutstockEntityList)) {
            throw new ServiceException(ApiError.ERROR_92004);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoDeliveryNoticeEntity::getId, entity.getId())
                .update();
//        //回滚库存
//        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_DELIVERY_NOTICE, ids);
//        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
//        //删除拣货详情
//        pickingDetailService.deleteBySourceId(ids);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个发货通知单【%s】", entity.getCode()), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】撤销流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "撤销流程操作");

        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单明细");
        }
        //审核不通过 待提交可以作废
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        pickingListsService.exist(ids);

        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(deliveryNoticeEntityList.stream().map(SoDeliveryNoticeEntity::getCode).collect(Collectors.toList()));
        deliveryNoticeEntityList.forEach(v->{
            PackingTaskEntity taskEntity = taskEntityList.stream().filter(t->t.getSourceCode().equals(v.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(taskEntity) && !taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.UNPACKED.getCode())){
                throw new ServiceException("装箱中&已装箱不允许作废");
            }
        });

        //修改状态为待提交
        lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoDeliveryNoticeEntity::getInvalidRemark, remark)
                .in(SoDeliveryNoticeEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = deliveryNoticeEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");

        //发货通知单释放库存
        handleUnLockVirtualInventory(deliveryNoticeEntityList,soDeliveryNoticeDetailList);
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单明细");
        }
        List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(ids);
        if(CollectionUtils.isNotEmpty(machineInfoEntityList)){
            throw new ServiceException("存在关联的加工单{}，B2B发货通知单禁止删除",machineInfoEntityList.stream().map(MachineInfoEntity::getCode).collect(Collectors.toList()));
        }
        pickingListsService.exist(ids);
        //待提交支持删除
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(deliveryNoticeEntityList.stream().map(SoDeliveryNoticeEntity::getCode).collect(Collectors.toList()));
        deliveryNoticeEntityList.forEach(v->{
            PackingTaskEntity taskEntity = taskEntityList.stream().filter(t->t.getSourceCode().equals(v.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(taskEntity) && !taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.UNPACKED.getCode())){
                throw new ServiceException("装箱中&已装箱不允许删除");
            }
        });
        //删除装箱任务
        taskEntityList.forEach(v->packingTaskService.delete(v));
        //删除详情表
        soDeliveryNoticeDetailService.delete(ids);

        boolean flag = this.removeByIds(ids);

        //释放冻结库存
        handleUnLockVirtualInventory(deliveryNoticeEntityList,soDeliveryNoticeDetailList);
        //删除主表
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO deleteEntity(SoDeliveryNoticeEntity entity) {
        List<String> ids = Collections.singletonList(entity.getId());
        
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单明细");
        }
        List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(ids);
        if(CollectionUtils.isNotEmpty(machineInfoEntityList)){
            throw new ServiceException("存在关联的加工单{}，B2B发货通知单禁止删除",machineInfoEntityList.stream().map(MachineInfoEntity::getCode).collect(Collectors.toList()));
        }
        pickingListsService.exist(ids);
        //待提交支持删除
        if (entity.getInvalidStatus() || !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(Collections.singletonList(entity.getCode()));
        PackingTaskEntity taskEntity = taskEntityList.stream().filter(t->t.getSourceCode().equals(entity.getCode())).findFirst().orElse(null);
        if(Objects.nonNull(taskEntity) && !taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.UNPACKED.getCode())){
            throw new ServiceException("装箱中&已装箱不允许删除");
        }
        //删除装箱任务
        taskEntityList.forEach(v->packingTaskService.delete(v));
        //删除详情表
        soDeliveryNoticeDetailService.delete(ids);

        boolean result = this.removeByIds(ids);

        //释放冻结库存
        handleUnLockVirtualInventory(Collections.singletonList(entity), soDeliveryNoticeDetailList);
        
        if (result) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "删除失败");
        }
    }

    @Override
    public Boolean exportExcel(SoDeliveryNoticeDTO.PagingParam dto) {
        downloadTaskFeign.saveDownloadTask("销售发货通知单", EXPORT_WMS_SO_DELIVERY_NOTICE.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
     * 下推销售出库单
     *
     * @param id           id
     * @param deliveryDate
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO generateSoDeliverySave(String id, LocalDate deliveryDate) {
        SoDeliveryNoticeEntity entity = getById(id);
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        //更新发货通知单实际发货日期
        updateDeliveryDate(id, deliveryDate);
        entity.setActualDeliveryDate(Objects.nonNull(deliveryDate) ? deliveryDate : LocalDate.now());
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(entity.getSourceId());
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(soInfoEntity.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_99105);
        }
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySourceId(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_99129);
        }
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> noInventorySkuIds = noInventorySku.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> entityList = soDeliveryNoticeDetailService.listNoInventoryOrPicking(id, noInventorySkuIds);
        boolean allNoInventorySku = Boolean.FALSE;
        if(CollectionUtils.isNotEmpty(entityList)){
            allNoInventorySku = entityList.stream().allMatch(v -> noInventorySkuIds.contains(v.getSkuId()));

            long closeCount = entityList.stream().filter(SoDeliveryNoticeDetailEntity::getIsClose).count();
            if (closeCount > 0) {
                throw new ServiceException(ApiError.ERROR_98068);
            }
        }
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(id));
        if (Boolean.FALSE.equals(allNoInventorySku) && CollectionUtils.isEmpty(views)) {
            throw new ServiceException(ApiError.ERROR_99101, entity.getCode());
        }

        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(entity.getId());
        //包含组合产品的发货通知单，必须有关联的下推的加工组装单且加工单审核通过
        List<String> skuIds = soDeliveryNoticeDetailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<String> existSkuId = bomChildrenList.stream().filter(v->BomTypeEnum.COMBINATION.getType().equals(v.getType())).map(BomChildrenSkuDTO::getParentSkuId).collect(Collectors.toList());
        soDeliveryNoticeDetailEntityList = soDeliveryNoticeDetailEntityList.stream().filter(v->existSkuId.contains(v.getSkuId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(soDeliveryNoticeDetailEntityList)){
            List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(Collections.singletonList(entity.getId()));
            List<MachineInfoEntity> approveMachineInfoEntityList = machineInfoEntityList.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(approveMachineInfoEntityList)){
                throw new ServiceException("存在组合产品，必须有关联的加工组装单且加工单审核通过");
            }
            List<MachineDetailEntity> machineDetailEntityList = machineDetailService.listByMainIds(approveMachineInfoEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            //key 为sourceDetailId, value为qty求和
            Map<String,Integer> alreadyMachineQtyMap = machineDetailEntityList.stream().collect(Collectors.groupingBy(MachineDetailEntity::getSourceDetailId,Collectors.summingInt(MachineDetailEntity::getQty)));
            List<String> errorSkuNoList = new ArrayList<>();
            for (SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity : soDeliveryNoticeDetailEntityList) {
                Integer machineQty = alreadyMachineQtyMap.getOrDefault(soDeliveryNoticeDetailEntity.getId(),0);
                if(soDeliveryNoticeDetailEntity.getPickingQty() > machineQty){
                    errorSkuNoList.add(soDeliveryNoticeDetailEntity.getSkuNo());
                }
            }
            if(CollectionUtils.isNotEmpty(errorSkuNoList)){
                throw new ServiceException("发货通知单{}中存在组合产品，未完全下推加工单或加工单未审核通过{}",entity.getCode(),errorSkuNoList);
            }
        }

        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        SoOutstockDTO.AddDTO addDTO = new SoOutstockDTO.AddDTO();
        String batchNo = "";
        String warehouseId;
        if (CharSequenceUtil.isNotBlank(entity.getTransferWarehouseIds())) {
            List<String> split = StrUtil.split(entity.getTransferWarehouseIds(), ",");
            batchNo = IdUtil.getSnowflake().nextIdStr();
            if (Boolean.FALSE.equals(allNoInventorySku)) {
                generateTransferInfo(batchNo, entity, entityList, warehouseStagingList, noInventorySkuIds, split);
            }
            warehouseId = split.get(split.size() - 1);
        }else {
            warehouseId = entity.getWarehouseId();
        }
        //详情id s
        List<String> detailIds = entityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentDbList = wmsAttachmentService.getByBusinessIds(detailIds);
        //获取到销售退货单 下推列表
        addDTO.buildAddDTO(entity);
        addDTO.setWarehouseId(warehouseId);
        addDTO.setCustomerOrderNo(soInfoEntity.getCustomerOrderNo());
        List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>();
        addDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        for (SoDeliveryNoticeDetailEntity item : entityList) {
            SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
            //附件信息
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentDbList.stream().filter(a -> a.getBusinessId().equals(item.getId())).collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            detail.setAttachNameList(attachmentNameList);
            detail.setSoDetailId(item.getSourceDetailId());
            detail.setSourceDetailId(item.getId());
            detail.setSkuId(item.getSkuId());
            detail.setSkuNo(item.getSkuNo());
            detail.setWarehouseId(warehouseId);
            if (CharSequenceUtil.isNotBlank(entity.getTransferWarehouseIds())) {
                detail.setWarehouseLocation("");
            }else {
                detail.setAttachUrlList(attachmentUrlList);
                // 获取仓库暂存区默认配置
                CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                        .filter(staging -> PickingBillTypeEnum.B2B.getCode().equals(staging.getBillType()))
                        .filter(staging -> staging.getWarehouseId().equals(warehouseId))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
                detail.setWarehouseLocation(pickingStaging.getWarehouseLocation());
            }
            if (noInventorySkuIds.contains(item.getSkuId())) {
                detail.setActualQty(item.getDeliveryQty());
                detail.setPlanQty(item.getDeliveryQty());
            }else {
                detail.setActualQty(item.getPickingQty());
                detail.setPlanQty(item.getPickingQty());
            }
            detail.setAttachNameList(attachmentNameList);
            detail.setAttachUrlList(attachmentUrlList);
            detailList.add(detail);
        }
        addDTO.setBatchNo(batchNo);
        addDTO.setDetailList(detailList);
        //销售出库单保存下推单据
        String outId = soOutstockService.add(addDTO);
        return BatchResultDTO.success(outId, "", "下推成功");
    }

    private void updateDeliveryDate(String id, LocalDate deliveryDate) {
        if (CharSequenceUtil.isBlank(id)){
            return;
        }
        this.lambdaUpdate().eq(SoDeliveryNoticeEntity::getId, id)
                .set(SoDeliveryNoticeEntity::getActualDeliveryDate, Objects.nonNull(deliveryDate) ? deliveryDate : LocalDate.now())
                .update();
    }

    /**
     * 生成调拨单
     *
     * @param batchNo
     * @param entity
     * @param entityList
     * @param warehouseStagingList
     * @param noInventorySkuIds
     * @param transferWarehouseIdList
     * @return
     */
    private void generateTransferInfo(String batchNo, SoDeliveryNoticeEntity entity, List<SoDeliveryNoticeDetailEntity> entityList, List<CfgRulePickingStagingEntity> warehouseStagingList, List<String> noInventorySkuIds, List<String> transferWarehouseIdList) {
        if (CollUtil.isEmpty(transferWarehouseIdList)){
            throw new ServiceException(ApiError.ERROR_92134);
        }
        if (CharSequenceUtil.isBlank(entity.getWarehouseId())){
            throw new ServiceException(ApiError.ERROR_92136, entity.getCode());
        }
        //订单调出仓和第一个中转仓一致时从第二个中转仓开始
        boolean firstWarehouseSame = transferWarehouseIdList.get(0).equals(entity.getWarehouseId());
//        List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(transferWarehouseIdList);
        for (int i = 0; i < transferWarehouseIdList.size(); i++) {
            if (firstWarehouseSame && 0 == i){
                continue;//跳过第一个仓库 从第二个开始
            }
            if (0 == i || firstWarehouseSame){
                addTransferOrder(Boolean.TRUE,entity.getWarehouseId(),transferWarehouseIdList.get(i), entity, entityList,warehouseStagingList, noInventorySkuIds,batchNo, i);
            }else {
                addTransferOrder(Boolean.FALSE,transferWarehouseIdList.get(i - 1),transferWarehouseIdList.get(i), entity, entityList,warehouseStagingList, noInventorySkuIds,batchNo, i);
            }
        }
    }

    /**
     * 生成中转调拨单
     *
     * @param fromWarehouseId
     * @param toWarehouseId
     * @param entity
     * @param detailEntityList
     * @param warehouseStagingList
     * @param noInventorySkuIds
     * @param batchNo
     * @param i
     */
    private void addTransferOrder(Boolean isFirst, String fromWarehouseId, String toWarehouseId, SoDeliveryNoticeEntity entity, List<SoDeliveryNoticeDetailEntity> detailEntityList, List<CfgRulePickingStagingEntity> warehouseStagingList, List<String> noInventorySkuIds, String batchNo, int i) {
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(Arrays.asList(fromWarehouseId, toWarehouseId));
        WarehouseEntity toWarehouse = warehouseEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(toWarehouseId)).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(toWarehouse)) {
            throw new ServiceException(ApiError.ERROR_92263, toWarehouseId);
        }
        WarehouseEntity fromWarehouse = warehouseEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(fromWarehouseId)).findFirst().orElse(null);
        //获取仓库信息
        if (ObjectUtil.isEmpty(fromWarehouse)) {
            throw new ServiceException(ApiError.ERROR_92263, fromWarehouseId);
        }
        TransferInfoDTO.AddDTO transferDto = new TransferInfoDTO.AddDTO();
        transferDto.setType(TransferTypeEnum.CROSS_ORG.getCode());
        transferDto.setBillDate(Objects.nonNull(entity.getActualDeliveryDate()) ? entity.getActualDeliveryDate() : LocalDate.now());
        transferDto.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        transferDto.setInOrgId(toWarehouse.getOrgId());
        transferDto.setOutOrgId(fromWarehouse.getOrgId());
        transferDto.setSourceId(entity.getId());
        transferDto.setSourceCode(entity.getCode());
        transferDto.setIndex(i);
        transferDto.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        transferDto.setBatchNo(batchNo);
        List<TransferInfoDetailDTO.AddDTO> detailList = getAddDTOS(entity, detailEntityList, fromWarehouseId,toWarehouseId, warehouseStagingList, noInventorySkuIds, isFirst);
        transferDto.setDetailList(detailList);
        transferInfoService.addAndApprove(transferDto);
    }

    private static List<TransferInfoDetailDTO.AddDTO> getAddDTOS(SoDeliveryNoticeEntity entity, List<SoDeliveryNoticeDetailEntity> entityList, String fromWarehouseId, String toWarehouseId, List<CfgRulePickingStagingEntity> warehouseStagingList, List<String> noInventorySkuIds, Boolean isFirst) {
            // 获取仓库暂存区默认配置
        CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                .filter(staging -> PickingBillTypeEnum.B2B.getCode().equals(staging.getBillType()))
                .filter(staging -> staging.getWarehouseId().equals(entity.getWarehouseId()))
                .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
        List<TransferInfoDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity view : entityList) {
            TransferInfoDetailDTO.AddDTO transferInfoDetail = new TransferInfoDetailDTO.AddDTO();
            transferInfoDetail.setSkuId(view.getSkuId());
            transferInfoDetail.setSkuNo(view.getSkuNo());
            if (noInventorySkuIds.contains(view.getSkuId())) {
                transferInfoDetail.setQty(view.getDeliveryQty());
            }else {
                transferInfoDetail.setQty(view.getPickingQty());
            }
            if (isFirst){
                transferInfoDetail.setOutWarehouseLocation(pickingStaging.getWarehouseLocation());
            }else {
                transferInfoDetail.setOutWarehouseLocation("");
            }
            transferInfoDetail.setOutWarehouseId(fromWarehouseId);
            transferInfoDetail.setInWarehouseId(toWarehouseId);
            transferInfoDetail.setSourceDetailId(view.getId());
            detailList.add(transferInfoDetail);
        }
        return detailList;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateDeliverySave(List<SoInfoDTO.GenerateDeliveryView> list) {
        List<String> soIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getSoId).distinct().collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getWarehouseId).distinct().collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO paramDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        // 产品属性为费用或服务的sku忽略库存计算
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        for (String soId : soIdList) {
            SoDeliveryNoticeDTO.Add add = new SoDeliveryNoticeDTO.Add();
            add.setSourceId(soId);
            add.setSourceType(SourceTypeEnum.SO_INFO.getCode());
            List<SoInfoDTO.GenerateDeliveryView> viewList = list.stream().filter(req -> req.getSoId().equals(soId)).collect(Collectors.toList());
            List<SoDeliveryNoticeDetailDTO.Add> detailList = new ArrayList<>();
            for (SoInfoDTO.GenerateDeliveryView view : viewList) {
                add.setWarehouseId(view.getWarehouseId());
                add.setPlanDeliveryDate(view.getPlanDeliveryDate());
                SoDeliveryNoticeDetailDTO.Add detailAdd = new SoDeliveryNoticeDetailDTO.Add();
                detailAdd.setDeliveryQty(view.getDeliveryQty());
                detailAdd.setRemark(view.getRemark());
                detailAdd.setSourceDetailId(view.getDetailId());
                detailAdd.setAttachNameList(view.getAttachmentNameList());
                detailAdd.setAttachUrlList(view.getAttachmentUrlList());
                detailList.add(detailAdd);
            }
            add.setDetailList(detailList);
            this.add(add);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoDeliveryNoticeDTO.PagingView> listSoReturnDetailBySourceId(String sourceId) {
        List<SoDeliveryNoticeDTO.PagingView> pagingViews = baseMapper.listSoReturnDetailBySourceId(sourceId);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoDeliveryNoticeDTO.PagingView obj : pagingViews) {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            if (obj.getDeliveryStatus() != null && obj.getDeliveryStatus()) {
                obj.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
            } else {
                obj.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
            }
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setSalesQty(soDetailEntity.getQty());
            obj.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            obj.setCustomerName(customerInfoEntity.getName());
        }
        return pagingViews;
    }


    /**
     * 根据销售 销售订单ids 获取是否有下推的单据
     *
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:27
     */
    @Override
    public Integer getPushDownBySourceIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return 0;
        }
        //发货通知的
        Integer deliveryNoticeCount = this.lambdaQuery().
                in(SoDeliveryNoticeEntity::getSourceId, soIds).
                eq(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.FALSE).
                count();

        Integer soOutStockCount = soOutstockService.getPushDownCountBySoIds(soIds);
        return deliveryNoticeCount + soOutStockCount;
    }

    @Override
    public Map<String,Long> getPushDownDeliveryNoticeCnt(List<String> soIds) {
        if(CollUtil.isEmpty(soIds)) {
            return Maps.newHashMap();
        }
        List<SoDeliveryNoticeEntity> deliveryNoticeList = this.lambdaQuery()
                .in(SoDeliveryNoticeEntity::getSourceId, soIds)
                .eq(SoDeliveryNoticeEntity::getSourceType, SourceTypeEnum.SO_INFO.getCode())
                .eq(SoDeliveryNoticeEntity::getInvalidStatus, Boolean.FALSE).list();

        if(CollUtil.isEmpty(deliveryNoticeList)) {
            return Maps.newHashMap();
        }
        return deliveryNoticeList.stream().collect(Collectors.groupingBy(SoDeliveryNoticeEntity::getSourceId, Collectors.counting()));
    }

    @Override
    public List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> pdaList(SoDeliveryNoticeDTO.PdaSoDeliveryNoticeParam dto) {
        List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> list = baseMapper.pdaList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> dnIds = list.stream().map(SoDeliveryNoticeDTO.PdaSoDeliveryNotice::getId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainIds(dnIds);
        List<String> dndIds = noticeDetailEntities.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(dndIds);
        //获取未全部入库的采购收货详情id
        List<String> receiveDetailIds = new ArrayList<>();
        Map<String, List<SoOutstockDetailEntity>> collect1 = soOutstockDetailEntities.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getSourceDetailId, Collectors.collectingAndThen(Collectors.toList(), m -> {
            int stockInQty = m.stream().mapToInt(SoOutstockDetailEntity::getActualQty).sum();
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(m.get(MathUtil.ZERO).getSourceDetailId())).findFirst().orElse(new SoDeliveryNoticeDetailEntity());
            if (stockInQty < soDeliveryNoticeDetailEntity.getDeliveryQty()) {
                receiveDetailIds.add(m.get(MathUtil.ZERO).getSourceDetailId());
            }
            return m;
        })));

        List<String> collect = soOutstockDetailEntities.stream().map(SoOutstockDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<String> ids = dndIds.stream().filter(e -> !collect.contains(e)).collect(Collectors.toList());
        receiveDetailIds.addAll(ids);

        if (CollectionUtils.isEmpty(receiveDetailIds)) {
            return new ArrayList<>();
        }

        //根据未发货的发货通知单详情id获取未入库收货单id
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listByIds(receiveDetailIds);
        List<String> notAllsoOutstockDetailIds = detailEntityList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        //获取到未发货的发货通知单返回数据
        List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> soDeliveryNoticeList = list.stream().filter(req -> notAllsoOutstockDetailIds.contains(req.getId())).collect(Collectors.toList());
        soDeliveryNoticeList.sort(Comparator.comparing(SoDeliveryNoticeDTO.PdaSoDeliveryNotice::getCode).reversed());
        soDeliveryNoticeList.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return soDeliveryNoticeList;
    }

    /**
     * 根据来源ids 获取数据
     *
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeEntity>
     * @author yl
     * @date 2023-08-30 19:23
     */
    @Override
    public List<SoDeliveryNoticeEntity> listBySourceIdList(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDeliveryNoticeEntity::getSourceId,sourceIds).list();
    }

    /**
     * PDA:根据发货通知单获取详情
     * @Author Luo_WG
     * @Date 2023/9/6 18:10
     * @param id
     * @return java.lang.Boolean
     **/
    @Override
    public List<SoOutstockDTO.GenerateSoOutstockViewDTO> pdaDeliveryDetail(String id) {
        SoDeliveryNoticeEntity entity = this.getById(id);
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        //获取到销售退货单 下推列表
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        List<SoOutstockDTO.GenerateSoOutstockViewDTO> resultList = baseMapper.listGenerateSoOutstockView(Collections.singletonList(id), soDeliveryNotice);
        long closeCount = resultList.stream().filter(s -> s.getIsClose()).count();
        if (closeCount > 0) {
            throw new ServiceException(ApiError.ERROR_98068);
        }

        //详情id s
        List<String> detailIds = resultList.stream().map(SoOutstockDTO.GenerateSoOutstockViewDTO::getSourceDetailId).distinct().collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getWarehouseId()));
        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentDbList = wmsAttachmentService.getByBusinessIds(detailIds);
        List<String> skuIds = resultList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SoOutstockDTO.GenerateSoOutstockViewDTO item : resultList) {
            item.setSourceType(soDeliveryNotice);
            String detailId = item.getSourceDetailId();
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            item.setVariantProperty(skuVO.getVariantProperty());
            item.setDeliveryQty(item.getQty());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(item.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            item.setWarehouseLocationName(warehouseLocationEntity.getName());
            //附件信息
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentDbList.stream().filter(a -> a.getBusinessId().equals(detailId)).collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            item.setAttachNameList(attachmentNameList);
            item.setAttachUrlList(attachmentUrlList);
        }
        //销售出库单保存下推单据
        return resultList;
    }

    @Override
    public SoDeliveryNoticeDTO.View pdaView(String id) {
        SoDeliveryNoticeDTO.View view = this.view(id);
        List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateSoOutstockViewDTOS = pdaDeliveryDetail(id);
        List<SoDeliveryNoticeDetailDTO.View> soDeliveryNoticeDetailDTOS = BeanMapper.copyList(generateSoOutstockViewDTOS, SoDeliveryNoticeDetailDTO.View.class);
        view.setDetailList(soDeliveryNoticeDetailDTOS);
        return view;
    }


    @Override
    public SoDeliveryNoticeEntity getDeliveryNoticeBySourceId(String sourceId) {
        if (StringUtils.isEmpty(sourceId)){
            return null;
        }
        return this.lambdaQuery().eq(SoDeliveryNoticeEntity::getSourceId, sourceId).eq(SoDeliveryNoticeEntity::getIsDeleted, false)
                .last("limit 1").one();
    }

    @Override
    public List<WarehouseLocationMoveDTO.GenPickToSkuMove> generatePickingList(SoDeliveryNoticeDTO.GeneratePickingDTO picking) {
        SoDeliveryNoticeEntity soDeliveryNotice = getById(picking.getId());
        if (ObjectUtil.isEmpty(soDeliveryNotice)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        if (ApproveStatusEnum.APPROVE.getStatus().equals(soDeliveryNotice.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_99160);
        }
        List<SoDeliveryNoticeDetailEntity> details = soDeliveryNoticeDetailService.list(Wrappers.<SoDeliveryNoticeDetailEntity>lambdaQuery()
                .eq(SoDeliveryNoticeDetailEntity::getMainId, picking.getId())
                .in(SoDeliveryNoticeDetailEntity::getId, picking.getDetailIds())
        );
        //判断sku是否被他人生成了拣货单
        boolean checkUnpickedQty = details.stream().allMatch(detail -> (detail.getDeliveryQty() - detail.getPickingQty()) > 0);
        if (Boolean.FALSE.equals(checkUnpickedQty)) {
            throw new ServiceException(ApiError.UNPICKED_QUANTITY_SHORTAGE);
        }
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(soDeliveryNotice.getSourceId());
        PickingListsDTO.AddDTO addDTO = new PickingListsDTO.AddDTO();
        addDTO.setBillType(PickingBillTypeEnum.B2B.getCode());
        addDTO.setCustomerId(soDeliveryNotice.getCustomerId());
        addDTO.setSourceId(soDeliveryNotice.getId());
        CustomerInfoEntity customerInfoEntity = customerFeign.getCustomerById(soDeliveryNotice.getCustomerId());
        if(Objects.nonNull(customerInfoEntity)){
            addDTO.setCountryCode(customerInfoEntity.getCountryId());
        }
        addDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        addDTO.setSourceCode(soDeliveryNotice.getCode());
        List<SoDeliveryNoticeDetailEntity> updateDetails = new ArrayList<>();
        List<PickingDetailDTO.AddDTO> detailList = picking.getDetailIds().stream()
                .map(id -> {
                    SoDeliveryNoticeDetailEntity detailEntity = details.stream().filter(v -> v.getId().equals(id))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货通知单明细"));
                    SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(v -> v.getId().equals(detailEntity.getSourceDetailId()))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单明细"));
                    PickingDetailDTO.AddDTO detail = new PickingDetailDTO.AddDTO(soDeliveryNotice.getWarehouseId(),
                            soDeliveryNotice.getWarehouseName(),
                            detailEntity.getSkuId(),
                            detailEntity.getSkuNo(),
                            detailEntity.getPlatformSkuNo(),
                            detailEntity.getDeliveryQty() - detailEntity.getPickingQty(),
                            detailEntity.getId(),soDetailEntity.getBomVersion()
                    );
                    detailEntity.setPickingQty(detailEntity.getDeliveryQty());
                    updateDetails.add(detailEntity);
                    return detail;
                }).collect(Collectors.toList());
        addDTO.setDetails(detailList);

        // 执行拣货规则
        List<WarehouseLocationMoveDTO.GenPickToSkuMove> moves = requisitionApplicationService.genPickToSkuMove(soDeliveryNotice.getWarehouseId(), soDeliveryNotice.getWarehouseName(), addDTO);
        if(CollectionUtils.isNotEmpty(moves)){
            return moves;
        }
        pickingListsService.add(addDTO);
        soDeliveryNoticeDetailService.updateBatchById(updateDetails);
        Map<String, Integer> qtyMap = updateDetails.stream().collect(Collectors.toMap(BaseEntity::getId, SoDeliveryNoticeDetailEntity::getPickingQty));
        packingTaskService.updateDetailQty(qtyMap);
        return Collections.emptyList();
    }

    @Override
    public PagingVO<SoDeliveryNoticeDTO.PickingViewDTO> generatePickingView(PagingDTO<String> page) {
        //判断是否存在下游单据，已有下游单据就不能再生成拣货单
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySourceId(Collections.singletonList(page.getParams()));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_99110, "销售出库单");
        }
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkus = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        IPage<SoDeliveryNoticeDTO.PickingViewDTO> picking = baseMapper.pagingPicking(new Page<>(page.getCurrPage(), page.getPageSize()), page.getParams(), ignoreInventorySkus);
        return new PagingVO<>(picking);
    }

    @Override
    public void writeBackData(List<String> sourceDetailIds) {
        List<SoDeliveryNoticeDetailEntity> detailEntities = soDeliveryNoticeDetailService.listByIds(sourceDetailIds);
        List<PickingDetailEntity> pickingDetailEntities = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getSourceDetailId, sourceDetailIds));
        SoDeliveryNoticeEntity entity = getById(detailEntities.get(0).getMainId());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(entity.getSourceId());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailBySourceIds(Collections.singletonList(entity.getSourceId()));
        // 回写数量，处理组合数据
        List<String> skuIds = detailEntities.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        Map<String, Integer> skuQty = soDetailEntities.stream().collect(Collectors.toMap(SoDetailEntity::getSkuId, SoDetailEntity::getQty, Math::addExact));
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        for (SoDeliveryNoticeDetailEntity detailEntity : detailEntities) {
            SoDetailEntity soDetailEntity = soDetailEntities.stream()
                    .filter(v -> v.getId().equals(detailEntity.getSourceDetailId()))
                    .findFirst()
                    .orElse(new SoDetailEntity());
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(soDetailEntity.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).findFirst().orElse(null);
            if (!org.springframework.util.ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                Integer qty = pickingDetailEntities.stream()
                        .filter(v -> v.getSourceDetailId().equals(detailEntity.getId()))
                        .filter(v -> v.getSkuId().equals(bomChildrenSkuDTO.getSkuId()))
                        .map(PickingDetailEntity::getQty)
                        .reduce(0, Math::addExact);
                detailEntity.setPickingQty(qty / Optional.ofNullable(bomChildrenSkuDTO.getQuantity()).orElse(1));
            }else {
                Integer qty = pickingDetailEntities.stream()
                        .filter(v -> v.getSourceDetailId().equals(detailEntity.getId()))
                        .filter(v -> v.getSkuId().equals(detailEntity.getSkuId()))
                        .map(PickingDetailEntity::getQty)
                        .reduce(0, Math::addExact);
                detailEntity.setPickingQty(qty);
            }
            if (detailEntity.getDeliveryQty() < detailEntity.getPickingQty()) {
                throw new ServiceException(ApiError.ERROR_99127, detailEntity.getSkuNo());
            }

        }
        // 增加当次拣货数量和
        soDeliveryNoticeDetailService.updateBatchById(detailEntities);
        Map<String, Integer> qtyMap = detailEntities.stream().collect(Collectors.toMap(v->v.getId(),v->v.getPickingQty()));
        packingTaskService.updateDetailQty(qtyMap);
    }

    @Override
    public void updatePackingStatus(String id, String packingStatus) {
        lambdaUpdate().set(SoDeliveryNoticeEntity::getPackingStatus, packingStatus)
                .eq(SoDeliveryNoticeEntity::getId, id)
                .update();
    }

    @Override
    public BatchResultDTO generatePackingTask(SoDeliveryNoticeEntity entity) {
        packingTaskService.addPackingByB2BDelivery(entity);
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"");
    }

    @Override
    public SoDeliveryNoticeEntity getByCode(String code) {
        if (CharSequenceUtil.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(SoDeliveryNoticeEntity::getCode, code).last("limit 1").one();
    }

    @Override
    public List<SoDeliveryNoticeEntity> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDeliveryNoticeEntity::getCode, codes).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO handleErrorData(String id) {
        SoDeliveryNoticeEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            return BatchResultDTO.fail(id, "", "未找到发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeDetailService.listDetailByMainIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(detailList)) {
            return BatchResultDTO.fail(id, entity.getCode(), "未找到发货通知单明细");
        }
        if (entity.getInvalidStatus()) {
            return BatchResultDTO.fail(id, entity.getCode(), "发货通知单已作废");
        }
        if (CharSequenceUtil.isBlank(entity.getVirtualWarehouseId())) {
            return BatchResultDTO.fail(id, entity.getCode(), "发货通知单无虚拟仓");
        }
        //未审核数据
        /**
         * 1、按现有逻辑进行冻结数量转移
         */
        if (!CharSequenceUtil.equals(entity.getApproveStatus(),ApproveStatusEnum.APPROVE.getCode())) {
            //虚拟库存扣减
            soDeliveryNoticeDetailService.handleVirtualInventory(id,detailList);

            lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                    .eq(SoDeliveryNoticeEntity::getId,id)
                    .update();
         return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
        }
        //已审核数据
        /**
         * 0、添加一条可用
         * 1、先根据发货数量减少可用添加冻结
         * 2、根据拣货数量进行多退少补
         * 3、判断是否存在直接调拨单，存在则根据直接调拨单进行出库
         * 4、无直接调拨单则根据销售出库单进行出库
         */

        //默认入库一条可用
        List<VirtualInventoryStockDTO.OutInStockDTO> inUsableList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(detailEntity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(entity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            //发货数量
            outInStockDTO.setQty(detailEntity.getDeliveryQty());
            inUsableList.add(outInStockDTO);
        }
        //默认入库一条可用
        if (CollectionUtils.isNotEmpty(inUsableList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.IN_USABLE.getCode());
            stockParamDTO.setParamList(inUsableList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }


        //减少可用添加冻结
        List<VirtualInventoryStockDTO.OutInStockDTO> addList = new ArrayList<>();
        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(detailEntity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(entity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            //发货数量
            outInStockDTO.setQty(detailEntity.getDeliveryQty());
            addList.add(outInStockDTO);
        }
        //补货
        if (CollectionUtils.isNotEmpty(addList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            stockParamDTO.setParamList(addList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
        //针对拣货单进行库存的多退少补
        handleApproveVirtualInventoryQty(entity);

        //判断是否存在直接调拨单（中转的情况会存在直接调拨单）
        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            //审核通过的直接调拨单
            transferInfoList = transferInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(transferInfoList)) {
                List<String> mainIdList = transferInfoList.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
                List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listByMainIds(mainIdList);
                if (ObjectUtil.isEmpty(transferInfoDetailList)) {
                    throw new ServiceException("未找到直接调拨单明细信息");
                }
                transferInfoService.updateVirtualInventoryTransCore(transferInfoList,transferInfoDetailList);
            }

            lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                    .eq(SoDeliveryNoticeEntity::getId,id)
                    .update();
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
        }

        //根据销售出库单出库
        List<SoOutstockEntity> soOutstockList = soOutstockService.listBySourceId(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(soOutstockList)) {

            //审核通过的直接调拨单
            soOutstockList = soOutstockList.stream().filter(obj -> CharSequenceUtil.equals(obj.getApproveStatus().getCode(),ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());
            //无数据则直接返回
            if (CollectionUtils.isEmpty(soOutstockList)) {
                lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                        .eq(SoDeliveryNoticeEntity::getId,id)
                        .update();
                return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
            }

            List<String> mainIdList = soOutstockList.stream().map(SoOutstockEntity::getId).distinct().collect(Collectors.toList());
            List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(mainIdList);
            if (ObjectUtil.isEmpty(soOutstockDetailList)) {
                throw new ServiceException("未找到销售出库单明细信息");
            }

            //减少可用添加冻结
            List<VirtualInventoryStockDTO.OutInStockDTO> outList = new ArrayList<>();
            for (SoOutstockDetailEntity outstockDetailEntity : soOutstockDetailList) {
                SoOutstockEntity outstockEntity = soOutstockList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), outstockDetailEntity.getMainId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(outstockEntity)) {
                    throw new ServiceException("销售出库单未找到");
                }
                //虚拟仓为空或者中转过来的出库单不生成流水
                if (CharSequenceUtil.isBlank(outstockDetailEntity.getVirtualWarehouseId()) || CharSequenceUtil.isNotBlank(outstockEntity.getBatchNo())) {
                    continue;
                }
                VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSourceId(outstockEntity.getId());
                outInStockDTO.setSourceCode(outstockEntity.getCode());
                outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_OUTSTOCK);
                outInStockDTO.setSourceDetailId(outstockDetailEntity.getId());
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSkuId(outstockDetailEntity.getSkuId());
                outInStockDTO.setSkuNo(outstockDetailEntity.getSkuNo());
                outInStockDTO.setVirtualWarehouseId(outstockDetailEntity.getVirtualWarehouseId());
                //出库数量
                outInStockDTO.setQty(outstockDetailEntity.getActualQty());
                outList.add(outInStockDTO);
            }
            if (CollectionUtils.isNotEmpty(outList)) {
                VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
                stockParamDTO.setParamList(outList);
                stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getCode());
                virtualInventoryTransCoreService.approve(stockParamDTO);
            }
        }
        lambdaUpdate().set(SoDeliveryNoticeEntity::getInvalidRemark,"数据处理")
                .eq(SoDeliveryNoticeEntity::getId,id)
                .update();
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 处理虚拟仓数量
     * @author will
     * @date 2024/7/25 21:38
     * @param entity
     */
    private void handleApproveVirtualInventoryQty (SoDeliveryNoticeEntity entity) {
        //无虚拟仓则不需要库存扣减
        if (CharSequenceUtil.isBlank(entity.getVirtualWarehouseId())) {
            return;
        }

        //发货通知单明细信息
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainId(entity.getId());

        //多退，退回冻结添加可用
        List<VirtualInventoryStockDTO.OutInStockDTO> subList = new ArrayList<>();

        //少补，减少可用添加冻结
        List<VirtualInventoryStockDTO.OutInStockDTO> addList = new ArrayList<>();

        for (SoDeliveryNoticeDetailEntity detailEntity : soDeliveryNoticeDetailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(detailEntity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(entity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            //发货数量
            Integer deliveryQty = detailEntity.getDeliveryQty();
            //拣货数量
            Integer pickingQty = detailEntity.getPickingQty();
            //上次发货数量
            Integer lastPickingQty = detailEntity.getLastPickingQty();
            if (pickingQty > deliveryQty) {
                throw new ServiceException(CharSequenceUtil.format("发货通知单【{}】SKU【{}】拣货数量【{}】不能大于发货数量【{}】",entity.getCode(),detailEntity.getSkuNo(),pickingQty,deliveryQty));
            }
            //数量
            Integer qty = pickingQty - lastPickingQty;
            outInStockDTO.setQty(Math.abs(qty));
            if (MathUtil.compareTo(qty,MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            //本次拣货数量大于上次拣货数量则需要补货
            if (pickingQty > lastPickingQty) {
                addList.add(outInStockDTO);
            } else {
                subList.add(outInStockDTO);
            }
            detailEntity.setLastPickingQty(pickingQty);
        }
        //补货
        if (CollectionUtils.isNotEmpty(addList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            stockParamDTO.setParamList(addList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
        //退货
        if (CollectionUtils.isNotEmpty(subList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_LESS.getCode());
            stockParamDTO.setParamList(subList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }

        //更新上次拣货数量
        soDeliveryNoticeDetailService.updateBatchById(soDeliveryNoticeDetailList);
    }

    /**
     * 处理虚拟库存数据
     * @author will
     * @date 2024/6/14 10:39
     * @param deliveryNoticeEntityList
     * @param detailList
     */
    private void handleUnLockVirtualInventory(List<SoDeliveryNoticeEntity> deliveryNoticeEntityList, List<SoDeliveryNoticeDetailEntity> detailList) {
        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();

        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {

            //发货通知单主表信息
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = deliveryNoticeEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_NOT_EXIST);
            }
            //发货通知单参数
            handleSoDeliveryNoticeParam(detailEntity, soDeliveryNoticeEntity,paramList);
        }
        //发货通知单扣减库存
        if (CollectionUtils.isNotEmpty(paramList)) {
            //扣减可用
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(paramList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE_APPROVE.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
    }

    /**
     * 发货通知单参数
     * @author will
     * @date 2024/7/16 19:58
     * @param detailEntity
     * @param soDeliveryNoticeEntity
     * @return OutInStockDTO
     */
    private void handleSoDeliveryNoticeParam (SoDeliveryNoticeDetailEntity detailEntity,SoDeliveryNoticeEntity soDeliveryNoticeEntity
            ,List<VirtualInventoryStockDTO.OutInStockDTO> paramList) {
        //无虚拟仓不扣库存
        if (CharSequenceUtil.isBlank(soDeliveryNoticeEntity.getVirtualWarehouseId())) {
            return;
        }
        //拣货数据已被删无需退回库存
        if (MathUtil.compareTo(detailEntity.getLastPickingQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return;
        }
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
        outInStockDTO.setSourceId(soDeliveryNoticeEntity.getId());
        outInStockDTO.setSourceCode(soDeliveryNoticeEntity.getCode());
        outInStockDTO.setSourceDetailId(detailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(detailEntity.getSkuId());
        outInStockDTO.setSkuNo(detailEntity.getSkuNo());
        outInStockDTO.setQty(detailEntity.getLastPickingQty());
        outInStockDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
        paramList.add(outInStockDTO);
    }


    @Override
    public PagingVO<SoDeliveryNoticeDTO.PagingView> exportSoDeliveryNotice(PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<SoDeliveryNoticeDTO.PagingView> pagingViews = baseMapper.soDeliveryNoticeExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //获取sku的id集合
        List<String> skuIdList = pagingViews.getRecords().stream().map(SoDeliveryNoticeDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.getRecords().stream().map(SoDeliveryNoticeDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        //中转仓map
        Map<String, String> warehouseMap =  warehouseService.list().stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        for (SoDeliveryNoticeDTO.PagingView pagingView : pagingViews.getRecords()) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            if (pagingView.getDeliveryStatus()) {
                pagingView.setDeliveryStatusName(DeliveryStatusEnum.COMPLETE_SHIPMENT.getName());
            } else {
                pagingView.setDeliveryStatusName(DeliveryStatusEnum.UN_SHIPPED.getName());
            }
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            pagingView.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
            pagingView.setDeliveryStatusName(pagingView.getDeliveryStatus() ? "已发货" : "未发货");
            //中转仓名称
            if (CharSequenceUtil.isNotBlank(pagingView.getTransferWarehouseIds())){
                StringBuilder sb = new StringBuilder();
                List<String> split = CharSequenceUtil.split(pagingView.getTransferWarehouseIds(), ",");
                for (String s : split) {
                    sb.append(warehouseMap.get(s)).append(",");
                }
                pagingView.setTransferWarehouseNames(sb.substring(0, sb.length() - 1));
            }
        }
        return new PagingVO<>(pagingViews);
    }

    @Override
    public BatchResultDTO updateTransferWarehouse(SoDeliveryNoticeEntity entity, List<String> changeIds) {
        //无需校验单据状态，关联的调拨单必须非审核通过、或者无关联的调拨单
        List<TransferInfoEntity> transferInfoEntities = transferInfoService.listBySourceId(entity.getId());
        if (CollectionUtils.isNotEmpty(transferInfoEntities)){
            List<TransferInfoEntity> collect = transferInfoEntities.stream().filter(e -> Objects.nonNull(e) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                List<String> codeList = collect.stream().map(TransferInfoEntity::getCode).distinct().collect(Collectors.toList());
                throw new ServiceException(ApiError.ERROR_92138, String.join(",", codeList));
            }
        }
        String transferWarehouseIdList = "";
        if (CollectionUtils.isNotEmpty(changeIds)){
            transferWarehouseIdList = String.join(",", changeIds);
        }
        this.lambdaUpdate().eq(SoDeliveryNoticeEntity::getId, entity.getId()).set(SoDeliveryNoticeEntity::getTransferWarehouseIds, transferWarehouseIdList).update();
        String msg = "【{}】更新了中转仓配置由【{}】改为【{}】";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, UserContext.getLoginUser().getUserName(),entity.getTransferWarehouseIds(),transferWarehouseIdList), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), entity.getId(), "批量修改中转仓配置");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"修改中转仓配置成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByNoticeChange(List<SoDeliveryNoticeDetailEntity> addList, List<SoDeliveryNoticeDetailEntity> updateList, List<SoDeliveryNoticeDetailEntity> deleteList) {
        // 添加日志
        List<OperateLogDTO.AddModuleOperateLogDTO> logList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(addList)) {
            addList.forEach(v->{
                logList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("发货通知变更单新增明细sku{}",v.getSkuNo()),ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(),v.getMainId(),"新增sku"));
            });
            soDeliveryNoticeDetailService.saveBatch(addList);
        }

        if (CollectionUtils.isNotEmpty(updateList)) {
            updateList.forEach(v->{
                if(v.getSkuNo().equals(v.getChangeBeforeSkuNo())){
                    logList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("发货通知变更单修改明细sku【{}】数量，从{}修改为{}",v.getSkuNo(),v.getChangeBeforeQty(),v.getDeliveryQty()),ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(),v.getMainId(),"修改sku"));
                }else{
                    logList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("发货通知变更单修改明细sku,从【{}】修改为【{}】，数量，从{}修改为{}",v.getSkuNo(),v.getChangeBeforeSkuNo(),v.getChangeBeforeQty(),v.getDeliveryQty()),ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(),v.getMainId(),"修改sku"));
                }
            });
            soDeliveryNoticeDetailService.updateBatchById(updateList);
        }

        if (CollectionUtils.isNotEmpty(deleteList)) {
            deleteList.forEach(v->{
                logList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("删除sku{}",v.getSkuNo()),ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(),v.getMainId(),"删除明细"));
            });
            List<String> deleteIds = deleteList.stream().map(v->v.getId()).collect(Collectors.toList());
            soDeliveryNoticeDetailService.removeByIds(deleteIds);
        }
        if(CollectionUtils.isNotEmpty(logList)){
            operateLogService.batchAddModuleOperateLog(logList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateMachineInfo(List<String> ids) {
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = soDeliveryNoticeDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailEntityList)) {
            throw new ServiceException("未找到发货通知单明细");
        }
        List<String> detailIds = soDeliveryNoticeDetailEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        List<MachineDetailEntity> existDetailList = machineDetailService.listBySourceDetailIds(detailIds);
        //主表信息
        List<String> mainIds = soDeliveryNoticeDetailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getMainId).collect(Collectors.toList());
        List<SoDeliveryNoticeEntity> soDeliveryNoticeEntityList = this.listByIds(mainIds);
        if (CollectionUtils.isEmpty(soDeliveryNoticeEntityList)) {
            throw new ServiceException("未找到发货通知单");
        }
        String codes = soDeliveryNoticeEntityList.stream().filter(obj -> !BillApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(SoDeliveryNoticeEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(codes)) {
            throw new ServiceException("{}未审核完成不允许下推",codes);
        }

        //非组合品不能下推加工单
        List<String> skuIds = soDeliveryNoticeDetailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<CfgRulePickingStagingEntity> warehouseStagingList = FeignQuery.list(CfgRulePickingStagingEntity.class);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Boolean isHasAdd = Boolean.FALSE;
        //生成加工单
        for (SoDeliveryNoticeEntity soDeliveryNoticeEntity : soDeliveryNoticeEntityList) {
            MachineInfoDTO.AddDTO addDTO = new MachineInfoDTO.AddDTO();
            addDTO.setType(MachineTypeEnum.ORDINARY.getCode());
            addDTO.setBillDate(soDeliveryNoticeEntity.getApproveTime().toLocalDate());

            addDTO.setReceiverId(userInfo.getUid());
            addDTO.setWarehouseKeeperId(userInfo.getUid());
            addDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
            addDTO.setWorkType(WorkTypeEnum.ASSEMBLE.getCode());
            addDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
            addDTO.setSourceId(soDeliveryNoticeEntity.getId());
            addDTO.setSourceCode(soDeliveryNoticeEntity.getCode());
            //明细信息
            List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailEntityList.stream().filter(obj -> soDeliveryNoticeEntity.getId().equals(obj.getMainId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                throw new ServiceException("未找到发货通知单明细");
            }
            List<MachineDetailDTO.AddDTO> detailList = new ArrayList<>();
            boolean isAllPush = false;
            for (SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity : detailEntityList) {
                MachineDetailDTO.AddDTO addDetailDTO = new MachineDetailDTO.AddDTO();
                addDetailDTO.setSkuId(soDeliveryNoticeDetailEntity.getSkuId());
                addDetailDTO.setSkuNo(soDeliveryNoticeDetailEntity.getSkuNo());
                // 获取仓库暂存区默认配置
                CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                        .filter(staging -> PickingBillTypeEnum.B2B.getCode().equals(staging.getBillType()))
                        .filter(staging -> staging.getWarehouseId().equals(soDeliveryNoticeEntity.getWarehouseId()))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
                addDetailDTO.setWarehouseLocation(pickingStaging.getWarehouseLocation());
                List<BomChildrenSkuDTO> bomList = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(soDeliveryNoticeDetailEntity.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(obj.getType())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(bomList)) {
                    continue;
                }
                List<MachineDetailEntity> currentExistDetailList = existDetailList.stream().filter(v->v.getSourceDetailId().equals(soDeliveryNoticeDetailEntity.getId())).collect(Collectors.toList());
                int alreadyQty = currentExistDetailList.stream().mapToInt(MachineDetailEntity::getQty).sum();
                if(alreadyQty >= soDeliveryNoticeDetailEntity.getPickingQty()){
                    isAllPush = true;
                    continue;
                }
                addDetailDTO.setQty(soDeliveryNoticeDetailEntity.getPickingQty() - alreadyQty);
                addDetailDTO.setReferenceVersion(bomList.get(0).getBomVersion());
                addDetailDTO.setRefCode(soDeliveryNoticeEntity.getCode());
                addDetailDTO.setRefId(soDeliveryNoticeEntity.getId());
                addDetailDTO.setSourceDetailId(soDeliveryNoticeDetailEntity.getId());
                addDetailDTO.setRefDetailId(soDeliveryNoticeDetailEntity.getId());
                List<MachineSubComponentsDTO.AddDTO> subComponentsList = new ArrayList<>();
                for (BomChildrenSkuDTO bomChildrenSkuDTO : bomList) {
                    MachineSubComponentsDTO.AddDTO subComponentsDTO = new MachineSubComponentsDTO.AddDTO();
                    subComponentsDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                    subComponentsDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    subComponentsDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
                    subComponentsDTO.setQty(addDetailDTO.getQty() * bomChildrenSkuDTO.getQuantity());
                    subComponentsDTO.setWarehouseLocation(pickingStaging.getWarehouseLocation());
                    subComponentsList.add(subComponentsDTO);
                }
                addDetailDTO.setSubComponentsList(subComponentsList);
                detailList.add(addDetailDTO);
            }
            //如果没有明细则跳过无需新增
            if (CollectionUtils.isEmpty(detailList)) {
                if(isAllPush){
                    throw new ServiceException("组合明细已经加工完成");
                }else{
                    throw new ServiceException("未找到需要加工的明细");
                }
            }
            addDTO.setDetailList(detailList);
            machineInfoService.add(addDTO);
            isHasAdd = Boolean.TRUE;
        }
        if (!isHasAdd) {
            throw new ServiceException(ApiError.ERROR_SO_INFO_PUSH_MACHINE_NOT_EXIST_DATA);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<VirtualFlowRefactorDTO.OutInStockDTO> rebuildB2bVirtualFlow() {
        return baseMapper.rebuildB2bVirtualFlow();
    }

    @Override
    public List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> printSkuLabelView(List<String> detailIds) {
        List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> printSkuLabelDTOS = baseMapper.printSkuLabelView(detailIds);
        if (CollUtil.isEmpty(printSkuLabelDTOS)){
            return printSkuLabelDTOS;
        }
        //补充产品名称
        List<String> skuIds = printSkuLabelDTOS.stream().map(SoDeliveryNoticeDTO.PrintSkuLabelDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntities = plmTaskFeign.getByIdList(skuIds);
        //判断sku是否组合品
        //子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        Map<String, String> productNameMap = productDetailEntities.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
        for (SoDeliveryNoticeDTO.PrintSkuLabelDTO printSkuLabelDTO : printSkuLabelDTOS) {
            printSkuLabelDTO.setProductName(productNameMap.get(printSkuLabelDTO.getSkuId()));
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(printSkuLabelDTO.getSkuId()) && BomTypeEnum.COMBINATION.getType().equalsIgnoreCase(req.getType()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                printSkuLabelDTO.setIsCombination(Boolean.TRUE);
                printSkuLabelDTO.setShowDate(Boolean.FALSE);
            } else {
                printSkuLabelDTO.setIsCombination(Boolean.FALSE);
                printSkuLabelDTO.setShowDate(Boolean.TRUE);
            }
        }
        return printSkuLabelDTOS;
    }

    @Override
    public void printSkuLabelConfirm(SoDeliveryNoticeDTO.PrintSkuLabelConfirmDTO dto, HttpServletResponse response) {
        List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> details = dto.getDetailList();
        if(CollUtil.isEmpty(details)){
            throw new ServiceException(ApiError.ERROR_1041,"客户SKU标签");
        }
        if (SkuPrintTypeEnum.BARCODE_INFO.getCode().equals(dto.getSkuPrintType()) && (CharSequenceUtil.isBlank(dto.getCompanyAddress()) || CharSequenceUtil.isBlank(dto.getCompanyName()))){
            throw new ServiceException("公司名称和公司地址不能为空");
        }
        //校验客户sku是否存在空值
        List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> collect = details.stream().filter(v -> StrUtil.isBlank(v.getPlatformSkuNo()) || StrUtil.isBlank(v.getSkuNo())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(collect)){
            throw new ServiceException("SKU和客户SKU不能为空");
        }
        int totalPrintNum = details.stream()
                .mapToInt(detail -> Optional.ofNullable(detail.getPrintNum()).orElse(0))
                .sum();
        if(totalPrintNum > 5000){
            throw new ServiceException("打印数量合计超过10000，建议减少打印数量后在预览页面下载pdf单独打印");
        }
        List<String> base64List = new ArrayList<>();
        generateBase64ByFnskuBill(base64List, dto);
        try {
            String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);
            // 设置响应头，告诉浏览器返回的是一个 PDF 文件
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\""); // 设置 PDF 的显示方式和文件名
            BASE64Decoder decoder = new BASE64Decoder();
            try (OutputStream out = response.getOutputStream()) {
                // 将 Base64 编码的字符串解码为字节数组
                byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
                // 将字节数组写入到响应输出流中
                out.write(pdfBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_CUSTOMER_SKU_PRINT);
        }
    }
    private void generateBase64ByFnskuBill(List<String> base64List, SoDeliveryNoticeDTO.PrintSkuLabelConfirmDTO dto){
        List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> detailList = dto.getDetailList();
        //查询标签链接
        buildSkuLabel(detailList);
        //模板查询
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.CUSTOMER_SKU_LABEL);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        byte[] content = null;
        try {
            content = FastDFSClientUtil.getStorageClient().download_file1(fileTemplateEntity.getUrl());
            InputStream inputStream = new ByteArrayInputStream(content);
            if (inputStream == null) {
                log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LocalDate localDate = LocalDate.now();
        String dateStr = localDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        for (SoDeliveryNoticeDTO.PrintSkuLabelDTO dtoDetail : detailList) {
            Integer printNum = dtoDetail.getPrintNum() == null || dtoDetail.getPrintNum() <= 0 ? 1 : dtoDetail.getPrintNum();
            String labelUrl = dtoDetail.getLabelUrl();
            Boolean showDate = dtoDetail.getShowDate();
            String labelSourceType = dtoDetail.getLabelSourceType();
            String base = "";
            //如果存在客户上传标签，以上传标签为准
            if ((Objects.isNull(showDate) || Boolean.FALSE.equals(showDate) || LabelSourceTypeEnum.CUSTOMER.getCode().equals(labelSourceType)) && CharSequenceUtil.isNotBlank(labelUrl)){
                try {
                    byte[] content2 = FastDFSClientUtil.getStorageClient().download_file1(labelUrl);
                    base = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(content2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (MyException e) {
                    throw new RuntimeException(e);
                }
            }else {
                Map<String, Object> map = new HashMap<>();
                map.put("skuNo", dtoDetail.getSkuNo());
                map.put("platformSkuNo", dtoDetail.getPlatformSkuNo());
                if (Objects.nonNull(dtoDetail.getShowDate()) && dtoDetail.getShowDate()){
                    map.put("dateStr", dateStr);
                }else {
                    map.put("dateStr", "");
                }
                InputStream inputStream = new ByteArrayInputStream(content);
                byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map);
                base = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes);
            }
            for (int i = 1; i <= printNum; i++) {
                base64List.add(base);
            }
        }
        if (SkuPrintTypeEnum.BARCODE_INFO.getCode().equals(dto.getSkuPrintType())){
            List<String> companyData = getCompanyData(dto);
            if (CollUtil.isNotEmpty(companyData)){
                base64List.addAll(companyData);
            }
        }
    }

    private List<String> getCompanyData(SoDeliveryNoticeDTO.PrintSkuLabelConfirmDTO dto) {
        List<String> companyData = new ArrayList<>();
        //模板查询
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.COMPANY_LABEL);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        byte[] content = null;
        try {
            content = FastDFSClientUtil.getStorageClient().download_file1(fileTemplateEntity.getUrl());
            InputStream inputStream = new ByteArrayInputStream(content);
            if (inputStream == null) {
                log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
                throw new ServiceException("公司标签模板不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> detailList = dto.getDetailList();
        String companyAddress = dto.getCompanyAddress();
        String companyName = dto.getCompanyName();
        for (SoDeliveryNoticeDTO.PrintSkuLabelDTO dtoDetail : detailList) {
            Integer printNum = dtoDetail.getPrintNum() == null || dtoDetail.getPrintNum() <= 0 ? 1 : dtoDetail.getPrintNum();
            Map<String, Object> map = new HashMap<>();
            map.put("companyAddress", "生产地址：" + companyAddress);
            map.put("companyName", "生产厂名：" + companyName);
            map.put("productName", "产品名称：" + maskString(dtoDetail.getProductName()));
            InputStream inputStream = new ByteArrayInputStream(content);
            byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map);
            String base = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes);
            for (int i = 1; i <= printNum; i++) {
                companyData.add(base);
            }
        }
        return companyData;
    }

    private String maskString(String input) {
        if (input == null || input.length() <= 23) {
            return input;
        }
        String prefix = input.substring(0, 10);
        String suffix = input.substring(input.length() - 10);
        return prefix + "***" + suffix;
    }

    private void buildLabelData(List<String> base64List, List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> detailList) {
        //模板查询
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.CUSTOMER_SKU_LABEL);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        byte[] content = null;
        try {
            content = FastDFSClientUtil.getStorageClient().download_file1(fileTemplateEntity.getUrl());
            InputStream inputStream = new ByteArrayInputStream(content);
            if (inputStream == null) {
                log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
                throw new ServiceException("客户sku标签模板不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LocalDate localDate = LocalDate.now();
        String dateStr = localDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        byte[] content2 = null;
        for (SoDeliveryNoticeDTO.PrintSkuLabelDTO dtoDetail : detailList) {
            Integer printNum = dtoDetail.getPrintNum() == null || dtoDetail.getPrintNum() <= 0 ? 1 : dtoDetail.getPrintNum();
            String labelUrl = dtoDetail.getLabelUrl();
            Boolean showDate = dtoDetail.getShowDate();
            String base = "";
            if ((Objects.isNull(showDate) || Boolean.FALSE.equals(showDate)) && CharSequenceUtil.isNotBlank(labelUrl)){
                try {
                    content2 = FastDFSClientUtil.getStorageClient().download_file1(fileTemplateEntity.getUrl());
                    base = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(content2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (MyException e) {
                    throw new RuntimeException(e);
                }
            }else {
                Map<String, Object> map = new HashMap<>();
                map.put("skuNo", CharSequenceUtil.isNotBlank(dtoDetail.getSkuNo()) ? dtoDetail.getSkuNo() : "");
                map.put("platformSkuNo", CharSequenceUtil.isNotBlank(dtoDetail.getPlatformSkuNo()) ? dtoDetail.getPlatformSkuNo() : "");
                if (Objects.nonNull(dtoDetail.getShowDate()) && dtoDetail.getShowDate()){
                    map.put("dateStr", dateStr);
                }else {
                    map.put("dateStr", "");
                }
                if (Objects.isNull(content)){
                    try {
                        content = FastDFSClientUtil.getStorageClient().download_file1(fileTemplateEntity.getUrl());
                        InputStream inputStream = new ByteArrayInputStream(content);
                        if (inputStream == null) {
                            log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
                            throw new ServiceException("客户sku标签模板不存在");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                InputStream inputStream = new ByteArrayInputStream(content);
                byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map);
                base = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes);
            }
            for (int i = 1; i <= printNum; i++) {
                base64List.add(base);
            }
        }
    }

    private void buildSkuLabel(List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> detailList) {
        List<String> platformSkuList = detailList.stream().map(SoDeliveryNoticeDTO.PrintSkuLabelDTO::getPlatformSkuNo).distinct().collect(Collectors.toList());
        ListingInfoDTO.QueryDTO queryDTO = new ListingInfoDTO.QueryDTO();
        queryDTO.setPlatformSkuNoList(platformSkuList);
        queryDTO.setType(RuleTypeEnum.CUSTOMER.getCode());
        List<ListingInfoEntity> infoEntityList = omsListingInfoFeign.listInfoByPlatformSkuNo(queryDTO);
        detailList.forEach(e -> {
            ListingInfoEntity listingInfoEntity = infoEntityList.stream().filter(obj -> obj.getPlatformSkuNo().equals(e.getPlatformSkuNo())).findFirst().orElse(null);
            e.setLabelUrl(Objects.nonNull(listingInfoEntity) ? listingInfoEntity.getLabelUrl() : "");
            e.setLabelSourceType(Objects.nonNull(listingInfoEntity)? listingInfoEntity.getLabelSourceType() : "");
        });
    }

    @Override
    public Map<String, SoDeliveryNoticeEntity> mapByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SoDeliveryNoticeEntity> list = this.listByIds(ids);
        return list.stream().collect(Collectors.toMap(SoDeliveryNoticeEntity::getId, Function.identity()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails) {
        // 先验证所有ID是否存在
        List<SoDeliveryNoticeEntity> deliveryNoticeEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliveryNoticeEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainIds(ids);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单明细");
        }
        List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(ids);
        if(CollectionUtils.isNotEmpty(machineInfoEntityList)){
            throw new ServiceException("存在关联的加工单{}，B2B发货通知单禁止删除",machineInfoEntityList.stream().map(MachineInfoEntity::getCode).collect(Collectors.toList()));
        }
        pickingListsService.exist(ids);
        //待提交支持删除
        long count = deliveryNoticeEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();

        if (count != deliveryNoticeEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(deliveryNoticeEntityList.stream().map(SoDeliveryNoticeEntity::getCode).collect(Collectors.toList()));
        deliveryNoticeEntityList.forEach(v->{
            PackingTaskEntity taskEntity = taskEntityList.stream().filter(t->t.getSourceCode().equals(v.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(taskEntity) && !taskEntity.getPackingStatus().equals(PackingTaskStatusEnum.UNPACKED.getCode())){
                throw new ServiceException("装箱中&已装箱不允许删除");
            }
        });
        //删除装箱任务
        taskEntityList.forEach(v->packingTaskService.delete(v));
        //删除详情表
        soDeliveryNoticeDetailService.delete(ids);

        boolean result = this.removeByIds(ids);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }
        //释放冻结库存
        handleUnLockVirtualInventory(deliveryNoticeEntityList,soDeliveryNoticeDetailList);
        
        // 返回成功结果
        return deliveryNoticeEntityList.stream()
                .map(entity -> BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功"))
                .collect(Collectors.toList());
    }
}
