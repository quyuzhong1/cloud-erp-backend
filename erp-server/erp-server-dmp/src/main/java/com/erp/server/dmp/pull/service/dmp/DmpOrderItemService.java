package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.plm.dto.NewProductDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单商品服务类
 */
public interface DmpOrderItemService extends IService<DmpOrderItemEntity> {
    /**
     * 添加订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    Boolean add(DmpOrderItemEntity dmpOrderInfoEntity);

    /**
     * 批量添加订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<DmpOrderItemEntity> dmpOrderInfoEntityList);

    /**
     * 根据erp平台商品id查询订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 22:11
     * @param erpOrderItemId erp平台商品id
     * @return com.erp.model.dmp.entity.DmpOrderItemEntity
     **/
    DmpOrderItemEntity getByErpOrderItemId(String erpOrderItemId);

    /**
     * 根据订单表id查询订单商品信息
     * @Author Luo_WG
     * @Date 2022/12/14 16:10
     * @param orderId 订单表id
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    List<DmpOrderItemEntity> getByOrderId(String orderId);

    /**
     * 根据erp平台商品id修改订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    Boolean updateOrderItemByErpOrderItemId(DmpOrderItemEntity dmpOrderItemEntity);

    /**
     * 校验订单商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void checkOrderItem(List<DmpOrderItemEntity> orderItem, LocalDate platformCreateTime);

    /**
     * 同步PLM的到货时间更新新老品
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void updateNewSign(Map<String,List<NewProductDTO>> dto);
}
