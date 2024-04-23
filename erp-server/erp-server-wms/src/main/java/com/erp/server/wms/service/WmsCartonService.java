package com.erp.server.wms.service;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.WmsCartonEntity;

import java.util.List;

/**
 * <p>
 * 发货单箱规信息 服务类
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
    * @param dto 界面传递的新增数据
    * @param sourceId 来源id
    * @param sourceType 来源类型
    * @return
    */
    void add(WmsCartonDTO.AddDTO dto, String sourceId, String sourceType);

    /**
    * 根据发货单id查询装箱信息
    * @author Luo_WG
    * @date: 2023-11-16
    * @param mainIds
    * @return
    */
    List<WmsCartonEntity> listBySourceIds(List<String> mainIds);

    /**
     * 查询装箱数量
     * @Author Luo_WG
     * @Date 2023/11/28 19:04
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.PackingQtyDTO>
     **/
    List<WmsCartonDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId, Integer boxSpecNo);

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
     * @param sourceId
     * @return java.util.List<com.erp.model.wms.dto.WmsCartonDTO.PackingQtyDTO>
     **/
    List<WmsCartonDTO.PackDateDTO> listPackDateBySourceId(String sourceId);

    /**
     * 校验打包数据是否超过发货数量
     * @param sourceId
     * @return
     */
    Boolean packQtyCheck(String sourceId);

    /**
     * 根据来源id查询装箱详情
     * @param sourceId
     * @return
     */
    WmsCartonDTO.WmsCartonView getCartonViewBySourceId(String sourceId);

    /**
     * 删除原装箱信息
     * @param id
     */
    void deleteCarton(String id);
}
