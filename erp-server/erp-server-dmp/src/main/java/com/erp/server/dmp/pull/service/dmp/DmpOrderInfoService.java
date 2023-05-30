package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.vo.CleanAmountAfterVO;
import com.erp.model.sys.dto.SysUserDeptDTO;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务类
 */
public interface DmpOrderInfoService extends IService<DmpOrderInfoEntity> {
    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    String add(DmpOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 根据平台订单id查询订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param platformOrderId 平台订单id
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    DmpOrderInfoEntity getOrderByPlatformOrderId(String platformOrderId);

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    Boolean updateOrderByPlatformOrderId(DmpOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 校验订单在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    String checkOrder(DmpOrderInfoEntity orderInfoEntity);

    /**
     * 清洗订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void cleanOrder(Integer pageSize);

    /**
     * 根据平台订单id查询订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param salesRecordNumber 销售订单号
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    DmpOrderInfoEntity getOrderBySalesRecordNumber(String salesRecordNumber, String platformOrderId, String platformSign);

    /**
     * 更新清洗数据
     * @param userDeptList
     * @param dmpOrderInfoEntity
     */
    void cleanDmpOrderInfo(List<SysUserDeptDTO> userDeptList, DmpOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 获取需要清理订单列表
     * @return
     */
    List<CleanAmountAfterVO> getCleanOrderList();

}
