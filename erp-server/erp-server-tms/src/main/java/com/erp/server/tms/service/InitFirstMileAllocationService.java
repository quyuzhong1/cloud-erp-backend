package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;
import com.erp.model.tms.entity.InitFirstMileAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 期初头程分摊 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
 */
public interface InitFirstMileAllocationService extends SuperService<InitFirstMileAllocationEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InitFirstMileAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-13
    * @param dto
    * @return
    */
    Boolean update(InitFirstMileAllocationDTO.UpdateDTO dto);

    /**
     * tab 列表
     * @param dto
     * @return
     */
    List<InitFirstMileAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<InitFirstMileAllocationDTO.PagingVO> paging(PagingDTO<InitFirstMileAllocationDTO.PagingParamDTO> dto);

    /**
     * 导出excel
     * @param dto
     * @return
     */
    void exportExcel(InitFirstMileAllocationDTO.PagingParamDTO dto);

    /**
     * 审核期初头程分摊
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return
     */
    BatchResultDTO approve(InitFirstMileAllocationEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 反审核
     * @param entity
     * @return
     */
    BatchResultDTO disApprove(InitFirstMileAllocationEntity entity);

    /**
     * 撤销
     * @param entity
     * @return
     */
    BatchResultDTO cancel(InitFirstMileAllocationEntity entity);

    /**
     * 提交审核
     * @param entity
     * @return
     */
    BatchResultDTO submit(InitFirstMileAllocationEntity entity);

    /**
     * 删除记录
     * @param entity
     * @return
     */
    BatchResultDTO delete(InitFirstMileAllocationEntity entity);

    /**
     * 详情
     * @param id
     * @return
     */
    InitFirstMileAllocationDTO.ViewDTO view(String id);

    /**
     * 导入excel
     * @param excelFile
     * @param detailList
     * @param response
     * @return
     */
    InitFirstMileAllocationDTO.ImportDTO importFile(MultipartFile excelFile, List<InitFirstMileAllocationDetailDTO.AddDTO> detailList, HttpServletResponse response);

    /**
     * 下载导入模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 提交修改并提审
     * @param dto
     */
    void updateAndSubmit(InitFirstMileAllocationDTO.UpdateDTO dto);

    /**
     * 下推初期头程对账单你
     * @param dto
     * @return
     */
    List<BatchResultDTO> generateReconciliation(InitFirstMileAllocationDTO.ReconciliationDTO dto);
}
