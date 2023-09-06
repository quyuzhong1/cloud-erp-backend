package com.erp.server.dmp.service;

import com.common.business.dto.DmpSyncTaskDTO;
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
     * @param id
     * @param syncStatus
     * @param responseMsg
     */
    void updateSyncInfo(String id, String syncStatus, String responseMsg);

    /**
     * @description: 新增或修改任务数据
     * @author Will
     * @date: 2023/6/30 15:40
     * @param dmpPullTaskEntity
     */
    String saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpPullTaskEntity);

    /**
     * 新增同步金蝶退货单到wms退货入库单的任务
     * @Author Luo_WG
     * @Date 2023/7/4 19:48
     * @param entity
     * @return void
     **/
    void syncKingdeeReturnOrderToWms(KingdeeReturnOrderEntity entity);

    /**
     * 根据Map条件查询金蝶数据
     * @param conditon 查询条件
     *                 支持：id，is_deleted，source_type，source_code，source_id，status，mq_tag，return_msg
     *                 注：lastSql 用于表示扩展SQL
     * @return 返回Mq_data中的金蝶列表
     */
    List<String> listKingdeeCode(Map<String, Object> conditon);

    /**
     * 统一发送MQ消息并保存任务
     * @param dto
     */
    void sendMqAndSaveTask(DmpSyncTaskDTO dto);
}
