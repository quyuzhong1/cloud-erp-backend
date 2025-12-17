package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
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
     * @param billEntity
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
     * 列表查询
     * @param query
     * @return
     */
    List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(LogisticsBillDetailQueryDTO query);

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
     *
     * @param max
     */
    void updateLogisticsBillDetailByTrackNo(LogisticsTrackEntity max);


    /**
     * 批量更新跟踪号信息
     * @param trackNoList
     * @param code
     * @param signTime
     * @param trackTime
     */
    void batchUpdateTrackStatus(List<String> trackNoList, String code, LocalDateTime signTime, LocalDateTime trackTime);

    /**
     * 更新注册状态
     * @param errorList
     * @param status
     */
    void updateRegisterStatus(List<LogisticsBillDetailDTO.BillDetailErrorDTO> errorList, int status);

    /**
     * 更新注册状态 携带参数
     * @param sucessList
     * @param status
     */
    void updateRegisterStatusByParams(List<LogisticsBillDetailDTO.BillDetailDTO> sucessList, int status);

    void updateTrackEnableByIds(List<String> detailIds);

    void updateRegisterParams(List<LogisticsTrackDTO.UpdateTrackDTO> refList);

    /**
     * 根据第三方配置关系id查询详情数量
     * @param id
     * @return
     */
    Integer countByThirdRefId(String id);
}
