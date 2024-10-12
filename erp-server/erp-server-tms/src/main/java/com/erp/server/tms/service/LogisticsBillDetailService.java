package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 物流单明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsBillDetailService extends SuperService<LogisticsBillDetailEntity> {

    /**
     * 新增
     *
     * @param mainId
     * @param list
     * @return
     * @author lambda
     * @date: 2023-11-09
     */
    Boolean add(LogisticsBillEntity billEntity, List<LogisticsBillDetailDTO.AddDTO> list ,boolean isGenerateCost);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author lambda
     * @date: 2023-11-09
     */
    Boolean update(LogisticsBillDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主表id查询详情
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.tms.entity.LogisticsBillDetailEntity>
     * @Author Luo_WG
     * @Date 2023/11/9 19:52
     **/
    List<LogisticsBillDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id删除详情
     *
     * @param mainIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/11/9 19:52
     **/
    Boolean removeByMainIds(List<String> mainIds,boolean isDeleteCost);

    /**
     * 更改状态
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-17
     */
    BatchResultDTO updateStatus(String id, String trackStatus, LocalDateTime trackTime,String trackDesc);

    /**
     * 分页获取轨迹数据
     *
     * @param query
     * @return
     */
    PagingVO<LogisticsBillDetailEntity> getPage(LogisticsBillDetailQueryDTO query);

    /**
     *
     * @param query
     * @return
     */
    PagingVO<LogisticsTrackDTO.UpdateTrackDTO> getTrackDtoPage(LogisticsBillDetailQueryDTO query);

    /**
     * 列表查询
     * @param query
     * @return
     */
    List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(LogisticsBillDetailQueryDTO query);

    /**
     * 根据运单号查询明细记录
     * @param trackNo
     * @return
     */
    List<LogisticsBillDetailEntity> getDetailByTrackNo(String trackNo);

    /**
     * 更改运输单号
     * @param billDTO
     * @return
     */
    Boolean updateTrackNo(LogisticsBillDTO.UpdateTrackNoDTO billDTO);

    List<LogisticsBillDetailEntity> listByTrackNo(List<String> trackNoList);
    /**
     * @description: 根据平台订单号和物流跟踪单号查询
     * @author Will
     * @date: 2024/5/11 11:50
     * @param platformCodeList
     * @param trackNoList
     * @return List<LogisticsBillDetailEntity>
     */
    List<LogisticsBillDetailEntity> listByPlatformCodeAndTrackNo(List<String> platformCodeList, List<String> trackNoList);

    /**
     * 根据跟踪号进行更新操作
     * @param max
     */
    void updateLogisticsBillDetailByTrackNo(LogisticsTrackEntity max);

    /**
     * 更新跟踪号信息
     * @param trackNo
     * @param status
     * @param signTime
     */
    void updateTrackStatus(String trackNo, String status, LocalDateTime signTime, LocalDateTime trackTime);

    /**
     * 批量更新跟踪号信息
     * @param trackNoList
     * @param code
     * @param now
     */
    void batchUpdateTrackStatus(List<String> trackNoList, String code, LocalDateTime now, LocalDateTime trackTime);
}
