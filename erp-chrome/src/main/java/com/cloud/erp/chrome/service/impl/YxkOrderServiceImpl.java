package com.cloud.erp.chrome.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.YxkOrderDTO;
import com.cloud.erp.chrome.entity.YxkOrderEntity;
import com.cloud.erp.chrome.handler.ConvertHandler;
import com.cloud.erp.chrome.mapper.YxkOrderMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.YxkOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-29
 */
@Service
public class YxkOrderServiceImpl extends ServiceImpl<YxkOrderMapper, YxkOrderEntity> implements YxkOrderService {


    @Autowired
    private ConvertHandler convertHandler;

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;

    /**
     * 保存云星空的数据
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-08-30 9:45
     */

    @Override
    public void saveYxkOrder(YxkOrderDTO dto) {

        String vurl = dto.getUrl();
        String cookie = dto.getCookie();
        try {
            URL url = new URL(vurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", cookie);
            InputStream inputStream = connection.getInputStream();
            ExcelReader excelReader = ExcelUtil.getReader(inputStream, "Sheet1");
            excelReader.addHeaderAlias("日期", "shipmentDate");
            excelReader.addHeaderAlias("单据编号", "documentNo");
            excelReader.addHeaderAlias("客户", "customers");
            excelReader.addHeaderAlias("销售部门", "salesDepartments");
            excelReader.addHeaderAlias("物料编码", "sku");
            excelReader.addHeaderAlias("物料名称", "tradeName");
            excelReader.addHeaderAlias("实发数量", "quantity");
            excelReader.addHeaderAlias("仓库", "warehouse");
            excelReader.addHeaderAlias("单价", "unitPrice");
            excelReader.addHeaderAlias("含税单价", "taxUnitPrice");
            excelReader.addHeaderAlias("金额", "money");
            excelReader.addHeaderAlias("价税合计", "taxMoney");
            excelReader.addHeaderAlias("订单单号", "orderNumber");
            System.out.println(excelReader.getRowCount());
            List<YxkOrderEntity> list = excelReader.read(0, 1, excelReader.getRowCount() - 1, YxkOrderEntity.class);
            //获取到空的 集合
            List<YxkOrderEntity> vacancyList = list.stream().filter(y -> StringUtils.isBlank(y.getDocumentNo())).collect(Collectors.toList());
            //非空的集合
            List<YxkOrderEntity> nonEmptyList = list.stream().filter(y -> StringUtils.isNotBlank(y.getDocumentNo())).collect(Collectors.toList());
            if (vacancyList != null && vacancyList.size() > 0) {
                for (YxkOrderEntity item : vacancyList) {
                    String orderNumber = item.getOrderNumber();
                    YxkOrderEntity entity = nonEmptyList.stream().filter(k -> k.getOrderNumber().equals(orderNumber)).findFirst().orElse(null);
                    if (!Objects.isNull(entity)) {
                        item.setShipmentDate(entity.getShipmentDate());
                        item.setDocumentNo(entity.getDocumentNo());
                        item.setCustomers(entity.getCustomers());
                        item.setSalesDepartments(entity.getSalesDepartments());
                    }
                }
            }
            nonEmptyList.addAll(vacancyList);
            List<List<YxkOrderEntity>> lists = convertHandler.splitList(nonEmptyList, 1000);
            for (List<YxkOrderEntity> listSub : lists) {
                this.saveBatch(listSub);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(1,"云星空保存数据失败  通过参数无法获取到数据");

        }

        chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);


    }
}
