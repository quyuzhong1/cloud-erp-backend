package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolFeedbackCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolFeedbackCostDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * KOL回片费用表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
public interface KolFeedbackCostService extends SuperService<KolFeedbackCostEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolFeedbackCostDTO.AddDTO dto);

    /**
    * 批量新增
    * @author wuhaotian
    * @date: 2025-12-03
    * @param dto
    * @return
    */
    List<BatchResultDTO> batchAdd(KolFeedbackCostDTO.BatchAddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolFeedbackCostDTO.UpdateDTO dto);

    /**
    * 分页查询
    * @author wuhaotian
    * @date: 2025-12-03
    * @param dto
    * @return
    */
    PagingVO<KolFeedbackCostDTO.ListDTO> paging(PagingDTO<KolFeedbackCostDTO.ParamDTO> dto);

    /**
     * 单个删除
     * @author wuhaotian
     * @date: 2025-12-03
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);

    /**
     * 导出
     * @author wuhaotian
     * @date: 2025-12-03
     * @param dto
     * @param response
     * @return
     */
    Boolean export(KolFeedbackCostDTO.ParamDTO dto, HttpServletResponse response);

    /**
     * 异步导入
     * @author wuhaotian
     * @date: 2025-12-03
     * @param dto
     * @return
     */
    Boolean importExcel(BaseDTO.ImportDTO dto);

    /**
     * 导入KOL回片费用
     * @author wuhaotian
     * @date: 2025-12-03
     * @param dto
     */
    void importKolFeedbackCost(BaseDTO.ImportDTO dto);

    /**
     * 处理导入成功的数据
     * @author wuhaotian
     * @date: 2025-12-03
     * @param successList
     * @param errorNoList
     * @param errorList2
     * @param importType
     */
    void handleImportSuccessList(List<com.erp.model.oms.dto.excel.KolFeedbackCostExcelDTO> successList, 
                                  List<String> errorNoList, 
                                  List<com.erp.model.oms.dto.excel.KolFeedbackCostExcelDTO> errorList2, 
                                  String importType);

    /**
     * 下载导入模板
     * @author wuhaotian
     * @date: 2025-12-03
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

}
