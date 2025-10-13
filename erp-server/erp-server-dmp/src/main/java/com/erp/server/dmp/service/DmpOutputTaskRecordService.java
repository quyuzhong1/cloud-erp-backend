package com.erp.server.dmp.service;

import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;

import java.util.List;

/**
 * <p>
 * 推送任务记录 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpOutputTaskRecordService extends SuperService<DmpOutputTaskRecordEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpOutputTaskRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpOutputTaskRecordDTO.UpdateDTO dto);



    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 14:53
     * @param dto
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>
     **/
    List<DmpOutputTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:35
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskDTO.PagingDTO>
     **/
    PagingVO<DmpOutputTaskRecordDTO.PagingDTO> paging(PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2024/9/5 17:30
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean exportExcel(DmpOutputTaskRecordDTO.ExpotParamDTO dto);

    /**
     * 无需同步
     * @Author Luo_WG
     * @Date 2024/9/5 16:30
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean batchNoNeedSync(List<String> ids , String remark);

    /**
     * 加入黑名单
     * @Author Luo_WG
     * @Date 2024/9/5 11:36
     * @param dto
     * @return java.lang.Object
     **/
    Boolean addOutputBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto);

    /**
     * 取消黑名单
     * @Author Luo_WG
     * @Date 2024/9/11 19:21
     * @param id
     * @return java.lang.Boolean
     **/
    BatchResultDTO cancelOutputBlack(String id);

    /**
     * 导出Excel数据解析
     * @Author Luo_WG
     * @Date 2024/9/5 17:39
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingDTO>
     **/
    PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportNewDmpPushTask(PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto);

    /**
     * 重新同步（批量同步）
     * @Author Luo_WG
     * @Date 2024/9/6 15:58
     * @param dmpOutputTaskRecordEntityList
     * @return java.lang.Boolean
     **/
    Boolean batchSync(List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList);
    
    /**
     * erp推送查询同步
     * @param dmpCfgOutputEntity
     * @param dmpCfgInputEntity
     * @param list
     */
    List<DmpOutputTaskRecordEntity> erpQuerySync(DmpCfgOutputEntity dmpCfgOutputEntity , List<DmpOutputTaskRecordEntity> list);
    /**
     * 获取推送任务数据
     * @author will
     * @date 2024/10/22 9:07
     * @param oneDTO
     * @return SyncInfoDTO
     */
    DmpPushTaskDTO.SyncInfoDTO getErrorData(DmpSyncTaskDTO.OneDTO oneDTO);
    
    List<DmpOutputTaskRecordEntity> getOutputErrorTask(String systemId , String size);
    
    void dmpInputMoveToHistoryTable(String beforeUpdateTime , String size);
    
    void dmpRelationMoveToHistoryTable(String beforeUpdateTime , String size);
    
    void dmpOutputMoveToHistoryTable(String beforeUpdateTime , String size);
    
    void dmpOutputNoRecordMoveToHistoryTable();

    List<DmpOutputTaskRecordEntity> queryBySourceCodeAndCfgOutputId(String sourceCode, String cfgOutputId);
    /**
     * 根据来源编码和输出类获取输出任务记录
     * @param sourceCode
     * @param outputClass
     * @return
     */
    List<DmpOutputTaskRecordEntity> getOutputTaskRecord(String sourceCode, String outputClass);

    List<DmpOutputTaskRecordEntity> getOutputTaskByIdAndType(List<String> sourceIdList, String sourceType);

    List<String> outputErrorCountMsg();


    DmpPushTaskDTO.SyncInfoDTO getSuccessData(DmpSyncTaskDTO.OneDTO oneDTO);

    PagingVO<DmpOutputTaskRecordDTO.PagingViewDTO> pagingOutLatest(PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto);
    /**
     * 获取最后一条拉取记录
     * @author will
     * @date 2025/8/27 18:24
     * @param paramDTO
     * @return LastPullDTO
     */
    DmpPushTaskDTO.LastPullDTO getLastPullRecord(DmpPushTaskDTO.LastPullParamDTO paramDTO);

    Boolean batchNoNeedSyncBySourceCode(List<String> sourceCodeList, String remark);
}
