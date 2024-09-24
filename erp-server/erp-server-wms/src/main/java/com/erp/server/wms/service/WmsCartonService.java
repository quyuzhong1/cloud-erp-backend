package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.entity.WmsCartonSpecEntity;

import java.util.List;

/**
 * <p>
 * 发货单箱子信息明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsCartonService extends SuperService<WmsCartonEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    */
    BaseResultDTO.AddDTO add(CartonDTO.AddDTO dto);
    /**
     * 修改
     * @author zdy
     * @date: 2024-07-02
     * @param dto
     * @return
     */
    Boolean update(CartonDTO.UpdateDTO dto);

    /**
     * 根据装箱id删除箱子明细
     * @Author Luo_WG
     * @Date 2023/11/28 16:31
     * @param cartonIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByCartonIds(List<String> cartonIds);

    /**
     * 根据发货单id删除箱子明细
     * @Author Luo_WG
     * @Date 2023/11/28 16:31
     * @param sourceIds
     * @return java.lang.Boolean
     **/
    Boolean deleteBySourceIds(List<String> sourceIds);

    /**
     * 根据发货单id查询箱子明细
     * @Author Luo_WG
     * @Date 2023/11/29 11:17
     * @param taskIds
     * @return java.util.List<com.erp.model.wms.entity.FirstMileCartonBillEntity>
     **/
    List<WmsCartonEntity> listByTaskIds(List<String> taskIds);

    /**
     * 装箱任务分页查询
     * @param dto
     * @return
     */
    PagingVO<CartonDTO.PagingViewDTO> paging(PagingDTO<CartonDTO.PagingParamDTO> dto);

    /**
     * 生成装箱信息
     * @param addDTO
     * @param wmsCartonSpecEntity
     */
    String add(WmsCartonSpecDTO.AddDTO addDTO, WmsCartonSpecEntity wmsCartonSpecEntity);

    /**
     * 根据任务id和箱号获取记录
     * @param taskId
     * @param boxNo
     * @return
     */
    WmsCartonEntity findCartonByTaskIdAndBoxNo(String taskId, Integer boxNo);

    /**
     * 获取箱号
     * @param id
     * @return
     */
    Integer getBoxNoByTaskId(String id);

    WmsCartonEntity getByTaskIdAndBoxNo(String id, String boxNo);

    /**
     * 历史数据装箱详情新增
     * @param taskId
     * @param cartonDTOList
     */
    void saveHistoryCartonList(String taskId, List<PackingTaskDetailDTO.HistoryCartonDTO> cartonDTOList);

    /**
     * 根据箱规获取箱子id
     * @param specId
     * @return
     */
    WmsCartonEntity getBySpecId(String specId);

    /**
     * 根据任务id和权限获取装箱列表
     * @param packedDetailDTO
     * @return
     */
    List<WmsCartonEntity> listByTaskIdsAndPermission(PackingTaskDTO.PackedDetailDTO packedDetailDTO);

    /**
     * 根据任务id获取箱子内容物详情
     */
    List<WmsCartonDTO.DetailDTO> listByPackingTaskId(String packingTaskId);
}
