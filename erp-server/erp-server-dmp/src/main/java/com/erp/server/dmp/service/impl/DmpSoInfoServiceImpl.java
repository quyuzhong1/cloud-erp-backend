package com.erp.server.dmp.service.impl;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.model.wms.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.service.DmpSoDetailService;
import org.apache.commons.collections4.CollectionUtils;
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


    /**
    * 数帝云线上字段映射
    */
    public List<ShudiyunB2cOrderDTO> shudiyunFieldDmpOrderHandler(String thirdCode) {
        DmpSoInfoEntity dmpSoInfoEntity = lambdaQuery().eq(DmpSoInfoEntity::getThirdCode, thirdCode).last("LIMIT 1").one();
        if (ObjectUtil.isEmpty(dmpSoInfoEntity)) {
            return Collections.emptyList();
        }

        List<ShopInfoEntity> list = FeignQuery.create(ShopInfoEntity.class).eq(ShopInfoEntity::getId, dmpSoInfoEntity.getShopId()).last("LIMIT 1").list();


        //优惠额
        BigDecimal totalDiscount = dmpSoInfoEntity.getTotalDiscount();

        //数帝云数据结构
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();

        //B2C订单详情
        List<DmpSoDetailEntity> dmpSoDetailEntities = dmpSoDetailService.listByMainId(dmpSoInfoEntity.getId());
        for (int i = 0; i < dmpSoDetailEntities.size(); i++) {
            DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntities.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
            shudiyunB2cOrderDTO.setTransaction_unique_key(dmpSoInfoEntity.getId() + dmpSoDetailEntity.getId());

            shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getThirdCode());
            shudiyunB2cOrderDTO.setBiz_time(dmpSoInfoEntity.getPayTime());
            shudiyunB2cOrderDTO.setTransaction_type("100.10");
            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getCode());
            shudiyunB2cOrderDTO.setBiz_status(dmpSoInfoEntity.getDeliveryStatus());

            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(dmpSoInfoEntity.getAllAmount());
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(dmpSoInfoEntity.getTotalDiscount());

            Integer totalQty = dmpSoDetailEntities.stream().mapToInt(DmpSoDetailEntity::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty);

            //取消金额、数量
            if (dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                    || dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.SHOPEE.getCode())
            ) {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getTotalCancelGoodsAmount());
            } else {
                if (dmpSoInfoEntity.getIsCancel()) {
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getAllAmount());

                    // 取消商品数量（合计）
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
                }
            }

            shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPayAmount());
            shudiyunB2cOrderDTO.setTotal_freight(dmpSoInfoEntity.getShippingAmount());
            // todo 收款组织：暂无数据需要新增(必填字段)
            shudiyunB2cOrderDTO.setSales_company_code("");
            // todo 收款组织：暂无数据需要新增(必填字段)
            shudiyunB2cOrderDTO.setReceiving_company_code("");
            // todo 财务组织名称：暂无数据需要新增(必填字段)
            shudiyunB2cOrderDTO.setOrganization_name("");
            // todo 财务组织编码：暂无数据需要新增(必填字段)
            shudiyunB2cOrderDTO.setOrganization_code("");
            shudiyunB2cOrderDTO.setPlatform_id(dmpSoInfoEntity.getSourceSystem());
            shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoInfoEntity.getSourcePlatform()));
            shudiyunB2cOrderDTO.setShop_no(dmpSoInfoEntity.getShopId());
            shudiyunB2cOrderDTO.setShop_name(dmpSoInfoEntity.getShopName());
            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getThirdCode());
            shudiyunB2cOrderDTO.setRoot_node_create_time(dmpSoInfoEntity.getPayTime());
            shudiyunB2cOrderDTO.setRoot_node_modify_time(dmpSoInfoEntity.getPlatformUpdateTime());
            shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setGoods_name(dmpSoDetailEntity.getSkuName());
            shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
            shudiyunB2cOrderDTO.setSpec_name("");
            if (dmpSoDetailEntity.getIsGift()) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }

            shudiyunB2cOrderDTO.setRemark(dmpSoDetailEntity.getItemRemark());

            // 商品状态
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getDeliveryStatus())) {
                shudiyunB2cOrderDTO.setGoods_status("10.10");
            }

            if (dmpSoInfoEntity.getIsCancel()) {
                shudiyunB2cOrderDTO.setGoods_status("10.20");
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getQty());
            shudiyunB2cOrderDTO.setUnit(dmpSoDetailEntity.getProductUnit());

            shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount());
            if (dmpSoDetailEntity.getAfterAmount().compareTo(BigDecimal.ZERO) > 0) {
                //获得分摊的商品优惠额
                BigDecimal shareDiscount = dmpSoDetailEntity.getAfterAmount().divide(dmpSoInfoEntity.getAllAmount(), 4, RoundingMode.HALF_UP).multiply(totalDiscount);
                //计算为真实售价(原始币别)-商品分摊优惠/订单数量
                if (dmpSoDetailEntities.size() == i-1) {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(dmpSoInfoEntity.getAllAmount()).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.HALF_UP));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(dmpSoInfoEntity.getAllAmount()));
                } else {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.HALF_UP));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount));
                }
                totalDiscount = totalDiscount.subtract(shareDiscount);
            }

            shudiyunB2cOrderDTO.setTransaction_currency_code(dmpSoDetailEntity.getCurrencyCode());

            shudiyunB2cOrderDTO.setPost_amount(dmpSoInfoEntity.getShippingAmount());
            shudiyunB2cOrderDTO.setMsku_code(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuName());
            shudiyunB2cOrderDTO.setSku_code("");
            shudiyunB2cOrderDTO.setSku_name("");

            // todo 店铺结算币种代码：暂无数据需要新增(必填字段)
            shudiyunB2cOrderDTO.setSettlement_currency_code("");
            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getPlatformCode());

            shudiyunB2cOrderDTOList.add(shudiyunB2cOrderDTO);

        }
        return shudiyunB2cOrderDTOList;
    }


}
