package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
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
import com.common.business.constant.ApproveType;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.utils.PdfUtil;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchaseEndReceiveImportExcelDTO;
import com.erp.model.scm.dto.excel.PurchaseOrderImportExcelDTO;
import com.erp.model.scm.dto.excel.PurchaseOrderMainExcelDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InstockForcastDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryCloseRecordFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.WarehouseLocationFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseOrderService;
import com.erp.server.scm.listener.PurchaseEndReceiveExcelListener;
import com.erp.server.scm.listener.PurchaseOrderExcelListener;
import com.erp.server.scm.listener.PurchaseOrderMainExcelListener;
import com.erp.server.scm.mapper.PurchaseOrderMapper;
import com.erp.server.scm.query.PurchaseOrderQueryHandler;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.constant.ThirdConstants.PURCHASE_ORDER_DETAIL;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_ORDER;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_ORDER_ADJUST;

/**
 * <p>
 * 采购订单表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@Service
public class PurchaseOrderServiceImpl extends SuperServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity> implements PurchaseOrderService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SyncKingdeePurchaseOrderService syncKingdeePurchaseOrderService;

    @Autowired
    private InventoryFeign inventoryFeign;

    @Autowired
    private PurchaseApplicationService purchaseApplicationService;

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private WarehouseLocationFeign warehouseLocationFeign;

    @Autowired
    private SubcontractOrderService subcontractOrderService;

    @Autowired
    private PurchaseChangeService purchaseChangeService;

    @Autowired
    private SrmCfgSettingFeign srmCfgSettingFeign;

    @Resource
    private PurchaseOrderQueryHandler purchaseOrderQueryHandler;

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryOrderFeign;

    @Resource
    private InventoryCloseRecordFeign inventoryCloseRecordFeign;

    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;

    @Resource
    private SupplierUserService supplierUserService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;
    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private FileTemplateFeign fileTemplateFeign;
    @Resource
    private SupplierAccountService supplierAccountService;
    @Resource
    private SupplierPurchaseQuantityService supplierPurchaseQuantityService;
    @Autowired
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;
    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    @Lazy
    private PurchaseOrderService selfService;


    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> paging(PagingDTO<PurchaseOrderDTO.SearchParamDTO> pagingDTO) {
        PurchaseOrderDTO.SearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<PurchaseOrderDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        doOpHandlePurchaseOrder(records);
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> srmOrderConfirmPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> pagingDTO) {
        PurchaseOrderDTO.SrmSearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        query.setOrders(buildOrders(pagingDTO.getParams().getSortList()));
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.srmPaging(query, params);
        List<PurchaseOrderDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        buildPurchaseOrderCount(records);
        return new PagingVO(pageData);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderEntity add(PurchaseOrderDTO.AddDTO dto) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(entity);
        log.info("采购订单新增");
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_PO);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购订单【%s】", entity.getCode()), ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "新增操作");
            //新增供应商信息
            purchaseOrderSupplierService.add(dto.getPurchaseOrderSupplierDTO(), entity.getId());
            //新增明细
            purchaseOrderDetailService.add(dto.getDetails(), entity.getId());
            //同步到WMS
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_TAG.getName(),Arrays.asList(entity), IdUtil.simpleUUID());
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseOrderDTO.UpdateDTO dto) {
        PurchaseOrderEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        if (StringUtils.isNotBlank(old.getSubcontractType())) {
            throw new ServiceException(ApiError.ERROR_98079);
        }

        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(entity);

        log.info("采购订单修改，id=【{}】", dto.getId());

        //操作日志
        moduleOperateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //供应商数据
        purchaseOrderSupplierService.update(dto.getPurchaseOrderSupplierDTO(), entity.getId());
        //更新明细数据
        purchaseOrderDetailService.update(dto.getDetails(), entity.getId());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_TAG.getName(),Arrays.asList(entity), IdUtil.simpleUUID());

        return Boolean.TRUE;
    }

    @Override
    public PurchaseOrderDTO.ViewDTO view(String id) {
        PurchaseOrderDTO.ViewDTO dto = new PurchaseOrderDTO.ViewDTO();

        //主表信息
        PurchaseOrderEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        BeanMapperUtils.copy(entity, dto);

        //能否编辑
        dto.setCanEdit(purchaseApplicationRefPoService.getPurchaseApplicationByPurchaseOrderId(id));
        //供应商账号信息
        if (CharSequenceUtil.isNotBlank(dto.getSupplierAccountId())) {
            SupplierAccountEntity supplierAccountEntity = supplierAccountService.getById(dto.getSupplierAccountId());
            if (ObjectUtils.isNotEmpty(supplierAccountEntity)) {
                dto.setPayee(supplierAccountEntity.getPayee());
                dto.setBankAccount(supplierAccountEntity.getBankAccount());
                if (CharSequenceUtil.isNotBlank(supplierAccountEntity.getBankName())) {
                    dto.setBankName(supplierAccountEntity.getBankName());
                } else if (CharSequenceUtil.isNotBlank(supplierAccountEntity.getBankId())) {
                    List<BaseIdDTO> bankList = sysUserFeign.getBankList(Collections.singletonList(supplierAccountEntity.getBankId()));
                    dto.setBankName(CollUtil.isNotEmpty(bankList) ? bankList.get(0).getName() : "");
                }
            }
        }
        // 采购员名称
        if (StrUtils.isNotEmpty(dto.getPurchaseUserId())) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(dto.getPurchaseUserId());
            if (ObjectUtils.isEmpty(purchaseUser)) {
                dto.setPurchaseUserName(purchaseUser.getUserName());
            }
        }
        //供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        PurchaseOrderSupplierDTO.UpdateDTO supplierUpdateDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        if (ObjectUtils.isEmpty(supplierUpdateDTO)) {
            throw new ServiceException(ApiError.ERROR_98031);
        }
        BeanMapperUtils.copy(purchaseOrderSupplierEntity, supplierUpdateDTO);

        //结算方式
        DictBasicEntity payMethod = dictBasicService.getById(supplierUpdateDTO.getPayMethodId());
        if (ObjectUtils.isNotEmpty(payMethod)) {
            supplierUpdateDTO.setPayMethodName(payMethod.getName());
        }
        //结算币种
        if (StringUtils.isNotBlank(supplierUpdateDTO.getPayCurrency())) {
            List<CurrencyDTO.ViewDTO> viewDTOS = sysUserFeign.listByCurrency(Collections.singletonList(supplierUpdateDTO.getPayCurrency()));
            if (CollectionUtils.isNotEmpty(viewDTOS)) {
                supplierUpdateDTO.setPayCurrencyName(viewDTOS.get(0).getName());
            }
        }
        // 采购订单供应商付款条件
        String paymentConditionCode = supplierUpdateDTO.getPaymentCondition();
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.nonNull(paymentCondition)) {
                supplierUpdateDTO.setPaymentConditionName(paymentCondition.getName());
            }
        }
        dto.setPurchaseOrderSupplierDTO(supplierUpdateDTO);

        //单据类型
        List<DictBasicDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.PURCHASE_ORDER_TYPE.getType());
        String typeName = dictBasicList.stream().filter(obj -> obj.getValue().equals(entity.getType())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        dto.setTypeName(typeName);

        //明细信息
        List<PurchaseOrderDetailEntity> entityDetails = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<PurchaseOrderDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(PurchaseOrderDetailDTO.UpdateDTO.class, entityDetails);
        details.forEach(obj -> {
            obj.setFirstMassProductName(FirstMassProductTypeEnum.getName(obj.getFirstMassProduct()));
            obj.setTaxRate(MathUtil.multiplyWithTwo(obj.getTaxRate(), MathUtil.BigDecimal_100));
            obj.setExecutionStatusName(ExecutionStatusEnum.getNameByCode(obj.getExecutionStatus()));
        });
        dto.setDetails(details);
        PurchaseOrderDetailDTO.UpdateDTO updateDTO = details.stream().filter(e -> StringUtils.isNotBlank(e.getExecutionStatusName())).findFirst().orElse(null);
        if (Objects.nonNull(updateDTO)) {
            dto.setExecutionStatus(updateDTO.getExecutionStatus());
            dto.setExecutionStatusName(updateDTO.getExecutionStatusName());
        }
        List<String> podIds = entityDetails.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //获取收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        //获取已审核收货信息
        if (CollectionUtils.isNotEmpty(receiveDetailList)) {
            receiveDetailList = receiveDetailList.stream().filter(obj -> ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).collect(Collectors.toList());

        }
        //审核状态
        dto.setApproveStatusName(ApproveStatusEnum.getName(dto.getApproveStatus()));
        //流程信息
        List<PurchaseOrderProcessDTO> processList = new ArrayList<>();
        PurchaseOrderProcessOperationEnum[] values = PurchaseOrderProcessOperationEnum.values();
        for (PurchaseOrderProcessOperationEnum item : values) {
            PurchaseOrderProcessDTO processDTO = new PurchaseOrderProcessDTO();
            processDTO.setOperation(item.getName());
            processDTO.setIsArrive(Boolean.FALSE);
            //创建
            if (PurchaseOrderProcessOperationEnum.CREATE.getCode().equals(item.getCode())) {
                processDTO.setUserName(entity.getCreateUserName());
                processDTO.setTime(entity.getCreateTime());
                processDTO.setIsArrive(Boolean.TRUE);
            }
            //审核
            if (PurchaseOrderProcessOperationEnum.APPROVE.getCode().equals(item.getCode())) {
                if (StringUtils.isNotBlank(entity.getApproveUserName())) {
                    processDTO.setIsArrive(Boolean.TRUE);
                }
                processDTO.setUserName(entity.getApproveUserName());
                processDTO.setTime(entity.getApproveTime());
            }
            //签收
            if (PurchaseOrderProcessOperationEnum.RECEIVE.getCode().equals(item.getCode())) {
                long count = entityDetails.stream().filter(obj ->
                        ExecutionStatusEnum.DELIVERY.getCode().equals(obj.getExecutionStatus())
                                || ExecutionStatusEnum.FINISH.getCode().equals(obj.getExecutionStatus())
                                || ExecutionStatusEnum.CLOSED.getCode().equals(obj.getExecutionStatus())
                ).count();
                if (count > 0) {
                    processDTO.setIsArrive(Boolean.TRUE);
                }
                //取开始一条
                if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                    WarehouseReceiveDetailEntity detailEntity = receiveDetailList.get(0);
                    processDTO.setUserName(detailEntity.getApproveUserName());
                    processDTO.setTime(detailEntity.getApproveTime());
                }
            }
            //签收完成
            if (PurchaseOrderProcessOperationEnum.FINISH_RECEIVE.getCode().equals(item.getCode())) {
                long count = entityDetails.stream().filter(obj ->
                        !ExecutionStatusEnum.FINISH.getCode().equals(obj.getExecutionStatus())
                                && !ExecutionStatusEnum.CLOSED.getCode().equals(obj.getExecutionStatus())
                ).count();
                if (count > 0) {
                    processDTO.setIsArrive(Boolean.FALSE);
                } else {
                    processDTO.setIsArrive(Boolean.TRUE);
                }
                //取最后一条
                if (processDTO.getIsArrive()) {
                    if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                        WarehouseReceiveDetailEntity detailEntity = receiveDetailList.get(receiveDetailList.size() - 1);
                        processDTO.setUserName(detailEntity.getApproveUserName());
                        processDTO.setTime(detailEntity.getApproveTime());
                    }
                }
            }
            processList.add(processDTO);
        }
        dto.setProcess(processList);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO delete(PurchaseOrderEntity entity) {
        //待提交允许删除
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());

        log.info("采购申请单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除供应商数据
        purchaseOrderSupplierService.deleteByPurchaseOrderIds(ids);
        //删除明细数据
        purchaseOrderDetailService.removeByPurchaseOrderIds(ids);
        //同步到WMS
//        List<PurchaseOrderEntity> entityList = lambdaQuery().in(PurchaseOrderEntity::getId, ids).list();
//        entityList.forEach(req -> req.setIsDeleted(Boolean.TRUE));
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_TAG.getName(), entityList, IdUtil.simpleUUID());
        //删除主表数据
        this.removeByIds(ids);
        //更新采购申请单的生成状态
        updateCreatePoType(ids);
        //删除采购申请单和订单关联表数据
        purchaseApplicationRefPoService.removeByPurchaseOrderIds(ids);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);

        //发送金蝶
        sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        PurchaseOrderEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = dto.getType();

        log.info("采购订单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(dto.getId()));

        //调用审核流程
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "采购订单", approveType.getName(), dto.getComment());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(ApproveOneDTO dto, PurchaseOrderEntity entity) {
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        Boolean result = this.updateApproveStatusForApprove(Collections.singletonList(entity.getId()), approveStatus.getStatus());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (dto.getType().equals(ApproveType.PASS)) {
            // 更新库存信息（生成入库预报）
            updateInventoryTransCore(Collections.singletonList(entity));
            // 填入首批下单时间
            setFirstPlaceOrder(Collections.singletonList(entity.getId()));
            //发送金蝶
            sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }


    private void setFirstPlaceOrder(List<String> ids) {
        //填入首批下单时间
        List<FirstPlaceOrderDTO> firstPlaceOrderList = baseMapper.listFirstPlaceOrderDate(ids);
        List<String> skuIds = firstPlaceOrderList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductPurchaseEntity> productPurchaseEntities = plmTaskFeign.listProductPurchaseBySkuId(skuIds);
        //获取到没有设置首批下单时间的sku
        List<String> skuIdList = productPurchaseEntities.stream().filter(req -> req.getPlaceOrderTime() == null).map(req -> req.getSkuId()).collect(Collectors.toList());
        List<ProductPurchaseEntity> purchaseEntityList = new ArrayList<>();
        for (String skuId : skuIdList) {
            FirstPlaceOrderDTO firstPlaceOrderDTO = firstPlaceOrderList.stream().filter(req -> req.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(firstPlaceOrderDTO)) {
                ProductPurchaseEntity purchaseEntity = new ProductPurchaseEntity();
                purchaseEntity.setSkuId(skuId);
                purchaseEntity.setPlaceOrderTime(firstPlaceOrderDTO.getPurchaseDate());
                purchaseEntityList.add(purchaseEntity);
            }
        }
        plmTaskFeign.updateProductPlaceOrderTimeBatch(purchaseEntityList);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        PurchaseOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到采购订单数据"));

        // 已审核支持反审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }

        List<String> podIds = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //验证有没有下推收货单据
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        if (CollectionUtils.isNotEmpty(receiveDetailList)) {
            throw new ServiceException(ApiError.ERROR_98055);
        }
        //验证有没有下推采购入库单据
        List<PoInstockDetailEntity> purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
            throw new ServiceException(ApiError.ERROR_98056);
        }
        //验证有没有下推变更单
        List<PurchaseChangeEntity> purchaseChangeList = purchaseChangeService.listByPoIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(purchaseChangeList)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN_CHANGE);
        }

        //验证与没有下推送货单
        List<DeliveryOrderDetailEntity> deliveryOrderDetailList = srmDeliveryOrderFeign.listDetailByDetailSourceIds(podIds);
        if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DELIVERY);
        }

        //送货中、已完成、已关闭不能反审核,但是上面验证了下推收货单据则只需要验证已关闭即可
        long closeCount = purchaseOrderDetailList.stream().filter(obj -> ExecutionStatusEnum.CLOSED.getCode().equals(obj.getExecutionStatus())).count();
        if (closeCount > 0) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_DISAPPROVE_CLOSE);
        }

        //验证存货核算是否关账
        /*List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = Arrays.asList(new InventoryClosedRecordDTO.ClosedParamDTO(entity.getPurchaseOrgId(), entity.getPurchaseDate()),
                new InventoryClosedRecordDTO.ClosedParamDTO(entity.getReceiveOrgId(), entity.getPurchaseDate()));
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);*/

        log.info("采购订单反审核，id=【{}】", JSONUtil.toJsonStr(id));

        //更新单据为待提交
        updateApproveStatus(Arrays.asList(id), ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //更新单据明细的执行状态为待确认
        purchaseOrderDetailService.updateExecutionStatus(Arrays.asList(id), ExecutionStatusEnum.TO_BE_CONFIRM);

        // 反审核更新库存数据（采购订单生成的入库预报）
        inventoryFeign.purchaseOrderUnApproveBatch(Arrays.asList(id));

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "采购订单");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "反审核操作");

        //发送金蝶
        sendPushTask(Arrays.asList(entity), SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(PurchaseOrderEntity entity) {
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        log.info("采购订单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.PURCHASE_ORDER.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //更新单据为待提交
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购订单【%s】取消流程", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "取消流程操作");
        //同步到WMS
//        List<PurchaseOrderEntity> toWmsList = this.getList(ids);
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_TAG.getName(), toWmsList, IdUtil.simpleUUID());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    public PurchaseOrderDTO.ExportPdfDTO listPurchaseContractPdf(String id) {
        PurchaseOrderDTO.ExportPdfDTO exportPdfDTO = new PurchaseOrderDTO.ExportPdfDTO();

        PurchaseOrderEntity purchaseOrderEntity = this.getById(id);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }

        List<PurchaseOrderDetailEntity> list = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //主数据处理
        exportPdfDTO.setCode(purchaseOrderEntity.getCode());
        exportPdfDTO.setCodeStr("合同号：" + purchaseOrderEntity.getCode());
        //采购组织
        exportPdfDTO.setPurchaseOrgName(purchaseOrderEntity.getPurchaseOrgName());
        //甲方签收日期
        exportPdfDTO.setFirstSignDate(purchaseOrderEntity.getCreateTime().toLocalDate());
        //乙方签收日期
        exportPdfDTO.setSecondSignDate(purchaseOrderEntity.getCreateTime().toLocalDate());

        //查询订单供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplier = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        if (ObjectUtils.isEmpty(purchaseOrderSupplier)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        exportPdfDTO.setSupplierTel(purchaseOrderSupplier.getContactTelNumber());
        // 采购订单供应商付款条件
        String paymentConditionCode = purchaseOrderSupplier.getPaymentCondition();
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.nonNull(paymentCondition)) {
                exportPdfDTO.setPaymentConditionName(paymentCondition.getName());
            }
        }

        //结算方式
        DictBasicEntity payMethod = dictBasicService.getById(purchaseOrderSupplier.getPayMethodId());
        if (ObjectUtils.isNotEmpty(payMethod)) {
            exportPdfDTO.setPayMethodName(payMethod.getName());
        }

        //原供应商信息
        SupplierEntity supplier = supplierService.getById(purchaseOrderSupplier.getSupplierId());
        if (ObjectUtils.isEmpty(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商账号信息
        String supplierBankNo = "";
        String supplierBankName = "";
        String supplierAccountName = "";
        if (CharSequenceUtil.isNotBlank(purchaseOrderEntity.getSupplierAccountId())) {
            SupplierAccountEntity supplierAccountEntity = supplierAccountService.getById(purchaseOrderEntity.getSupplierAccountId());
            supplierBankNo = Objects.nonNull(supplierAccountEntity) ? supplierAccountEntity.getBankAccount() : "";
            supplierBankName = Objects.nonNull(supplierAccountEntity) ? supplierAccountEntity.getBankName() : "";
            supplierAccountName = Objects.nonNull(supplierAccountEntity) ? supplierAccountEntity.getPayee() : "";
        }
        exportPdfDTO.setSupplierBankName(supplierBankName);
        exportPdfDTO.setSupplierBankNo(supplierBankNo);
        exportPdfDTO.setSupplierAccountName(supplierAccountName);

        exportPdfDTO.setSupplierName(supplier.getName());
        exportPdfDTO.setSupplierAddress(supplier.getCompanyAddress());


        //供应商联系人信息
        if (StringUtils.isNotBlank(purchaseOrderSupplier.getSupplierContactId())) {
            SupplierContactEntity supplierContact = supplierContactService.getById(purchaseOrderSupplier.getSupplierContactId());
            if (ObjectUtils.isEmpty(supplierContact)) {
                throw new ServiceException(ApiError.ERROR_98039);
            }
            exportPdfDTO.setSupplierEmail(supplierContact.getEmail());
            exportPdfDTO.setSupplierContract(supplierContact.getPerson());
        }

        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(purchaseOrderEntity.getDeliveryWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.get(0);
        exportPdfDTO.setDeliveryWarehouseAddress(warehouseDTO.getAddress());
        exportPdfDTO.setDeliveryWarehouseTel(warehouseDTO.getContactTelNumber());
        exportPdfDTO.setDeliveryWarehouseContract(warehouseDTO.getContacts());
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        DecimalFormat df4 = new DecimalFormat("#,##0.0000");
        //明细物料信息
        List<PurchaseOrderDetailDTO.ExportPdfDTO> details = new ArrayList<>();
        for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : list) {
            PurchaseOrderDetailDTO.ExportPdfDTO detailDTO = new PurchaseOrderDetailDTO.ExportPdfDTO();
            BeanMapperUtils.copy(purchaseOrderDetailEntity, detailDTO);
            //明细数据处理
            detailDTO.setUnitName("个");
            //不含税单价（不含税价格=含税价格/（1+增值税税率））
            detailDTO.setPrice(MathUtil.divide(detailDTO.getTaxPrice(), MathUtil.add(BigDecimal.ONE, detailDTO.getTaxRate())));
            //不含税单价 增加千分位分割
            detailDTO.setPriceStr(df4.format(detailDTO.getPrice()));
            //含税金额 增加千分位分割
            detailDTO.setTaxPriceStr(df4.format(detailDTO.getTaxPrice()));
            //不含税金额
            detailDTO.setNotTaxPurchaseAmount(MathUtil.multiplyWithTwo(detailDTO.getPrice(), detailDTO.getPurchaseQty()).setScale(2, RoundingMode.HALF_UP));
            //不含税金额 增加千分位分割
            detailDTO.setNotTaxPurchaseAmountStr(df2.format(detailDTO.getNotTaxPurchaseAmount()));
            //含税金额 增加千分位分割
            detailDTO.setPurchaseAmountStr(df2.format(detailDTO.getPurchaseAmount()));
            detailDTO.setTaxRate(MathUtil.multiplyWithTwo(detailDTO.getTaxRate(), MathUtil.BigDecimal_100));
            details.add(detailDTO);
        }
        //含税金额合计
        BigDecimal totalAmount = details.stream().map(PurchaseOrderDetailDTO.ExportPdfDTO::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        exportPdfDTO.setTotalAmount(totalAmount);
        //含税金额合计  增加千分位分割
        exportPdfDTO.setTotalAmountStr(df2.format(totalAmount));
        //不含税金额合计
        BigDecimal totalNotTaxAmount = details.stream().map(PurchaseOrderDetailDTO.ExportPdfDTO::getNotTaxPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        exportPdfDTO.setTotalNotTaxAmount(totalNotTaxAmount);
        //不含税金额合计  增加千分位分割
        exportPdfDTO.setTotalNotTaxAmountStr(df2.format(totalNotTaxAmount));
        String currency = list.get(0).getCurrency();
        String currencyName = "";
        if (StrUtil.isNotBlank(currency)) {
            currencyName = CurrencyEnum.getNameByCode(currency);
        }
        //将totalNotTaxAmount转换为中文大写
        String totalNotTaxAmountChinese = Convert.digitToChinese(totalAmount.doubleValue());
        exportPdfDTO.setTotalNotTaxAmountChinese(currencyName + totalNotTaxAmountChinese);
        exportPdfDTO.setCurrency(currency);
        exportPdfDTO.setDetails(details);
        return exportPdfDTO;
    }

    @Override
    public PurchaseOrderDetailDTO.ImportDTO importFile(ExcelImportDTO.purchaseOrderExcelImportDTO excelImportDTO, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();

        PurchaseOrderExcelListener excelListenerUtil = new PurchaseOrderExcelListener(skuList, excelImportDTO.getSkuIds());

        try {
            EasyExcel.read(excelImportDTO.getExcelFile().getInputStream(), PurchaseOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseOrderImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        PurchaseOrderDetailDTO.ImportDTO importDTO = new PurchaseOrderDetailDTO.ImportDTO();
        //导入数据处理
        List<PurchaseOrderDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseOrderImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理未查询到报价的SKU
        doOpHandleNotExistPrice(excelDateList, successList, errorList, excelImportDTO.getSupplierId(), excelImportDTO.getPurchaseOrgId());

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购订单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchaseOrderImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public Boolean exportExcel(PurchaseOrderDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购订单数据", EXPORT_SCM_PURCHASE_ORDER.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(PurchaseOrderEntity entity, Boolean isStartProcess) {
        //待提交或审核不通过并且未作废允许提交
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //校验必填信息
        checkRequiredData(Collections.singletonList(entity), Collections.singletonList(entity.getId()));

        log.info("采购订单提交，id=【{}】", JSONUtil.toJsonStr(entity.getId()));
        if (isStartProcess) {
            //提交流程
            startProcess(entity);
        }
        //更新审核状态
        updateApproveStatus(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购订单【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO addAndSubmit(PurchaseOrderDTO.AddDTO dto) {
        //新增
        PurchaseOrderEntity purchaseOrderEntity = this.add(dto);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        purchaseOrderEntity = this.getById(purchaseOrderEntity.getId());
        //提交
        return this.submit(purchaseOrderEntity, Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseOrderDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        PurchaseOrderEntity entity = this.getById(dto.getId());
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    public List<ListStatusCountDTO.PurchaseOrderCountDTO> listCount(PermissionsDTO dto) {
        PoTableFlagEnum[] values = PoTableFlagEnum.values();
        List<ListStatusCountDTO.PurchaseOrderCountDTO> list = new ArrayList<>();
        for (PoTableFlagEnum item : values) {
            PurchaseOrderDTO.SearchParamDTO searchParamDTO = new PurchaseOrderDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            ListStatusCountDTO.PurchaseOrderCountDTO resultDTO = new ListStatusCountDTO.PurchaseOrderCountDTO();
            String tabSql = purchaseOrderQueryHandler.getTabSql(item.getCode());
            HashMap<String, String> map = new HashMap<>();
            map.put("default", tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO invalid(PurchaseOrderEntity entity, String reason) {
        //非待提交和审核不通过不能作废
        long count = Stream.of(entity).filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = Stream.of(entity).filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        //验证存货核算是否关账
        /*List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList = list.stream().flatMap(obj -> Stream.of(new InventoryClosedRecordDTO.ClosedParamDTO(obj.getReceiveOrgId(),obj.getPurchaseDate())
                        ,new InventoryClosedRecordDTO.ClosedParamDTO(obj.getPurchaseOrgId(),obj.getPurchaseDate()))).
                distinct().collect(Collectors.toList());
        inventoryCloseRecordFeign.checkHsClosed(closedParamList);*/

        log.info("采购订单作废，ids=【{}】", JSONUtil.toJsonStr(ids));
        //更新订单作废状态
        updateInvalidStatus(ids, reason);
        //更新采购申请单的生成状态
        updateCreatePoType(ids);
        //删除采购申请单和订单关联表数据
        purchaseApplicationRefPoService.removeByPurchaseOrderIds(ids);
        //操作日志
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个采购订单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "作废操作");

        //发送金蝶
        sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_INVALID.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }


    @Override
    public List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(List<String> purchaseDetailIdList) {
        //采购订单明细信息
        List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailService.listByIds(purchaseDetailIdList);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //采购订单主表id集合
        List<String> mainIdList = detailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).collect(Collectors.toList());

        //供应商信息
        List<PurchaseOrderSupplierEntity> purchaseOrderSupplierList = purchaseOrderSupplierService.listByPurchaseOrderIds(mainIdList);
        if (CollectionUtils.isEmpty(purchaseOrderSupplierList)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceiveDTOS = baseMapper.viewGenerateReceive(purchaseDetailIdList);

        List<String> detailIdList = viewGenerateReceiveDTOS.stream().map(PurchaseOrderDTO.ViewGenerateReceiveDTO::getPurchaseOrderDetailId).collect(Collectors.toList());

        //获取sku的id集合
        List<String> skuIdList = viewGenerateReceiveDTOS.stream().map(PurchaseOrderDTO.ViewGenerateReceiveDTO::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //退货数量
        List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = wmsTaskFeign.listReturnOrderDetailByPodIds(detailIdList);

        //获取签收数量
        List<WarehouseReceiveDetailEntity> receiveQtyList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(detailIdList);
        for (PurchaseOrderDTO.ViewGenerateReceiveDTO viewGenerateReceiveDTO : viewGenerateReceiveDTOS) {
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(req -> req.getId().equals(viewGenerateReceiveDTO.getSkuId())).findFirst().orElse(null);
            viewGenerateReceiveDTO.setProductName(productDetailEntity.getName());
            viewGenerateReceiveDTO.setBillDate(LocalDate.now());
            viewGenerateReceiveDTO.setReceiveUserId(userInfo.getUid());
            viewGenerateReceiveDTO.setReceiveUserName(userInfo.getUserName());
            viewGenerateReceiveDTO.setFirstMassProductName(FirstMassProductTypeEnum.getName(viewGenerateReceiveDTO.getFirstMassProduct()));
            Integer receiveQty = receiveQtyList.stream().filter(obj -> obj.getSkuId().equals(viewGenerateReceiveDTO.getSkuId()) && obj.getPurchaseOrderDetailId().equals(viewGenerateReceiveDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            viewGenerateReceiveDTO.setReceiveQty(receiveQty);
            //补货数量
            Integer replenishQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(viewGenerateReceiveDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && req.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);

            viewGenerateReceiveDTO.setUnReceiveQty(viewGenerateReceiveDTO.getPurchaseQty() + replenishQty - receiveQty);
            viewGenerateReceiveDTO.setExceedQty(0);
        }

        return viewGenerateReceiveDTOS;
    }

    @Override
    public PurchaseChangeDTO.ViewDTO viewPurchaseChange(List<String> ids) {
        PurchaseChangeDTO.ViewDTO viewDTO = new PurchaseChangeDTO.ViewDTO();

        //采购订单明细
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        long count = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).distinct().count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_ID_REPEAT);
        }

        //采购主表信息
        PurchaseOrderEntity purchaseOrderEntity = this.getById(purchaseOrderDetailList.get(0).getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        viewDTO.setType(purchaseOrderEntity.getType());
        viewDTO.setTypeName(PurchaseOrderTypeEnum.getNameByCode(purchaseOrderEntity.getType()));
        viewDTO.setPurchaseOrderId(purchaseOrderEntity.getId());
        viewDTO.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
        viewDTO.setReceiveOrgId(purchaseOrderEntity.getReceiveOrgId());
        viewDTO.setDeliveryWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());
        //采购供应商信息
        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(purchaseOrderEntity.getId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        PurchaseOrderSupplierDTO.UpdateDTO supplierDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        BeanMapperUtils.copy(supplierEntity, supplierDTO);
        viewDTO.setSupplierDTO(supplierDTO);
        viewDTO.setSupplierId(supplierEntity.getSupplierId());
        //退货单记录
        List<PoReturnDetailEntity> poReturnDetailEntityList = null;
        if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(purchaseOrderEntity.getType())) {

            String sourceId = purchaseOrderEntity.getSourceId();
            List<PoReturnEntity> poReturnEntityList = wmsTaskFeign.listPoReturnByIdList(Collections.singletonList(sourceId));
            if (CollectionUtils.isEmpty(poReturnEntityList)) {
                throw new ServiceException(StrUtil.format("采购退货单【{}】记录不存在", purchaseOrderEntity.getSourceCode()));
            }
            //根据主键唯一 只会存在一个退货单记录
            PoReturnEntity poReturnEntity = poReturnEntityList.get(0);
            poReturnDetailEntityList = wmsTaskFeign.listPurchaseReturnOrderDetailByMainIds(Collections.singletonList(poReturnEntity.getId()));
            if (CollectionUtils.isEmpty(poReturnDetailEntityList)) {
                throw new ServiceException(StrUtil.format("采购退货单【{}】明细记录不存在", poReturnEntity.getCode()));
            }
        }
        List<PurchaseChangeDetailDTO.UpdateDTO> detailDTOList = new ArrayList<>();
        for (PurchaseOrderDetailEntity detailEntity : purchaseOrderDetailList) {
            PurchaseChangeDetailDTO.UpdateDTO detailDTO = new PurchaseChangeDetailDTO.UpdateDTO();
            //补货采购订单
            if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(purchaseOrderEntity.getType())) {
                PoReturnDetailEntity poReturnDetailEntity = poReturnDetailEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())
                        && StrUtil.isNotBlank(detailEntity.getSkuId()) && Objects.equals(e.getSkuId(), detailEntity.getSkuId())).findFirst().orElse(null);
                if (Objects.nonNull(poReturnDetailEntity)) {
                    detailDTO.setPurchaseOrderDetailId(detailEntity.getId());
                    detailDTO.setSkuId(detailEntity.getSkuId());
                    detailDTO.setSkuNo(detailEntity.getSkuNo());
                    detailDTO.setProductName(detailEntity.getProductName());
                    detailDTO.setCurrency(poReturnDetailEntity.getCurrency());
                    detailDTO.setCurrencySymbol(poReturnDetailEntity.getCurrencySymbol());
                    detailDTO.setOldQty(detailEntity.getPurchaseQty());
                    detailDTO.setOldPrice(detailEntity.getTaxPrice());
                    detailDTO.setOldAmount(detailEntity.getPurchaseAmount());
                    detailDTO.setPrice(detailEntity.getTaxPrice());//退货单下推单采购订单-这里可以直接取采购订单含税单价
                    detailDTO.setFirstMassProduct(detailEntity.getFirstMassProduct());
                    detailDTO.setFirstMassProductName(FirstMassProductTypeEnum.getName(detailEntity.getFirstMassProduct()));
                    detailDTO.setOldTaxRate(MathUtil.multiplyWithTwo(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
                    detailDTOList.add(detailDTO);
                }
            } else {
                detailDTO.setPurchaseOrderDetailId(detailEntity.getId());
                detailDTO.setSkuId(detailEntity.getSkuId());
                detailDTO.setSkuNo(detailEntity.getSkuNo());
                detailDTO.setProductName(detailEntity.getProductName());
                detailDTO.setCurrency(detailEntity.getCurrency());
                detailDTO.setCurrencySymbol(detailEntity.getCurrencySymbol());
                detailDTO.setOldQty(detailEntity.getPurchaseQty());
                detailDTO.setOldPrice(detailEntity.getTaxPrice());
                detailDTO.setOldAmount(detailEntity.getPurchaseAmount());
                detailDTO.setOldTaxRate(MathUtil.multiplyWithTwo(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
                detailDTO.setFirstMassProduct(detailEntity.getFirstMassProduct());
                detailDTO.setFirstMassProductName(FirstMassProductTypeEnum.getName(detailEntity.getFirstMassProduct()));
                detailDTOList.add(detailDTO);
            }

        }
        viewDTO.setDetails(detailDTOList);
        return viewDTO;
    }

    @Override
    public List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(List<String> purchaseDetailIdList) {
        List<PurchaseOrderDTO.ViewGenerateStockInDTO> resultList = new ArrayList<>();
        List<PurchaseOrderDetailEntity> list = purchaseOrderDetailService.listByIds(purchaseDetailIdList);
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //采购订单主表id集合
        List<String> mainIdList = list.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).collect(Collectors.toList());

        //采购订单
        List<PurchaseOrderEntity> purchaseOrderList = this.listByIds(mainIdList);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            return resultList;
        }

        //订单供应商
        List<PurchaseOrderSupplierEntity> supplierList = purchaseOrderSupplierService.listByPurchaseOrderIds(mainIdList);
        if (CollectionUtils.isEmpty(supplierList)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }

        List<String> podIds = list.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);

        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        //仓位信息
        List<String> warehouseIds = purchaseOrderList.stream().map(PurchaseOrderEntity::getDeliveryWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationFeign.listByWarehouseIds(warehouseIds);
        for (PurchaseOrderDetailEntity detailEntity : list) {
            PurchaseOrderDTO.ViewGenerateStockInDTO viewGenerateStockInDTO = new PurchaseOrderDTO.ViewGenerateStockInDTO();
            BeanMapperUtils.copy(detailEntity, viewGenerateStockInDTO);
            viewGenerateStockInDTO.setPurchaseOrderDetailId(detailEntity.getId());

            PurchaseOrderEntity entity = purchaseOrderList.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98025);
            }
            viewGenerateStockInDTO.setPurchaseOrderCode(entity.getCode());
            viewGenerateStockInDTO.setDeliveryWarehouseId(entity.getDeliveryWarehouseId());
            viewGenerateStockInDTO.setDeliveryWarehouseName(entity.getDeliveryWarehouseName());
            viewGenerateStockInDTO.setFirstMassProduct(detailEntity.getFirstMassProduct());
            viewGenerateStockInDTO.setFirstMassProductName(FirstMassProductTypeEnum.getName(detailEntity.getFirstMassProduct()));
            //供应商信息
            PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = supplierList.stream().filter(obj -> obj.getPurchaseOrderId().equals(detailEntity.getPurchaseOrderId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
                throw new ServiceException(ApiError.ERROR_98036);
            }
            viewGenerateStockInDTO.setSupplierName(purchaseOrderSupplierEntity.getSupplierName());
            //收货数量
            Integer receiveQty = MathUtil.ZERO;
            //超收数量
            Integer exceedQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                receiveQty = receiveDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                exceedQty = receiveDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId())).map(WarehouseReceiveDetailEntity::getExceedQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewGenerateStockInDTO.setReceiveQty(receiveQty);
            viewGenerateStockInDTO.setExceedQty(exceedQty);
            //已入库数量
            Integer hasStockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                hasStockInQty = stockInDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId()))
                        .map(PoInstockDetailEntity::getStockInQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
            }
            //已退货补货数量
            Integer hasReturnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                hasReturnQty = returnOrderDetailList.stream()
                        .filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId())
                                && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())
                                && !ReturnOrderSourceEnum.QC.getCode().equals(obj.getSourceType())
                                && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode()))
                        .map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //未入库数量
            viewGenerateStockInDTO.setUnStockInQty(detailEntity.getPurchaseQty() - hasStockInQty + hasReturnQty);
            //入库数量
            viewGenerateStockInDTO.setStockInQty(viewGenerateStockInDTO.getUnStockInQty());
            //如果未入库数量已全部下推则无需继续显示
            if (MathUtil.compareTo(MathUtil.ZERO, viewGenerateStockInDTO.getUnStockInQty()) >= MathUtil.ZERO) {
                continue;
            }
            //仓位信息填充
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getCode().equals(detailEntity.getWarehouseLocation())
                    && e.getWarehouseId().equals(entity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseLocationEntity());
            viewGenerateStockInDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
            resultList.add(viewGenerateStockInDTO);
        }
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.ERROR_98092);
        }
        return resultList;
    }

    @Override
    public PurchaseOrderDTO.GetOneDTO getPurchaseOrder(String id) {
        PurchaseOrderDTO.GetOneDTO getOneDTO = new PurchaseOrderDTO.GetOneDTO();
        PurchaseOrderEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        BeanMapperUtils.copy(entity, getOneDTO);
        getOneDTO.setWarehouseName(entity.getDeliveryWarehouseName());
        //采购供应商信息
        PurchaseOrderSupplierDTO.UpdateDTO updateDTO = new PurchaseOrderSupplierDTO.UpdateDTO();
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        BeanMapperUtils.copy(purchaseOrderSupplierEntity, updateDTO);
        getOneDTO.setPurchaseOrderSupplierDTO(updateDTO);
        //供应商地址
        SupplierEntity supplier = supplierService.getById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isNotEmpty(supplier)) {
            getOneDTO.setCompanyAddress(supplier.getCompanyAddress());
            getOneDTO.setSupplierName(supplier.getName());
        }

        return getOneDTO;
    }


    /**
     * 根据采购订单id 获取对应产品信息
     *
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetQcProductDTO
     * @author yl
     * @date 2023-04-17 18:27
     */
    @Override
    public PurchaseOrderDTO.GetQcProductDTO getQcProductInfo(String purchaseOrderId) {
        PurchaseOrderDTO.GetQcProductDTO result = new PurchaseOrderDTO.GetQcProductDTO();
        PurchaseOrderDTO.GetOneDTO entity = this.getPurchaseOrder(purchaseOrderId);
        List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailService.listByPurchaseOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        result.setSupplierId(entity.getPurchaseOrderSupplierDTO().getSupplierId());
        result.setSupplierName(entity.getSupplierName());
        result.setWarehouseId(entity.getDeliveryWarehouseId());
        result.setWarehouseName(entity.getWarehouseName());
        //是否新品首批
        boolean isFirstMassProduct = detailList.stream().anyMatch(v -> !FirstMassProductTypeEnum.SUBSEQUENT_BATCH.getCode().equals(v.getFirstMassProduct()));
        //入库质检
        String qcType = QcTypeEnum.STOCK_IN.getCode();
        //是
        if (isFirstMassProduct) {
            qcType = QcTypeEnum.NEW_PRODUCT_STOCK_IN.getCode();
        }
        Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
        result.setQcType(qcType);
        result.setIsInside(isInside);

        //采购订单详情
        List<PurchaseOrderDetailEntity> orderDetailList = purchaseOrderDetailService.listByPurchaseOrderId(purchaseOrderId);
        List<String> skuIdList = orderDetailList.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductVO.ProductPackVO> skuList = plmTaskFeign.getProductPackBySkuIds(skuIdList);
        List<ProductVO.ProductPackVO> productList = new ArrayList<>(orderDetailList.size());
        for (PurchaseOrderDetailEntity item : orderDetailList) {
            ProductVO.ProductPackVO flag = new ProductVO.ProductPackVO();
            String skuId = item.getSkuId();
            ProductVO.ProductPackVO find = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (find != null) {
                BeanMapper.copy(find, flag);
            }
            flag.setPurchaseOrderDetailId(item.getId());
            flag.setQty(item.getPurchaseQty());
            flag.setSkuId(skuId);

            //入库质检
            String detailQcType = QcTypeEnum.STOCK_IN.getCode();
            //非首批：否
            if (!item.getFirstMassProduct().equals(FirstMassProductTypeEnum.SUBSEQUENT_BATCH.getCode())) {
                detailQcType = QcTypeEnum.NEW_PRODUCT_STOCK_IN.getCode();
            }
            flag.setQcType(detailQcType);
            flag.setIsInside(QcTypeEnum.getIsInsideByCode(detailQcType));
            productList.add(flag);
        }
        result.setProductList(productList);
        return result;
    }

    @Override
    public PurchaseOrderDTO.GetQcProductDTO getQcProductInfoByDetailId(String purchaseOrderDetailId) {
        PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailService.getById(purchaseOrderDetailId);
        if (ObjectUtils.isEmpty(detailEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        PurchaseOrderDTO.GetQcProductDTO result = new PurchaseOrderDTO.GetQcProductDTO();
        PurchaseOrderDTO.GetOneDTO entity = this.getPurchaseOrder(detailEntity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        result.setSupplierId(entity.getPurchaseOrderSupplierDTO().getSupplierId());
        result.setSupplierName(entity.getSupplierName());
        result.setWarehouseId(entity.getDeliveryWarehouseId());
        result.setWarehouseName(entity.getWarehouseName());
        //入库质检
        String qcType = QcTypeEnum.STOCK_IN.getCode();
        //非首批：否
        if (!detailEntity.getFirstMassProduct().equals(FirstMassProductTypeEnum.SUBSEQUENT_BATCH.getCode())) {
            qcType = QcTypeEnum.NEW_PRODUCT_STOCK_IN.getCode();
        }
        Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
        result.setQcType(qcType);
        result.setIsInside(isInside);
        //采购订单详情
        String skuId = detailEntity.getSkuId();
        List<String> skuIdList = Collections.singletonList(skuId);
        List<ProductVO.ProductPackVO> skuList = plmTaskFeign.getProductPackBySkuIds(skuIdList);
        ProductVO.ProductPackVO flag = new ProductVO.ProductPackVO();
        ProductVO.ProductPackVO find = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (find != null) {
            BeanMapper.copy(find, flag);
        }
        flag.setPurchaseOrderDetailId(detailEntity.getId());
        flag.setQty(detailEntity.getPurchaseQty());
        flag.setSkuId(skuId);
        result.setProductList(Collections.singletonList(flag));
        return result;
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(PurchaseOrderEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchaseOrderEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    /**
     * 根据采购订单id 集合获取对应数量
     *
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     * @author yl
     * @date 2023-04-23 14:03
     */
    @Override
    public List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getPurchaseOrderByOrderIds(List<String> purchaseOrderIds) {
        if (CollectionUtils.isEmpty(purchaseOrderIds)) {
            return Collections.emptyList();
        }
        List<PurchaseOrderDTO.PurchaseOrderInfoDTO> resultList = baseMapper.getPurchaseOrderByOrderIds(purchaseOrderIds);
        return resultList;
    }


    /**
     * 采购订单 下推 退货数据显示
     *
     * @param purchaseDetailIdList
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     * @author yl
     * @date 2023-04-25 9:39
     */
    @Override
    public List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> purchaseDetailIdList) {

        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = baseMapper.viewGeneratePurchaseReturnOrder(purchaseDetailIdList);
        List<String> podIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PoInstockDetailEntity> stockInSkuList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = list.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getDeliveryWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationFeign.listByWarehouseIdAndCode(paramList);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        stockInSkuList = stockInSkuList.stream().filter(s -> approveStatus.equals(s.getApproveStatus())).collect(Collectors.toList());
        String type = SourceTypeEnum.PURCHASE_ORDER.getCode();
        for (PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO item : list) {
            item.setSourceType(type);
            Integer qty = stockInSkuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId()) &&
                            item.getPurchaseOrderDetailId().equals(s.getPurchaseOrderDetailId()))
                    .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            item.setStockInQty(qty);
            //仓位信息填充
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getCode().equals(item.getWarehouseLocation())
                    && e.getWarehouseId().equals(item.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseLocationEntity());
            item.setWarehouseLocationName(warehouseLocationEntity.getName());
            //相同采购单号清空后面数据的采购单号和供应商
            List<String> poIds = list.stream().map(PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO::getPurchaseOrderId).collect(Collectors.toList());
            boolean contains = poIds.contains(item.getPurchaseOrderId());
            if (contains) {
                item.setPurchaseOrderCode(null);
                item.setSupplierName(null);
                continue;
            }
        }

        return list;
    }

    @Override
    public List<PurchaseOrderDTO.ListDTO> listBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

    /**
     * @param records
     * @description: 列表查询数据处理
     * @author Will
     * @date: 2023/4/19 18:42
     */
    @Override
    public void doOpHandlePurchaseOrder(List<PurchaseOrderDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        // 采购订单明细id集合
        List<String> podIds = records.stream().map(PurchaseOrderDTO.ListDTO::getPurchaseDetailId).collect(Collectors.toList());
        // 采购订单id集合
        List<String> purchaseOrderIds = records.stream().map(PurchaseOrderDTO.ListDTO::getId).collect(Collectors.toList());
        //入库信息
        List<PoInstockDetailEntity> purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);

        //退货数量
        List<PoReturnDetailEntity> purchaseReturnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);

        //根据SKU查询BOM判断是否是组合SKU
        List<String> skuIds = records.stream().map(PurchaseOrderDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIds);

        //单据类型
        List<DictBasicDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.PURCHASE_ORDER_TYPE.getType());


        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        records.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_ORDER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        //关联信息
        List<PurchaseApplicationRefPoEntity> refList = purchaseApplicationRefPoService.listByPurchaseOrderIds(purchaseOrderIds);

        //来源于委外的采购订单
        List<String> subIdList = records.stream().filter(obj -> SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(obj.getSourceType())).map(PurchaseOrderDTO.ListDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractOrderEntity> subcontractOrderList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subIdList)) {
            subcontractOrderList = subcontractOrderService.listByIds(subIdList);
        }

        //来源于采购退货的采购订单
        List<String> poReturnIdList = records.stream().filter(obj -> SourceTypeEnum.PO_RETURN.getCode().equals(obj.getSourceType())).map(PurchaseOrderDTO.ListDTO::getSourceId).collect(Collectors.toList());
        List<PoReturnEntity> purchaseReturnOrderList = wmsTaskFeign.listPoReturnByIdList(poReturnIdList);

        //供应商信息
        List<String> supplierIdList = records.stream().map(PurchaseOrderDTO.ListDTO::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIdList);

        // 采购申请单id集合
        List<String> purchaseApplicationIds = Lists.newArrayList();
        List<String> warehouseLocationList = records.stream().map(v -> v.getWarehouseLocation()).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(warehouseLocationList)) {
            warehouseLocationEntityList = FeignQuery.create(WarehouseLocationEntity.class).in(WarehouseLocationEntity::getCode, warehouseLocationEntityList).list();
        }
        for (PurchaseOrderDTO.ListDTO obj : records) {
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(v -> v.getCode().equals(obj.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            obj.setWarehouseLocationName(warehouseLocationEntity.getName());
            if (StringUtils.isBlank(obj.getWarehouseLocation())) {
                obj.setWarehouseLocationName("空仓位");
            }
            SkuVO skuVO = skuList.stream().filter(e -> e.getSkuId().equals(obj.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                obj.setDeclareModel(skuVO.getDeclareModel());
                obj.setDeclareName(skuVO.getDeclareName());
            }
            //退货补货数量
            Integer replenishQty = purchaseReturnOrderDetailList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                            && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                            && !ReturnOrderSourceEnum.QC.getCode().equals(req.getSourceType())
                            && req.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode()))
                    .map(PoReturnDetailEntity::getReplenishQty)
                    .reduce(MathUtil.ZERO, Integer::sum);

            //已收货数量
            Integer receiveQty = MathUtil.ZERO;

            //收货数量
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //入库数量
            Integer stockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                stockInQty = purchaseStockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            /**
             * 是否结束交货-无：待交数量 =【采购数量-入库数量】+退货数量[退货补货量]
             * 是否结束交货-有：
             *      1、结束交货时间 > 退货单审核时间，则待交数量不计退货数量
             *      2、结束交货时间 <= 退货单审核时间，则待交数量 = 退货数量[退货补货量]
             */
            Integer deliveryQty;
            if (ObjectUtils.isNotEmpty(obj.getIsEndReceive()) && obj.getIsEndReceive()) {
                //等于或晚于结束交货的退货补货的数量
                deliveryQty = purchaseReturnOrderDetailList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                                && req.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())
                                && (req.getApproveTime().isAfter(obj.getEndReceiveTime()) || req.getApproveTime().isEqual(obj.getEndReceiveTime())))
                        .map(PoReturnDetailEntity::getReplenishQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
            } else {
                deliveryQty = obj.getPurchaseQty() + replenishQty - stockInQty;
            }
            //已收货数量
            obj.setReceiveQty(receiveQty);
            //待交货数量
            obj.setDeliveryQty(deliveryQty);
            //退货数量
            Integer returnQtyt = purchaseReturnOrderDetailList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                            && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()))
                    .map(PoReturnDetailEntity::getReturnQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            obj.setReturnQty(returnQtyt);
            //退货补货数量
            Integer qcReturnQty = purchaseReturnOrderDetailList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                            && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                            && ReturnOrderSourceEnum.QC.getCode().equals(req.getSourceType()))
                    .map(PoReturnDetailEntity::getReturnQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            obj.setQcReturnQty(qcReturnQty);
            //库存补货数量
            Integer stockReturnQty = purchaseReturnOrderDetailList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                            && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                            && !ReturnOrderSourceEnum.QC.getCode().equals(req.getSourceType()))
                    .map(PoReturnDetailEntity::getReturnQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            obj.setStockReturnQty(stockReturnQty);

            obj.setStockInQty(stockInQty);
            obj.setTaxRateStr(MathUtil.multiplyWithTwo(obj.getTaxRate(), MathUtil.BigDecimal_100).toString().concat("%"));

            //是否是组合SKU
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(obj.getSkuId())).count();
                if (count > 0) {
                    obj.setIsConstitute(Boolean.TRUE);
                }
            }
            //单据类型名称
            if (CollectionUtils.isNotEmpty(dictBasicList)) {
                String typeName = dictBasicList.stream().filter(e -> e.getValue().equals(obj.getType())).findFirst().flatMap(e -> Optional.ofNullable(e.getName())).orElse("");
                obj.setTypeName(typeName);
            }
            //退货方式

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setExecutionStatusName(ExecutionStatusEnum.getNameByCode(obj.getExecutionStatus()));
            // 采购申请单号
            if (CollUtil.isNotEmpty(refList) && StringUtils.isBlank(obj.getSourceType())) {
                // 采购申请单明细id和采购订单明细id是多对多，可能存在多条
                List<PurchaseApplicationRefPoEntity> filterRefList = refList.stream().filter(r -> {
                    if (Objects.equals(obj.getId(), r.getPurchaseOrderId())
                            && Objects.equals(obj.getPurchaseDetailId(), r.getPurchaseOrderDetailId())) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(filterRefList)) {
                    List<String> applicationIds = filterRefList.stream().map(PurchaseApplicationRefPoEntity::getPurchaseApplicationId).distinct().collect(Collectors.toList());
                    purchaseApplicationIds.addAll(applicationIds);
                    obj.setPurchaseApplicationIds(applicationIds);
                }

            }
            //委外订单
            if (CollectionUtils.isNotEmpty(subcontractOrderList) && SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(obj.getSourceType())) {
                String subCode = subcontractOrderList.stream().filter(e -> e.getId().equals(obj.getSourceId())).findFirst().flatMap(e -> Optional.ofNullable(e.getCode())).orElse("");
                obj.setSourceCode(subCode);
            }
            //采购退货
            if (CollectionUtils.isNotEmpty(purchaseReturnOrderList) && SourceTypeEnum.PO_RETURN.getCode().equals(obj.getSourceType())) {
                PoReturnEntity poReturnEntity = purchaseReturnOrderList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getId(), obj.getSourceId())).findFirst().orElse(null);
                obj.setSourceCode(Objects.nonNull(poReturnEntity) ? poReturnEntity.getCode() : "");
                obj.setReturnType(Objects.nonNull(poReturnEntity) ? poReturnEntity.getReturnMode() : "");
                obj.setReturnTypeName(ReturnModeEnum.getName(obj.getReturnType()));
            }

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                obj.setApproveUserName(curApprove);
            }
            //确认类型
            obj.setConfirmTypeName(ConfirmTypeEnum.getNameByCode(obj.getConfirmType()));
            //srm协同
            Boolean srmDisabled = supplierList.stream().filter(e -> StrUtil.equals(e.getId(), obj.getSupplierId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSrmDisabled())).orElse(null);
            obj.setSrmDisabled(srmDisabled);
            obj.setSrmDisabledName(Boolean.TRUE.equals(srmDisabled) ? "未开启" : "已开启");
            //含税单价
            obj.setTaxPriceName(obj.getCurrencySymbol() + obj.getTaxPrice());
            //价税合计
            obj.setPurchaseAmountName(obj.getCurrencySymbol() + obj.getPurchaseAmount().stripTrailingZeros().toPlainString());
            //合同盖章状态
            obj.setContractStampStatusName(ContractStampStatusEnum.getName(obj.getContractStampStatus()));
        }
        ;

        if (CollUtil.isNotEmpty(purchaseApplicationIds)) {
            List<PurchaseApplicationEntity> purchaseApplicationList = purchaseApplicationService.listByIds(purchaseApplicationIds);
            // 采购申请单id和采购申请单对应map
            Map<String, PurchaseApplicationEntity> refMap = purchaseApplicationList.stream().collect(Collectors.toMap(PurchaseApplicationEntity::getId, Function.identity()));

            records.forEach(obj -> {
                if (CollUtil.isNotEmpty(obj.getPurchaseApplicationIds())) {
                    StringBuffer applicationCodes = new StringBuffer("");
                    obj.getPurchaseApplicationIds().stream().forEach(applicationId -> {
                        PurchaseApplicationEntity refEntity = refMap.get(applicationId);
                        if (Objects.nonNull(refEntity)) {
                            applicationCodes.append(refEntity.getCode()).append(",");
                        }
                    });
                    if (applicationCodes.toString().endsWith(",")) {
                        applicationCodes.deleteCharAt(applicationCodes.length() - 1);
                    }
                    obj.setSourceCode(applicationCodes.toString());
                }
            });
        }
    }

    @Override
    public List<PurchaseOrderEntity> findByCodes(List<String> codes) {
        return lambdaQuery().in(PurchaseOrderEntity::getCode, codes).list();
    }

    @Override
    public List<PurchaseOrderDTO.ViewSubcontractPoDTO> viewSubcontractPo(String poId) {
        PurchaseOrderEntity purchaseOrderEntity = this.getById(poId);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        if (StringUtils.isBlank(purchaseOrderEntity.getSubcontractType())) {
            return Collections.EMPTY_LIST;
        }
        List<PurchaseOrderDTO.ViewSubcontractPoDTO> list = baseMapper.viewSubcontractPo(purchaseOrderEntity.getSourceId());
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> skuIds = list.stream().map(PurchaseOrderDTO.ViewSubcontractPoDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        for (PurchaseOrderDTO.ViewSubcontractPoDTO viewSubcontractPoDTO : list) {
            //产品名称
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewSubcontractPoDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            viewSubcontractPoDTO.setProductName(productName);
            //单据状态名称
            viewSubcontractPoDTO.setApproveStatusName(ApproveStatusEnum.getName(viewSubcontractPoDTO.getApproveStatus()));
        }
        return list;
    }

    @Override
    public List<PurchaseOrderDTO.SubcontractOrderChildDTO> listPoRefSubChildByParentPodIds(List<String> parentPodIds) {
        return baseMapper.listPoRefSubChildByParentPodIds(parentPodIds);
    }


    @Override
    public List<PurchaseOrderEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(PurchaseOrderEntity::getSourceId, sourceIds).eq(PurchaseOrderEntity::getInvalidStatus, Boolean.FALSE).list();
    }

    /**
     * 更新采购申请单的生成状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCreatePoType(List<String> purchaseOrderIds) {
        if (CollectionUtils.isEmpty(purchaseOrderIds)) {
            return;
        }
        //关联信息
        List<PurchaseApplicationRefPoEntity> list = purchaseApplicationRefPoService.listByPurchaseOrderIds(purchaseOrderIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> purchaseApplicationDetailIds = list.stream().map(PurchaseApplicationRefPoEntity::getPurchaseApplicationDetailId).distinct().collect(Collectors.toList());
        //查询采购订单明细下采购数量
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(new PurchaseApplicationRefPoDTO.SearchParamDTO().setPurchaseApplicationDetailIds(purchaseApplicationDetailIds));
        if (CollectionUtils.isEmpty(refList)) {
            return;
        }
        //查询采购申请单明细申请数量
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(purchaseApplicationDetailIds);
        if (CollectionUtils.isEmpty(purchaseApplicationDetailList)) {
            log.error("采购订单删除，未找到关联采购申请单明细！");
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<PurchaseApplicationDetailEntity> updateList = new ArrayList<>();
        //建采购订单明细按采购申请明细id分组后比较数量
        Map<String, List<PurchaseApplicationRefPoDTO.ListDTO>> map = refList.stream().collect(Collectors.groupingBy(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseApplicationDetailId));
        for (Map.Entry<String, List<PurchaseApplicationRefPoDTO.ListDTO>> entry : map.entrySet()) {
            String purchaseApplicationDetailId = entry.getKey();
            List<PurchaseApplicationRefPoDTO.ListDTO> value = entry.getValue();

            PurchaseApplicationDetailEntity update = new PurchaseApplicationDetailEntity();
            update.setId(purchaseApplicationDetailId);
            //采购数量
            Integer purchaseQty = value.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            //申请数量
            Integer applyQty = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(purchaseApplicationDetailId)).map(PurchaseApplicationDetailEntity::getApplyQty).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(applyQty)) {
                log.error(String.format("采购申请单明细Id【%s】不存在！", purchaseApplicationDetailId));
                throw new ServiceException(ApiError.ERROR_98017);
            }
            if (MathUtil.compareTo(purchaseQty, MathUtil.ZERO) == 0) {
                update.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            } else if (MathUtil.compareTo(applyQty, purchaseQty) > 0) {
                update.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            } else {
                update.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            }
            updateList.add(update);
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            purchaseApplicationDetailService.updateBatchById(updateList);
        }
    }


    @Override
    public PurchaseOrderDTO.PagingTotalDTO pagingTotal(PurchaseOrderDTO.SearchParamDTO dto) {
        PurchaseOrderDTO.PagingTotalDTO pagingTotalDTO = baseMapper.pagingTotal(dto);
        if (ObjectUtil.isEmpty(pagingTotalDTO)) {
            return new PurchaseOrderDTO.PagingTotalDTO(MathUtil.ZERO, BigDecimal.ZERO);
        }
        return pagingTotalDTO;
    }

    @Override
    public Boolean updateRemark(BaseIdsDTO.RemarkDTO dto) {
        purchaseOrderDetailService.updateRemarkByIds(dto.getIds(), dto.getRemark());
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseOrderEntity> listPoBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(PurchaseOrderEntity::getSourceId, sourceIds)
                .eq(PurchaseOrderEntity::getInvalidStatus, Boolean.FALSE)
                .list();
    }

    /**
     * @param entity
     * @description: 启动审核流程
     * @author Will
     * @date: 2023/7/11 14:11
     */
    private void startProcess(PurchaseOrderEntity entity) {
        LoginUser userInfo = UserContext.getLoginUser();
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.PURCHASE_ORDER.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(userInfo.getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * @param entity
     * @param dto
     * @description: 结束深审核
     * @author Will
     * @date: 2023/7/11 14:22
     */
    private void approveProcess(PurchaseOrderEntity entity, ApproveOneDTO dto) {
        /**
         * 目前代码里面批量审核的都是内部审核，不走流程，赋值可取第一条
         */
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PURCHASE_ORDER.getCode());
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

    /**
     * variablesMap值赋值
     *
     * @param entity
     * @return Map<String, Object>
     * @author will
     * @date 2025/5/21 10:51
     */
    private Map<String, Object> getVariablesMap(PurchaseOrderEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailService.listByPurchaseOrderId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<CfgQueryOptionEntity> purchaseOrder = FeignQuery.create(CfgQueryOptionEntity.class).eq(CfgQueryOptionEntity::getBussinessKey, "purchaseOrder").list();
        HashMap<String, String> stringHashMap = new HashMap<>();
        purchaseOrder.forEach(item->{
            stringHashMap.put(item.getTableName(),item.getFieldBelongsType());
        });
        if (stringHashMap==null){
            throw new ServiceException("未找到明细表对应entityCode");
        }

        String id = entity.getId();
        PurchaseOrderSupplierEntity supplier = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        List<SupplierAccountDTO.UpdateDTO> account = supplierAccountService.getBySupplierId(supplier.getSupplierId());
        if (CollectionUtils.isEmpty(account)){
            throw new ServiceException("供应商账户为空");
        }
        //税率额外处理
        detailList.forEach(item->{
            //item.getTaxRate() bigdecimal乘以100
            item.setTaxRate(item.getTaxRate().multiply(new BigDecimal(100)));
        });

        variablesMap.put(stringHashMap.get(PURCHASE_ORDER_DETAIL), BeanUtil.copyToList(detailList, Map.class));

        //供应商map
        Map<String, Object> supplierMap = BeanUtil.beanToMap(supplier);
        List<Map<String, Object>> maps = Collections.singletonList(supplierMap);
        variablesMap.put(stringHashMap.get(ThirdConstants.PURCHASE_ORDER_SUPPLIER), maps);
        //价税合计
        BigDecimal taxPriceTotal = detailList.stream().map(obj -> MathUtil.multiplyWithTwo(obj.getTaxPrice(), obj.getPurchaseQty())).reduce(BigDecimal.ZERO, BigDecimal::add);
        variablesMap.put("taxPriceTotal", taxPriceTotal);
        //不含税合计
        BigDecimal notTaxPriceTotal = detailList.stream().map(obj -> MathUtil.multiplyWithTwo(MathUtil.divide(obj.getTaxPrice(), MathUtil.add(BigDecimal.ONE, obj.getTaxRate())), obj.getPurchaseQty())).reduce(BigDecimal.ZERO, BigDecimal::add);
        variablesMap.put("notTaxPriceTotal", notTaxPriceTotal);
        //总计采购数量
        Integer purchaseQtyTotal = detailList.stream().map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
        variablesMap.put("purchaseQtyTotal", purchaseQtyTotal);
        //存在加急
        Boolean isUrgent = detailList.stream().anyMatch(PurchaseOrderDetailEntity::getIsUrgent);
        variablesMap.put("isUrgentTotal", isUrgent);
        //存在赠品
        Boolean isGift = detailList.stream().anyMatch(PurchaseOrderDetailEntity::getIsGift);
        variablesMap.put("isGiftTotal", isGift);
        //新品首批
        String firstMassProduct = detailList.stream().map(PurchaseOrderDetailEntity::getFirstMassProduct).collect(Collectors.joining(","));
        variablesMap.put("firstMassProduct", firstMassProduct);
        return variablesMap;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(PurchaseOrderEntity entity) {
        //采购员
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(entity.getPurchaseUserId());
            if (ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            entity.setPurchaseUserName(purchaseUser.getUserName());
            // 部门
            entity.setPurchaseDeptId(purchaseUser.getDepartmentId());
            entity.setPurchaseDeptName(purchaseUser.getDepartmentName());
        }

        //委外订单来源
        if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType())) {
            SubcontractOrderEntity subcontractOrderEntity = subcontractOrderService.getById(entity.getSourceId());
            if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            entity.setSourceCode(subcontractOrderEntity.getCode());
        }
        //采购退货订单来源
        if (SourceTypeEnum.PO_RETURN.getCode().equals(entity.getSourceType())) {
            List<PoReturnEntity> purchaseReturnOrderList = wmsTaskFeign.listPoReturnByIdList(Arrays.asList(entity.getSourceId()));
            if (CollectionUtils.isEmpty(purchaseReturnOrderList)) {
                throw new ServiceException(ApiError.ERROR_99008);
            }
            entity.setSourceCode(purchaseReturnOrderList.get(0).getCode());
        }

        //仓库信息
        if (ObjectUtils.isNotEmpty(entity.getDeliveryWarehouseId())) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(entity.getDeliveryWarehouseId()));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(updateDTO)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            entity.setDeliveryWarehouseName(updateDTO.getName());
            entity.setReceiveOrgId(updateDTO.getOrgId());
        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(), entity.getReceiveOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9040);
        }
        //采购组织
        BaseIdDTO.CodeDTO purchaseOrgDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(purchaseOrgDTO)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORG_NOT_FOUND);
        }
        entity.setPurchaseOrgName(purchaseOrgDTO.getName());

        //收料组织
        if (StringUtils.isNotBlank(entity.getReceiveOrgId())) {
            BaseIdDTO.CodeDTO receiveOrgDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(receiveOrgDTO)) {
                throw new ServiceException(ApiError.ERROR_RECEIVE_ORG_NOT_FOUND);
            }
            entity.setReceiveOrgName(receiveOrgDTO.getName());
        }
    }


    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getApproveStatus, approveStatus)
                .set(PurchaseOrderEntity::getApproveUserId, "")
                .set(PurchaseOrderEntity::getApproveUserName, "")
                .set(PurchaseOrderEntity::getApproveTime, null)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private Boolean updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        return this.lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getApproveUserId, userInfo.getUid())
                .set(PurchaseOrderEntity::getApproveUserName, userInfo.getUserName())
                .set(PurchaseOrderEntity::getApproveStatus, approveStatus)
                .set(PurchaseOrderEntity::getApproveTime, LocalDateTime.now())
                .update();
    }


    /**
     * 根据ids查询数据
     */
    @Override
    public List<PurchaseOrderEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        List<PurchaseOrderEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        return list;
    }


    /**
     * @param excelDateList
     * @param successList
     * @param errorList
     * @param supplierId
     * @description: 查询供应商报价信息
     * @author Will
     * @date: 2023/3/30 14:58
     */
    private void doOpHandleNotExistPrice(List<PurchaseOrderImportExcelDTO> excelDateList, List<PurchaseOrderDetailDTO.AddDTO> successList, List<PurchaseOrderImportExcelDTO> errorList, String supplierId, String purchaseOrgId) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<PurchaseOrderDetailDTO.AddDTO> removeList = new ArrayList<>();
        for (PurchaseOrderDetailDTO.AddDTO addDTO : successList) {
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(addDTO.getPurchaseQty(), addDTO.getSkuId(), addDTO.getSkuNo(), supplierId, purchaseOrgId);
            PurchaseOrderImportExcelDTO purchaseOrderImportExcelDTO = excelDateList.stream().filter(obj -> addDTO.getSkuNo().equals(obj.getSkuNo()) && addDTO.getPurchaseQty().toString().equals(obj.getPurchaseQtyStr())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderImportExcelDTO)) {
                continue;
            }
            Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> pair = purchasePriceDetailService.listPurchaseTaxPriceView(searchDTO);
            String error = pair.getKey();
            //存在错误信息则
            if (StringUtils.isNotBlank(error)) {
                purchaseOrderImportExcelDTO.setErrorMsg("1、".concat(error));
                errorList.add(purchaseOrderImportExcelDTO);
                removeList.add(addDTO);
                continue;
            }
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> value = pair.getValue();
            addDTO.setTaxRate(value.get(0).getTaxRate());
            addDTO.setTaxPrice(value.get(0).getTaxPrice());
            addDTO.setCurrency(value.get(0).getCurrency());
            addDTO.setCurrencySymbol(value.get(0).getCurrencySymbol());
        }
        if (CollectionUtils.isNotEmpty(removeList)) {
            successList.removeAll(removeList);
        }
    }

    /**
     * @param ids
     * @param reason
     * @description: 更新作废状态
     * @author Will
     * @date: 2023/3/30 18:09
     */
    private void updateInvalidStatus(List<String> ids, String reason) {
        //更新
        lambdaUpdate().in(PurchaseOrderEntity::getId, ids)
                .set(PurchaseOrderEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PurchaseOrderEntity::getInvalidTime, LocalDateTime.now())
                .set(PurchaseOrderEntity::getInvalidRemark, reason)
                .update();
    }


    /**
     * @param list
     * @description: 更新库存（采购订单审核）
     * @author zhangchunlin
     * @date: 2023/5/23 12:11
     */
    public void updateInventoryTransCore(List<PurchaseOrderEntity> list) {
        Map<String, PurchaseOrderEntity> orderMap = list.stream().collect(Collectors.toMap(PurchaseOrderEntity::getId, Function.identity()));
        List<InstockForcastDTO.AddDTO> dataList = Lists.newArrayList();

        List<String> ids = list.stream().map(PurchaseOrderEntity::getId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByPurchaseOrderIds(ids);

        // 获取采购订单明细
        orderMap.forEach((id, order) -> {
            List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPurchaseOrderId(), id)).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailList)) {
                throw new ServiceException("未找到采购订单明细信息");
            }
            InstockForcastDTO.AddDTO inventoryForcastDTO = new InstockForcastDTO.AddDTO();
            inventoryForcastDTO.setPurchaseOrderId(id);
            inventoryForcastDTO.setPurchaseOrderCode(order.getCode());
            inventoryForcastDTO.setWarehouseId(order.getDeliveryWarehouseId());
            inventoryForcastDTO.setBillDate(order.getPurchaseDate());

            List<InstockForcastDetailDTO.AddDTO> details = Lists.newArrayListWithExpectedSize(detailList.size());
            detailList.stream().forEach(detail -> {
                InstockForcastDetailDTO.AddDTO member = new InstockForcastDetailDTO.AddDTO();
                member.setSkuId(detail.getSkuId());
                member.setSkuNo(detail.getSkuNo());
                member.setPurchaseOrderDetailId(detail.getId());
                member.setQty(detail.getPurchaseQty());
                member.setProductName(detail.getProductName());

                details.add(member);
            });
            inventoryForcastDTO.setDetails(details);
            dataList.add(inventoryForcastDTO);
        });
        if (CollUtil.isEmpty(dataList)) {
            return;
        }
        inventoryFeign.generateByPurchaseOrderBatch(dataList);
    }


    /**
     * 导出网采合同
     *
     * @param id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/13 15:09
     **/
    @Override
    public Boolean exportPurchaseContract(String id, HttpServletResponse response) {
//        downloadTaskFeign.saveDownloadTask("采购单网采合同", EXPORT_SCM_PURCHASE_ORDER_CONTRACT.getCode(), id);

        PurchaseOrderDTO.ExportPurchaseContractDTO contractDTO = new PurchaseOrderDTO.ExportPurchaseContractDTO();
        PurchaseOrderEntity purchaseOrderEntity = this.getById(id);
        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(purchaseOrderEntity.getId());
        contractDTO.setCreateTime(purchaseOrderEntity.getCreateTime());
        contractDTO.setApproveUserName(purchaseOrderEntity.getApproveUserName());
        contractDTO.setCode(purchaseOrderEntity.getCode());
        contractDTO.setCreateUserName(purchaseOrderEntity.getCreateUserName());
        List<DictBasicDTO> supplierPayMode = dictBasicService.getByKey("supplierPayMode");
        DictBasicDTO dictBasicDTO = supplierPayMode.stream().filter(req -> req.getId().equals(supplierEntity.getPayMethodId())).findFirst().orElse(new DictBasicDTO());
        contractDTO.setSettleMethod(dictBasicDTO.getName());
        contractDTO.setSupplierName(supplierEntity.getSupplierName());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = purchaseOrderDetailService.listByPurchaseOrderId(purchaseOrderEntity.getId());
        contractDTO.setSumQty(purchaseOrderDetailEntityList.stream().mapToInt(PurchaseOrderDetailEntity::getPurchaseQty).sum());
        BigDecimal sumTaxAmount = purchaseOrderDetailEntityList.stream().map(PurchaseOrderDetailEntity::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        contractDTO.setSumTaxAmount(sumTaxAmount);
        List<PurchaseOrderDTO.PurchaseContractDetailDTO> contractDetailList = new ArrayList<>();
        List<String> skuIdList = purchaseOrderDetailEntityList.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Integer sort = MathUtil.ZERO;
        for (PurchaseOrderDetailEntity detailEntity : purchaseOrderDetailEntityList) {
            sort++;
            SkuVO skuVO = skuList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            PurchaseOrderDTO.PurchaseContractDetailDTO detailDTO = new PurchaseOrderDTO.PurchaseContractDetailDTO();
            detailDTO.setSort(sort);
            detailDTO.setImg("");
            detailDTO.setSkuNo(detailEntity.getSkuNo());
            detailDTO.setProductName(skuVO.getSkuName());
            detailDTO.setRemark(detailEntity.getRemark());
            Integer qty = detailEntity.getPurchaseQty();
            detailDTO.setQty(qty);
            //含税单价
            BigDecimal taxPrice = detailEntity.getTaxPrice();
            detailDTO.setTaxPrice(taxPrice);
            //0.0900
            BigDecimal taxRate = detailEntity.getTaxRate();
            BigDecimal flagTaxRate = BigDecimal.ZERO;
            if (Objects.nonNull(taxRate)) {
                flagTaxRate = MathUtil.multiplyWithTwo(taxRate, MathUtil.BigDecimal_100).setScale(2);
            }
            detailDTO.setTaxRate(flagTaxRate + "%");
            BigDecimal multiplyTax = MathUtil.add(taxRate, MathUtil.BigDecimal_1);
            //未税单价
            BigDecimal price = BigDecimal.ZERO;
            if (Objects.nonNull(taxPrice)) {
                price = MathUtil.divide(taxPrice, multiplyTax);
            }
            detailDTO.setPrice(price);
            //未税金额
            detailDTO.setAmount(MathUtil.multiplyWithTwo(price, qty));
            //单位
            detailDTO.setUnit(skuVO.getUnitName());
            //含税金额
            detailDTO.setTaxAmount(detailEntity.getPurchaseAmount());
            contractDetailList.add(detailDTO);
        }
        BigDecimal sumAmount = contractDetailList.stream().map(PurchaseOrderDTO.PurchaseContractDetailDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        contractDTO.setSumAmount(sumAmount);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/purchaseContractExport.xlsx";
        String name = "采购单网采合同";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);

        try {
            new ExcelPrintUtils().patchExport(contractDetailList, contractDTO, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售单发票导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseOrderDTO.PdaPurchaseOrder> pdaList(PurchaseOrderDTO.PdaPurchaseOrderParam dto) {
        List<PurchaseOrderDTO.PdaPurchaseOrder> list = baseMapper.pdaList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> poIds = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> detailEntityList = purchaseOrderDetailService.listByPurchaseOrderIds(poIds);
        List<String> podIds = detailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailEntities = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        //获取未全部到货的采购详情id
        List<String> purchaseOrderDetailIds = new ArrayList<>();
        List<PoReturnDetailEntity> returnDetailEntityList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        //根据采购订单明细id分组
        Map<String, List<WarehouseReceiveDetailEntity>> map = receiveDetailEntities.stream().collect(Collectors.groupingBy(n -> n.getPurchaseOrderDetailId()));
        for (Map.Entry<String, List<WarehouseReceiveDetailEntity>> entry : map.entrySet()) {
            List<WarehouseReceiveDetailEntity> m = entry.getValue();
            int receiveQty = m.stream().mapToInt(WarehouseReceiveDetailEntity::getReceiveQty).sum();
            PurchaseOrderDetailEntity detailEntity = detailEntityList.stream().filter(req -> req.getId().equals(m.get(MathUtil.ZERO).getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(detailEntity)) {
                continue;
            }
            //退货补货数量
            Integer returnQty = returnDetailEntityList.stream().filter(obj ->
                            obj.getPurchaseOrderDetailId().equals(detailEntity.getId())
                                    && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
                                    && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())
                    )
                    .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (receiveQty < detailEntity.getPurchaseQty() + returnQty) {
                purchaseOrderDetailIds.add(m.get(MathUtil.ZERO).getPurchaseOrderDetailId());
            }
        }
        List<String> collect = receiveDetailEntities.stream().map(req -> req.getPurchaseOrderDetailId()).distinct().collect(Collectors.toList());
        List<String> ids = podIds.stream().filter(poid -> !collect.contains(poid)).collect(Collectors.toList());
        purchaseOrderDetailIds.addAll(ids);

        if (CollectionUtils.isEmpty(purchaseOrderDetailIds)) {
            return new ArrayList<>();
        }
        //根据未到货的采购单详情id获取采购单id
        List<PurchaseOrderDetailEntity> detailEntityListList = purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        List<String> notAllReceivePoOrderId = detailEntityListList.stream().map(req -> req.getPurchaseOrderId()).distinct().collect(Collectors.toList());

        //获取到未到货的采购单返回数据
        List<PurchaseOrderDTO.PdaPurchaseOrder> purchaseOrderEntitieList = list.stream().filter(req -> notAllReceivePoOrderId.contains(req.getId())).collect(Collectors.toList());

/*        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = purchaseOrderDetailService.listByPurchaseOrderIds(poIds);
        //入库信息
        List<String> podIds = purchaseOrderDetailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        for (PurchaseOrderDTO.PdaPurchaseOrder entity : purchaseOrderEntitieList) {
            entity.setApproveStatusName(ApproveStatusEnum.getName(entity.getApproveStatus()));
            List<PurchaseOrderDetailEntity> poDetailEntityList = purchaseOrderDetailEntityList.stream().filter(req -> req.getPurchaseOrderId().equals(entity.getId())).collect(Collectors.toList());
            List<PurchaseOrderDetailDTO.PdaPurchaseOrderDetail> itemList = new ArrayList<>();
            for (PurchaseOrderDetailEntity detailEntity : poDetailEntityList) {
                PurchaseOrderDetailDTO.PdaPurchaseOrderDetail detail = new PurchaseOrderDetailDTO.PdaPurchaseOrderDetail();
                detail.setId(detailEntity.getId());
                detail.setSkuNo(detailEntity.getSkuNo());
                detail.setPurchaseQty(detailEntity.getPurchaseQty());

                Integer receiveQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                    receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(detailEntity.getId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                            .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                detail.setReceiveQty(receiveQty);
                //入库数量
                Integer stockInQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                    stockInQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(detailEntity.getId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                            .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                detail.setStockInQty(stockInQty);
                itemList.add(detail);
            }
            entity.setItemList(itemList);
        }*/
        purchaseOrderEntitieList.sort(Comparator.comparing(PurchaseOrderDTO.PdaPurchaseOrder::getCode).reversed());
        purchaseOrderEntitieList.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return purchaseOrderEntitieList;
    }

    @Override
    public PurchaseOrderDTO.PdaViewDTO pdaView(String id) {
        PurchaseOrderDTO.PdaViewDTO dto = new PurchaseOrderDTO.PdaViewDTO();

        //主表信息
        PurchaseOrderEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        BeanMapperUtils.copy(entity, dto);

        // 采购员名称
        if (StrUtils.isNotEmpty(dto.getPurchaseUserId())) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(dto.getPurchaseUserId());
            if (ObjectUtils.isEmpty(purchaseUser)) {
                dto.setPurchaseUserName(purchaseUser.getUserName());
            }
        }

        //供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        PurchaseOrderSupplierDTO.PdaView supplierUpdateDTO = new PurchaseOrderSupplierDTO.PdaView();
        if (ObjectUtils.isEmpty(supplierUpdateDTO)) {
            throw new ServiceException(ApiError.ERROR_98031);
        }
        BeanMapperUtils.copy(purchaseOrderSupplierEntity, supplierUpdateDTO);
        //原供应商信息
        SupplierEntity supplier = supplierService.getById(supplierUpdateDTO.getSupplierId());
        if (ObjectUtils.isNotEmpty(supplier)) {
            supplierUpdateDTO.setSupplierAddress(supplier.getCompanyAddress());
        }
        if (StringUtils.isNotBlank(supplierUpdateDTO.getSupplierContactId())) {
            SupplierContactEntity supplierContactEntity = supplierContactService.getById(supplierUpdateDTO.getSupplierContactId());
            supplierUpdateDTO.setSupplierContactName(supplierContactEntity.getPerson());
        }
        dto.setPurchaseOrderSupplierDTO(supplierUpdateDTO);

        //单据类型
        List<DictBasicDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.PURCHASE_ORDER_TYPE.getType());
        String typeName = dictBasicList.stream().filter(obj -> obj.getValue().equals(entity.getType())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        dto.setTypeName(typeName);

        //明细信息
        List<PurchaseOrderDetailEntity> entityDetails = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<String> podIds = entityDetails.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<WarehouseReceiveDetailEntity> receiveDetailEntities = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        List<PoReturnDetailEntity> returnDetailEntityList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        List<PurchaseOrderDetailDTO.PdaViewDTO> details = BeanMapperUtils.copyList(PurchaseOrderDetailDTO.PdaViewDTO.class, entityDetails);

        List<String> skuNos = entityDetails.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listBySkuNoList(skuNos);

        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        for (PurchaseOrderDetailDTO.PdaViewDTO detail : details) {
            SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuId().equals(detail.getSkuId())).findFirst().orElse(new SkuVO());
            detail.setUnitName(skuVO.getUnitName());
            detail.setTaxRate(MathUtil.multiplyWithTwo(detail.getTaxRate(), MathUtil.BigDecimal_100));
            Integer receiveQty = receiveDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(detail.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            detail.setReceiveQty(receiveQty);
            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detail.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            detail.setUnReceiveQty(detail.getPurchaseQty() + returnQty - receiveQty);
            //已入库数量
            Integer hasStockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                hasStockInQty = stockInDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detail.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            detail.setHasStockInQty(hasStockInQty);
        }
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationFeign.listByWarehouseIds(Arrays.asList(entity.getDeliveryWarehouseId()));

        AtomicReference<Integer> index = new AtomicReference<>(0);
        Map<String, PurchaseOrderDetailDTO.PdaViewDTO> collect = details.stream().collect(Collectors.groupingBy(n -> n.getSkuNo() + "-" + n.getWarehouseLocation(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            int purchaseQty = m.stream().mapToInt(PurchaseOrderDetailDTO.PdaViewDTO::getPurchaseQty).sum();
            int receiveQty = m.stream().mapToInt(PurchaseOrderDetailDTO.PdaViewDTO::getReceiveQty).sum();
            int unReceiveQty = m.stream().mapToInt(PurchaseOrderDetailDTO.PdaViewDTO::getUnReceiveQty).sum();
            int hasStockInQty = m.stream().mapToInt(PurchaseOrderDetailDTO.PdaViewDTO::getHasStockInQty).sum();
            String podId = m.stream().max(Comparator.comparing(PurchaseOrderDetailDTO.PdaViewDTO::getId)).map(PurchaseOrderDetailDTO.PdaViewDTO::getId).get();
            PurchaseOrderDetailDTO.PdaViewDTO updateDTO = new PurchaseOrderDetailDTO.PdaViewDTO();
            BeanMapper.copy(m.get(MathUtil.ZERO), updateDTO);
            updateDTO.setId(podId);
            updateDTO.setPurchaseQty(purchaseQty);
            updateDTO.setReceiveQty(receiveQty);
            updateDTO.setUnReceiveQty(unReceiveQty);
            updateDTO.setHasStockInQty(hasStockInQty);
            m.forEach(obj -> {
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(obj.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                updateDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
            });
            index.getAndSet(index.get() + 1);
            return updateDTO;
        })));

        List<PurchaseOrderDetailDTO.PdaViewDTO> updateDTOS = new ArrayList<>();
        for (Map.Entry<String, PurchaseOrderDetailDTO.PdaViewDTO> stringUpdateDTOEntry : collect.entrySet()) {
            updateDTOS.add(stringUpdateDTOEntry.getValue());
        }
        dto.setDetails(updateDTOS);
        return dto;
    }

    @Override
    public List<PurchaseOrderDTO.PdaPurchaseOrder> pdaListAll(PurchaseOrderDTO.PdaPurchaseOrderParam dto) {
        List<PurchaseOrderDTO.PdaPurchaseOrder> list = baseMapper.pdaList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        list.sort(Comparator.comparing(PurchaseOrderDTO.PdaPurchaseOrder::getCode).reversed());
        list.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return list;
    }

    @Override
    public List<SkuCostDTO> listPurchaseOrderByPurchaseDate(SkuCostDTO.QueryPurchaseDTO queryPurchaseDTO) {
        if (queryPurchaseDTO.getLocalDateList().size() != 2) {
            return Collections.emptyList();
        }
        List<SkuCostDTO> list = baseMapper.listPurchaseOrderByPurchaseDate(queryPurchaseDTO.getLocalDateList());
        return list;
    }

    @Override
    public List<SkuCostDTO> listPurchaseOrderCost(SkuCostDTO.ParamDTO paramDTO) {
        return baseMapper.listPurchaseOrderCost(paramDTO);
    }

    @Override
    public PurchaseStatisticsDTO.ResponseDTO statisticsBySupplier(PurchaseStatisticsDTO.RequestDTO requestDTO) {
        PurchaseStatisticsDTO.ResponseDTO responseDTO = new PurchaseStatisticsDTO.ResponseDTO();
        responseDTO.setStatisticsMonthDTOList(baseMapper.statisticsBySupplier(requestDTO));
        return responseDTO;
    }

    /**
     * 全部  all
     * 待确认  toBeConfirm
     * 已确认  confirm
     * 已拒绝  reject
     * 送货中  delivery
     * 已完成  finish
     * 已关闭  closed
     */
    @Override
    public List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> srmOrderConfirmCount(PurchaseOrderSrmDTO.SearchParamDTO dto) {

        ExecutionStatusEnum[] typeEnums = ExecutionStatusEnum.values();
        List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> countDTOS = new ArrayList<>(typeEnums.length);

        for (ExecutionStatusEnum typeEnum : typeEnums) {
            countDTOS.add(getSrmConfirmCount(dto.getSupplierId(), typeEnum));
        }
        return countDTOS;
    }

    @Override
    public BatchResultDTO supplierConfirm(String id) {
        PurchaseOrderEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //判断审核状态
        if (!StrUtil.equals(ApproveStatusEnum.APPROVE.getCode(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_SUPPLIER_CONFIRM, entity.getCode());
        }

        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByPurchaseOrderId(id);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //判断执行状态
        long count = purchaseOrderDetailList.stream().filter(obj -> !StrUtil.equals(ExecutionStatusEnum.TO_BE_CONFIRM.getCode(), obj.getExecutionStatus()) && StrUtil.equals(ApproveStatusEnum.APPROVE.getCode(), entity.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_DETAIL_SUPPLIER_CONFIRM, entity.getCode());
        }
        //采购订单明细id集合
        List<String> detailIdList = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());

        purchaseOrderDetailService.purchaseOrderConfirm(detailIdList, ExecutionStatusEnum.CONFIRM, "", ConfirmTypeEnum.MANUAL);
//        if (CollectionUtils.isNotEmpty(detailIdList)){
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.putOpt("id",id);
//            jsonObject.putOpt("detailIds",detailIdList);
//            jsonObject.putOpt("executionStatus",ExecutionStatusEnum.CONFIRM.getCode());
//            //同步scm 确认订单 到 srm
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_SRM_PURCHASE_ORDER_DETAIL_TOPIC, RocketMqTagEnum.SYNC_SRM_PURCHASE_ORDER_DETAIL_TAG.getName(),jsonObject, IdUtil.simpleUUID());
//        }
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("【人工】操作【确认】采购订单【%s】", entity.getCode()), ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), "供应商确认操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public List<BatchResultDTO> srmOrderConfirmStatus(PurchaseOrderDTO.ConfirmDTO dto) {
        Set<String> ids;
        if (CollectionUtils.isEmpty(dto.getIds())) {
            return Collections.emptyList();
        }
        //订单去重
        ids = new HashSet<>(dto.getIds());
        List<BatchResultDTO> dtos = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            PurchaseOrderEntity entity = this.getById(id);
            if (ObjectUtil.isEmpty(entity)) {
                batchResultDTO.setId(id);
                batchResultDTO.setSuccess(false);
                batchResultDTO.setMsg(ApiError.ERROR_98025.msg);
                dtos.add(batchResultDTO);
                continue;
            }
            try {
                if (1 == dto.getStatus()) {
                    batchResultDTO = purchaseOrderConfirm(entity, ExecutionStatusEnum.CONFIRM, dto, ConfirmTypeEnum.MANUAL);
                    dtos.add(batchResultDTO);
                } else if (2 == dto.getStatus()) {
                    batchResultDTO = purchaseOrderConfirm(entity, ExecutionStatusEnum.REJECT, dto, ConfirmTypeEnum.MANUAL);
                    dtos.add(batchResultDTO);
                }

            } catch (Exception e) {
                batchResultDTO.setId(id);
                batchResultDTO.setCode(entity.getCode());
                batchResultDTO.setSuccess(false);
                batchResultDTO.setMsg(e.getMessage());
                dtos.add(batchResultDTO);
            }
        }
        return dtos;
    }

    private BatchResultDTO purchaseOrderConfirm(PurchaseOrderEntity entity, ExecutionStatusEnum typeEnum, PurchaseOrderDTO.ConfirmDTO dto, ConfirmTypeEnum confirmTypeEnum) {
        //判断审核状态
        if (!StrUtil.equals(ApproveStatusEnum.APPROVE.getCode(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_SUPPLIER_CONFIRM, entity.getCode());
        }

        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByPurchaseOrderId(entity.getId());
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //判断执行状态
        long count = purchaseOrderDetailList.stream().filter(obj -> !StrUtil.equals(ExecutionStatusEnum.TO_BE_CONFIRM.getCode(), obj.getExecutionStatus()) && StrUtil.equals(ApproveStatusEnum.APPROVE.getCode(), entity.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_DETAIL_SUPPLIER_CONFIRM, entity.getCode());
        }
        //供应商数据归属判断
        PurchaseOrderSupplierEntity orderSupplier = purchaseOrderSupplierService.getByPurchaseOrderId(entity.getId());
        if (Objects.isNull(orderSupplier) || StringUtils.isBlank(orderSupplier.getSupplierId())) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_NO_SUPPLIER_CONFIRM, entity.getCode());
        }
        if (!dto.getSupplierId().equalsIgnoreCase(orderSupplier.getSupplierId())) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_REF_SUPPLIER_CONFIRM_DIFF, entity.getCode());
        }
        //采购订单明细id集合
        List<String> detailIdList = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        purchaseOrderDetailService.purchaseOrderConfirm(detailIdList, typeEnum, dto.getRemark(), confirmTypeEnum);
//        if(typeEnum.equals(ExecutionStatusEnum.CONFIRM)){
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.putOpt("id",entity.getId());
//            jsonObject.putOpt("detailIds",detailIdList);
//            jsonObject.putOpt("executionStatus",ExecutionStatusEnum.CONFIRM.getCode());
//            //同步scm 确认订单 到 srm
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_SRM_PURCHASE_ORDER_DETAIL_TOPIC, RocketMqTagEnum.SYNC_SRM_PURCHASE_ORDER_DETAIL_TAG.getName(),jsonObject, IdUtil.simpleUUID());
//        }
        //操作日志
        String content = "";
        String operate = String.format("%s确认操作", confirmTypeEnum.getName());
        ;
        if (typeEnum.getCode().equals(ExecutionStatusEnum.CONFIRM.getCode())) {
            content = String.format("【%s】操作【确认】采购订单【%s】【接受原因是：%s】", confirmTypeEnum.getName(), entity.getCode(), dto.getRemark());
        } else {
            content = String.format("【%s】操作【拒绝】采购订单【%s】【拒绝原因是：%s】", confirmTypeEnum.getName(), entity.getCode(), dto.getRemark());
        }
        moduleOperateLogService.addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_ORDER.getCode(), entity.getId(), operate);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public void purchaseOrderAutoConfirm() {
        //查询规则设置
        List<CfgSettingDTO.ViewDTO> list = srmCfgSettingFeign.listByKey(ConfigKeyEnum.ORDER_AUTO_ACCEPT.getCode());
        if (CollectionUtils.isEmpty(list)) {
            log.warn("未找到存在采购订单设置的供应商！");
            return;
        }
        List<PurchaseOrderDetailDTO.PurchaseOrderConfirmDTO> purchaseOrderList = baseMapper.listPurchaseOrderAutoConfirm(list);
        if (CollectionUtils.isEmpty(purchaseOrderList)) {
            return;
        }
        List<String> detailIdList = purchaseOrderList.stream().map(PurchaseOrderDetailDTO.PurchaseOrderConfirmDTO::getDetailId).collect(Collectors.toList());
        purchaseOrderDetailService.purchaseOrderAutoConfirm(detailIdList);
//        if (CollectionUtils.isNotEmpty(detailIdList)){
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.putOpt("id",purchaseOrderList.get(0).getId());
//            jsonObject.putOpt("detailIds",detailIdList);
//            jsonObject.putOpt("executionStatus",ExecutionStatusEnum.CONFIRM.getCode());
//            //同步scm 确认订单 到 srm
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_SRM_PURCHASE_ORDER_DETAIL_TOPIC, RocketMqTagEnum.SYNC_SRM_PURCHASE_ORDER_DETAIL_TAG.getName(),jsonObject, IdUtil.simpleUUID());
//        }
        //操作日志
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(detailIdList);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            return;
        }
        List<Pair<String, String>> pairList = purchaseOrderDetailList.stream().map(obj -> new Pair<>(obj.getId(), obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("【人工】操作【确认】采购订单SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "自动确认操作");
    }


    private void doWaitDeliveryPurchaseOrder(List<PurchaseOrderDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        // 采购订单明细id集合
        List<String> podIds = records.stream().map(PurchaseOrderDTO.ListDTO::getPurchaseDetailId).collect(Collectors.toList());
        List<String> ids = records.stream().map(PurchaseOrderDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = srmDeliveryOrderFeign.listDetailDTOByDetailSourceIds(podIds);
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIds(ids);
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        records.forEach(obj -> {
            //已收货数量
            Integer receiveQty = MathUtil.ZERO;
            //已送货数量
            Integer deliveredQty = MathUtil.ZERO;
            //有送货单的收货数量
            Integer hasDeliveryReceiveQty = MathUtil.ZERO;
            //无送货单收货数量
            Integer unDeliveryReceiveQty = MathUtil.ZERO;
            //无收货单的入库数量
            Integer unReceiveInstockQty = MathUtil.ZERO;
            //收发差异
            Integer diffSendAndReceive = MathUtil.ZERO;
            //退货补货数量
            Integer returnQty = MathUtil.ZERO;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                hasDeliveryReceiveQty = receiveList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSourceType())
                                && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                && e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                unDeliveryReceiveQty = receiveList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isEmpty(e.getSourceId()) && e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            obj.setReceiveQty(receiveQty);
            //无收货单的入库数量
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                // 采购入库单（无收货单），只有审核通过的才占用库存数量
                unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && Objects.equals(e.getSourceDetailId(), obj.getPurchaseDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            // 退货单（退货补货的才会导致在途数量变化）
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                        .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //已送货数量
            if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
                deliveredQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                //收发差异
//                diffSendAndReceive = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId())
//                                && StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
//                        .map(deliveryOrderDetailEntity -> deliveryOrderDetailEntity.getDeliveryQty() - obj.getReceiveQty())
//                        .reduce(MathUtil.ZERO, Integer::sum);
                //发货数量 - 已审核收货数量
                Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId())
                                && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
            }
            obj.setDeliveredQty(deliveredQty);
            //剩余送货量/可下推量=采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
            obj.setWaitDeliveryQty(obj.getPurchaseQty() - deliveredQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty);
            //交货周期
            Boolean deliveryCycleFlag = false;
            if (Objects.nonNull(obj.getDeliveryCycle())) {
                if (obj.getDeliveryCycle() < 0) {
                    obj.setDeliveryCycleName(String.format("已超期%s天", obj.getDeliveryCycle() * -1));
                    deliveryCycleFlag = true;
                } else if (7 >= obj.getDeliveryCycle() && obj.getDeliveryCycle() >= 0) {
                    obj.setDeliveryCycleName(String.format("%s天后超期", obj.getDeliveryCycle()));
                    deliveryCycleFlag = true;
                } else if (30 >= obj.getDeliveryCycle() && obj.getDeliveryCycle() > 7) {
                    obj.setDeliveryCycleName(WaitDeliveryCycleEnum.IN_ONE_MONTH.getName());
                } else if (60 >= obj.getDeliveryCycle() && obj.getDeliveryCycle() > 30) {
                    obj.setDeliveryCycleName(WaitDeliveryCycleEnum.IN_TWO_MONTH.getName());
                } else if (obj.getDeliveryCycle() > 60) {
                    obj.setDeliveryCycleName("2个月以上");
                }
            }
            obj.setDeliveryCycleFlag(deliveryCycleFlag);
        });
    }

    @Override
    public PurchaseOrderDTO.ListDTO srmOrderConfirmTotal(PurchaseOrderDTO.SrmSearchParamDTO pagingDTO) {
        PurchaseOrderDTO.ListDTO dto = new PurchaseOrderDTO.ListDTO();
        List<PurchaseOrderDTO.ListDTO> list = this.baseMapper.srmPurchaseOrderList(pagingDTO);
        if (CollectionUtils.isEmpty(list)) {
            return countPurchaseOrder(dto, list);
        }
        //数据赋值处理
        buildPurchaseOrderCount(list);
        //汇总
        return countPurchaseOrder(dto, list);
    }

    @Override
    public List<PurchaseOrderDTO.ListDTO> generateDeliveryList(PurchaseOrderSrmDTO.GenerateDeliveryParamDTO dto) {
        if (CollectionUtils.isEmpty(dto.getPurchaseDetailIds())) {
            return Collections.emptyList();
        }
        List<PurchaseOrderDTO.ListDTO> listDTOS = this.listByDetailIds(dto.getPurchaseDetailIds());
        if (CollectionUtils.isEmpty(listDTOS)) {
            return listDTOS;
        }
        //数据赋值处理
        doWaitDeliveryPurchaseOrder(listDTOS);
        return listDTOS;
    }

    @Override
    public PurchaseStatisticsDTO.StatusDTO statisticsExecutionStatus(PurchaseStatisticsDTO.RequestDTO requestDTO) {
        PurchaseStatisticsDTO.StatusDTO responseDTO = new PurchaseStatisticsDTO.StatusDTO();
        PurchaseOrderSrmDTO.SearchParamDTO params = new PurchaseOrderSrmDTO.SearchParamDTO();
        params.setSupplierId(requestDTO.getSupplierId());
        params.setExecutionStatus(requestDTO.getExecutionStatus());
        params.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        responseDTO.setCount(baseMapper.srmPurchaseOrderCount(params));
        return responseDTO;
    }

    @Override
    public Boolean importEndReceiveFile(MultipartFile excelFile, HttpServletResponse response) {

        PurchaseEndReceiveExcelListener excelListenerUtil = new PurchaseEndReceiveExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseEndReceiveImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseEndReceiveImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<PurchaseEndReceiveImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseEndReceiveImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportEndReceiveFile(successList, errorList);

        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/purchaseEndReceiveError.xlsx";
            String name = "purchaseEndReceiveError";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportSrmExcel(PurchaseOrderDTO.SrmSearchParamDTO dto, HttpServletResponse response) {
        List<PurchaseOrderDTO.ListDTO> list = baseMapper.srmPurchaseOrderList(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //数据处理
        buildPurchaseOrderCount(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/srmPurchaseOrder.xlsx";
        String name = "采购订单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public SupplierUserInfoVO getSrmSupplierUserInfo() {
        LoginUser loginUser = UserContext.getLoginUser();
        if (Objects.isNull(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        String uid = loginUser.getUid();
        //获取用户关联供应商
        SupplierUserInfoVO info = supplierUserService.getById(uid);
        if (Objects.nonNull(info) && org.apache.commons.lang3.StringUtils.isNotEmpty(info.getSupplierId())) {
            //用户是否禁用
            if (Objects.isNull(info.getUserState()) || 0 == info.getUserState()) {
                throw new ServiceException(ApiError.ERROR_9016);
            }
        } else {
            //用户未关联供应商
            throw new ServiceException(ApiError.ERROR_USER_NOT_REL_SUPPLIER);
        }
        return info;
    }

    /**
     * @param successList
     * @param errorList
     * @description: 处理导入采购结束交货
     * @author Will
     * @date: 2024/3/5 11:33
     */
    public void handleImportEndReceiveFile(List<PurchaseEndReceiveImportExcelDTO> successList, List<PurchaseEndReceiveImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //采购订单编码集合
        List<String> codeList = successList.stream().map(PurchaseEndReceiveImportExcelDTO::getCode).collect(Collectors.toList());
        //sku编码集合
        List<String> skuNoList = successList.stream().map(PurchaseEndReceiveImportExcelDTO::getSkuNo).collect(Collectors.toList());
        List<PurchaseOrderDetailDTO.ImportEndReceiveDTO> importEndReceiveList = purchaseOrderDetailService.listImportEndReceive(codeList, skuNoList);

        Map<String, List<PurchaseEndReceiveImportExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(obj -> obj.getCode().concat(obj.getSkuNo())));
        for (Map.Entry<String, List<PurchaseEndReceiveImportExcelDTO>> entry : map.entrySet()) {
            List<PurchaseEndReceiveImportExcelDTO> value = entry.getValue();
            Boolean isError = Boolean.FALSE;
            for (PurchaseEndReceiveImportExcelDTO excelDTO : value) {
                List<String> errorMsgList = new ArrayList<>();
                //判断导入数据是否重复
                if (value.size() > 1) {
                    errorMsgList.add("存在两条相同的数据，请重新导入");
                }
                List<PurchaseOrderDetailDTO.ImportEndReceiveDTO> detailList = importEndReceiveList.stream().filter(obj -> StrUtil.equals(obj.getCode(), excelDTO.getCode())
                                && StrUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo()))
                        .collect(Collectors.toList());
                //验证是否存在未作废数据
                if (CollectionUtils.isEmpty(detailList)) {
                    errorMsgList.add(StrUtil.format("采购订单【{}】SKU【{}】不存在", excelDTO.getCode(), excelDTO.getSkuNo()));
                } else {
                    //存在多条相同sku则不允许更新
                    if (detailList.size() > MathUtil.ONE) {
                        errorMsgList.add(StrUtil.format("系统中存在多条采购订单【{}】SKU【{}】的数据，请在页面上操作", excelDTO.getCode(), excelDTO.getSkuNo()));
                    }
                }
                //存在错误信息则
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    isError = Boolean.TRUE;
                    continue;
                }
                excelDTO.setDetailId(detailList.get(0).getDetailId());
            }
            //结束交货
            if (!isError) {
                List<String> errorMsgList = new ArrayList<>();
                PurchaseEndReceiveImportExcelDTO excelDTO = value.get(0);
                try {
                    purchaseOrderDetailService.finishDelivery(Arrays.asList(excelDTO.getDetailId()), excelDTO.getRemark(), Boolean.TRUE);
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }
                //存在错误信息则
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                }
            }
        }
    }


    private List<PurchaseOrderDTO.ListDTO> listByDetailIds(List<String> purchaseDetailIds) {
        if (CollectionUtils.isEmpty(purchaseDetailIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listByDetailIds(purchaseDetailIds);
    }

    private void buildPurchaseOrderCount(List<PurchaseOrderDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        // 采购订单明细id集合
        List<String> podIds = records.stream().map(PurchaseOrderDTO.ListDTO::getPurchaseDetailId).collect(Collectors.toList());
        List<String> ids = records.stream().map(PurchaseOrderDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = srmDeliveryOrderFeign.listDetailDTOByDetailSourceIds(podIds);
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIds(ids);
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        //根据SKU查询BOM判断是否是组合SKU
        List<String> skuIds = records.stream().map(PurchaseOrderDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //单据类型
        List<DictBasicDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.PURCHASE_ORDER_TYPE.getType());


        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        records.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_ORDER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        //关联信息
        List<PurchaseApplicationRefPoEntity> refList = purchaseApplicationRefPoService.listByPurchaseOrderIds(ids);

        //来源于委外的采购订单
        List<String> subIdList = records.stream().filter(obj -> SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(obj.getSourceType())).map(PurchaseOrderDTO.ListDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractOrderEntity> subcontractOrderList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subIdList)) {
            subcontractOrderList = subcontractOrderService.listByIds(subIdList);
        }

        //来源于采购退货的采购订单
        List<String> poReturnIdList = records.stream().filter(obj -> SourceTypeEnum.PO_RETURN.getCode().equals(obj.getSourceType())).map(PurchaseOrderDTO.ListDTO::getSourceId).collect(Collectors.toList());
        List<PoReturnEntity> purchaseReturnOrderList = wmsTaskFeign.listPoReturnByIdList(poReturnIdList);

        //供应商信息
        List<String> supplierIdList = records.stream().map(PurchaseOrderDTO.ListDTO::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIdList);

        // 采购申请单id集合
        List<String> purchaseApplicationIds = Lists.newArrayList();
        for (PurchaseOrderDTO.ListDTO obj : records) {
            //已收货数量
            Integer receiveQty = MathUtil.ZERO;
            //有送货单的收货数量
            Integer hasDeliveryReceiveQty = MathUtil.ZERO;
            //已送货数量
            Integer deliveredQty = MathUtil.ZERO;
            //无送货单收货数量
            Integer unDeliveryReceiveQty = MathUtil.ZERO;
            //无收货单的入库数量
            Integer unReceiveInstockQty = MathUtil.ZERO;
            //收发差异
            Integer diffSendAndReceive = MathUtil.ZERO;
            //退货补货数量
            Integer returnQty = MathUtil.ZERO;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                hasDeliveryReceiveQty = receiveList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSourceType())
                                && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                && e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                unDeliveryReceiveQty = receiveList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isEmpty(e.getSourceId()) && e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            obj.setReceiveQty(receiveQty);
            //无收货单的入库数量
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                // 采购入库单（无收货单），只有审核通过的才占用库存数量
                unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && Objects.equals(e.getSourceDetailId(), obj.getPurchaseDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            // 退货单（退货补货的才会导致在途数量变化）
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                        .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //入库数量
            Integer stockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                stockInQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            obj.setStockInQty(stockInQty);
            //退货数量
            Integer returnQtyt = returnOrderDetailList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                    && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            obj.setReturnQty(returnQtyt);
            //已送货数量
            if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
                deliveredQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                //收发差异
//                diffSendAndReceive = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId())
//                                && StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
//                        .map(deliveryOrderDetailEntity -> deliveryOrderDetailEntity.getDeliveryQty() - obj.getReceiveQty())
//                        .reduce(MathUtil.ZERO, Integer::sum);
                //发货数量 - 已审核收货数量
                Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId())
                                && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
            }
            obj.setDeliveredQty(deliveredQty);
            //剩余送货量/可下推量=采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
            obj.setWaitDeliveryQty(obj.getPurchaseQty() - deliveredQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty);
            obj.setTaxRateStr(MathUtil.multiplyWithTwo(obj.getTaxRate(), MathUtil.BigDecimal_100).toString().concat("%"));

            //是否是组合SKU
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(obj.getSkuId())).count();
                if (count > 0) {
                    obj.setIsConstitute(Boolean.TRUE);
                }
            }
            //单据类型名称
            if (CollectionUtils.isNotEmpty(dictBasicList)) {
                String typeName = dictBasicList.stream().filter(e -> e.getValue().equals(obj.getType())).findFirst().flatMap(e -> Optional.ofNullable(e.getName())).orElse("");
                obj.setTypeName(typeName);
            }

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setExecutionStatusName(ExecutionStatusEnum.getNameByCode(obj.getExecutionStatus()));
            // 采购申请单号
            if (CollUtil.isNotEmpty(refList) && StringUtils.isBlank(obj.getSourceType())) {
                // 采购申请单明细id和采购订单明细id是多对多，可能存在多条
                List<PurchaseApplicationRefPoEntity> filterRefList = refList.stream().filter(r -> {
                    if (Objects.equals(obj.getId(), r.getPurchaseOrderId())
                            && Objects.equals(obj.getPurchaseDetailId(), r.getPurchaseOrderDetailId())) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(filterRefList)) {
                    List<String> applicationIds = filterRefList.stream().map(PurchaseApplicationRefPoEntity::getPurchaseApplicationId).distinct().collect(Collectors.toList());
                    purchaseApplicationIds.addAll(applicationIds);
                    obj.setPurchaseApplicationIds(applicationIds);
                }

            }
            //委外订单
            if (CollectionUtils.isNotEmpty(subcontractOrderList) && SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(obj.getSourceType())) {
                String subCode = subcontractOrderList.stream().filter(e -> e.getId().equals(obj.getSourceId())).findFirst().flatMap(e -> Optional.ofNullable(e.getCode())).orElse("");
                obj.setSourceCode(subCode);
            }
            //采购退货
            if (CollectionUtils.isNotEmpty(purchaseReturnOrderList) && SourceTypeEnum.PO_RETURN.getCode().equals(obj.getSourceType())) {
                PoReturnEntity poReturnEntity = purchaseReturnOrderList.stream().filter(e -> e.getId().equals(obj.getSourceId())).findFirst().orElse(new PoReturnEntity());
                obj.setSourceCode(poReturnEntity.getCode());
                obj.setReturnMode(poReturnEntity.getReturnMode());
            }

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                obj.setApproveUserName(curApprove);
            }
            //确认类型
            obj.setConfirmTypeName(ConfirmTypeEnum.getNameByCode(obj.getConfirmType()));
            //srm协同
            Boolean srmDisabled = supplierList.stream().filter(e -> StrUtil.equals(e.getId(), obj.getSupplierId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSrmDisabled())).orElse(null);
            obj.setSrmDisabled(srmDisabled);
            obj.setSrmDisabledName(Boolean.TRUE.equals(srmDisabled) ? "未开启" : "已开启");
            //含税单价
            obj.setTaxPriceName(obj.getCurrencySymbol() + obj.getTaxPrice());
            //价税合计
            obj.setPurchaseAmountName(obj.getCurrencySymbol() + obj.getPurchaseAmount());
            //交货周期
            boolean deliveryCycleFlag = false;
            if (Objects.nonNull(obj.getDeliveryCycle())) {
                if (obj.getDeliveryCycle() < 0) {
                    obj.setDeliveryCycleName(String.format("已超期%s天", obj.getDeliveryCycle() * -1));
                    deliveryCycleFlag = true;
                } else if (7 >= obj.getDeliveryCycle()) {
                    obj.setDeliveryCycleName(String.format("%s天后超期", obj.getDeliveryCycle()));
                    deliveryCycleFlag = true;
                } else if (30 >= obj.getDeliveryCycle()) {
                    obj.setDeliveryCycleName(WaitDeliveryCycleEnum.IN_ONE_MONTH.getName());
                } else if (60 >= obj.getDeliveryCycle()) {
                    obj.setDeliveryCycleName(WaitDeliveryCycleEnum.IN_TWO_MONTH.getName());
                } else if (obj.getDeliveryCycle() > 60) {
                    obj.setDeliveryCycleName("2个月以上");
                }
            }
            obj.setDeliveryCycleFlag(deliveryCycleFlag);
        }
        if (CollUtil.isNotEmpty(purchaseApplicationIds)) {
            List<PurchaseApplicationEntity> purchaseApplicationList = purchaseApplicationService.listByIds(purchaseApplicationIds);
            // 采购申请单id和采购申请单对应map
            Map<String, PurchaseApplicationEntity> refMap = purchaseApplicationList.stream().collect(Collectors.toMap(PurchaseApplicationEntity::getId, Function.identity()));

            records.forEach(obj -> {
                if (CollUtil.isNotEmpty(obj.getPurchaseApplicationIds())) {
                    StringBuffer applicationCodes = new StringBuffer("");
                    obj.getPurchaseApplicationIds().stream().forEach(applicationId -> {
                        PurchaseApplicationEntity refEntity = refMap.get(applicationId);
                        if (Objects.nonNull(refEntity)) {
                            applicationCodes.append(refEntity.getCode()).append(",");
                        }
                    });
                    if (applicationCodes.toString().endsWith(",")) {
                        applicationCodes.deleteCharAt(applicationCodes.length() - 1);
                    }
                    obj.setSourceCode(applicationCodes.toString());
                }
            });
        }
    }

    private PurchaseOrderDTO.ListDTO countPurchaseOrder(PurchaseOrderDTO.ListDTO dto, List<PurchaseOrderDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            dto.setPurchaseQty(MathUtil.ZERO);
            dto.setReceiveQty(MathUtil.ZERO);
            dto.setDeliveredQty(MathUtil.ZERO);
            dto.setWaitDeliveryQty(MathUtil.ZERO);
            dto.setStockInQty(MathUtil.ZERO);
            dto.setReturnQty(MathUtil.ZERO);
            dto.setPurchaseAmount(BigDecimal.ZERO);
            return dto;
        }
        dto.setPurchaseQty(list.stream().filter(e -> Objects.nonNull(e.getPurchaseQty())).mapToInt(PurchaseOrderDTO.ListDTO::getPurchaseQty).sum());
        dto.setReceiveQty(list.stream().filter(e -> Objects.nonNull(e.getReceiveQty())).mapToInt(PurchaseOrderDTO.ListDTO::getReceiveQty).sum());
        dto.setDeliveredQty(list.stream().filter(e -> Objects.nonNull(e.getDeliveredQty())).mapToInt(PurchaseOrderDTO.ListDTO::getDeliveredQty).sum());
        dto.setWaitDeliveryQty(list.stream().filter(e -> Objects.nonNull(e.getWaitDeliveryQty())).mapToInt(PurchaseOrderDTO.ListDTO::getWaitDeliveryQty).sum());
        dto.setStockInQty(list.stream().filter(e -> Objects.nonNull(e.getStockInQty())).mapToInt(PurchaseOrderDTO.ListDTO::getStockInQty).sum());
        dto.setReturnQty(list.stream().filter(e -> Objects.nonNull(e.getReturnQty())).mapToInt(PurchaseOrderDTO.ListDTO::getReturnQty).sum());
        dto.setPurchaseAmount(list.stream().map(PurchaseOrderDTO.ListDTO::getPurchaseAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.DOWN));
        return dto;
    }


    private ListStatusCountDTO.PurchaseOrderConfirmCountDTO getSrmConfirmCount(String supplierId, ExecutionStatusEnum typeEnum) {
        PurchaseOrderSrmDTO.SearchParamDTO params = new PurchaseOrderSrmDTO.SearchParamDTO();
        params.setSupplierId(supplierId);
        params.setExecutionStatus(typeEnum.getCode());
        params.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        ListStatusCountDTO.PurchaseOrderConfirmCountDTO dto = new ListStatusCountDTO.PurchaseOrderConfirmCountDTO();
        dto.setType(typeEnum.getCode());
        dto.setName(typeEnum.getName());
        dto.setCount(baseMapper.srmPurchaseOrderCount(params));
        return dto;
    }

    /**
     * @param list
     * @param ids
     * @description: 提交时校验
     * @author Will
     * @date: 2024/1/19 10:47
     */
    private void checkRequiredData(List<PurchaseOrderEntity> list, List<String> ids) {
        //交货仓库校验
        String whCodes = list.stream().filter(obj -> ObjectUtils.isEmpty(obj.getDeliveryWarehouseId())).map(PurchaseOrderEntity::getCode).distinct().collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(whCodes)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_WH_REQUIRED, whCodes);
        }
        //收料组织校验
        String orgCodes = list.stream().filter(obj -> ObjectUtils.isEmpty(obj.getReceiveOrgId())).map(PurchaseOrderEntity::getCode).distinct().collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(orgCodes)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORG_REQUIRED, orgCodes);
        }
        List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailService.listByPurchaseOrderIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //明细预计交货日期校验
        for (PurchaseOrderEntity entity : list) {
            String skuNos = detailList.stream().filter(obj -> entity.getId().equals(obj.getPurchaseOrderId()) && ObjectUtils.isEmpty(obj.getPlanDeliveryDate())).map(PurchaseOrderDetailEntity::getSkuNo).distinct().collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(skuNos)) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_DETAIL_DATE, entity.getCode(), skuNos);
            }
            if (CharSequenceUtil.isBlank(entity.getSupplierAccountId())) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_SUPPLIER_ACCOUNT, entity.getCode());
            }
        }
    }

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> pagingDTO) {
        PurchaseOrderDTO.SrmSearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());

        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        query.setOrders(buildOrders(pagingDTO.getParams().getSortList()));
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.srmWaitDeliveryPaging(query, params);
        List<PurchaseOrderDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        buildPurchaseOrderCount(records);
        return new PagingVO(pageData);
    }

    @Override
    public PurchaseOrderDTO.ListDTO srmWaitDeliveryTotal(PurchaseOrderDTO.SrmSearchParamDTO pagingDTO) {
        PurchaseOrderDTO.ListDTO dto = new PurchaseOrderDTO.ListDTO();
        List<PurchaseOrderDTO.ListDTO> list = this.baseMapper.srmWaitDeliveryList(pagingDTO);
        if (CollectionUtils.isEmpty(list)) {
            return countPurchaseOrder(dto, list);
        }
        //数据赋值处理
        buildPurchaseOrderCount(list);
        //汇总
        return countPurchaseOrder(dto, list);
    }

    @Override
    public PagingVO<BomExportExcelVO> exportPurchaseOrderContract(PagingDTO<String> dto) {
        return null;
    }

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> exportPurchaseOrder(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<PurchaseOrderDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandlePurchaseOrder(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public void exportPurchaseContractPdf(String id, HttpServletResponse response) {
        PurchaseOrderDTO.ExportPdfDTO result = listPurchaseContractPdf(id);
        if (ObjectUtil.isEmpty(result)) {
            throw new ServiceException("未发现采购合同订单数据");
        }
        List<String> base64List = new ArrayList<>();
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.PO_CONTRACT_PDF);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        InputStream inputStream = FastDFSClientUtil.getInputStream(fileTemplateEntity.getUrl());
        if (inputStream == null) {
            log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
            return;
        }

        List<com.erp.model.sys.entity.DictBasicEntity> dictBasicEntity = FeignQuery.create(com.erp.model.sys.entity.DictBasicEntity.class)
                .eq(com.erp.model.sys.entity.DictBasicEntity::getType, "url")
                .eq(com.erp.model.sys.entity.DictBasicEntity::getName, "logo")
                .list();
        //logo url地址
        result.setLogoUrl(FastDFSClientUtil.publicUrl + "/" + dictBasicEntity.get(0).getValue());
        Map<String, Object> map = BeanUtil.beanToMap(result);
        JRBeanCollectionDataSource detail = new JRBeanCollectionDataSource(result.getDetails());
        map.put("detail", detail);
        //JasperHelperUtil.export(FileTypeEnum.PDF.getCode(), "pfd", inputStream, map, result.getDetails());

        byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map, Arrays.asList(result));
        String base = Base64.getEncoder().encodeToString(bytes);
        base64List.add("data:application/pdf;base64," + base);
        PdfUtil.exportBase64ForPdf(response, base64List);
    }

    @Override
    public PagingVO<PurchaseOrderDTO.SourceCodeDTO> purchaseCodePaging(PagingDTO<PurchaseOrderDTO.SourceCodeParamDTO> pagingDTO) {
        PurchaseOrderDTO.SourceCodeParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());

        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        query.setOrders(buildOrders(pagingDTO.getParams().getSortList()));
        IPage<PurchaseOrderDTO.SourceCodeDTO> pageData = this.baseMapper.purchaseCodePaging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseOrderDTO.PurchaseCalcQtyDTO> listAllPurchaseBySkuIdAndSupplier(PurchaseOrderDTO.PurchaseCalcQtyParamsDTO purchaseCalcQtyParamsDTO) {

        List<PurchaseOrderDTO.PurchaseCalcQtyDTO> result = baseMapper.listAllPurchaseBySkuIdAndSupplier(purchaseCalcQtyParamsDTO.getSkuIdList(), purchaseCalcQtyParamsDTO.getSupplierIdList());
        // 采购订单明细id集合
        List<String> podIds = result.stream().map(PurchaseOrderDTO.PurchaseCalcQtyDTO::getPurchaseDetailId).collect(Collectors.toList());
        //入库信息
        List<PoInstockDetailEntity> purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        for (PurchaseOrderDTO.PurchaseCalcQtyDTO obj : result) {
            //已收货数量
            Integer receiveQty = MathUtil.ZERO;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //入库数量
            Integer stockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                stockInQty = purchaseStockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //已收货数量
            obj.setReceiveQty(receiveQty);
            obj.setStockInQty(stockInQty);
        }
        return result;
    }

    @Override
    public List<DeliveryOrderDTO.WaitDeliveryCountDTO> srmWaitDeliveryCount(PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto) {
        WaitDeliveryCycleEnum[] values = WaitDeliveryCycleEnum.values();
        List<DeliveryOrderDTO.WaitDeliveryCountDTO> dtos = new ArrayList<>(values.length);
        String supplierId = dto.getSupplierId();
        for (WaitDeliveryCycleEnum item : values) {
            dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(item.getCode(), item.getName(), baseMapper.srmWaitDeliveryCount(supplierId, item.getCode())));
        }
        return dtos;
    }

    /**
     * @param list
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     */
    private void sendPushTask(List<PurchaseOrderEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeePurchaseOrderService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContractStampStatus(PurchaseOrderDTO.ContractStampStatusParamsDTO dto) {
        List<String> ids = dto.getIds();
        if (CollUtil.isNotEmpty(ids) && StringUtil.isNotBlank(dto.getContractStampStatus())) {
            List<PurchaseOrderEntity> purchaseOrderEntities = listByIds(ids);
            lambdaUpdate()
                    .set(PurchaseOrderEntity::getContractStampStatus, dto.getContractStampStatus())
                    .in(PurchaseOrderEntity::getId, ids)
                    .update();

            //操作日志
            String name = ContractStampStatusEnum.getName(dto.getContractStampStatus());

            for (PurchaseOrderEntity purchaseOrderEntity : purchaseOrderEntities) {
                String oldName = ContractStampStatusEnum.getName(purchaseOrderEntity.getContractStampStatus());
                moduleOperateLogService.addModuleOperateLog(String.format("合同盖章状态由[%s]变更为[%s]", oldName, name), ModuleTypeEnum.PURCHASE_ORDER.getCode(), purchaseOrderEntity.getId(), "合同盖章状态更新");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calSupplierPurchaseQty() {
        List<SupplierPurchaseQuantityEntity> newList = baseMapper.listSupplierPurchaseQty();
        if (CollUtil.isNotEmpty(newList)) {
            List<SupplierPurchaseQuantityEntity> oldList = supplierPurchaseQuantityService.list();

            if (CollUtil.isEmpty(oldList)) {
                // oldList为空，直接批量插入newList
                supplierPurchaseQuantityService.saveBatch(newList);
            } else {
                // 将oldList转换为Map，便于后续查找，key为supplierId+skuId组合
                Map<String, SupplierPurchaseQuantityEntity> oldMap = oldList.stream()
                        .collect(Collectors.toMap(
                                entity -> entity.getSupplierId() + "-" + entity.getSkuId(),
                                entity -> entity));

                // 遍历newList，判断是更新还是插入
                for (SupplierPurchaseQuantityEntity newEntity : newList) {
                    String key = newEntity.getSupplierId() + "-" + newEntity.getSkuId();
                    SupplierPurchaseQuantityEntity oldEntity = oldMap.get(key);

                    if (Objects.isNull(oldEntity)) {
                        // 插入新记录
                        supplierPurchaseQuantityService.save(newEntity);
                    } else {
                        // 执行更新操作
                        supplierPurchaseQuantityService.lambdaUpdate()
                                .set(SupplierPurchaseQuantityEntity::getSupplierQty,newEntity.getSupplierQty())
                                .set(SupplierPurchaseQuantityEntity::getSkuTotalQty,newEntity.getSkuTotalQty())
                                .set(SupplierPurchaseQuantityEntity::getPurchaseRatio,newEntity.getPurchaseRatio())
                                .eq(SupplierPurchaseQuantityEntity::getId, oldEntity.getId())
                                .update();
                    }
                }
            }
        }
    }

    @Override
    public List<PurchaseOrderDTO.SupplierSkuDTO> listSkuBySupplierIds(List<String> supplierIds) {
        if(CollUtil.isEmpty(supplierIds)){
            return Collections.emptyList();
        }
        return baseMapper.listSkuBySupplierIds(supplierIds);
    }

    @Override
    public PagingVO<PurchaseOrderDTO.AdjustListDTO> adjustPaging(PagingDTO<PurchaseOrderDTO.SearchAdjustParamDTO> pagingDTO) {
        PurchaseOrderDTO.SearchAdjustParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        PurchasePriceChangeDetailEntity entity = purchasePriceChangeDetailService.getById(pagingDTO.getParams().getPurchasePriceChangeDetailId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        params.setSkuId(entity.getSkuId());
        params.setMinQty(entity.getMinQty());
        params.setMaxQty(entity.getMaxQty());
        params.setSupplierId(entity.getSupplierId());
        IPage<PurchaseOrderDTO.AdjustListDTO> pageData = this.baseMapper.adjustPaging(query, params);
        handleAdjustPaging(pageData.getRecords(),entity);
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportAdjustExcel(PurchaseOrderDTO.SearchAdjustParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史未完结订单", EXPORT_SCM_PURCHASE_ORDER_ADJUST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdjustPrice(PurchaseOrderDTO.AdjustPriceDTO dto) {
        PurchasePriceChangeDetailEntity priceChangeDetailEntity = purchasePriceChangeDetailService.getById(dto.getPurchasePriceChangeDetailId());
        if (ObjectUtil.isEmpty(priceChangeDetailEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"采购调价明细");
        }
        PurchasePriceChangeEntity priceChangeEntity = purchasePriceChangeService.getById(priceChangeDetailEntity.getPurchasePriceChangeId());
        if (ObjectUtil.isEmpty(priceChangeEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"采购调价");
        }
        if (!CharSequenceUtil.equals(priceChangeEntity.getApproveStatus().getStatus(),ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_CHANGE_APPROVE_STATUS);
        }
        //获取调价表对应价目明细的最新数据，存在则本条调价数据属于历史数据不支持批量调价，不存在则属于最新调价可以直接更新
        PurchasePriceChangeDetailEntity latestPriceChangeDetail = purchasePriceChangeDetailService.getLatest(priceChangeDetailEntity.getPurchasePriceDetailId(),priceChangeDetailEntity.getId());
        if (ObjectUtil.isNotEmpty(latestPriceChangeDetail)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_CHANGE_ADJUST);
        }

        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(dto.getDetailIdList());
        if (CollUtil.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        Map<String, List<PurchaseOrderDetailEntity>> podMap = purchaseOrderDetailList.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getPurchaseOrderId));

        List<String> poIdList = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).distinct().collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = this.listByIds(poIdList);
        if (CollUtil.isEmpty(purchaseOrderList)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //供应商信息
        List<PurchaseOrderSupplierEntity> purchaseOrderSupplierList = purchaseOrderSupplierService.listByPurchaseOrderIds(poIdList);
        if (CollUtil.isEmpty(purchaseOrderSupplierList)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        Map<String, String> poSupplierMap = purchaseOrderSupplierList.stream().collect(Collectors.toMap(PurchaseOrderSupplierEntity::getPurchaseOrderId, PurchaseOrderSupplierEntity::getSupplierId));

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        SysDepartmentUserNumberDTO departmentUserNumberDTO = sysUserFeign.getDeptByUserId(userInfo.getUid());
        if (ObjectUtil.isEmpty(departmentUserNumberDTO)) {
            throw new ServiceException(ApiError.ERROR_9029);
        }
        for (PurchaseOrderEntity purchaseOrderEntity : purchaseOrderList) {
            //已审核走变更
            if (ApproveStatusEnum.APPROVE.getStatus().equals(purchaseOrderEntity.getApproveStatus())) {
                batchAdjustAddChange(purchaseOrderEntity,userInfo,departmentUserNumberDTO,podMap,poSupplierMap,priceChangeDetailEntity);
            } else if (ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(purchaseOrderEntity.getApproveStatus()) || ApproveStatusEnum.REJECT.getStatus().equals(purchaseOrderEntity.getApproveStatus())) {
                batchAdjustUpdatePurchaseOrder(purchaseOrderEntity,podMap,priceChangeDetailEntity);
            } else {
                throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_ADJUST_PRICE);
            }
        }
        return Boolean.TRUE;
    }


    /**
     *
     * @author will
     * @date 2025/8/5 18:09
     * @param purchaseOrderEntity
     * @param podMap
     * @param priceChangeDetailEntity
     * @return void
     */
    private void batchAdjustUpdatePurchaseOrder (PurchaseOrderEntity purchaseOrderEntity,Map<String, List<PurchaseOrderDetailEntity>> podMap,PurchasePriceChangeDetailEntity priceChangeDetailEntity) {
        List<PurchaseOrderDetailEntity> detailList = podMap.get(purchaseOrderEntity.getId());
        if (CollUtil.isEmpty(detailList)) {
            log.error("采购订单{}未找到采购订单明细信息", purchaseOrderEntity.getCode());
            throw new ServiceException(ApiError.ERROR_98026);
        }
        for (PurchaseOrderDetailEntity detailEntity : detailList) {
            detailEntity.setTaxPrice(priceChangeDetailEntity.getTaxPrice());
            detailEntity.setTaxRate(priceChangeDetailEntity.getTaxRate());
            detailEntity.setPurchaseAmount(MathUtil.multiplyWithTwo(detailEntity.getTaxPrice(),detailEntity.getPurchaseQty()));
            detailEntity.setUpdateTime(LocalDateTime.now());
            detailEntity.setUpdateUserId(UserContext.getDefaultLoginUser().getUid());
        }
        purchaseOrderDetailService.updateBatchById(detailList);
        //添加日志
        moduleOperateLogService.addModuleOperateLog(CharSequenceUtil.format("采购订单明细含税单价调整为[{}],税率调整为[{}]", priceChangeDetailEntity.getTaxPrice().toString(),MathUtil.multiplyWithTwo(priceChangeDetailEntity.getTaxRate(),MathUtil.BigDecimal_100).toString().concat("%"))
                , ModuleTypeEnum.PURCHASE_ORDER.getCode(), purchaseOrderEntity.getId(), "采购订单明细调价");
    }

    /**
     * 新增数据
     * @author will
     * @date 2025/8/5 17:01
     * @param purchaseOrderEntity
     * @param userInfo
     * @param departmentUserNumberDTO
     * @param podMap
     * @param poSupplierMap
     * @return void
     */
    private void batchAdjustAddChange (PurchaseOrderEntity purchaseOrderEntity,LoginUser userInfo,SysDepartmentUserNumberDTO departmentUserNumberDTO
            ,Map<String, List<PurchaseOrderDetailEntity>> podMap,Map<String, String> poSupplierMap,PurchasePriceChangeDetailEntity priceChangeDetailEntity) {
        PurchaseChangeDTO.AddDTO addDTO = new PurchaseChangeDTO.AddDTO();
        addDTO.setPurchaseOrderId(purchaseOrderEntity.getId());
        addDTO.setPurchaseOrgId(purchaseOrderEntity.getPurchaseOrgId());
        addDTO.setType(purchaseOrderEntity.getType());
        addDTO.setChangeDate(LocalDate.now());
        addDTO.setChangeUserId(userInfo.getUid());
        addDTO.setChangeDeptId(departmentUserNumberDTO.getDepartmentId());

        String supplierId = poSupplierMap.get(purchaseOrderEntity.getId());
        if (CharSequenceUtil.isBlank(supplierId)) {
            log.error("采购订单{}未找到供应商信息", purchaseOrderEntity.getCode());
            throw new ServiceException(ApiError.ERROR_98036);
        }
        addDTO.setSupplierId(supplierId);
        List<PurchaseOrderDetailEntity> detailList = podMap.get(purchaseOrderEntity.getId());
        if (CollUtil.isEmpty(detailList)) {
            log.error("采购订单{}未找到采购订单明细信息", purchaseOrderEntity.getCode());
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<PurchaseChangeDetailDTO.AddDTO> details = new ArrayList<>();
        for (PurchaseOrderDetailEntity detailEntity : detailList) {
            PurchaseChangeDetailDTO.AddDTO detailAddDTO = new PurchaseChangeDetailDTO.AddDTO();
            detailAddDTO.setPurchaseOrderDetailId(detailEntity.getId());
            detailAddDTO.setSkuId(detailEntity.getSkuId());
            detailAddDTO.setSkuNo(detailEntity.getSkuNo());
            detailAddDTO.setProductName(detailEntity.getProductName());
            detailAddDTO.setOldQty(detailEntity.getPurchaseQty());
            detailAddDTO.setOldPrice(detailEntity.getTaxPrice());
            detailAddDTO.setOldAmount(MathUtil.multiplyWithTwo(detailAddDTO.getOldPrice(),detailAddDTO.getOldQty()));
            detailAddDTO.setQty(detailEntity.getPurchaseQty());
            detailAddDTO.setPrice(priceChangeDetailEntity.getTaxPrice());
            detailAddDTO.setAmount(MathUtil.multiplyWithTwo(detailAddDTO.getPrice(),detailAddDTO.getQty()));
            detailAddDTO.setCurrency(detailEntity.getCurrency());
            detailAddDTO.setCurrencySymbol(detailEntity.getCurrencySymbol());
            detailAddDTO.setFirstMassProduct(detailEntity.getFirstMassProduct());
            details.add(detailAddDTO);
        }
        addDTO.setDetails(details);
        purchaseChangeService.add(addDTO);
    }

    @Override
    public Boolean importMainFile(MultipartFile excelFile, HttpServletResponse response) {
        PurchaseOrderMainExcelListener excelListenerUtil = new PurchaseOrderMainExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseOrderMainExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseOrderMainExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<PurchaseOrderMainExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseOrderMainExcelDTO> errorList = excelListenerUtil.getErrorList();
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
        String excelPath = "excel/purchaseOrderMainError.xlsx";
        String name = "purchaseOrderMainError";
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
    private void handleMainFile (List<PurchaseOrderMainExcelDTO> successList, List<PurchaseOrderMainExcelDTO> errorList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        //产品信息
        List<String> skuNos = successList.stream().map(PurchaseOrderMainExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNos)
                .eq(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_PASS.getCode())
                .list();
        //仓库信息
        List<String> warehouseNames = successList.stream().map(PurchaseOrderMainExcelDTO::getDeliveryWarehouseName).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.create(WarehouseEntity.class)
                .in(WarehouseEntity::getName, warehouseNames)
                .eq(WarehouseEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus())
                .eq(WarehouseEntity::getDisabled,Boolean.FALSE)
                .list();
        Map<String, WarehouseEntity> warehosueMap = CollUtil.isEmpty(warehouseList) ? new HashMap<>() : warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getName, Function.identity()));

        //供应商信息
        List<String> supplierNames = successList.stream().map(PurchaseOrderMainExcelDTO::getSupplierName).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.lambdaQuery()
                .in(SupplierEntity::getName,supplierNames)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus())
                .eq(SupplierEntity::getDisabled,Boolean.FALSE)
                .list();
        Map<String, SupplierEntity> supplierMap = CollUtil.isEmpty(supplierList) ? new HashMap<>() : supplierList.stream().collect(Collectors.toMap(SupplierEntity::getName, Function.identity()));

        List<String> supplierIdList = CollUtil.isEmpty(supplierList) ? Collections.emptyList() : supplierList.stream().map(SupplierEntity::getId).distinct().collect(Collectors.toList());

        //供应商联系人信息
        List<String> contactNames = successList.stream().map(PurchaseOrderMainExcelDTO::getContactName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SupplierContactEntity> supplierContactList = supplierContactService.listByNameList(contactNames,supplierIdList);

        //供应商账户信息
        List<String> supplierAccountNames = successList.stream().map(PurchaseOrderMainExcelDTO::getSupplierAccountName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SupplierAccountEntity> supplierAccountList = supplierAccountService.listByNameList(supplierAccountNames,supplierIdList);

        //采购组织
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        Map<String, BaseIdDTO> orgMap = companyList.stream().collect(Collectors.toMap(BaseIdDTO::getName, Function.identity()));

        //结算方式
        List<String> payMethodNames = successList.stream().map(PurchaseOrderMainExcelDTO::getPayMethodName).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = dictBasicService.listByNameList(payMethodNames,DictBasicEnum.SUPPLIER_PAY_MODE);
        Map<String, DictBasicEntity> payMethodMap = CollUtil.isEmpty(dictBasicList) ? new HashMap<>() : dictBasicList.stream().collect(Collectors.toMap(DictBasicEntity::getName, Function.identity()));

        //付款条件
        List<String> paymentConditionNames = successList.stream().map(PurchaseOrderMainExcelDTO::getPaymentConditionName).distinct().collect(Collectors.toList());
        List<KingdeePaymentConditionEntity> kingdeePaymentConditionList = kingdeePaymentConditionService.listByNameList(paymentConditionNames);
        Map<String, KingdeePaymentConditionEntity> paymentConditionMap = CollUtil.isEmpty(kingdeePaymentConditionList) ? new HashMap<>() : kingdeePaymentConditionList.stream().collect(Collectors.toMap(KingdeePaymentConditionEntity::getName, Function.identity()));

        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //部门
        SysDepartmentUserNumberDTO departmentUserNumberDTO = sysUserFeign.getDeptByUserId(userInfo.getUid());

        Map<String, List<PurchaseOrderMainExcelDTO>> excelMap = successList.stream().collect(Collectors.groupingBy(PurchaseOrderMainExcelDTO::getIndex));
        for ( Map.Entry<String, List<PurchaseOrderMainExcelDTO>> entry : excelMap.entrySet()) {
            List<PurchaseOrderMainExcelDTO> value = entry.getValue();
            PurchaseOrderMainExcelDTO mainExcelDTO = value.get(0);
            PurchaseOrderDTO.AddDTO addDTO = new PurchaseOrderDTO.AddDTO();
            //注解验证信息
            List<String> mainErrorMsgList = new ArrayList<>();
            //仓库验证
            WarehouseEntity warehouseEntity = warehosueMap.get(mainExcelDTO.getDeliveryWarehouseName());
            if (ObjectUtil.isEmpty(warehouseEntity)) {
                mainErrorMsgList.add(CharSequenceUtil.format("未找到审核通过并启用的仓库名称"));
            }
            //采购组织验证
            BaseIdDTO company = orgMap.get(mainExcelDTO.getPurchaseOrgName());
            if (ObjectUtil.isEmpty(company)) {
                mainErrorMsgList.add(CharSequenceUtil.format("未找到采购组织"));
            }
            //供应商信息验证
            SupplierEntity supplierEntity = supplierMap.get(mainExcelDTO.getSupplierName());
            String supplierAccountId = "";
            String supplierContactId = "";
            if (ObjectUtil.isEmpty(supplierEntity)) {
                mainErrorMsgList.add(CharSequenceUtil.format("未找到审核通过并启用的供应商"));
            } else {
                //账户名称验证
                if (CharSequenceUtil.isNotBlank(mainExcelDTO.getSupplierAccountName())) {
                    SupplierAccountEntity supplierAccountEntity = supplierAccountList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPayee(), mainExcelDTO.getSupplierAccountName()) && CharSequenceUtil.equals(obj.getSupplierId(), supplierEntity.getId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(supplierAccountEntity)) {
                        mainErrorMsgList.add(CharSequenceUtil.format("未找到账户名称"));
                    } else {
                        supplierAccountId = supplierAccountEntity.getId();
                    }
                } else {
                    //取默认账户,无默认则无需取值
                    SupplierAccountEntity supplierAccountEntity = supplierAccountList.stream().filter(obj -> obj.getIsDefault() && CharSequenceUtil.equals(obj.getSupplierId(), supplierEntity.getId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(supplierAccountEntity)) {
                        supplierAccountId = supplierAccountEntity.getId();
                    }
                }
                //联系人名称验证
                if (CharSequenceUtil.isNotBlank(mainExcelDTO.getContactName())) {
                    SupplierContactEntity supplierContactEntity = supplierContactList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPerson(), mainExcelDTO.getContactName()) && CharSequenceUtil.equals(obj.getSupplierId(), supplierEntity.getId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(supplierContactEntity)) {
                        mainErrorMsgList.add(CharSequenceUtil.format("未找到联系人名称"));
                    } else {
                        supplierContactId = supplierContactEntity.getId();
                    }
                } else {
                    //取默认联系人,无默认则无需取值
                    SupplierContactEntity supplierContactEntity = supplierContactList.stream().filter(obj -> obj.getIsDefault() && CharSequenceUtil.equals(obj.getSupplierId(), supplierEntity.getId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(supplierContactEntity)) {
                        supplierContactId = supplierContactEntity.getId();
                    }
                }
            }

            //结算方式验证
            String payMethodId = "";
            if (CharSequenceUtil.isNotBlank(mainExcelDTO.getPayMethodName())) {
                DictBasicEntity dictBasicEntity = payMethodMap.get(mainExcelDTO.getPayMethodName());
                if (ObjectUtil.isEmpty(dictBasicEntity)) {
                    mainErrorMsgList.add(CharSequenceUtil.format("未找到结算方式"));
                } else {
                    payMethodId = dictBasicEntity.getId();
                }
            } else {
                payMethodId = ObjectUtil.isNotEmpty(supplierEntity) ? supplierEntity.getPayMethodId() : "";
            }

            //付款条件验证
            String paymentCondition = "";
            if (CharSequenceUtil.isNotBlank(mainExcelDTO.getPaymentConditionName())) {
                KingdeePaymentConditionEntity kingdeePaymentConditionEntity = paymentConditionMap.get(mainExcelDTO.getPaymentConditionName());
                if (ObjectUtil.isEmpty(kingdeePaymentConditionEntity)) {
                    mainErrorMsgList.add(CharSequenceUtil.format("未找到付款条件"));
                } else {
                    paymentCondition = kingdeePaymentConditionEntity.getCode();
                }
            } else {
                paymentCondition = ObjectUtil.isNotEmpty(supplierEntity) ? supplierEntity.getPaymentCondition() : "";
            }

            //存在错误直接返回
            if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                value.forEach(obj -> obj.setErrorMsg(FieldValidUtil.getMsgSort(mainErrorMsgList)));
                errorList.addAll(value);
                continue;
            }
            addDTO.setDeliveryWarehouseId(warehouseEntity.getId());
            addDTO.setDeliveryWarehouseName(warehouseEntity.getName());
            addDTO.setPurchaseOrgId(company.getId());
            addDTO.setPurchaseOrgName(company.getName());
            addDTO.setPurchaseDate(LocalDateUtil.parseStrToLocalDate(mainExcelDTO.getPurchaseDateStr()));
            addDTO.setPurchaseUserId(userInfo.getUid());
            addDTO.setPurchaseDeptId(ObjectUtil.isEmpty(departmentUserNumberDTO) ? "" : departmentUserNumberDTO.getDepartmentId());
            addDTO.setType(PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode());
            addDTO.setSupplierAccountId(supplierAccountId);
            //供应商信息
            PurchaseOrderSupplierDTO.AddDTO supplierAddDTO = new PurchaseOrderSupplierDTO.AddDTO();
            supplierAddDTO.setSupplierId(supplierEntity.getId());
            supplierAddDTO.setPayCurrency(supplierEntity.getPayCurrency());
            supplierAddDTO.setPayMethodId(payMethodId);
            supplierAddDTO.setPayMethodName(mainExcelDTO.getPayMethodName());
            supplierAddDTO.setPaymentCondition(paymentCondition);
            supplierAddDTO.setPaymentConditionName(mainExcelDTO.getPaymentConditionName());
            supplierAddDTO.setContactTelNumber(mainExcelDTO.getContactTelNumber());
            supplierAddDTO.setSupplierContactId(supplierContactId);
            addDTO.setPurchaseOrderSupplierDTO(supplierAddDTO);

            List<PurchaseOrderDetailDTO.AddDTO> details = new ArrayList<>();

            for (PurchaseOrderMainExcelDTO excelDTO : value) {
                //注解验证信息
                List<String> errorMsgList = new ArrayList<>();
                //SKU验证
                ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    errorMsgList.add(CharSequenceUtil.format("未找到审核通过的SKU"));
                }
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                PurchaseOrderDetailDTO.AddDTO addDetailDTO = new PurchaseOrderDetailDTO.AddDTO();
                addDetailDTO.setSkuId(productDetailEntity.getId());
                addDetailDTO.setSkuNo(productDetailEntity.getSkuNo());
                addDetailDTO.setPurchaseQty(Integer.valueOf(excelDTO.getPurchaseQtyStr()));
                addDetailDTO.setPlanDeliveryDate(LocalDateUtil.parseStrToLocalDate(mainExcelDTO.getPlanDeliveryDateStr()));
                addDetailDTO.setFirstMassProduct(FirstMassProductTypeEnum.getCode(excelDTO.getFirstMassProductName()));
                addDetailDTO.setFirstMassProductName(excelDTO.getFirstMassProductName());
                addDetailDTO.setIsUrgent(BooleanEnum.getByName(excelDTO.getIsUrgentStr()));
                addDetailDTO.setIsGift(BooleanEnum.getByName(excelDTO.getIsGiftStr()));
                addDetailDTO.setRemark(excelDTO.getRemark());
                details.add(addDetailDTO);
            }
            if (CollUtil.isEmpty(details)) {
                continue;
            }
            addDTO.setDetails(details);
            //新增错误信息
            List<String> addErrorMsgList = new ArrayList<>();
            Boolean isPriceError = Boolean.FALSE;
            try {
                //新增采购订单
                selfService.add(addDTO);
            } catch (ServiceException e) {
                if (ApiError.ERROR_PURCHASE_PRICE_SKU.code.equals(e.getCode())) {
                    addErrorMsgList.addAll(Arrays.stream(e.getMsg().split(",")).collect(Collectors.toList()));
                    isPriceError = Boolean.TRUE;
                } else {
                    addErrorMsgList.add(e.getMessage());
                }
            }
            //新增返回错误
            if (CollUtil.isNotEmpty(addErrorMsgList)) {
                for (PurchaseOrderMainExcelDTO excelDTO : value) {
                    if (isPriceError) {
                        addErrorMsgList.stream().filter(obj -> CharSequenceUtil.contains(obj,excelDTO.getSkuNo())).findFirst().ifPresent(obj -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(Collections.singletonList(obj))));
                    } else {
                        excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(addErrorMsgList));
                    }
                }
                errorList.addAll(value);
            }
        }
    }

    /**
     * 处理分页查询数据
     * @author will
     * @date 2025/7/29 15:36
     * @param records
     * @return void
     */
    private void handleAdjustPaging(List<PurchaseOrderDTO.AdjustListDTO> records,PurchasePriceChangeDetailEntity entity) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        for (PurchaseOrderDTO.AdjustListDTO adjustListDTO : records) {
            //待调整单价
            adjustListDTO.setAdjustTaxPrice(entity.getTaxPrice());
            //审核状态名称
            adjustListDTO.setApproveStatusName(ApproveStatusEnum.getName(adjustListDTO.getApproveStatus()));
            //执行状态名称
            adjustListDTO.setExecutionStatusName(ExecutionStatusEnum.getNameByCode(adjustListDTO.getExecutionStatus()));
            //税率
            adjustListDTO.setTaxRateStr(MathUtil.multiplyWithTwo(adjustListDTO.getTaxRate(), MathUtil.BigDecimal_100).toString().concat("%"));

            if (MathUtil.compareTo(entity.getTaxPrice(), adjustListDTO.getTaxPrice()) == 0) {
                adjustListDTO.setIsSame(Boolean.TRUE);
            } else {
                adjustListDTO.setIsSame(Boolean.FALSE);
            }
            //单价是否一致
            adjustListDTO.setIsSameName(adjustListDTO.getIsSame() ? "一致" : "不一致");
        }
    }
}