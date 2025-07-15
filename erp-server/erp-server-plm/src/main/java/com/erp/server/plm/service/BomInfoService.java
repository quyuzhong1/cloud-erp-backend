package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * bom 信息表(BomInfo)表服务接口
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
public interface BomInfoService  extends IService<BomInfoEntity> {


    String insert(AddBomDTO dto);

    PagingVO<BomPagingVO> paging(PagingDTO<SearchPagingDTO> dto);

    BomDTO getBomDetails(String bomId);

    Boolean edit(UpdateBomDTO dto);

    Boolean deleteById(String bomId);

    BatchResultDTO submitAudit(String bomId,Boolean isStartProcess);

    Boolean freeze(String bomId);

    Boolean defrost(String bomId);

    Boolean scrap(String bomId);

    Boolean recover(String id);

    List<BomVO> getByIds(List<String> bomIdList);

    List<ChangeInfoDTO> getBomInfo(String  searchKeyword);

    Boolean removeArchive(String id);

    void checkIfChange(String sourceId,String detailsJson);

    void updateState(String sourceId, Integer state);

    void exportExcel(SearchPagingDTO dto);

    void changeBom(BomDTO bom);

    List<ApproveNodeRecordVO> auditInfo(String id);


     String getUpdateContent(List<BomSkuDTO> oldBomList, List<BomSkuDTO> newBomList);
    /**
     * @description: 导入bom
     * @author Will
     * @date: 2023/3/7 9:39
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description:
     * @author Will
     * @date: 2023/3/9 10:04
     * @return Boolean
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);
    /**
     * @description: 分页查询显示组合SKU列表
     * @author Will
     * @date: 2023/6/14 11:20
     * @param dto
     * @return PagingVO<List<ListDTO>>
     */
    PagingVO<BomSkuPageDTO.ListDTO> skuPaging(PagingDTO<BomSkuPageDTO.PagingParamDTO> dto);

    /**
     * bom导出数据
     * @param dto 分页条件
     */
    PagingVO<BomExportExcelVO> exportBom(PagingDTO<SearchPagingDTO> dto);
    /**
     * 审核
     * @author will
     * @date 2025/5/16 15:02
     * @param approveOneDTO
     * @return BatchResultDTO
     */
    BatchResultDTO approve(ApproveOneDTO approveOneDTO);
    /**
     * 审核结束
     * @author will
     * @date 2025/5/16 15:18
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, BomInfoEntity entity);
    /**
     * 撤销流程
     * @author will
     * @date 2025/5/16 16:06
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelProcess(String id);
}
