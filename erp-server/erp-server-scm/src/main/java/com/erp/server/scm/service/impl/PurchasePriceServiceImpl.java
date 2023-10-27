package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.ImportPurchasePriceExcelDTO;
import com.erp.model.scm.dto.excel.PurchasePriceExportExcelDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.listener.PurchasePriceExcelListener;
import com.erp.server.scm.mapper.PurchasePriceMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private PurchasePriceHistoryService purchasePriceHistoryService;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private SyncKingdeePurchasePriceService syncKingdeePurchasePriceService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;


    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

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
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(PurchasePriceDTO.AddDTO dto) {
        //供应商id
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        List<String> skuIdList = dto.getPurchasePriceDetailList().stream().map(PurchasePriceDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        //根据供应商 获取到 对应 已有的区间
        List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList = priceDetailService.getBySupplierId(supplierId, new ArrayList<>(), skuIdList);

        //历史报价
        List<PurchasePriceDetailDTO.AddDTO> historyList = purchasePriceHistoryService.getBySupplierId(supplierId, skuIdList);

        //检查sku 区间报价
        priceDetailService.checkSkuInterval(dto.getPurchasePriceDetailList(), supplierPriceDetailList, historyList);
        PurchasePriceEntity purchasePrice = new PurchasePriceEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, purchasePrice);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGJM, BusinessNoTypeEnum.CODE_CGJM.getCode()));
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
            return id;

        }
        return "";
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
        List<SkuVO> skuNoList = plmTaskFeign.getSkuInfoByIds(skuIds);
        purchasePriceDetailList.forEach(req -> {
            SkuVO skuVO = skuNoList.stream().filter(obj -> obj.getSkuId().equals(req.getSkuId())).findFirst().orElse(new SkuVO());
            req.setProductName(skuVO.getSkuName());
        });

        viewDTO.setPurchasePriceDetailList(purchasePriceDetailList);

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
    public String updatePurchasePrice(PurchasePriceDTO.UpdateDTO dto) {
        String id = dto.getId();
        PurchasePriceEntity purchasePrice = this.getById(id);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        List<PurchasePriceDetailEntity> detailList = priceDetailService.listDetailByMainId(id);
        List<String> detailIds = detailList.stream().map(PurchasePriceDetailEntity::getId).collect(Collectors.toList());
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
        List<String> skuIdList = dto.getPurchasePriceDetailList().stream().map(PurchasePriceDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        BeanMapper.copy(dto, purchasePrice);
        //根据供应商 获取到 对应 已有的区间
        List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList = priceDetailService.getBySupplierId(purchasePrice.getSupplierId(), detailIds, skuIdList);
        //历史报价
        List<PurchasePriceDetailDTO.AddDTO> historyList = purchasePriceHistoryService.getBySupplierId(purchasePrice.getSupplierId(), skuIdList);
        //检查sku 区间报价
        List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList = BeanMapper.copyList(dto.getPurchasePriceDetailList(), PurchasePriceDetailDTO.AddDTO.class);
        priceDetailService.checkSkuInterval(purchasePriceDetailList, supplierPriceDetailList, historyList);

        //编号
        String code = purchasePrice.getCode();
        purchasePrice.setCode(code);
        purchasePrice.setApproveStatus(status);

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

        //修改成功
        Boolean result = this.updateById(purchasePrice);
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
            return id;
        }
        return "";
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchasePriceDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submitApprove(Arrays.asList(id));
        return result;
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchasePriceDTO.UpdateDTO dto) {
        String id = this.updatePurchasePrice(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submitApprove(Arrays.asList(id));
    }


    /**
     * 批量删除采购价目信息
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceEntity> purchasePriceList = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = purchasePriceList.stream().filter(p -> !p.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除价目表
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除价目表[%s]";
            List<Pair<String, String>> pairList = purchasePriceList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "删除");
            attachmentService.deleteByBusinessIds(ids);
            //删除同步金蝶
            purchasePriceList.forEach(obj -> syncKingdeePurchasePriceService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));

        }
        return result;
    }


    /**
     * 采购价目表 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submitApprove(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceEntity> list = this.listByIds(ids);
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
        }

        return result;
    }

    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 12:29
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<PurchasePriceEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //调用审核流程
        approveProcess(list, dto);

        //添加日志
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        batchAddModuleOperateLog(String.format("审核【%s】了一个采购价目", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    /**
     * @param dto
     * @param list
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(BaseApproveParamDTO dto, List<PurchasePriceEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        Boolean result;
        if (dto.getType().equals(ScmConstant.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
        }
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        //审核通过发送金蝶
        if (dto.getType().equals(ScmConstant.PASS)) {
            list.forEach(obj -> syncKingdeePurchasePriceService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));
        }
        return Boolean.TRUE;
    }


    /**
     * 取消流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-27 14:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<PurchasePriceEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        //撤销现有流程
        LoginUser userInfo = commonService.getUserInfo();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        if (result) {
            String content = String.format("状态由[%s]变更为[%s] ", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "取消流程");

        }

        return result;
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
        String searchType = params.getSearchType();
        List<String> statusList = new ArrayList<>(1);
        //待我审核
        if (SearchType.WAIT_APPROVE.equals(searchType)) {
            statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PURCHASE_PRICE.getCode());
            if (CollectionUtils.isEmpty(businessIds)) {
                return new PagingVO(new Page());
            }
            params.setIdList(businessIds);
        }

        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params, statusList);
        List<PurchasePriceDTO.PagingViewDTO> list = pageData.getRecords();
        List<String> flagList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> skuIds = list.stream().map(PurchasePriceDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuNoList = plmTaskFeign.getSkuInfoByIds(skuIds);
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
                    throw new ServiceException(ApiError.ERROR_500);
                }
            }

            for (PurchasePriceDTO.PagingViewDTO item : list) {
                SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
                item.setProductName(skuVO.getSkuName());
                boolean contains = flagList.contains(item.getId());
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
                    item.setApproveUserName(curApprove);
                }

                if (contains) {
                    item.setCode("");
                    item.setSupplierName("");
                    item.setPurchaseOrgId("");
                    item.setPurchaseOrgName("");
                    item.setApproveStatus(null);
                    item.setApproveStatusName("");
                    item.setCreateUserName("");
                    item.setCreateTime(null);
                }
                flagList.add(item.getId());
            }
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 采购价目表导出
     *
     * @param dto
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-27 17:55
     */
    @Override
    public void exportPurchasePrice(PurchasePriceDTO.ExportDTO dto, HttpServletResponse response) {
        //获取导出数据
        List<PurchasePriceDTO.PagingViewDTO> viewList = baseMapper.getExport(dto);
        List<PurchasePriceExportExcelDTO> resultList = new ArrayList<>(viewList.size());
        if (CollectionUtils.isNotEmpty(viewList)) {
            List<String> currencyIdList = viewList.stream().map(PurchasePriceDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
            //币种信息
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
            List<String> skuIds = viewList.stream().map(PurchasePriceDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuNoList = plmTaskFeign.getSkuInfoByIds(skuIds);
            for (PurchasePriceDTO.PagingViewDTO item : viewList) {
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
                resultList.add(excelDTO);
            }

        }
        String fileName = "采购价目数据";
        try {
            ExcelUtil.export(fileName, "采购价目数据", resultList, PurchasePriceExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
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
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(obj -> {
                obj.setApproveStatus(statusEnum);
            });
            return this.updateBatchById(list);
        }
        return false;
    }

    /**
     * 修改状态
     *
     * @param ids
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDTO.SupplierSkuPrice>
     * @Author Luo_WG
     * @Date 2023/6/30 19:47
     **/
    @Override
    public List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listSupplierSkuPrice(ids);
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
        if (CollUtil.isEmpty(handList)) {
            return;
        }
        // 新增的采购价目信息
        List<PurchasePriceEntity> addList = Lists.newArrayList();
        // 新增采购价目明细信息
        List<PurchasePriceDetailEntity> addDetailList = Lists.newArrayList();
        // 修改的采购价目明细信息
        List<PurchasePriceDetailEntity> updateDetailList = Lists.newArrayList();
        for (PurchasePriceDTO.ImportAddDTO item : handList) {
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
            for (PurchasePriceDetailDTO.ImportSaveDTO detailItem : detailList) {
                LocalDate expireDate = null;
                if (Objects.nonNull(detailItem.getEffectiveDate())) {
                    expireDate = detailItem.getEffectiveDate().plusDays(100);
                }
                BigDecimal taxRate = null;
                if (Objects.nonNull(detailItem.getTaxRate())) {
                    BigDecimal rate = detailItem.getTaxRate().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                    taxRate = rate;
                }
                if (CollUtil.isNotEmpty(detailItem.getIds())) {
                    for (String detailId : detailItem.getIds()) {
                        PurchasePriceDetailEntity savePurchasePriceDetailEntity = new PurchasePriceDetailEntity();
                        BeanMapper.copy(detailItem, savePurchasePriceDetailEntity);
                        savePurchasePriceDetailEntity.setExpireDate(expireDate);
                        savePurchasePriceDetailEntity.setTaxRate(taxRate);
                        savePurchasePriceDetailEntity.setId(detailId);
                        updateItemList.add(savePurchasePriceDetailEntity);
                    }
                } else {
                    PurchasePriceDetailEntity savePurchasePriceDetailEntity = new PurchasePriceDetailEntity();
                    BeanMapper.copy(detailItem, savePurchasePriceDetailEntity);
                    savePurchasePriceDetailEntity.setExpireDate(expireDate);
                    savePurchasePriceDetailEntity.setTaxRate(taxRate);
                    savePurchasePriceDetailEntity.setCurrency(item.getCurrency());
                    addItemList.add(savePurchasePriceDetailEntity);
                }
            }
            // 当该新增的主单有明细时才新增
            if (CollUtil.isNotEmpty(addItemList)) {
                //生成单号
                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGJM, BusinessNoTypeEnum.CODE_CGJM.getCode()));
                purchasePriceEntity.setCode(code);
                String id = IdWorker.getIdStr();
                purchasePriceEntity.setId(id);
                addList.add(purchasePriceEntity);
                addItemList.stream().forEach(data -> data.setPurchasePriceId(id));
                addDetailList.addAll(addItemList);
            }
            if (CollUtil.isNotEmpty(updateItemList)) {
                updateDetailList.addAll(updateItemList);
            }
        }
        // 保存主单
        if (CollUtil.isNotEmpty(addList)) {
            super.saveBatch(addList);
            List<Pair<String, String>> pairList = addList.stream().
                    map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            String content = "导入采购价目信息[%s]";
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE.getCode(), pairList, "新增操作");
        }
        // 保存采购价目明细信息
        if (CollUtil.isNotEmpty(addDetailList)) {
            priceDetailService.saveBatch(addDetailList);
        }
        if (CollUtil.isNotEmpty(updateDetailList)) {
            List<String> detailIds = updateDetailList.stream().map(PurchasePriceDetailEntity::getId).distinct().collect(Collectors.toList());
            List<PurchasePriceDetailEntity> detailList = priceDetailService.listByIds(detailIds);
            for (PurchasePriceDetailEntity updateDetail : updateDetailList) {
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<PurchasePriceEntity> list = this.listByIds(ids);
        long count = list.stream().filter(r -> !Objects.equals(ApproveStatusEnum.APPROVE, r.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<PurchasePriceChangeEntity> priceChangeList = purchasePriceChangeService.listByPurchasePriceIds(ids);
        if (CollectionUtils.isNotEmpty(priceChangeList)) {
            List<String> priceIdList = priceChangeList.stream().map(PurchasePriceChangeEntity::getPurchasePriceId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(priceIdList)) {
                String code = list.stream().filter(p -> priceIdList.contains(p.getId())).
                        map(PurchasePriceEntity::getCode).collect(Collectors.joining(","));
               if(StringUtils.isNotBlank(code)){
                    throw new ServiceException(ApiError.ERROR_NOT_DISAPPROVE_CHANGE,code);
               }
            }
        }

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.APPROVE)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.WAIT_SUBMIT);
        if (result) {
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), rejectPairList, "状态变更");
            //发送金蝶
            list.forEach(obj -> syncKingdeePurchasePriceService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
        }
        return result;
    }

    @Override
    public Boolean updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        purchasePriceDetailService.updateDetailRemark(ids, remark);
        return Boolean.TRUE;
    }

    /**
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<PurchasePriceEntity> list) {
        LoginUser userInfo = commonService.getUserInfo();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * @param list
     * @param dto
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     */
    private void approveProcess(List<PurchasePriceEntity> list, BaseApproveParamDTO dto) {
        ValidList<ProcessManagementDTO.ApproveDTO> resultList = new ValidList<>();
        LoginUser userInfo = commonService.getUserInfo();
        list.forEach(obj -> {
            ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
            approveDTO.setBusinessId(obj.getId());
            approveDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE.getCode());
            approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
            approveDTO.setComment(dto.getComment());
            approveDTO.setUserId(userInfo.getUid());
            approveDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(approveDTO);
        });
        ApiResult<List<ProcessManagementDTO.ApproveResultDTO>> listApiResult = workflowFeign.batchApproveProcess(resultList);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<ProcessManagementDTO.ApproveResultDTO> data = listApiResult.getData();
        List<String> updateIdList = data.stream()
                .filter(obj -> ObjectUtils.isEmpty(obj.getIsExistProcess()) || !obj.getIsExistProcess())
                .map(ProcessManagementDTO.ApproveResultDTO::getBusinessId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(updateIdList)) {
            List<PurchasePriceEntity> updateList = list.stream().filter(obj -> updateIdList.contains(obj.getId())).collect(Collectors.toList());
            approveEnd(dto, updateList);
        }
    }


}
