package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolFeedbackDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;

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
     * 导出
     * @author wuhaotian
     * @date: 2025-12-01
     * @param dto
     * @param response
     */
    void export(PagingDTO<KolFeedbackDTO.ParamDTO> dto, HttpServletResponse response);

    /**
     * 导入
     * @author wuhaotian
     * @date: 2025-12-01
     * @param file
     */
    void importData(MultipartFile file);

    /**
     * 下载导入模板
     * @author wuhaotian
     * @date: 2025-12-01
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

}
