package com.erp.server.wms.service;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.entity.WmsCartonSpecEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 发货单箱规信息 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsCartonSpecService extends SuperService<WmsCartonSpecEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto 界面传递的新增数据
    * @param taskId 来源id
    * @return
    */
    String add(WmsCartonSpecDTO.AddDTO dto, String taskId);

    /**
    * 根据发货单id查询装箱信息
    * @author Luo_WG
    * @date: 2023-11-16
    * @param mainIds
    * @return
    */
    List<WmsCartonSpecEntity> listByMainIds(List<String> mainIds);

    /**
     * 查询装箱数量
     * @Author Luo_WG
     * @Date 2023/11/28 19:04
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.PackingQtyDTO>
     **/
    List<WmsCartonSpecDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId, Integer boxSpecNo);

    /**
     * 根据主表id删除箱规信息
     * @Author Luo_WG
     * @Date 2023/11/29 10:33
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean deleteBySourceIds(List<String> mainIds);

    /**
     * 根据来源Id查询包装信息
     * @Author Luo_WG
     * @Date 2024/3/21 14:24
     * @param packingTaskId
     * @return java.util.List<com.erp.model.wms.dto.WmsCartonSpecDTO.PackingQtyDTO>
     **/
    List<WmsCartonSpecDTO.PackDateDTO> listPackDateByPackingTaskId(String packingTaskId);

    /**
     * 校验打包数据是否超过发货数量
     * @param sourceId
     * @return
     */
    Boolean packQtyCheck(String sourceId);

    /**
     * 根据来源id查询装箱详情
     * @param packingTaskEntity
     * @return
     */
    WmsCartonSpecDTO.WmsCartonSpecView getCartonViewByPackingTaskId(PackingTaskEntity packingTaskEntity);

    /**
     * 删除原装箱信息
     * @param taskId
     */
    void deleteCarton(String taskId);

    /**
     * 获取箱子预警信息
     * @param sourceType
     * @param grossWeight
     * @return
     */
    WmsCartonSpecDTO.WeightRuleDTO getWarnMsg(String sourceType, BigDecimal grossWeight);

    /**
     * 根据来源类型和 装箱的sku进行校验
     * @param sourceType
     * @param skuIds
     */
    void checkProductPropertyIds(String sourceType, List<String> skuIds);
    /**
     * 更新箱规 pda
     * @param dto
     */
    void updateSpec(WmsCartonSpecDTO.SpecSaveDTO dto);

    WmsCartonSpecEntity getByTaskIdAndBoxNo(String packingTaskId, String boxNo);
}
