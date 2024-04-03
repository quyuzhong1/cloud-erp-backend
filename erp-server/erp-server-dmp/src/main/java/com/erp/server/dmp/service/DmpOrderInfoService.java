package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.enums.SalesDataReportEnum;
import com.erp.model.dmp.vo.CleanAmountAfterVO;
import com.erp.model.dmp.vo.SyncDataReportVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 订单服务类
 */
public interface DmpOrderInfoService extends IService<DmpOrderInfoEntity> {
    /**
     * 添加订单信息
     *
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    String add(DmpOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 根据平台订单id查询订单信息
     *
     * @param platformOrderId 平台订单id
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     **/
    DmpOrderInfoEntity getOrderByPlatformOrderId(String platformOrderId);

    /**
     * 根据平台订单id修改订单信息
     *
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     **/
    Boolean updateOrderByPlatformOrderId(DmpOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 根据id删除订单记录
     *
     * @param ids
     * @return
     */
    Boolean removeOrderByIds(List<String> ids);

    /**
     * 根据id删除订单记录
     *
     * @param codes
     * @return
     */
    Boolean removeOrderByCode(List<String> codes);

    /**
     * 校验订单在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    String checkOrder(DmpOrderInfoEntity orderInfoEntity);

    /**
     * 清洗订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    void cleanOrder(Integer pageSize);

    /**
     * 根据平台订单id查询订单信息
     *
     * @param salesRecordNumber 销售订单号
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     **/
    DmpOrderInfoEntity getOrderBySalesRecordNumber(String salesRecordNumber, String platformOrderId, String platformSign);

    /**
     * 更新清洗数据
     *
     * @param userDeptList
     * @param dmpOrderInfoEntity
     */
    void cleanDmpOrderInfo(List<SysUserDeptDTO> userDeptList, DmpOrderInfoEntity dmpOrderInfoEntity, List<CustomerDTO.SellerUserDeptDTO> sellerUserDeptDTOS);

    /**
     * 获取需要清理订单列表
     *
     * @return
     */
    List<CleanAmountAfterVO> getCleanOrderList();

    /**
     * 同步销售函数到物理库
     *
     * @return
     */
    CompletableFuture<SyncDataReportVO> salesDataToPhysical(SalesDataReportEnum reportEnum);
}
