package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.scm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.model.scm.enums.AssetNoticeTabListEnum;
import com.erp.model.scm.enums.AssetPurchaseOrderTypeEnum;
import com.erp.model.plm.enums.MoldInfoTagEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.server.scm.service.AttachmentService;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.listener.AssetNoticeExcelListener;
import com.erp.server.scm.mapper.AssetNoticeMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import java.util.Objects;
import java.util.stream.Stream;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import org.springframework.web.multipart.MultipartFile;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetNoticeServiceImpl extends SuperServiceImpl<AssetNoticeMapper, AssetNoticeEntity> implements AssetNoticeService {

    @Autowired
    private ModuleOperateLogService moduleOperateLogService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private AssetNoticeDetailService assetNoticeDetailService;

    @Autowired
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Autowired
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Autowired
    private SupplierContactService supplierContactService;

    @Autowired
    private SupplierService suppliserService;

    @Autowired
    private KingdeePaymentConditionService kingdeePaymentConditionService;

    @Autowired
    private SupplierAccountService supplierAccountService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private FileFeign fileFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetNoticeDTO.AddDTO addDTO) {
        AssetNoticeEntity assetNoticeEntity = new AssetNoticeEntity();
        BeanMapperUtils.copy(addDTO, assetNoticeEntity);

        // 数据处理
        handleData(assetNoticeEntity);

        log.info("开始新增开模通知单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPL);
        assetNoticeEntity.setCode(code);
        boolean save = super.save(assetNoticeEntity);
        if(!save) {
            throw new ServiceException("开模通知单保存失败");
        }

        hanleAddDetailData(assetNoticeEntity,addDTO.getAssetNoticeDetailDTO());
        // 新增明细
        assetNoticeDetailService.add(addDTO.getAssetNoticeDetailDTO(),assetNoticeEntity.getId());

        // 保存附件
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && CollectionUtils.isNotEmpty(attachmentNameList) 
                && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<AssetNoticeEntity> entityClass = AssetNoticeEntity.class;
            TableName tableName = entityClass.getDeclaredAnnotation(TableName.class);
            String type = tableName.value();
            attachmentService.batchSave(attachmentUrlList, attachmentNameList, type, assetNoticeEntity.getId());
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "开模通知单" , assetNoticeEntity.getCode());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), assetNoticeEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(assetNoticeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetNoticeDTO.UpdateDTO addOrUpdateDTO) {
        AssetNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetNoticeEntity assetNoticeEntity =  BeanMapperUtils.map(AssetNoticeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetNoticeEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetNoticeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        hanleUpdateDetailData(assetNoticeEntity,addOrUpdateDTO.getAssetNoticeDetailDTO());
        assetNoticeDetailService.update(addOrUpdateDTO.getAssetNoticeDetailDTO(),assetNoticeEntity.getId());

        // 更新附件
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && CollectionUtils.isNotEmpty(attachmentNameList)
                && attachmentUrlList.size() == attachmentNameList.size()) {
            // 获取旧附件列表
            List<AttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessId(assetNoticeEntity.getId());
            if (CollUtil.isNotEmpty(oldAttachmentList)) {
                // 处理删除的数据
                List<AttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(remove)) {
                    attachmentService.deleteByUrlList(remove.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                }
            }

            // 处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> addUrls = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(addUrls)) {
                Class<AssetNoticeEntity> entityClass = AssetNoticeEntity.class;
                TableName tableName = entityClass.getDeclaredAnnotation(TableName.class);
                String type = tableName.value();
                List<String> addNames = new ArrayList<>();
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if (addUrls.contains(attachmentUrlList.get(i))) {
                        addNames.add(attachmentNameList.get(i));
                    }
                }
                attachmentService.batchSave(addUrls, addNames, type, assetNoticeEntity.getId());
            }
        }

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，单号：【{}】", assetNoticeEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetNoticeEntity.getCode(), "开模通知单");
        moduleOperateLogService.addModuleOperateLogByObj(old, assetNoticeEntity, ModuleTypeEnum.ASSET_NOTICE.getCode(), assetNoticeEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetNoticeDTO.ListDTO> paging(PagingDTO<AssetNoticeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetNoticeDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetNoticeDTO.PagingParamDTO searchParam = new AssetNoticeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AssetNoticeDTO.TabListDTO> returnList = new ArrayList<>();
        //统计非审核状态数量
        List<AssetNoticeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        //统计已生成和待生成状态数量
        List<AssetNoticeDTO.TabListDTO> refPurchaseTabList = baseMapper.refPurchaseTabList(searchParam);
        list.addAll(refPurchaseTabList);

        // 获取状态列表（all最后统计）
        List<String> statusList = AssetNoticeTabListEnum.getStatusList();
        statusList.remove("all");

        // 设置状态名称
        list.forEach(tabListDTO ->
                tabListDTO.setTabFlagName(AssetNoticeTabListEnum.getName(tabListDTO.getTabFlag()))
        );

        // 补全缺失的状态（确保顺序与枚举一致）
        List<AssetNoticeDTO.TabListDTO> finalList = new ArrayList<>();
        statusList.forEach(status -> {
            Optional<AssetNoticeDTO.TabListDTO> existingItem = list.stream()
                    .filter(item -> item.getTabFlag().equals(status))
                    .findFirst();
            if (existingItem.isPresent()) {
                finalList.add(existingItem.get()); // 已存在的状态直接添加
            } else {
                // 缺失的状态补0
                finalList.add(new AssetNoticeDTO.TabListDTO(
                        status,
                        AssetNoticeTabListEnum.getName(status),
                        0
                ));
            }
        });

        // 添加合计项（all）
        returnList.add(new AssetNoticeDTO.TabListDTO(
                "all",
                AssetNoticeTabListEnum.ALL.getName(),
                finalList.stream().mapToInt(AssetNoticeDTO.TabListDTO::getCount).sum()
        ));

        // 按枚举顺序添加所有状态
        returnList.addAll(finalList);

        return returnList;
    }

    @Override
    public void exportList(AssetNoticeDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("开模通知单导出", EXPORT_SCM_ASSET_NOTICE.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetNoticeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "开模通知单");
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), pairList, "提交");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetNoticeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetNoticeDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AssetNoticeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getCode())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "开模通知单", approveType.getName(), dto.getComment());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "审核");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    @Override
    public List<AssetNoticeDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrder(List<String> idList) {
        List<AssetNoticeDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrderDTOS = new ArrayList<>();
        List<AssetNoticeDetailEntity> assetNoticeDetailEntityList = assetNoticeDetailService.listByIds(idList);
        if (assetNoticeDetailEntityList.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_95298);
        }


        //可以生成采购订单的明细（未生成、部分生成）
        List<AssetNoticeDetailEntity> collect = assetNoticeDetailEntityList.stream()
                .filter(obj -> !CreatePoTypeEnum.ALL_GENERATED.getStatus().equals(obj.getCreatePoType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            throw new ServiceException(ApiError.ERROR_95299);
        }

        for (AssetNoticeDetailEntity assetNoticeDetailEntity : collect) {
            AssetNoticeDTO.ViewGeneratePurchaseOrderDTO viewGeneratePurchaseOrderDTO = new AssetNoticeDTO.ViewGeneratePurchaseOrderDTO();

            AssetNoticeEntity assetNoticeEntity = this.getById(assetNoticeDetailEntity.getMainId());
            //主表数据
            if (Objects.isNull(assetNoticeEntity)) {
                throw new ServiceException(ApiError.ERROR_95297);
            }
            BeanUtils.copyProperties(assetNoticeDetailEntity,viewGeneratePurchaseOrderDTO);

            //获取公司信息
            SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(assetNoticeDetailEntity.getPurchaseOrgId());
            if (Objects.isNull(companyEntity)) {
                throw new ServiceException(ApiError.ERROR_9014);
            }
            viewGeneratePurchaseOrderDTO.setPurchaseOrgName(companyEntity.getCompanyName());

            //获取sku信息
            List<SkuVO> skuVOList = plmTaskFeign.listSkuPurchaseByIds(Arrays.asList(assetNoticeDetailEntity.getAssetId()));
            if (!skuVOList.isEmpty()) {
                viewGeneratePurchaseOrderDTO.setMoq(skuVOList.get(0).getMoq());
                viewGeneratePurchaseOrderDTO.setDeliveryDay(skuVOList.get(0).getDeliveryCycle());
                viewGeneratePurchaseOrderDTO.setSkuId(skuVOList.get(0).getSkuId());
                viewGeneratePurchaseOrderDTO.setSkuNo(skuVOList.get(0).getSkuNo());
                viewGeneratePurchaseOrderDTO.setProductName(skuVOList.get(0).getSkuName());
            }

            //关联待采购数量
            LambdaQueryWrapper<AssetPurchaseOrderDetailEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(AssetPurchaseOrderDetailEntity::getSourceDetailId,assetNoticeDetailEntity.getId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted,Boolean.FALSE);
            List<AssetPurchaseOrderDetailEntity> list = assetPurchaseOrderDetailService.list(lambdaQueryWrapper);
            BigDecimal totalPurchaseQty = list.stream()
                    .map(AssetPurchaseOrderDetailEntity::getPurchaseQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            viewGeneratePurchaseOrderDTO.setApplyQty(assetNoticeDetailEntity.getApplyQty());
            viewGeneratePurchaseOrderDTO.setWaitQty(assetNoticeDetailEntity.getApplyQty().subtract(totalPurchaseQty));

            viewGeneratePurchaseOrderDTO.setId(assetNoticeDetailEntity.getMainId());
            viewGeneratePurchaseOrderDTO.setAssetNoticeDetailId(assetNoticeDetailEntity.getId());
            viewGeneratePurchaseOrderDTO.setCode(assetNoticeEntity.getCode());
            viewGeneratePurchaseOrderDTO.setPlanDeliveryDate(assetNoticeDetailEntity.getPlanDeliveryDate());
            // 供应商信息从明细获取
            viewGeneratePurchaseOrderDTO.setSupplierId(assetNoticeDetailEntity.getSupplierId());
            viewGeneratePurchaseOrderDTO.setSupplierName(assetNoticeDetailEntity.getSupplierName());

            viewGeneratePurchaseOrderDTOS.add(viewGeneratePurchaseOrderDTO);
        }

        return viewGeneratePurchaseOrderDTOS;
    }

    @Override
    public Boolean generatePurchaseOrder(List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO> dtoList) {
        List<String> ids = dtoList.stream().map(AssetNoticeDTO.ListGeneratePurchaseOrderDTO::getId).collect(Collectors.toList());
        //主表数据
        List<AssetNoticeEntity> mainList = this.listByIds(ids);

        if (CollectionUtils.isEmpty(mainList)) {
            throw new ServiceException(ApiError.ERROR_95297);
        }

        //已审核数据才能生成采购单
        long statusCount = mainList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (statusCount > 0) {
            throw new ServiceException(ApiError.ERROR_95299);
        }

        //sku信息
        List<String> assetIdList = dtoList.stream().map(AssetNoticeDTO.ListGeneratePurchaseOrderDTO::getAssetId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(assetIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }

        log.info("生成采购订单 ids= {}",ids);

        //设置采购订单生成类型
        List<AssetNoticeDetailEntity> detailList = setCreatePoType(dtoList, mainList);

        AssetPurchaseOrderEntity assetPurchaseOrderEntity = new AssetPurchaseOrderEntity();
        assetPurchaseOrderEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPO));
        assetPurchaseOrderEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());//默认待审核
        assetPurchaseOrderEntity.setContractStampStatus(ContractStampStatusEnum.WAIT_SUBMIT.getCode());
        assetPurchaseOrderEntity.setOrderType(AssetPurchaseOrderTypeEnum.ASSET_PURCHASE.getCode());

        //供应商默认联系人
        List<String> supplierIds = dtoList.stream().map(AssetNoticeDTO.ListGeneratePurchaseOrderDTO::getSupplierId).collect(Collectors.toList());
        List<SupplierContactEntity> supplierContactEntityList = supplierContactService.lambdaQuery()
                .in(SupplierContactEntity::getSupplierId, supplierIds)
                .eq(SupplierContactEntity::getIsDefault,Boolean.TRUE)
                .eq(SupplierContactEntity::getIsDeleted, Boolean.FALSE)
                .list();

        //采购订单新增数据
        List<AssetPurchaseOrderDTO.AddDTO> resultList = new ArrayList<>();

        //主表数据按供应商和采购组织分组
        Map<String, List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO>> collect = dtoList.stream()
                .collect(Collectors.groupingBy(obj -> obj.getSupplierId().concat("|").concat(obj.getPurchaseOrgId())));

        for (Map.Entry<String, List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO>> entry : collect.entrySet()) {
            List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO> value = entry.getValue();
            //采购订单主表数据
            AssetPurchaseOrderDTO.AddDTO addDTO = new AssetPurchaseOrderDTO.AddDTO();
            AssetNoticeEntity entity = mainList.stream().filter(obj -> obj.getId().equals(value.get(0).getId())).findFirst().orElse(null);
            if (org.springframework.util.ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }
            addDTO.setOrderType(AssetPurchaseOrderTypeEnum.ASSET_PURCHASE.getCode());
            addDTO.setPurchaseOrgId(value.get(0).getPurchaseOrgId());
            addDTO.setPurchaseOrgName(value.get(0).getPurchaseOrgName());
            if (StringUtils.isNotBlank(value.get(0).getPurchaseUserId())) {
                addDTO.setPurchaseUserId(value.get(0).getPurchaseUserId());
                addDTO.setPurchaseUserName(value.get(0).getPurchaseUserName());
                SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysUserFeign.getDeptByUserId(value.get(0).getPurchaseUserId());
                if (Objects.nonNull(sysDepartmentUserNumberDTO)) {
                    addDTO.setPurchaseDeptId(sysDepartmentUserNumberDTO.getDepartmentId());
                    addDTO.setPurchaseDeptName(sysDepartmentUserNumberDTO.getDepartmentName());
                }
            }

            addDTO.setPurchaseDate(LocalDate.now());
            addDTO.setSourceCode(value.get(0).getCode());
            addDTO.setSourceId(value.get(0).getId());
            addDTO.setSourceType(SourceTypeEnum.ASSET_NOTICE.getCode());

            //采购订单供应商信息
            AssetPurchaseOrderSupplierDTO.AddDTO supplierDTO = new AssetPurchaseOrderSupplierDTO.AddDTO();
            String supplierId = value.get(0).getSupplierId();
            supplierDTO.setSupplierId(supplierId);
            supplierDTO.setSupplierName(value.get(0).getSupplierName());
            SupplierDTO.ViewDTO supplier = suppliserService.getBySupplierId(supplierId);
            if (Objects.nonNull(supplier)) {
                //付款条件
                KingdeePaymentConditionEntity kingdeePaymentConditionEntity = kingdeePaymentConditionService.getByCode(supplier.getPaymentCondition());
                if (Objects.nonNull(kingdeePaymentConditionEntity)) {
                    supplierDTO.setPaymentCondition(supplier.getPaymentCondition());
                    supplierDTO.setPaymentConditionName(kingdeePaymentConditionEntity.getName());
                }
                //结算方式
                supplierDTO.setPayMethodId(supplier.getPayMethodId());
                //结算币种
                supplierDTO.setPayCurrency(supplier.getPayCurrency());

                List<SupplierContactDTO.UpdateDTO> updateDTOS = supplierContactService.listBySupplierId(supplierId);

                SupplierContactDTO.UpdateDTO selectedDTO = updateDTOS.stream()
                        .filter(obj -> Boolean.TRUE.equals(obj.getIsDefault()))
                        .findFirst()
                        .orElse(updateDTOS.isEmpty() ? null : updateDTOS.get(0));

                if (selectedDTO != null) {
                    supplierDTO.setContactName(selectedDTO.getPerson());
                    supplierDTO.setContactTelNumber(selectedDTO.getTelNumber());
                }

            }
            List<SupplierAccountDTO.UpdateDTO> supplierAccountList = supplierAccountService.getBySupplierId(supplierId);
            SupplierAccountDTO.UpdateDTO supplierAccount = supplierAccountList.stream()
                    .filter(obj -> Boolean.TRUE.equals(obj.getIsDefault()))
                    .findFirst()
                    .orElseGet(() -> supplierAccountList.stream().findFirst().orElse(null));
            // 供应商账户
            if (Objects.isNull(supplierAccount)) {
                throw new ServiceException(ApiError.ERROR_98154);
            }
            supplierDTO.setBankName(supplierAccount.getBankSubbranch());
            supplierDTO.setBankAccount(supplierAccount.getBankAccount());
            supplierDTO.setPayee(supplierAccount.getPayee());
            addDTO.setAssetPurchaseOrderSupplierDTO(supplierDTO);

            //采购订单明细信息
            List<AssetPurchaseOrderDetailDTO.AddDTO> details = new ArrayList<>();
            for (AssetNoticeDTO.ListGeneratePurchaseOrderDTO generatePurchaseOrderDTO : value) {
                AssetPurchaseOrderDetailDTO.AddDTO addDetailDTO = new AssetPurchaseOrderDetailDTO.AddDTO();

                LambdaQueryWrapper<AssetPurchaseOrderDetailEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(AssetPurchaseOrderDetailEntity::getSourceDetailId,generatePurchaseOrderDTO.getAssetNoticeDetailId())
                        .eq(AssetPurchaseOrderDetailEntity::getIsDeleted,Boolean.FALSE);

                ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(generatePurchaseOrderDTO.getAssetCode())).findFirst().orElse(null);
                if (org.springframework.util.ObjectUtils.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }

                MoldInfoEntity moldInfoEntity = plmTaskFeign.getMoldInfoByCode(generatePurchaseOrderDTO.getAssetCode());
                if (Objects.isNull(moldInfoEntity)) {
                    throw new ServiceException(ApiError.ERROR_MOLD_NOT_EXIST);
                }

                BeanUtils.copyProperties(generatePurchaseOrderDTO,addDetailDTO);
                addDetailDTO.setAssetId(productDetailEntity.getId());
                addDetailDTO.setAssetCode(productDetailEntity.getSkuNo());
                addDetailDTO.setAssetName(productDetailEntity.getName());
                addDetailDTO.setPurchaseQty(generatePurchaseOrderDTO.getApplyQty());
                if (generatePurchaseOrderDTO.getTaxPrice() == null
                        || generatePurchaseOrderDTO.getTaxPrice().compareTo(BigDecimal.ZERO) == 0) {
                    throw new ServiceException(ApiError.ERROR_PRICE_ZERO_SKUNO,generatePurchaseOrderDTO.getAssetCode());
                }
                //采购金额
                addDetailDTO.setTotalAmount(generatePurchaseOrderDTO.getTaxPrice().multiply(generatePurchaseOrderDTO.getApplyQty()));
                addDetailDTO.setMainId(generatePurchaseOrderDTO.getId());
                addDetailDTO.setIsUrgent(Boolean.FALSE);
                addDetailDTO.setEndReceive(AssetPurchaseOrderReceiveEnum.WAIT_RECEIVE.getCode());
                addDetailDTO.setSourceDetailId(generatePurchaseOrderDTO.getAssetNoticeDetailId());
                addDetailDTO.setTag(moldInfoEntity.getTag());
                details.add(addDetailDTO);
            }
            addDTO.setAssetPurchaseOrderDetailDTOList(details);
            resultList.add(addDTO);
        }
        //新增采购订单
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList.forEach(obj -> assetPurchaseOrderService.add(obj));
        }

        //更新申请明细生成状态
        assetNoticeDetailService.saveOrUpdateBatch(detailList);
        return Boolean.TRUE;
    }

    private List<AssetNoticeDetailEntity> setCreatePoType(List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO> list,List<AssetNoticeEntity> mainList) {
        List<String> detailIds = list.stream().map(AssetNoticeDTO.ListGeneratePurchaseOrderDTO::getAssetNoticeDetailId).distinct().collect(Collectors.toList());

        //明细数据
        List<AssetNoticeDetailEntity> detailList = assetNoticeDetailService.listByIds(detailIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_95298);
        }

        // 不允许下推的申请单明细id集合
        List<AssetNoticeDetailEntity> prohibitDetails = Lists.newArrayList();
        Map<String, AssetNoticeEntity> detailMainMap = Maps.newHashMap();
        for (AssetNoticeDetailEntity detail : detailList) {
            //已采购数量
            List<AssetPurchaseOrderDetailEntity> detailEntityList = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getSourceDetailId, detail.getId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();
            BigDecimal purchaseQty = detailEntityList.stream().map(obj -> obj.getPurchaseQty()).reduce(BigDecimal.ZERO, BigDecimal::add);


            //本次采购数量
            BigDecimal thisPurchaseQty = list.stream()
                    .filter(obj -> obj.getAssetNoticeDetailId().equals(detail.getId()))
                    .map(AssetNoticeDTO.ListGeneratePurchaseOrderDTO::getApplyQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            AssetNoticeEntity entity = mainList.stream().filter(obj -> obj.getId().equals(detail.getMainId())).findFirst().orElse(null);
            detailMainMap.put(detail.getId(), entity);

            //申请数量
            BigDecimal applyQty = detail.getApplyQty();
            if (applyQty.compareTo(purchaseQty.add(thisPurchaseQty)) < 0) {
                // 不允许下推
                prohibitDetails.add(detail);
            } else if (applyQty.compareTo(purchaseQty.add(thisPurchaseQty)) == 0) {
                detail.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            } else {
                detail.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            }
        }

        if(CollUtil.isNotEmpty(prohibitDetails)) {
            StringBuilder errMsg = new StringBuilder("");
            List<String> prohibitDetailIds = prohibitDetails.stream().map(AssetNoticeDetailEntity::getId).distinct().collect(Collectors.toList());
            Map<String, Object> exceptionDataMap = new HashMap<>();
            exceptionDataMap.put("assetNoticeDetailIds", prohibitDetailIds);
            prohibitDetails.stream().forEach(detail->{
                AssetNoticeEntity entity = detailMainMap.get(detail.getId());
                errMsg.append(CharSequenceUtil.format(ApiError.ERROR_95300.msg,entity.getCode(),detail.getAssetCode())).append("</br>");
            });
            throw new ServiceException(new ApiResult<>(ApiError.ERROR_95300.code,errMsg.toString(), exceptionDataMap));
        }
        return  detailList;
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetNoticeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_NOTICE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        BatchResultDTO batchResultDTO = new BatchResultDTO();
        AssetNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据
        List<AssetNoticeDetailEntity> assetNoticeDetailEntityList = assetNoticeDetailService.list(new LambdaQueryWrapper<AssetNoticeDetailEntity>().eq(AssetNoticeDetailEntity::getMainId, id));
        long createCount = assetNoticeDetailEntityList.stream().filter(obj -> !CreatePoTypeEnum.NOT_GENERATED.getStatus().equals(obj.getCreatePoType())).count();
        if (createCount > 0) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
        }
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "开模通知单");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "反审核");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetNoticeEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        LambdaQueryWrapper<AssetPurchaseOrderEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssetPurchaseOrderEntity::getSourceId,entity.getId())
                .eq(AssetPurchaseOrderEntity::getIsDeleted,Boolean.FALSE);

        List<AssetPurchaseOrderEntity> purchaseOrderEntityList = assetPurchaseOrderService.list(lambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(purchaseOrderEntityList)) {
            throw new ServiceException(ApiError.ERROR_98132);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有待提交并且未作废数据支持删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getCode(), entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        List<AssetNoticeDetailEntity> list = assetNoticeDetailService.lambdaQuery()
                .eq(AssetNoticeDetailEntity::getMainId, id)
                .eq(AssetNoticeDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();

        if (list.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_98135);
        }

        List<String> collect = list.stream().map(obj -> obj.getId()).collect(Collectors.toList());

        //删除明细
        assetNoticeDetailService.removeByIds(collect);

        // 删除附件
        log.info("删除 开始删除附件数据，id：【{}】", id);
        attachmentService.deleteByBusinessIds(Arrays.asList(id));

        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        super.removeById(id);

        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "开模通知单");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "删除");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "开模通知单");
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), pairList, "撤销");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_NOTICE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetNoticeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入开模通知单", IMPORT_SCM_ASSET_NOTICE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importAssetNotice(BaseDTO.ImportDTO dto) {
        //设置操作人
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        FindUserDTO findUserDTO = userList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
        AssetNoticeExcelListener excelListenerUtil = new AssetNoticeExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), AssetNoticeImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<AssetNoticeImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "开模通知单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetNoticeImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    @Transactional
    public BatchResultDTO invalid(AssetNoticeEntity entity, String remark) {
        super.getByIdOpt(entity.getId()).orElseThrow(() -> new ServiceException("未找到开模通知单"));

        // 待提交或审核不通过并且未作废允许作废
        if(!InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())){
            throw new ServiceException(ApiError.ERROR_98012);
        }
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        log.info("作废 开始修改开模通知单状态数据，id：【{}】", entity.getId());
        lambdaUpdate().eq(AssetNoticeEntity::getId, entity.getId())
                .set(AssetNoticeEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(AssetNoticeEntity::getInvalidReason, remark)
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", entity.getId());
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "开模通知单", remark);
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "作废");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public AssetNoticeDTO.ViewDTO view(String id) {
        AssetNoticeEntity assetNoticeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
        AssetNoticeDTO.ViewDTO data = BeanMapperUtils.map(AssetNoticeDTO.ViewDTO.class, assetNoticeEntity);
        // 数据填充处理
        fillOne(data);
        LambdaQueryWrapper<AssetNoticeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetNoticeDetailEntity::getMainId,id);
        List<AssetNoticeDetailEntity> detailList = assetNoticeDetailService.list(queryWrapper);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_95298);
        }
        List<AssetNoticeDetailDTO.ViewDTO> dtoList = BeanMapperUtils.copyList(AssetNoticeDetailDTO.ViewDTO.class, detailList);
        fillViewList(dtoList);
        data.setAssetNoticeDetailDTOList(dtoList);

        // 查询附件信息
        Class<AssetNoticeEntity> entityClass = AssetNoticeEntity.class;
        TableName tableName = entityClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIdAndType(Arrays.asList(id), type);
        if (CollUtil.isNotEmpty(attachmentList)) {
            List<String> attachmentUrlList = attachmentList.stream()
                    .map(AttachmentDTO.UpdateDTO::getAttachUrl)
                    .collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream()
                    .map(AttachmentDTO.UpdateDTO::getAttachName)
                    .collect(Collectors.toList());
            data.setAttachmentUrlList(attachmentUrlList);
            data.setAttachmentNameList(attachmentNameList);
        }

        return data;
    }

    public void fillViewList(List<AssetNoticeDetailDTO.ViewDTO> dtoList){

        for (AssetNoticeDetailDTO.ViewDTO detailDTO : dtoList) {
            detailDTO.setTagName(MoldInfoTagEnum.getName(detailDTO.getTag()));
            // 项目名称从模具档案获取
            if (StringUtils.isNotBlank(detailDTO.getAssetCode())) {
                MoldInfoEntity moldInfoEntity = plmTaskFeign.getMoldInfoByCode(detailDTO.getAssetCode());
                if (Objects.nonNull(moldInfoEntity)) {
                    detailDTO.setProjectName(moldInfoEntity.getProjectName());
                }
            }
        }

    }
    /**
    * 启动流程
    * @param entity
    * @return void
    * @Date
    **/

    public void startProcess(AssetNoticeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_NOTICE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetNoticeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        // 采购开发用户名称
        if (StringUtils.isNotBlank(data.getPurchaseDevUserId())) {
            FindUserDTO purchaseDevUser = sysUserFeign.getUserByUserId(data.getPurchaseDevUserId());
            if (Objects.nonNull(purchaseDevUser)) {
                data.setPurchaseDevUserName(purchaseDevUser.getUserName());
            }
        }
        // 采购跟单用户名称
        if (StringUtils.isNotBlank(data.getPurchaseFollowUserId())) {
            FindUserDTO purchaseFollowUser = sysUserFeign.getUserByUserId(data.getPurchaseFollowUserId());
            if (Objects.nonNull(purchaseFollowUser)) {
                data.setPurchaseFollowUserName(purchaseFollowUser.getUserName());
            }
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(AssetNoticeEntity::getId, id)
            .set(AssetNoticeEntity::getApproveUserId, userInfo.getUid())
            .set(AssetNoticeEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetNoticeEntity::getApproveStatus, approveStatus)
            .set(AssetNoticeEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetNoticeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetNoticeEntity::getId, id)
            .set(AssetNoticeEntity::getApproveUserId, "")
            .set(AssetNoticeEntity::getApproveUserName, "")
            .set(AssetNoticeEntity::getApproveStatus, approveStatus)
            .set(AssetNoticeEntity::getApproveTime, null)
            .update(new AssetNoticeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetNoticeEntity::getId, id)
        .set(AssetNoticeEntity::getApproveStatus, approveStatus)
        .update(new AssetNoticeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetNoticeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.ASSET_NOTICE.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        List<String> skuIdList = list.stream().map(item -> item.getAssetId()).collect(Collectors.toList());

        List<ProductPurchaseEntity> productPurchaseEntityList = plmTaskFeign.listProductPurchaseBySkuId(skuIdList);
        
        // 批量查询采购开发和采购跟单用户
        Set<String> userIdSet = new HashSet<>();
        list.forEach(data -> {
            if (StringUtils.isNotBlank(data.getPurchaseDevUserId())) {
                userIdSet.add(data.getPurchaseDevUserId());

            }
            if (StringUtils.isNotBlank(data.getPurchaseFollowUserId())) {
                userIdSet.add(data.getPurchaseFollowUserId());
            }
        });
        Map<String, FindUserDTO> userMap = new HashMap<>();
        if (!userIdSet.isEmpty()) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(new ArrayList<>(userIdSet));
            if (CollectionUtils.isNotEmpty(userList)) {
                userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, user -> user, (k1, k2) -> k1));
            }
        }
        
        // 批量查询模具档案获取项目名称
        Set<String> assetCodeSet = list.stream()
                .map(AssetNoticeDTO.ListDTO::getAssetCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, MoldInfoEntity> moldInfoMap = new HashMap<>();
        if (!assetCodeSet.isEmpty()) {
            List<MoldInfoEntity> moldInfoList = plmTaskFeign.listMoldInfoByCodes(new ArrayList<>(assetCodeSet));
            if (CollectionUtils.isNotEmpty(moldInfoList)) {
                moldInfoMap = moldInfoList.stream()
                        .collect(Collectors.toMap(MoldInfoEntity::getCode, mold -> mold, (k1, k2) -> k1));
            }
        }
        
        // 属性赋值
        for(AssetNoticeDTO.ListDTO data : list) {
            List<AssetPurchaseOrderDetailEntity> assetPurchaseOrderDetailEntityList = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getSourceDetailId, data.getDetailId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();
            if (!assetPurchaseOrderDetailEntityList.isEmpty()) {
                BigDecimal realPurchaseQty = assetPurchaseOrderDetailEntityList.stream().map(obj -> obj.getPurchaseQty()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                data.setRealPurchaseQty(realPurchaseQty);
                data.setWaitQty(data.getApplyQty().subtract(realPurchaseQty));
            } else {
                data.setRealPurchaseQty(BigDecimal.ZERO);
                data.setWaitQty(data.getApplyQty());
            }

            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setCreatePoTypeName(CreatePoTypeEnum.getName(data.getCreatePoType()));
            
            // 采购开发用户名称
            if (StringUtils.isNotBlank(data.getPurchaseDevUserId())) {
                FindUserDTO purchaseDevUser = userMap.get(data.getPurchaseDevUserId());
                if (Objects.nonNull(purchaseDevUser)) {
                    data.setPurchaseDevUserName(purchaseDevUser.getUserName());
                }
            }
            // 采购跟单用户名称
            if (StringUtils.isNotBlank(data.getPurchaseFollowUserId())) {
                FindUserDTO purchaseFollowUser = userMap.get(data.getPurchaseFollowUserId());
                if (Objects.nonNull(purchaseFollowUser)) {
                    data.setPurchaseFollowUserName(purchaseFollowUser.getUserName());
                }
            }
            
            // 项目名称从模具档案获取
            if (StringUtils.isNotBlank(data.getAssetCode())) {
                MoldInfoEntity moldInfoEntity = moldInfoMap.get(data.getAssetCode());
                if (Objects.nonNull(moldInfoEntity)) {
                    data.setProjectName(moldInfoEntity.getProjectName());
                }
            }

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && org.apache.commons.lang3.StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(curApprove)) {
                    data.setApproveUserName(curApprove);
                }
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetNoticeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        if(Objects.equals(entity.getInvalidStatus(), Boolean.TRUE)) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetNoticeEntity assetNoticeEntity) {
        //状态默认待提交
        assetNoticeEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
        assetNoticeEntity.setInvalidStatus(Boolean.FALSE);
        //采购员
        if (StringUtils.isNotBlank(assetNoticeEntity.getApplyUserId())) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(assetNoticeEntity.getApplyUserId());

            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            assetNoticeEntity.setApplyUserName(purchaseUser.getUserName());


        }
        // 部门
        if (StringUtils.isNotBlank(assetNoticeEntity.getApplyDeptId())) {
            List<String> depIdList = new ArrayList<>(1);
            depIdList.add(assetNoticeEntity.getApplyDeptId());
            List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(depIdList);
            if (deptList.isEmpty()) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            assetNoticeEntity.setApplyDeptId(deptList.get(0).getId());
            assetNoticeEntity.setApplyDeptName(deptList.get(0).getName());
        }
        // 采购开发用户验证
        if (StringUtils.isNotBlank(assetNoticeEntity.getPurchaseDevUserId())) {
            FindUserDTO purchaseDevUser = sysUserFeign.getUserByUserId(assetNoticeEntity.getPurchaseDevUserId());
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(purchaseDevUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
        }
        // 采购跟单用户验证
        if (StringUtils.isNotBlank(assetNoticeEntity.getPurchaseFollowUserId())) {
            FindUserDTO purchaseFollowUser = sysUserFeign.getUserByUserId(assetNoticeEntity.getPurchaseFollowUserId());
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(purchaseFollowUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(List<AssetNoticeImportExcelDTO> successList, 
                                       List<String> errorNoList, 
                                       List<AssetNoticeImportExcelDTO> errorList2, 
                                       String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 过滤掉错误序号的数据
        if (CollectionUtils.isNotEmpty(errorNoList)) {
            List<AssetNoticeImportExcelDTO> filteredList = successList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSerialNumber()) && !errorNoList.contains(e.getSerialNumber()))
                    .collect(Collectors.toList());
            
            // 将错误序号的数据添加到错误列表
            List<AssetNoticeImportExcelDTO> errorData = successList.stream()
                    .filter(e -> StringUtils.isBlank(e.getSerialNumber()) || errorNoList.contains(e.getSerialNumber()))
                    .collect(Collectors.toList());
            errorList2.addAll(errorData);
            
            successList = filteredList;
        }

        // 查询基础数据
        List<SkuVO> skuVOList = plmTaskFeign.listAssetProduct();
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        // 收集所有的assetCode并去重
        Set<String> assetCodeSet = successList.stream()
                .map(AssetNoticeImportExcelDTO::getAssertCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        // 批量获取供应商信息
        Map<String, com.erp.model.plm.dto.MoldInfoDTO.SupplierInfoByCodeDTO> supplierInfoMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(assetCodeSet)) {
            try {
                supplierInfoMap = plmTaskFeign.batchGetSupplierInfoByCodes(new ArrayList<>(assetCodeSet));
            } catch (Exception e) {
                log.warn("批量获取模具供应商信息失败", e);
            }
        }

        // 按序号分组
        Map<String, List<AssetNoticeImportExcelDTO>> groupedBySerialNumber = successList.stream()
                .collect(Collectors.groupingBy(AssetNoticeImportExcelDTO::getSerialNumber));

        for (Map.Entry<String, List<AssetNoticeImportExcelDTO>> entry : groupedBySerialNumber.entrySet()) {
            List<AssetNoticeImportExcelDTO> value = entry.getValue();
            AssetNoticeImportExcelDTO importMainDTO = value.get(0);
            
            try {
                // 数据校验和转换
                List<String> errorMsgList = new ArrayList<>();
                AssetNoticeDetailDTO.MoldImportDTO moldImportDTO = validateAndConvertData(
                        importMainDTO, value, skuVOList, companyList, deptList, userList, errorMsgList, supplierInfoMap);
                
                // 如果存在错误，添加到错误列表
                if (errorMsgList.size() > 0) {
                    String errorMsg = FieldValidUtil.getMsgSort(errorMsgList);
                    for (AssetNoticeImportExcelDTO dto : value) {
                        dto.setErrorMsg(errorMsg);
                        errorList2.add(dto);
                    }
                    continue;
                }

                // 保存数据
                AssetNoticeEntity entity = new AssetNoticeEntity();
                entity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPL));
                entity.setApplyDate(moldImportDTO.getApplyDate());
                entity.setApplyUserId(moldImportDTO.getApplyUserId());
                entity.setApplyUserName(moldImportDTO.getApplyUserName());
                entity.setApplyDeptId(moldImportDTO.getApplyDeptId());
                entity.setApplyDeptName(moldImportDTO.getApplyDeptName());
                entity.setPurchaseDevUserId(moldImportDTO.getPurchaseDevUserId());
                entity.setPurchaseFollowUserId(moldImportDTO.getPurchaseFollowUserId());
                entity.setInvalidStatus(Boolean.FALSE);
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());

                // 保存主表
                boolean save = super.save(entity);
                if (!save) {
                    throw new ServiceException("开模通知单单头导入保存失败");
                }

                // 处理明细数据
                List<AssetNoticeDetailEntity> assetNoticeDetailEntities = new ArrayList<>();
                for (AssetNoticeDetailDTO.MoldDetailImportDTO moldDetailImportDTO : moldImportDTO.getMoldDetailImportDTOList()) {
                    AssetNoticeDetailEntity assetNoticeDetailEntity = new AssetNoticeDetailEntity();
                    BeanMapperUtils.copy(moldDetailImportDTO, assetNoticeDetailEntity);
                    assetNoticeDetailEntity.setMainId(entity.getId());
                    assetNoticeDetailEntity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
                    // 供应商信息已在validateAndConvertData中处理，这里直接使用
                    assetNoticeDetailEntities.add(assetNoticeDetailEntity);
                }

                // 批量保存明细
                boolean saveDetail = assetNoticeDetailService.saveBatch(assetNoticeDetailEntities);
                if (!saveDetail) {
                    throw new ServiceException("开模通知单明细导入保存失败");
                }

                // 记录操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】",
                        UserContext.getDefaultLoginUser().getUserName(),
                        "开模通知单",
                        entity.getCode());
                moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "导入");
            } catch (Exception e) {
                // 保存失败，添加到错误列表
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 200) {
                    errorMsg = errorMsg.substring(0, 200);
                }
                for (AssetNoticeImportExcelDTO dto : value) {
                    dto.setErrorMsg(errorMsg);
                    errorList2.add(dto);
                }
                log.error("导入第{}条开模通知单失败", importMainDTO.getSerialNumber(), e);
            }
        }
    }

    /**
     * 数据校验和转换
     */
    private AssetNoticeDetailDTO.MoldImportDTO validateAndConvertData(
            AssetNoticeImportExcelDTO importMainDTO,
            List<AssetNoticeImportExcelDTO> value,
            List<SkuVO> skuVOList,
            List<BaseIdDTO> companyList,
            List<SysDepartmentDTO> deptList,
            List<FindUserDTO> userList,
            List<String> errorMsgList,
            Map<String, MoldInfoDTO.SupplierInfoByCodeDTO> supplierInfoMap) {
        
        AssetNoticeDetailDTO.MoldImportDTO moldImportDTO = new AssetNoticeDetailDTO.MoldImportDTO();
        
        // 申请日期
        LocalDate applyDate = parseDate(importMainDTO.getApplyDate());
        if (applyDate == null && StringUtils.isNotBlank(importMainDTO.getApplyDate())) {
            errorMsgList.add("申请日期格式错误");
        }
        moldImportDTO.setApplyDate(applyDate);
        moldImportDTO.setSerialNumber(importMainDTO.getSerialNumber());

        // 申请人
        if (StringUtils.isNotBlank(importMainDTO.getApplyUserName())) {
            FindUserDTO findUserDTO = userList.stream()
                    .filter(obj -> obj.getUserName().equals(importMainDTO.getApplyUserName()))
                    .findFirst()
                    .orElse(null);
            if (findUserDTO == null) {
                errorMsgList.add("请录入申请人信息");
            } else {
                moldImportDTO.setApplyUserId(findUserDTO.getUserId());
                moldImportDTO.setApplyUserName(findUserDTO.getUserName());
            }
        }

        // 申请部门
        if (StringUtils.isNotBlank(importMainDTO.getApplyDeptName())) {
            SysDepartmentDTO sysDepartmentDTO = deptList.stream()
                    .filter(obj -> obj.getName().equals(importMainDTO.getApplyDeptName()))
                    .findFirst()
                    .orElse(null);
            if (sysDepartmentDTO == null) {
                errorMsgList.add("请录入申请部门信息");
            } else {
                moldImportDTO.setApplyDeptId(sysDepartmentDTO.getId());
                moldImportDTO.setApplyDeptName(sysDepartmentDTO.getName());
            }
        }

        // 采购开发用户
        if (StringUtils.isNotBlank(importMainDTO.getPurchaseDevUserName())) {
            FindUserDTO purchaseDevUser = userList.stream()
                    .filter(obj -> obj.getUserName().equals(importMainDTO.getPurchaseDevUserName()))
                    .findFirst()
                    .orElse(null);
            if (purchaseDevUser == null) {
                errorMsgList.add("请录入采购开发用户信息");
            } else {
                moldImportDTO.setPurchaseDevUserId(purchaseDevUser.getUserId());
                moldImportDTO.setPurchaseDevUserName(purchaseDevUser.getUserName());
            }
        }

        // 采购跟单用户
        if (StringUtils.isNotBlank(importMainDTO.getPurchaseFollowUserName())) {
            FindUserDTO purchaseFollowUser = userList.stream()
                    .filter(obj -> obj.getUserName().equals(importMainDTO.getPurchaseFollowUserName()))
                    .findFirst()
                    .orElse(null);
            if (purchaseFollowUser == null) {
                errorMsgList.add("请录入采购跟单用户信息");
            } else {
                moldImportDTO.setPurchaseFollowUserId(purchaseFollowUser.getUserId());
                moldImportDTO.setPurchaseFollowUserName(purchaseFollowUser.getUserName());
            }
        }

        // 处理明细数据
        List<AssetNoticeDetailDTO.MoldDetailImportDTO> detailList = new ArrayList<>();
        for (AssetNoticeImportExcelDTO importExcelDTO : value) {
            AssetNoticeDetailDTO.MoldDetailImportDTO detail = new AssetNoticeDetailDTO.MoldDetailImportDTO();
            
            // 采购组织
            if (StringUtils.isNotBlank(importExcelDTO.getPurchaseOrgName())) {
                BaseIdDTO baseIdDTO = companyList.stream()
                        .filter(obj -> obj.getName().equals(importExcelDTO.getPurchaseOrgName()))
                        .findFirst()
                        .orElse(null);
                if (baseIdDTO == null) {
                    errorMsgList.add("请录入启用采购组织");
                } else {
                    detail.setPurchaseOrgId(baseIdDTO.getId());
                    detail.setPurchaseOrgName(importExcelDTO.getPurchaseOrgName());
                }
            }

            // 模具信息
            if (StringUtils.isNotBlank(importExcelDTO.getAssertCode())) {
                SkuVO skuVO = skuVOList.stream()
                        .filter(obj -> obj.getSkuNo().equals(importExcelDTO.getAssertCode()))
                        .findFirst()
                        .orElse(null);
                if (skuVO == null) {
                    errorMsgList.add("请录入启用的模具信息");
                } else {
                    detail.setAssetId(skuVO.getSkuId());
                    detail.setAssetCode(skuVO.getSkuNo());
                    detail.setAssetName(skuVO.getSkuName());
                }
            }

            // 供应商信息处理
            if (StringUtils.isNotBlank(importExcelDTO.getSupplierName())) {
                // 如果Excel中有供应商名称，根据名称查找供应商
                List<SupplierEntity> supplierList = suppliserService.listBySupplierByNames(Arrays.asList(importExcelDTO.getSupplierName()));
                if (CollectionUtils.isEmpty(supplierList)) {
                    errorMsgList.add("请录入有效的供应商名称");
                } else {
                    SupplierEntity supplierEntity = supplierList.get(0);
                    detail.setSupplierId(supplierEntity.getId());
                    detail.setSupplierName(supplierEntity.getName());
                }
            } else {
                // 如果Excel中没有供应商名称，从批量获取的供应商信息中获取
                if (StringUtils.isNotBlank(detail.getAssetCode())) {
                    MoldInfoDTO.SupplierInfoByCodeDTO supplierInfo = supplierInfoMap.get(detail.getAssetCode());
                    if (supplierInfo != null && StringUtils.isNotBlank(supplierInfo.getSupplierId())) {
                        detail.setSupplierId(supplierInfo.getSupplierId());
                        detail.setSupplierName(supplierInfo.getSupplierName());
                    } else {
                        errorMsgList.add("模具档案中未维护供应商信息，请手动录入供应商名称");
                    }
                }
            }

            // 其他字段
            detail.setPlanDeliveryDate(parseDate(importExcelDTO.getPlanDeliveryDateStr()));
            if (StringUtils.isNotBlank(importExcelDTO.getApplyQtyStr())) {
                try {
                    detail.setApplyQty(new BigDecimal(importExcelDTO.getApplyQtyStr()));
                } catch (Exception e) {
                    errorMsgList.add("申请数量格式错误");
                }
            }
            if (StringUtils.isNotBlank(importExcelDTO.getIsUrgentName())) {
                detail.setIsUrgent("是".equals(importExcelDTO.getIsUrgentName()) ? Boolean.TRUE : Boolean.FALSE);
            }
            detail.setRemark(importExcelDTO.getRemark());
            
            detailList.add(detail);
        }
        
        moldImportDTO.setMoldDetailImportDTOList(detailList);
        return moldImportDTO;
    }

    /**
     * 日期解析
     */
    private LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e1) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    public void hanleAddDetailData(AssetNoticeEntity assetNoticeEntity,List<AssetNoticeDetailDTO.AddDTO> assetNoticeDetailDTO){
        LocalDate today = LocalDate.now();
        for (AssetNoticeDetailDTO.AddDTO addDTO : assetNoticeDetailDTO) {
            LocalDate planDeliveryDate = addDTO.getPlanDeliveryDate();
            if (planDeliveryDate == null) {
                continue;
            }

            // 校验日期必须 ≥ 今天
            if (planDeliveryDate.isBefore(today)) {
                throw new ServiceException(
                        "开模通知单模具编码【{}】的预计交货日期【{}】预计交货日期不能小于",
                        assetNoticeEntity.getCode(),
                        planDeliveryDate
                );
            }
        }

    }

    public void hanleUpdateDetailData(AssetNoticeEntity assetNoticeEntity,List<AssetNoticeDetailDTO.UpdateDTO> assetNoticeDetailDTO){
        LocalDate today = LocalDate.now();
        for (AssetNoticeDetailDTO.UpdateDTO updateDTO : assetNoticeDetailDTO) {
            LocalDate planDeliveryDate = updateDTO.getPlanDeliveryDate();
            if (planDeliveryDate == null) {
                continue;
            }

            // 校验日期必须 ≥ 今天
            if (planDeliveryDate.isBefore(today)) {
                throw new ServiceException(
                        "开模通知单模具编码【{}】的预计交货日期【{}】预计交货日期不能小于",
                        assetNoticeEntity.getCode(),
                        planDeliveryDate
                );
            }
        }

    }
}
