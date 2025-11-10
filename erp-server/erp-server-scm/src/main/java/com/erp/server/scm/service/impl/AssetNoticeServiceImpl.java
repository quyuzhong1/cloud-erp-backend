package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.excel.MoldInfoImportExcelDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.model.scm.enums.AssetNoticeTabListEnum;
import com.erp.model.scm.enums.AssetPurchaseOrderTypeEnum;
import com.erp.model.plm.enums.MoldInfoTagEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
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
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Stream;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
                addDTO.setPurchaseUserName(value.get(0).getPurchaseUserId());
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
                //联系人名称
                supplierDTO.setContactName(supplier.getPerson());
                //联系电话
                supplierDTO.setContactTelNumber(supplier.getTelNumber());
            }
            List<SupplierAccountDTO.UpdateDTO> supplierAccountList = supplierAccountService.getBySupplierId(supplierId);
            SupplierAccountDTO.UpdateDTO supplierAccount = supplierAccountList.stream().filter(obj -> obj.getIsDefault().equals(Boolean.TRUE)).findFirst().get();
            //供应商默认账户
            if (Objects.nonNull(supplierAccount)) {
                supplierDTO.setBankName(supplierAccount.getBankSubbranch());
                supplierDTO.setBankAccount(supplierAccount.getBankAccount());
                supplierDTO.setPayee(supplierAccount.getPayee());
            }

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
            throw new ServiceException(ApiError.ERROR_95305);
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
            throw new ServiceException(ApiError.ERROR_95308);
        }

        List<String> collect = list.stream().map(obj -> obj.getId()).collect(Collectors.toList());

        //删除明细
        assetNoticeDetailService.removeByIds(collect);

        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);

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
        //查询所有审核通过的模具
        List<SkuVO> skuVOList = plmTaskFeign.listAssetProduct();

        //查询所有启用核算公司
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();

        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //设置操作人
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
        AssetNoticeExcelListener excelListenerUtil = new AssetNoticeExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),skuVOList,userList,deptList,companyList);
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
        return data;
    }

    public void fillViewList(List<AssetNoticeDetailDTO.ViewDTO> dtoList){

        for (AssetNoticeDetailDTO.ViewDTO detailDTO : dtoList) {
            detailDTO.setTagName(MoldInfoTagEnum.getName(detailDTO.getTag()));
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

        List<String> collect = list.stream().map(item -> item.getSupplierId()).collect(Collectors.toList());

        Map<String, SupplierDTO.SupplierSimpleDTO> supplierSimpleInfo = suppliserService.getSupplierSimpleInfo(collect);
        // 属性赋值
        for(AssetNoticeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setCreatePoTypeName(CreatePoTypeEnum.getName(data.getCreatePoType()));
            if (StringUtils.isNotBlank(data.getSupplierId())) {
                SupplierDTO.SupplierSimpleDTO supplierSimpleDTO = supplierSimpleInfo.get(data.getSupplierId());
                data.setSupplierName(StringUtils.isNotBlank(supplierSimpleDTO.getName()) ? supplierSimpleDTO.getName() : null);
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

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleImportSuccessList(List<AssetNoticeDetailDTO.MoldImportDTO> successList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 按 serialNumber 分组
        Map<String, List<AssetNoticeDetailDTO.MoldImportDTO>> groupedBySerialNumber = successList.stream()
                .collect(Collectors.groupingBy(AssetNoticeDetailDTO.MoldImportDTO::getSerialNumber));

        try {
            for (Map.Entry<String, List<AssetNoticeDetailDTO.MoldImportDTO>> entry : groupedBySerialNumber.entrySet()) {
                String serialNumber = entry.getKey();
                List<AssetNoticeDetailDTO.MoldImportDTO> moldImportDTOList = entry.getValue();

                if (CollectionUtils.isEmpty(moldImportDTOList)) {
                    continue;
                }

                // 取第一个元素作为主表数据
                AssetNoticeDetailDTO.MoldImportDTO firstMoldImportDTO = moldImportDTOList.get(0);
                AssetNoticeEntity entity = new AssetNoticeEntity();
                entity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPL));
                entity.setApplyDate(firstMoldImportDTO.getApplyDate());
                entity.setApplyUserId(firstMoldImportDTO.getApplyUserId());
                entity.setApplyUserName(firstMoldImportDTO.getApplyUserName());
                entity.setApplyDeptId(firstMoldImportDTO.getApplyDeptId());
                entity.setApplyDeptName(firstMoldImportDTO.getApplyDeptName());
                entity.setInvalidStatus(Boolean.FALSE);
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());

                // 保存主表
                boolean save = super.save(entity);
                if (!save) {
                    throw new ServiceException("开模通知单单头导入保存失败");
                }

                // 处理明细数据
                List<AssetNoticeDetailEntity> assetNoticeDetailEntities = new ArrayList<>();
                for (AssetNoticeDetailDTO.MoldImportDTO moldImportDTO : moldImportDTOList) {
                    List<AssetNoticeDetailDTO.MoldDetailImportDTO> moldDetailImportDTOList = moldImportDTO.getMoldDetailImportDTOList();
                    for (AssetNoticeDetailDTO.MoldDetailImportDTO moldDetailImportDTO : moldDetailImportDTOList) {
                        AssetNoticeDetailEntity assetNoticeDetailEntity = new AssetNoticeDetailEntity();
                        BeanMapperUtils.copy(moldDetailImportDTO, assetNoticeDetailEntity);
                        assetNoticeDetailEntity.setMainId(entity.getId()); // 关联主表ID
                        assetNoticeDetailEntity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
                        assetNoticeDetailEntities.add(assetNoticeDetailEntity);
                    }
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
            }
        } catch (Exception e) {
            throw new ServiceException("开模通知单导入保存失败", e);
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
