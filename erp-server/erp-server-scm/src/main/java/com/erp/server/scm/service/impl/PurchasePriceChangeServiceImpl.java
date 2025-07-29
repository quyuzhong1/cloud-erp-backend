package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchasePriceChangeExportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchasePriceChangeTabFlagEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.model.sys.dto.SysUserSimpleDTO;
import com.erp.model.sys.enums.NoticeNodeEnum;
import com.erp.model.sys.enums.NoticePurItemRoleEnum;
import com.erp.model.sys.enums.NoticeReceiverEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceChangeService;
import com.erp.server.scm.mapper.PurchasePriceChangeMapper;
import com.erp.server.scm.query.PurchasePriceChangeQueryHandler;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_PRICE_CHANGE;

/**
 * <p>
 * 采购价变更表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
@Slf4j
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

    @Resource
    private SyncKingdeePurchasePriceChangeService syncKingdeePurchasePriceChangeService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchasePriceChangeQueryHandler purchasePriceChangeQueryHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    @Resource
    private ProcessTaskManagementFeign processTaskManagementFeign;

    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
     * 添加采购价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     * @author yl
     * @date 2023-03-28 11:49
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public PurchasePriceChangeEntity add(PurchasePriceChangeDTO.AddDTO dto) {

        /**
         * 报价明细
         */
        PurchasePriceChangeEntity changeEntity = new PurchasePriceChangeEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, changeEntity);
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGTJ, BusinessNoTypeEnum.CODE_CGTJ.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGTJ);
        changeEntity.setCode(code);
        changeEntity.setId(id);
        String pricingUserId = dto.getAdjustUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            changeEntity.setAdjustUserName(user != null ? user.getUserName() : "");
        }
        String orgId = dto.getPurchaseOrgId();

        //获取组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
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
            //添加日志
            String content = String.format("新增了一个{%s}-采购调价-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), id, "新增操作");
            return changeEntity;
        }
        return null;
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public PurchasePriceChangeEntity addAndSubmit(PurchasePriceChangeDTO.AddDTO dto) {
        PurchasePriceChangeEntity entity = this.add(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submitApprove(Collections.singletonList(entity.getId()), Boolean.TRUE);
        return entity;
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
        List<String> skuIds = purchasePriceDetailList.stream().map(PurchasePriceChangeDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);

        //获取供应商
        List<String> supplierIds = purchasePriceDetailList.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIds);

        //查询采购价目表
        List<String> purchasePriceDetailId = purchasePriceDetailList.stream().map(req -> req.getPurchasePriceDetailId()).distinct().collect(Collectors.toList());
        List<PurchasePriceDetailDTO.ViewDTO> priceDetailView = purchasePriceDetailService.listByPurchasePriceDetailIds(purchasePriceDetailId);
        List<String> purchasePriceIds = priceDetailView.stream().map(req -> req.getPurchasePriceId()).distinct().collect(Collectors.toList());
        List<PurchasePriceEntity> purchasePriceEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(purchasePriceIds)) {
            purchasePriceEntities = purchasePriceService.listByIds(purchasePriceIds);
        }

        for (PurchasePriceChangeDetailDTO.ViewDTO dto : purchasePriceDetailList) {
            SkuVO skuVO = skuNoList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).findFirst().orElse(new SkuVO());
            dto.setProductName(skuVO.getSkuName());
            SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> dto.getSupplierId().equals(req.getId())).findFirst().orElse(new SupplierEntity());
            dto.setSupplierName(supplierEntity.getName());
        }

        //查询价目表主标Id
        List<String> purchasePriceDetailIds = purchasePriceDetailList.stream().map(req -> req.getPurchasePriceDetailId()).distinct().collect(Collectors.toList());
        List<PurchasePriceDetailEntity> purchasePriceDetailEntities = purchasePriceDetailService.listByIds(purchasePriceDetailIds);
        List<String> purchasePriceIdList = purchasePriceDetailEntities.stream().map(req -> req.getPurchasePriceId()).distinct().collect(Collectors.toList());
        viewDTO.setPurchasePriceIdList(purchasePriceIdList);


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
    @Transactional(rollbackFor = Exception.class)
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
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            priceChangeEntity.setPurchaseOrgName(orgList.get(0).getName());
        }
        //修改成功
        Boolean result = this.updateById(priceChangeEntity);
        if (result) {
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submitApprove(List<String> ids, Boolean isStartProcess) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        //要去掉已审核通过的
        List<PurchasePriceChangeEntity> priceChangeList = this.listByIds(ids);
        priceChangeList = priceChangeList.stream().filter(p -> !ApproveStatusEnum.APPROVE.equals(p.getApproveStatus())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(priceChangeList)) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        //校验附件信息
        ids.forEach(this::checkAttachment);

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

        if (isStartProcess) {
            //提交流程
            startProcess(priceChangeList);
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
     * 发送消息
     * @param idList
     */
    @Override
    public void sendMsg(List<String> idList, ApproveStatusEnum approveStatus,String comment ) {
        if(CollUtil.isEmpty(idList)){
            return ;
        }
        List<PurchasePriceChangeDTO.NoticeMsgViewDTO> noticeMsgViewDTOS = this.baseMapper.listPurchasePriceChange(idList);
        if(CollUtil.isEmpty(noticeMsgViewDTOS)){
            return ;
        }

        //接收人
        List<NoticeReceiverDTO.InfoDTO> receiverList = null;
        if(ApproveStatusEnum.WAIT_SUBMIT.equals(approveStatus)){ //待审核
            receiverList = sysUserFeign.listNoticeReceiverByNodeKey(NoticeNodeEnum.PURCHASE_PRICE_CHANGE_WAIT.getCode());
        }else if(ApproveStatusEnum.REJECT.equals(approveStatus)){//不通过
            receiverList = sysUserFeign.listNoticeReceiverByNodeKey(NoticeNodeEnum.PURCHASE_PRICE_CHANGE_REJECT.getCode());
        }else if(ApproveStatusEnum.APPROVE.equals(approveStatus)){//通过
            receiverList = sysUserFeign.listNoticeReceiverByNodeKey(NoticeNodeEnum.PURCHASE_PRICE_CHANGE_APPROVE.getCode());
        }
        if (CollectionUtils.isEmpty(receiverList)) {
            return;
        }

        List<String> userIdList = new ArrayList<>();
        //其它人员
        String otherPeople = NoticeReceiverEnum.OTHER_PEOPLE.getCode();
        List<String> otherUsers = receiverList.stream().filter(r -> otherPeople.equals(r.getReceiverType())).
                map(NoticeReceiverDTO.InfoDTO::getReceiverValue).collect(Collectors.toList());

        //这个是项目角色
        String itemRole = NoticeReceiverEnum.PUR_ITEM_ROLE.getCode();
        List<String> itemRoles = receiverList.stream().filter(r -> itemRole.equals(r.getReceiverType())).
                map(NoticeReceiverDTO.InfoDTO::getReceiverValue).collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String msgHead = "";
        String msgContent = "";
        String approveTime = LocalDateTime.now().format(formatter);
        for (String id : idList) {
            List<PurchasePriceChangeDTO.NoticeMsgViewDTO> collect = noticeMsgViewDTOS.stream().filter(obj -> obj.getId().equals(id)).collect(Collectors.toList());
            PurchasePriceChangeDTO.NoticeMsgViewDTO noticeMsgViewDTO = collect.get(0);
            //这个是审核人
            List<ProcessTaskManagementDTO.ApproveHistoryDTO> approveHistoryList = processTaskManagementFeign.listApproveHistory(id).stream().filter(item -> item.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getCode())).collect(Collectors.toList());
            List<String> approveUserIdList = approveHistoryList.stream().map(ProcessTaskManagementDTO.ApproveHistoryDTO::getCurApproveId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
            String approveUserName = approveHistoryList.stream().map(ProcessTaskManagementDTO.ApproveHistoryDTO::getCurApproveName).filter(StringUtil::isNotBlank).collect(Collectors.joining(","));;

            //当不为空
            if (CollUtil.isNotEmpty(itemRoles)) {
                for (String role : itemRoles) {
                    if(role.equals(NoticePurItemRoleEnum.CREATE_USER.getCode())){
                        userIdList.add(noticeMsgViewDTO.getCreateUserId());
                    }else if(role.equals(NoticePurItemRoleEnum.APPROVE_USER.getCode())){
                        userIdList.addAll(approveUserIdList);
                    }else if(role.equals(NoticePurItemRoleEnum.PURCHASER.getCode())){
                        userIdList.addAll( collect.stream().map(PurchasePriceChangeDTO.NoticeMsgViewDTO::getPurchaseUserId).distinct().collect(Collectors.toList()));
                    }
                }
            }
            userIdList.addAll(otherUsers);
            if(CollUtil.isEmpty(userIdList)){
                continue;
            }
            //去重
            userIdList = userIdList.stream().distinct().filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());

            //排除禁用人员
            List<SysUserSimpleDTO> userSimpleInfoByIds = sysUserFeign.getUserSimpleInfoByIds(userIdList);
            if(CollUtil.isEmpty(userSimpleInfoByIds)){
                continue;
            }
            List<String> sendIdList = userSimpleInfoByIds.stream().map(SysUserSimpleDTO::getUid).filter(StringUtils::isNotBlank).collect(Collectors.toList());


            String code = noticeMsgViewDTO.getCode();
            String createUserName = noticeMsgViewDTO.getCreateUserName();
            String createTime = noticeMsgViewDTO.getCreateTime().format(formatter);
            String supplierNames = collect.stream()
                    .map(PurchasePriceChangeDTO.NoticeMsgViewDTO::getSupplierName)
                    .distinct()
                    .collect(Collectors.joining(", "));
            if(ApproveStatusEnum.WAIT_SUBMIT.equals(approveStatus)){ //待审核
                msgHead = NoticeMsgConstant.PRUCHASE_PRICE_CHANGE_WAIT_HEAD;
                msgContent = String.format(NoticeMsgConstant.PRUCHASE_PRICE_CHANGE_WAIT_CONTENT,code,supplierNames,createUserName,createTime,approveUserName);
            }else if(ApproveStatusEnum.REJECT.equals(approveStatus)){//不通过
                msgHead = NoticeMsgConstant.PRUCHASE_PRICE_CHANGE_REJECT_HEAD;
                msgContent = String.format(NoticeMsgConstant.PRUCHASE_PRICE_CHANGE_REJECT_CONTENT,code,supplierNames,createUserName,createTime,approveUserName,approveTime,comment);
            }else if(ApproveStatusEnum.APPROVE.equals(approveStatus)){//通过
                msgHead = NoticeMsgConstant.PRUCHASE_PRICE_CHANGE_APPROVE_HEAD;
                msgContent = String.format(NoticeMsgConstant.PRUCHASE_PRICE_CHANGE_APPROVE_CONTENT,code,supplierNames,createUserName,createTime,approveUserName,approveTime,comment);
            }
            NoticeMsgInfoDTO noticeMsgInfo = new NoticeMsgInfoDTO();
            noticeMsgInfo.setReceiverUserIds(sendIdList);
            noticeMsgInfo.setTitle(msgHead);
            noticeMsgInfo.setContent(msgContent);
            noticeMsgInfo.setNoticeTypeEnum(NoticeTypeEnum.SCM_TASK);
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, RocketMqTagEnum.MSG_NOTICE_TAG.getName(),
                    noticeMsgInfo, IdUtil.simpleUUID());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
            }
        }
    }

    /**
     * 采购价目变更 审核
     *
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(PurchasePriceChangeEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        BatchResultDTO resultDTO = approveProcess(entity, type, comment, isNeedProcess);
        if (resultDTO.getSuccess()){
            //添加日志
            moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个采购价目【%s】", ApproveTypeEnum.getName(type), entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), entity.getId(), "审核操作");
        }
        return resultDTO;
    }

    /**
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approveEnd(PurchasePriceChangeEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (ObjectUtils.isEmpty(entity)) {
            return BatchResultDTO.success();
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(type);
        Boolean  result = this.updateApproveStatus(Collections.singletonList(entity), approveStatus);

        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (ScmConstant.PASS.equals(type)) {
            //更新价目表数据
            purchasePriceChangeDetailService.updatePurchasePriceDetail(Collections.singletonList(entity));
            //审核通过发送金蝶
            DmpPushTaskEntity pushTaskEntity = syncKingdeePurchasePriceChangeService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
                }
            });
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<PurchasePriceChangeEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode());
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
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        IPage pageData = baseMapper.paging(query, params);
        List<PurchasePriceChangeDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> skuIds = list.stream().map(PurchasePriceChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
            List<String> currencyIdList = list.stream().map(PurchasePriceChangeDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
            //币种信息
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

            //最新审核人
            ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
            list.forEach(obj -> {
                dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), obj.getId()));
            });
            ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
            if (CollectionUtils.isNotEmpty(dtoList)) {
                listApiResult = workflowFeign.curApprover(dtoList);
                Integer code = listApiResult.getCode();
                if (200 != code) {
                    throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
                }
            }
            //历史调价数据
            List<String> changeDetailIdList = list.stream().map(PurchasePriceChangeDTO.PagingViewDTO::getChangeDetailId).collect(Collectors.toList());
            List<PurchasePriceHistoryEntity> purchasePriceHistoryList = purchasePriceHistoryService.listByChangeDetailIdList(changeDetailIdList);

            //原调价表数据
            List<String> priceDetailIdList = list.stream().map(PurchasePriceChangeDTO.PagingViewDTO::getPurchasePriceDetailId).collect(Collectors.toList());
            List<PurchasePriceDetailDTO.ViewDTO> priceDetailList = purchasePriceDetailService.listByPurchasePriceDetailIds(priceDetailIdList);


            List<String> supplierIdList = list.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
            List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIdList);

            //根据供应商、sku、数量区间查询
            List<PurchasePriceChangeDTO.PurchaseOrderAdjustParamDTO> adjustParamList = list.stream().map(obj -> new PurchasePriceChangeDTO.PurchaseOrderAdjustParamDTO(obj.getSupplierId(), obj.getSkuId(), obj.getMinQty(), obj.getMaxQty())).collect(Collectors.toList());
            List<PurchasePriceChangeDTO.PurchaseOrderAdjustResultDTO> purchaseOrderAdjustList = purchaseOrderDetailService.listAdjustPurchaseOrder(adjustParamList);

            for (PurchasePriceChangeDTO.PagingViewDTO item : list) {
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
                    item.setApproveUserName(curApprove);
                }

                //供应商
                SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> item.getSupplierId().equals(req.getId())).findFirst().orElse(new SupplierEntity());
                item.setSupplierName(supplierEntity.getName());
                //历史调价
                PurchasePriceHistoryEntity purchasePriceHistoryEntity = purchasePriceHistoryList.stream().filter(obj -> StrUtil.equals(item.getChangeDetailId(), obj.getChangeDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchasePriceHistoryEntity)) {
                    //升降比例
                    BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), purchasePriceHistoryEntity.getTaxPrice()), purchasePriceHistoryEntity.getTaxPrice()).multiply(MathUtil.BigDecimal_100);
                    item.setOffsetRate(StrUtil.format("{}%",offsetRate.stripTrailingZeros().toPlainString()));
                } else {
                    PurchasePriceDetailDTO.ViewDTO viewDTO = priceDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), item.getPurchasePriceDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(viewDTO)) {
                        continue;
                    }
                    //升降比例
                    BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), viewDTO.getTaxPrice()), viewDTO.getTaxPrice()).multiply(MathUtil.BigDecimal_100);
                    item.setOffsetRate(StrUtil.format("{}%",offsetRate.stripTrailingZeros().toPlainString()));
                }

                //查询全部调整数量
                long totalAdjustedCount = purchaseOrderAdjustList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSupplierId(), item.getSupplierId())
                                && CharSequenceUtil.equals(obj.getSkuId(), item.getSkuId())
                                && MathUtil.compareTo(obj.getPurchaseQty(), item.getMinQty()) >= 0
                                && MathUtil.compareTo(item.getMaxQty(), obj.getPurchaseQty()) > 0
                ).map(PurchasePriceChangeDTO.PurchaseOrderAdjustResultDTO::getPurchaseOrderId).distinct().count();
                item.setTotalAdjustedCount(Integer.valueOf(String.valueOf(totalAdjustedCount)));
                //查询已调整数量
                long adjustedCount = purchaseOrderAdjustList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSupplierId(), item.getSupplierId())
                                && CharSequenceUtil.equals(obj.getSkuId(), item.getSkuId())
                                && MathUtil.compareTo(obj.getPurchaseQty(), item.getMinQty()) >= MathUtil.ZERO
                                && MathUtil.compareTo(item.getMaxQty(), obj.getPurchaseQty()) > MathUtil.ZERO
                                && MathUtil.compareTo(item.getTaxPrice(), obj.getTaxPrice()) == MathUtil.ZERO
                ).map(PurchasePriceChangeDTO.PurchaseOrderAdjustResultDTO::getPurchaseOrderId).distinct().count();
                item.setAdjustedCount(Integer.valueOf(String.valueOf(adjustedCount)));
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchasePriceChangeDTO.UpdateDTO dto) {
        String id = this.updatePurchasePriceChange(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submitApprove(Arrays.asList(id), Boolean.TRUE);
    }


    /**
     * 根据采购价目表id  获取对应产品信息
     *
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO>
     * @author yl
     * @date 2023-03-31 16:07
     */
    @Override
    public List<PurchasePriceChangeDetailDTO.ViewDTO> getSkuChangeList(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto) {
        return purchasePriceDetailService.listPriceChangeDetail(dto);
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(PurchasePriceChangeEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), PurchasePriceChangeEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public Boolean updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        purchasePriceChangeDetailService.updateDetailRemark(ids,remark);
        return Boolean.TRUE;
    }

    @Override
    public void export(PurchasePriceChangeDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购调价数据", EXPORT_SCM_PURCHASE_PRICE_CHANGE.getCode(), dto);
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
            });
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

    /**
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<PurchasePriceChangeEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode());
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
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     */
    private BatchResultDTO approveProcess(PurchasePriceChangeEntity entity, String type, String comment, Boolean isNeedProcess) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(type));
        approveDTO.setComment(comment);
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> result = workflowFeign.approve(approveDTO);
        Integer code = result.getCode();
        if (200 != code) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_94006.msg);
        }
        ProcessManagementDTO.ApproveResultDTO data = result.getData();
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.REJECT;
        if (type.equals(ScmConstant.PASS)) {
            approveStatusEnum = ApproveStatusEnum.APPROVE;
        }
        //异步发送通知
        ApproveStatusEnum finalApproveStatusEnum = approveStatusEnum;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                sendMsg(Collections.singletonList(entity.getId()), finalApproveStatusEnum, comment);
            }
        });

        if (ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            return approveEnd(entity, type,comment,isNeedProcess);
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(PurchasePriceChangeEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.PURCHASEPRICECHANGE.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> variablesMap = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);

        List<PurchasePriceChangeDetailEntity> detailList = purchasePriceChangeDetailService.listByPurchasePriceChangeId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.PRICE_NOT_EXIST);
        }
        //新品首批
        String skuNo = detailList.stream().map(PurchasePriceChangeDetailEntity::getSkuNo).collect(Collectors.joining(","));
        variablesMap.put("skuNo", skuNo);
        return variablesMap;
    }


    /**
     * 临时修复线上数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tempUpdateHistoryDb() {

        List<PurchasePriceChangeDetailEntity> priceChangeDetailList = baseMapper.listTemp();

        Map<String, List<PurchasePriceChangeDetailEntity>> map = priceChangeDetailList.stream().collect(Collectors.groupingBy(PurchasePriceChangeDetailEntity::getPurchasePriceDetailId));
        List<PurchasePriceHistoryEntity> updateList = new ArrayList<>(10);
        for (Map.Entry<String, List<PurchasePriceChangeDetailEntity>> item : map.entrySet()) {
            //采购价目详情id
            String purchasePriceDetailId = item.getKey();
            List<PurchasePriceChangeDetailEntity> list = item.getValue();
            List<PurchasePriceHistoryEntity> priceEntityList = purchasePriceHistoryService.getHistoryByDetailIds(Arrays.asList(purchasePriceDetailId));
            list = list.stream().
                    sorted(Comparator.comparing(PurchasePriceChangeDetailEntity::getUpdateTime).reversed()).collect(Collectors.toList());
            for (int i = 0; i < priceEntityList.size(); i++) {
                if (list.size() > i) {
                    PurchasePriceHistoryEntity historyEntity = priceEntityList.get(i);
                    historyEntity.setChangeDetailId(list.get(i).getId());
                    updateList.add(historyEntity);
                }
            }

        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            purchasePriceHistoryService.updateBatchById(updateList);
        }
    }

    @Override
    public List<PurchasePriceChangeDTO.TabListDTO> tabList(PermissionsDTO dto) {
        PurchasePriceChangeTabFlagEnum[] values = PurchasePriceChangeTabFlagEnum.values();
        List<PurchasePriceChangeDTO.TabListDTO> list = new ArrayList<>();
        for (PurchasePriceChangeTabFlagEnum item : values) {
            PurchaseOrderDTO.SearchParamDTO searchParamDTO = new PurchaseOrderDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            PurchasePriceChangeDTO.TabListDTO resultDTO = new PurchasePriceChangeDTO.TabListDTO();
            String tabSql = purchasePriceChangeQueryHandler.getTabSql(item.getCode());
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
    public PagingVO<PurchasePriceChangeExportExcelDTO> exportPurchasePriceChange(PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        //获取导出数据
        Page<PurchasePriceChangeDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }

        List<PurchasePriceChangeExportExcelDTO> resultList = new ArrayList<>();

        List<String> skuIds = page.getRecords().stream().map(PurchasePriceChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);

        //采购价目变更详情id
        List<String> changeDetailIdList = page.getRecords().stream().map(PurchasePriceChangeDTO.PagingViewDTO::getChangeDetailId).collect(Collectors.toList());

        //采购价目详情表id
        List<String> purchasePriceDetailIds = page.getRecords().stream().map(PurchasePriceChangeDTO.PagingViewDTO::getPurchasePriceDetailId).collect(Collectors.toList());
        //历史的
        List<PurchasePriceHistoryEntity> historyList = purchasePriceHistoryService.listByChangeDetailIdList(changeDetailIdList);
        //获取到对应的价目明细
        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listByIds(purchasePriceDetailIds);

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        page.getRecords().forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), obj.getId())));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(ApiError.ERROR_500);
            }
        }

        for (PurchasePriceChangeDTO.PagingViewDTO item : page.getRecords()) {
            PurchasePriceChangeExportExcelDTO excelDTO = new PurchasePriceChangeExportExcelDTO();
            BeanMapper.copy(item, excelDTO);
            //sku信息
            SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            excelDTO.setProductName(skuVO.getSkuName());
            //历史报价
            PurchasePriceHistoryEntity historyEntity = historyList.stream().filter(h -> h.getChangeDetailId().equals(item.getChangeDetailId())).findFirst().orElse(null);
            //现有报价
            PurchasePriceDetailEntity priceDetailEntity = purchasePriceDetailList.stream().filter(p -> p.getId().equals(item.getPurchasePriceDetailId())).findFirst().orElse(null);
            if (historyEntity != null) {
                excelDTO.setOldTaxPrice(historyEntity.getTaxPrice());
                if (historyEntity.getTaxRate() != null) {
                    excelDTO.setOldTaxRate(historyEntity.getTaxRate().multiply(MathUtil.BigDecimal_100));
                    //升降比例
                    BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), historyEntity.getTaxPrice()), historyEntity.getTaxPrice()).multiply(MathUtil.BigDecimal_100);
                    excelDTO.setOffsetRate(StrUtil.format("{}%",offsetRate.stripTrailingZeros().toPlainString()));
                }
            } else {
                if (priceDetailEntity != null) {
                    excelDTO.setOldTaxPrice(priceDetailEntity.getTaxPrice());
                    if (priceDetailEntity.getTaxRate() != null) {
                        excelDTO.setOldTaxRate(priceDetailEntity.getTaxRate().multiply(MathUtil.BigDecimal_100));
                        //升降比例
                        BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), priceDetailEntity.getTaxPrice()), priceDetailEntity.getTaxPrice()).multiply(MathUtil.BigDecimal_100);
                        excelDTO.setOffsetRate(StrUtil.format("{}%",offsetRate.stripTrailingZeros().toPlainString()));
                    }
                }
            }
            ApproveStatusEnum approveStatusEnum = item.getApproveStatus();
            excelDTO.setApproveStatusName(approveStatusEnum.getName());
            Integer minQty = item.getMinQty();
            Integer maxQty = item.getMaxQty();
            excelDTO.setQtySection(minQty + "-" + maxQty);
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                excelDTO.setApproveUserName(curApprove);
            }
            excelDTO.setApproveTime(item.getApproveTime());
            resultList.add(excelDTO);
        }
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public List<PurchasePriceChangeEntity> listByCodes(List<String> codes) {
        return this.list(new QueryWrapper<PurchasePriceChangeEntity>().lambda().in(PurchasePriceChangeEntity::getCode, codes).eq(PurchasePriceChangeEntity::getIsDeleted,false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateApproveStatus(PurchasePriceChangeDTO.UpdateApprovalStatusDTO  updateApprovalStatusDTO) {
        PurchasePriceChangeEntity entity = updateApprovalStatusDTO.getPurchasePricechangeEntity();
        ApproveStatusEnum approveStatus = updateApprovalStatusDTO.getApproveStatus();
        updateApproveStatus(Collections.singletonList(entity), approveStatus);
    }

    /**
     * 校验附件必填
     * @author will
     * @date 2025/3/25 16:32
     * @param bussinessId
     */
    private void checkAttachment (String bussinessId) {
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(bussinessId);
        if (CollectionUtils.isEmpty(attachmentList)){
            throw new ServiceException(ApiError.TIME_NOT_NULL,"附件信息");
        }
    }
}
