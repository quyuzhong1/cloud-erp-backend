package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.dto.excel.SampleScrapImportExcelDTO;
import com.erp.server.wms.service.SampleScrapInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author jack
 * @Classname SupplierVisitExcelListener
 * @Date 2025-06-27
 */
public class SampleScrapExcelListener extends AnalysisEventListener<SampleScrapImportExcelDTO> {

    //sku信息
    private Map<String,SkuVO> skuMap ;
    //用户
    private List<FindUserDTO> userList ;
    //部门
    private List<SysDepartmentDTO> deptList ;

    /**
     * 错误信息
     */
    private List<SampleScrapImportExcelDTO> errorList = new ArrayList<>();

    private List<SampleScrapInfoDTO.ImportDTO> addList = new ArrayList<>();

    public SampleScrapExcelListener(List<SysDepartmentDTO> deptList,
                                    Map<String,SkuVO> skuMap,
                                    List<FindUserDTO> userList) {
        this.skuMap = skuMap;
        this.deptList = deptList;
        this.userList = userList;
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-06-27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleScrapImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        SampleScrapInfoDTO.ImportDTO addDTO = new SampleScrapInfoDTO.ImportDTO();
        //序号
        addDTO.setNo(excelDTO.getNo());

        //报废人
        String scrapUserName = excelDTO.getScrapUserName();
        FindUserDTO findUserDTO = userList.stream().filter(e -> scrapUserName.equals(e.getUserName())).findFirst().orElse(null);
        if(Objects.isNull(findUserDTO)){
            errorMsgList.add("报废人不存在");
        }else {
            addDTO.setScrapUserId(findUserDTO.getUserId());
            addDTO.setScrapUserName(findUserDTO.getUserName());
        }

        //报废日期
        String scrapDateStr = excelDTO.getScrapDateStr();
        if(StringUtils.isNotBlank(scrapDateStr)){
            try {
                LocalDate scrapDate = StringUtils.isBlank(scrapDateStr) ? null : LocalDate.parse(scrapDateStr, dateTimeFormatter);
                addDTO.setScrapDate(scrapDate);
            }catch (Exception e){
                errorMsgList.add("报废时间格式错误、请使用yyyy-MM-dd格式");
            }
        }

        String scrapDeptName = excelDTO.getScrapDeptName();
        SysDepartmentDTO sysDepartmentDTO = deptList.stream().filter(e -> scrapDeptName.equals(e.getName())).findFirst().orElse(null);
        if(Objects.isNull(sysDepartmentDTO)){
            errorMsgList.add("报废部门不存在");
        }else {
            addDTO.setScrapDeptId(sysDepartmentDTO.getId());
            addDTO.setScrapDeptName(sysDepartmentDTO.getName());
        }

        //备注
        addDTO.setRemark(excelDTO.getRemark());
        //sku
        String skuNo = excelDTO.getSkuNo();
        if(StringUtils.isNotBlank(skuNo)){
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if(Objects.isNull(skuVO)){
                errorMsgList.add("SKU不存在");
            }else {
                addDTO.setSkuId(skuVO.getSkuId());
                addDTO.setSkuNo(skuVO.getSkuNo());
                addDTO.setProductName(skuVO.getSkuName());
            }
        }
        //报废数量
        String scrapQty = excelDTO.getScrapQty();
        if(StringUtils.isNotBlank(scrapQty)){
            addDTO.setScrapQty(Integer.parseInt(scrapQty));
        }

        //明细备注
        addDTO.setDetailRemark(excelDTO.getDetailRemark());
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        addList.add(addDTO);
    }


    /**
     * 数据全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:51
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<SampleScrapImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<SampleScrapInfoDTO.ImportDTO> getSuccessList() {
        return addList;
    }

}
