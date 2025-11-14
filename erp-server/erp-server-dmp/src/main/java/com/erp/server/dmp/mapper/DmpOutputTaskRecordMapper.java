package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpSyncTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;


/**
 * <p>
 * 推送任务记录 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpOutputTaskRecordMapper extends BaseMapper<DmpOutputTaskRecordEntity> {


    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:05
     * @param permissionSql
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.TabListDTO>
     **/
    List<DmpOutputTaskRecordDTO.TabListDTO> listStatusCount(@Param("permissionSql")String permissionSql);
    
    Integer listStatusCountHis(@Param("permissionSql")String permissionSql);
    
    /**
     * 添加进黑名单的数量
     * @Author Luo_WG
     * @Date 2024/9/11 17:22
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.TabListDTO>
     **/
    Integer listBlackCount(@Param("permissionSql")String permissionSql);

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:35
     * @param params
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingDTO>
     **/
    IPage<DmpOutputTaskRecordDTO.PagingDTO> paging(Page query, @Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);
    
    IPage<DmpOutputTaskRecordDTO.PagingDTO> hisPaging(Page query, @Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);
    
    IPage<DmpOutputTaskRecordDTO.PagingDTO> blackPaging(Page query, @Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);

    /**
     * 导出数据查询
     * @Author Luo_WG
     * @Date 2024/9/5 17:40
     * @param objectPage
     * @param params
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingDTO>
     **/
    Page<DmpOutputTaskRecordDTO.PagingDTO> listExportExcel(Page<Object> objectPage, @Param("params") DmpOutputTaskRecordDTO.ExpotParamDTO params);
    /**
     * 查询新中台推送任务记录
     * @author will
     * @date 2024/10/22 9:13
     * @param params
     * @return SyncInfoDTO
     */
    DmpPushTaskDTO.SyncInfoDTO getErrorData(@Param("params") DmpSyncTaskDTO.OneDTO params);
    
    List<DmpOutputTaskRecordEntity> getOutputErrorTask(@Param("systemId") String systemId , @Param("size") String size);
    
    void dmpOutputMoveToHistoryTable(@Param("conditionSql") String conditionSql);
    
    List<String> getDmpOutputMoveToHistoryTable(@Param("beforeUpdateTime") String beforeUpdateTime , @Param("size") String size);
    
    List<String> getDmpRelationMoveToHistoryTable(@Param("beforeUpdateTime") String beforeUpdateTime , @Param("size") String size);
    
    void dmpOutputNoRecordMoveToHistoryTable(@Param("conditionSql") String conditionSql);
    
    List<String> getDmpOutputNoRecordMoveToHistoryTable();
    
    void dmpRelationMoveToHistoryTable(@Param("fileConditionSql") String fileConditionSql , @Param("dmpConditionSql") String dmpConditionSql);
    
    void dmpInputMoveToHistoryTable(@Param("conditionSql") String conditionSql);

    List<DmpOutputTaskRecordEntity> queryBySourceCodeAndCfgOutputId(@Param("sourceCode") String sourceCode, @Param("cfgOutputId") String cfgOutputId);

    List<DmpOutputTaskRecordEntity> getOutputTaskRecord(@Param("sourceCode") String sourceCode, @Param("outputClass") String outputClass);
    
    List<String> outputErrorCountMsg();

    DmpPushTaskDTO.SyncInfoDTO getSuccessData(@Param("params") DmpSyncTaskDTO.OneDTO params);

    IPage<DmpOutputTaskRecordDTO.PagingViewDTO> pagingOutLatest(Page<T> query,@Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);

    List<DmpOutputTaskRecordEntity> getOutputTaskByIdAndType(@Param("sourceIdList") List<String> sourceIdList, @Param("sourceType") String sourceType);
    /**
     * 获取最后一条拉取记录
     * @author will
     * @date 2025/8/27 18:25
     * @param paramDTO
     * @return LastPullDTO
     */
    DmpPushTaskDTO.LastPullDTO getLastPullRecord(@Param("params")DmpPushTaskDTO.LastPullParamDTO paramDTO);

    /**
     * 获取最新的推送记录
     * @param sourceCodeList 来源编码列表
     * @param outputClass 输出处理器
     * @return 分组后的推送记录列表
     */
    List<DmpOutputTaskRecordEntity> getLastOutputTaskRecordList(@Param("sourceCodeList") List<String> sourceCodeList, @Param("outputClass") String outputClass);
}
