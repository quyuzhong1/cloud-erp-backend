package com.erp.server.dmp.service.impl;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.bean.DeliverysBean;
import com.erp.model.dmp.gyy.bean.DetailsBean;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoReceiverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSoInfoDTO;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.server.dmp.mapper.DmpSoInfoMapper;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * <p>
 * 中台销售订单表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
@Slf4j
@Service
public class DmpSoInfoServiceImpl extends SuperServiceImpl<DmpSoInfoMapper, DmpSoInfoEntity> implements DmpSoInfoService {

    @Resource
    private DmpSoDetailService dmpSoDetailService;
    @Resource
    private DmpSoReceiverService dmpSoReceiverService;
    @Resource
    private MongoService mongoService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoInfoDTO.AddDTO addDTO) {
        DmpSoInfoEntity dmpSoInfoEntity = new DmpSoInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoInfoEntity);

        // 数据处理
        handleData(dmpSoInfoEntity);

        log.info("开始新增中台销售订单表");
        boolean save = super.save(dmpSoInfoEntity);
        if (!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单表", dmpSoInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoInfoEntity.getId(), dmpSoInfoEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoInfoDTO.UpdateDTO updateDTO) {
        DmpSoInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单表"));
        DmpSoInfoEntity dmpSoInfoEntity = BeanMapperUtils.map(DmpSoInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoInfoEntity);
        log.info("编辑 开始修改中台销售订单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoInfoEntity);
        if (!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录中台销售订单表日志数据，id：【{}】", dmpSoInfoEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoInfoEntity.getId(), "中台销售订单表");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DmpSoInfoEntity dmpSoInfoEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public void addGyyOrder(List<GyyOrderEntity> mongoData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (GyyOrderEntity gyyOrderEntity : mongoData) {
            DmpSoInfoEntity entity = new DmpSoInfoEntity();
            if (!"销售订单".equals(gyyOrderEntity.getOrderTypeName())) {
                continue;
            }
            String mainId = IdWorker.getIdStr();
            entity.setId(mainId);
            if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getCreatetime())) {
                entity.setPlatformCreateTime(LocalDateTime.parse(gyyOrderEntity.getCreatetime(), formatter));
            }
            if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getModifytime())) {
                entity.setPlatformUpdateTime(LocalDateTime.parse(gyyOrderEntity.getModifytime(), formatter));
            }
            entity.setSourceSystem("gyy");
            entity.setSourcePlatform("gyy");
            entity.setThirdCode(gyyOrderEntity.getCode());
            entity.setPlatformCode(gyyOrderEntity.getPlatformCode());
            entity.setInvalidStatus(gyyOrderEntity.getCancle());
            entity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            if (gyyOrderEntity.getDeliveryState() == 1) {
                entity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_PARTIAL_SHIPPED.getCode());
            } else if (gyyOrderEntity.getDeliveryState() == 2) {
                entity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else {
                entity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            }

            if (gyyOrderEntity.getRefundState() == 1) {
                entity.setReturnStatus(DmpOrderReturnStatusEnum.PARTIAL_RETURN.getCode());
            } else if (gyyOrderEntity.getDeliveryState() == 2) {
                entity.setReturnStatus(DmpOrderReturnStatusEnum.ORDER_RETURN.getCode());
            } else {
                entity.setReturnStatus(DmpOrderReturnStatusEnum.NOT_RETURN.getCode());
            }

            entity.setPlatformOriginalStatus(String.valueOf(gyyOrderEntity.getDeliveryState()));
            entity.setShopId(gyyOrderEntity.getShopCode());
            entity.setShopName(gyyOrderEntity.getShopName());
            entity.setSellRemark(gyyOrderEntity.getSellerMemo());
            entity.setBuyerRemark(gyyOrderEntity.getBuyerMemo());
            if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getPaytime())) {
                entity.setPayTime(LocalDateTime.parse(gyyOrderEntity.getPaytime(), formatter));
            }
            entity.setPayStatus(Boolean.TRUE);
            entity.setPayMethod("");
            entity.setCurrencyCode(CurrencyEnum.CNY.getCurrencyCode());
            entity.setExchangeRate(BigDecimal.ZERO);
            entity.setPayAmount(gyyOrderEntity.getPayment());
            entity.setAllAmount(gyyOrderEntity.getPaymentAmount());
            entity.setShippingAmount(gyyOrderEntity.getPostFee());
            entity.setPlatformCost(gyyOrderEntity.getOtherServiceFee());
            entity.setDeliveryTime(null);
            List<DeliverysBean> deliverys = gyyOrderEntity.getDeliverys();
            if (CollUtil.isNotEmpty(deliverys)) {
                entity.setLogisticsCode(deliverys.get(0).getMailNo());
            }
            entity.setLogisticsName(gyyOrderEntity.getExpressName());
            if (gyyOrderEntity.getApprove()) {
                entity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            } else {
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            entity.setTotalDiscount(gyyOrderEntity.getDiscountFee());
            BigDecimal totalCancelGoodsAmount = gyyOrderEntity.getDetails().stream()
                    .filter(req -> req.getRefund() == 1 || req.getCancel())
                    .map(req -> req.getPrice().multiply(MathUtil.valueOf(req.getQty())))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            entity.setTotalCancelGoodsAmount(totalCancelGoodsAmount);
            entity.setCancelGoodsCurrency("CNY");
            try {
                this.save(entity);
            } catch (Exception e) {
                log.error("数据：" + JSONUtil.toJsonStr(entity));
                e.printStackTrace();
                return;
            }
            try {
                dmpSoReceiverService.save(this.receiverHandler(gyyOrderEntity, mainId));
            } catch (Exception e) {
                log.error("数据：" + JSONUtil.toJsonStr(gyyOrderEntity));
                e.printStackTrace();
                return;
            }
            try {
                dmpSoDetailService.saveBatch(this.detailHandler(gyyOrderEntity.getDetails(), mainId));
            } catch (Exception e) {
                log.error("数据：" + JSONUtil.toJsonStr(entity));
                e.printStackTrace();
                return;
            }

        }
    }

    private List<DmpSoDetailEntity> detailHandler(List<DetailsBean> detailsBeans, String mainId) {
        List<DmpSoDetailEntity> list = new ArrayList<>();
        for (DetailsBean detailsBean : detailsBeans) {
            DmpSoDetailEntity dmpSoDetailEntity = new DmpSoDetailEntity();
            dmpSoDetailEntity.setMainId(mainId);
            dmpSoDetailEntity.setThirdDetailId(detailsBean.getItemCode());
            dmpSoDetailEntity.setSkuNo(detailsBean.getItemCode());
            dmpSoDetailEntity.setPlatformSku(detailsBean.getItemCode());
            dmpSoDetailEntity.setQty(detailsBean.getQty());
            dmpSoDetailEntity.setSkuName(detailsBean.getItemName());
            dmpSoDetailEntity.setIsGift(detailsBean.getIsGift());
            dmpSoDetailEntity.setItemRemark(detailsBean.getNote());
            dmpSoDetailEntity.setExchangeRate(detailsBean.getExchangeRate());
            dmpSoDetailEntity.setSellPriceOrigin(detailsBean.getPrice());
            dmpSoDetailEntity.setDiscountAmount(detailsBean.getDiscountFee());
            dmpSoDetailEntity.setSellPrice(detailsBean.getPrice().subtract((detailsBean.getDiscountFee().divide(MathUtil.valueOf(detailsBean.getQty()), 4, RoundingMode.DOWN))));
            dmpSoDetailEntity.setAfterAmount(detailsBean.getAmountAfter());
            dmpSoDetailEntity.setShippingCost(detailsBean.getPostFee());
            dmpSoDetailEntity.setCurrencyCode("CNY");
            list.add(dmpSoDetailEntity);
        }
        return list;
    }

    private DmpSoReceiverEntity receiverHandler(GyyOrderEntity gyyOrderEntity, String mainId) {
        DmpSoReceiverEntity soReceiverEntity = new DmpSoReceiverEntity();
        soReceiverEntity.setMainId(mainId);
        soReceiverEntity.setCountry("CN");
        soReceiverEntity.setBuyerId(gyyOrderEntity.getVipCode());
        soReceiverEntity.setBuyerName(gyyOrderEntity.getVipName());
        soReceiverEntity.setReceiverName(gyyOrderEntity.getReceiverName());
        soReceiverEntity.setReceiverTelNumber(gyyOrderEntity.getReceiverMobile());
        soReceiverEntity.setPostCode(gyyOrderEntity.getReceiverZip());
        if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getReceiverArea())) {
            String[] split = gyyOrderEntity.getReceiverArea().split("-");
            if (split.length > 0) {
                soReceiverEntity.setProvince(split[0]);
            }
            if (split.length > 1) {
                soReceiverEntity.setCity(split[1]);
            }
            if (split.length > 2) {
                soReceiverEntity.setDistrict(split[2]);
            }
        }
        soReceiverEntity.setFullAddress(gyyOrderEntity.getReceiverAddress());
        soReceiverEntity.setMainStreet(gyyOrderEntity.getReceiverAddress());
        soReceiverEntity.setEmail(gyyOrderEntity.getVipEmail());
        return soReceiverEntity;
    }

}
