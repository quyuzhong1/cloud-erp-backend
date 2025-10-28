package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.SkuPurchaseDTO;
import com.erp.model.plm.entity.PilotApplicationDetailEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import com.erp.model.plm.enums.PilotPushPurchaseStatusEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.scm.dto.excel.PurchaseApplicationMainExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.scm.enums.PurchaseTableFlagEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PilotApplicationFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.listener.PurchaseApplicationExcelListener;
import com.erp.server.scm.listener.PurchaseApplicationMainExcelListener;
import com.erp.server.scm.mapper.PurchaseApplicationMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private PurchasePriceService purchasePriceService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PilotApplicationFeign pilotApplicationFeign;
    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    @Lazy
    private PurchaseApplicationService purchaseApplicationService;


    @Override
    public PagingVO<PurchaseApplicationDTO.ListDTO> paging(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page<PurchaseApplicationDTO.SearchParamDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO<>(pageData);
        }
        //数据处理
        doOpHandlePurchaseApplication(records);
        return new PagingVO<>(pageData);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public PurchaseApplicationEntity add(PurchaseApplicationDTO.AddDTO dto) {
        PurchaseApplicationEntity entity = new PurchaseApplicationEntity();
        BeanMapperUtils.copy(dto,entity);
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),entity);
        log.info("采购申请单新增");
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
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseApplicationDTO.UpdateDTO dto) {
        //操作日志
        PurchaseApplicationEntity old = this.getById(dto.getId());
        if (ObjUtil.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        if (SourceTypeEnum.PURCHASE_SUGGESTION_MERGE.getCode().equals(old.getSourceType())) {
            throw new ServiceException("补货建议下推采购申请不支持更新");
        }

        PurchaseApplicationEntity entity = new PurchaseApplicationEntity();
        BeanMapperUtils.copy(dto,entity);

        List<PurchaseApplicationDetailDTO.UpdateDTO> details = dto.getDetails();

        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),entity);

        log.info("采购申请单修改，id=【{}】", dto.getId());


        moduleOperateLogService.addModuleOperateLogByObj(old,entity,ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseApplicationDetailService.update(details,entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(PurchaseApplicationEntity entity, ApproveOneDTO dto) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }

        log.info("采购申请订单【{}】，ids=【{}】", ApproveTypeEnum.getName(dto.getType()), JSONUtil.toJsonStr(entity.getId()));
        //调用审核流程
        approveProcess(entity, dto);
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购申请单【%s】",ApproveTypeEnum.getName(dto.getType()),entity.getCode()).concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }


    /**
     * @param entity
     * @param dto
     * @description: 结束深审核
     * @author Will
     * @date: 2023/7/11 14:22
     */
    private void approveProcess(PurchaseApplicationEntity entity, ApproveOneDTO dto) {
        /**
         * 目前代码里面批量审核的都是内部审核，不走流程，赋值可取第一条
         */
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PURCHASE_APPLICATION.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(ApproveOneDTO dto, PurchaseApplicationEntity entity) {
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        Boolean result = this.updateApproveStatusForApprove(entity, approveStatus);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        return Boolean.TRUE;
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
        //价目查询
        List<PurchasePriceDTO.PriceDTO> priceList = new ArrayList<>();
        list.forEach(e -> {
            Integer purchaseQty = MathUtil.ZERO;
            // 采购员、供应商
            SkuPurchaseDTO.PurchaseInfo skuPurchase = skuPurchaseMap.getOrDefault(e.getSkuId(), new SkuPurchaseDTO.PurchaseInfo());
            //查询已采购数量
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(obj -> e.getId().equals(obj.getPurchaseApplicationDetailId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(0, Integer::sum);
            }
            Integer applyQty = Objects.nonNull(e.getApplyQty()) ? e.getApplyQty() : MathUtil.ZERO;
            priceList.add(PurchasePriceDTO.PriceDTO.builder()
                            .purchaseOrgId(e.getPurchaseOrgId())
                            .qty(applyQty - purchaseQty)
                            .skuId(e.getSkuId())
                            .supplierId(skuPurchase.getSupplierId())
                    .build());
        });
        List<PurchasePriceDTO.PriceDTO> viewDTOList = purchasePriceService.batchGetPurchasePrice(priceList);
        for (PurchaseApplicationDetailEntity entity :list) {
            PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO dto = new PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO();
            BeanMapperUtils.copy(entity,dto);
            dto.setId(entity.getPurchaseApplicationId());
            dto.setPurchaseApplicationDetailId(entity.getId());
            //查询主表数据
            PurchaseApplicationEntity purchaseApplicationEntity = purchaseApplicationList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseApplicationId())).findFirst().orElse(null);
            if (org.springframework.util.ObjectUtils.isEmpty(purchaseApplicationEntity)) {
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
            dto.setFirstMassProduct(entity.getFirstMassProduct());
            dto.setFirstMassProductName(FirstMassProductTypeEnum.getName(entity.getFirstMassProduct()));
            dto.setSupplierId(skuPurchase.getSupplierId());
            dto.setSupplierName(skuPurchase.getSupplierName());
            //预计交货日期
            dto.setPlanDeliveryDate(entity.getPlanDeliveryDate());
            dto.setDetailRemark(entity.getRemark());
            int qty = entity.getApplyQty().intValue() - purchaseQty.intValue();
            //采购单价赋值
            PurchasePriceDTO.PriceDTO viewDTO = viewDTOList.stream().filter(obj ->
                            obj.getSkuId().equals(entity.getSkuId())
                            && obj.getSupplierId().equals(dto.getSupplierId())
                            && CharSequenceUtil.equals(obj.getPurchaseOrgId(),entity.getPurchaseOrgId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(viewDTO)){
                dto.setTaxPrice(viewDTO.getTaxPrice());
                dto.setTaxRate(viewDTO.getTaxRate());
                dto.setCurrency(viewDTO.getCurrency());
                dto.setCurrencySymbol(viewDTO.getCurrencySymbol());
                dto.setTaxAmount(MathUtil.multiplyWithTwo(viewDTO.getTaxPrice(),qty).setScale(4, RoundingMode.DOWN));
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
        List<SupplierAccountEntity> defaultSupplierAccountList = supplierAccountService.getDefaultBySupplierIdList(supplierIds);

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
            if (org.springframework.util.ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }
            addDTO.setType(PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode());
            addDTO.setPurchaseOrgId(value.get(0).getPurchaseOrgId());
            addDTO.setPurchaseDate(LocalDate.now());
            addDTO.setDeliveryWarehouseId(value.get(0).getDestWarehouseId());
            addDTO.setPurchaseUserId(value.get(0).getPurchaseUserId());

            //采购订单供应商信息
            PurchaseOrderSupplierDTO.AddDTO supplierDTO = new PurchaseOrderSupplierDTO.AddDTO();
            supplierDTO.setSupplierId(value.get(0).getSupplierId());

            //付款条件
            SupplierEntity supplierEntity = supplierList.stream().filter(obj -> obj.getId().equals(value.get(0).getSupplierId())).findFirst().orElse(null);
            if (!org.springframework.util.ObjectUtils.isEmpty(supplierEntity)) {
                supplierDTO.setPaymentCondition(supplierEntity.getPaymentCondition());
                supplierDTO.setPayMethodId(supplierEntity.getPayMethodId());
            }

            //供应商默认联系人
            if (CollectionUtils.isNotEmpty(defaultSupplierContactList)) {
                SupplierContactEntity supplierContactEntity = defaultSupplierContactList.stream().filter(obj -> obj.getSupplierId().equals(value.get(0).getSupplierId())).findFirst().orElse(null);
                if (!org.springframework.util.ObjectUtils.isEmpty(supplierContactEntity)) {
                    supplierDTO.setSupplierContactId(supplierContactEntity.getId());
                    supplierDTO.setContactTelNumber(supplierContactEntity.getTelNumber());
                }
            }
            //供应商默认账户
            if (CollectionUtils.isNotEmpty(defaultSupplierAccountList)) {
                SupplierAccountEntity supplierAccountEntity = defaultSupplierAccountList.stream().filter(obj -> obj.getSupplierId().equals(value.get(0).getSupplierId())).findFirst().orElse(null);
                if (Objects.nonNull(supplierAccountEntity)) {
                    supplierDTO.setSupplierAccountId(supplierAccountEntity.getId());
                }
            }
            addDTO.setPurchaseOrderSupplierDTO(supplierDTO);

            //采购订单明细信息
            List<PurchaseOrderDetailDTO.AddDTO> details = new ArrayList<>();
            for (PurchaseApplicationDTO.GeneratePurchaseOrderDTO generatePurchaseOrderDTO : value) {

                PurchaseOrderDetailDTO.AddDTO addDetailDTO = new PurchaseOrderDetailDTO.AddDTO();
                //采购申请对应明细信息
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(generatePurchaseOrderDTO.getSkuId())).findFirst().orElse(null);
                if (org.springframework.util.ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
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
                addDetailDTO.setPurchaseAmount(MathUtil.multiplyWithTwo(addDetailDTO.getTaxPrice(), addDetailDTO.getPurchaseQty()));
                //是否加急
                addDetailDTO.setIsGift(generatePurchaseOrderDTO.getIsGift());
                addDetailDTO.setPurchaseApplicationId(generatePurchaseOrderDTO.getId());
                addDetailDTO.setPurchaseApplicationDetailId(generatePurchaseOrderDTO.getPurchaseApplicationDetailId());
                addDetailDTO.setRemark(generatePurchaseOrderDTO.getDetailRemark());
                addDetailDTO.setFirstMassProduct(generatePurchaseOrderDTO.getFirstMassProduct());
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
        PurchaseApplicationExcelListener excelListenerUtil = new PurchaseApplicationExcelListener(skuList,wmsTaskFeign,skuIds,companyList);

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), PurchaseApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
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
    public BatchResultDTO delete(PurchaseApplicationEntity entity) {
        //待提交允许删除
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        log.info("采购申请单删除，ids=【{}】", JSONUtil.toJsonStr(ids));

        //采购申请单明细数据
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.lambdaQuery().in(PurchaseApplicationDetailEntity::getPurchaseApplicationId,ids).list();
        Map<String, Integer> collect = purchaseApplicationDetailList.stream()
                .collect(Collectors.groupingBy(PurchaseApplicationDetailEntity::getSourceDetailId, Collectors.summingInt(PurchaseApplicationDetailEntity::getApplyQty)));
        List<String> sourceDetailIds = purchaseApplicationDetailList.stream().filter(v -> StringUtils.isNotBlank(v.getSourceDetailId())).map(PurchaseApplicationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if(sourceDetailIds.size() > 0){
            List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> purchaseSkuQtyList = purchaseApplicationDetailService.listSkuAndQty(null,sourceDetailIds);
            if(CollectionUtils.isNotEmpty(purchaseSkuQtyList)){
                Map<String, Integer> purchaseSkuQtyMap = purchaseSkuQtyList.stream().collect(Collectors.toMap(item -> item.getSourceDetailId(), item2 -> item2.getQty()));
                Map<String,String> map = new HashedMap();
                for (Map.Entry<String, Integer> entry : purchaseSkuQtyMap.entrySet()) {
                    String sourceDetailId = entry.getKey();
                    //已申请量
                    int qty = entry.getValue() == null ? 0 : entry.getValue();
                    //本次删除的申请量
                    int deleteQty = collect.get(sourceDetailId) == null ? 0 : collect.get(sourceDetailId);
                    if(deleteQty>0){
                        String status = (qty - deleteQty) > 0 ? PilotPushPurchaseStatusEnum.PART_ORDER.getCode() : PilotPushPurchaseStatusEnum.NOT_ORDER.getCode();
                        map.put(sourceDetailId,status);
                    }
                }
                //更新试产量产明细表订单状态
                if(map.size()>0){
                    pilotApplicationFeign.updateDetailByPilotApplicationDetailIds(map);
                }
            }
        }
        //删除明细数据
        purchaseApplicationDetailService.removeByPurchaseApplicationIds(ids);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);
        //删除主表数据
        boolean result = this.removeByIds(ids);
        if (result){
            return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id) {
        //根据ids查询
        PurchaseApplicationEntity entity = getById(id);
        return this.submitEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO addAndSubmit(PurchaseApplicationDTO.AddDTO dto) {
        //新增
        PurchaseApplicationEntity entity = this.add(dto);
        if (StringUtils.isBlank(entity.getId())) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        entity = this.getById(entity.getId());
        //提交
        return this.submitEntity(entity);
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
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto,PurchaseApplicationEntity entity) {

        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        log.info("采购申请单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setExecuteSystem(dto.getExecuteSystem());
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.PURCHASE_APPLICATION.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购申请单【%s】取消流程", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"取消流程操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseApplicationDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        BatchResultDTO resultDTO = this.submit(dto.getId());
        return resultDTO.getSuccess();
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
            throw new ServiceException(new ApiResult<>(ApiError.ERROR_1039.code, CharSequenceUtil.format(ApiError.ERROR_1039.msg,sourceCodes)));
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
        //价目查询
        List<PurchasePriceDTO.PriceDTO> priceList = new ValidList<>();
        list.forEach(e -> {
            //产品信息
            String supplierId = skuList.stream().filter(obj -> obj.getSkuId().equals(e.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSupplierId())).orElse("");
            priceList.add(PurchasePriceDTO.PriceDTO.builder()
                    .purchaseOrgId(e.getPurchaseOrgId())
                    .qty(e.getQty())
                    .skuId(e.getSkuId())
                    .supplierId(supplierId)
                    .build());
        });
        List<PurchasePriceDTO.PriceDTO> viewDTOList = purchasePriceService.batchGetPurchasePrice(priceList);
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
            viewDTO.setFirstMassProductName(FirstMassProductTypeEnum.getName(viewDTO.getFirstMassProduct()));
            //报价信息
            PurchasePriceDTO.PriceDTO priceDTO = viewDTOList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())
                            && obj.getSupplierId().equals(supplierId)
                            && CharSequenceUtil.equals(obj.getPurchaseOrgId(),viewDTO.getPurchaseOrgId()))
                    .findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(priceDTO)) {
                viewDTO.setPrice(priceDTO.getTaxPrice());
                viewDTO.setTaxRate(priceDTO.getTaxRate());
                viewDTO.setCurrency(priceDTO.getCurrency());
                viewDTO.setCurrencySymbol(priceDTO.getCurrencySymbol());
                viewDTO.setAmount(MathUtil.multiplyWithTwo(viewDTO.getPrice(),viewDTO.getQty()));
            }
            viewDTO.setIndex(index);
            index++;
            //填充BOM子件信息
            List<BomChildrenSkuDTO> childList = bomChildList.stream().filter(obj -> obj.getParentSkuId().equals(viewDTO.getSkuId()) && CharSequenceUtil.equals(obj.getType(),BomTypeEnum.SINGLE.getType())).collect(Collectors.toList());
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
                viewGenerateDTO.setFirstMassProduct(viewDTO.getFirstMassProduct());
                viewGenerateDTO.setFirstMassProductName(FirstMassProductTypeEnum.getName(viewDTO.getFirstMassProduct()));
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
        //获取采购单价
        List<PurchasePriceDTO.PriceDTO> childPriceList = new ValidList<>();
        resultList.forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getChildList())){
                e.getChildList().forEach(f ->{
                    childPriceList.add(PurchasePriceDTO.PriceDTO.builder()
                            .purchaseOrgId(f.getPurchaseOrgId())
                            .qty(f.getQty())
                            .skuId(f.getSkuId())
                            .supplierId(f.getSupplierId())
                            .build());
                });
            }
        });
        List<PurchasePriceDTO.PriceDTO> childViewDTOList = purchasePriceService.batchGetPurchasePrice(childPriceList);
        for (PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO orderDTO : resultList){
            if (CollectionUtils.isNotEmpty(orderDTO.getChildList())){
                for (PurchaseApplicationDTO.ViewChildGenerateSubcontractOrderDTO viewGenerateDTO : orderDTO.getChildList()){
                    //报价信息
                    PurchasePriceDTO.PriceDTO priceDTO2 = childViewDTOList.stream().filter(obj -> Objects.nonNull(obj) && obj.getSkuId().equals(viewGenerateDTO.getSkuId())
                                    && obj.getSupplierId().equals(viewGenerateDTO.getSupplierId())
                                    && CharSequenceUtil.equals(obj.getPurchaseOrgId(),viewGenerateDTO.getPurchaseOrgId()))
                            .findFirst().orElse(null);
                    if (!org.springframework.util.ObjectUtils.isEmpty(priceDTO2)) {
                        viewGenerateDTO.setPrice(priceDTO2.getTaxPrice());
                        viewGenerateDTO.setTaxRate(priceDTO2.getTaxRate());
                        viewGenerateDTO.setCurrency(priceDTO2.getCurrency());
                        viewGenerateDTO.setCurrencySymbol(priceDTO2.getCurrencySymbol());
                        viewGenerateDTO.setAmount(MathUtil.multiplyWithTwo(viewGenerateDTO.getPrice(),viewGenerateDTO.getQty()));
                    }
                }
            }
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
            throw new ServiceException(new ApiResult<>(ApiError.ERROR_1039.code, CharSequenceUtil.format(ApiError.ERROR_1039.msg,codes)));
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
            if (org.springframework.util.ObjectUtils.isEmpty(purchaseApplicationEntity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }

            //委外订单主表数据
            SubcontractOrderDTO.AddDTO addDTO = BeanMapperUtils.map(SubcontractOrderDTO.AddDTO.class, subcontractOrderDTO);
            addDTO.setBillDate(LocalDate.now());
            addDTO.setSubcontractOrgId(subcontractOrderDTO.getPurchaseOrgId());
            addDTO.setPurchaserId(userInfo.getUid());
            addDTO.setDeptId(findUserDTO.getDepartmentId());

            //委外订单明细数据
            List<SubcontractOrderDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (PurchaseApplicationDTO.GenerateSubcontractOrderDTO generateDetailDTO :  value) {
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
            if(waitQty == 0 || waitQty > v.getApplyQty()){
                throw new ServiceException("只有SKU剩余数量小于等于申请数量，且不为0时，可以提交关闭");
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
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<PurchaseApplicationDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandlePurchaseApplication(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public ApiResult<PurchaseApplicationDTO.SubcontractPurchasePriceDTO> batchGetSubcontractPurchasePrice(ValidList<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> list) {
        PurchaseApplicationDTO.SubcontractPurchasePriceDTO subcontractPurchasePriceDTO = new PurchaseApplicationDTO.SubcontractPurchasePriceDTO();
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();

        if (CollectionUtils.isEmpty(list)){
            throw new ServiceException("下推列表不能为空");
        }
        //采购组织ids
        List<String> purchaseOrgIdList = list.stream().map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getChildList).flatMap(Collection::stream).map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getPurchaseOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(purchaseOrgIdList);
        if (CollectionUtils.isEmpty(companyList)){
            throw new ServiceException("使用组织不存在");
        }
        //skuId
        List<String> skuIdList = list.stream().map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getChildList).flatMap(Collection::stream).map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)){
            throw new ServiceException("sku不存在");
        }
        //根据sku查询是否是组合品
        List<BomChildrenSkuDTO> skuDTOList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        //供应商Id
        List<String> supplierIdList = list.stream().map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getChildList).flatMap(Collection::stream).map(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSupplierId).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntityList = supplierService.listByIds(supplierIdList);
        if (CollectionUtils.isEmpty(supplierEntityList)){
            throw new ServiceException("供应商不存在");
        }
        //采购数量-需要根据sku进行汇总
        List<Integer> purchaseQtyList = new ArrayList<>();
        for (PurchaseApplicationDTO.GenerateSubcontractOrderDTO dto : list){
            Map<String, Integer> skuQtyList = dto.getChildList().stream().collect(Collectors.groupingBy(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSkuId, Collectors.summingInt(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getQty)));
            List<Integer> qtyList = skuQtyList.values().stream().distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(purchaseQtyList) && CollectionUtils.isNotEmpty(qtyList)){
                purchaseQtyList = qtyList;
            }else if (CollectionUtils.isNotEmpty(purchaseQtyList) && CollectionUtils.isNotEmpty(qtyList)){
                purchaseQtyList = Stream.concat(purchaseQtyList.stream(),qtyList.stream()).distinct().collect(Collectors.toList());
            }
        }
        //查询对应采购价目
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> viewList = purchasePriceDetailService.batchGetTaxPrice(skuIdList,supplierIdList,purchaseQtyList,purchaseOrgIdList);
        if (CollectionUtils.isEmpty(viewList)){
            //未查到结果，直接返回
            throw new ServiceException("采购单价信息查询结果为空");
        }
        for (PurchaseApplicationDTO.GenerateSubcontractOrderDTO dto : list){
            //计算子件采购数量
            Map<String, Integer> skuQtyList = dto.getChildList().stream().collect(Collectors.groupingBy(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getSkuId, Collectors.summingInt(PurchaseApplicationDTO.GenerateSubcontractOrderDTO::getQty)));
            for (PurchaseApplicationDTO.GenerateSubcontractOrderDTO updateDTO : dto.getChildList()){
                List<BomChildrenSkuDTO> bomChildrenSkuDTOList = skuDTOList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getParentSkuId()) && Objects.equals(e.getParentSkuId(), updateDTO.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOList)){
                    //sku是组合品时，不计算采购单价
                    BigDecimal price = Objects.nonNull(updateDTO.getPrice()) ? updateDTO.getPrice():BigDecimal.ZERO;
                    Integer qty = Objects.nonNull(updateDTO.getQty()) ? updateDTO.getQty() : MathUtil.ZERO;
                    updateDTO.setAmount(MathUtil.multiplyWithTwo(price,qty));
                    continue;
                }
                //获取sku汇总数量
                Integer purchaseQty = skuQtyList.getOrDefault(updateDTO.getSkuId(), MathUtil.ZERO);
                PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = viewList.stream().filter(obj -> obj.getSkuId().equals(updateDTO.getSkuId())
                                && obj.getSupplierId().equals(updateDTO.getSupplierId())
                                && CharSequenceUtil.equals(obj.getPurchaseOrgId(),updateDTO.getPurchaseOrgId())
                                && (purchaseQty >= obj.getMinQty() && obj.getMaxQty() > purchaseQty))
                        .findFirst().orElse(null);
                if (Objects.nonNull(viewDTO)){
                    updateDTO.setPrice(viewDTO.getTaxPrice());
                    updateDTO.setAmount(MathUtil.multiplyWithTwo(viewDTO.getTaxPrice(),updateDTO.getQty()));
                }else {
                    SkuVO skuVO = skuVOList.stream().filter(f -> f.getSkuId().equals(updateDTO.getSkuId())).findFirst().orElse(null);
                    if (Objects.isNull(skuVO)){
                        batchResultDTOList.add(BatchResultDTO.fail(updateDTO.getSkuId(), "", "sku基础信息未找到"));
                    }
                    SupplierEntity supplierEntity = supplierEntityList.stream().filter(f -> f.getId().equals(updateDTO.getSupplierId())).findFirst().orElse(null);
                    if (Objects.isNull(supplierEntity)){
                        batchResultDTOList.add(BatchResultDTO.fail(updateDTO.getSupplierId(), "", "供应商基础信息未找到"));
                    }
                    BaseIdDTO.CodeDTO company = companyList.stream().filter(f -> f.getId().equals(updateDTO.getPurchaseOrgId())).findFirst().orElse(null);
                    if (Objects.isNull(company)){
                        batchResultDTOList.add(BatchResultDTO.fail(updateDTO.getSupplierId(), "", "供应商基础信息未找到"));
                    }
                    if (Objects.nonNull(skuVO) && Objects.nonNull(supplierEntity) && Objects.nonNull(company)){
                        batchResultDTOList.add(BatchResultDTO.fail(updateDTO.getSkuId(), "", CharSequenceUtil.format("采购组织【{}】在供应商【{}】SKU【{}】数量【{}】未匹配到采购单价", company.getName(), supplierEntity.getName(), skuVO.getSkuName(), purchaseQty)));
                    }
                }
            }
        }
        subcontractPurchasePriceDTO.setList(list);
        subcontractPurchasePriceDTO.setBatchResultDTOList(batchResultDTOList);
        return CollectionUtils.isEmpty(batchResultDTOList) ? ApiResult.success(subcontractPurchasePriceDTO) : ApiResult.error("获取采购单价异常", subcontractPurchasePriceDTO);
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
    private Boolean updateApproveStatusForApprove(PurchaseApplicationEntity entity, ApproveStatusEnum statusEnum ) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ApproveStatusEnum.APPROVE.equals(statusEnum) || ApproveStatusEnum.REJECT.equals(statusEnum)) {
            entity.setApproveTime(LocalDateTime.now());
            entity.setApproveUserId(CharSequenceUtil.isBlank(entity.getApproveUserId()) ? userInfo.getUid() : entity.getApproveUserId());
            entity.setApproveUserName(CharSequenceUtil.isBlank(entity.getApproveUserName()) ? userInfo.getUserName() : entity.getApproveUserName());
        } else {
            entity.setApproveTime(null);
            entity.setApproveUserId("");
            entity.setApproveUserName("");
        }
        entity.setApproveStatus(statusEnum.getCode());
        return super.updateById(entity);
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
            StringBuilder errMsg = new StringBuilder("");
            List<String> prohibitDetailIds = prohibitDetails.stream().map(PurchaseApplicationDetailEntity::getId).distinct().collect(Collectors.toList());
            Map<String, Object> exceptionDataMap = new HashMap<>();
            exceptionDataMap.put("purchaseApplicationDetailIds", prohibitDetailIds);
            prohibitDetails.stream().forEach(detail->{
                PurchaseApplicationEntity entity = detailMainMap.get(detail.getId());
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_99998.msg,entity.getCode(),detail.getSkuNo())).append("</br>");
            });
            throw new ServiceException(new ApiResult<>(ApiError.ERROR_99998.code,errMsg.toString(), exceptionDataMap));
        }
        return  detailList;
    }

    /**
     * @description: 列表查询数据处理
     * @author Will
     * @date: 2023/4/19 17:57
     * @param records
     */
    private void doOpHandlePurchaseApplication(List<PurchaseApplicationDTO.ListDTO> records){
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

        //查询审核流程
        List<String> ids = records.stream().map(PurchaseApplicationDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_APPLICATION.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
        }
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
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(obj.getSkuId()) && CharSequenceUtil.equals(e.getType(), BomTypeEnum.SINGLE.getType())).count();
                if (count > 0) {
                    obj.setIsConstitute(Boolean.TRUE);
                }
            }
            obj.setFirstMassProductName(FirstMassProductTypeEnum.getName(obj.getFirstMassProduct()));
            obj.setCreatePoTypeName(CreatePoTypeEnum.getName(obj.getCreatePoType()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            if(obj.getCreatePoType().equals(CreatePoTypeEnum.CLOSED.getStatus())){
                obj.setWaitQty(0);
            }else{
                obj.setWaitQty(obj.getApplyQty() - (Objects.isNull(obj.getRealPurchaseQty())?0:obj.getRealPurchaseQty()));
            }
            obj.setFirstMassProductName(FirstMassProductTypeEnum.getName(obj.getFirstMassProduct()));
            //采购建议数据
            if (SourceTypeEnum.PURCHASE_SUGGESTION_MERGE.getCode().equals(obj.getSourceType())) {
                List<PurchaseSuggestMergeDTO.PushSourceDTO> pushSourceList = BeanUtil.copyToList(obj.getSourceJson(), PurchaseSuggestMergeDTO.PushSourceDTO.class);
                String codes = pushSourceList.stream().map(PurchaseSuggestMergeDTO.PushSourceDTO::getCode).distinct().collect(Collectors.joining(","));
                obj.setSourceCode(codes);
            }

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && CharSequenceUtil.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                obj.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,obj.getApproveUserName()));
            }
        }
    }

    @Override
    public List<PurchaseApplicationDTO.ListDTO> listStockInQty(List<PurchaseApplicationDTO.ListDTO> records){
        //查询关联采购
        List<String> detailIds = records.stream().map(PurchaseApplicationDTO.ListDTO::getPurchaseApplicationDetailId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(detailIds)){
            PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
            searchParamDTO.setPurchaseApplicationDetailIds(detailIds);
            List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(searchParamDTO);
            //入库信息
            List<PoInstockDetailEntity> purchaseStockInDetailList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(refList)) {
                List<String> podIds = refList.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
                //查询入库
                purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
            }
            List<PurchaseApplicationDetailEntity> detailList = purchaseApplicationDetailService.listByIds(detailIds);
            for (PurchaseApplicationDTO.ListDTO obj : records) {
                PurchaseApplicationDetailEntity detailEntity = detailList.stream().filter(r -> r.getId().equals(obj.getPurchaseApplicationDetailId())).findFirst().orElse(null);
                if(null != detailEntity){
                    obj.setSourceDetailId(detailEntity.getSourceDetailId());
                }
                //入库数量
                if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                    List<String> thisPodIds = refList.stream()
                            .filter(e -> e.getPurchaseApplicationDetailId().equals(obj.getPurchaseApplicationDetailId()))
                            .map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseOrderDetailId)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(thisPodIds)) {
                        Integer stockInQty = purchaseStockInDetailList.stream()
                                .filter(e -> thisPodIds.contains(e.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                                .map(PoInstockDetailEntity::getStockInQty)
                                .reduce(MathUtil.ZERO, Integer::sum);
                        obj.setStockInQty(stockInQty);
                    }
                }
            }
        }
        return records;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submitEntity(PurchaseApplicationEntity entity) {
        //待提交并且未作废允许提交
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        log.info("采购申请单提交，ids=【{}】", JSONUtil.toJsonStr(ids));
        //启动流程
        startProcess(entity);
        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购申请单【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pushPurchaseApplication(SoB2cDTO.PushPurchaseApplicationDTO pushDTO) {
        List<SoB2cDTO.PushDetailDTO> detailList = pushDTO.getDetailList();

        List<String> soDetailIdList = detailList.stream().map(SoB2cDTO.PushDetailDTO::getSoDetailId).distinct().collect(Collectors.toList());
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listBySourceDetailIdList(soDetailIdList);

        //原销售订单明细
        List<SoDetailEntity> soDetailList = FeignQuery.getByIds(SoDetailEntity.class, soDetailIdList);
        if (CollUtil.isEmpty(soDetailIdList)) {
            throw new ServiceException(ApiError.ERROR_SO_DETAIL_NOT_EXIST);
        }
        Map<String, SoDetailEntity> soDetailMap = soDetailList.stream().collect(Collectors.toMap(SoDetailEntity::getId, Function.identity()));

        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(userInfo.getUid());
        if (ObjectUtils.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }

        //产品包装信息
        List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductPackEntity> list = FeignQuery.create(ProductPackEntity.class).in(ProductPackEntity::getSkuId, skuIdList).list();

        Map<String, List<SoB2cDTO.PushDetailDTO>> map = detailList.stream().collect(Collectors.groupingBy(SoB2cDTO.PushDetailDTO::getSoId));
        for (Map.Entry<String, List<SoB2cDTO.PushDetailDTO>> entry : map.entrySet()) {
            List<SoB2cDTO.PushDetailDTO> value = entry.getValue();
            PurchaseApplicationDTO.AddDTO dto = new PurchaseApplicationDTO.AddDTO();
            dto.setSourceId(entry.getKey());
            dto.setSourceType(SourceTypeEnum.SO_INFO.getCode());
            dto.setApplyDate(LocalDate.now());
            dto.setSourceCode(value.get(0).getSoCode());
            dto.setApplyUserId(userInfo.getUid());
            dto.setApplyDeptId(findUserDTO.getDepartmentId());
            List<PurchaseApplicationDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (SoB2cDTO.PushDetailDTO detailDTO : value) {
                PurchaseApplicationDetailDTO.AddDTO addDetailDTO = new PurchaseApplicationDetailDTO.AddDTO();
                BeanUtil.copyProperties(detailDTO, addDetailDTO);
                addDetailDTO.setSourceDetailId(detailDTO.getSoDetailId());
                //已下推数量
                Integer hasPushQty = purchaseApplicationDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), addDetailDTO.getSourceDetailId())).map(PurchaseApplicationDetailEntity::getApplyQty).reduce(MathUtil.ZERO, Integer::sum);
                //销售订单数量
                SoDetailEntity soDetailEntity = soDetailMap.get(detailDTO.getSoDetailId());
                if (ObjectUtil.isEmpty(soDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_SO_DETAIL_NOT_EXIST);
                }
                if (hasPushQty + detailDTO.getApplyQty() > soDetailEntity.getQty()) {
                    throw new ServiceException(CharSequenceUtil.format("销售订单【{}】SKU【{}】的申请数量【{}】和已下推数量【{}】之和不能大于销售订单数量【{}】",value.get(0).getSoCode(),soDetailEntity.getSkuNo(),detailDTO.getApplyQty(), hasPushQty, soDetailEntity.getQty()));
                }
                ProductPackEntity packEntity = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), soDetailEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(packEntity) && ObjectUtil.isNotEmpty(packEntity.getBoxQty())) {
                    addDetailDTO.setUnitQty(packEntity.getBoxQty().intValue());
                }
                addDetailDTO.setSkuId(soDetailEntity.getSkuId());
                addDetailDTO.setSkuNo(soDetailEntity.getSkuNo());
                addDetailDTO.setDestWarehouseId(detailDTO.getWarehouseId());
                addDetailDTO.setPurchaseOrgId(detailDTO.getOrgId());
                addDetailList.add(addDetailDTO);
            }
            dto.setDetails(addDetailList);
            PurchaseApplicationServiceImpl bean = ApplicationContextUtils.getBean(PurchaseApplicationServiceImpl.class);
            PurchaseApplicationEntity add = bean.add(dto);
            //提交
            if (Boolean.TRUE.equals(pushDTO.getIsSubmit())) {
                bean.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseApplicationDTO.CheckUpDTO> checkUp(List<String> ids) {
        List<PurchaseApplicationDetailEntity> detailList = purchaseApplicationDetailService.listByIds(ids);
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<String> mainIdList = detailList.stream().map(PurchaseApplicationDetailEntity::getPurchaseApplicationId).distinct().collect(Collectors.toList());
        List<PurchaseApplicationEntity> purchaseApplicationList = this.listByIds(mainIdList);
        if (CollUtil.isEmpty(purchaseApplicationList)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        //销售订单明细
        List<String> sourceDetailIdList = detailList.stream().map(PurchaseApplicationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollUtil.isEmpty(sourceDetailIdList) ? Collections.emptyList() : FeignQuery.getByIds(SoDetailEntity.class, sourceDetailIdList);
        Map<String, SoDetailEntity> soDetailMap = soDetailList.stream().collect(Collectors.toMap(SoDetailEntity::getId, Function.identity()));

        //试产量产明细
        List<PilotApplicationDetailEntity> pilotApplicationDetailList = CollUtil.isEmpty(sourceDetailIdList) ? Collections.emptyList() : FeignQuery.getByIds(PilotApplicationDetailEntity.class, sourceDetailIdList);
        Map<String, PilotApplicationDetailEntity> pilotApplicationDetailMap = pilotApplicationDetailList.stream().collect(Collectors.toMap(PilotApplicationDetailEntity::getId, Function.identity()));

        //所有明细
        List<PurchaseApplicationDetailEntity> allDetailList = purchaseApplicationDetailService.listBySourceDetailIdList(sourceDetailIdList);
        Map<String, List<PurchaseApplicationDetailEntity>> allDetailMap = allDetailList.stream().collect(Collectors.groupingBy(PurchaseApplicationDetailEntity::getSourceDetailId));


        Map<String, PurchaseApplicationEntity> map = purchaseApplicationList.stream().collect(Collectors.toMap(PurchaseApplicationEntity::getId, Function.identity()));
        Set<PurchaseApplicationDTO.CheckUpDTO> resultList = new HashSet<>();
        for (PurchaseApplicationDetailEntity detailEntity : detailList) {
            PurchaseApplicationEntity purchaseApplicationEntity = map.get(detailEntity.getPurchaseApplicationId());
            if (ObjUtil.isEmpty(purchaseApplicationEntity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }
            PurchaseApplicationDTO.CheckUpDTO checkUpDTO = new PurchaseApplicationDTO.CheckUpDTO();
            checkUpDTO.setSourceId(purchaseApplicationEntity.getSourceId());
            checkUpDTO.setSourceCode(purchaseApplicationEntity.getSourceCode());
            checkUpDTO.setSourceType(purchaseApplicationEntity.getSourceType());
            checkUpDTO.setSkuId(detailEntity.getSkuId());
            checkUpDTO.setSkuNo(detailEntity.getSkuNo());

            //类型为销售订单
            if (CharSequenceUtil.equals(purchaseApplicationEntity.getSourceType(),SourceTypeEnum.SO_INFO.getCode())) {
                SoDetailEntity soDetailEntity = soDetailMap.get(detailEntity.getSourceDetailId());
                checkUpDTO.setOldQty(ObjectUtil.isEmpty(soDetailEntity) ? MathUtil.ZERO : soDetailEntity.getQty());
            } else if (CharSequenceUtil.equals(purchaseApplicationEntity.getSourceType(),SourceTypeEnum.PILOT_APPLICATION.getCode())){
                PilotApplicationDetailEntity pilotApplicationDetailEntity = pilotApplicationDetailMap.get(detailEntity.getSourceDetailId());
                checkUpDTO.setOldQty(ObjectUtil.isEmpty(pilotApplicationDetailEntity) ? MathUtil.ZERO : pilotApplicationDetailEntity.getApplyQty());
            } else {
               continue;
            }
            //采购申请量
            List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = allDetailMap.get(detailEntity.getSourceDetailId());
            Integer applyQty = purchaseApplicationDetailList.stream().map(PurchaseApplicationDetailEntity::getApplyQty).reduce(MathUtil.ZERO, Integer::sum);
            checkUpDTO.setApplyQty(applyQty);
            checkUpDTO.setUnApplyQty(checkUpDTO.getOldQty() - checkUpDTO.getApplyQty());
            resultList.add(checkUpDTO);
        }
        return resultList.stream().collect(Collectors.toList());
    }

    @Override
    public List<PurchaseApplicationEntity> listByCodes(List<String> list) {
        if (CollUtil.isNotEmpty(list)) {
            return this.list(new LambdaQueryWrapper<PurchaseApplicationEntity>().in(PurchaseApplicationEntity::getCode,list));
        }
        return new ArrayList<>();
    }

    @Override
    public void updateApproveStatus(PurchaseApplicationEntity one, String approveStatus) {
        String userId = sysUserFeign.getUserByThird(ThirdpartyPlatformEnum.FS.getCode(), one.getApproveUserId()).getUserId();
        //更新审核状态
        lambdaUpdate().in(PurchaseApplicationEntity::getId,Arrays.asList(one.getId()))
                .set(PurchaseApplicationEntity::getApproveUserId,userId)
                .set(PurchaseApplicationEntity::getApproveStatus,approveStatus)
                .update();
    }

    @Override
    public void updatePA(PurchaseApplicationDTO.updatePADTO updateDTO) {
        PurchaseApplicationEntity one = this.getOne(new QueryWrapper<PurchaseApplicationEntity>().eq(updateDTO.getField(), updateDTO.getValue()));
        if (ObjUtil.isEmpty(one)) {
            throw new ServiceException("未找到采购申请单");
        }
        BeanUtil.copyProperties(updateDTO,one);
        this.updateById(one);
    }

    @Override
    public Integer getPushDownBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return 0;
        }
        return this.lambdaQuery().in(PurchaseApplicationEntity::getSourceId,sourceIds).count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseApplicationEntity addAndApprove(PurchaseApplicationDTO.InsertDTO dto) {
        PurchaseApplicationEntity entity = purchaseApplicationService.add(dto);
        if(Objects.isNull(entity)){
            throw new ServiceException(ApiError.ERROR_1019);
        }
        PurchaseApplicationEntity oldEntity = this.getById(entity.getId());
        //直接审核通过
        purchaseApplicationService.thirdApproveEnd(new PurchaseApplicationDTO.UpdateApproveStatusDTO(dto.getApprovalStatus(),dto.getThirdApproveUserId(),dto.getThirdApproveTime(),oldEntity, ApproveStatusEnum.APPROVE));
        return oldEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean thirdApproveEnd(PurchaseApplicationDTO.UpdateApproveStatusDTO updateApproveStatusDTO) {
        ApproveStatusEnum approveStatus = updateApproveStatusDTO.getApproveStatus();
        PurchaseApplicationEntity entity = updateApproveStatusDTO.getPurchaseApplicationEntity();
        if (approveStatus == ApproveStatusEnum.APPROVE && CharSequenceUtil.isNotBlank(entity.getApproveUserId())){
            SysUserThirdEntity userByThird = sysUserFeign.getUserByThird(ThirdpartyPlatformEnum.FS.getCode(), entity.getApproveUserId());
            if (Objects.isNull(userByThird)) {
                throw new ServiceException("第三方用户信息不存在");
            }
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(userByThird.getUserId());
            if (ObjUtil.isEmpty(findUserDTO)) {
                throw new ServiceException(ApiError.ERROR_1037, userByThird.getUserId());
            }
            entity.setApproveUserId(findUserDTO.getUserId());
            entity.setApproveUserName(findUserDTO.getUserName());
            entity.setApproveTime(updateApproveStatusDTO.getThirdApproveTime());
        }
        return approveEnd(new ApproveOneDTO(entity.getId(),ApproveTypeEnum.PASS.getStatus(),""), entity);
    }

    @Override
    public Boolean importMainFile(MultipartFile excelFile, HttpServletResponse response) {
        PurchaseApplicationMainExcelListener excelListenerUtil = new PurchaseApplicationMainExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseApplicationMainExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseApplicationMainExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<PurchaseApplicationMainExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseApplicationMainExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理数据
        handleMainFile(successList,errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        //errorList根据index顺序排序
        errorList.sort(Comparator.comparing(
                dto -> {
                    try {
                        return dto.getIndex() != null ? Integer.parseInt(dto.getIndex()) : null;
                    } catch (NumberFormatException e) {
                        return null;  // 非数字视为null
                    }
                },
                Comparator.nullsFirst(Comparator.naturalOrder())
        ));
        String excelPath = "excel/purchaseApplicationMainError.xlsx";
        String name = "purchaseApplicationMainError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
        return Boolean.FALSE;
    }
    /**
     * 处理数据
     * @author will
     * @date 2025/7/31 09:06
     * @param successList
     * @param errorList
     * @return void
     */
    private void handleMainFile (List<PurchaseApplicationMainExcelDTO> successList,List<PurchaseApplicationMainExcelDTO> errorList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        //产品信息
        List<String> skuNos = successList.stream().map(PurchaseApplicationMainExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNos)
                .eq(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_PASS.getCode())
                .list();
        //仓库信息
        List<String> warehouseNames = successList.stream().map(PurchaseApplicationMainExcelDTO::getDestWarehouseName).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.create(WarehouseEntity.class)
                .in(WarehouseEntity::getName, warehouseNames)
                .eq(WarehouseEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus())
                .eq(WarehouseEntity::getDisabled,Boolean.FALSE)
                .list();
        Map<String, WarehouseEntity> warehosueMap = CollUtil.isEmpty(warehouseList) ? new HashMap<>() : warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getName, Function.identity()));

        //采购组织
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        Map<String, BaseIdDTO> orgMap = companyList.stream().collect(Collectors.toMap(BaseIdDTO::getName, Function.identity()));

        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //部门
        SysDepartmentUserNumberDTO departmentUserNumberDTO = sysUserFeign.getDeptByUserId(userInfo.getUid());

        Map<String, List<PurchaseApplicationMainExcelDTO>> excelMap = successList.stream().collect(Collectors.groupingBy(PurchaseApplicationMainExcelDTO::getIndex));
        for ( Map.Entry<String, List<PurchaseApplicationMainExcelDTO>> entry : excelMap.entrySet()) {
            List<PurchaseApplicationMainExcelDTO> value = entry.getValue();
            PurchaseApplicationDTO.AddDTO addDTO = new PurchaseApplicationDTO.AddDTO();
            addDTO.setApplyDate(LocalDateUtil.parseStrToLocalDate(value.get(0).getApplyDateStr()));
            addDTO.setApplyUserId(userInfo.getUid());
            addDTO.setApplyDeptId(ObjectUtil.isEmpty(departmentUserNumberDTO) ? "" : departmentUserNumberDTO.getDepartmentId());
            List<PurchaseApplicationDetailDTO.AddDTO> details = new ArrayList<>();

            for (PurchaseApplicationMainExcelDTO excelDTO : value) {
                //注解验证信息
                List<String> errorMsgList = new ArrayList<>();
                //SKU验证
                ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    errorMsgList.add(CharSequenceUtil.format("未找到审核通过的SKU"));
                }
                //仓库验证
                WarehouseEntity warehouseEntity = warehosueMap.get(excelDTO.getDestWarehouseName());
                if (ObjectUtil.isEmpty(warehouseEntity)) {
                    errorMsgList.add(CharSequenceUtil.format("未找到审核通过并启用的仓库名称"));
                }
                //采购组织验证
                BaseIdDTO company = orgMap.get(excelDTO.getPurchaseOrgName());
                if (ObjectUtil.isEmpty(company)) {
                    errorMsgList.add(CharSequenceUtil.format("未找到采购组织"));
                }
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                PurchaseApplicationDetailDTO.AddDTO addDetailDTO = new PurchaseApplicationDetailDTO.AddDTO();
                addDetailDTO.setSkuId(productDetailEntity.getId());
                addDetailDTO.setSkuNo(productDetailEntity.getSkuNo());
                addDetailDTO.setDestWarehouseId(warehouseEntity.getId());
                addDetailDTO.setDestWarehouseName(warehouseEntity.getName());
                addDetailDTO.setPurchaseOrgId(company.getId());
                addDetailDTO.setPurchaseOrgName(company.getName());
                addDetailDTO.setApplyQty(Integer.valueOf(excelDTO.getApplyQtyStr()));
                addDetailDTO.setFirstMassProduct(FirstMassProductTypeEnum.getCode(excelDTO.getFirstMassProductName()));
                addDetailDTO.setFirstMassProductName(excelDTO.getFirstMassProductName());
                addDetailDTO.setIsUrgent(BooleanEnum.getByName(excelDTO.getIsUrgentStr()));
                addDetailDTO.setPlanDeliveryDate(LocalDateUtil.parseStrToLocalDate(excelDTO.getPlanDeliveryDateStr()));
                addDetailDTO.setRemark(excelDTO.getRemark());
                details.add(addDetailDTO);
            }
            if (CollUtil.isEmpty(details)) {
                continue;
            }
            addDTO.setDetails(details);
            //新增错误信息
            List<String> addErrorMsgList = new ArrayList<>();
            try {
                //新增采购申请单
                purchaseApplicationService.add(addDTO);
            } catch (Exception e) {
                addErrorMsgList.add(e.getMessage());
            }
            if (CollUtil.isNotEmpty(addErrorMsgList)) {
                value.forEach(obj -> obj.setErrorMsg(FieldValidUtil.getMsgSort(addErrorMsgList)));
                errorList.addAll(value);
            }
        }
    }

    /**
     * @param entity
     * @description: 启动审核流程
     * @author Will
     * @date: 2023/7/11 14:11
     */
    private void startProcess(PurchaseApplicationEntity entity) {
        LoginUser userInfo = UserContext.getLoginUser();
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.PURCHASE_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(userInfo.getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(PurchaseApplicationEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<PurchaseApplicationDetailEntity> detailList = purchaseApplicationDetailService.listByPurchaseApplicationId(entity.getId());
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.ERROR_98049);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));

        //存在加急
        Boolean isUrgent = detailList.stream().anyMatch(PurchaseApplicationDetailEntity::getIsUrgent);
        variablesMap.put("isUrgentTotal", isUrgent);
        //新品首批
        String firstMassProduct = detailList.stream().map(PurchaseApplicationDetailEntity::getFirstMassProduct).collect(Collectors.joining(","));
        variablesMap.put("firstMassProductTotal", firstMassProduct);
        return variablesMap;
    }
}
