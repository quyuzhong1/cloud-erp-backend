package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.mapper.PurchasePriceChangeMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购价变更表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceChangeServiceImpl extends SuperServiceImpl<PurchasePriceChangeMapper, PurchasePriceChangeEntity> implements PurchasePriceChangeService {

    @Resource
    private PurchasePriceService purchasePriceService;
    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;


    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;

    @Resource
    private PurchasePriceHistoryService purchasePriceHistoryService;


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private AttachmentService attachmentService;


    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    /**
     * 添加采购价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     * @author yl
     * @date 2023-03-28 11:49
     */
    @Override
    @GlobalTransactional
    public String add(PurchasePriceChangeDTO.AddDTO dto) {
        //采购价目表的id
        String priceId = dto.getPurchasePriceId();
        PurchasePriceEntity purchasePrice = purchasePriceService.getById(priceId);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        String approveStatus = purchasePrice.getApproveStatus().getStatus();
        if (!approveStatus.equals(ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98029);
        }
        String supplierId = purchasePrice.getSupplierId();
        List<String> detailIds = new ArrayList<>();
        /**
         * 报价明细
         */
        List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList = dto.getPurchasePriceChangeDetailList();
        if (CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)) {
            detailIds = purchasePriceChangeDetailList.stream().map(PurchasePriceChangeDetailDTO.AddDTO::getPurchasePriceDetailId).collect(Collectors.toList());
        }
        //根据供应商 获取到 对应 已有的区间
        List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList = purchasePriceDetailService.getBySupplierId(supplierId, detailIds);
        //历史报价
        List<PurchasePriceDetailDTO.AddDTO> historyList = purchasePriceHistoryService.getBySupplierId(purchasePrice.getSupplierId());

        //检查区间报价是否重叠
        purchasePriceChangeDetailService.checkSkuInterval(priceId, purchasePriceChangeDetailList, supplierPriceDetailList, historyList);
        PurchasePriceChangeEntity changeEntity = new PurchasePriceChangeEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, changeEntity);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGTJ, BusinessNoTypeEnum.CODE_CGTJ.getCode()));
        changeEntity.setCode(code);
        changeEntity.setId(id);
        String pricingUserId = dto.getAdjustUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            changeEntity.setAdjustUserName(user != null ? user.getUserName() : "");
        }
        String orgId = dto.getPurchaseOrgId();

        //判断是否能通过
        Boolean isPass = getIsPass(dto.getPurchasePriceChangeDetailList());
        if (isPass) {
            changeEntity.setApproveStatus(ApproveStatusEnum.getByStatus(approveStatus));
        }
        //获取组织
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            changeEntity.setPurchaseOrgName(orgList.get(0).getName());
        }
        //保存成功
        Boolean addResult = this.save(changeEntity);
        if (addResult) {

            Class<PurchasePriceChangeEntity> credentialClass = PurchasePriceChangeEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);

            //添加价格变更明细
            purchasePriceChangeDetailService.addPriceChangeDetail(id, dto.getPurchasePriceChangeDetailList());

            if (isPass) {
                purchasePriceChangeDetailService.updatePurchasePriceDetail(Arrays.asList(changeEntity));
            }

            //添加日志
            String content = String.format("新增了一个{%s}-采购调价-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), id, "新增操作");


            return id;
        }

        return "";
    }

    /**
     * 可以做自动审核的 就是判断他报价和税率全部都各自不大于原先值的情况  就给他自动审核通过
     *
     * @param purchasePriceChangeDetailList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-11 12:27
     */
    private Boolean getIsPass(List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList) {
        if (CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)) {
            List<Integer> flagList = new ArrayList<>();
            List<String> detailIds = purchasePriceChangeDetailList.stream().map(PurchasePriceChangeDetailDTO.AddDTO::getPurchasePriceDetailId).collect(Collectors.toList());

            List<PurchasePriceDetailEntity> detailEntityList = purchasePriceDetailService.listByIds(detailIds);
            int i = 0;
            for (PurchasePriceChangeDetailDTO.AddDTO item : purchasePriceChangeDetailList) {
                PurchasePriceDetailEntity entity = detailEntityList.stream().filter(d -> d.getId().equals(item.getPurchasePriceDetailId())).findFirst().orElse(null);
                if (entity != null) {
                    //新的报价
                    BigDecimal newTaxPrice = item.getTaxPrice();
                    //原有的报价
                    BigDecimal oldTaxPrice = entity.getTaxPrice();

                    //新的税率
                    BigDecimal newTaxRate = item.getTaxRate();

                    //原有的税率
                    BigDecimal oldTaxRate = entity.getTaxRate().multiply(new BigDecimal("100"));
                    if (newTaxPrice != null && oldTaxPrice != null && newTaxRate != null && oldTaxRate != null) {
                        if (newTaxPrice.compareTo(oldTaxPrice) <= 0 && newTaxRate.compareTo(oldTaxRate) <= 0) {
                            i++;
                            flagList.add(i);
                        }

                    }
                }
            }
            if (flagList.size() == purchasePriceChangeDetailList.size()) {
                return true;
            }
        }

        return false;

    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 14:08
     */
    @Override
    public Boolean addAndSubmit(PurchasePriceChangeDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submitApprove(Arrays.asList(id));
        return result;
    }

    /**
     * 采购价目变更详情
     *
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.UpdateDTO
     * @author yl
     * @date 2023-03-28 14:24
     */
    @Override
    public PurchasePriceChangeDTO.ViewDTO view(String id) {
        PurchasePriceChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        PurchasePriceChangeDTO.ViewDTO viewDTO = new PurchasePriceChangeDTO.ViewDTO();
        BeanMapper.copy(changeEntity, viewDTO);
        viewDTO.setApproveStatus(changeEntity.getApproveStatus().getStatus());
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachmentNameList(attachmentNameList);
        viewDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<PurchasePriceChangeDetailDTO.ViewDTO> purchasePriceDetailList = purchasePriceChangeDetailService.getByPriceChangeId(id);
        viewDTO.setPurchasePriceChangeDetailList(purchasePriceDetailList);
        return viewDTO;
    }


    /**
     * 修改采购价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     * @author yl
     * @date 2023-03-28 16:40
     */
    @Override
    public String updatePurchasePriceChange(PurchasePriceChangeDTO.UpdateDTO dto) {
        String id = dto.getId();
        PurchasePriceChangeEntity priceChangeEntity = this.getById(id);
        if (Objects.isNull(priceChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        PurchasePriceChangeEntity old = new PurchasePriceChangeEntity();
        BeanMapper.copy(priceChangeEntity, old);
        //状态值
        String status = priceChangeEntity.getApproveStatus().getStatus();
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(status)) {
            throw new ServiceException(ApiError.ERROR_98019);
        }

        String supplierId = priceChangeEntity.getSupplierId();
        List<String> detailIds = new ArrayList<>();
        /**
         * 报价明细
         */
        List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceChangeDetailList = dto.getPurchasePriceChangeDetailList();
        if (CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)) {
            detailIds = purchasePriceChangeDetailList.stream().map(PurchasePriceChangeDetailDTO.UpdateDTO::getPurchasePriceDetailId).collect(Collectors.toList());
        }

        //根据供应商 获取到 对应 已有的区间
        List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList = purchasePriceDetailService.getBySupplierId(supplierId, detailIds);
        //检查区间报价是否重叠
        List<PurchasePriceChangeDetailDTO.AddDTO> priceChangeDetailList = BeanMapper.copyList(dto.getPurchasePriceChangeDetailList(), PurchasePriceChangeDetailDTO.AddDTO.class);

        //判断是否能通过
        Boolean isPass = getIsPass(priceChangeDetailList);
        if (isPass) {
            priceChangeEntity.setApproveStatus(ApproveStatusEnum.getByStatus(status));
        }
        //历史报价
        List<PurchasePriceDetailDTO.AddDTO> historyList = purchasePriceHistoryService.getBySupplierId(supplierId);

        purchasePriceChangeDetailService.checkSkuInterval(priceChangeEntity.getPurchasePriceId(), priceChangeDetailList, supplierPriceDetailList, historyList);
        //code
        String code = priceChangeEntity.getCode();
        BeanMapper.copy(dto, priceChangeEntity);
        priceChangeEntity.setCode(code);
        String pricingUserId = dto.getAdjustUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            priceChangeEntity.setAdjustUserName(user != null ? user.getUserName() : "");
        }
        String orgId = dto.getPurchaseOrgId();
        //获取组织
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            priceChangeEntity.setPurchaseOrgName(orgList.get(0).getName());
        }
        //修改成功
        Boolean result = this.updateById(priceChangeEntity);
        if (result) {

            if (isPass) {
                purchasePriceChangeDetailService.updatePurchasePriceDetail(Arrays.asList(priceChangeEntity));
            }

            /**
             * 添加修改日志
             */
            moduleOperateLogService.addModuleOperateLogByObj(old, priceChangeEntity, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), id, "", "");

            Class<PurchasePriceChangeEntity> credentialClass = PurchasePriceChangeEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
            //修改明细
            purchasePriceChangeDetailService.updatePriceChangeDetail(id, dto.getPurchasePriceChangeDetailList());
            return id;
        }
        return "";
    }


    /**
     * 删除 采购价目变更
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:42
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceChangeEntity> priceChangeList = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = priceChangeList.stream().filter(p -> !p.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除价目表
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除价目表[%s]";
            List<Pair<String, String>> pairList = priceChangeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "删除");
            attachmentService.deleteByBusinessIds(ids);
        }

        return result;
    }


    /**
     * 采购价目变更 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:47
     */
    @Override
    public Boolean submitApprove(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceChangeEntity> priceChangeList = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = priceChangeList.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        List<Pair<String, String>> pairList = priceChangeList.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(waitSubmitStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = priceChangeList.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(priceChangeList, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "状态变更");

            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), rejectPairList, "状态变更");

        }

        return result;
    }


    /**
     * 采购价目变更 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<PurchasePriceChangeEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        Boolean isPass = false;
        if (dto.getType().equals(ScmConstant.PASS)) {
            isPass = true;
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));
            content = String.format("状态由[%s]变更为[%s],意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //当是审核通过的时候 就要去复写 且添加历史数据
            if (isPass) {
                purchasePriceChangeDetailService.updatePurchasePriceDetail(list);
            }
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "状态变更");
        }

        return result;
    }


    /**
     * 取消流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:56
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<PurchasePriceChangeEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        if (result) {
            String content = String.format("状态由[%s]变更为[%s] ", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "取消流程");
        }
        return result;
    }

    /**
     * 分页获取采购价目变更数据
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchasePriceChangeDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-28 17:15
     */
    @Override
    public PagingVO<PurchasePriceChangeDTO.PagingViewDTO> paging(PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        PurchasePriceChangeDTO.PagingParamDTO params = dto.getParams();
        params.setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        String searchType = params.getSearchType();

        List<String> statusList = new ArrayList<>(1);
        //待我审核
        if (SearchType.WAIT_APPROVE.equals(searchType)) {
            statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }

        IPage pageData = baseMapper.paging(query, params, statusList);
        List<PurchasePriceChangeDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {

            List<String> flagIdList = new ArrayList<>(10);

            List<String> currencyIdList = list.stream().map(PurchasePriceChangeDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
            //币种信息
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
            for (PurchasePriceChangeDTO.PagingViewDTO item : list) {
                boolean contains = flagIdList.contains(item.getId());

                ApproveStatusEnum approveStatusEnum = item.getApproveStatus();
                item.setApproveStatusCode(approveStatusEnum.getStatus());
                item.setApproveStatusName(approveStatusEnum.getName());
                //币种
                String currency = item.getCurrency();
                String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
                item.setCurrencySymbol(currencySymbol);

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

                flagIdList.add(item.getId());
            }
        }

        return new PagingVO<>(pageData);
    }


    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-29 9:42
     */
    @Override
    public Boolean updateAndSubmit(PurchasePriceChangeDTO.UpdateDTO dto) {
        String id = this.updatePurchasePriceChange(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submitApprove(Arrays.asList(id));
    }


    /**
     * 根据采购价目表id  获取对应产品信息
     *
     * @param purchasePriceId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO>
     * @author yl
     * @date 2023-03-31 16:07
     */
    @Override
    public List<PurchasePriceChangeDetailDTO.ViewDTO> getSkuChangeList(String purchasePriceId) {
        return purchasePriceDetailService.getPriceChangeDetail(purchasePriceId);
    }

    @Override
    public Boolean updateSyncKingdeeStatus(List<String> ids, String syncKingdeeStatus, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .in(PurchasePriceChangeEntity::getId,ids)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),PurchasePriceChangeEntity::getSyncKingdeeStatus,syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),PurchasePriceChangeEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId),PurchasePriceChangeEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    /**
     * 修改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:50
     */
    private Boolean updateApproveStatus(List<PurchasePriceChangeEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return false;
    }


    /**
     * 批量添加日志
     *
     * @param content
     * @param code
     * @param pairList
     * @param operation
     * @return void
     * @author yl
     * @date 2023-03-28 16:46
     */
    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);

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
     * @date 2023-03-28 12:25
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);

    }
}
