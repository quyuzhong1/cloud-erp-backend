package com.erp.server.wms.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.model.sys.enums.NoticeItemRoleEnum;
import com.erp.model.sys.enums.NoticeNodeEnum;
import com.erp.model.sys.enums.NoticeReceiverEnum;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.QcResultEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcResultMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.QcResultService;
import com.erp.server.wms.service.WmsAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
@Slf4j
public class QcResultServiceImpl extends SuperServiceImpl<QcResultMapper, QcResultEntity> implements QcResultService {

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private DictBasicService dictBasicService;


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private CommonService commonService;

    @Autowired
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;


    /**
     * 质检信息 暂存
     *
     * @param billId
     * @param qcInfo
     * @return void
     * @author yl
     * @date 2023-04-19 10:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String billId, QcResultDTO.AddDTO qcInfo) {
        QcResultEntity qcResultEntity = new QcResultEntity();
        String id = qcInfo.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }

        String qcType = qcInfo.getQcType();
        //是否内检
        Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
        BeanMapper.copy(qcInfo, qcResultEntity);

        //计算比率
        calculateRatio(qcResultEntity);
        qcResultEntity.setMainId(billId);
        qcResultEntity.setId(id);
        qcResultEntity.setIsInside(isInside);
        //
        List<String> imageNameList = qcInfo.getBadImageNameList();
        List<String> imageUrlList = qcInfo.getBadImageUrlList();
        wmsAttachmentService.batchSave(imageUrlList, imageNameList, WmsConstant.BAD, id);
        this.saveOrUpdate(qcResultEntity);

    }


    /**
     * 计算比率
     *
     * @param qcResultEntity
     * @return void
     * @author yl
     * @date 2023-04-19 14:39
     */
    private void calculateRatio(QcResultEntity qcResultEntity) {
        if (qcResultEntity != null) {
            //总数量
            Integer totalQty = qcResultEntity.getTotalQty() != null ? qcResultEntity.getTotalQty() : 0;
            //质检量
            Integer qcQty = qcResultEntity.getQcQty() != null ? qcResultEntity.getQcQty() : 0;
            if (totalQty != 0) {
                BigDecimal qcSampleRate = MathUtil.divide(new BigDecimal(qcQty), new BigDecimal(totalQty));
                qcResultEntity.setQcSampleRate(qcSampleRate);
            }
            //质检合格量
            Integer qcGoodQty = qcResultEntity.getQcGoodQty() != null ? qcResultEntity.getQcGoodQty() : 0;
            //质检不良量
            Integer qcBadQty = qcResultEntity.getQcBadQty() != null ? qcResultEntity.getQcBadQty() : 0;
            if (qcQty != 0) {
                BigDecimal qcGoodRate = MathUtil.divide(new BigDecimal(qcGoodQty), new BigDecimal(qcQty));
                qcResultEntity.setQcGoodRate(qcGoodRate);
                BigDecimal qcBadRate = MathUtil.divide(new BigDecimal(qcBadQty), new BigDecimal(qcQty));
                qcResultEntity.setQcBadRate(qcBadRate);
            } else {
                qcResultEntity.setQcGoodRate(BigDecimal.ZERO);
                qcResultEntity.setQcBadRate(BigDecimal.ZERO);
            }
        }
    }

    /**
     * 获取到质检信息
     *
     * @param billId
     * @return com.erp.model.wms.dto.QcInfoDTO.ViewDTO
     * @author yl
     * @date 2023-04-19 12:24
     */
    @Override
    public QcResultDTO.ViewDTO getByMainId(String billId) {
        QcResultDTO.ViewDTO qcInfoView = new QcResultDTO.ViewDTO();
        QcResultEntity qcInfo = this.getByBillId(billId);
        if (qcInfo != null) {
            BeanMapper.copy(qcInfo, qcInfoView);
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(qcInfo.getId()));
            List<String> imageUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> nameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            qcInfoView.setBadImageNameList(nameList);
            qcInfoView.setBadImageUrlList(imageUrlList);
            String qcType = qcInfoView.getQcType();
            //不良率
            BigDecimal badRate = qcInfoView.getQcBadRate();
            qcInfoView.setQcBadRate(MathUtil.BigDecimal_100.multiply(badRate));
            BigDecimal goodRate = qcInfoView.getQcGoodRate();
            qcInfoView.setQcGoodRate(MathUtil.BigDecimal_100.multiply(goodRate));

            BigDecimal sampleRate = qcInfoView.getQcSampleRate();
            qcInfoView.setQcSampleRate(MathUtil.BigDecimal_100.multiply(sampleRate));
            qcInfoView.setQcTypeName(QcTypeEnum.getByCode(qcType));
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            //处理措施
            String handleModeDict = qcInfoView.getHandleModeDict();
            String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            qcInfoView.setHandleModeName(handleModeName);

            //处理措施
            String qcProblemDict = qcInfoView.getQcProblemDict();
            String qcProblemName = dictList.stream().filter(d -> d.getValue().equals(qcProblemDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            qcInfoView.setQcProblemName(qcProblemName);
            String qcResult = qcInfoView.getQcResult();
            qcInfoView.setQcResultName(QcResultEnum.getByCode(qcResult));

        }
        return qcInfoView;
    }

    /**
     * 根据采购订单id集合 获取到已质检的数量
     *
     * @param purOrderIds
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.QcQtyDTO>
     * @author yl
     * @date 2023-04-20 12:59
     */
    @Override
    public List<QcResultDTO.QcQtyDTO> getPurOrderIds(List<String> purOrderIds) {
        if (CollectionUtils.isEmpty(purOrderIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getByPurOrderIds(purOrderIds);
    }

    @Override
    public List<QcResultEntity> getByMainIdList(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return this.lambdaQuery().in(QcResultEntity::getMainId, ids).list();
        }
        return Collections.emptyList();
    }


    /**
     * 批量免检后 批量去更新 数量
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2023-04-20 17:07
     */
    @Override
    public void updateQcQty(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            LambdaUpdateWrapper<QcResultEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(QcResultEntity::getQcBadQty, 0);
            updateWrapper.set(QcResultEntity::getQcGoodQty, 0);
            updateWrapper.set(QcResultEntity::getQcQty, 0);
            updateWrapper.set(QcResultEntity::getQcBadRate, 0);
            updateWrapper.set(QcResultEntity::getQcGoodRate, 0);
            updateWrapper.set(QcResultEntity::getQcSampleRate, 0);
            updateWrapper.in(QcResultEntity::getMainId, ids);
            this.update(updateWrapper);
        }

    }


    /**
     * 更新处理措施
     *
     * @param mainIds
     * @param handleModeDict
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 19:17
     */
    @Override
    public Boolean updateHandleMode(List<String> mainIds, String handleModeDict) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return false;
        }
        LambdaUpdateWrapper<QcResultEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.in(QcResultEntity::getMainId, mainIds);
        updateWrapper.set(QcResultEntity::getHandleModeDict, handleModeDict);
        return this.update(updateWrapper);
    }


    /**
     * 根据质检单id集合 获取到一些需要入库的数据
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.StockInDTO>
     * @author yl
     * @date 2023-04-24 15:47
     */
    @Override
    public List<QcResultDTO.StockInDTO> getStockIn(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.getStockIn(mainIdList);
    }


    /**
     * 根据质检单id 集合 获取删除数据
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-04-25 16:15
     */
    @Override
    public void removeByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            LambdaQueryWrapper<QcResultEntity> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(QcResultEntity::getMainId, mainIdList);
            this.remove(queryWrapper);
        }

    }


    /**
     * 根据质检单id 获取到质检信息
     *
     * @param billId
     * @return com.erp.model.wms.entity.QcInfoEntity
     * @author yl
     * @date 2023-04-19 12:26
     */
    private QcResultEntity getByBillId(String billId) {
        LambdaQueryWrapper<QcResultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QcResultEntity::getMainId, billId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }


    /**
     * 发送质检结果消息
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-04-27 19:24
     */
    @Override
    @Async
    public void sendQcResultMsg(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        //根据主表id 获取到发送质检的信息
        List<QcResultDTO.QcNoticeDTO> list = baseMapper.listQcResultMsg(ids);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.HANDLE_MODE_TYPE.getKey()));
        List<String> skuIdList = list.stream().map(QcResultDTO.QcNoticeDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //采购订单id
        List<String> poIds = list.stream().map(QcResultDTO.QcNoticeDTO::getPurchaseOrderId).collect(Collectors.toList());
        List<PurchaseOrderEntity> poList = scmTaskFeign.listPurchaseOrderByIds(poIds);

        String userName = commonService.getUserInfo().getUserName();
        for (QcResultDTO.QcNoticeDTO item : list) {
            String qcType = item.getQcType();
            String qcTypeName = QcTypeEnum.getByCode(qcType);
            item.setQcTypeName(qcTypeName);
            item.setUserName(userName);
            String handleModeName = dictList.stream().filter(r -> r.getValue().equals(item.getHandleModeDict())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setHandleModeName(handleModeName);
            String skuName = skuVOList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            item.setSkuName(skuName);
            Boolean isFirstMassProduct = poList.stream().filter(p -> p.getId().equals(item.getPurchaseOrderId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getIsFirstMassProduct())).orElse(Boolean.FALSE);
            item.setIsFirstMassProduct(isFirstMassProduct);
        }
        //以新 老品分组
        Map<Boolean, List<QcResultDTO.QcNoticeDTO>> map = list.stream().collect(Collectors.groupingBy(QcResultDTO.QcNoticeDTO::getIsFirstMassProduct));
        for (Map.Entry<Boolean, List<QcResultDTO.QcNoticeDTO>> entry : map.entrySet()) {
            //是否新品 true 是
            Boolean isFirstMassProduct = entry.getKey();
            List<QcResultDTO.QcNoticeDTO> value = entry.getValue();
            sendMsg(isFirstMassProduct, value);
        }

    }

    /**
     * 发送消息 根据新老品
     * @param isFirstMassProduct
     */
    private void sendMsg(Boolean isFirstMassProduct, List<QcResultDTO.QcNoticeDTO> list) {
        //老品质检
        String qcNewProductCode = NoticeNodeEnum.QC_OLD_PRODUCT.getCode();
        //表示新品
        if(isFirstMassProduct){
            qcNewProductCode=NoticeNodeEnum.QC_NEW_PRODUCT.getCode();
        }
        List<String> skuIdList = list.stream().map(QcResultDTO.QcNoticeDTO::getSkuId).collect(Collectors.toList());

        List<NoticeReceiverDTO.InfoDTO> receiverList = sysUserFeign.listNoticeReceiverByNodeKey(qcNewProductCode);
        if (CollectionUtils.isEmpty(receiverList)) {
            return;
        }
        //根据sku 获取角色的
        List<ProductInfoDTO.ProductRolePeopleDTO> rolePeopleList = new ArrayList<>();

        List<String> userIdList = new ArrayList<>();
        //这个是项目角色
        String itemRole = NoticeReceiverEnum.ITEM_ROLE.getCode();
        //其它人员
        String otherPeople = NoticeReceiverEnum.OTHER_PEOPLE.getCode();
        List<String> otherUsers = receiverList.stream().filter(r -> otherPeople.equals(r.getReceiverType())).
                map(NoticeReceiverDTO.InfoDTO::getReceiverValue).collect(Collectors.toList());

        userIdList.addAll(otherUsers);
        //这个是项目角色的
        List<String> itemRoles = receiverList.stream().filter(r -> itemRole.equals(r.getReceiverType())).
                map(NoticeReceiverDTO.InfoDTO::getReceiverValue).collect(Collectors.toList());
        //当不为空
        if (CollectionUtils.isNotEmpty(itemRoles)) {
            //根据sku 获取角色的
            rolePeopleList = plmTaskFeign.listProductRolePeople(skuIdList);
        }

        //项目经理
        String itemCharge = NoticeItemRoleEnum.ITEM_MANAGER.getCode();
        //产品经理
        String productCharge = NoticeItemRoleEnum.PRODUCT_MANAGER.getCode();
        //是不是 包含项目经理
        Boolean isItemCharge = itemRoles.contains(itemCharge);
        //是不是 包含产品经理
        Boolean isProductCharge = itemRoles.contains(productCharge);
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        for (QcResultDTO.QcNoticeDTO item : list) {
            String msgHead = String.format(NoticeMsgConstant.QC_RESULT_HEAD, item.getUserName(), item.getSkuNo(), item.getQcTypeName());
            String msgContent = String.format(NoticeMsgConstant.QC_RESULT_CONTENT, item.getPurchaseOrderCode(),
                    item.getSkuName(), item.getQcUserName(), item.getQcFinishTime(), item.getHandleModeName());
            String skuId = item.getSkuId();
            //对应产品人员
            List<ProductInfoDTO.ProductRolePeopleDTO> peopleList = rolePeopleList.stream().filter(r -> r.getSkuId().equals(skuId)).collect(Collectors.toList());
            for (ProductInfoDTO.ProductRolePeopleDTO people : peopleList) {
                if (isItemCharge) {
                    userIdList.addAll(people.getProjectChargeIdList());
                }
                if (isProductCharge) {
                    userIdList.addAll(people.getProductChargeIdList());
                }
            }
            userIdList = userIdList.stream().filter(u -> StringUtils.isNotBlank(u)).collect(Collectors.toList());
            NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
            noticeMsgInfoDTO.setReceiverUserIds(userIdList);
            noticeMsgInfoDTO.setTitle(msgHead);
            noticeMsgInfoDTO.setContent(msgContent);
            noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                    noticeMsgInfoDTO, IdUtil.simpleUUID());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                log.error("消息发送结果失败：{}", JSONObject.toJSONString(result));
            }

        }
    }


}
