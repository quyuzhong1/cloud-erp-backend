package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.model.wms.entity.WarehouseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface OtherOutstockService extends SuperService<OtherOutstockEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/17 16:14
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<OtherOutstockDTO.ListDTO> paging(PagingDTO<OtherOutstockDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/17 16:14
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<OtherOutstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/17 16:14
     * @param dto
     * @return String
     */
    String add(OtherOutstockDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/17 16:15
     * @param dto
     * @return String
     */
    String addAndSubmit(OtherOutstockDTO.AddDTO dto);
    /**
     * 新增并审核
     * @Author Luo_WG
     * @Date 2023/12/8 11:07
     * @param dto
     * @return java.lang.String
     **/
    String addAndApprove(OtherOutstockDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/17 16:15
     * @param dto
     * @return Boolean
     */
    Boolean update(OtherOutstockDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/17 16:15
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(OtherOutstockDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/17 16:15
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/17 16:16
     * @param id
     * @return ViewDTO
     */
    OtherOutstockDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/18 17:54
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/18 17:54
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/18 17:54
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/18 17:54
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/18 17:54
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/5/18 17:55
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto, HttpServletResponse response);

    /**
     * @description: 更新金蝶状态等信息
     * @author Will
     * @date: 2023/5/23 17:41
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * PDA:分页查询
     * @Author Luo_WG
     * @Date 2023/8/23 11:16
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.OtherOutstockDTO.PdaListDTO>
     **/
    PagingVO<OtherOutstockDTO.PdaListDTO> PdaPaging(PagingDTO<OtherOutstockDTO.PdaSearchParamDTO> dto);

    /**
     * PDA:列表数量
     * @Author Luo_WG
     * @Date 2023/8/23 17:44
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.OtherOutstockDTO.PdaListStatusCountDTO>
     **/
    List<OtherOutstockDTO.PdaListStatusCountDTO> PdaListCount(PermissionsDTO dto);

    /**
     * 海外仓入库生成其他出库单
     * @param entity
     * @param detailEntityList
     * @param remark
     */
    String generateByOverseasInbound(OverseasWarehouseInboundEntity entity, List<OverseasWarehouseInboundDetailEntity> detailEntityList, String remark,Boolean isOnwayWarehouse);


    /**
     * 封装报损出库单主记录
     * @param warehouse 目的仓
     * @return OtherOutstockDTO.AddDTO
     */
    OtherOutstockDTO.AddDTO buildLossMainDto(WarehouseEntity warehouse,Boolean isOnwayWarehouse);
}
