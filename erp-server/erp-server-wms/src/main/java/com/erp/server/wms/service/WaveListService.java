package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListEntity;

import java.util.List;

public interface WaveListService extends SuperService<WaveListEntity> {

    /**
     * 新增
     */
    BaseResultDTO.AddDTO add(WaveListDTO.AddDTO dto);
    /**
     * 统计发货批次的数量
     */
    int countDelivery(PermissionsDTO param);

    /**
     * 根据状态获取
     * @param status
     * @return
     */
    List<String> listDeliveryIdByStatus(String status);

    /**
     * 通过波次号/拣货车号查询波次
     * @param code 波次号/拣货车号
     */
    WaveListEntity getByCodeOrCarCode(String code);

    /**
     * 获取波次明细
     * @param waveId 波次号
     */
    List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId);

    /**
     * 获取波次明细
     * @param waveId 波次号
     * @param basketNo 篮号
     */
    List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo);

    /**
     * 获取波次明细
     * @param waveId 波次号
     * @param basketNo 篮号
     * @param skuId sku
     */
    List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo, String skuId);

    WaveListEntity getByCode(String code);

    List<WaveListEntity> listByCarCode(String carCode);

    /**
     * 分页查询
     */
    PagingVO<WaveListDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO);

    /**
     * tabList
     */
    List<WaveListDTO.TabDTO> tabList();

    /**
     * 取消波次
     */
    BatchResultDTO cancelWave(String id);

    /**
     * 取消已打印（修改波次状态为：待拣货）
     */
    BatchResultDTO cancelPrinted(String id);

    /**
     * 通过发货单id查询波次
     *
     * @return
     */
    List<WaveListDTO.WaveDeliveryDTO> listByDeliverIds(List<String> ids);

    List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillPreview(SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param);

    /**
     * 高级查询查波次表的发货单id
     * @param compareCodeSplicingValueSql 拼好的sql
     */
    List<String> listDeliveryIdBySql(String compareCodeSplicingValueSql);
}
