package com.erp.server.dmp.service;

import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 中台同步任务表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
public interface DmpPullTaskService extends SuperService<DmpPullTaskEntity> {


    /**
     * 更新出入库同步信息（成功/失败）
     *
     * @param id
     * @param syncStatus
     * @param responseMsg
     */
    void updateSyncInfo(String id, String syncStatus, String responseMsg);

    /**
     * @param dmpSyncTaskEntity
     * @description: 新增或修改任务数据
     * @author Will
     * @date: 2023/6/30 15:40
     */
    void saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpSyncTaskEntity);

    /**
     * 新增同步金蝶退货单到wms退货入库单的任务
     *
     * @param entity
     * @return void
     * @Author Luo_WG
     * @Date 2023/7/4 19:48
     **/
    void syncKingdeeReturnOrderToWms(KingdeeReturnOrderEntity entity);

    /**
     * 根据Map条件查询金蝶数据
     *
     * @param conditon 查询条件
     *                 支持：id，is_deleted，source_type，source_code，source_id，status，mq_tag，return_msg
     *                 注：lastSql 用于表示扩展SQL
     * @return 返回Mq_data中的金蝶列表
     */
    List<String> listKingdeeCode(Map<String, Object> conditon);

    /**
     * 处理oms推送订单审核消息
     *
     * @param resultMap
     */
    void syncOmsOrderToDmp(Map<String, Object> resultMap);
    /**
     * 处理oms推送出库订单审核消息
     *
     * @param resultMap
     */
    void syncWmsOutStockToDmp(Map<String, Object> resultMap);
    /**
     * 处理oms推送入库订单审核消息
     *
     * @param resultMap
     */
    void syncOmsReturnToDmp(Map<String, Object> resultMap);
    /**
     * 发送mq并保存任务
     * @param dto
     */
    Boolean sendMqAndSaveTask(DmpPullTaskFeignDTO dto);

    /**
     * 发送mq并保存任务
     * @param dto
     */
    String savePullTask(DmpPullTaskFeignDTO dto);
}
