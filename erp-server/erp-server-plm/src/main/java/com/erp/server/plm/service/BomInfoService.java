package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.plm.dto.AuditParamDTO;
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


    Boolean insert(AddBomDTO dto);

    PagingVO<List<BomPagingVO>> paging(PagingDTO<SearchPagingDTO> dto);

    BomDTO getBomDetails(String bomId);

    Boolean edit(UpdateBomDTO dto);

    Boolean deleteById(String bomId);

    Boolean submitAudit(String bomId);

    Boolean restartAudit(String bomId);

    Boolean freeze(String bomId);

    Boolean defrost(String bomId);

    Boolean scrap(String bomId);

    Boolean recover(String id);

    Boolean startChange(UpdateBomDTO dto);

    List<BomVO> getByIds(List<String> bomIdList);

    List<ChangeInfoDTO> getBomInfo(String  searchKeyword);

    Boolean removeArchive(String id);

    void checkIfChange(String sourceId);

    void updateState(String sourceId, Integer state);

    void exportExcel(SearchPagingDTO dto, HttpServletResponse response);

    void approvalNoPass(AuditParamDTO dto);

    void approvalPass(AuditParamDTO dto);

    void bomProcessPass(ProcessPassDTO dto);

    void changeBom(BomDTO bom);

    List<ApproveNodeRecordVO> auditInfo(String id);

    void checkAuditor(List<BomSkuDTO> skuList);

    List<String> getSkuIdList(List<BomSkuDTO> skuList);

    Integer getMaxSequence();
    /**
     * @description: 导入bom
     * @author Will
     * @date: 2023/3/7 9:39
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);


}
