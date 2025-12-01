package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolFeedbackDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * KOL回片列表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
public interface KolFeedbackService extends SuperService<KolFeedbackEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolFeedbackDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolFeedbackDTO.UpdateDTO dto);

    /**
    * 分页查询
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    PagingVO<KolFeedbackDTO.ListDTO> paging(PagingDTO<KolFeedbackDTO.ParamDTO> dto);

    /**
     * 状态统计
     * @author wuhaotian
     * @date: 2025-12-01
     * @param param
     * @return
     */
    List<KolFeedbackDTO.TabListDTO> tabList(PermissionsDTO param);

    /**
     * 批量删除
     * @author wuhaotian
     * @date: 2025-12-01
     * @param dto
     */
    void batchDelete(BaseIdsDTO.IdsDTO dto);

    /**
     * 单个删除
     * @author wuhaotian
     * @date: 2025-12-01
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);

    /**
     * 导出
     * @author wuhaotian
     * @date: 2025-12-01
     * @param dto
     * @return
     */
    Boolean export(PagingDTO<KolFeedbackDTO.ParamDTO> dto);

    /**
     * 异步导入
     * @author wuhaotian
     * @date: 2025-12-01
     * @param dto
     * @return
     */
    Boolean importExcel(BaseDTO.ImportDTO dto);

    /**
     * 导入KOL回片列表
     * @author wuhaotian
     * @date: 2025-12-01
     * @param dto
     */
    void importKolFeedback(BaseDTO.ImportDTO dto);

    /**
     * 处理导入成功的数据
     * @author wuhaotian
     * @date: 2025-12-01
     * @param successList
     * @param errorNoList
     * @param errorList2
     * @param importType
     */
    void handleImportSuccessList(List<com.erp.model.oms.dto.excel.KolFeedbackExcelDTO> successList, List<String> errorNoList, List<com.erp.model.oms.dto.excel.KolFeedbackExcelDTO> errorList2, String importType);

    /**
     * 下载导入模板
     * @author wuhaotian
     * @date: 2025-12-01
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

}
