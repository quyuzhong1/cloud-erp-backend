package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpSoPrestockInfoDTO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface OtherInstockService extends SuperService<OtherInstockEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/17 15:13
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<OtherInstockDTO.ListDTO> paging(PagingDTO<OtherInstockDTO.SearchParamDTO> dto);
    /**
     * @description: 查寻数量
     * @author Will
     * @date: 2023/5/17 15:13
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<OtherInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/17 15:14
     * @param dto
     * @return String
     */
    String add(OtherInstockDTO.AddDTO dto);

    String addAndApprove(OtherInstockEntity dto, Boolean isPushWdt);
    String disApproveAndGenerate(String dbId,DmpSoPrestockInfoDTO.PrestockDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/17 15:14
     * @param dto
     * @return String
     */
    String addAndSubmit(OtherInstockDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/17 15:14
     * @param dto
     * @return Boolean
     */
    Boolean update(OtherInstockDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/17 15:15
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(OtherInstockDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/17 15:15
     * @param id
     * @return Boolean
     */
    BatchResultDTO submit(String id,Boolean isProcess);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/5/17 15:15
     * @param id
     * @return ViewDTO
     */
    OtherInstockDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);

    /**
     * @description: 原子批量删除其他入库单
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);

    /**
     * @description: 删除单个实体
     * @author Will
     * @date: 2023/5/17 15:15
     * @param entity
     * @return BatchResultDTO
     */
    BatchResultDTO deleteEntity(OtherInstockEntity entity);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/17 15:16
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @param id
     * @param type
     * @param comment
     * @param isPushWdt
     * @description: 审核
     * @author Will
     * @date: 2023/12/5 11:58
     */
    BatchResultDTO approve(String id, String type, String comment, Boolean isPushWdt);

    /**
     * 结束审核
     * @author will
     * @date 2025/5/16 10:28
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, OtherInstockEntity entity);
    /**
     * @param id
     * @param isPushWdt
     * @return Boolean
     * @description: 反审核
     * @author Will
     * @date: 2023/5/17 15:16
     */
    BatchResultDTO disApprove(String id, Boolean isPushWdt);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/17 15:16
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/5/17 15:17
     */
    Boolean exportExcel(OtherInstockDTO.SearchParamDTO dto);

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
     * PDA:列表查询
     * @Author Luo_WG
     * @Date 2023/8/23 9:58
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.OtherInstockDTO.PdaListDTO>
     **/
    PagingVO<OtherInstockDTO.PdaListDTO> PdaPaging(PagingDTO<OtherInstockDTO.PdaSearchParamDTO> dto);

    /**
     * PDA:列表数量
     * @Author Luo_WG
     * @Date 2023/8/23 10:35
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.OtherInstockDTO.PdaListStatusCountDTO>
     **/
    List<OtherInstockDTO.PdaListStatusCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * 新增并审核
     * @Author Luo_WG
     * @Date 2023/12/8 9:15
     * @param dto
     * @return java.lang.String
     **/
    String addAndApprove(OtherInstockDTO.AddDTO dto);

    /**
     * 海外仓入库生成其他出库单
     * @param entity
     * @param detailEntityList
     * @param remark
     */
    String generateByOverseasInbound(OverseasWarehouseInboundEntity entity, List<OverseasWarehouseInboundDetailEntity> detailEntityList, String remark,boolean isTransitWarehouse);

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
    void importBatchSave(List<OtherInstockEntity> saveList);

    void syncWdtPreInstock(DmpSoPrestockInfoDTO.PrestockDTO dto);

    OtherInstockEntity getByThirdCode(String thirdCode, InventoryDirectionEnum inventoryDirectionEnum);

    void generateOpposite(OtherInstockEntity dbOtherInstockEntity, String code);

    /**
     * 其他入库
     */
    PagingVO<OtherInstockDTO.ListDTO> exportOtherInStock(PagingDTO<OtherInstockDTO.SearchParamDTO> dto);

    List<OtherInstockEntity> listByCodes(List<String> list);

    void updateApproveStatus(OtherInstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);

    /**
     * 根据ID列表获取实体Map
     * @param ids
     * @return Map<String, OtherInstockEntity>
     */
    Map<String, OtherInstockEntity> mapByIds(List<String> ids);

    /**
     * 样品退回单-关联其他入库单据
     * @author wuhaotian
     * @date: 2025/8/25 10:16
     * @param dto
     * @return List<ListDTO>
     */
    List<OtherInstockDTO.ListDTO> viewAssociatedDocuments(BaseIdDTO dto);
}
