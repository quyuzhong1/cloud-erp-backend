package com.erp.server.tms.service;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 中转报关详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferDeclareDetailService extends SuperService<TransferDeclareDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    void add(TransferDeclareDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    void update(TransferDeclareDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2024/1/23 11:23
     * @param mainIds
     * @return java.util.List<com.erp.model.tms.entity.TransferDeclareDetailEntity>
     **/
    List<TransferDeclareDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 用于分页查询 子查询关联过滤
     * @param mainIds
     * @param params
     * @return
     */
    List<TransferDeclareDetailEntity> listByCondition(List<String> mainIds,TransferDeclareDTO.PagingParamDTO params);

    /**
     * 详情明细高级查询
     * @Author Luo_WG
     * @Date 2024/1/24 10:15
     * @param dto
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDetailDTO.ViewDTO>
     **/
    List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(TransferDeclareDTO.ViewDetailParamDTO dto);

    /**
     * 修改中转状态
     * @Author Luo_WG
     * @Date 2024/1/27 18:45
     * @param id
     * @param transferStatus
     * @return java.lang.Boolean
     **/
    Boolean updateTransferStatus(String id, String transferStatus);

    /**
     * 查询待同步中转状态的订单
     * @Author Luo_WG
     * @Date 2024/1/29 9:28
     * @return java.util.List<com.erp.model.tms.entity.TransferDeclareDetailEntity>
     **/
    List<TransferDeclareDetailEntity> listWaitSyncTransferStatus();

    /**
     * 根据主标id删除详情
     * @Author Luo_WG
     * @Date 2024/2/1 14:21
     * @param mainIds
     * @return void
     **/
    void deleteByMainIds(List<String> mainIds);

    /**
     * 根据销售订单id获取
     * @param soId
     * @return
     */
    TransferDeclareDetailEntity getBySoId(String soId);

    /**
     * @description: 根据销售订单编号集合查询中专报关
     * @author Will
     * @date: 2024/3/27 12:13
     * @param soCodeList
     * @return List<TransferDeclareDetailEntity>
     */
    List<TransferDeclareDetailEntity> listBySoCodeList(List<String> soCodeList);
    /**
     * @description: 根据物流渠道id集合查询
     * @author Will
     * @date: 2024/4/1 12:32
     * @param logisticsChannelIdList
     * @return List<TransferDeclareDetailEntity>
     */
    List<TransferDeclareDetailEntity> listByLogisticsChannelIdList(List<String> logisticsChannelIdList);

    /**
     * 修改详情上传状态
     * @param list
     * @return
     */
    Boolean updateTransferStatusByBatch(List<TransferDeclareDTO.UpdateForcastStatusDTO> list);
}
