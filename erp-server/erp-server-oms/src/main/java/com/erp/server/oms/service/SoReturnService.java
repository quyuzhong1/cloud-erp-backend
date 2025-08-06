package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoReturnService extends SuperService<SoReturnEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SoReturnDTO.PagingView>
     **/
    PagingVO<SoReturnDTO.PagingView> paging(PagingDTO<SoReturnDTO.PagingParam> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.StatusCountDTO>
     **/
    List<SoReturnDTO.StatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param dto
     * @return java.lang.String
     **/
    String add(SoReturnDTO.Add dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnDTO.Update dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param id
     * @return com.erp.model.oms.dto.SoReturnDTO.View
     **/
    SoReturnDTO.View view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(SoReturnDTO.Add dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(SoReturnDTO.Update dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(BaseApproveParamDTO baseApproveParamDTO,SoReturnEntity entity);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param ids
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(SoReturnEntity entity);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/5/10 16:47
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 删除单个销售退货订单
     * @Author Luo_WG
     * @Date 2023/5/10 16:47
     * @param entity
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO deleteEntity(SoReturnEntity entity);

    /**
     * 根据ID列表获取实体Map
     * @Author Luo_WG
     * @Date 2023/5/10 16:47
     * @param ids
     * @return java.util.Map<java.lang.String, com.erp.model.oms.entity.SoReturnEntity>
     **/
    Map<String, SoReturnEntity> mapByIds(List<String> ids);

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/5/10 16:47
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean exportExcel(@RequestBody SoReturnDTO.PagingParam dto);

    /**
     * 下推退货通知单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/11 11:20
     * @param ids
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.GenerateSoReturnNoticeView>
     **/
    List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids);

    /**
     * 获取所有已审核订单
     * @Author Luo_WG
     * @Date 2023/5/11 11:20
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.GenerateSoReturnNoticeView>
     **/
    List<SoReturnEntity> listSoReturnByApproveStatus();

    /**
     * 根据退货单id查询退货单信息
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @return com.common.core.controller.vo.ApiResult
     **/
    SoReturnDTO.SoReturnEntityDTO getSoReturnById(String id);

    /**
     * 下推销售退货订单-保存
     * @Author Luo_WG
     * @Date 2023/5/25 15:56
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateSoReturnSave(List<SoInfoDTO.GenerateSoReturnView> list);

    /**
     * 销售单详情-单据关联-退货订单号列表
     * @Author Luo_WG
     * @Date 2023/5/25 16:21
     * @param sourceId sourceId
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.PagingView>
     **/
    List<SoReturnDTO.PagingView> listSoReturnDetailBySourceId(String sourceId);

    /**
     * 根据来源id 集合获取到对应的下推数据
     * @author yl
     * @date 2023-05-29 16:47
     * @param soIds
     * @return java.lang.Integer
     */
    Integer getPushDownBySourceIds(List<String> soIds);

    /**
     * 更新金蝶状态等信息
     * @Author Luo_WG
     * @date: 2023/5/23 17:41
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     **/
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);

    /**
     * 更新销售订单地址信息
     * @param soId
     * @param receiveAddress
     * @param receiverName
     * @param telNumber
     */
    void updateAddress(String soId, String receiveAddress, String receiverName, String telNumber);

    /**
     * 根据sku编号查询销售退货单
     * @Author Luo_WG
     * @Date 2023/8/15 16:50
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.PdaSoReturn>
     **/
    List<SoReturnDTO.PdaSoReturn> pdaList(SoReturnDTO.PdaSoReturnParam dto);

    /**
     * PDA:根据id查询详情
     * @Author Luo_WG
     * @Date 2023/8/16 9:31
     * @param id
     * @return com.erp.model.oms.dto.SoReturnDTO.View
     **/
    SoReturnDTO.View pdaView(String id);

    void syncOrderToDmp(SoReturnEntity entity, String syncOperate);

    /**
     * 导出销售退货
     * @param dto 参数
     */
    PagingVO<SoReturnDTO.PagingView> exportSoReturn(PagingDTO<SoReturnDTO.PagingParam> dto);

    BigDecimal calLocalCurrency(BigDecimal exchangeRate, BigDecimal returnAmount);

    BigDecimal calReturnAmount(BigDecimal amount, Integer qty, Integer returnQty);

    Map<String,String> getCurrencySymbol(List<String> currencys);

    Map<String,BigDecimal> getCurrencyMap(List<String> currencys);
}
