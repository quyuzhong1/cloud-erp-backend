package com.erp.server.wms.service;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import com.common.business.dto.base.BaseIdDTO;

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
     * @param id
     * @return Boolean
     */
    BatchResultDTO submit(String id,Boolean isProcess);
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
     * @description: 原子批量删除其他出库单
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);
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
     * @param id
     */
    BatchResultDTO approve(String id, String type, String comment,Boolean isNeedProcess);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/18 17:54
     * @param id
     */
    BatchResultDTO approve(String id, String type, String comment);
    /**
     * 结束审核
     * @author will
     * @date 2025/5/16 10:28
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, OtherOutstockEntity entity);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/18 17:54
     * @param id
     * @return Boolean
     */
    BatchResultDTO disApprove(String id);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/18 17:54
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/5/18 17:55
     */
    Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto);

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
    OtherOutstockDTO.AddDTO buildLossMainDto(WarehouseEntity warehouse,Boolean isOnwayWarehouse,String userId);

    /**
     * 下载导入模板
     *
     * @author Jim
     * {@code @date:} 2024/03/21
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入
     *
     * @author Jim
     * {@code @date:} 2024/03/21
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 导入批量保存
     *
     * @author Jim
     * {@code @date:} 2024/03/21
     */
    void importBatchSave(List<OtherOutstockEntity> saveList);


    /**
     * 检查并新增
     *
     * @author Jim
     * {@code @date:} 2024/05/31
     */
    void checkAndAdd(OtherOutstockDTO.AddDTO generateDTO);

    PagingVO<OtherOutstockDTO.ListDTO> exportOtherOutStock(PagingDTO<OtherOutstockDTO.SearchParamDTO> dto);

    List<OtherOutstockEntity> listByCodes(List<String> list);

    void updateApproveStatus(OtherOutstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);

    /**
     * 查看关联出库单
     * @author Will
     * @date: 2024/12/19 10:16
     * @param dto
     * @return List<ListDTO>
     */
    List<OtherOutstockDTO.ListDTO> viewAssociatedDocuments(BaseIdDTO dto);
}
