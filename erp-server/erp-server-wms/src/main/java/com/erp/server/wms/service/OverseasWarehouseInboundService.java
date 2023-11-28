package com.erp.server.wms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 海外仓入库单 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasWarehouseInboundService extends SuperService<OverseasWarehouseInboundEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-16
     */
    BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-16
     */
    Boolean update(OverseasWarehouseInboundDTO.UpdateDTO dto);


    OverseasWarehouseInboundEntity getByCode(String receivingCode);

    /**
     * 分页查询
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-21
     */
    PagingVO<OverseasWarehouseInboundDTO.ListDTO> paging(PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto);

    /**
     * 手动完结
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-24
     */
    BatchResultDTO manualFinish(OverseasWarehouseInboundDTO.FinishDTO dto);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    OverseasWarehouseInboundDTO.ViewDTO view(String id);

    /**
     * 详情列表
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    List<OverseasWarehouseInboundDetailDTO.ViewListDTO> viewList(OverseasWarehouseInboundDTO.ViewListReqDTO dto);

    /**
     * 状态数量统计
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    List<OverseasWarehouseInboundDTO.CountDTO> listCount(PermissionsDTO dto);

    /**
     * 取消
     *
     * @param id ID
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    BatchResultDTO cancel(String id);

    /**
     * 删除
     *
     * @param id ID
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    BatchResultDTO delete(String id);

    /**
     * d导出
     *
     * @param dto 条件
     * @return Boolean
     * @author Jim
     * @date: 2023-11-27
     */
    Boolean exportExcel(OverseasWarehouseInboundDTO.ExportDTO dto, HttpServletResponse response);

    List<String> getReceiptNumbersForStatus(List<String> statusList);

    /**
     * 根据来源id查询入库单
     * @Author Luo_WG
     * @Date 2023/11/27 17:36
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.OverseasWarehouseInboundEntity>
     **/
    List<OverseasWarehouseInboundEntity> listBySourceIds(List<String> sourceIds);
}
