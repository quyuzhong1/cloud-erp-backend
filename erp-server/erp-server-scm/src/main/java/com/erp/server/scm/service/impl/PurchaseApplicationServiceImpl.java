package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.SkuPurchaseDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.scm.enums.PurchaseTableFlagEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.listener.PurchaseApplicationExcelListener;
import com.erp.server.scm.mapper.PurchaseApplicationMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_APPLICATION;

/**
 * <p>
 * 采购申请表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Slf4j
@Service
public class PurchaseApplicationServiceImpl extends SuperServiceImpl<PurchaseApplicationMapper, PurchaseApplicationEntity> implements PurchaseApplicationService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<PurchaseApplicationDTO.ListDTO> paging(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandlePurchaseApplication(records,false);
        return new PagingVO(pageData);
    }

    @Override
    public PurchaseApplicationDTO.PagingTotalDTO pagingTotal(PurchaseApplicationDTO.SearchParamDTO dto) {
        PurchaseApplicationDTO.PagingTotalDTO pagingTotalDTO = baseMapper.pagingTotal(dto);
        return pagingTotalDTO;
    }


    @Override
    public List<ListStatusCountDTO.PurchaseApplicationCountDTO> listCount(PermissionsDTO dto) {
        PurchaseTableFlagEnum[] values = PurchaseTableFlagEnum.values();
        List<ListStatusCountDTO.PurchaseApplicationCountDTO> list = new ArrayList<>();
        for (PurchaseTableFlagEnum item: values) {
            PurchaseApplicationDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            ListStatusCountDTO.PurchaseApplicationCountDTO resultDTO = new ListStatusCountDTO.PurchaseApplicationCountDTO();
            Integer count = MathUtil.ZERO;
            if(PurchaseTableFlagEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseTableFlagEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseTableFlagEnum.TO_BE_CREATE.getCode().equals(item.getCode())) {
                searchParamDTO.setCreatePoTypeList(Arrays.asList(CreatePoTypeEnum.NOT_GENERATED.getStatus(),CreatePoTypeEnum.PARTIAL_GENERATED.getStatus()));
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseTableFlagEnum.CREATED.getCode().equals(item.getCode())) {
                searchParamDTO.setCreatePoTypeList(Arrays.asList(CreatePoTypeEnum.ALL_GENERATED.getStatus()));
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseTableFlagEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(PurchaseApplicationDTO.AddDTO dto) {
        PurchaseApplicationEntity entity = new PurchaseApplicationEntity();
        BeanMapperUtils.copy(dto,entity);
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),entity);
        log.info("采购申请单新增");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PL, BusinessNoTypeEnum.CODE_PL.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_PL);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购申请单【%s】",code), ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"新增操作");
            //新增明细
            purchaseApplicationDetailService.add(dto.getDetails(),entity.getId());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseApplicationDTO.UpdateDTO dto) {
        PurchaseApplicationEntity entity = new PurchaseApplicationEntity();
        BeanMapperUtils.copy(dto,entity);

        List<PurchaseApplicationDetailDTO.UpdateDTO> details = dto.getDetails();

        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),entity);

        log.info("采购申请单修改，id=【{}】", dto.getId());

        //操作日志
        PurchaseApplicationEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old,entity,ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseApplicationDetailService.update(details,entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(PurchaseApplicationEntity entity, String type, String comment, Boolean isNeedProcess) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }

        log.info("采购申请单【{}】，id=【{}】",ApproveTypeEnum.getName(type), entity.getId());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO

            //更新单据状态(后面有流程了可删)
            updateApproveStatusForApprove(entity.getId(),ApproveStatusEnum.APPROVE.getStatus());
        }
        //审核不通过
        if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(entity.getId(),ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购申请单【%s】",ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(PurchaseApplicationEntity entity) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        log.info("采购申请单反审核，id=【{}】", entity.getId());
        //更新单据为待提交
        updateApproveStatusForDisApprove(Collections.singletonList(entity.getId()),ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("反审核了一个采购申请单【%s】", entity.getCode()), ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    public List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrder(List<String> ids) {
        List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> resultList = new ArrayList<>();

        //明细数据
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseApplicationDetailList)) {
            throw new ServiceException(ApiError.ERROR_98015);
        }
        //可以生成采购订单的明细（未生成、部分生成）
        List<PurchaseApplicationDetailEntity> list = purchaseApplicationDetailList.stream().filter(obj -> !CreatePoTypeEnum.ALL_GENERATED.getStatus().equals(obj.getCreatePoType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98015);
        }
        List<String> mainIds = list.stream().map(PurchaseApplicationDetailEntity::getPurchaseApplicationId).collect(Collectors.toList());
        //主表数据
        List<PurchaseApplicationEntity> purchaseApplicationList = this.listByIds(mainIds);
        if (CollectionUtils.isEmpty(purchaseApplicationList)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }

        List<String> detailIds = list.stream().map(PurchaseApplicationDetailEntity::getId).collect(Collectors.toList());
        //查询关联信息
        PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
        searchParamDTO.setPurchaseApplicationDetailIds(detailIds);
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(searchParamDTO);

        //已下推委外订单的数量
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listBySourceDetailIdsWithNoPurchase(detailIds);

        List<String> skuIds = list.stream().map(PurchaseApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        BaseIdsDTO.IdsDTO skuDTO = new BaseIdsDTO.IdsDTO();
        skuDTO.setIds(skuIds);
        Map<String, SkuPurchaseDTO.PurchaseInfo> skuPurchaseMap = plmTaskFeign.getPurchaseInfoBySkuIds(skuDTO);

        for (PurchaseApplicationDetailEntity entity :list) {
            PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO dto = new PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO();
            BeanMapperUtils.copy(entity,dto);
            dto.setId(entity.getPurchaseApplicationId());
            dto.setPurchaseApplicationDetailId(entity.getId());
            //查询主表数据
            PurchaseApplicationEntity purchaseApplicationEntity = purchaseApplicationList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseApplicationId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseApplicationEntity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }
            //必须为审核通过的单据
            if (!ApproveStatusEnum.APPROVE.getStatus().equals(purchaseApplicationEntity.getApproveStatus())) {
                continue;
            }

            dto.setCode(purchaseApplicationEntity.getCode());
            Integer purchaseQty = MathUtil.ZERO;
            Integer subcontractQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(subcontractOrderDetailList)) {
                subcontractQty = subcontractOrderDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(entity.getId()) && StringUtils.isBlank(obj.getParentId())).map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //查询已采购数量
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(obj -> entity.getId().equals(obj.getPurchaseApplicationDetailId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(0, Integer::sum);
            }
            dto.setPurchasedQty(purchaseQty+subcontractQty);

            // 采购员、供应商
            SkuPurchaseDTO.PurchaseInfo skuPurchase = skuPurchaseMap.getOrDefault(entity.getSkuId(), new SkuPurchaseDTO.PurchaseInfo());
            dto.setPurchaseUserId(skuPurchase.getPurchaseUserId());
            dto.setPurchaseUserName(skuPurchase.getPurchaseUserName());
            dto.setSupplierId(skuPurchase.getSupplierId());
            dto.setSupplierName(skuPurchase.getSupplierName());
            //预计交货日期
            dto.setPlanDeliveryDate(entity.getPlanDeliveryDate());
            dto.setDetailRemark(entity.getRemark());
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO  = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
            searchDTO.setSkuId(entity.getSkuId());
            searchDTO.setSkuNo(entity.getSkuNo());
            searchDTO.setSupplierId(skuPurchase.getSupplierId());
            searchDTO.setPurchaseOrgId(entity.getPurchaseOrgId());
            searchDTO.setPurchaseQty(entity.getApplyQty().intValue() - purchaseQty.intValue());
            Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> pair = purchasePriceDetailService.listPurchaseTaxPriceView(searchDTO);
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> value = pair.getValue();
            if (CollectionUtils.isNotEmpty(value)) {
                dto.setTaxPrice(value.get(0).getTaxPrice());
                dto.setTaxRate(value.get(0).getTaxRate());
                dto.setCurrency(value.get(0).getCurrency());
                dto.setCurrencySymbol(value.get(0).getCurrencySymbol());
                dto.setTaxAmount(MathUtil.multiply(value.get(0).getTaxPrice(),searchDTO.getPurchaseQty()));
            }
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generatePurchaseOrder(PurchaseApplicationDTO.ListGeneratePurchaseOrderDTO dto) {
        List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO> list = dto.getList();

        List<String> ids = list.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getId).collect(Collectors.toList());
        //主表数据
        List<PurchaseApplicationEntity> mainList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(mainList)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        //已审核数据才能生成采购单
        long statusCount = mainList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (statusCount > 0) {
            throw new ServiceException(ApiError.ERROR_98033);
        }
        //sku信息
        List<String> skuIds = list.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        log.info("生成采购订单 ids= {}",ids);

        //设置采购订单生成类型
        List<PurchaseApplicationDetailEntity> detailList = setCreatePoType(list, mainList);

        //供应商默认联系人
        List<String> supplierIds = list.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getSupplierId).collect(Collectors.toList());
        List<SupplierContactEntity> defaultSupplierContactList = supplierContactService.getDefaultBySupplierIdList(supplierIds);

        //供应商
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);

        //采购订单新增数据
        List<PurchaseOrderDTO.AddDTO> resultList = new ArrayList<>();

        //主表数据按供应商和采购组织分组
        Map<String, List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSupplierId().concat("|").concat(obj.getPurchaseOrgId()).concat("|").concat(obj.getReceiveOrgId()).concat("|").concat(obj.getDestWarehouseId())));
        for (Map.Entry<String, List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO>> entry : map.entrySet()) {
            List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO> value = entry.getValue();
            //采购订单主表数据
            PurchaseOrderDTO.AddDTO addDTO = new PurchaseOrderDTO.AddDTO();
            PurchaseApplicationEntity entity = mainList.stream().filter(obj -> obj.getId().equals(value.get(0).getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }
            addDTO.setType(PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode());
            addDTO.setPurchaseOrgId(value.get(0).getPurchaseOrgId());
            addDTO.setPurchaseDate(LocalDate.now());
            addDTO.setDeliveryWarehouseId(value.get(0).getDestWarehouseId());
            addDTO.setPurchaseUserId(value.get(0).getPurchaseUserId());
            addDTO.setIsFirstMassProduct(entity.getIsFirstMassProduct());

            //采购订单供应商信息
            PurchaseOrderSupplierDTO.AddDTO supplierDTO = new PurchaseOrderSupplierDTO.AddDTO();
            supplierDTO.setSupplierId(value.get(0).getSupplierId());

            //付款条件
            SupplierEntity supplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(value.get(0).getSupplierId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                supplierDTO.setPaymentCondition(supplierEntity.getPaymentCondition());
                supplierDTO.setPayMethodId(supplierEntity.getPayMethodId());
            }

            //供应商默认联系人
            if (CollectionUtils.isNotEmpty(defaultSupplierContactList)) {
                SupplierContactEntity supplierContactEntity = defaultSupplierContactList.stream().filter(obj -> obj.getSupplierId().equals(value.get(0).getSupplierId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(supplierContactEntity)) {
                    supplierDTO.setSupplierContactId(supplierContactEntity.getId());
                    supplierDTO.setContactTelNumber(supplierContactEntity.getTelNumber());
                }
            }
            addDTO.setPurchaseOrderSupplierDTO(supplierDTO);

            //采购订单明细信息
            List<PurchaseOrderDetailDTO.AddDTO> details = new ArrayList<>();
            for (PurchaseApplicationDTO.GeneratePurchaseOrderDTO generatePurchaseOrderDTO : value) {

                PurchaseOrderDetailDTO.AddDTO addDetailDTO = new PurchaseOrderDetailDTO.AddDTO();
                //采购申请对应明细信息
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(generatePurchaseOrderDTO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
//                if (CollectionUtils.isNotEmpty(sourceDetailList)) {
//                    long count = sourceDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(generatePurchaseOrderDTO.getPurchaseApplicationDetailId())).count();
//                    if (count > 0) {
//                        log.error("采购申请单【{}】明细SKU【{}】已下推委外订单",entity.getCode(), generatePurchaseOrderDTO.getSkuId());
//                        throw new ServiceException(new ApiResult(ApiError.ERROR_98089.code,StrUtil.format(ApiError.ERROR_98089.msg,entity.getCode(),skuVO.getSkuNo())));
//                    }
//                }
                addDetailDTO.setCurrency(generatePurchaseOrderDTO.getCurrency());
                addDetailDTO.setCurrencySymbol(generatePurchaseOrderDTO.getCurrencySymbol());
                addDetailDTO.setPlanDeliveryDate(generatePurchaseOrderDTO.getPlanDeliveryDate());
                addDetailDTO.setSkuId(skuVO.getSkuId());
                addDetailDTO.setSkuNo(skuVO.getSkuNo());
                addDetailDTO.setProductName(skuVO.getSkuName());
                addDetailDTO.setVariantProperty(skuVO.getVariantProperty());
                addDetailDTO.setDeclareModel(skuVO.getDeclareModel());
                addDetailDTO.setDeclareName(skuVO.getDeclareName());
                addDetailDTO.setTaxPrice(generatePurchaseOrderDTO.getTaxPrice());
                //采购数量
                addDetailDTO.setPurchaseQty(generatePurchaseOrderDTO.getPurchaseQty());
                //采购金额
                addDetailDTO.setPurchaseAmount(MathUtil.multiply(addDetailDTO.getTaxPrice(), addDetailDTO.getPurchaseQty()));
                //是否加急
                addDetailDTO.setIsGift(generatePurchaseOrderDTO.getIsGift());
                addDetailDTO.setPurchaseApplicationId(generatePurchaseOrderDTO.getId());
                addDetailDTO.setPurchaseApplicationDetailId(generatePurchaseOrderDTO.getPurchaseApplicationDetailId());
                addDetailDTO.setRemark(generatePurchaseOrderDTO.getDetailRemark());
                details.add(addDetailDTO);
            }
            addDTO.setDetails(details);
            resultList.add(addDTO);
        }
        //新增采购订单
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList.forEach(obj -> purchaseOrderService.add(obj));
        }

        //更新申请明细生成状态
        purchaseApplicationDetailService.saveOrUpdateBatch(detailList);
        return Boolean.TRUE;
    }



    @Override
    public PurchaseApplicationDetailDTO.ImportDTO importFile(MultipartFile excelFile,List<String> skuIds, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        //查询所有审核通过并启用的仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        //查询所有启用核算公司
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        PurchaseApplicationExcelListener excelListenerUtil = new PurchaseApplicationExcelListener(skuList,warehouseList,skuIds,companyList);

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseApplicationImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        PurchaseApplicationDetailDTO.ImportDTO importDTO = new PurchaseApplicationDetailDTO.ImportDTO();
        //导入数据处理
        List<PurchaseApplicationDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购申请错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchaseApplicationImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public Boolean exportExcel(PurchaseApplicationDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购申请单数据", EXPORT_SCM_PURCHASE_APPLICATION.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        log.info("采购申请单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        purchaseApplicationDetailService.removeByPurchaseApplicationIds(ids);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return  this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);
        //待提交并且未作废允许提交
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        log.info("采购申请单提交，ids=【{}】", JSONUtil.toJsonStr(ids));
        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购申请单【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"提交操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchaseApplicationDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }

    @Override
    public PurchaseApplicationDTO.ViewDTO view(String id) {
        PurchaseApplicationDTO.ViewDTO dto = new PurchaseApplicationDTO.ViewDTO();

        //主表信息
        PurchaseApplicationEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        BeanMapperUtils.copy(entity,dto);

        //明细信息
        List<PurchaseApplicationDetailEntity> entityDetails = purchaseApplicationDetailService.listByPurchaseApplicationId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<PurchaseApplicationDetailDTO.ViewDTO> details = BeanMapperUtils.copyList(PurchaseApplicationDetailDTO.ViewDTO.class, entityDetails);
        dto.setDetails(details);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购申请单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购申请单【%s】取消流程", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateAndSubmit(PurchaseApplicationDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO> viewGenerateSubcontractOrder(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO> list = baseMapper.viewGenerateSubcontractOrder(ids);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //未审核完成数据
        List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO> foundList = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(foundList)) {
            String sourceCodes = foundList.stream().map(PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO::getSourceCode).collect(Collectors.joining());
            log.error("单据【{}】未审核完成，不支持下推",sourceCodes);
            throw new ServiceException(new ApiResult(ApiError.ERROR_1039.code, StrUtil.format(ApiError.ERROR_1039.msg,sourceCodes)));
        }

        //已下推信息
        List<String> sourceDetailIds = list.stream().map(PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO::getSourceDetailId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listBySourceDetailIdsWithNoPurchase(sourceDetailIds);

        //查询下推的采购单信息
        PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
        searchParamDTO.setPurchaseApplicationDetailIds(sourceDetailIds);
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(searchParamDTO);

        //查询bom信息填充子件信息
        List<String> skuIds = list.stream().map(PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomChildList)) {
            throw new ServiceException(ApiError.ERROR_98093);
        }
        List<String> childSkuList = bomChildList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        skuIds.addAll(childSkuList);
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuPurchaseByIds(skuIds);

        //供应商信息
        List<String> supplierIdList = skuList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId()))
                .map(SkuVO::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(supplierIdList)) {
            supplierList = supplierService.listByIds(supplierIdList);
        }
        Integer index = MathUtil.ONE;
        List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO> resultList = new ArrayList<>();
        for (PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO viewDTO : list) {
            //已下推数量
            Integer pushdownQty;
            Integer purchaseQty = MathUtil.ZERO;
            Integer subcontractQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(subcontractOrderDetailList)) {
                subcontractQty = subcontractOrderDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(viewDTO.getSourceDetailId()) && StringUtils.isBlank(obj.getParentId()))
                        .map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //查询已采购数量
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(obj -> viewDTO.getSourceDetailId().equals(obj.getPurchaseApplicationDetailId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty)
                        .reduce(0, Integer::sum);
            }
            pushdownQty = purchaseQty+subcontractQty;
            if (MathUtil.compareTo(viewDTO.getQty(),pushdownQty) == MathUtil.ZERO) {
                continue;
            }
            //产品信息
            String supplierId = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSupplierId())).orElse("");
            viewDTO.setSupplierId(supplierId);
            //付款条件
            String paymentCondition = supplierList.stream().filter(obj -> obj.getId().equals(supplierId) && StringUtils.isNotBlank(obj.getPaymentCondition()))
                    .map(SupplierEntity::getPaymentCondition).findFirst().orElse("");
            viewDTO.setPaymentCondition(paymentCondition);

            //可下推数量
            viewDTO.setToPushdownQty(viewDTO.getQty() - pushdownQty);
            viewDTO.setQty(viewDTO.getToPushdownQty());
            viewDTO.setDeliveryQty(viewDTO.getToPushdownQty());
            viewDTO.setSourceType(SourceTypeEnum.PURCHASE_APPLICATION.getCode());
            //报价信息
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceDTO =  new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(viewDTO.getQty(), viewDTO.getSkuId(), viewDTO.getSkuNo(), viewDTO.getSupplierId(),viewDTO.getPurchaseOrgId());
            getTaxPrice(priceDTO,viewDTO,null);
            viewDTO.setIndex(index);
            index++;
            //填充BOM子件信息
            List<BomChildrenSkuDTO> childList = bomChildList.stream().filter(obj -> obj.getParentSkuId().equals(viewDTO.getSkuId()) && StrUtil.equals(obj.getType(),BomTypeEnum.SINGLE.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childList)) {
                throw new ServiceException(ApiError.ERROR_98093);
            }
            List<PurchaseApplicationDTO.ViewChildGenerateSubcontractOrderDTO> generateChildList = new ArrayList<>();
            for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                PurchaseApplicationDTO.ViewChildGenerateSubcontractOrderDTO viewGenerateDTO = new PurchaseApplicationDTO.ViewChildGenerateSubcontractOrderDTO();
                BeanMapperUtils.copy(viewDTO,viewGenerateDTO);

                //产品信息
                String childSupplierId = skuList.stream().filter(obj -> obj.getSkuId().equals(childrenSkuDTO.getSkuId())).findFirst()
                        .flatMap(obj -> Optional.ofNullable(obj.getSupplierId())).orElse("");
                viewGenerateDTO.setSupplierId(childSupplierId);
                viewGenerateDTO.setSkuId(childrenSkuDTO.getSkuId());
                viewGenerateDTO.setSkuNo(childrenSkuDTO.getSkuNo());
                viewGenerateDTO.setProductName(childrenSkuDTO.getSkuName());
                viewGenerateDTO.setQuantity(childrenSkuDTO.getQuantity());
                viewGenerateDTO.setToPushdownQty(null);
                viewGenerateDTO.setQty(viewDTO.getQty() * viewGenerateDTO.getQuantity());
                viewGenerateDTO.setDeliveryQty(viewDTO.getDeliveryQty() * viewGenerateDTO.getQuantity());
                viewGenerateDTO.setPlanDeliveryDate(viewDTO.getPlanDeliveryDate());
                viewGenerateDTO.setPrice(null);
                viewGenerateDTO.setTaxRate(null);
                viewGenerateDTO.setCurrency(null);
                viewGenerateDTO.setCurrencySymbol(null);
                viewGenerateDTO.setAmount(null);
                //报价信息
                PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO childPriceDTO =  new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(viewGenerateDTO.getQty(), viewGenerateDTO.getSkuId(), viewGenerateDTO.getSkuNo(), viewGenerateDTO.getSupplierId(),viewGenerateDTO.getPurchaseOrgId());
                getTaxPrice(childPriceDTO,null,viewGenerateDTO);
                viewGenerateDTO.setIndex(index);
                index++;
                generateChildList.add(viewGenerateDTO);
            }
            viewDTO.setChildList(generateChildList);
            resultList.add(viewDTO);
        }
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.ERROR_98092);
        }
        return resultList;
    }

    @Override
    public void generateSubcontractOrder(ValidList<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> validList) {
        List<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> list = validList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<String> sourceIds = list.stream().map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSourceId).collect(Collectors.toList());
        List<PurchaseApplicationEntity> purchaseApplicationList = this.listByIds(sourceIds);
        String codes = purchaseApplicationList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(PurchaseApplicationEntity::getCode).collect(Collectors.joining());
        if (StringUtils.isNotBlank(codes)) {
            log.error("单据【{}】未审核完成，不支持下推",codes);
            throw new ServiceException(new ApiResult(ApiError.ERROR_1039.code, StrUtil.format(ApiError.ERROR_1039.msg,codes)));
        }
        List<String> sourceDetailIds = list.stream().map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSourceDetailId).collect(Collectors.toList());
        //申请单明细
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(purchaseApplicationDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }



        List<String> skuIds = list.stream().map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //创建人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(userInfo.getUid());
        if (ObjectUtils.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }

        Map<String, List<PurchaseApplicationDTO.GenerateSubcontractOrderDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSourceId().concat(obj.getPurchaseOrgId()).concat(obj.getReceiveOrgId())));
        for (Map.Entry<String, List<PurchaseApplicationDTO.GenerateSubcontractOrderDTO>> entry :  map.entrySet()) {
            List<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> value = entry.getValue();
            PurchaseApplicationDTO.GenerateSubcontractOrderDTO subcontractOrderDTO = entry.getValue().get(0);
            //采购申请单
            PurchaseApplicationEntity purchaseApplicationEntity = purchaseApplicationList.stream().filter(obj -> obj.getId().equals(subcontractOrderDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseApplicationEntity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }

            //委外订单主表数据
            SubcontractOrderDTO.AddDTO addDTO = BeanMapperUtils.map(SubcontractOrderDTO.AddDTO.class, subcontractOrderDTO);
            addDTO.setBillDate(LocalDate.now());
            addDTO.setIsFirstMassProduct(purchaseApplicationEntity.getIsFirstMassProduct());
            addDTO.setSubcontractOrgId(subcontractOrderDTO.getPurchaseOrgId());
            addDTO.setPurchaserId(userInfo.getUid());
            addDTO.setDeptId(findUserDTO.getDepartmentId());

            //委外订单明细数据
            List<SubcontractOrderDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (PurchaseApplicationDTO.GenerateSubcontractOrderDTO generateDetailDTO :  value) {

                //存在采购订单、不存在委外订单的数据不能下推委外订单
//                if (CollectionUtils.isNotEmpty(purchaseApplicationRefPoList) && subCount == 0) {
//                    String skuNo = skuList.stream().filter(obj -> obj.getSkuId().equals(generateDetailDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
//                    long count = purchaseApplicationRefPoList.stream().filter(obj -> obj.getPurchaseApplicationDetailId().equals(generateDetailDTO.getSourceDetailId())).count();
//                    if (count > 0) {
//                        log.error("采购申请单【{}】明细SKU【{}】已下推采购订单",generateDetailDTO.getSourceCode(),generateDetailDTO.getSkuId());
//                        throw new ServiceException(new ApiResult(ApiError.ERROR_98075.code,StrUtil.format(ApiError.ERROR_98075.msg,generateDetailDTO.getSourceCode(),skuNo)));
//                    }
//                }
                //委外订单父级SKU
                SubcontractOrderDetailDTO.AddDTO detail = BeanMapperUtils.map(SubcontractOrderDetailDTO.AddDTO.class, generateDetailDTO);
                PurchaseApplicationDetailEntity purchaseApplicationDetailEntity = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(generateDetailDTO.getSourceDetailId())).findFirst().orElse(new PurchaseApplicationDetailEntity());
                detail.setIsUrgent(purchaseApplicationDetailEntity.getIsUrgent());

                //委外订单子件SKU
                List<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> generateChildList = generateDetailDTO.getChildList();
                List<SubcontractOrderDetailDTO.AddDTO> childList = new ArrayList<>();
                for (PurchaseApplicationDTO.GenerateSubcontractOrderDTO generateChild : generateChildList) {
                    SubcontractOrderDetailDTO.AddDTO child = BeanMapperUtils.map(SubcontractOrderDetailDTO.AddDTO.class, generateChild);
                    child.setSourceDetailId("");
                    childList.add(child);
                }
                detail.setChildList(childList);
                detailList.add(detail);
            }
            addDTO.setDetailList(detailList);
            subcontractOrderService.add(addDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean close(PurchaseApplicationDTO.CloseDTO dto) {
        List<PurchaseApplicationDetailEntity> detailEntityList = purchaseApplicationDetailService.listByIds(dto.getDetailIds());
        if(CollectionUtils.isEmpty(detailEntityList)){
            throw new ServiceException(ApiError.ERROR_98017);
        }
        PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
        searchParamDTO.setPurchaseApplicationDetailIds(dto.getDetailIds());
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(searchParamDTO);
        detailEntityList.forEach(v->{
            Integer purchaseQty = 0;
            //采购数量
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(e -> e.getPurchaseApplicationDetailId().equals(v.getId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //数量
            int waitQty = v.getApplyQty() - purchaseQty;
            if(waitQty == 0 || waitQty >= v.getApplyQty()){
                throw new ServiceException("只有SKU剩余数量小于申请数量，且不为0时，可以提交关闭");
            }
            v.setCloseReason(dto.getCloseReason());
            v.setCreatePoType(CreatePoTypeEnum.CLOSED.getStatus());
            String content = String.format("终止SKU【%s】剩余采购量【%s】的采购",v.getSkuNo(),waitQty);
            moduleOperateLogService.addModuleOperateLog(content,ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),v.getPurchaseApplicationId(),"关闭操作");
        });
        if(!purchaseApplicationDetailService.updateBatchById(detailEntityList)){
            throw new ServiceException("申请单明细更新失败");
        }
        return true;
    }

    @Override
    public PagingVO<PurchaseApplicationDTO.ListDTO> exportPurchaseApplication(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto) {
        Page<PurchaseApplicationDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandlePurchaseApplication(page.getRecords(),true);
        }
        return new PagingVO<>(page);
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids,String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseApplicationEntity::getId,ids)
                .set(PurchaseApplicationEntity::getApproveStatus,approveStatus)
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids,String approveStatus) {

        this.lambdaUpdate().in(PurchaseApplicationEntity::getId,ids)
                .set(PurchaseApplicationEntity::getApproveStatus,approveStatus)
                .set(PurchaseApplicationEntity::getApproveUserId,"")
                .set(PurchaseApplicationEntity::getApproveUserName,"")
                .set(PurchaseApplicationEntity::getApproveTime,null)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(String id,String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().eq(PurchaseApplicationEntity::getId,id)
                .set(PurchaseApplicationEntity::getApproveUserId,userInfo.getUid())
                .set(PurchaseApplicationEntity::getApproveUserName,userInfo.getUserName())
                .set(PurchaseApplicationEntity::getApproveStatus,approveStatus)
                .set(PurchaseApplicationEntity::getApproveTime,LocalDateTime.now())
                .update();
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String applyUserId,String applyDeptId,PurchaseApplicationEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO applyUser = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(applyUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            entity.setApplyUserName(applyUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(applyDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(applyDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setApplyDeptName(depart.getName());
        }
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseApplicationEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseApplicationEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        return list;
    }

    /**
     * @description: 获取报价
     * @author Will
     * @date: 2023/10/24 11:54
     * @param priceDTO
     * @param viewDTO
     * @param viewChildDTO
     */
    private void getTaxPrice (PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceDTO,PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO viewDTO,PurchaseApplicationDTO.ViewChildGenerateSubcontractOrderDTO viewChildDTO) {
        Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> pair = purchasePriceDetailService.listPurchaseTaxPriceView(priceDTO);
        String error = pair.getKey();
        //存在报价信息
        if (StringUtils.isBlank(error) && CollectionUtils.isNotEmpty(pair.getValue())) {
            PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO priceViewDTO = pair.getValue().get(0);
            if (ObjectUtils.isNotEmpty(viewDTO)) {
                viewDTO.setPrice(priceViewDTO.getTaxPrice());
                viewDTO.setTaxRate(priceViewDTO.getTaxRate());
                viewDTO.setCurrency(priceViewDTO.getCurrency());
                viewDTO.setCurrencySymbol(priceViewDTO.getCurrencySymbol());
                viewDTO.setAmount(MathUtil.multiply(viewDTO.getPrice(),viewDTO.getQty()));
            }
            if (ObjectUtils.isNotEmpty(viewChildDTO)) {
                viewChildDTO.setPrice(priceViewDTO.getTaxPrice());
                viewChildDTO.setTaxRate(priceViewDTO.getTaxRate());
                viewChildDTO.setCurrency(priceViewDTO.getCurrency());
                viewChildDTO.setCurrencySymbol(priceViewDTO.getCurrencySymbol());
                viewChildDTO.setAmount(MathUtil.multiply(viewChildDTO.getPrice(),viewChildDTO.getQty()));
            }

        }
    }

    /**
     * @description: 设置采购订单生成类型
     * @author Will
     * @date: 2023/3/30 11:25
     * @param list
     * @param mainList
     * @return List<PurchaseApplicationDetailEntity>
     */
    private List<PurchaseApplicationDetailEntity> setCreatePoType(List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO> list,List<PurchaseApplicationEntity> mainList) {

        List<String> detailIds = list.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getPurchaseApplicationDetailId).distinct().collect(Collectors.toList());

        //查询关联信息
        PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
        searchParamDTO.setPurchaseApplicationDetailIds(detailIds);
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(searchParamDTO);
        //已下推委外订单的数量
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listBySourceDetailIdsWithNoPurchase(detailIds);

        //明细数据
        List<PurchaseApplicationDetailEntity> detailList = purchaseApplicationDetailService.listByIds(detailIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }

        // 不允许下推的申请单明细id集合
        List<PurchaseApplicationDetailEntity> prohibitDetails = Lists.newArrayList();
        Map<String, PurchaseApplicationEntity> detailMainMap = Maps.newHashMap();
        for (PurchaseApplicationDetailEntity detail : detailList) {
            //已采购数量
            Integer purchaseQty = MathUtil.ZERO;
            Integer subcontractQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(subcontractOrderDetailList)) {
                subcontractQty = subcontractOrderDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detail.getId()) && StringUtils.isBlank(obj.getParentId())).map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(obj -> obj.getPurchaseApplicationDetailId().equals(detail.getId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            purchaseQty = purchaseQty+subcontractQty;
            //本次采购数量
            Integer thisPurchaseQty = list.stream().filter(obj -> obj.getPurchaseApplicationDetailId().equals(detail.getId())).map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);

            PurchaseApplicationEntity entity = mainList.stream().filter(obj -> obj.getId().equals(detail.getPurchaseApplicationId())).findFirst().orElse(null);
            detailMainMap.put(detail.getId(), entity);

            //申请数量
            Integer applyQty = detail.getApplyQty();
            if (thisPurchaseQty > (applyQty - purchaseQty)) {
                // 不允许下推
                prohibitDetails.add(detail);
            } else if (thisPurchaseQty == (applyQty - purchaseQty)) {
                detail.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            } else {
                detail.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            }
        }

        if(CollUtil.isNotEmpty(prohibitDetails)) {
            StringBuffer errMsg = new StringBuffer("");
            List<String> prohibitDetailIds = prohibitDetails.stream().map(PurchaseApplicationDetailEntity::getId).distinct().collect(Collectors.toList());
            Map<String, Object> exceptionDataMap = new HashMap(){{put("purchaseApplicationDetailIds", prohibitDetailIds);}};
            prohibitDetails.stream().forEach(detail->{
                PurchaseApplicationEntity entity = detailMainMap.get(detail.getId());
                errMsg.append(StrUtil.format(ApiError.ERROR_99998.msg,entity.getCode(),detail.getSkuNo())).append("</br>");
            });
            throw new ServiceException(new ApiResult(ApiError.ERROR_99998.code,errMsg.toString(), exceptionDataMap));
        }
        return  detailList;
    }

    /**
     * @description: 列表查询数据处理
     * @author Will
     * @date: 2023/4/19 17:57
     * @param records
     */
    private void doOpHandlePurchaseApplication(List<PurchaseApplicationDTO.ListDTO> records,boolean isExport){
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        //查询关联采购
        List<String> detailIds = records.stream().map(PurchaseApplicationDTO.ListDTO::getPurchaseApplicationDetailId).collect(Collectors.toList());
        PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
        searchParamDTO.setPurchaseApplicationDetailIds(detailIds);
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(searchParamDTO);
        //已下推委外订单的数量
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listBySourceDetailIdsWithNoPurchase(detailIds);

        //入库信息
        List<PoInstockDetailEntity> purchaseStockInDetailList = new ArrayList<>();
        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList =  new ArrayList<>();
        if (CollectionUtils.isNotEmpty(refList)) {
            List<String> podIds = refList.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //查询入库
            purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
            //查询收货
            receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        }

        //根据SKU查询BOM判断是否是组合SKU
        List<String> skuIds = records.stream().map(PurchaseApplicationDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);

        for (PurchaseApplicationDTO.ListDTO obj : records){

            //委外数量
            Integer subcontractQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(subcontractOrderDetailList)) {
                subcontractQty = subcontractOrderDetailList.stream().filter(v -> v.getSourceDetailId().equals(obj.getPurchaseApplicationDetailId()) && StringUtils.isBlank(v.getParentId()))
                        .map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //采购数量
            Integer purchaseQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(e -> e.getPurchaseApplicationDetailId().equals(obj.getPurchaseApplicationDetailId()))
                        .map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            obj.setRealPurchaseQty(purchaseQty+subcontractQty);
            //入库数量
            if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                List<String> thisPodIds = refList.stream().filter(e -> e.getPurchaseApplicationDetailId().equals(obj.getPurchaseApplicationDetailId()))
                        .map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(thisPodIds)) {
                    Integer stockInQty = purchaseStockInDetailList.stream().filter(e -> thisPodIds.contains(e.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                            .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setStockInQty(stockInQty);
                }
            }
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                List<String> thisPodIds = refList.stream().filter(e -> e.getPurchaseApplicationDetailId().equals(obj.getPurchaseApplicationDetailId())).
                        map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(thisPodIds)) {
                    Integer receiveQty = receiveDetailList.stream().filter(e -> thisPodIds.contains(e.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                            .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setReceiveQty(receiveQty);
                }
            }

            //是否是组合SKU
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(obj.getSkuId()) && StrUtil.equals(e.getType(), BomTypeEnum.SINGLE.getType())).count();
                if (count > 0) {
                    obj.setIsConstitute(Boolean.TRUE);
                }
            }

            obj.setCreatePoTypeName(CreatePoTypeEnum.getName(obj.getCreatePoType()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            if(obj.getCreatePoType().equals(CreatePoTypeEnum.CLOSED.getStatus())){
                obj.setWaitQty(0);
            }else{
                obj.setWaitQty(obj.getApplyQty() - (Objects.isNull(obj.getRealPurchaseQty())?0:obj.getRealPurchaseQty()));
            }
            if(isExport){
                obj.setIsFirstMassProductStr(obj.getIsFirstMassProduct()?"是":"否");
            }
        }
    }
}
