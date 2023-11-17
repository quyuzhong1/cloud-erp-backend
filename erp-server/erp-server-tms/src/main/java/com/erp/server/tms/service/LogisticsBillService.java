package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsBillDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 物流单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsBillService extends SuperService<LogisticsBillEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsBillDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillDTO.UpdateDTO dto);

    /**
     * 根据来源id查询物流单
     * @Author Luo_WG
     * @Date 2023/11/9 19:40
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.LogisticsBillEntity>
     **/
    List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 新增物流单
     * @Author Luo_WG
     * @Date 2023/11/9 18:04
     * @param addDTOList
     * @return java.lang.Boolean
     **/
    Boolean logisticsBillBatchSave(List<LogisticsBillDTO.AddDTO> addDTOList);

    /**
     * 根据来源id查询物流信息及跟踪号
     * @Author Luo_WG
     * @Date 2023/11/10 9:07
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(List<String> sourceIdList);

    /**
     * @description: 根据出库单号查询
     * @author Will
     * @date: 2023/11/14 19:32
     * @param outstockCodeList
     * @return List<LogisticsBillEntity>
     */
    List<LogisticsBillEntity> listByOutstockCodeList(List<String> outstockCodeList);
    
    /**
     * tab 列表
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    List<LogisticsBillDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页
     * @author yl
     * @date 2023-11-16 14:01
     * @param dto
     * @return 
     */
    PagingVO<LogisticsBillDTO.PagingVO> paging(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto);

    /**
     * 导出
     *@parms
     *@return
     *@author yl
     *@date 2023-11-16
     */
    Boolean exportExcel(LogisticsBillDTO.ExportDTO dto, HttpServletResponse response);
}
