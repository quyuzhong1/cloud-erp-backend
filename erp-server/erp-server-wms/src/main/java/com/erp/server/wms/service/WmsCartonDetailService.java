package com.erp.server.wms.service;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.model.wms.entity.WmsCartonSpecEntity;

import java.util.List;

/**
 * <p>
 * 发货单箱子信息表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsCartonDetailService extends SuperService<WmsCartonDetailEntity> {


    /**
     * 根据装箱id查询箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/28 18:55
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.FirstMileCartonDetailEntity>
     **/
    List<WmsCartonDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据任务idh获取全部装箱数据
     * @param mainIds
     * @return
     */
    List<WmsCartonDetailEntity> listByTaskIds(List<String> mainIds);

    /**
     * 根据箱规id删除箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/29 10:35
     * @param cartonIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByCartonIds(List<String> cartonIds);


    /**
     * 装箱详情清单(以箱号和SKU号维度)
     * @param mainId
     * @return
     */
    List<WmsCartonSpecDTO.PackingItemDTO> boxInfoBySourceIds(List<String> mainId);

    /**
     * 新增箱子明细
     * @param addDTO
     * @param wmsCartonEntity
     * @param wmsCartonSpecEntity
     */
    void add(WmsCartonSpecDTO.AddDTO addDTO, WmsCartonEntity wmsCartonEntity, WmsCartonSpecEntity wmsCartonSpecEntity);

    /**
     * 根据箱子获取装箱明细列表
     * @param cartonIds
     * @return
     */
    List<WmsCartonDetailDTO.BoxDTO> listCartonDetailByMainIds(List<String> cartonIds);
}
