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
        if(!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单表" , dmpSoInfoEntity.getId());
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单表"));
        DmpSoInfoEntity dmpSoInfoEntity =  BeanMapperUtils.map(DmpSoInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoInfoEntity);
        log.info("编辑 开始修改中台销售订单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoInfoEntity);
        if(!save) {
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
    public void addGyyOrder(DmpSoInfoDTO.addGyyOrderDTO dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Integer page = 0;
        while(true) {
            List<GyyOrderEntity> mongoData = mongoService.findMongoData(dto, page, 1000, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
            if (CollUtil.isEmpty(mongoData)) {
                return;
            }
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
                entity.setSubsidyAmount(gyyOrderEntity.getDiscountFee());
                entity.setDeliveryTime(null);
                List<DeliverysBean> deliverys = gyyOrderEntity.getDeliverys();
                if(CollUtil.isNotEmpty(deliverys)) {
                    entity.setLogisticsCode(deliverys.get(0).getMailNo());
                }
                entity.setLogisticsName(gyyOrderEntity.getExpressName());
                if (gyyOrderEntity.getApprove()) {
                    entity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
                } else {
                    entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                }
                BigDecimal discount = gyyOrderEntity.getDetails().stream().map(req -> req.getDiscount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal platDiscountAmount = gyyOrderEntity.getDetails().stream().map(req -> req.getPlatDiscountAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                entity.setTotalDiscount(discount.add(platDiscountAmount));
                BigDecimal totalCancelGoodsAmount = gyyOrderEntity.getDetails().stream()
                        .filter(req -> req.getRefund() == 1 || req.getCancel())
                        .map(req -> req.getCostPrice().multiply(MathUtil.valueOf(req.getQty())))
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                entity.setTotalCancelGoodsAmount(totalCancelGoodsAmount);
                entity.setCancelGoodsCurrency("CNY");
                this.save(entity);

                dmpSoDetailService.saveBatch(detailHandler(gyyOrderEntity.getDetails(), mainId));
            }



            page++;
        }
//        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "original_mabang_order");
    }

    private List<DmpSoDetailEntity> detailHandler(List<DetailsBean> detailsBeans, String mainId) {
        List<DmpSoDetailEntity> list = new ArrayList<>();
        for (DetailsBean detailsBean : detailsBeans) {
            DmpSoDetailEntity dmpSoDetailEntity = new DmpSoDetailEntity();
            dmpSoDetailEntity.setMainId(mainId);
//            dmpSoDetailEntity.setThirdDetailId();

        }
        return list;
    }
}
