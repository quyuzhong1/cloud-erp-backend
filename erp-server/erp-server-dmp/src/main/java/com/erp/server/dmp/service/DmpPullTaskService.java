package com.erp.server.dmp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;

import java.time.LocalDateTime;
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
     * @description: 列表tab
     * @author Will
     * @date: 2023/10/17 14:38
     * @param dto
     * @return List<TabListDTO>
     */
    List<DmpPullTaskDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/17 14:38
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<DmpPullTaskDTO.ListDTO> paging(PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/10/17 14:38
     */
    Boolean exportExcel(DmpPullTaskDTO.ParamDTO dto);
    /**
     * @description: 批量同步
     * @author Will
     * @date: 2023/10/17 14:38
     * @param ids
     * @return Boolean
     */
    Boolean batchSync(List<String> ids);

    /**
     * @param ids
     * @return Boolean
     * @description: 批量修改无需同步
     * @author hyj
     * @date 2024/4/11 16:51
     */
    Boolean batchNoNeedSync(List<String> ids);

    /**
     * @description: 预警
     * @author Will
     * @date: 2023/11/17 11:53
     * @param syncTaskId
     */
    void sendWarnMsg(String syncTaskId);

    /**
     * 批量更新或保存
     */
    List<DmpPullTaskEntity> batchCheckSaveAndUpdate(List<DmpPullTaskEntity> entityList, String platform, String code, String targetPlatform, String topic, String tag);

    /**
     * 批量查询
     */
    List<DmpPullTaskEntity> findList(String platform, String sourceType, String targetPlatform, String topic, String tag, List<String> sourceIds);

    /**
     * 删除已归档数据
     */
    void deleteByIds(List<String> ids);

    int countMonth(LocalDateTime date);

    List<DmpPullTaskEntity> listMonth(LocalDateTime date, int pageSize, int effect);

    /**
     * 获取飞书预警信息需要推送的(PullTask任务记录)
     * @return
     */
    List<DmpPullTaskEntity> getWarnPullTaskList(List<String> statusList);

    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTask(PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
}
