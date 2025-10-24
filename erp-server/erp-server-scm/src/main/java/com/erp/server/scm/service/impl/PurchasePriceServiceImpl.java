package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.ImportPurchasePriceExcelDTO;
import com.erp.model.scm.dto.excel.PurchasePriceExportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchasePriceTabFlagEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.listener.PurchasePriceExcelListener;
import com.erp.server.scm.mapper.PurchasePriceMapper;
import com.erp.server.scm.query.PurchasePriceQueryHandler;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_PRICE;

/**
 * <p>
 * 采购价目表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceServiceImpl extends SuperServiceImpl<PurchasePriceMapper, PurchasePriceEntity> implements PurchasePriceService {


    @Resource
    private SupplierService supplierService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PurchasePriceDetailService priceDetailService;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private SyncKingdeePurchasePriceService syncKingdeePurchasePriceService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private PurchasePriceQueryHandler purchasePriceQueryHandler;

    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;

    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Lazy
    @Resource
    private PurchaseSkuOrgRefService purchaseSkuOrgRefService;

    /**
     * 添加采购价目表
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     * @author yl
     * @date 2023-03-24 12:22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public PurchasePriceEntity add(PurchasePriceDTO.AddDTO dto) {
        //供应商id
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        PurchasePriceEntity purchasePrice = new PurchasePriceEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, purchasePrice);
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGJM);
        purchasePrice.setCode(code);
        purchasePrice.setId(id);
        String pricingUserId = dto.getPricingUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            purchasePrice.setPricingUserName(user != null ? user.getUserName() : "");
        }

        String orgId = dto.getPurchaseOrgId();
        //获取组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            purchasePrice.setPurchaseOrgName(orgList.get(0).getName());
        }
        Boolean addResult = this.save(purchasePrice);
        //保存成功
        if (addResult) {
            Class<PurchasePriceEntity> credentialClass = PurchasePriceEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
            /**
             * 添加明细
             */
            priceDetailService.addPriceDetail(id, dto.getPurchasePriceDetailList());
            //添加日志
            String content = String.format("新增了一个{%s}-采购价目-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), id, "新增操作");
            return purchasePrice;

        }
        return null;
    }


    /**
     * 添加日志
     *
     * @param content
     * @param code
     * @param businessId
     * @param operation
     * @return void
     * @author yl
     * @date 2023-03-27 12:19
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);

    }

    /**
     * 获取采购价目详情
     *
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceDTO.ViewDTO
     * @author yl
     * @date 2023-03-27 9:11
     */
    @Override
    public PurchasePriceDTO.ViewDTO view(String id) {
        PurchasePriceEntity purchasePrice = this.getById(id);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        PurchasePriceDTO.ViewDTO viewDTO = new PurchasePriceDTO.ViewDTO();
        BeanMapper.copy(purchasePrice, viewDTO);
        viewDTO.setApproveStatus(purchasePrice.getApproveStatus().getStatus());
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachmentNameList(attachmentNameList);
        viewDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<PurchasePriceDetailDTO.ViewDTO> purchasePriceDetailList = priceDetailService.getByPurchasePriceId(id);


        List<String> skuIds = purchasePriceDetailList.stream().map(PurchasePriceDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
        purchasePriceDetailList.forEach(req -> {
            SkuVO skuVO = skuNoList.stream().filter(obj -> obj.getSkuId().equals(req.getSkuId())).findFirst().orElse(new SkuVO());
            req.setProductName(skuVO.getSkuName());
        });

        viewDTO.setPurchasePriceDetailList(purchasePriceDetailList);

        //供应商信息
        SupplierDTO.ViewDTO supplierDTO = supplierService.getBySupplierId(purchasePrice.getSupplierId());
        viewDTO.setSupplierContactName(supplierDTO.getPerson());
        viewDTO.setContactTelNumber(supplierDTO.getTelNumber());

        String paymentConditionCode = supplierDTO.getPaymentCondition();

        //付款条件
        KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
        String paymentConditionName = paymentCondition != null ? paymentCondition.getName() :"";
        viewDTO.setPaymentConditionName(paymentConditionName);
        return viewDTO;
    }


    /**
     * 修改采购价目
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     * @author yl
     * @date 2023-03-27 10:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePriceEntity updatePurchasePrice(PurchasePriceDTO.UpdateDTO dto) {
        String id = dto.getId();
        PurchasePriceEntity purchasePrice = this.getById(id);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        ApproveStatusEnum status = purchasePrice.getApproveStatus();
        PurchasePriceEntity old = new PurchasePriceEntity();
        BeanMapper.copy(purchasePrice, old);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(status.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98019);
        }
        BeanMapper.copy(dto, purchasePrice);
        //编号
        String code = purchasePrice.getCode();
        purchasePrice.setCode(code);
        purchasePrice.setApproveStatus(status);

        String pricingUserId = dto.getPricingUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            purchasePrice.setPricingUserName(user != null ? user.getUserName() : "");
        }

        //修改成功
        boolean result = this.updateById(purchasePrice);
        if (result) {
            /**
             * 添加修改日志
             */
            moduleOperateLogService.addModuleOperateLogByObj(old, purchasePrice, ModuleTypeEnum.PURCHASE_PRICE.getCode(), id, "", "");

            Class<PurchasePriceEntity> credentialClass = PurchasePriceEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
            //修改明细
            priceDetailService.updatePriceDetail(id, dto.getPurchasePriceDetailList());
            return purchasePrice;
        }
        return null;
    }

    /**
     * 保存并提交审核 价目
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 11:46
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public PurchasePriceEntity addAndSubmit(PurchasePriceDTO.AddDTO dto) {
        PurchasePriceEntity entity = this.add(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        entity = this.getById(entity.getId());
        this.submitEntity(entity);
        return entity;
    }


    /**
     * 修改并审核采购价目
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 11:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public PurchasePriceEntity updateAndSubmit(PurchasePriceDTO.UpdateDTO dto) {
        PurchasePriceEntity entity = this.updatePurchasePrice(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        this.submitEntity(entity);
        return entity;
    }


    /**
     * 批量删除采购价目信息
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO deleteEntity(PurchasePriceEntity entity) {
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = Stream.of(entity).filter(p -> !p.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        //删除价目表
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除价目表[%s]";
            List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "删除");
            attachmentService.deleteByBusinessIds(ids);

            //发送金蝶
            sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());
            return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        }
    }


    /**
     * 采购价目表 提交审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submitEntity(PurchasePriceEntity entity) {
        List<PurchasePriceEntity> list = Collections.singletonList(entity);

        //校验附件信息
        checkSubmitData(entity.getId());
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //提交流程
        startProcess(list);

        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(waitSubmitStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "状态变更");

            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.PURCHASE_PRICE.getCode(), rejectPairList, "状态变更");
            return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
        }

    }

    /**
     * 审核
     *
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:29
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO approve(PurchasePriceEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        approveProcess(entity, type, comment);
        //添加日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购价目【%s】", ApproveTypeEnum.getName(type), entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.PURCHASE_PRICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * @param entity
     * @param type
     * @param comment
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(PurchasePriceEntity entity, String type, String comment) {
        if (ObjectUtils.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(type);
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), approveStatus);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        //审核通过发送金蝶
        if (type.equals(ScmConstant.PASS)) {
            //发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }


    /**
     * 取消流程
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 14:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO cancelProcessEntity(PurchasePriceEntity entity) {
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = Stream.of(entity).filter(s -> !s.getApproveStatus().getStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        List<String> ids = Collections.singletonList(entity.getId());

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.getByStatus(waitSubmitStatus));
        if (result) {
            String content = String.format("状态由[%s]变更为[%s] ", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            List<Pair<String, String>> pairList = Stream.of(entity).
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "取消流程");
            return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
        }
    }


    /**
     * 采购价目表明细
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchasePriceDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-27 14:43
     */
    @Override
    public PagingVO<PurchasePriceDTO.PagingViewDTO> paging(PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        PurchasePriceDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<PurchasePriceDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> skuIds = list.stream().map(PurchasePriceDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
            List<String> currencyIdList = list.stream().map(PurchasePriceDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
            //币种信息
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

            //最新审核人
            ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
            list.forEach(obj -> {
                dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_PRICE.getCode(), obj.getId()));
            });
            ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
            if (CollectionUtils.isNotEmpty(dtoList)) {
                listApiResult = workflowFeign.curApprover(dtoList);
                Integer code = listApiResult.getCode();
                if (200 != code) {
                    throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
                }
            }

            for (PurchasePriceDTO.PagingViewDTO item : list) {
                SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
                item.setProductName(skuVO.getSkuName());
                ApproveStatusEnum approveStatusEnum = item.getApproveStatus();
                item.setApproveStatusCode(approveStatusEnum.getStatus());
                item.setApproveStatusName(approveStatusEnum.getName());
                //币种
                String currency = item.getCurrency();
                String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
                item.setCurrencySymbol(currencySymbol);

                //最新审核人
                if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                    String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                    item.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,item.getApproveUserName()));
                }
            }
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 采购价目表导出
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-27 17:55
     */
    @Override
    public void exportPurchasePrice(PurchasePriceDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购价目数据", EXPORT_SCM_PURCHASE_PRICE.getCode(), dto);
    }


    /**
     * 删除供应商的时候后 看是否有关联 如果有就不能删除
     *
     * @param supplierIds
     * @return void
     * @author yl
     * @date 2023-04-14 11:00
     */
    @Override
    public void checkIsRefSupplier(List<String> supplierIds) {
        if (CollectionUtils.isNotEmpty(supplierIds)) {
            long count = lambdaQuery().in(PurchasePriceEntity::getSupplierId, supplierIds).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_98052);
            }
        }

    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(PurchasePriceEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchasePriceEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    /**
     * 批量保存日志
     *
     * @param content
     * @param code
     * @param pairList
     * @param operation
     * @return void
     * @author yl
     * @date 2023-03-27 12:16
     */
    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);

    }


    /**
     * 修改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:13
     */
    private Boolean updateApproveStatus(List<PurchasePriceEntity> list, ApproveStatusEnum statusEnum) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(obj -> {
                if (ApproveStatusEnum.APPROVE.equals(statusEnum) || ApproveStatusEnum.REJECT.equals(statusEnum)) {
                    obj.setApproveTime(LocalDateTime.now());
                    obj.setApproveUserId(userInfo.getUid());
                    obj.setApproveUserName(userInfo.getUserName());
                } else {
                    obj.setApproveTime(null);
                    obj.setApproveUserId("");
                    obj.setApproveUserName("");
                }
                obj.setApproveStatus(statusEnum);
                //记录sku与采购组织关系
                purchaseSkuOrgRefService.addByPurchasePrice(obj);
            });
            return this.updateBatchById(list);
        }
        return false;
    }

    /**
     * 修改状态
     * @Author Luo_WG
     * @Date 2023/6/30 19:47
     * @param ids
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDTO.SupplierSkuPrice>
     **/
    @Override
    public List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listSupplierSkuPrice(ids);
    }

    @Override
    public List<PurchasePriceDTO.SupplierSkuPrice> listAllSupplierSkuPrice(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<PurchasePriceDTO.SupplierSkuPrice> list = baseMapper.listAllSupplierSkuPrice(ids);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> currencyList = list.stream().map(PurchasePriceDTO.SupplierSkuPrice::getCurrency).collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> viewList = sysUserFeign.listByCurrency(currencyList);
            for (PurchasePriceDTO.SupplierSkuPrice price : list) {
                String currencySymbol = viewList.stream().filter(obj -> obj.getId().equals(price.getCurrency())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
                price.setCurrencySymbol(currencySymbol);
            }
        }
        return list;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        // 查询所有审核通过的产品信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        // 组织
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();
        // 币制
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        // 供应商
        List<Map<String, Object>> supplierList = supplierService.listApproveSupplier();
        PurchasePriceExcelListener excelListener = new PurchasePriceExcelListener(userList, skuList, currencyList, supplierList, orgList, priceDetailService, this);
        try {
            EasyExcel.read(excelFile.getInputStream(), ImportPurchasePriceExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("采购价目导入错误", e);
            return Boolean.FALSE;
        }
        List<ImportPurchasePriceExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "采购价目导入错误信息";
            ExcelUtil.export(fileName, "导入异常", errorList, ImportPurchasePriceExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchImport(List<PurchasePriceDTO.ImportAddDTO> handList) {
        if(CollUtil.isEmpty(handList)) {
            return;
        }
        // 新增的采购价目信息
        List<PurchasePriceEntity> addList = Lists.newArrayList();
        // 新增采购价目明细信息
        List<PurchasePriceDetailEntity> addDetailList = Lists.newArrayList();
        // 修改的采购价目明细信息
        List<PurchasePriceDetailEntity> updateDetailList = Lists.newArrayList();
        for(PurchasePriceDTO.ImportAddDTO item : handList) {
            PurchasePriceEntity purchasePriceEntity = new PurchasePriceEntity();
            purchasePriceEntity.setSupplierId(item.getSupplierId());
            purchasePriceEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
            purchasePriceEntity.setQuotedDate(item.getQuotedDate());
            purchasePriceEntity.setPricingUserId(item.getPricingUserId());
            purchasePriceEntity.setPricingUserName(item.getPricingUserName());
            purchasePriceEntity.setPurchaseOrgId(item.getPurchaseOrgId());
            purchasePriceEntity.setPurchaseOrgName(item.getPurchaseOrgName());
            purchasePriceEntity.setCurrency(item.getCurrency());
            // 明细信息
            List<PurchasePriceDetailDTO.ImportSaveDTO> detailList = item.getDetailList();
            List<PurchasePriceDetailEntity> addItemList = Lists.newArrayList();
            List<PurchasePriceDetailEntity> updateItemList = Lists.newArrayList();
            // 此处需要过滤掉修改的明细
            for(PurchasePriceDetailDTO.ImportSaveDTO detailItem : detailList) {
                BigDecimal taxRate = null;
                if(Objects.nonNull(detailItem.getTaxRate())) {
                    BigDecimal rate = detailItem.getTaxRate().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                    taxRate = rate;
                }
                if(CollUtil.isNotEmpty(detailItem.getIds())) {
                    for(String detailId : detailItem.getIds()) {
                        PurchasePriceDetailEntity savePurchasePriceDetailEntity = new PurchasePriceDetailEntity();
                        BeanMapper.copy(detailItem, savePurchasePriceDetailEntity);
                        savePurchasePriceDetailEntity.setTaxRate(taxRate);
                        savePurchasePriceDetailEntity.setId(detailId);
                        if (StringUtils.isBlank(savePurchasePriceDetailEntity.getCurrency())){
                            savePurchasePriceDetailEntity.setCurrency(item.getCurrency());
                        }
                        updateItemList.add(savePurchasePriceDetailEntity);
                    }
                } else {
                    PurchasePriceDetailEntity savePurchasePriceDetailEntity = new PurchasePriceDetailEntity();
                    BeanMapper.copy(detailItem, savePurchasePriceDetailEntity);
                    savePurchasePriceDetailEntity.setTaxRate(taxRate);
                    savePurchasePriceDetailEntity.setCurrency(item.getCurrency());
                    addItemList.add(savePurchasePriceDetailEntity);
                }
            }
            // 当该新增的主单有明细时才新增
            if(CollUtil.isNotEmpty(addItemList)) {
                //生成单号
//                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGJM, BusinessNoTypeEnum.CODE_CGJM.getCode()));
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGJM);
                purchasePriceEntity.setCode(code);
                String id = IdWorker.getIdStr();
                purchasePriceEntity.setId(id);
                addList.add(purchasePriceEntity);
                addItemList.stream().forEach(data->data.setPurchasePriceId(id));
                addDetailList.addAll(addItemList);
            }
            if(CollUtil.isNotEmpty(updateItemList)) {
                updateDetailList.addAll(updateItemList);
            }
        }
        // 保存主单
        if(CollUtil.isNotEmpty(addList)) {
            super.saveBatch(addList);
            List<Pair<String, String>> pairList = addList.stream().
                    map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            String content = "导入采购价目信息[%s]";
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "新增操作");
        }
        // 保存采购价目明细信息
        if(CollUtil.isNotEmpty(addDetailList)) {
            priceDetailService.saveBatch(addDetailList);
        }
        if(CollUtil.isNotEmpty(updateDetailList)) {
            List<String> detailIds = updateDetailList.stream().map(PurchasePriceDetailEntity::getId).distinct().collect(Collectors.toList());
            List<PurchasePriceDetailEntity> detailList = priceDetailService.listByIds(detailIds);
            for(PurchasePriceDetailEntity updateDetail : updateDetailList) {
                PurchasePriceDetailEntity old = detailList.stream().filter(r -> Objects.equals(r.getId(), updateDetail.getId())).findFirst().orElse(null);
                priceDetailService.updateDetail(updateDetail, old);
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/purchasePriceTemplate.xlsx";
        String excelName = "purchasePriceTemplate.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(PurchasePriceEntity entity,List<PurchasePriceDetailEntity> purchasePriceDetailEntities,List<PurchasePriceChangeDetailEntity> changeDetailEntityList) {
        if (!Objects.equals(ApproveStatusEnum.APPROVE, entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        if (CollectionUtils.isNotEmpty(changeDetailEntityList)) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),String.format(ApiError.ERROR_NOT_DISAPPROVE_CHANGE.msg, entity.getCode()));
        }
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.WAIT_SUBMIT);
        //反审核时移除sku和采购组织表记录
        purchaseSkuOrgRefService.removeByPrice(entity);
        if (result) {
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            moduleOperateLogService.addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");
            //发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    public Boolean updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        purchasePriceDetailService.updateDetailRemark(ids,remark);
        return Boolean.TRUE;
    }

    @Override
    public List<PurchasePriceDTO.TabListDTO> tabList(PermissionsDTO dto) {
        PurchasePriceTabFlagEnum[] values = PurchasePriceTabFlagEnum.values();
        List<PurchasePriceDTO.TabListDTO> list = new ArrayList<>();
        for (PurchasePriceTabFlagEnum item : values) {
            PurchaseOrderDTO.SearchParamDTO searchParamDTO = new PurchaseOrderDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            PurchasePriceDTO.TabListDTO resultDTO = new PurchasePriceDTO.TabListDTO();
            String tabSql = purchasePriceQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.tabList(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public PagingVO<PurchasePriceExportExcelDTO> exportPurchasePrice(PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        //获取导出数据
        Page<PurchasePriceDTO.PagingViewDTO> page = baseMapper.getExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        List<PurchasePriceExportExcelDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            List<String> currencyIdList = page.getRecords().stream().map(PurchasePriceDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
            //币种信息
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
            List<String> skuIds = page.getRecords().stream().map(PurchasePriceDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);

            //最新审核人
            ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
            page.getRecords().forEach(obj -> {
                dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_PRICE.getCode(), obj.getId()));
            });
            ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
            if (CollectionUtils.isNotEmpty(dtoList)) {
                listApiResult = workflowFeign.curApprover(dtoList);
                Integer code = listApiResult.getCode();
                if (200 != code) {
                    throw new ServiceException(ApiError.ERROR_500);
                }
            }
            for (PurchasePriceDTO.PagingViewDTO item : page.getRecords()) {
                SkuVO skuVO = skuNoList.stream().filter(obj -> obj.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
                PurchasePriceExportExcelDTO excelDTO = new PurchasePriceExportExcelDTO();
                BeanMapper.copy(item, excelDTO);
                excelDTO.setProductName(skuVO.getSkuName());
                Integer minQty = item.getMinQty();
                Integer maxQty = item.getMaxQty();
                excelDTO.setQtySection(minQty + "-" + maxQty);
                ApproveStatusEnum approveStatusEnum = item.getApproveStatus();
                excelDTO.setApproveStatusName(approveStatusEnum.getName());

                Boolean disabled = item.getDisabled();
                excelDTO.setEnabled((disabled != null && disabled) ? "停用" : "启用");
                //含税单价
                BigDecimal taxPrice = item.getTaxPrice();
                //币种
                String currency = item.getCurrency();
                String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
                excelDTO.setTaxPrice(currencySymbol + taxPrice.toString());

                //最新审核人
                if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                    String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                    excelDTO.setApproveUserName(curApprove);
                }
                excelDTO.setApproveTime(item.getApproveTime());

                resultList.add(excelDTO);
            }
        }
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public List<PurchasePriceDTO.PriceDTO> batchGetPurchasePrice(List<PurchasePriceDTO.PriceDTO> list) {
        List<PurchasePriceDTO.PriceDTO> updateList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        //采购组织Id
        List<String> purchaseOrgIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getPurchaseOrgId())).map(PurchasePriceDTO.PriceDTO::getPurchaseOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(purchaseOrgIdList);
        if (CollectionUtils.isEmpty(companyList)){
            return Collections.emptyList();
        }

        //skuId
        List<String> skuIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())).map(PurchasePriceDTO.PriceDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            return Collections.emptyList();
        }
        //根据sku查询是否是组合品
//        List<BomChildrenSkuDTO> skuDTOList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        //供应商Id
        List<String> supplierIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSupplierId())).map(PurchasePriceDTO.PriceDTO::getSupplierId).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntityList = supplierService.listByIds(supplierIdList);
        if (CollectionUtils.isEmpty(supplierEntityList)) {
            return Collections.emptyList();
        }
        //采购数量-需要根据sku进行汇总
        Map<String, Integer> skuQtyList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())
                        && StrUtil.isNotBlank(e.getSupplierId()) && Objects.nonNull(e.getQty()))
                .collect(Collectors.groupingBy(e -> e.getSkuId() + "_" + e.getSupplierId(), Collectors.summingInt(PurchasePriceDTO.PriceDTO::getQty)));
        List<Integer> purchaseQtyList = skuQtyList.values().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(purchaseQtyList)) {
            return Collections.emptyList();
        }
        //查询对应采购价目
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> viewList = purchasePriceDetailService.batchGetTaxPrice(skuIdList,supplierIdList,purchaseQtyList,purchaseOrgIdList);
        if (CollectionUtils.isEmpty(viewList)){
            //未查到结果，直接返回
            return Collections.emptyList();
        }
        for(PurchasePriceDTO.PriceDTO updateDTO : list){
            if (StrUtil.isBlank(updateDTO.getPurchaseOrgId()) || StrUtil.isBlank(updateDTO.getSkuId()) || StrUtil.isBlank(updateDTO.getSupplierId()) || Objects.isNull(updateDTO.getQty())){
                continue;
            }
            //获取sku汇总数量
            Integer purchaseQty = skuQtyList.getOrDefault(updateDTO.getSkuId() + "_" + updateDTO.getSupplierId(), null);
            PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = viewList.stream().filter(obj -> StrUtil.isNotBlank(updateDTO.getSkuId())
                            && StrUtil.isNotBlank(obj.getSkuId()) && obj.getSkuId().equals(updateDTO.getSkuId())
                            && StrUtil.isNotBlank(obj.getSupplierId()) && StrUtil.isNotBlank(updateDTO.getSupplierId()) && obj.getSupplierId().equals(updateDTO.getSupplierId())
                            && StrUtil.isNotBlank(obj.getPurchaseOrgId()) && StrUtil.isNotBlank(updateDTO.getPurchaseOrgId()) && StrUtil.equals(obj.getPurchaseOrgId(),updateDTO.getPurchaseOrgId())
                            && Objects.nonNull(purchaseQty) && (purchaseQty >= obj.getMinQty() && obj.getMaxQty() > purchaseQty))
                    .findFirst().orElse(null);
            if (Objects.nonNull(viewDTO)){
                updateDTO.setTaxPrice(viewDTO.getTaxPrice());
                updateDTO.setTaxRate(viewDTO.getTaxRate());
                updateDTO.setCurrency(viewDTO.getCurrency());
                updateDTO.setCurrencySymbol(CurrencyEnum.getSymbolByCode(viewDTO.getCurrency()));
                updateDTO.setAmount(MathUtil.multiplyWithTwo(viewDTO.getTaxPrice(), updateDTO.getQty()).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString());
                updateDTO.setDeliveryDay(viewDTO.getDeliveryDay());
                updateList.add(updateDTO);
            }
        }
        return updateList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateOutPlatformCode(PurchasePriceEntity entity, String outPlatformCode) {
        List<String> ids = Collections.singletonList(entity.getId());
        this.lambdaUpdate()
                .in(PurchasePriceEntity::getId, ids)
                .set(PurchasePriceEntity::getVoucherNo, outPlatformCode)
                .update(new PurchasePriceEntity());
        ids.forEach(v->{
            String content = StrUtil.format("更新外部平台单号为：{}", outPlatformCode);
            moduleOperateLogService.addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), v, "更新外部平台单号");
        });
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public List<PurchasePriceEntity> listByCodes(List<String> codes) {
        return this.list(new LambdaQueryWrapper<PurchasePriceEntity>().in(PurchasePriceEntity::getCode, codes).eq(PurchasePriceEntity::getIsDeleted, false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void updateApproveStatus(PurchasePriceDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        ApproveStatusEnum approveStatus = updateApprovalStatusDTO.getApproveStatus();
        PurchasePriceEntity purchasePriceEntity = updateApprovalStatusDTO.getPurchasePriceEntity();
        updateApproveStatus(Collections.singletonList(purchasePriceEntity), approveStatus);
    }

    /**
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     * @param list
     */
    private void startProcess (List<PurchasePriceEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(getVariablesMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     * @param entity
     * @param type
     * @param comment
     */
    private void approveProcess (PurchasePriceEntity entity, String type, String comment) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(type));
        approveDTO.setComment(comment);
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> result = workflowFeign.approve(approveDTO);
        Integer code = result.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = result.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(entity,type,comment);
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(PurchasePriceEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.PURCHASEPRICE.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> variablesMap = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);

        List<PurchasePriceDetailEntity> detailList = purchasePriceDetailService.listDetailByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.PRICE_NOT_EXIST);
        }
        //SKU
        String skuNo = detailList.stream().map(PurchasePriceDetailEntity::getSkuNo).collect(Collectors.joining(","));
        variablesMap.put("skuNo", skuNo);
        return variablesMap;
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<PurchasePriceEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeePurchasePriceService.syncDataToKingdee(obj, operate);
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

    /**
     * 校验附件必填
     * @author will
     * @date 2025/3/25 16:32
     * @param bussinessId
     */
    private void checkSubmitData(String bussinessId) {
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(bussinessId);
        if (CollectionUtils.isEmpty(attachmentList)){
            throw new ServiceException(ApiError.TIME_NOT_NULL,"附件信息");
        }
        //查询明细
        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listDetailByMainId(bussinessId);
        if (CollUtil.isEmpty(purchasePriceDetailList)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_SO_PRICE_DETAIL);
        }
        List<String> skuIdList = purchasePriceDetailList.stream().map(PurchasePriceDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        String skuNos = productDetailList.stream().filter(obj -> !ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(obj.getStatus())).map(ProductDetailEntity::getSkuNo).distinct().collect(Collectors.joining(","));
        if  (CharSequenceUtil.isNotBlank(skuNos)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_SUBMIT_SKU_UN_APPROVE,skuNos);
        }
    }
}
