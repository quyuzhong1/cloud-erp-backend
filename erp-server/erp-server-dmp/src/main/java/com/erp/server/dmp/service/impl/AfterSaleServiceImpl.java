package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CountrySiteEnum;
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
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.enums.ThirdMappingSystemEnum;
import com.erp.model.dmp.validator.AfterSaleLogisticsManualOrderGroup;
import com.erp.model.dmp.validator.AfterSaleLogisticsPlatformOrderGroup;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;
import com.erp.model.tms.enums.ExceptionTypeEnum;
import com.erp.model.tms.enums.LogisticsLabelStatusEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.ShipmentTypeEnum;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.LogisticsOrderFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.dmp.constant.DmpConstant;
import com.erp.server.dmp.enums.AfterSaleLogisticsOrderModeEnum;
import com.erp.server.dmp.enums.AfterSaleStatusEnum;
import com.erp.server.dmp.enums.OutboundTrackNoTypeEnum;
import com.erp.server.dmp.mapper.AfterSaleMapper;
import com.erp.server.dmp.service.*;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import com.sdk.wx.miniapp.request.SubscribeMsgRequest;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_AFTER_SALE;
import com.common.message.constant.DistributeKeyConstant;

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
    @Resource
    private DmpPushMsgService dmpPushMsgService;

    @Resource
    private CfgAfterPlatformShopService cfgAfterPlatformShopService;

    @Resource
    private ThirdMappingService thirdMappingService;

    @Resource
    private DmpBasicSystemService dmpBasicSystemService;

    @Resource
    private OmsDropDownFeign omsDropDownFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @Resource
    private LogisticsOrderFeign logisticsOrderFeign;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Resource
    private Validator validator;

    private static final String AFTER_SALE_LOCK = "dmp:after:sale";

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSaleDTO.AddDTO addDTO) {
        if(addDTO.getType().equals("selfAdd") && CollUtil.isEmpty(addDTO.getDetailList()) && CollUtil.isEmpty(addDTO.getAttachUrlList())){
            throw new ServiceException("sku明细或图片附件至少填写一种");
        }

        AfterSaleEntity afterSaleEntity = new AfterSaleEntity();
        BeanMapperUtils.copy(addDTO, afterSaleEntity);
        afterSaleEntity.setType("");
        List<AfterSaleDTO.NodeDTO> nodeList = loadAllNodeList();


        //第三方用户id为空的情况下，则新增用户
        if (addDTO.getType().equals("wx")
                && StringUtils.isBlank(addDTO.getThridUserId())) {
            throw new ServiceException("登录异常，请退出小程序重新授权登录");
        } else if (addDTO.getType().equals("selfAdd")
                && StringUtils.isBlank(addDTO.getThridUserId())) {
            ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
            thridUserInfoEntity.setUsername(addDTO.getThridUserName());
            thridUserInfoEntity.setPhoneNumber(addDTO.getPhoneNumber());
            thridUserInfoEntity.setType(addDTO.getType());
            thridUserInfoService.save(thridUserInfoEntity);
            afterSaleEntity.setThridUserId(thridUserInfoEntity.getId());
        } else {
            ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
            thridUserInfoEntity.setId(afterSaleEntity.getThridUserId());
            thridUserInfoEntity.setUsername(addDTO.getThridUserName());
            thridUserInfoEntity.setPhoneNumber(addDTO.getPhoneNumber());
            thridUserInfoService.updateById(thridUserInfoEntity);
            afterSaleEntity.setThridUserId(addDTO.getThridUserId());
        }

        //总货值
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<AfterSaleDetailEntity> detailList = BeanMapper.copyList(addDTO.getDetailList(), AfterSaleDetailEntity.class);
        if (CollUtil.isNotEmpty(detailList)) {
            totalAmount = getBigDecimal(detailList, addDTO.getPlatformCode(), afterSaleEntity, totalAmount);
        }
        if (Objects.isNull(afterSaleEntity.getTotalPrice())) {
            afterSaleEntity.setTotalPrice(totalAmount);
        }

        if (Objects.isNull(afterSaleEntity.getTotalRepairAmount())) {
            afterSaleEntity.setTotalRepairAmount(BigDecimal.ZERO);
        }

        log.info("开始新增售后申请单");
        afterSaleEntity.setBillDate(LocalDate.now());

        afterSaleEntity.setUsername(addDTO.getThridUserName());
        afterSaleEntity.setPhoneNumber(addDTO.getPhoneNumber());

        String platformCode = addDTO.getPlatformCode();
        DmpSoInfoEntity dmpSoInfoEntity = dmpSoInfoService.lambdaQuery()
                .eq(DmpSoInfoEntity::getPlatformCode, platformCode)
                .eq(DmpSoInfoEntity::getSourceSystem, PlatformDictEnum.WDT.getCode())
                .eq(DmpSoInfoEntity::getInvalidStatus, Boolean.FALSE)
                .orderByDesc(DmpSoInfoEntity::getCreateTime)
                .last("limit 1")
                .one();
        // 【需求】优先按旺店通订单店铺匹配售后人员：dmp_so_info.shopId → third_shop → third_mapping → 系统店铺
        if (Objects.nonNull(dmpSoInfoEntity)) {
            List<CfgAfterPlatformShopDTO.CsAgentDTO> csAgentDTOList = cfgAfterPlatformShopService.matchCsAgent(addDTO.getDictPlatform(), dmpSoInfoEntity.getShopId());
            if (!csAgentDTOList.isEmpty()) {
                String ids = csAgentDTOList.stream()
                        .map(CfgAfterPlatformShopDTO.CsAgentDTO::getId)
                        .collect(Collectors.joining(","));
                String names = csAgentDTOList.stream()
                        .map(CfgAfterPlatformShopDTO.CsAgentDTO::getName)
                        .collect(Collectors.joining(","));
                afterSaleEntity.setCsAgentId(ids);
                afterSaleEntity.setCsAgentName(names);
            }
        } else {
            // 【需求】未找到旺店通订单时，按平台兜底匹配售后人员
            log.warn("未找到旺店通订单，按平台兜底匹配售后人员，platformCode={}", platformCode);
            ThirdMappingEntity thirdMapping = thirdMappingService.lambdaQuery()
                    .eq(ThirdMappingEntity::getThirdSysType, ThirdMappingSystemEnum.ERP.getCode())
                    .eq(ThirdMappingEntity::getThirdCode, addDTO.getDictPlatform())
                    .eq(ThirdMappingEntity::getDisabled, Boolean.FALSE)
                    .orderByDesc(ThirdMappingEntity::getCreateTime)
                    .last("limit 1")
                    .one();
            if (Objects.nonNull(thirdMapping)) {
                // 如果店铺ID为空，则按平台匹配
                List<CfgAfterPlatformShopDTO.CsAgentDTO> csAgentDTOList = cfgAfterPlatformShopService.matchCsAgent(thirdMapping.getThirdCode(), null);
                if (!csAgentDTOList.isEmpty()) {
                    String ids = csAgentDTOList.stream()
                            .map(CfgAfterPlatformShopDTO.CsAgentDTO::getId)
                            .collect(Collectors.joining(","));
                    String names = csAgentDTOList.stream()
                            .map(CfgAfterPlatformShopDTO.CsAgentDTO::getName)
                            .collect(Collectors.joining(","));
                    afterSaleEntity.setCsAgentId(ids);
                    afterSaleEntity.setCsAgentName(names);
                }
            }
        }

        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHSQ);
        afterSaleEntity.setCode(code);
        // 商家寄出快递单号不为空 设置类型为手动获取：MANUAL
        if (StringUtils.isNotBlank(addDTO.getOutboundTrackNo())) {
            afterSaleEntity.setType(OutboundTrackNoTypeEnum.MANUAL.getCode());
        }
        boolean save = super.save(afterSaleEntity);
        if (!save) {
            throw new ServiceException("售后申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后申请单", afterSaleEntity.getCode());
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
            if (nodeDTO.getNode().equals(AfterSaleStatusEnum.APPROVE_ING.getCode())) {
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            //客户寄件
            if (nodeDTO.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())
                    && StringUtils.isNotBlank(addDTO.getReturnTrackNo())) {
                afterSaleProgressEntity.setTrackNo(addDTO.getReturnTrackNo());
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            //售后发货
            if (nodeDTO.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                    && StringUtils.isNotBlank(addDTO.getOutboundTrackNo())) {
                afterSaleProgressEntity.setTrackNo(addDTO.getOutboundTrackNo());
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            //已完成
            if (nodeDTO.getNode().equals(AfterSaleStatusEnum.COMPLETED.getCode())
                    && StringUtils.isNotBlank(addDTO.getOutboundTrackNo())) {
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            progressList.add(afterSaleProgressEntity);
        }
        afterSaleProgressService.saveBatch(progressList);

        //保存web附件
        if (CollUtil.isNotEmpty(addDTO.getAttachNameList()) && CollUtil.isNotEmpty(addDTO.getAttachUrlList())) {
            List<String> attachUrlList = addDTO.getAttachUrlList();
            List<String> attachNameList = addDTO.getAttachNameList();
            for (int i = 0; i < attachUrlList.size(); i++) {
                DmpAttachmentEntity entity = new DmpAttachmentEntity();
                entity.setAttachName(attachNameList.get(i));
                entity.setAttachUrl(attachUrlList.get(i));
                entity.setType("after_sale");
                entity.setBusinessId(afterSaleEntity.getId());
                attachmentService.save(entity);
            }
        }
        //保存小程序附件
        if (CollUtil.isNotEmpty(addDTO.getAttachmentList())) {
            for (String attachment : addDTO.getAttachmentList()) {
                DmpAttachmentEntity entity = new DmpAttachmentEntity();
                entity.setAttachName(attachment);
                entity.setAttachUrl(attachment);
                entity.setType("after_sale");
                entity.setBusinessId(afterSaleEntity.getId());
                attachmentService.save(entity);
            }
        }
        return new BaseResultDTO.AddDTO(afterSaleEntity.getId(), code);
    }

    private static Map<String, Integer> checkDetailQty(List<AfterSaleDTO.DropDownDTO> detailByPlatformCode, List<AfterSaleDetailEntity> detailList, Map<String, AfterSaleDTO.DropDownDTO> downDTOMap) {
        Map<String, Integer> platformSummary = detailByPlatformCode.stream()
                .filter(detail -> StringUtil.isNotBlank(detail.getSkuId())) // 过滤掉 skuId 为空的数据
                .collect(Collectors.groupingBy(
                        detail -> detail.getSkuId(), // 按照 skuId
                        Collectors.summingInt(AfterSaleDTO.DropDownDTO::getSkuQty) // 统计 skuQty 的总和
                ));

        Map<String, Integer> skuQtySummary = detailList.stream()
                .filter(detail -> StringUtil.isNotBlank(detail.getSkuId())) // 过滤掉 skuId 为空的数据
                .collect(Collectors.groupingBy(
                        detail -> detail.getSkuId(), // 按照 skuId
                        Collectors.summingInt(AfterSaleDetailEntity::getSkuQty) // 统计 skuQty 的总和
                ));
        for (Map.Entry<String, Integer> entry : skuQtySummary.entrySet()) {
            // 按照 skuId 和 price 组合成新的 key
            String key = entry.getKey();
            Integer detailQty = entry.getValue();
            if (platformSummary.containsKey(key)) {
                Integer qty = platformSummary.get(key);
                if (detailQty.compareTo(qty) > 0) {
                    throw new ServiceException("【" + downDTOMap.get(entry.getKey()).getSkuNo() + "】明细数量不能大于" + qty);
                }
            }
        }

        platformSummary.clear();

        skuQtySummary.clear();

        platformSummary = detailByPlatformCode.stream()
                .filter(detail -> StringUtil.isNotBlank(detail.getSkuId())) // 过滤掉 skuId 为空的数据
                .collect(Collectors.groupingBy(
                        detail -> detail.getSkuId() + ":" + detail.getPrice().setScale(4), // 按照 skuId 和 price 组合成新的 key
                        Collectors.summingInt(AfterSaleDTO.DropDownDTO::getSkuQty) // 统计 skuQty 的总和
                ));

        skuQtySummary = detailList.stream()
                .filter(detail -> StringUtil.isNotBlank(detail.getSkuId()) && Objects.nonNull(detail.getPrice())) // 过滤掉 skuId 为空的数据
                .collect(Collectors.groupingBy(
                        detail -> detail.getSkuId() + ":" + detail.getPrice().setScale(4), // 按照 skuId 和 price 组合成新的 key
                        Collectors.summingInt(AfterSaleDetailEntity::getSkuQty) // 统计 skuQty 的总和
                ));

        for (Map.Entry<String, Integer> entry : skuQtySummary.entrySet()) {
            // 按照 skuId 和 price 组合成新的 key
            String key = entry.getKey();
            Integer detailQty = entry.getValue();
            if (platformSummary.containsKey(key)) {
                Integer qty = platformSummary.get(key);
                if (detailQty.compareTo(qty) > 0) {
                    throw new ServiceException("【" + downDTOMap.get(entry.getKey().split(":")[0]).getSkuNo() + "】明细数量不能大于" + qty);
                }
            }
        }
        return platformSummary;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSaleDTO.UpdateDTO updateDTO) {
        AfterSaleEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后申请单"));
        if (!InvalidStatusEnum.NOT_VOIDED.getStatus().equals(old.getInvalidStatus())) {
            throw new ServiceException("只有未作废的单据才能进行编辑");
        } else if (AfterSaleStatusEnum.isTerminalStatus(old.getStatus())) {
            AfterSaleEntity afterSaleEntity = new AfterSaleEntity();
            BeanMapper.copy(old, afterSaleEntity);
            afterSaleEntity.setRemark(updateDTO.getRemark());
            afterSaleEntity.setUsername(updateDTO.getThridUserName());
            afterSaleEntity.setPhoneNumber(updateDTO.getPhoneNumber());
            log.info("编辑 开始修改售后申请单数据，单号：【{}】", old.getCode());
            boolean save = super.updateById(afterSaleEntity);
            if (!save) {
                throw new ServiceException("售后申请单更新失败");
            }
            // 记录主单操作日志
            log.info("编辑 开始记录售后申请单日志数据，单号：【{}】", afterSaleEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "售后申请单");
            operateLogService.addModuleOperateLogByObj(old, afterSaleEntity, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), msg);
        } else { //正常更新
            AfterSaleEntity afterSaleEntity = BeanMapperUtils.map(AfterSaleEntity.class, updateDTO);
            //更新用户信息
            if (StringUtils.isNotBlank(afterSaleEntity.getThridUserId())) {
                ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
                thridUserInfoEntity.setId(afterSaleEntity.getThridUserId());
                thridUserInfoEntity.setUsername(updateDTO.getThridUserName());
                thridUserInfoEntity.setPhoneNumber(updateDTO.getPhoneNumber());
                thridUserInfoService.updateById(thridUserInfoEntity);
            }

            afterSaleEntity.setUsername(updateDTO.getThridUserName());
            afterSaleEntity.setPhoneNumber(updateDTO.getPhoneNumber());

            //新增明细
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<AfterSaleDetailEntity> oldDetailList = afterSaleDetailService.lambdaQuery().eq(AfterSaleDetailEntity::getMainId, afterSaleEntity.getId()).list();
            List<AfterSaleDetailEntity> detailList = BeanMapper.copyList(updateDTO.getDetailList(), AfterSaleDetailEntity.class);
            String platformCode = updateDTO.getPlatformCode();
            if (CollUtil.isNotEmpty(detailList)) {
                totalAmount = getBigDecimal(detailList, platformCode, afterSaleEntity, totalAmount);

                //删除明细
                List<String> ids = detailList.stream().map(AfterSaleDetailEntity::getId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
                List<AfterSaleDetailEntity> removeList = oldDetailList.stream().filter(item -> !ids.contains(item.getId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(removeList)) {
                    String skuNos = removeList.stream()
                            .map(AfterSaleDetailEntity::getSkuNo) // 提取每个对象的 skuNo
                            .filter(StringUtil::isNotBlank) // 过滤掉可能为 null 的值
                            .collect(Collectors.joining(",")); // 使用逗号拼接字符串
                    String msg = StrUtil.format("用户【{}】删除明细【{}】 ", UserContext.getDefaultLoginUser().getUserName(), skuNos);
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
                }
                //新增明细
                List<AfterSaleDetailEntity> newList = detailList.stream().filter(item -> StringUtil.isBlank(item.getId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(newList)) {
                    String skuNos = newList.stream()
                            .map(AfterSaleDetailEntity::getSkuNo) // 提取每个对象的 skuNo
                            .filter(StringUtil::isNotBlank) // 过滤掉可能为 null 的值
                            .collect(Collectors.joining(",")); // 使用逗号拼接字符串
                    String msg = StrUtil.format("用户【{}】新增明细【{}】 ", UserContext.getDefaultLoginUser().getUserName(), skuNos);
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
                }
                //编辑明细
                List<AfterSaleDetailEntity> updateList = detailList.stream().filter(item -> StringUtil.isNotBlank(item.getId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(updateList)) {
                    String msg = StrUtil.format("用户【{}】编辑明细【{}】 ", UserContext.getDefaultLoginUser().getUserName());
                    for (AfterSaleDetailEntity afterSaleDetailEntity : updateList) {
                        AfterSaleDetailEntity oldDetail = oldDetailList.stream().filter(item -> item.getId().equals(afterSaleDetailEntity.getId())).findFirst().orElse(null);
                        if (Objects.nonNull(oldDetail)) {
                            operateLogService.addModuleOperateLogByObj(oldDetail, afterSaleDetailEntity, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), msg);
                        }
                    }
                }
                if (CollUtil.isNotEmpty(ids)) {
                    afterSaleDetailService.lambdaUpdate().eq(AfterSaleDetailEntity::getMainId, updateDTO.getId()).notIn(AfterSaleDetailEntity::getId, ids).remove();
                } else {
                    afterSaleDetailService.lambdaUpdate().eq(AfterSaleDetailEntity::getMainId, updateDTO.getId()).remove();
                }
                afterSaleDetailService.saveOrUpdateBatch(detailList);
            }

            if (Objects.isNull(afterSaleEntity.getTotalPrice())) {
                afterSaleEntity.setTotalPrice(totalAmount);
            }

            if (Objects.isNull(afterSaleEntity.getTotalRepairAmount())) {
                afterSaleEntity.setTotalRepairAmount(BigDecimal.ZERO);
            }

            // 删除明细数据
            attachmentService.lambdaUpdate().eq(DmpAttachmentEntity::getType, "after_sale").eq(DmpAttachmentEntity::getBusinessId, updateDTO.getId()).remove();
            //保存web附件
            if (CollUtil.isNotEmpty(updateDTO.getAttachNameList()) && CollUtil.isNotEmpty(updateDTO.getAttachUrlList())) {
                List<String> attachUrlList = updateDTO.getAttachUrlList();
                List<String> attachNameList = updateDTO.getAttachNameList();
                for (int i = 0; i < attachUrlList.size(); i++) {
                    DmpAttachmentEntity entity = new DmpAttachmentEntity();
                    entity.setAttachName(attachNameList.get(i));
                    entity.setAttachUrl(attachUrlList.get(i));
                    entity.setType("after_sale");
                    entity.setBusinessId(afterSaleEntity.getId());
                    attachmentService.save(entity);
                }
            }
            //保存小程序附件
            if (CollUtil.isNotEmpty(updateDTO.getAttachmentList())) {
                for (String attachment : updateDTO.getAttachmentList()) {
                    DmpAttachmentEntity entity = new DmpAttachmentEntity();
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
                if (afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())) {//客户寄件
                    if (!afterSaleProgressEntity.getTrackNo().equals(updateDTO.getReturnTrackNo())) {
                        String msg = StrUtil.format("用户【{}】编辑买家寄出快递单号由[{}]变更为[{}]  ", UserContext.getDefaultLoginUser().getUserName(), afterSaleProgressEntity.getTrackNo(), updateDTO.getReturnTrackNo());
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
                    }
                    afterSaleProgressEntity.setTrackNo(updateDTO.getReturnTrackNo());
                    afterSaleProgressEntity.setNodeTime(LocalDateTime.now());

                }
                if (afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())) {//售后发货
                    if (!afterSaleProgressEntity.getTrackNo().equals(updateDTO.getOutboundTrackNo())) {
                        String msg = StrUtil.format("用户【{}】编辑商家寄出快递单号由[{}]变更为[{}] ", UserContext.getDefaultLoginUser().getUserName(), afterSaleProgressEntity.getTrackNo(), updateDTO.getOutboundTrackNo());
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
                    }
                    afterSaleProgressEntity.setTrackNo(updateDTO.getOutboundTrackNo());
                    afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
                    afterSaleEntity.setType(OutboundTrackNoTypeEnum.MANUAL.getCode());
                }
                afterSaleProgressService.updateById(afterSaleProgressEntity);
            }

            log.info("编辑 开始修改售后申请单数据，单号：【{}】", old.getCode());
            boolean save = super.updateById(afterSaleEntity);
            if (!save) {
                throw new ServiceException("售后申请单保存失败");
            }
            // 记录主单操作日志
            log.info("编辑 开始记录售后申请单日志数据，单号：【{}】", old.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "售后申请单");
            operateLogService.addModuleOperateLogByObj(old, afterSaleEntity, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), msg);
        }
        return Boolean.TRUE;
    }

    private BigDecimal getBigDecimal(List<AfterSaleDetailEntity> detailList, String platformCode, AfterSaleEntity afterSaleEntity, BigDecimal totalAmount) {
        List<String> skuIds = detailList.stream().map(AfterSaleDetailEntity::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, t -> t));
        List<AfterSaleDTO.DropDownDTO> detailByPlatformCode = getDetailByPlatformCode(platformCode);
        if (CollUtil.isNotEmpty(detailByPlatformCode)) {
            Map<String, AfterSaleDTO.DropDownDTO> downDTOMap = detailByPlatformCode.stream().collect(Collectors.toMap(AfterSaleDTO.DropDownDTO::getSkuId, t -> t, (k1, k2) -> k1));
            checkDetailQty(detailByPlatformCode, detailList, downDTOMap);
            for (AfterSaleDetailEntity detail : detailList) {
                detail.setMainId(afterSaleEntity.getId());
                ProductDetailEntity productDetail = productDetailMap.getOrDefault(detail.getSkuId(), null);
                if(Objects.nonNull(productDetail)){
                    detail.setSkuNo(productDetail.getSkuNo());
                    detail.setProductName(productDetail.getName());
                    if (Objects.isNull(detail.getPrice()) && downDTOMap.containsKey(detail.getSkuId())) {
                        AfterSaleDTO.DropDownDTO downDTO = downDTOMap.get(detail.getSkuId());
                        detail.setPrice(downDTO.getPrice());
                    }
                    if (Objects.nonNull(detail.getPrice())) {
                        //计算总货值
                        totalAmount = totalAmount.add(detail.getPrice().multiply(new BigDecimal(detail.getSkuQty())));
                    }
                }
            }
        } else {
            for (AfterSaleDetailEntity detail : detailList) {
                detail.setMainId(afterSaleEntity.getId());
                ProductDetailEntity productDetail = productDetailMap.getOrDefault(detail.getSkuId(), null);
                if(Objects.nonNull(productDetail)){
                    detail.setSkuNo(productDetail.getSkuNo());
                    detail.setProductName(productDetail.getName());
                    if (Objects.nonNull(detail.getPrice())) {
                        //计算总货值
                        totalAmount = totalAmount.add(detail.getPrice().multiply(new BigDecimal(detail.getSkuQty())));
                    }
                }
            }
        }
        return totalAmount;
    }


    @Override
    public PagingVO<AfterSaleDTO.ListDTO> paging(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AfterSaleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<DmpAfterSaleExcelDTO> exportList(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpAfterSaleExcelDTO> pageData = this.baseMapper.listExport(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        Map<String, String> dictPlatformNameMap = loadDictPlatformNameMap();
        // 获取商家寄出快递单号
        List<String> outboundTrackNoList = pageData.getRecords().stream().map(DmpAfterSaleExcelDTO::getOutboundTrackNo).collect(Collectors.toList());
        Map<String, LogisticsOrderDTO.ListDTO> listDTOMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(outboundTrackNoList)) {
            List<LogisticsOrderDTO.ListDTO> list = logisticsOrderFeign.getLogisticsOrderListByTrackNo(outboundTrackNoList);
            listDTOMap = list.stream().collect(Collectors.toMap(LogisticsOrderDTO.ListDTO::getTrackNo, item -> item));
        }
        // 属性赋值
        for (DmpAfterSaleExcelDTO data : pageData.getRecords()) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //单据状态
            data.setStatusName(AfterSaleStatusEnum.getNode(data.getStatus()));
            fillDictPlatformName(data.getDictPlatform(), dictPlatformNameMap, data::setDictPlatformName);
            if (listDTOMap.containsKey(data.getOutboundTrackNo())) {
                data.setLogisticsChannelName(listDTOMap.get(data.getOutboundTrackNo()).getLogisticsChannelName());
            }
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
        statusList.forEach(status -> {
            if (!existStatusList.contains(status) && !ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(status)) {
                list.add(new AfterSaleDTO.TabListDTO(status, ApproveStatusEnum.getName(status), 0));
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
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "id", unlockAfterTx = true)
    public BatchResultDTO submit(String id) {
        AfterSaleEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后申请");
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AfterSaleDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AfterSaleDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "dto.id", unlockAfterTx = true)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        AfterSaleEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }
        // 审核中的数据允许审核
        if (Objects.equals(entity.getInvalidStatus(), InvalidStatusEnum.VOIDED.getStatus())) {
            throw new ServiceException("已作废数据不支持审核");
        }

        // 调用流程审核
        entity.setRemark(dto.getComment());
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
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
        Map<String, Object> map = buildVariablesMap(entity);
        approveDTO.setVariablesMap(map);
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
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
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "id", unlockAfterTx = true)
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
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
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
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "id", unlockAfterTx = true)
    public BatchResultDTO invalid(String id, String remark) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        if (AfterSaleStatusEnum.isTerminalStatus(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "只有未完成、未中止、未作废的数据支持作废");
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
        afterSaleProgressEntity.setRemark(remark);
        afterSaleProgressService.updateById(afterSaleProgressEntity);

        //发送微信订阅消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendAfterSaleCancelMsgRequest(entity, AfterSaleStatusEnum.TERMINATED.getCode(), remark);
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
        if (StringUtils.isBlank(code)) {
            throw new ServiceException("工单号不能为空");
        }
        AfterSaleEntity afterSaleEntity = lambdaQuery().eq(AfterSaleEntity::getCode, code).one();
        if (Objects.isNull(afterSaleEntity)) {
            throw new ServiceException("售后申请单不存在");
        }

        if (AfterSaleStatusEnum.isTerminalStatus(afterSaleEntity.getStatus())) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "只有未完成、未中止的数据支持作废");
        }
        String remark = "用户主动取消申请单";
        afterSaleEntity.setInvalidStatus(InvalidStatusEnum.VOIDED.getStatus());
        afterSaleEntity.setInvalidTime(LocalDateTime.now());
        afterSaleEntity.setStatus(AfterSaleStatusEnum.TERMINATED.getCode());
        afterSaleEntity.setRemark(remark);
        updateById(afterSaleEntity);

        //更新节点时间
        //更新通过，则进入下一个节点：终止
        AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        afterSaleProgressEntity.setNode(AfterSaleStatusEnum.TERMINATED.getCode());
        afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
        afterSaleProgressEntity.setRemark(remark);
        afterSaleProgressService.updateById(afterSaleProgressEntity);

        operateLogService.addModuleOperateLog(remark, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "作废操作");

        //发送微信订阅消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendAfterSaleCancelMsgRequest(afterSaleEntity, AfterSaleStatusEnum.TERMINATED.getCode(), remark);
            }
        });

        return BatchResultDTO.success(afterSaleEntity.getId(), afterSaleEntity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public Map<String, String> listCsAgent(AfterSaleDTO.ListCsAgentDTO dto) {
        Map<String, String> resultMap = new HashMap<>();
        List<String> afterSaleIds = dto.getAfterSaleIds();
        if (!afterSaleIds.isEmpty()) {
            List<AfterSaleEntity> afterSaleList = this.listByIds(afterSaleIds);
            for (AfterSaleEntity afterSaleEntity : afterSaleList) {
                if (StringUtils.isNotBlank(afterSaleEntity.getCsAgentId())) {
                    resultMap.put("csAgent",afterSaleEntity.getCsAgentId());
                }
            }
        }
        return resultMap;
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "dto.id", unlockAfterTx = true)
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后申请"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        log.info("撤销 开始修改售后申请单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "取消流程操作");

        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "dto.id", unlockAfterTx = true)
    public Boolean approveEnd(ApproveOneDTO dto, AfterSaleEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        if (approveStatus.equals(ApproveStatusEnum.REJECT)) {

            entity.setStatus(AfterSaleStatusEnum.TERMINATED.getCode());

            //审批不通过，则最后的节点为终止
            AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(entity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
            afterSaleProgressEntity.setNode(AfterSaleStatusEnum.TERMINATED.getCode());
            afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            afterSaleProgressEntity.setRemark(entity.getRemark());
            afterSaleProgressService.updateById(afterSaleProgressEntity);

            //发送微信订阅消息
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                    bean.sendAfterSaleCancelMsgRequest(entity, AfterSaleStatusEnum.TERMINATED.getCode(), entity.getRemark());
                }
            });
        } else {

            entity.setStatus(AfterSaleStatusEnum.TO_BE_RETURNED.getCode());
            //更新节点时间
            //更新通过，则进入下一个节点：待寄回
            updateProgressByMainId(entity.getId(), AfterSaleStatusEnum.TO_BE_RETURNED.getCode(), "", entity.getRemark());
            //发送微信订阅消息
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                    bean.sendAfterSaleApproveMsgRequest(entity, AfterSaleStatusEnum.TO_BE_RETURNED.getCode());
                }
            });
        }
        //更新单据状态
        updateById(entity);
        return updateForApprove(entity.getId(), approveStatus.getStatus());
    }

    //更新节点时间
    private void updateProgressByMainId(String id, String node, String logisticsCode, String remark) {
        //更新单据状态
        AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(id, node);
        if (Objects.nonNull(afterSaleProgressEntity)) {
            afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            if (StringUtils.isNotBlank(logisticsCode)) {
                afterSaleProgressEntity.setTrackNo(logisticsCode);
            }
            if (StringUtils.isNotBlank(remark)) {
                afterSaleProgressEntity.setRemark(remark);
            }
            afterSaleProgressService.updateById(afterSaleProgressEntity);
        }
    }

    @Override
    public AfterSaleDTO.ViewDTO view(String id) {
        AfterSaleEntity afterSaleEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        AfterSaleDTO.ViewDTO data = BeanMapperUtils.map(AfterSaleDTO.ViewDTO.class, afterSaleEntity);
        data.setPhoneNumber(afterSaleEntity.getPhoneNumber());
        data.setThridUserName(afterSaleEntity.getUsername());
        // 数据填充处理
        fillOne(data);
        //查询用户信息
//        ThridUserInfoEntity thridUserInfoEntity = thridUserInfoService.getById(afterSaleEntity.getThridUserId());
//        if(Objects.nonNull(thridUserInfoEntity)){
//            data.setNickName(thridUserInfoEntity.getNickName());
//            data.setPhoneNumber(thridUserInfoEntity.getPhoneNumber());
//            data.setThridUserName(thridUserInfoEntity.getUsername());
//        }

        //查询明细
        List<AfterSaleDetailEntity> detailList = afterSaleDetailService.listByMainIds(Collections.singletonList(id));
        data.setDetailList(detailList);
        //查询附件
        List<DmpAttachmentEntity> attachmentList = attachmentService.lambdaQuery().eq(DmpAttachmentEntity::getBusinessId, id).eq(DmpAttachmentEntity::getType, "after_sale").list();
        if (CollUtil.isNotEmpty(attachmentList)) {
            List<String> attachNameList = new ArrayList<>();
            List<String> attachUrlList = new ArrayList<>();

            for (DmpAttachmentEntity dto : attachmentList) {
                attachNameList.add(dto.getAttachName());
                attachUrlList.add(dto.getAttachUrl());
            }
            data.setAttachNameList(attachNameList);
            data.setAttachUrlList(attachUrlList);
        }
        //查询进度
        List<AfterSaleProgressEntity> afterSaleProgressList = afterSaleProgressService.listByMainIds(Collections.singletonList(id));
        for (AfterSaleProgressEntity afterSaleProgressEntity : afterSaleProgressList) {
            if (afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())) {//客户寄件
                data.setReturnTrackNo(afterSaleProgressEntity.getTrackNo());
            }
            if (afterSaleProgressEntity.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())) {//售后发货
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
        Map<String, Object> map = buildVariablesMap(entity);
        startDTO.setVariablesMap(map);
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private Map<String, Object> buildVariablesMap(AfterSaleEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        AfterSaleDTO.ViewDTO viewDTO = this.view((entity.getId()));
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.AFTER_SALE.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(viewDTO));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        map.put("detailList", viewDTO.getDetailList());

        Map<String,String> progresstMap = new HashMap<>();
        if (StringUtils.isNotBlank(viewDTO.getOutboundTrackNo())) {
            progresstMap.put("progress",viewDTO.getOutboundTrackNo());
        }

        if(!progresstMap.isEmpty()){
            map.put("progress", progresstMap);
        }
        return map;
    }

    private void fillOne(AfterSaleDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus().getCode()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        //单据状态
        data.setStatusName(AfterSaleStatusEnum.getNode(data.getStatus()));
        if (StringUtils.isNotBlank(data.getDictPlatform())) {
            ApiResult<List<BaseDropDownDTO.CommonDTO>> listApiResult = omsDropDownFeign.listInternalSalesPlatform(DictBasicTypeEnum.MINI_PROGRAM_SALES_PLATFORM_INTERNAL.getType());
            BaseDropDownDTO.CommonDTO commonDTO = listApiResult.getData().stream().filter(e -> e.getCode().equals(data.getDictPlatform())).findFirst().orElse(null);
            if (Objects.nonNull(commonDTO)) {
                data.setDictPlatformName(commonDTO.getValue());
            }
        }

        if (!data.getApproveStatus().equals(ApproveStatusEnum.APPROVE)) {
            data.setApproveTime(null);
        }
    }

    /**
     * 审核更新审核信息
     *
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
     *
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
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> dictPlatformNameMap = loadDictPlatformNameMap();

        List<String> shopIdList = list.stream().map(item -> item.getShopId()).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(shopIdList);
        // 获取商家寄出快递单号
        List<String> outboundTrackNoList = list.stream().map(AfterSaleDTO.ListDTO::getOutboundTrackNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<LogisticsOrderDTO.ListDTO> dtoList = logisticsOrderFeign.getLogisticsOrderListByTrackNo(outboundTrackNoList);
        Map<String, LogisticsOrderDTO.ListDTO> listDTOMap = dtoList.stream().collect(Collectors.toMap(LogisticsOrderDTO.ListDTO::getTrackNo, Function.identity(), (v1, v2) -> v1));
        // 查询附件信息
        List<String> idList = list.stream().map(AfterSaleDTO.ListDTO::getId).collect(Collectors.toList());
        List<DmpAttachmentEntity> attachmentList = attachmentService.lambdaQuery().in(DmpAttachmentEntity::getBusinessId, idList).eq(DmpAttachmentEntity::getType, DmpConstant.AFTER_SALE_LABEL).list();
        Map<String, DmpAttachmentEntity> attachmentMap = attachmentList.stream().collect(Collectors.toMap(DmpAttachmentEntity::getBusinessId, Function.identity(), (v1, v2) -> v1));
        // 查询国家信息
        List<String> countryIdList = dtoList.stream().map(LogisticsOrderDTO.ListDTO::getCountry).collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);
        Map<String, DictCountryEntity> countryMap = countryList.stream().collect(Collectors.toMap(DictCountryEntity::getId, Function.identity(), (v1, v2) -> v1));
        // 属性赋值
        for (AfterSaleDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //单据状态
            data.setStatusName(AfterSaleStatusEnum.getNode(data.getStatus()));
            fillDictPlatformName(data.getDictPlatform(), dictPlatformNameMap, data::setDictPlatformName);
            if (!shopInfoList.isEmpty()) {
                if (StringUtils.isNotBlank(data.getShopId())) {
                    ShopInfoEntity shopInfoEntity = shopInfoList.stream()
                            .filter(item -> Objects.equals(item.getId(), data.getShopId()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.nonNull(shopInfoEntity)) {
                        data.setShopName(shopInfoEntity.getName());
                    }
                }
            }
            if (attachmentMap.get(data.getId()) != null) {
                data.setLabelStatus(LogisticsLabelStatusEnum.OBTAINED.getCode());
                data.setLabelStatusName(LogisticsLabelStatusEnum.OBTAINED.getName());
                data.setAttachment(attachmentMap.get(data.getId()));
            } else {
                data.setLabelStatus(LogisticsLabelStatusEnum.NOT_OBTAINED.getCode());
                data.setLabelStatusName(LogisticsLabelStatusEnum.NOT_OBTAINED.getName());
            }
            fillOutboundTrackStatusName(data);
            if (Objects.nonNull(listDTOMap.get(data.getOutboundTrackNo()))) {
                data.setLogisticsChannelId(listDTOMap.get(data.getOutboundTrackNo()).getLogisticsChannelId());
                data.setLogisticsChannelName(listDTOMap.get(data.getOutboundTrackNo()).getLogisticsChannelName());
                data.setCountry(listDTOMap.get(data.getOutboundTrackNo()).getCountry());
                data.setCountryName(countryMap.get(data.getCountry()).getNameCn());
                data.setProvince(listDTOMap.get(data.getOutboundTrackNo()).getProvince());
                data.setCity(listDTOMap.get(data.getOutboundTrackNo()).getCity());
                data.setDistrict(listDTOMap.get(data.getOutboundTrackNo()).getDistrict());
                data.setDetailedAddress(listDTOMap.get(data.getOutboundTrackNo()).getDetailedAddress());
                data.setLabelStatus(listDTOMap.get(data.getOutboundTrackNo()).getLabelStatus());
                data.setLabelStatusName(listDTOMap.get(data.getOutboundTrackNo()).getLabelStatusName());
            }
        }
    }

    private void fillOutboundTrackStatusName(AfterSaleDTO.ListDTO data) {
        if (StringUtils.isBlank(data.getOutboundTrackStatus())) {
            return;
        }
        data.setOutboundTrackStatusName(LogisticTrackStatusEnum.getName(data.getOutboundTrackStatus()));
    }

    private Map<String, String> loadDictPlatformNameMap() {
        ApiResult<List<BaseDropDownDTO.CommonDTO>> listApiResult = omsDropDownFeign.listInternalSalesPlatform(
                DictBasicTypeEnum.MINI_PROGRAM_SALES_PLATFORM_INTERNAL.getType());
        return loadDictPlatformNameMap(listApiResult);
    }

    private Map<String, String> loadDictPlatformNameMap(ApiResult<List<BaseDropDownDTO.CommonDTO>> listApiResult) {
        if (listApiResult == null || CollUtil.isEmpty(listApiResult.getData())) {
            return Collections.emptyMap();
        }
        return listApiResult.getData().stream()
                .filter(item -> StringUtils.isNotBlank(item.getCode()))
                .collect(Collectors.toMap(BaseDropDownDTO.CommonDTO::getCode, BaseDropDownDTO.CommonDTO::getValue, (v1, v2) -> v1));
    }

    private void fillDictPlatformName(String dictPlatform, Map<String, String> dictPlatformNameMap,
                                      java.util.function.Consumer<String> nameSetter) {
        if (StringUtils.isBlank(dictPlatform) || dictPlatformNameMap.isEmpty()) {
            return;
        }
        String dictPlatformName = dictPlatformNameMap.get(dictPlatform);
        if (StringUtils.isNotBlank(dictPlatformName)) {
            nameSetter.accept(dictPlatformName);
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(AfterSaleEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }


    /**
     * 获取可手动变更的节点配置（供编辑状态下拉、OpenAPI/Feign 调用；非完整进度节点列表）。
     * 排除 completed：仅物流下单成功后自动进入已完成。
     * 排除 terminated：终态，须通过作废/取消/审核驳回等专用入口流转，不支持 PC 手动 changeStatus。
     *
     * @return 可选手动变更的节点列表
     */
    @Override
    public List<AfterSaleDTO.NodeDTO> getNodeList() {
        return loadAllNodeList().stream()
                // 已完成：禁止 PC 手动改状态，须走 logisticsOrder
                .filter(node -> !AfterSaleStatusEnum.COMPLETED.getCode().equals(node.getNode())
                        // 已终止：终态，禁止 PC 手动 changeStatus
                        && !AfterSaleStatusEnum.TERMINATED.getCode().equals(node.getNode()))
                .collect(Collectors.toList());
    }

    /**
     * 加载 cfg_setting 中的完整售后维修节点配置（含已完成、已终止，供进度条与物流下单等内部逻辑使用）。
     *
     * @return 完整节点列表
     */
    private List<AfterSaleDTO.NodeDTO> loadAllNodeList() {
        String value = cfgSettingService.getValue(SettingEnum.AFTER_SALSE_NODE);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("售后维修节点配置不存在");
        }
        // 解析 JSON 字符串为 List<AfterSaleDTO.NodeDTO>
        return JSONUtil.toList(JSONUtil.parseObj(value).getJSONArray("nodeList"), AfterSaleDTO.NodeDTO.class);
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

        if (StringUtil.isBlank(dto.getNode())) {
            throw new ServiceException("请选择单据状态");
        }

        AfterSaleStatusEnum afterSaleStatus = AfterSaleStatusEnum.getByCode(dto.getNode());
        if (Objects.isNull(afterSaleStatus)) {
            throw new ServiceException("未找到单据状态信息");
        }
        // 终态禁止 PC 手动 changeStatus（与 getNodeList 下拉过滤一致，直连 API 亦拦截）
        if (Objects.equals(afterSaleStatus, AfterSaleStatusEnum.COMPLETED)) {
            throw new ServiceException("已完成状态不支持手动修改，请通过物流下单完成");
        }
        if (Objects.equals(afterSaleStatus, AfterSaleStatusEnum.TERMINATED)) {
            throw new ServiceException("已终止状态不支持手动修改");
        }

        // 节点 index 校验需完整配置，故用 loadAllNodeList，不能用对外过滤后的 getNodeList
        Map<String, AfterSaleDTO.NodeDTO> nodeMap = loadAllNodeList().stream().collect(Collectors.toMap(AfterSaleDTO.NodeDTO::getNode, w -> w));
        AfterSaleDTO.NodeDTO newNodeDTO = nodeMap.get(dto.getNode());
        if (Objects.isNull(newNodeDTO)) {
            throw new ServiceException("目标节点配置不存在，请检查售后维修节点配置");
        }

        for (AfterSaleEntity entity : entityList) {
            BatchResultDTO batchResultDTO;
            if (!ApproveStatusEnum.APPROVE.getCode().equals(entity.getApproveStatus().getCode())
                    || AfterSaleStatusEnum.isTerminalStatus(entity.getStatus())) {
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "只有审核通过并且单据状态未完成、未中止数据支持状态修改");
            } else {
                AfterSaleDTO.NodeDTO oldNodeDTO = nodeMap.get(entity.getStatus());
                if (Objects.isNull(oldNodeDTO)) {
                    batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "当前进度节点配置异常，请联系管理员");
                } else if (oldNodeDTO.getIndex() > newNodeDTO.getIndex()) {
                    batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "当前节点不能小于等于原节点");
                } else {
                    AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressService.getByNode(entity.getId(), dto.getNode());
                    if (Objects.isNull(afterSaleProgressEntity)) {
                        batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "进度节点不存在，请联系管理员");
                        resultList.add(batchResultDTO);
                        continue;
                    }
                    AfterSaleProgressEntity receivedProgressEntity = null;
                    if (StringUtils.isNotBlank(dto.getTrackNo())
                            && afterSaleStatus.getCode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())) {
                        receivedProgressEntity = afterSaleProgressService.getByNode(entity.getId(), AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode());
                        if (Objects.isNull(receivedProgressEntity)) {
                            batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "待售后签收进度节点不存在，请联系管理员");
                            resultList.add(batchResultDTO);
                            continue;
                        }
                    }

                    batchResultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);

                    //更新状态
                    entity.setStatus(dto.getNode());
                    if (Objects.equals(afterSaleStatus, AfterSaleStatusEnum.TO_BE_SHIPPED) && StringUtils.isNotBlank(dto.getRmaRemark())) {
                        entity.setRmaRemark(dto.getRmaRemark());
                        afterSaleProgressEntity.setRemark(dto.getRmaRemark());
                    } else {
                        afterSaleProgressEntity.setRemark(dto.getRemark());
                    }

                    afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
                    if (StringUtils.isNotBlank(dto.getTrackNo())) {
                        //客户寄件
                        if (Objects.equals(afterSaleStatus, AfterSaleStatusEnum.TO_BE_RETURNED)) {
                            if (!Objects.equals(afterSaleProgressEntity.getTrackNo(), dto.getTrackNo())) {
                                String msg = StrUtil.format("用户【{}】编辑买家寄出快递单号由[{}]变更为[{}]  ", UserContext.getDefaultLoginUser().getUserName(), afterSaleProgressEntity.getTrackNo(), dto.getTrackNo());
                                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "编辑信息");
                            }
                            afterSaleProgressEntity.setTrackNo(dto.getTrackNo());
                        }
                    }

                    //erp状态变更客户寄件，输入快递单号后，需要流转至待售后签收（待签收）
                    if (StringUtils.isNotBlank(dto.getTrackNo()) && afterSaleStatus.getCode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())) {
                        receivedProgressEntity.setNodeTime(LocalDateTime.now());
                        receivedProgressEntity.setRemark("待签收");
                        afterSaleProgressService.updateById(receivedProgressEntity);
                        //更新状态
                        entity.setStatus(AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode());
                    }
                    afterSaleProgressService.updateById(afterSaleProgressEntity);
                    //更新
                    updateById(entity);
                    //日志
                    AfterSaleDTO.NodeDTO updatedNodeDTO = nodeMap.get(entity.getStatus());
                    String updatedNodeName = updatedNodeDTO != null ? updatedNodeDTO.getNodeName() : AfterSaleStatusEnum.getNode(entity.getStatus());
                    String msg = StrUtil.format("用户【{}】更新状态由[{}]变更为[{}]  ", UserContext.getDefaultLoginUser().getUserName(), oldNodeDTO.getNodeName(), updatedNodeName);
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "编辑信息");
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
        AfterSaleProgressDTO.RepairRecordListDTO shippedProgress = repairProgress.stream()
                .filter(t -> AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(t.getNode()))
                .findFirst().orElse(null);
        repairProgress.forEach(t -> {
            t.setNodeName(AfterSaleStatusEnum.getNode(t.getNode()));
            if (Objects.nonNull(t.getNodeTimeLd())) {
                t.setNodeTime(LocalDateTimeUtil.format(t.getNodeTimeLd(), DateUtil.fmt));
            }
            if (t.getNode().equals(AfterSaleStatusEnum.TO_BE_RETURNED.getCode())) {
                if (StringUtils.isNotBlank(t.getTrackNo())) {
                    if (StringUtils.isBlank(t.getRemark())) {
                        t.setRemark("买家寄出快递单号：" + t.getTrackNo());
                    } else {
                        t.setRemark("买家寄出快递单号：" + t.getTrackNo() + "\n" + t.getRemark());
                    }
                }
            }
            if (t.getNode().equals(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                    || t.getNode().equals(AfterSaleStatusEnum.COMPLETED.getCode())) {
                String outboundTrackNo = StringUtils.isNotBlank(t.getTrackNo()) ? t.getTrackNo()
                        : (Objects.nonNull(shippedProgress) ? shippedProgress.getTrackNo() : null);
                if (StringUtils.isNotBlank(outboundTrackNo)) {
                    if (StringUtils.isBlank(t.getRemark())) {
                        t.setRemark("商家寄出快递单号：" + outboundTrackNo);
                    } else {
                        t.setRemark("商家寄出快递单号：" + outboundTrackNo + "\n" + t.getRemark());
                    }
                }
            }
        });
        List<AfterSaleDTO.NodeDTO> nodeList = loadAllNodeList();
        if (AfterSaleStatusEnum.TERMINATED.getCode().equals(repairProgress.get(0).getStatus())) {
            repairRecordDTO.setActive(nodeList.size() - 1);
        } else {
            AfterSaleDTO.NodeDTO nodeDTO = nodeList.stream().filter(e -> e.getNode().equals(repairProgress.get(0).getStatus())).findFirst().orElse(null);
            repairRecordDTO.setActive(nodeDTO.getIndex() - 1);
        }
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
    public WxJscodeToSessionResponse jsCode2SessionInfo(String jsCode) {
        WxJscodeToSessionResponse wxJscodeToSessionResponse = wxMiniAppService.jsCode2SessionInfo(jsCode);
        return wxJscodeToSessionResponse;
    }


    @Override
    public void syncWdtToAfterSale() {
        List<AfterSaleEntity> list = lambdaQuery().eq(AfterSaleEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .ne(AfterSaleEntity::getStatus, AfterSaleStatusEnum.COMPLETED.getCode())
                .ne(AfterSaleEntity::getStatus, AfterSaleStatusEnum.TERMINATED.getCode())
                .ne(AfterSaleEntity::getRepairInvoiceCode, "")
                .list();
        if (CollUtil.isNotEmpty(list)) {
            List<String> repairInvoiceCodeList = list.stream().map(AfterSaleEntity::getRepairInvoiceCode).collect(Collectors.toList());
            List<DmpSoOutstockEntity> dmpSoOutstockList = dmpSoOutstockService.lambdaQuery()
                    .eq(DmpSoOutstockEntity::getStatus, "1")
                    .in(DmpSoOutstockEntity::getThirdBillNo, repairInvoiceCodeList).list();
            if (CollUtil.isNotEmpty(dmpSoOutstockList)) {
                Map<String, DmpSoOutstockEntity> map = dmpSoOutstockList.stream().collect(Collectors.toMap(DmpSoOutstockEntity::getThirdBillNo, t -> t, (k1, k2) -> k1));
                for (AfterSaleEntity afterSaleEntity : list) {
                    String repairInvoiceCode = afterSaleEntity.getRepairInvoiceCode();
                    if (map.containsKey(repairInvoiceCode) && StringUtils.isNotBlank(map.get(repairInvoiceCode).getLogisticsCode())) {
                        String logisticsCode = map.get(repairInvoiceCode).getLogisticsCode();
                        ApplicationContextUtils.getBean(AfterSaleServiceImpl.class)
                                .syncWdtOutstockToAfterSale(afterSaleEntity, logisticsCode);
                        sendSubscribeMsgRequest(afterSaleEntity, AfterSaleStatusEnum.COMPLETED.getCode());
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncWdtOutstockToAfterSale(AfterSaleEntity afterSaleEntity, String logisticsCode) {
        updateProgressByMainId(afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode(), logisticsCode, "");
        updateProgressByMainId(afterSaleEntity.getId(), AfterSaleStatusEnum.COMPLETED.getCode(), "", "");
        afterSaleEntity.setStatus(AfterSaleStatusEnum.COMPLETED.getCode());
        updateById(afterSaleEntity);
        registerAfterSaleTrackAfterCommit(afterSaleEntity, logisticsCode, true);
    }

    @Override
    public void syncAfterSaleTrackStatus() {
        List<AfterSaleProgressEntity> progressList = afterSaleProgressService.lambdaQuery()
                .eq(AfterSaleProgressEntity::getNode, AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                .ne(AfterSaleProgressEntity::getTrackNo, "")
                .list();
        if (CollUtil.isEmpty(progressList)) {
            return;
        }
        List<String> mainIds = progressList.stream().map(AfterSaleProgressEntity::getMainId).distinct().collect(Collectors.toList());
        Map<String, AfterSaleEntity> afterSaleMap = lambdaQuery()
                .in(AfterSaleEntity::getId, mainIds)
                .eq(AfterSaleEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .list()
                .stream()
                .collect(Collectors.toMap(AfterSaleEntity::getId, Function.identity(), (v1, v2) -> v1));
        progressList = progressList.stream()
                .filter(item -> afterSaleMap.containsKey(item.getMainId()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(progressList)) {
            return;
        }

        List<LogisticsBillDTO.LogisticsBillVo> trackQueryList = new ArrayList<>(progressList.size());
        for (AfterSaleProgressEntity progress : progressList) {
            LogisticsBillDTO.LogisticsBillVo billVo = new LogisticsBillDTO.LogisticsBillVo();
            billVo.setSourceId(progress.getMainId());
            billVo.setTrackNo(progress.getTrackNo());
            trackQueryList.add(billVo);
        }

        Map<String, String> trackStatusMap = new HashMap<>();
        int batchSize = 500;
        for (int i = 0; i < trackQueryList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, trackQueryList.size());
            List<LogisticsBillDTO.LogisticsBillVo> batch = trackQueryList.subList(i, end);
            try {
                List<LogisticsBillDTO.LogisticsBillVo> resultList = logisticsBillFeign.getTrackStatusByTrackNo(batch);
                if (CollUtil.isEmpty(resultList)) {
                    continue;
                }
                for (LogisticsBillDTO.LogisticsBillVo vo : resultList) {
                    if (StringUtils.isBlank(vo.getSourceId()) || StringUtils.isBlank(vo.getTrackNo())) {
                        continue;
                    }
                    String status = StringUtils.isBlank(vo.getTrackStatus())
                            ? LogisticTrackStatusEnum.NOT_FIND.getCode() : vo.getTrackStatus();
                    trackStatusMap.put(vo.getSourceId() + "|" + vo.getTrackNo(), status);
                }
            } catch (Exception e) {
                log.warn("寄修单物流轨迹状态同步失败，批次起始索引={}", i, e);
            }
        }

        Map<String, String> mainIdStatusMap = new HashMap<>();
        for (AfterSaleProgressEntity progress : progressList) {
            String statusKey = progress.getMainId() + "|" + progress.getTrackNo();
            String newStatus = trackStatusMap.get(statusKey);
            if (StringUtils.isNotBlank(newStatus)) {
                mainIdStatusMap.put(progress.getMainId(), newStatus);
            }
        }
        List<AfterSaleEntity> updateList = new ArrayList<>();
        for (Map.Entry<String, String> entry : mainIdStatusMap.entrySet()) {
            AfterSaleEntity afterSaleEntity = afterSaleMap.get(entry.getKey());
            if (Objects.isNull(afterSaleEntity) || Objects.equals(afterSaleEntity.getOutboundTrackStatus(), entry.getValue())) {
                continue;
            }
            afterSaleEntity.setOutboundTrackStatus(entry.getValue());
            updateList.add(afterSaleEntity);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            ApplicationContextUtils.getBean(AfterSaleServiceImpl.class).batchUpdateOutboundTrackStatus(updateList);
        }
        log.warn("寄修单物流轨迹状态同步完成，待处理={}，已更新={}", progressList.size(), updateList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateOutboundTrackStatus(List<AfterSaleEntity> updateList) {
        if (CollUtil.isEmpty(updateList)) {
            return;
        }
        updateBatchById(updateList);
    }

    /**
     * 根据平台代码获取详情信息
     * 此方法首先会根据平台代码从两个不同的服务中获取数据，然后分别对获取到的数据进行处理
     * 处理过程中，会通过不同的API调用获取更多的产品信息，并将这些信息整合到最终的结果列表中
     *
     * @param platformCode 平台代码，用于查询详情信息
     * @return 返回一个包含详情信息的列表，如果查询不到相关信息，则返回空列表
     */
    @Override
    public List<AfterSaleDTO.DropDownDTO> getDetailByPlatformCode(String platformCode) {
        // 检查平台代码是否为空，如果为空则直接返回空列表
        if (StringUtil.isEmpty(platformCode)) {
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
        if (CollUtil.isNotEmpty(wdtDropDownDTOS)) {
            // 提取并去重商品的平台SKU编号列表
            List<String> platformSkuNoList = wdtDropDownDTOS.stream().map(AfterSaleDTO.DropDownDTO::getSkuNo).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
            // 调用远程服务获取商品信息列表
            List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(platformSkuNoList);
            // 将商品信息列表转换为Map，以便后续查询
            Map<String, String> map = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId, (k1, k2) -> k1));
            // 遍历原始信息列表，更新商品SKU ID
            for (AfterSaleDTO.DropDownDTO drop : wdtDropDownDTOS) {
                String skuId = map.getOrDefault(drop.getSkuNo(), "");
                if (StringUtil.isNotBlank(skuId)) {
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
        if (CollUtil.isNotEmpty(dropDownDTOS)) {
            // 提取并去重商品的平台SKU ID列表
            List<String> platformSkuIdList = dropDownDTOS.stream().map(AfterSaleDTO.DropDownDTO::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
            // 获取第一个元素的店铺ID和平台类型
            String shopId = dropDownDTOS.get(0).getShopId();
            String platform = dropDownDTOS.get(0).getThirdType();

            // 创建查询参数对象
            ListingInfoParamDTO dto = new ListingInfoParamDTO();
            dto.setPlatform(platform);
            dto.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
            dto.setPlatformSkuIdList(platformSkuIdList);
            dto.setShopIdList(Collections.singletonList(shopId));

            // 调用远程服务获取商品信息列表
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOS = skuMappingFeign.listingInfoWithSkuMappingList(dto);
            // 如果获取到的商品信息列表不为空，则进一步处理
            if (CollUtil.isNotEmpty(listingInfoWithSkuMappingDTOS)) {
                // 将商品信息列表转换为Map，以便后续查询
                Map<String, ListingInfoWithSkuMappingDTO> map = listingInfoWithSkuMappingDTOS.stream().collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuId, t -> t, (k1, k2) -> k1));
                // 遍历原始信息列表，更新商品SKU信息
                for (AfterSaleDTO.DropDownDTO drop : dropDownDTOS) {
                    ListingInfoWithSkuMappingDTO skuMappingDTO = map.getOrDefault(drop.getSkuId(), null);
                    if (Objects.nonNull(skuMappingDTO)) {
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
                .set(AfterSaleProgressEntity::getTrackNo, trackNo).set(AfterSaleProgressEntity::getNodeTime, LocalDateTime.now())
                .update();

        //更新节点时间
        //更新通过，则进入下一个节点：待售后签收
        updateProgressByMainId(afterSaleEntity.getId(), AfterSaleStatusEnum.AFTER_SALES_RECEIVED.getCode(), "", "待签收");
        //日志
        String msg = StrUtil.format("用户填写买家寄出快递单号为[{}]", trackNo);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
        registerAfterSaleTrackAfterCommit(afterSaleEntity, trackNo, false);
        return update;
    }


    // 维修推送
    public void sendSubscribeMsgRequest(AfterSaleEntity afterSaleEntity, String status) {
        sendWechatSubscribeMsg(afterSaleEntity, status, SettingEnum.AFTER_SALSE_SUBSCRIBE_MSG, null);
    }

    // 售后申请单审批通过通知
    public void sendAfterSaleApproveMsgRequest(AfterSaleEntity afterSaleEntity, String status) {
        sendWechatSubscribeMsg(afterSaleEntity, status, SettingEnum.AFTER_SALSE_ORDER_APPROVE_MSG, null);
    }

    // 售后申请单取消通知
    public void sendAfterSaleCancelMsgRequest(AfterSaleEntity afterSaleEntity, String status, String remark) {
        sendWechatSubscribeMsg(afterSaleEntity, status, SettingEnum.AFTER_SALSE_ORDER_CANCEL_MSG, remark);
    }

    // 公共方法，处理微信订阅消息推送
    private void sendWechatSubscribeMsg(AfterSaleEntity afterSaleEntity, String status, SettingEnum settingEnum, String remark) {
        if (validateAfterSaleEntity(afterSaleEntity, status)) {
            return;
        }

        ThridUserInfoEntity thridUserInfoEntity = thridUserInfoService.getById(afterSaleEntity.getThridUserId());
        String openId = getOpenId(thridUserInfoEntity);
        if (StringUtils.isBlank(openId)) {
            log.error("微信用户信息openid不存在");
            return;
        }

        String msg = cfgSettingService.getValue(settingEnum);
        if (StringUtils.isBlank(msg)) {
            throw new ServiceException("售后微信消息订阅配置不存在");
        }

        SubscribeMsgRequest request = JSONUtil.toBean(msg, SubscribeMsgRequest.class);
        request.setTouser(openId);
        request.setPage(request.getPage() + afterSaleEntity.getCode());
        Map<String, SubscribeMsgRequest.DataItem> data = request.getData();

        switch (settingEnum) {
            case AFTER_SALSE_SUBSCRIBE_MSG:
                data.get("character_string1").setValue(afterSaleEntity.getCode());
                data.get("thing2").setValue(AfterSaleStatusEnum.getNode(status));
                data.get("time6").setValue(LocalDateTimeUtil.format(LocalDateTime.now(), DateUtil.fmt));
                break;
            case AFTER_SALSE_ORDER_APPROVE_MSG:
                data.get("date2").setValue(LocalDateTimeUtil.format(afterSaleEntity.getApproveTime(), DateUtil.fmt));
                data.get("phrase6").setValue(AfterSaleStatusEnum.getNode(status));
                break;
            case AFTER_SALSE_ORDER_CANCEL_MSG:
                data.get("character_string3").setValue(afterSaleEntity.getCode());
                data.get("phrase4").setValue(AfterSaleStatusEnum.getNode(status));
                data.get("thing6").setValue(remark);
                break;
            default:
                throw new ServiceException("未知的消息配置类型");
        }

        DmpPushMsgEntity dmpPushMsgEntity = buildDmpPushMsgEntity(afterSaleEntity, request);
        SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.DMP_WECHAT_SUBSCRIBE_MSG_TOPIC, RocketMqTagEnum.DMP_WECHAT_SUBSCRIBE_MSG_TAG.getName(),
                dmpPushMsgEntity, IdUtil.simpleUUID());

//        //发送微信订阅消息
//        String result = wxMiniAppService.sendSubscribeMsg(dmpPushMsgEntity.getPushData());
//        log.info("【{}】发送微信订阅消息结果：{}",dmpPushMsgEntity.getSourceCode(),result);
//        dmpPushMsgEntity.setRemark(result);
//        dmpPushMsgService.save(dmpPushMsgEntity);
    }

    // 参数校验
    private boolean validateAfterSaleEntity(AfterSaleEntity afterSaleEntity, String status) {
        if (Objects.isNull(afterSaleEntity) || StringUtils.isBlank(afterSaleEntity.getCode()) || StringUtils.isBlank(status) || StringUtils.isBlank(afterSaleEntity.getThridUserId())) {
            return true;
        }
        return false;
    }

    // 获取openId
    private String getOpenId(ThridUserInfoEntity thridUserInfoEntity) {
        if (Objects.nonNull(thridUserInfoEntity) && StringUtils.isNotBlank(thridUserInfoEntity.getOpenid())) {
            return thridUserInfoEntity.getOpenid();
        }
        return null;
    }

    // 构建DmpPushMsgEntity
    private DmpPushMsgEntity buildDmpPushMsgEntity(AfterSaleEntity afterSaleEntity, SubscribeMsgRequest request) {
        DmpPushMsgEntity dmpPushMsgEntity = new DmpPushMsgEntity();
        String jsonStr = JSONUtil.toJsonStr(request);
        dmpPushMsgEntity.setTargetPlatform("wx");
        dmpPushMsgEntity.setSourcePlatform(ServiceCodeNameEnum.DMP.getCode());
        dmpPushMsgEntity.setSourceType(SourceTypeEnum.AFTER_SALE.getCode());
        dmpPushMsgEntity.setSourceId(afterSaleEntity.getId());
        dmpPushMsgEntity.setSourceCode(afterSaleEntity.getCode());
        dmpPushMsgEntity.setPushData(jsonStr);
        dmpPushMsgEntity.setMessageCreateTime(LocalDateTime.now());
        return dmpPushMsgEntity;
    }

    @Override
    public List<BatchResultDTO> logisticsOrder(AfterSaleDTO.LogisticsOrderDTO dto) {
        validateLogisticsOrderDto(dto);
        return ApplicationContextUtils.getBean(AfterSaleServiceImpl.class).logisticsOrderWithLock(dto);
    }

    /**
     * 寄修物流下单（加分布式锁后执行）：校验通过后按下单方式分流至平台/自行寄出逻辑。
     *
     * @param dto 已通过 {@link #validateLogisticsOrderDto} 校验的物流下单入参
     * @return 各售后单下单结果
     */
    @DistributeLocker(businessType = AFTER_SALE_LOCK, keyName = "dto.orderInfoDTOList.id", waiteTime = 60)
    public List<BatchResultDTO> logisticsOrderWithLock(AfterSaleDTO.LogisticsOrderDTO dto) {
        AfterSaleLogisticsOrderModeEnum orderMode = AfterSaleLogisticsOrderModeEnum.getByCode(dto.getOrderMode());
        if (AfterSaleLogisticsOrderModeEnum.MANUAL.equals(orderMode)) {
            return manualLogisticsOrder(dto);
        }
        return platformLogisticsOrder(dto);
    }

    /**
     * 按物流下单方式校验入参：platform 校验渠道等平台字段，manual 校验自行寄出运单号等字段。
     *
     * @param dto 物流下单入参
     */
    private void validateLogisticsOrderDto(AfterSaleDTO.LogisticsOrderDTO dto) {
        AfterSaleLogisticsOrderModeEnum orderMode = AfterSaleLogisticsOrderModeEnum.getByCode(dto.getOrderMode());
        if (orderMode == null) {
            throw new ServiceException("物流下单方式不正确");
        }
        Class<?> validateGroup = AfterSaleLogisticsOrderModeEnum.MANUAL.equals(orderMode)
                ? AfterSaleLogisticsManualOrderGroup.class
                : AfterSaleLogisticsPlatformOrderGroup.class;
        Set<ConstraintViolation<AfterSaleDTO.LogisticsOrderDTO>> violations = validator.validate(dto, validateGroup);
        if (CollectionUtils.isNotEmpty(violations)) {
            throw new ServiceException(violations.iterator().next().getMessage());
        }
    }

    private static final String EXISTING_OUTBOUND_TRACK_MSG = "商家寄出快递单号不为空，不能进行下单，请先取消物流订单后操作";

    private Set<String> getExistingOutboundTrackMainIds(List<AfterSaleProgressEntity> shippedProgressList) {
        return shippedProgressList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getTrackNo()))
                .map(AfterSaleProgressEntity::getMainId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<BatchResultDTO> platformLogisticsOrder(AfterSaleDTO.LogisticsOrderDTO dto) {
        List<String> afterSaleIdList = dto.getOrderInfoDTOList().stream()
                .map(AfterSaleDTO.OrderInfoDTO::getId)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
        Map<String, AfterSaleEntity> afterSaleEntityMap = Collections.emptyMap();
        Set<String> existingTrackMainIds = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(afterSaleIdList)) {
            List<AfterSaleEntity> afterSaleEntityList = super.listByIds(afterSaleIdList);
            afterSaleEntityMap = afterSaleEntityList.stream()
                    .collect(Collectors.toMap(AfterSaleEntity::getId, Function.identity(), (v1, v2) -> v1));
            List<AfterSaleProgressEntity> afterSaleProgressList = afterSaleProgressService.lambdaQuery()
                    .in(AfterSaleProgressEntity::getMainId, afterSaleIdList)
                    .eq(AfterSaleProgressEntity::getNode, AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                    .list();
            existingTrackMainIds = getExistingOutboundTrackMainIds(afterSaleProgressList);
        }
        List<BatchResultDTO> resultList = new ArrayList<>();
        AfterSaleServiceImpl afterSaleService = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
        for (AfterSaleDTO.OrderInfoDTO orderInfoDTO : dto.getOrderInfoDTOList()) {
            if (StringUtils.isBlank(orderInfoDTO.getId())) {
                resultList.add(BatchResultDTO.fail(orderInfoDTO.getId(), orderInfoDTO.getCode(), "主键id不能为空"));
                continue;
            }
            AfterSaleEntity afterSaleEntity = afterSaleEntityMap.get(orderInfoDTO.getId());
            if (afterSaleEntity == null) {
                resultList.add(BatchResultDTO.fail(orderInfoDTO.getId(), orderInfoDTO.getCode(), "未找到售后申请单数据"));
                continue;
            }
            if (existingTrackMainIds.contains(afterSaleEntity.getId())) {
                resultList.add(BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), EXISTING_OUTBOUND_TRACK_MSG));
                continue;
            }
            try {
                resultList.add(afterSaleService.platformLogisticsOrderSingle(orderInfoDTO, dto));
            } catch (Exception e) {
                resultList.add(BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), e));
            }
        }
        return resultList;
    }

    public BatchResultDTO platformLogisticsOrderSingle(AfterSaleDTO.OrderInfoDTO orderInfoDTO, AfterSaleDTO.LogisticsOrderDTO dto) {
        AfterSaleEntity afterSaleEntity = super.getById(orderInfoDTO.getId());
        if (afterSaleEntity == null) {
            return BatchResultDTO.fail(orderInfoDTO.getId(), orderInfoDTO.getCode(), "未找到售后申请单数据");
        }
        AfterSaleProgressEntity shippedProgressEntity = afterSaleProgressService.getByNode(
                afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        if (shippedProgressEntity != null && StringUtils.isNotBlank(shippedProgressEntity.getTrackNo())) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), EXISTING_OUTBOUND_TRACK_MSG);
        }
        LogisticsOrderEntity logisticsOrderEntity = buildPlatformLogisticsOrderEntity(orderInfoDTO, dto, afterSaleEntity);
        List<AfterSaleDTO.LogisticsOrderResultDTO> resultDTOList = logisticsOrderFeign.addBatch(Collections.singletonList(logisticsOrderEntity));
        AfterSaleDTO.LogisticsOrderResultDTO resultDTO = CollectionUtils.isEmpty(resultDTOList) ? null : resultDTOList.get(0);
        if (resultDTO == null) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "下单失败");
        }
        if (!Boolean.TRUE.equals(resultDTO.getStatus())) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), resultDTO.getErrorMsg());
        }
        String trackNo = resultDTO.getTrackNo();
        try {
            BatchResultDTO persistResult = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class)
                    .platformLogisticsOrderPersistSingle(afterSaleEntity.getId(), dto.getLogisticsChannelId(), trackNo);
            if (!Boolean.TRUE.equals(persistResult.getSuccess())) {
                log.warn("平台物流下单远程成功但本地落库失败, afterSaleId={}, code={}, trackNo={}, msg={}",
                        afterSaleEntity.getId(), afterSaleEntity.getCode(), trackNo, persistResult.getMsg());
            }
            return persistResult;
        } catch (Exception e) {
            log.warn("平台物流下单远程成功但本地落库异常, afterSaleId={}, code={}, trackNo={}",
                    afterSaleEntity.getId(), afterSaleEntity.getCode(), trackNo, e);
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), e);
        }
    }

    private LogisticsOrderEntity buildPlatformLogisticsOrderEntity(AfterSaleDTO.OrderInfoDTO orderInfoDTO,
                                                                     AfterSaleDTO.LogisticsOrderDTO dto,
                                                                     AfterSaleEntity afterSaleEntity) {
        LogisticsOrderEntity logisticsOrderEntity = new LogisticsOrderEntity();
        BeanMapperUtils.copy(orderInfoDTO, logisticsOrderEntity);
        logisticsOrderEntity.setId(null);
        logisticsOrderEntity.setTrackNo(null);
        logisticsOrderEntity.setAfterSaleId(orderInfoDTO.getId());
        logisticsOrderEntity.setLogisticsPlatform(dto.getLogisticsPlatform());
        logisticsOrderEntity.setLogisticsChannelId(dto.getLogisticsChannelId());
        logisticsOrderEntity.setSourceCode(afterSaleEntity.getCode());
        logisticsOrderEntity.setLabelStatus(LogisticsLabelStatusEnum.NOT_OBTAINED.getCode());
        logisticsOrderEntity.setSourceType(SourceTypeEnum.AFTER_SALE.getCode());
        logisticsOrderEntity.setCountry(CountrySiteEnum.CHINA.getSite());
        logisticsOrderEntity.setReceiver(afterSaleEntity.getUsername());
        logisticsOrderEntity.setContactNumber(afterSaleEntity.getPhoneNumber());
        return logisticsOrderEntity;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BatchResultDTO platformLogisticsOrderPersistSingle(String afterSaleId, String logisticsChannelId, String trackNo) {
        AfterSaleEntity afterSaleEntity = super.getById(afterSaleId);
        if (afterSaleEntity == null) {
            return BatchResultDTO.fail(afterSaleId, afterSaleId, "未找到售后申请单数据");
        }
        AfterSaleProgressEntity shippedProgressEntity = afterSaleProgressService.getByNode(
                afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        if (shippedProgressEntity != null && StringUtils.isNotBlank(shippedProgressEntity.getTrackNo())) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), EXISTING_OUTBOUND_TRACK_MSG);
        }
        afterSaleEntity.setType(OutboundTrackNoTypeEnum.API.getCode());
        afterSaleEntity.setLogisticsChannelId(logisticsChannelId);
        afterSaleEntity.setStatus(AfterSaleStatusEnum.COMPLETED.getCode());
        super.updateById(afterSaleEntity);
        if (shippedProgressEntity == null) {
            shippedProgressEntity = new AfterSaleProgressEntity();
            shippedProgressEntity.setMainId(afterSaleEntity.getId());
            shippedProgressEntity.setIndex(1);
            shippedProgressEntity.setNode(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        } else if (!AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(shippedProgressEntity.getNode())) {
            shippedProgressEntity.setNode(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        } else {
            String oldTrackNo = shippedProgressEntity.getTrackNo();
            if (!Objects.equals(oldTrackNo, trackNo)) {
                String msg = StrUtil.format("用户【{}】编辑商家寄出快递单号由[{}]变更为[{}] ",
                        UserContext.getDefaultLoginUser().getUserName(), oldTrackNo, trackNo);
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
            }
        }
        shippedProgressEntity.setTrackNo(trackNo);
        shippedProgressEntity.setNodeTime(LocalDateTime.now());
        afterSaleProgressService.saveOrUpdate(shippedProgressEntity);
        AfterSaleProgressEntity completedProgressEntity = afterSaleProgressService.getByNode(
                afterSaleEntity.getId(), AfterSaleStatusEnum.COMPLETED.getCode());
        if (completedProgressEntity != null) {
            completedProgressEntity.setNodeTime(LocalDateTime.now());
            afterSaleProgressService.updateById(completedProgressEntity);
        }
        registerAfterSaleTrackAfterCommit(afterSaleEntity, trackNo, true);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendSubscribeMsgRequest(afterSaleEntity, AfterSaleStatusEnum.COMPLETED.getCode());
            }
        });
        return BatchResultDTO.success(afterSaleEntity.getId(), afterSaleEntity.getCode(), "下单成功");
    }

    private List<BatchResultDTO> manualLogisticsOrder(AfterSaleDTO.LogisticsOrderDTO dto) {
        List<String> afterSaleIdList = dto.getOrderInfoDTOList().stream()
                .map(AfterSaleDTO.OrderInfoDTO::getId)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
        Map<String, AfterSaleEntity> afterSaleEntityMap = Collections.emptyMap();
        Set<String> existingTrackMainIds = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(afterSaleIdList)) {
            List<AfterSaleEntity> afterSaleEntityList = super.listByIds(afterSaleIdList);
            afterSaleEntityMap = afterSaleEntityList.stream()
                    .collect(Collectors.toMap(AfterSaleEntity::getId, Function.identity(), (v1, v2) -> v1));
            List<AfterSaleProgressEntity> afterSaleProgressList = afterSaleProgressService.lambdaQuery()
                    .in(AfterSaleProgressEntity::getMainId, afterSaleIdList)
                    .eq(AfterSaleProgressEntity::getNode, AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                    .list();
            existingTrackMainIds = getExistingOutboundTrackMainIds(afterSaleProgressList);
        }
        Map<String, AfterSaleDTO.NodeDTO> nodeMap = loadAllNodeList().stream()
                .collect(Collectors.toMap(AfterSaleDTO.NodeDTO::getNode, w -> w));
        AfterSaleDTO.NodeDTO completedNodeDTO = nodeMap.get(AfterSaleStatusEnum.COMPLETED.getCode());
        if (Objects.isNull(completedNodeDTO)) {
            throw new ServiceException("目标节点配置不存在，请检查售后维修节点配置");
        }
        List<BatchResultDTO> resultList = new ArrayList<>();
        AfterSaleServiceImpl afterSaleService = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
        for (AfterSaleDTO.OrderInfoDTO orderInfoDTO : dto.getOrderInfoDTOList()) {
            if (StringUtils.isBlank(orderInfoDTO.getId())) {
                resultList.add(BatchResultDTO.fail(orderInfoDTO.getId(), orderInfoDTO.getCode(), "主键id不能为空"));
                continue;
            }
            AfterSaleEntity afterSaleEntity = afterSaleEntityMap.get(orderInfoDTO.getId());
            if (afterSaleEntity == null) {
                resultList.add(BatchResultDTO.fail(orderInfoDTO.getId(), orderInfoDTO.getCode(), "未找到售后申请单数据"));
                continue;
            }
            if (existingTrackMainIds.contains(afterSaleEntity.getId())) {
                resultList.add(BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), EXISTING_OUTBOUND_TRACK_MSG));
                continue;
            }
            String trackNo = StringUtils.trimToEmpty(orderInfoDTO.getTrackNo());
            try {
                resultList.add(afterSaleService.manualLogisticsOrderSingle(
                        orderInfoDTO.getId(), trackNo, nodeMap, completedNodeDTO));
            } catch (Exception e) {
                resultList.add(BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), e));
            }
        }
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BatchResultDTO manualLogisticsOrderSingle(String afterSaleId, String trackNo,
                                                     Map<String, AfterSaleDTO.NodeDTO> nodeMap,
                                                     AfterSaleDTO.NodeDTO completedNodeDTO) {
        AfterSaleEntity afterSaleEntity = super.getById(afterSaleId);
        if (afterSaleEntity == null) {
            return BatchResultDTO.fail(afterSaleId, afterSaleId, "未找到售后申请单数据");
        }
        if (!ApproveStatusEnum.APPROVE.getCode().equals(afterSaleEntity.getApproveStatus().getCode())
                || AfterSaleStatusEnum.isTerminalStatus(afterSaleEntity.getStatus())) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "只有审核通过并且单据状态未完成、未中止数据支持状态修改");
        }
        AfterSaleDTO.NodeDTO oldNodeDTO = nodeMap.get(afterSaleEntity.getStatus());
        if (Objects.isNull(oldNodeDTO)) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "当前进度节点配置异常，请联系管理员");
        }
        if (oldNodeDTO.getIndex() > completedNodeDTO.getIndex()) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "当前节点不能小于等于原节点");
        }
        AfterSaleProgressEntity completedProgressEntity = afterSaleProgressService.getByNode(
                afterSaleEntity.getId(), AfterSaleStatusEnum.COMPLETED.getCode());
        if (Objects.isNull(completedProgressEntity)) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "进度节点不存在，请联系管理员");
        }
        AfterSaleProgressEntity shippedProgressEntity = afterSaleProgressService.getByNode(
                afterSaleEntity.getId(), AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        if (shippedProgressEntity != null && StringUtils.isNotBlank(shippedProgressEntity.getTrackNo())) {
            return BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), EXISTING_OUTBOUND_TRACK_MSG);
        }
        afterSaleEntity.setType(OutboundTrackNoTypeEnum.MANUAL.getCode());
        afterSaleEntity.setStatus(AfterSaleStatusEnum.COMPLETED.getCode());
        super.updateById(afterSaleEntity);
        if (shippedProgressEntity == null) {
            AfterSaleDTO.NodeDTO shippedNodeDTO = nodeMap.get(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
            shippedProgressEntity = new AfterSaleProgressEntity();
            shippedProgressEntity.setMainId(afterSaleEntity.getId());
            shippedProgressEntity.setIndex(shippedNodeDTO != null ? shippedNodeDTO.getIndex() : oldNodeDTO.getIndex());
            shippedProgressEntity.setNode(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode());
        } else if (!Objects.equals(shippedProgressEntity.getTrackNo(), trackNo)) {
            String msg = StrUtil.format("用户【{}】编辑商家寄出快递单号由[{}]变更为[{}] ",
                    UserContext.getDefaultLoginUser().getUserName(), shippedProgressEntity.getTrackNo(), trackNo);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
        }
        shippedProgressEntity.setTrackNo(trackNo);
        shippedProgressEntity.setNodeTime(LocalDateTime.now());
        afterSaleProgressService.saveOrUpdate(shippedProgressEntity);
        completedProgressEntity.setNodeTime(LocalDateTime.now());
        afterSaleProgressService.updateById(completedProgressEntity);
        registerAfterSaleTrackAfterCommit(afterSaleEntity, trackNo, true);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                bean.sendSubscribeMsgRequest(afterSaleEntity, AfterSaleStatusEnum.COMPLETED.getCode());
            }
        });
        String msg = StrUtil.format("用户【{}】更新状态由[{}]变更为[{}]  ",
                UserContext.getDefaultLoginUser().getUserName(), oldNodeDTO.getNodeName(), completedNodeDTO.getNodeName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "编辑信息");
        return BatchResultDTO.success(afterSaleEntity.getId(), afterSaleEntity.getCode(), "提交成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = AFTER_SALE_LOCK, keyName = "dto.getIds", waiteTime = 60)
    public List<BatchResultDTO> batchCancel(AfterSaleDTO.IdsDTO dto) {
        List<AfterSaleEntity> list = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> afterSaleIdList = list.stream().map(AfterSaleEntity::getId).collect(Collectors.toList());
        List<AfterSaleProgressEntity> afterSaleProgressList = afterSaleProgressService.lambdaQuery()
                .in(AfterSaleProgressEntity::getMainId, afterSaleIdList)
                .eq(AfterSaleProgressEntity::getNode, AfterSaleStatusEnum.TO_BE_SHIPPED.getCode())
                .list();
        Map<String, AfterSaleProgressEntity> afterSaleProgressMap = afterSaleProgressList.stream().collect(Collectors.toMap(AfterSaleProgressEntity::getMainId, Function.identity(), (v1, v2) -> v1));
        List<DmpAttachmentEntity> attachmentList = attachmentService.lambdaQuery().in(DmpAttachmentEntity::getBusinessId, afterSaleIdList).eq(DmpAttachmentEntity::getType, DmpConstant.AFTER_SALE_LABEL).list();
        Map<String, DmpAttachmentEntity> attachmentMap = attachmentList.stream().collect(Collectors.toMap(DmpAttachmentEntity::getBusinessId, Function.identity(), (v1, v2) -> v1));
        // 筛选出单据类型不是API的
        List<AfterSaleEntity> manualList = list.stream().filter(item -> !OutboundTrackNoTypeEnum.API.getCode().equals(item.getType())).collect(Collectors.toList());
        // 筛选出单据类型是API的
        List<AfterSaleEntity> apiList = list.stream().filter(item -> OutboundTrackNoTypeEnum.API.getCode().equals(item.getType())).collect(Collectors.toList());
        List<AfterSaleProgressEntity> progressList = new ArrayList<>();
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (AfterSaleEntity afterSaleEntity : manualList) {
            BatchResultDTO cancelResult;
            if (StringUtils.isBlank(afterSaleEntity.getType())) {
                cancelResult = BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "没有下单的数据不能操作取消");
                resultList.add(cancelResult);
            } else if (OutboundTrackNoTypeEnum.MANUAL.getCode().equals(afterSaleEntity.getType())) {
                // 取消成功清空商家寄出快递单号和面单信息
                AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressMap.get(afterSaleEntity.getId());
                if (afterSaleProgressEntity != null && StringUtils.isNotBlank(afterSaleProgressEntity.getTrackNo())) {
                    afterSaleProgressEntity.setTrackNo("");
                    progressList.add(afterSaleProgressEntity);
                    DmpAttachmentEntity dmpAttachmentEntity = attachmentMap.get(afterSaleEntity.getId());
                    if (dmpAttachmentEntity != null) {
                        attachmentService.removeById(dmpAttachmentEntity.getId());
                    }
                    cancelResult = BatchResultDTO.success(afterSaleEntity.getId(), afterSaleEntity.getCode(), "操作成功");
                    resultList.add(cancelResult);
                } else {
                    cancelResult = BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "商家寄出快递单号不为空才可操作取消");
                    resultList.add(cancelResult);
                }
            }
        }
        Map<String, AfterSaleEntity> apiMap = apiList.stream().collect(Collectors.toMap(AfterSaleEntity::getId, Function.identity(), (v1, v2) -> v1));
        if (CollectionUtils.isNotEmpty(apiList)) {
            List<String> codeList = apiList.stream().map(AfterSaleEntity::getCode).collect(Collectors.toList());
            List<AfterSaleDTO.LogisticsOrderResultDTO> resultDTOList = logisticsOrderFeign.batchCancel(codeList);
            List<AfterSaleEntity> updateApiList = new ArrayList<>();
            for (AfterSaleDTO.LogisticsOrderResultDTO resultDTO : resultDTOList) {
                AfterSaleEntity afterSaleEntity = apiMap.get(resultDTO.getAfterSaleId());
                if (afterSaleEntity == null) {
                    log.warn("取消物流订单时未找到对应的售后单，afterSaleId: {}", resultDTO.getAfterSaleId());
                    continue;
                }
                BatchResultDTO cancelResult;
                if (Boolean.TRUE.equals(resultDTO.getStatus())) {
                    afterSaleEntity.setType("");
                    afterSaleEntity.setLogisticsChannelId("");
                    updateApiList.add(afterSaleEntity);
                    // 取消成功清空商家寄出快递单号和面单信息
                    AfterSaleProgressEntity afterSaleProgressEntity = afterSaleProgressMap.get(resultDTO.getAfterSaleId());
                    if (afterSaleProgressEntity != null) {
                        afterSaleProgressEntity.setTrackNo("");
                        progressList.add(afterSaleProgressEntity);
                    }
                    DmpAttachmentEntity dmpAttachmentEntity = attachmentMap.get(resultDTO.getAfterSaleId());
                    if (dmpAttachmentEntity != null) {
                        attachmentService.removeById(dmpAttachmentEntity.getId());
                    }
                    cancelResult = BatchResultDTO.success(resultDTO.getAfterSaleId(), resultDTO.getCode(), "操作成功");
                } else {
                    cancelResult = BatchResultDTO.fail(resultDTO.getAfterSaleId(), resultDTO.getCode(), resultDTO.getErrorMsg());
                }
                resultList.add(cancelResult);
            }
            if (CollectionUtils.isNotEmpty(updateApiList)) {
                this.updateBatchById(updateApiList);
            }
        }
        if (CollectionUtils.isNotEmpty(progressList)) {
            afterSaleProgressService.updateBatchById(progressList);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "dto.getId()")
    public String uploadLogisticLabel(AfterSaleDTO.UploadFileDTO dto) {
        AfterSaleEntity entity = getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException("寄修申请单不存在");
        }
        // 校验是不是pdf文件
        if (!StringUtils.endsWithIgnoreCase(dto.getAttachName(), ".pdf")) {
            throw new ServiceException("仅支持上传PDF格式的文件");
        }
        DmpAttachmentEntity attachmentEntity = attachmentService.lambdaQuery().eq(DmpAttachmentEntity::getBusinessId, dto.getId()).eq(DmpAttachmentEntity::getType, DmpConstant.AFTER_SALE_LABEL).one();
        if (Objects.nonNull(attachmentEntity)) {
            attachmentService.removeById(attachmentEntity.getId());
        }
        DmpAttachmentEntity dmpAttachmentEntity = new DmpAttachmentEntity();
        dmpAttachmentEntity.setAttachName(dto.getAttachName());
        dmpAttachmentEntity.setAttachUrl(dto.getAttachUrl());
        dmpAttachmentEntity.setType(DmpConstant.AFTER_SALE_LABEL);
        dmpAttachmentEntity.setBusinessId(entity.getId());
        attachmentService.save(dmpAttachmentEntity);
        // 更新面单状态
        logisticsOrderFeign.updateLogisticsOrder(entity.getId());
        String msg = CharSequenceUtil.format("用户【{}】上传文件名为【{}】的物流面单 ", UserContext.getDefaultLoginUser().getUserName(), dto.getAttachName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), dto.getId(), "上传面单");
        return "";
    }

    @Override
    public List<AfterSaleDTO.OrderInfoDTO> getPlaceOrderPreview(BaseIdsDTO.IdsDTO dto) {
        List<AfterSaleDTO.OrderInfoDTO> list = this.baseMapper.getPlaceOrderPreview(dto.getIds());
        // 筛选出商家寄出快递单号不为空的数据
        List<String> codeList = list.stream()
                .filter(e -> StringUtils.isNotBlank(e.getOutboundTrackNo()))
                .map(AfterSaleDTO.OrderInfoDTO::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(codeList)) {
            String content = StrUtil.format("单据编号：【{}】的商家寄出快递单号不为空，不能进行下单，请先取消物流订单后操作", String.join(",", codeList));
            throw new ServiceException(content);
        }
        // 过滤出商家寄出快递单号为空的数据
        List<AfterSaleDTO.OrderInfoDTO> filterList = list.stream().filter(item -> StringUtils.isBlank(item.getOutboundTrackNo())).collect(Collectors.toList());
        ApiResult<List<BaseDropDownDTO.CommonDTO>> listApiResult = omsDropDownFeign.listInternalSalesPlatform(DictBasicTypeEnum.MINI_PROGRAM_SALES_PLATFORM_INTERNAL.getType());
        for (AfterSaleDTO.OrderInfoDTO orderInfoDTO : filterList) {
            if (StringUtils.isNotBlank(orderInfoDTO.getDictPlatform())) {
                BaseDropDownDTO.CommonDTO commonDTO = listApiResult.getData().stream().filter(e -> e.getCode().equals(orderInfoDTO.getDictPlatform())).findFirst().orElse(null);
                if (Objects.nonNull(commonDTO)) {
                    orderInfoDTO.setDictPlatformName(commonDTO.getValue());
                }
            }
        }
        return filterList;
    }

    @Override
    public AfterSaleDTO.LogisticsLabelPreviewDTO printLogisticsLabelPreview(BaseIdsDTO.IdsDTO dto) {
        AfterSaleDTO.LogisticsLabelPreviewDTO result = new AfterSaleDTO.LogisticsLabelPreviewDTO();
        List<AfterSaleDTO.OrderInfoDTO> list = this.baseMapper.getPlaceOrderPreview(dto.getIds());
        // 取出商家寄出快递单号不为空的并且单据类型是API的
        List<AfterSaleDTO.OrderInfoDTO> filterList = list.stream().filter(item -> OutboundTrackNoTypeEnum.API.getCode().equals(item.getType())).collect(Collectors.toList());

        // 查询物流商信息
        List<String> logisticsChannelIds = filterList.stream().map(AfterSaleDTO.OrderInfoDTO::getLogisticsChannelId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(logisticsChannelIds)) {
            List<LogisticsChannelDTO.BaseDTO> channelInfoList = logisticsFeign.listChannelInfoById(logisticsChannelIds);
            List<String> paperSizeList = channelInfoList.stream().map(LogisticsChannelDTO.BaseDTO::getPaperSize).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            // 打印配货单默认100*100不校验
            if (paperSizeList.size() > 1) {
                throw new ServiceException(ApiError.COMMON_PAPER_SIZE_INCONSISTENT_NOT_PRINT);
            }
        }

        List<String> outboundTrackNoList = filterList.stream().map(AfterSaleDTO.OrderInfoDTO::getOutboundTrackNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        Map<String, LogisticsOrderDTO.ListDTO> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(outboundTrackNoList)) {
            List<LogisticsOrderDTO.ListDTO> listDTOS = logisticsOrderFeign.getLogisticsOrderListByTrackNo(outboundTrackNoList);
            map = listDTOS.stream().collect(Collectors.toMap(LogisticsOrderDTO.ListDTO::getAfterSaleId, item -> item));
        }
        // 查询面单信息
        List<DmpAttachmentEntity> attachmentList = attachmentService.list(new QueryWrapper<DmpAttachmentEntity>().lambda()
                .in(DmpAttachmentEntity::getBusinessId, dto.getIds())
                .eq(DmpAttachmentEntity::getType, DmpConstant.AFTER_SALE_LABEL));
        List<String> bussinessIdList = attachmentList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getAttachUrl())).map(DmpAttachmentEntity::getBusinessId).distinct().collect(Collectors.toList());
        List<String> notPrintCodes = list.stream().filter(e -> !bussinessIdList.contains(e.getId())).map(AfterSaleDTO.OrderInfoDTO::getCode).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notPrintCodes)) {
            throw new ServiceException(ApiError.SO_LOGISTICS_WAYBILL_NOT_OBTAINED, CharSequenceUtil.join(",", notPrintCodes));
        }
        Map<String, DmpAttachmentEntity> attachmentMap = attachmentList.stream().collect(Collectors.toMap(DmpAttachmentEntity::getBusinessId, Function.identity(), (v1, v2) -> v1));
        Map<String, String> notPrintReasonMap = new HashMap<>();
        List<AfterSaleDTO.LogisticsLabelPreviewListDTO> labelPreviewListDTOS = new ArrayList<>();
        for (AfterSaleDTO.OrderInfoDTO orderInfoDTO : list) {
            AfterSaleDTO.LogisticsLabelPreviewListDTO labelPreviewListDTO = new AfterSaleDTO.LogisticsLabelPreviewListDTO();
            labelPreviewListDTO.setId(orderInfoDTO.getId());
            labelPreviewListDTO.setTrackNo(orderInfoDTO.getOutboundTrackNo());
            labelPreviewListDTO.setCode(orderInfoDTO.getCode());
            LogisticsOrderDTO.ListDTO listDTO = map.get(orderInfoDTO.getId());
            if (listDTO != null) {
                labelPreviewListDTO.setLogisticsPlatform(listDTO.getLogisticsPlatform());
                labelPreviewListDTO.setLogisticsPlatformName(listDTO.getLogisticsPlatformName());
                labelPreviewListDTO.setLogisticsChannelId(listDTO.getLogisticsChannelId());
                labelPreviewListDTO.setLogisticsChannelName(listDTO.getLogisticsChannelName());
            }
            DmpAttachmentEntity attachmentEntity = attachmentMap.get(orderInfoDTO.getId());
            if (attachmentEntity == null) {
                if (listDTO != null) {
                    String channelName = labelPreviewListDTO.getLogisticsChannelName();
                    if (StringUtils.isBlank(channelName)) {
                        channelName = "未知物流商";
                    }
                    if (StringUtils.isBlank(listDTO.getExceptionType())) {
                        notPrintReasonMap.put(channelName, "未获取面单");
                    } else if (ExceptionTypeEnum.LABEL_EXCEPTION.getCode().equals(listDTO.getExceptionType())) {
                        notPrintReasonMap.put(channelName, listDTO.getExceptionReason());
                    }
                }
            } else {
                labelPreviewListDTO.setAttachName(attachmentEntity.getAttachName());
                labelPreviewListDTO.setAttachUrl(attachmentEntity.getAttachUrl());
            }
            labelPreviewListDTOS.add(labelPreviewListDTO);
        }
        // 有运单号数量
        Integer trackNoCount = Math.toIntExact(list.stream().filter(req -> CharSequenceUtil.isNotBlank(req.getOutboundTrackNo())).count());
        // 无运单号数量
        Integer notTrackNoCount = Math.toIntExact(list.stream().filter(req -> CharSequenceUtil.isBlank(req.getOutboundTrackNo())).count());
        result.setLabelPreviewListDTOS(labelPreviewListDTOS);
        result.setTrackNoCount(trackNoCount);
        result.setNotTrackNoCount(notTrackNoCount);
        result.setNotPrintCount(dto.getIds().size() - attachmentList.size());
        result.setNotPrintReasonMap(notPrintReasonMap.isEmpty() ? null : notPrintReasonMap);
        return result;
    }

    @Override
    public String printLogisticsLabelConfirm(BaseIdsDTO.IdsDTO dto) {
        // 查询面单信息
        List<DmpAttachmentEntity> attachmentList = attachmentService.list(new QueryWrapper<DmpAttachmentEntity>().lambda()
                .in(DmpAttachmentEntity::getBusinessId, dto.getIds())
                .eq(DmpAttachmentEntity::getType, DmpConstant.AFTER_SALE_LABEL)
                .orderByDesc(DmpAttachmentEntity::getCreateTime));
        if (CollectionUtils.isEmpty(attachmentList)) {
            throw new ServiceException("无可打印的物流面单");
        }
        // 同一业务id可能存在多条面单记录，按创建时间倒序后保留最新一条
        Map<String, String> baseMap = attachmentList.stream().collect(Collectors.toMap(DmpAttachmentEntity::getBusinessId, DmpAttachmentEntity::getAttachUrl, (v1, v2) -> v1));
        List<String> urlList = dto.getIds().stream().map(e -> baseMap.getOrDefault(e, null)).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        try {
            return fileFeign.mergeFiles(urlList);
        } catch (Exception e) {
            log.error("合并文件失败", e);
            throw new ServiceException(ApiError.LOGISTICS_PDF_SO_MERGE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = AFTER_SALE_LOCK, keyName = "afterSaleIds", waiteTime = 60)
    public List<BatchResultDTO> getLogisticsOrderLabel(List<String> afterSaleIds, List<LogisticsOrderDTO.LogisticsLabelDTO> logisticsLabelDTOS) {
        log.info("getLogisticsOrderLabel开始：{}", JSON.toJSONString(logisticsLabelDTOS));
        List<String> afterSaleIdList = logisticsLabelDTOS.stream().map(LogisticsOrderDTO.LogisticsLabelDTO::getAfterSaleId).collect(Collectors.toList());
        List<DmpAttachmentEntity> attachmentList = attachmentService.list(new QueryWrapper<DmpAttachmentEntity>().lambda()
                .in(DmpAttachmentEntity::getBusinessId, afterSaleIdList)
                .eq(DmpAttachmentEntity::getType, DmpConstant.AFTER_SALE_LABEL));
        Map<String, DmpAttachmentEntity> attachmentMap = attachmentList.stream().collect(Collectors.toMap(DmpAttachmentEntity::getBusinessId, Function.identity(), (v1, v2) -> v1));
        List<AfterSaleDTO.LogisticsOrderResultDTO> resultDTOList = logisticsOrderFeign.batchGetLabel(logisticsLabelDTOS);
        log.info("调用TMS获取顺丰面单结束：{}", JSON.toJSONString(resultDTOList));
        List<AfterSaleEntity> afterSaleEntityList = super.listByIds(afterSaleIdList);
        Map<String, AfterSaleEntity> afterSaleEntityMap = afterSaleEntityList.stream().collect(Collectors.toMap(AfterSaleEntity::getId, Function.identity(), (v1, v2) -> v1));
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<DmpAttachmentEntity> saveList = new ArrayList<>();
        for (AfterSaleDTO.LogisticsOrderResultDTO resultDTO : resultDTOList) {
            BatchResultDTO batchResultDTO;
            if (Boolean.TRUE.equals(resultDTO.getStatus())) {
                if (attachmentMap.get(resultDTO.getAfterSaleId()) != null) {
                    attachmentService.removeById(attachmentMap.get(resultDTO.getAfterSaleId()).getId());
                }
                DmpAttachmentEntity attachmentEntity = new DmpAttachmentEntity();
                attachmentEntity.setBusinessId(resultDTO.getAfterSaleId());
                attachmentEntity.setType(DmpConstant.AFTER_SALE_LABEL);
                attachmentEntity.setAttachName(resultDTO.getTrackNo() + ".pdf");
                attachmentEntity.setAttachUrl(resultDTO.getUrl());
                saveList.add(attachmentEntity);
                AfterSaleEntity afterSaleEntity = afterSaleEntityMap.get(resultDTO.getAfterSaleId());
                batchResultDTO = BatchResultDTO.success(resultDTO.getAfterSaleId(), afterSaleEntity != null ? afterSaleEntity.getCode() : "", "获取面单成功");
            } else {
                AfterSaleEntity afterSaleEntity = afterSaleEntityMap.get(resultDTO.getAfterSaleId());
                batchResultDTO = BatchResultDTO.fail(resultDTO.getAfterSaleId(), afterSaleEntity != null ? afterSaleEntity.getCode() : "", resultDTO.getErrorMsg());
            }
            batchResultDTOList.add(batchResultDTO);
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            attachmentService.saveOrUpdateBatch(saveList);
        }
        return batchResultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.DMP_AFTER_SALE_KEY, keyName = "dto.ids", waiteTime = 60, unlockAfterTx = true)
    public List<BatchResultDTO> manualBatchGetLabel(BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<AfterSaleEntity> afterSaleEntityList = super.listByIds(dto.getIds());
        // 筛选出单据类型不是API的
        List<AfterSaleEntity> manualList = afterSaleEntityList.stream().filter(e -> !OutboundTrackNoTypeEnum.API.getCode().equals(e.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(manualList)) {
            for (AfterSaleEntity afterSaleEntity : manualList) {
                BatchResultDTO batchResultDTO = BatchResultDTO.fail(afterSaleEntity.getId(), afterSaleEntity.getCode(), "手动添加类型的单据不能获取面单");
                batchResultDTOList.add(batchResultDTO);
            }
        }
        // 筛选出单据类型是API的
        List<AfterSaleEntity> apiList = afterSaleEntityList.stream().filter(e -> OutboundTrackNoTypeEnum.API.getCode().equals(e.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(apiList)) {
            List<LogisticsOrderDTO.LogisticsLabelDTO> logisticsLabelDTOS = apiList.stream().map(e -> {
                LogisticsOrderDTO.LogisticsLabelDTO labelDTO = new LogisticsOrderDTO.LogisticsLabelDTO();
                labelDTO.setAfterSaleId(e.getId());
                return labelDTO;
            }).collect(Collectors.toList());
            List<String> afterSaleIds = logisticsLabelDTOS.stream().map(LogisticsOrderDTO.LogisticsLabelDTO::getAfterSaleId).filter(ObjectUtil::isNotEmpty).distinct().collect(Collectors.toList());
            List<BatchResultDTO> batchResultDTOS = getLogisticsOrderLabel(afterSaleIds, logisticsLabelDTOS);
            batchResultDTOList.addAll(batchResultDTOS);
        }
        return batchResultDTOList;
    }

    private void registerAfterSaleTrackAfterCommit(AfterSaleEntity entity, String trackNo, boolean outbound) {
        if (Objects.isNull(entity) || StringUtils.isBlank(trackNo)) {
            return;
        }
        AfterSaleEntity trackEntity = new AfterSaleEntity();
        trackEntity.setId(entity.getId());
        trackEntity.setCode(entity.getCode());
        trackEntity.setPlatformCode(entity.getPlatformCode());
        trackEntity.setShopId(entity.getShopId());
        trackEntity.setDictPlatform(entity.getDictPlatform());
        trackEntity.setPhoneNumber(entity.getPhoneNumber());
        trackEntity.setLogisticsChannelId(entity.getLogisticsChannelId());
        String registerTrackNo = trackNo;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                AfterSaleServiceImpl bean = ApplicationContextUtils.getBean(AfterSaleServiceImpl.class);
                if (!bean.registerAfterSaleTrack(trackEntity, registerTrackNo, outbound)) {
                    log.warn("寄修单物流轨迹注册失败（事务已提交），工单号={}，快递单号={}，商家寄出={}",
                            trackEntity.getCode(), registerTrackNo, outbound);
                }
            }
        });
    }

    private boolean registerAfterSaleTrack(AfterSaleEntity entity, String trackNo, boolean outbound) {
        if (Objects.isNull(entity) || StringUtils.isBlank(trackNo)) {
            return true;
        }
        try {
            LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
            addDTO.setOrderType(BillTypeEnum.AFTER_SALES.getCode());
            addDTO.setSourceType(SourceTypeEnum.AFTER_SALE.getCode());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setPlatformCode(entity.getPlatformCode());
            addDTO.setShopId(entity.getShopId());
            addDTO.setSalesPlatform(entity.getDictPlatform());
            addDTO.setTransportNo(trackNo);
            addDTO.setTelNumber(entity.getPhoneNumber());
            addDTO.setOrderTime(LocalDateTime.now());
            addDTO.setShipmentType(ShipmentTypeEnum.SELF_DELIVER.getCode());
            if (StringUtils.isNotBlank(entity.getLogisticsChannelId())) {
                addDTO.setChannelId(entity.getLogisticsChannelId());
                LogisticsChannelDTO.BaseDTO channelInfo = logisticsFeign.getChannelInfoById(entity.getLogisticsChannelId());
                if (Objects.nonNull(channelInfo)) {
                    addDTO.setChannelName(channelInfo.getName());
                }
            }

            LogisticsBillDetailDTO.AddDTO detailDTO = new LogisticsBillDetailDTO.AddDTO();
            detailDTO.setTrackNo(trackNo);
            detailDTO.setTrackEnable(Boolean.TRUE);
            addDTO.setDetailList(Collections.singletonList(detailDTO));
            logisticsBillFeign.addLogisticsBill(addDTO);
            log.warn("寄修单物流轨迹注册成功，工单号={}，快递单号={}，商家寄出={}", entity.getCode(), trackNo, outbound);
            return true;
        } catch (Exception e) {
            log.warn("寄修单物流轨迹注册失败，工单号={}，快递单号={}，商家寄出={}", entity.getCode(), trackNo, outbound, e);
            return false;
        }
    }

}
