package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.AttachmentDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcekDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.dmp.enums.AfterSaleStatusEnum;
import com.erp.server.dmp.mapper.AfterSaleMapper;
import com.erp.server.dmp.service.*;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import com.sdk.wx.miniapp.request.SubscribeMsgRequest;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_AFTER_SALE;

/**
 * <p>
 * 售后申请表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
@Slf4j
@Service
public class AfterSaleServiceImpl extends SuperServiceImpl<AfterSaleMapper, AfterSaleEntity> implements AfterSaleService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private AttachmentService attachmentService;
    @Resource
    private ThridUserInfoService thridUserInfoService;
    @Resource
    private AfterSaleDetailService afterSaleDetailService;
    @Resource
    private AfterSaleProgressService afterSaleProgressService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private WxMiniAppService wxMiniAppService;
    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private DmpSoOriginalInfoService dmpSoOriginalInfoService;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpSoOutstockService dmpSoOutstockService;
    @Resource
    private MQProducerService mQProducerService;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSaleDTO.AddDTO addDTO) {
        AfterSaleEntity afterSaleEntity = new AfterSaleEntity();
        BeanMapperUtils.copy(addDTO, afterSaleEntity);

        List<AfterSaleDTO.NodeDTO> nodeList = getNodeList();

        //第三方用户id为空的情况下，则新增用户
        if(StringUtils.isBlank(addDTO.getType())
                && addDTO.getType().equals("wx")
                && StringUtils.isBlank(addDTO.getThridUserId())){
            throw new ServiceException("第三方用户id不能为空");
        }else if(StringUtils.isBlank(addDTO.getThridUserId())){
            ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
            thridUserInfoEntity.setUsername(addDTO.getThridUserName());
            thridUserInfoEntity.setPhoneNumber(addDTO.getPhoneNumber());
            thridUserInfoEntity.setType(addDTO.getType());
            thridUserInfoService.save(thridUserInfoEntity);
            afterSaleEntity.setThridUserId(thridUserInfoEntity.getId());
        }else {
            ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
            thridUserInfoEntity.setId(afterSaleEntity.getThridUserId());
            thridUserInfoEntity.setUsername(addDTO.getThridUserName());
            thridUserInfoEntity.setPhoneNumber(addDTO.getPhoneNumber());
            thridUserInfoService.updateById(thridUserInfoEntity);

            afterSaleEntity.setThridUserId(addDTO.getThridUserId());
        }

        //总货值
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<AfterSaleDetailEntity> detailList = BeanMapper.copyList( addDTO.getDetailList(),AfterSaleDetailEntity.class);
        if(CollUtil.isNotEmpty(detailList)){
            List<String> skuIds = detailList.stream().map(AfterSaleDetailEntity::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
            Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, t -> t));
            List<AfterSaleDTO.DropDownDTO> detailByPlatformCode = getDetailByPlatformCode(addDTO.getPlatformCode());
            Map<String, AfterSaleDTO.DropDownDTO> downDTOMap = detailByPlatformCode.stream().collect(Collectors.toMap(AfterSaleDTO.DropDownDTO::getSkuId, t -> t, (k1, k2) -> k1));
            for (AfterSaleDetailEntity detail : detailList) {
                detail.setMainId(afterSaleEntity.getId());
                ProductDetailEntity productDetail = productDetailMap.getOrDefault(detail.getSkuId(), new ProductDetailEntity());
                detail.setSkuNo(productDetail.getSkuNo());
                detail.setProductName(productDetail.getName());
                if(downDTOMap.containsKey(detail.getSkuId())){
                    AfterSaleDTO.DropDownDTO downDTO = downDTOMap.get(detail.getSkuId());
                    detail.setPrice(downDTO.getPrice());
                    if(detail.getSkuQty().compareTo(downDTO.getSkuQty()) > 0){
                        throw new ServiceException("【"+detail.getSkuNo()+"】明细数量不能大于"+downDTO.getSkuQty());
                    }
                    //计算总货值
                    totalAmount = totalAmount.add(detail.getPrice().multiply(new BigDecimal(detail.getSkuQty())));
                }
            }
        }
        if(Objects.isNull(afterSaleEntity.getTotalPrice())){
            afterSaleEntity.setTotalPrice(totalAmount);
        }


        log.info("开始新增售后申请单");
        afterSaleEntity.setBillDate(LocalDate.now());
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHSQ);
        afterSaleEntity.setCode(code);
        boolean save = super.save(afterSaleEntity);
        if(!save) {
            throw new ServiceException("售后申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后申请单" , afterSaleEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "新增操作");

        //新增明细
        detailList.stream().forEach(e -> e.setMainId(afterSaleEntity.getId()));
        afterSaleDetailService.saveBatch(detailList);

        //新增维修记录
        List<AfterSaleProgressEntity> progressList = new ArrayList<>();
        for (AfterSaleDTO.NodeDTO nodeDTO : nodeList) {
            AfterSaleProgressEntity afterSaleProgressEntity = new AfterSaleProgressEntity();
            afterSaleProgressEntity.setMainId(afterSaleEntity.getId());
            afterSaleProgressEntity.setIndex(nodeDTO.getIndex());
            afterSaleProgressEntity.setNode(nodeDTO.getNode());
            //售后申请单，自动进入审核中
            if(nodeDTO.getNode().equals(AfterSaleStatusEnum.APPROVE_ING.getCode())){
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            //客户寄件
            if(nodeDTO.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())
                    && StringUtils.isNotBlank(addDTO.getReturnTrackNo())){
                afterSaleProgressEntity.setTrackNo(addDTO.getReturnTrackNo());
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            //售后发货
            if(nodeDTO.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                    && StringUtils.isNotBlank(addDTO.getOutboundTrackNo())){
                afterSaleProgressEntity.setTrackNo(addDTO.getOutboundTrackNo());
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            progressList.add(afterSaleProgressEntity);
        }
        afterSaleProgressService.saveBatch(progressList);

        //保存web附件
        if(CollUtil.isNotEmpty(addDTO.getAttachNameList()) && CollUtil.isNotEmpty(addDTO.getAttachUrlList())){
            List<String> attachUrlList = addDTO.getAttachUrlList();
            List<String> attachNameList = addDTO.getAttachNameList();
            for (int i = 0; i < attachUrlList.size(); i++) {
                AttachmentEntity entity = new AttachmentEntity();
                entity.setAttachName(attachNameList.get(i));
                entity.setAttachUrl(attachUrlList.get(i));
                entity.setType("after_sale");
                entity.setBusinessId(afterSaleEntity.getId());
                attachmentService.save(entity);
            }
        }
        //保存小程序附件
        if(CollUtil.isNotEmpty(addDTO.getAttachmentList())){
            for (String attachment : addDTO.getAttachmentList()) {
                AttachmentEntity entity = new AttachmentEntity();
                entity.setAttachName(attachment);
                entity.setAttachUrl(attachment);
                entity.setType("after_sale");
                entity.setBusinessId(afterSaleEntity.getId());
                attachmentService.save(entity);
            }
        }
        return new BaseResultDTO.AddDTO(afterSaleEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSaleDTO.UpdateDTO updateDTO) {
        AfterSaleEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "售后申请单"));
        if (!InvalidStatusEnum.NOT_VOIDED.getStatus().equals(old.getInvalidStatus())){
            throw new ServiceException("只有未作废的单据才能进行编辑");
        }else if(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(old.getStatus()) || AfterSaleStatusEnum.TERMINATED.getCode().equals(old.getStatus())  ){
            AfterSaleEntity afterSaleEntity = new AfterSaleEntity();
            BeanMapper.copy(old, afterSaleEntity);
            afterSaleEntity.setRemark(updateDTO.getRemark());
            log.info("编辑 开始修改售后申请单数据，单号：【{}】", old.getCode());
            boolean save = super.updateById(afterSaleEntity);
            if(!save) {
                throw new ServiceException("售后申请单更新失败");
            }
            // 记录主单操作日志
            log.info("编辑 开始记录售后申请单日志数据，单号：【{}】", afterSaleEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSaleEntity.getCode(), "售后申请单");
            operateLogService.addModuleOperateLogByObj(old, afterSaleEntity, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), msg);
        }else{ //正常更新
            AfterSaleEntity afterSaleEntity =  BeanMapperUtils.map(AfterSaleEntity.class, updateDTO);
            //更新用户信息
            if(StringUtils.isNotBlank(afterSaleEntity.getThridUserId())) {
                ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
                thridUserInfoEntity.setId(afterSaleEntity.getThridUserId());
                thridUserInfoEntity.setUsername(updateDTO.getThridUserName());
                thridUserInfoEntity.setPhoneNumber(updateDTO.getPhoneNumber());
                thridUserInfoService.updateById(thridUserInfoEntity);
            }

            //新增明细
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<AfterSaleDetailEntity> detailList = BeanMapper.copyList( updateDTO.getDetailList(),AfterSaleDetailEntity.class);
            if(CollUtil.isNotEmpty(detailList)){
                List<String> skuIds = detailList.stream().map(AfterSaleDetailEntity::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
                List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
                Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, t -> t));
                List<AfterSaleDTO.DropDownDTO> detailByPlatformCode = getDetailByPlatformCode(updateDTO.getPlatformCode());
                Map<String, AfterSaleDTO.DropDownDTO> downDTOMap = detailByPlatformCode.stream().collect(Collectors.toMap(AfterSaleDTO.DropDownDTO::getSkuId, t -> t, (k1, k2) -> k1));
                for (AfterSaleDetailEntity detail : detailList) {
                    detail.setMainId(afterSaleEntity.getId());
                    ProductDetailEntity productDetail = productDetailMap.getOrDefault(detail.getSkuId(), new ProductDetailEntity());
                    detail.setSkuNo(productDetail.getSkuNo());
                    detail.setProductName(productDetail.getName());
                    if(downDTOMap.containsKey(detail.getSkuId())){
                        AfterSaleDTO.DropDownDTO downDTO = downDTOMap.get(detail.getSkuId());
                        if(detail.getSkuQty().compareTo(downDTO.getSkuQty()) > 0){
                            throw new ServiceException("【"+detail.getSkuNo()+"】明细数量不能大于"+downDTO.getSkuQty());
                        }
                        detail.setPrice(downDTO.getPrice());
                        //计算总货值
                        totalAmount = totalAmount.add(detail.getPrice().multiply(new BigDecimal(detail.getSkuQty())));
                    }
                }
                // 删除明细数据
                List<String> ids = detailList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
                afterSaleDetailService.lambdaUpdate().eq(AfterSaleDetailEntity::getMainId, updateDTO.getId()).notIn(AfterSaleDetailEntity::getId, ids).remove();
                afterSaleDetailService.saveOrUpdateBatch(detailList);
            }

            if(Objects.isNull(afterSaleEntity.getTotalPrice())){
                afterSaleEntity.setTotalPrice(totalAmount);
            }

            // 删除明细数据
            attachmentService.lambdaUpdate().eq(AttachmentEntity::getType, "after_sale").eq(AttachmentEntity::getBusinessId, updateDTO.getId()).remove();
            //保存web附件
            if(CollUtil.isNotEmpty(updateDTO.getAttachNameList()) && CollUtil.isNotEmpty(updateDTO.getAttachUrlList())){
                List<String> attachUrlList = updateDTO.getAttachUrlList();
                List<String> attachNameList = updateDTO.getAttachNameList();
                for (int i = 0; i < attachUrlList.size(); i++) {
                    AttachmentEntity entity = new AttachmentEntity();
                    entity.setAttachName(attachNameList.get(i));
                    entity.setAttachUrl(attachUrlList.get(i));
                    entity.setType("after_sale");
                    entity.setBusinessId(afterSaleEntity.getId());
                    attachmentService.save(entity);
                }
            }
            //保存小程序附件
            if(CollUtil.isNotEmpty(updateDTO.getAttachmentList())){
                for (String attachment : updateDTO.getAttachmentList()) {
                    AttachmentEntity entity = new AttachmentEntity();
                    entity.setAttachName(attachment);
                    entity.setAttachUrl(attachment);
                    entity.setType("after_sale");
                    entity.setBusinessId(afterSaleEntity.getId());
                    attachmentService.save(entity);
                }
            }

            //更新运单号
            List<AfterSaleProgressEntity> afterSaleProgressList = afterSaleProgressService.listByMainIds(Collections.singletonList(updateDTO.getId()));
            for (AfterSaleProgressEntity afterSaleProgressEntity : afterSaleProgressList) {
                if(afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode()) && StringUtils.isNotBlank(updateDTO.getReturnTrackNo())){//客户寄件
                    afterSaleEntity.setStatus(AfterSaleStatusEnum.TO_BE_RETURNED.getCode());

                    afterSaleProgressEntity.setTrackNo(updateDTO.getReturnTrackNo());
                    afterSaleProgressEntity.setNodeTime(LocalDateTime.now());

//                    //发送微信订阅消息
//                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                        @Override
//                        public void afterCommit() {
//                            AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
//                            bean.sendSubscribeMsgRequest(afterSaleEntity,AfterSaleStatusEnum.TO_BE_RETURNED.getCode());
//                        }
//                    });
                }
                if(afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode()) && StringUtils.isNotBlank(updateDTO.getOutboundTrackNo())){//售后发货
                    afterSaleEntity.setStatus(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());

                    afterSaleProgressEntity.setTrackNo(updateDTO.getOutboundTrackNo());
                    afterSaleProgressEntity.setNodeTime(LocalDateTime.now());

//                    //发送微信订阅消息
//                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                        @Override
//                        public void afterCommit() {
//                            AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
//                            bean.sendSubscribeMsgRequest(afterSaleEntity,AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
//                        }
//                    });
                }
                afterSaleProgressService.updateById(afterSaleProgressEntity);
            }

            log.info("编辑 开始修改售后申请单数据，单号：【{}】", old.getCode());
            boolean save = super.updateById(afterSaleEntity);
            if(!save) {
                throw new ServiceException("售后申请单保存失败");
            }
            // 记录主单操作日志
            log.info("编辑 开始记录售后申请单日志数据，单号：【{}】", afterSaleEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSaleEntity.getCode(), "售后申请单");
            operateLogService.addModuleOperateLogByObj(old, afterSaleEntity, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), msg);
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AfterSaleDTO.ListDTO> paging(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AfterSaleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<DmpAfterSaleExcekDTO> exportList(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpAfterSaleExcekDTO> pageData = this.baseMapper.listExport(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 属性赋值
        for(DmpAfterSaleExcekDTO data : pageData.getRecords()) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //单据状态
            data.setStatusName(AfterSaleStatusEnum.getNode(data.getStatus()));
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<AfterSaleDTO.TabListDTO> tabList(PermissionsDTO param) {
        AfterSaleDTO.PagingParamDTO searchParam = new AfterSaleDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AfterSaleDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        list.forEach(item -> item.setTabFlagName(ApproveStatusEnum.getName(item.getTabFlag())));
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(AfterSaleDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status) && !ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(status)) {
                list.add(new AfterSaleDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
            }
        });
        return list;
    }

    @Override
    public void exportList(AfterSaleDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("售后申请导出", EXPORT_DMP_AFTER_SALE.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AfterSaleEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"售后申请");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改售后申请单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        log.info("提交 开始启动售后申请单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录售后申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "提交操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AfterSaleDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AfterSaleDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AfterSaleEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 审核中的数据允许审核
        if(Objects.equals(entity.getInvalidStatus(), InvalidStatusEnum.VOIDED.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98012);
        }

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg,  ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AfterSaleEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AfterSaleEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除主单数据
        log.info("删除 开始删除售后申请单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除售后申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getCode(), "删除售后申请单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        if(AfterSaleStatusEnum.TERMINATED.getCode().equals(entity.getStatus())
                || AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(entity.getStatus())){
           return  BatchResultDTO.fail(entity.getId(), entity.getCode(),"只有未完成、未中止、未作废的数据支持作废");
        }
        log.info("作废 开始修改售后申请单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AfterSaleEntity::getId, id)
            .set(AfterSaleEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
             .set(AfterSaleEntity::getInvalidTime, LocalDateTime.now())
            .set(AfterSaleEntity::getStatus, AfterSaleStatusEnum.TERMINATED.getCode())
            .set(AfterSaleEntity::getInvalidRemark, remark)
            .update();

        //更新节点时间
        //更新通过，则进入下一个节点：终止
        AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(entity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        afterSaleProgressEntity.setNode(AfterSaleStatusEnum.TERMINATED.getCode());
        afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
        afterSaleProgressService.updateById(afterSaleProgressEntity);


        //发送微信订阅消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendSubscribeMsgRequest(entity,AfterSaleStatusEnum.TERMINATED.getCode());
            }
        });

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }


    /**
     * 作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalidByCode(String code) {
        if(StringUtils.isBlank(code)){
            throw new ServiceException("工单号不能为空");
        }
        AfterSaleEntity afterSaleEntity = lambdaQuery().eq(AfterSaleEntity::getCode, code).one();
        if (Objects.isNull(afterSaleEntity)) {
            throw new ServiceException("售后申请单不存在");
        }

        if(AfterSaleStatusEnum.TERMINATED.getCode().equals(afterSaleEntity.getStatus())
                || AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(afterSaleEntity.getStatus())){
            return  BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(),"只有未完成、未中止的数据支持作废");
        }

        afterSaleEntity.setInvalidStatus(InvalidStatusEnum.VOIDED.getStatus());
        afterSaleEntity.setInvalidTime(LocalDateTime.now());
        afterSaleEntity.setStatus(AfterSaleStatusEnum.TERMINATED.getCode());
        updateById(afterSaleEntity);

        //更新节点时间
        //更新通过，则进入下一个节点：终止
        AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        afterSaleProgressEntity.setNode(AfterSaleStatusEnum.TERMINATED.getCode());
        afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
        afterSaleProgressService.updateById(afterSaleProgressEntity);

        //发送微信订阅消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendSubscribeMsgRequest(afterSaleEntity,AfterSaleStatusEnum.TERMINATED.getCode());
            }
        });

        return BatchResultDTO.success(afterSaleEntity.getId(), afterSaleEntity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"售后申请"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始修改售后申请单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "取消流程操作");

        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AfterSaleEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        if(approveStatus.equals(ApproveStatusEnum.REJECT)){

            entity.setStatus(AfterSaleStatusEnum.TERMINATED.getCode());

            //审批不通过，则最后的节点为终止
            AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(entity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
            afterSaleProgressEntity.setNode(AfterSaleStatusEnum.TERMINATED.getCode());
            afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            afterSaleProgressService.updateById(afterSaleProgressEntity);

            //发送微信订阅消息
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                    bean.sendSubscribeMsgRequest(entity,AfterSaleStatusEnum.TERMINATED.getCode());
                }
            });
        }else {

            entity.setStatus(AfterSaleStatusEnum.TO_BE_RETURNED.getCode());
            //更新节点时间
            //更新通过，则进入下一个节点：待寄回
            updateProgressByMainId(entity.getId(), AfterSaleStatusEnum.TO_BE_RETURNED.getCode(),"","");
            //发送微信订阅消息
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                    bean.sendSubscribeMsgRequest(entity,AfterSaleStatusEnum.TO_BE_RETURNED.getCode());
                }
            });
        }
        //更新单据状态
        updateById(entity);
        return updateForApprove(entity.getId(), approveStatus.getStatus());
    }

    //更新节点时间
    private void updateProgressByMainId(String id,String node,String logisticsCode,String remark) {
        //更新单据状态
        AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(id, node);
        if (Objects.nonNull(afterSaleProgressEntity)) {
            afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            if(StringUtils.isNotBlank(logisticsCode)){
                afterSaleProgressEntity.setTrackNo(logisticsCode);
            }
            if(StringUtils.isNotBlank(remark)){
                afterSaleProgressEntity.setRemark(remark);
            }
            afterSaleProgressService.updateById(afterSaleProgressEntity);
        }
    }

    @Override
    public AfterSaleDTO.ViewDTO view(String id) {
        AfterSaleEntity afterSaleEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到售后申请单数据"));
        AfterSaleDTO.ViewDTO data = BeanMapperUtils.map(AfterSaleDTO.ViewDTO.class, afterSaleEntity);
        // 数据填充处理
        fillOne(data);
        //查询用户信息
        ThridUserInfoEntity thridUserInfoEntity = thridUserInfoService.getById(afterSaleEntity.getThridUserId());
        if(Objects.nonNull(thridUserInfoEntity)){
            data.setNickName(thridUserInfoEntity.getNickName());
            data.setPhoneNumber(thridUserInfoEntity.getPhoneNumber());
            data.setThridUserName(thridUserInfoEntity.getUsername());
        }

        //查询明细
        List<AfterSaleDetailEntity> detailList = afterSaleDetailService.listByMainIds(Collections.singletonList(id));
        data.setDetailList(detailList);
        //查询附件
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        if(CollUtil.isNotEmpty(attachmentList)) {
            List<String> attachNameList = new ArrayList<>();
            List<String> attachUrlList = new ArrayList<>();

            for (AttachmentDTO.UpdateDTO dto : attachmentList) {
                attachNameList.add(dto.getAttachName());
                attachUrlList.add(dto.getAttachUrl());
            }
            data.setAttachNameList(attachNameList);
            data.setAttachUrlList(attachUrlList);
        }
        //查询进度
        List<AfterSaleProgressEntity> afterSaleProgressList = afterSaleProgressService.listByMainIds(Collections.singletonList(id));
        for (AfterSaleProgressEntity afterSaleProgressEntity : afterSaleProgressList) {
            if(afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())){//客户寄件
                data.setReturnTrackNo(afterSaleProgressEntity.getTrackNo());
            }
            if(afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())){//售后发货
                data.setOutboundTrackNo(afterSaleProgressEntity.getTrackNo());
            }
        }
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AfterSaleEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AfterSaleDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus().getCode()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        //单据状态
        data.setStatusName(AfterSaleStatusEnum.getNode(data.getStatus()));

        if(data.getStatus().equals(ApproveStatusEnum.APPROVE_ING.getCode())){
            data.setApproveTime(null);
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public Boolean updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return this.lambdaUpdate().eq(AfterSaleEntity::getId, id)
            .set(AfterSaleEntity::getApproveUserId, userInfo.getUid())
            .set(AfterSaleEntity::getApproveUserName, userInfo.getUserName())
            .set(AfterSaleEntity::getApproveStatus, approveStatus)
            .set(AfterSaleEntity::getApproveTime, LocalDateTime.now())
            .update(new AfterSaleEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AfterSaleEntity::getId, id)
            .set(AfterSaleEntity::getApproveUserId, "")
            .set(AfterSaleEntity::getApproveUserName, "")
            .set(AfterSaleEntity::getApproveStatus, approveStatus)
            .set(AfterSaleEntity::getApproveTime, null)
            .update(new AfterSaleEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        LoginUser defaultLoginUser = UserContext.getDefaultLoginUser();
        lambdaUpdate().eq(AfterSaleEntity::getId, id)
        .set(AfterSaleEntity::getApproveStatus, approveStatus)
        .set(AfterSaleEntity::getApproveUserId, defaultLoginUser.getUid())
        .set(AfterSaleEntity::getApproveUserName, defaultLoginUser.getUserName())
        .set(AfterSaleEntity::getApproveTime, LocalDateTime.now())
        .update(new AfterSaleEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AfterSaleDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AfterSaleDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //单据状态
            data.setStatusName(AfterSaleStatusEnum.getNode(data.getStatus()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AfterSaleEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }


    /**
     * 获取节点配置信息
     */
    @Override
    public List<AfterSaleDTO.NodeDTO>  getNodeList() {
        String value = cfgSettingService.getValue(SettingEnum.AFTER_SALSE_NODE);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("售后维修节点配置不存在");
        }
        // 解析 JSON 字符串为 List<AfterSaleDTO.NodeDTO>
        List<AfterSaleDTO.NodeDTO> nodeList = JSONUtil.toList(JSONUtil.parseObj(value).getJSONArray("nodeList"), AfterSaleDTO.NodeDTO.class);
        return nodeList;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AfterSaleEntity afterSaleEntity) {



    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> changeStatus(AfterSaleDTO.IdsDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>();
        List<AfterSaleEntity> entityList = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(entityList)) {
            throw new ServiceException("未找到售后申请单数据");
        }

        if(StringUtil.isBlank(dto.getNode())){
            throw new ServiceException("请选择单据状态");
        }

        AfterSaleStatusEnum afterSaleStatus = AfterSaleStatusEnum.getByCode(dto.getNode());
        if(Objects.isNull(afterSaleStatus)){
            throw new ServiceException("未找到单据状态信息");
        }

        Map<String, AfterSaleDTO.NodeDTO> nodeMap = getNodeList().stream().collect(Collectors.toMap(AfterSaleDTO.NodeDTO::getNode, w -> w));
        AfterSaleDTO.NodeDTO newNodeDTO = nodeMap.get(dto.getNode());

        for (AfterSaleEntity entity : entityList) {
            BatchResultDTO batchResultDTO;
            if( !ApproveStatusEnum.APPROVE.getCode().equals(entity.getApproveStatus().getCode())
                    || AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(entity.getStatus())
                    || AfterSaleStatusEnum.TERMINATED.getCode().equals(entity.getStatus())) {
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(),"只有审核通过并且单据状态未完成、未中止数据支持状态修改");
            }else {
                AfterSaleDTO.NodeDTO oldNodeDTO = nodeMap.get(entity.getStatus());
                if(oldNodeDTO.getIndex() > newNodeDTO.getIndex()){
                    batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "当前节点不能小于等于原节点");
                }else {
                    batchResultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);


                    //更新状态
                    entity.setStatus(dto.getNode());

                    AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(entity.getId(), dto.getNode());
                    afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
                    if(StringUtils.isNotBlank(dto.getTrackNo())){
                        afterSaleProgressEntity.setTrackNo(dto.getTrackNo());
                    }
                    afterSaleProgressEntity.setRemark(dto.getRemark());
                    afterSaleProgressService.updateById(afterSaleProgressEntity);

                    //erp状态变更客户寄件，输入快递单号后，需要流转至售后签收（待签收）
                    if(StringUtils.isNotBlank(dto.getTrackNo()) && afterSaleStatus.getCode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())){
                        AfterSaleProgressEntity byNode = afterSaleProgressService.getByNode(entity.getId(), AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode());
                        byNode.setNodeTime(LocalDateTime.now());
                        afterSaleProgressService.updateById(byNode);
                        //更新状态
                        entity.setStatus(AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode());
                    }

                    //更新
                    updateById(entity);

                    //发送微信订阅消息
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                            bean.sendSubscribeMsgRequest(entity,afterSaleStatus.getCode());
                        }
                    });
                }
            }
            resultList.add(batchResultDTO);
        }
        return resultList;
    }


    /**
     * 寄修进度
     */
    @Override
    public AfterSaleProgressDTO.RepairRecordDTO getRepairProgress(AfterSaleDTO.ProgressDTO dto) {
        AfterSaleProgressDTO.RepairRecordDTO repairRecordDTO = new AfterSaleProgressDTO.RepairRecordDTO();
        List<AfterSaleProgressDTO.RepairRecordListDTO> repairProgress = this.baseMapper.getRepairProgress(dto);
        repairProgress.forEach(t -> {
            t.setNodeName(AfterSaleStatusEnum.getNode(t.getNode()));
            if (Objects.nonNull(t.getNodeTimeLd())) {
                t.setNodeTime(LocalDateTimeUtil.format(t.getNodeTimeLd(), DateUtil.fmt));
            }
            if(StringUtils.isNotBlank(t.getTrackNo())){
                if(StringUtils.isBlank(t.getRemark())){
                    t.setRemark(t.getTrackNo());
                }else {
                    t.setRemark(t.getTrackNo() + "\n" +t.getRemark());
                }
            }
        });

        // 过滤 nodeTime 不为空的记录找出 index 最大的记录
        AfterSaleProgressDTO.RepairRecordListDTO repairRecordListDTO = repairProgress.stream()
                .filter(record -> record.getNodeTimeLd() != null)
                .max(Comparator.comparingInt(AfterSaleProgressDTO.RepairRecordListDTO::getIndex)).get();

        repairRecordDTO.setActive(repairRecordListDTO.getIndex() - 1);
        repairRecordDTO.setRecordList(repairProgress);
        return repairRecordDTO;
    }
    /**
     * 寄修历史
     */
    @Override
    public List<AfterSaleProgressDTO.RepairHistoryListDTO> getRepairHistory(AfterSaleDTO.ThridUserDTO dto) {
        List<AfterSaleProgressDTO.RepairHistoryListDTO> repairHistory = this.baseMapper.getRepairHistory(dto);
        repairHistory.forEach(t -> t.setStatusName(AfterSaleStatusEnum.getNode(t.getStatus())));
        return repairHistory;
    }

    @Override
    public String getAccessToken() {
        return wxMiniAppService.getAccessToken();
    }

    @Override
    public WxJscodeToSessionResponse jsCode2SessionInfo(String jsCode){
        WxJscodeToSessionResponse wxJscodeToSessionResponse = wxMiniAppService.jsCode2SessionInfo(jsCode);
        return wxJscodeToSessionResponse;
    }


    @Override
    public void syncWdtToAfterSale() {
        List<AfterSaleEntity> list = lambdaQuery().eq(AfterSaleEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .ne(AfterSaleEntity::getRepairInvoiceCode, "")
                .list();
        if(CollUtil.isNotEmpty(list)){
            List<String> repairInvoiceCodeList = list.stream().map(AfterSaleEntity::getRepairInvoiceCode).collect(Collectors.toList());
            List<DmpSoOutstockEntity> dmpSoOutstockList = dmpSoOutstockService.lambdaQuery()
                    .eq(DmpSoOutstockEntity::getStatus, "1")
                    .in(DmpSoOutstockEntity::getThirdBillNo, repairInvoiceCodeList).list();
            if(CollUtil.isNotEmpty(dmpSoOutstockList)){
                Map<String, DmpSoOutstockEntity> map = dmpSoOutstockList.stream().collect(Collectors.toMap(DmpSoOutstockEntity::getPlatformCode, t -> t, (k1, k2) -> k1));
                for (AfterSaleEntity afterSaleEntity : list) {
                    String repairInvoiceCode = afterSaleEntity.getRepairInvoiceCode();
                    if(map.containsKey(repairInvoiceCode) && StringUtils.isNotBlank(map.get(repairInvoiceCode).getLogisticsCode())){
                        //更新节点时间
                        //更新通过，则进入下一个节点：已完成
                        String logisticsCode = map.get(repairInvoiceCode).getLogisticsCode();
                        updateProgressByMainId( afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode(),logisticsCode,"");

                        //发送微信订阅消息
                        sendSubscribeMsgRequest(afterSaleEntity,AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
                    }
                }
            }
        }
    }

    /**
     * 根据平台代码获取详情信息
     * 此方法首先会根据平台代码从两个不同的服务中获取数据，然后分别对获取到的数据进行处理
     * 处理过程中，会通过不同的API调用获取更多的产品信息，并将这些信息整合到最终的结果列表中
     * @param platformCode 平台代码，用于查询详情信息
     * @return 返回一个包含详情信息的列表，如果查询不到相关信息，则返回空列表
     */
    @Override
    public List<AfterSaleDTO.DropDownDTO> getDetailByPlatformCode(String platformCode) {
        // 检查平台代码是否为空，如果为空则直接返回空列表
        if(StringUtil.isEmpty(platformCode)){
            return Collections.emptyList();
        }
        List<AfterSaleDTO.DropDownDTO> resultList = new ArrayList<>();
//        // 从dmpSoInfoService服务中获取详情信息列表
//        getPlatformMappingList(platformCode, resultList);
        // 从dmpSoOriginalInfoService服务中获取详情信息列表
        getWdtMappingList(platformCode, resultList);
        // 返回最终的结果列表
        return resultList;
    }




    private void getWdtMappingList(String platformCode, List<AfterSaleDTO.DropDownDTO> resultList) {
        // 从dmpSoOriginalInfoService服务中获取详情信息列表
        List<AfterSaleDTO.DropDownDTO> wdtDropDownDTOS = dmpSoOriginalInfoService.listDetailByPlatformCode(platformCode);
        // 如果获取到的信息列表不为空，则进一步处理
        if(CollUtil.isNotEmpty(wdtDropDownDTOS)){
            // 提取并去重商品的平台SKU编号列表
            List<String> platformSkuNoList = wdtDropDownDTOS.stream().map(AfterSaleDTO.DropDownDTO::getSkuNo).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
            // 调用远程服务获取商品信息列表
            List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(platformSkuNoList);
            // 将商品信息列表转换为Map，以便后续查询
            Map<String, String> map = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId, (k1, k2) -> k1));
            // 遍历原始信息列表，更新商品SKU ID
            for (AfterSaleDTO.DropDownDTO drop : wdtDropDownDTOS) {
                String skuId = map.getOrDefault(drop.getSkuNo(), "");
                if(StringUtil.isNotBlank(skuId)){
                    drop.setSkuId(skuId);
                    resultList.add(drop);
                }
            }
        }
    }

    private void getPlatformMappingList(String platformCode, List<AfterSaleDTO.DropDownDTO> resultList) {
        // 从dmpSoInfoService服务中获取详情信息列表
        List<AfterSaleDTO.DropDownDTO> dropDownDTOS = dmpSoInfoService.listDetailByPlatformCode(platformCode);
        // 如果获取到的信息列表不为空，则进一步处理
        if(CollUtil.isNotEmpty(dropDownDTOS)){
            // 提取并去重商品的平台SKU ID列表
            List<String> platformSkuIdList = dropDownDTOS.stream().map(AfterSaleDTO.DropDownDTO::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
            // 获取第一个元素的店铺ID和平台类型
            String shopId = dropDownDTOS.get(0).getShopId();
            String platform = dropDownDTOS.get(0).getThirdType();

            // 创建查询参数对象
            ListingInfoParamDTO dto = new ListingInfoParamDTO();
            dto.setPlatform(platform);
            dto.setType(RuleTypeEnum.PLATFORM.getCode());
            dto.setPlatformSkuIdList(platformSkuIdList);
            dto.setShopIdList(Collections.singletonList(shopId));

            // 调用远程服务获取商品信息列表
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOS = skuMappingFeign.listingInfoWithSkuMappingList(dto);
            // 如果获取到的商品信息列表不为空，则进一步处理
            if(CollUtil.isNotEmpty(listingInfoWithSkuMappingDTOS)){
                // 将商品信息列表转换为Map，以便后续查询
                Map<String, ListingInfoWithSkuMappingDTO> map = listingInfoWithSkuMappingDTOS.stream().collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuId, t->t, (k1, k2) -> k1));
                // 遍历原始信息列表，更新商品SKU信息
                for (AfterSaleDTO.DropDownDTO drop : dropDownDTOS) {
                    ListingInfoWithSkuMappingDTO skuMappingDTO = map.getOrDefault(drop.getSkuId(), null);
                    if(Objects.nonNull(skuMappingDTO)){
                        drop.setSkuId(skuMappingDTO.getProductSkuId());
                        drop.setSkuNo(skuMappingDTO.getProductSkuNo());
                        drop.setProductName(skuMappingDTO.getProductName());
                        resultList.add(drop);
                    }
                }
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean udpateTrackNo(AfterSaleDTO.UpdateTrackNoDTO dto) {
        String code = dto.getCode();
        String trackNo = dto.getTrackNo();

        AfterSaleEntity afterSaleEntity = lambdaQuery().eq(AfterSaleEntity::getCode, code).one();
        if (Objects.isNull(afterSaleEntity)) {
            throw new ServiceException("售后申请单不存在");
        }
        //更新单据状态
        afterSaleEntity.setStatus(AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode());
        updateById(afterSaleEntity);
        //更新运单号
        boolean update = afterSaleProgressService.lambdaUpdate().eq(AfterSaleProgressEntity::getMainId, afterSaleEntity.getId())
                .eq(AfterSaleProgressEntity::getNode, AfterSaleStatusEnum.TO_BE_RETURNED.getCode())
                .set(AfterSaleProgressEntity::getTrackNo, trackNo)
                .update();

        //更新节点时间
        //更新通过，则进入下一个节点：售后签收
        updateProgressByMainId(afterSaleEntity.getId(), AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode(),trackNo,"待签收");

        //发送微信订阅消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendSubscribeMsgRequest(afterSaleEntity,AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode());
            }
        });
        return update;
    }




    public void sendSubscribeMsgRequest(AfterSaleEntity afterSaleEntity, String status){
        String code = afterSaleEntity.getCode();
//        String repairInvoiceCode = afterSaleEntity.getRepairInvoiceCode();
        String thridUserId = afterSaleEntity.getThridUserId();
        if(Objects.isNull(afterSaleEntity)){
            return ;

        }
        if(StringUtils.isBlank(code)
                || StringUtils.isBlank(status)
                || StringUtils.isBlank(thridUserId)){
            return ;
        }

        ThridUserInfoEntity thridUserInfoEntity = thridUserInfoService.getById(thridUserId);
        String openId;
        if(Objects.nonNull(thridUserInfoEntity) && StringUtils.isNotBlank(thridUserInfoEntity.getOpenid())){
            openId = thridUserInfoEntity.getOpenid();
        }else {
            return ;
        }

        String value = cfgSettingService.getValue(SettingEnum.AFTER_SALSE_SUBSCRIBE_MSG);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("售后微信消息订阅配置不存在");
        }

        //推送mq 让消费者去执行消息推送
        SubscribeMsgRequest request = JSONUtil.toBean(value, SubscribeMsgRequest.class);
        request.setTouser(openId);
        request.setPage(request.getPage()+code);
        Map<String, SubscribeMsgRequest.DataItem> data = request.getData();
        data.get("character_string1").setValue(code);
        data.get("thing2").setValue(AfterSaleStatusEnum.getNode(status));
        data.get("time6").setValue(LocalDateTimeUtil.format(LocalDateTime.now(), DateUtil.fmt));

        DmpPushMsgEntity dmpPushMsgEntity = new DmpPushMsgEntity();
        // 将请求对象转换为 JSON 字符串
        String jsonStr = JSONUtil.toJsonStr(request);

        dmpPushMsgEntity.setTargetPlatform("wx");
        dmpPushMsgEntity.setSourcePlatform(ServiceCodeNameEnum.DMP.getCode());
        dmpPushMsgEntity.setSourceType(SourceTypeEnum.AFTER_SALE.getCode());
        dmpPushMsgEntity.setSourceId(afterSaleEntity.getId());
        dmpPushMsgEntity.setSourceCode(afterSaleEntity.getCode());
        dmpPushMsgEntity.setPushData(jsonStr);
        dmpPushMsgEntity.setMessageCreateTime(LocalDateTime.now());
        dmpPushMsgEntity.setMessageCreateTime(LocalDateTime.now());

        SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.DMP_WECHAT_SUBSCRIBE_MSG_TOPIC, RocketMqTagEnum.DMP_WECHAT_SUBSCRIBE_MSG_TAG.getName(),
                dmpPushMsgEntity, IdUtil.simpleUUID());
    }

}
