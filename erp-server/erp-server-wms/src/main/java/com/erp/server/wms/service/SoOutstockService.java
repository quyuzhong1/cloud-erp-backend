package com.erp.server.wms.service;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDetailDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.PlatformGenerateSoOutstockDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售订单出库单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoOutstockService extends SuperService<SoOutstockEntity> {

    /**
     * 根据来源id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/12 12:18
     * @param ids
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockEntity>
     **/
    List<SoOutstockEntity> listBySourceId(List<String> ids);

    /**
     * 销售订单ids获取销售出库单主表信息
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     **/
    List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds);

    /**
     * 销售订单ids获取销售出库单主表信息
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     **/
    SoOutstockEntity getBySoId(String soId);

    /**
     * 添加销售出库单a
     * @author yl
     * @date 2023-05-19 9:50
     * @param dto
     * @return java.lang.String
     */
    String add(SoOutstockDTO.AddDTO dto);

    /**
     * 批量提交
     * @author yl
     * @date 2023-05-19 10:34
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-19 10:42
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SoOutstockDTO.AddDTO dto);

    
    /**
     * 销售出库单详情
     * @author yl
     * @date 2023-05-19 10:45
     * @param id
     * @return com.erp.model.wms.dto.SoOutstockDTO.ViewDTO
     */
    SoOutstockDTO.ViewDTO view(String id);

    /**
     * 审核通过
     * @param dto
     * @param entity
     * @return
     */
    Boolean approveEnd(ApproveOneDTO dto, SoOutstockEntity entity);

    /**
     * 审核
     * @author yl
     * @date 2023-05-19 11:42
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(ApproveOneDTO dto);

    //TODO 物流单
//    @Async("saveLogisticsBill")
    void saveLogisticsBill(SoOutstockEntity entity);

    /**
     * 反审核
     * @author yl
     * @date 2023-05-19 12:10
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO disApprove(SoOutstockEntity entity, Boolean isPushKingDee);

    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-19 12:13
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 删除销售出库单
     * @author yl
     * @date 2023-05-19 12:16
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean delete(List<String> ids);


    /**
     * 作废
     * @author yl
     * @date 2023-05-19 14:16
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids,String remark);

    /**
     * 获取tab
     * @author yl
     * @date 2023-05-19 14:23
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.TabListDTO>
     */
    List<SoOutstockDTO.TabListDTO> tabList(PermissionsDTO dto);

    
    /**
     * 分页列表
     * @author yl
     * @date 2023-05-22 8:56
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PagingViewDTO>
     */
    PagingVO<SoOutstockDTO.PagingViewDTO> paging(PagingDTO<SoOutstockDTO.PagingParamDTO> dto);

    
    /**
     * 导出销售出库单
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-22 11:41
     */
    Boolean exportExcel(SoOutstockDTO.ExportDTO dto);

    
    /**
     * 修改销售出库单
     * @author yl
     * @date 2023-05-22 17:58
     * @param dto
     * @return java.lang.String
     */
    String updateSoOutstock(SoOutstockDTO.UpdateDTO dto);

    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-22 19:04
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SoOutstockDTO.UpdateDTO dto);

    
    /**
     * 销售出库单保存下推单据
     * @author yl
     * @date 2023-05-23 14:30
     * @param resultList
     * @return java.lang.Boolean
     */
    Boolean addB2bPushDownNo(List<SoOutstockDTO.GenerateSoOutstockViewDTO> resultList);

    
    /**
     * 销售订单获取销售出库单的数据
     * @author yl
     * @date 2023-05-23 18:37
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.SoRefDTO>
     */
    List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(String soId);

    /**
     * 保存销售订单下推销售出库单
     * @author yl
     * @date 2023-05-25 15:02
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean generateSoSave(ValidList<SoInfoDTO.GenerateDeliveryView> dto);

    /**
     *
     *@parms
     *@return 
     *@author yl
     *@date 
     */
    Integer getPushDownCountBySoIds(List<String> soIds);

    /**
     * 修改金蝶同步状态
     * @param id
     * @param syncKingdeeId
     * @Author Luo_WG
     * @Date 2023/6/1 18:55
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 根据code 获取到销售出库单信息
     * @author yl
     * @date 2023-06-28 10:17
     * @param code
     * @return com.erp.model.wms.entity.SoOutstockEntity
     */
    String  getByCode(String code);
    /**
     * @description: 列表修改
     * @author Will
     * @date: 2023/7/12 16:58
     * @param dto
     * @return String
     */
    List<BatchResultDTO> pagingUpdate(List<SoOutstockDTO.PagingUpdateDTO> dto);

    /**
     * 打印
     * @Author Luo_WG
     * @Date 2023/7/13 10:44
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     **/
    List<SoOutstockDTO.PrintDTO> print(List<String> ids);

    /**
     * 临时修复数据查询id
     * @return
     */
    List<String> getIdsByTemp(String tableName);


    /**
     * 金蝶同步到系统
     * @author yl
     * @date 2023-07-21 14:44
     * @param soOutstock 销售出库单
     * @param detailList 销售出库详情
     * @param flagId  已存在的flagId
     * @return void
     */
    void handleKingdeeToErp(SoOutstockEntity soOutstock, List<SoOutstockDetailEntity> detailList, String flagId);

    /**
     * 金蝶同步到系统，不单独事务
     * @author yl
     * @date 2023-07-21 14:44
     * @param soOutstock 销售出库单
     * @param detailList 销售出库详情
     * @param flagId  已存在的flagId
     * @return void
     */
    void handleNewKingdeeToErp(SoOutstockEntity soOutstock, List<SoOutstockDetailEntity> detailList, String flagId);

    /**
     * PDA:分页列表
     * @Author Luo_WG
     * @Date 2023/8/22 11:32
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PdaPagingViewDTO>
     **/
    PagingVO<SoOutstockDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<SoOutstockDTO.PdaPagingParamDTO> dto);

    /**
     * pda:列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/22 14:53
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.PdaPoReceiveCountDTO>
     **/
    List<SoOutstockDTO.PdaCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * PDA:新增
     * @Author Luo_WG
     * @Date 2023/9/6 17:45
     * @param dto
     * @return java.lang.String
     **/
    String pdaAdd(SoOutstockDTO.AddDTO dto);

    /**
     * PDA:修改
     * @Author Luo_WG
     * @Date 2023/9/6 17:55
     * @param dto
     * @return java.lang.String
     **/
    String pdaUpdate(SoOutstockDTO.UpdateDTO dto);


    /**
     * PDA:新增并提交
     * @Author Luo_WG
     * @Date 2023/9/6 17:57
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaAddAndSubmit(SoOutstockDTO.AddDTO dto);

    /**
     * PDA:修改并提交
     * @Author Luo_WG
     * @Date 2023/9/6 17:58
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdateAndSubmit(SoOutstockDTO.UpdateDTO dto);

    /**
     * 修改线上历史数据
     * @author yl
     * @date 2023-09-28 10:20
     * @param
     * @return void
     */
    void tempRepairHistoryDb();

    /**
     * 根据运单号模糊查询销售出库信息
     * @author yl
     * @date 2023-10-19 15:48
     * @param trackNo
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     */
    List<SoOutstockEntity> listByTrackNo(String trackNo);
    /**
     * @description: 查询总数量
     * @author Will
     * @date: 2023/11/1 14:17
     * @param dto
     * @return PagingTotalDTO
     */
    SoOutstockDTO.PagingTotalDTO getTotalByQuery(SoOutstockDTO.PagingParamDTO dto);

    /**
     * 生成B2C销售出库单
     * @author yl
     * @date 2023-12-11 16:16
     * @param b2cSoId 销售订单id
     * @return 
     */
    Boolean generateB2cSoOutstock(String b2cSoId);

    /**
     * 生成B2C销售出库单
     * @Author Luo_WG
     * @Date 2024/1/19 17:18
     * @param generateB2cDTO
     * @return java.lang.Boolean
     **/
    Boolean generateB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO);


    Boolean handleCreateB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto);


    /**
     * 检查关账或已有盘盈盘亏单据，并记录明细备注
     */
    Boolean checkClosedAndUpdateRemark(SoOutstockDTO.GenerateB2cDTO dto, String soOutStockId);

    /**
     * 审核并提交
     */
    void submitAndApprove(String id);

    /**
     * 保存与（提交, 审核）事务分开
     */
    Boolean handleCreateB2cSoOutstockWithoutTx(SoOutstockDTO.GenerateB2cDTO dto);


    String addB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto);

    /**
     * 生成B2C销售出库单
     * @author yl
     * @date 2023-12-11 16:16
     * @param soB2cCode 销售订单cdode
     * @return
     */
    Boolean generateB2cSoOutstockByCode(String soB2cCode);

    List<SoOutstockEntity> listByAdvanceQuery(AdvanceQueryContainer container);


    /**
     * 检查和生成销售出库单
     *
     * @param generateB2cDTO 根据销售订单生成的销售出库单DTO != 平台的销售出库单
     * @param dto 平台销售出库单信息
     * @param soB2cEntity B2C 销售订单
     *
     * @author Jim
     * @date 2024-03-07
     */
    Boolean checkAndGenerate(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformSoOutStockDTO dto, SoB2cEntity soB2cEntity);

    /**
     * 默认重推销售出库单逻辑
     *
     * @author Jim
     * @date 2024-03-07
     */
    Boolean defaultHandleRetry(SoB2cEntity soB2c, List<SoB2cEntity> instantList);

    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateDeclare(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq);

    Boolean updateStatus(TmsDeclareBillDTO.UpdateStatusDTO dto);

    List<SoOutstockEntity> listByCodes(List<String> codes);

    Boolean generateB2cSoOutstockByPlatformData(PlatformGenerateSoOutstockDTO platformGenerateSoOutstockDTO, String redissonKey);
    /**
     * @description: 重新生成销售出库单
     * @author Will
     * @date: 2024/4/28 10:18
     * @param ids
     * @return Boolean
     */
    Boolean afreshGenerateB2cOutstock(List<String> ids);

    void updateRemarkBySoId(String id,String remark);

    /**
     * 检查销售订单是否已生成销售出库单
     * @param soCode 销售单号
     * @param sourceType 来源类型
     * @param orderType 订单类型
     * @return true=已存在
     */
    boolean checkExist(String soCode, String sourceType, String orderType);

    /**
     * 第三方仓生成销售出库单
     */
    void thirdWarehouseCheckAndGenerate(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformOutboundDTO dto);


    /**
     * 新增平台仓B2C销售出库单
     */
    Boolean generatePlatformB2cOutStock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformSoOutStockDTO dto, SoB2cEntity soB2cEntity, Collection<PlatformSoOutStockDetailDTO> generateSourceDetailList);

    /**
     * 更新平台仓B2C销售出库单
     */

    Boolean updatePlatformB2cOutStock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO,
                                   PlatformSoOutStockDTO dto,
                                   SoB2cEntity soB2cEntity,
                                   Collection<PlatformSoOutStockDetailDTO> updateGenerateSourceDetailList,
                                   Map<String, SoOutstockEntity> mainEntityMap,
                                   Map<String, SoOutstockDetailEntity> detailEntityListMap
    );

    /**
     * 检查清理历史销售出库单异常信息
     */
    void checkAndDeletePlatformB2cOutStock(List<PlatformSoOutStockDetailDTO> existSourceDetailList, SoB2cEntity soB2cEntity, Collection<SoOutstockDetailEntity> detailEntityList);

    /**
     * 停止更新销售出库单数据的日期
     */
    LocalDate getStopSoOutStockDate();

    /**
     * 更新主表和明细
     */
    void updateMainAndDetail(SoOutstockEntity soOutstockEntity, List<SoOutstockDetailEntity> list);
    /**
     * 根据来源id获取未作废且未删除的数据
     *
     * @param id id
     * @return
     */
    int countNotVoided(String id);

    /**
     * 删除直接调拨单
     * @author will
     * @date 2024/7/22 11:27
     * @param list
     */
    void deleteTransferInfo(List<SoOutstockEntity> list);

    PagingVO<SoOutstockDTO.PagingViewDTO> exportSoOutStock(PagingDTO<SoOutstockDTO.ExportDTO> dto);
}
