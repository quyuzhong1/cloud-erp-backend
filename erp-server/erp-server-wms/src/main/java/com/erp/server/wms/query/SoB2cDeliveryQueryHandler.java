package com.erp.server.wms.query;

import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.erp.model.wms.enums.ShipmentMarkTypeEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class SoB2cDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("sbl.track_no".equals(field)) {
            DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
            String tableName = "so_b2c_logistics";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c_logistics";
            }
            return " EXISTS (SELECT 1 from " + tableName + " sbl where sbl.main_id = sbd.source_id and sbl.is_deleted = false and sbl.track_no " + compareCodeSplicingValueSql + ")";
        }
        if ("isFullyManaged".equals(field)) {
            boolean isFullyManaged = Boolean.parseBoolean(value.toString());
            if (isFullyManaged) {
                return " sbd.dict_platform = 'TikTokFully'";
            } else {
                return " sbd.dict_platform != 'TikTokFully'";
            }
        }
        if ("sbd.logistic_type".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            } else {
                super.buildDefaultDTO("sbd.logistic_type", searchType);
            }
        }
        if ("waveCode".equals(field)) {
            String waveSql = getWaveCodeCompareCodeSplicingValueSql(compareCodeSplicingValueSql);
            String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
            return waveSql + " AND " + addDeliveryInterceptFilterSql;
        }

        if ("sbd.tab".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }

            // 待处理
            if (SoB2cDeliveryStatusEnum.WAIT_HANDLE.getStatus().equals(searchType)) {
                String statusFilterSql = " sbd.status = 'waitHandle' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return statusFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }
            //拣货中
            if (SoB2cDeliveryStatusEnum.PICKING.getStatus().equals(searchType)) {
                String statusFilterSql = " sbd.status = 'picking' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return statusFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }
            //已发货
            if (SoB2cDeliveryStatusEnum.SHIPPED.getStatus().equals(searchType)) {
                String statusFilterSql = " sbd.status = 'shipped' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return statusFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }
            //取消发货
            if (SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus().equals(searchType)) {
                String statusFilterSql = " sbd.status = 'cancelDelivery' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return statusFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }
            //生成波次
            if (SoB2cDeliveryStatusEnum.GENERATE_WAVE.getStatus().equals(searchType)) {
                String statusFilterSql = " sbd.status = 'generation_waves' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return statusFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }

            //拦截中
            if ("intercepting".equals(searchType)) {
                return getDeliveryInterceptFilterSql();
            }
            if (SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getStatus().equals(searchType)) {
                String statusFilterSql = " sbd.status = 'exceptionOrder' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return statusFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }
            //虚假发货
            if ("falseShipment".equals(searchType)) {
                String shipmentMarkFilterSql = " sbd.shipment_mark = '" + ShipmentMarkTypeEnum.MANUAL.getCode() + "' ";
                String addDeliveryInterceptFilterSql = getDeliveryInterceptFilterSql();
                return shipmentMarkFilterSql + " AND " + addDeliveryInterceptFilterSql;
            }
        }

        if ("isIntercept".equals(field)) {
            Boolean bool = (Boolean) value;

            DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
            String tableName = "so_b2c";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c";
            }
            return " EXISTS ( " +
                    " SELECT 1  " +
                    " FROM " + tableName + " AS b2c " +
                    " WHERE " +
                    " b2c.is_deleted = FALSE  " +
                    " AND b2c.is_intercept = " + bool +
                    " AND sbd.source_id = b2c.id)";
        }
        if ("logisticsLabelUrl".equals(field)) {
            DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
            String tableName = "foreign_so_b2c_label";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c_label";
            }
            Boolean bool = (Boolean) value;
            if (bool) {
                return " EXISTS ( " +
                        " SELECT 1 " +
                        " FROM " + tableName + " fsbl " +
                        " WHERE fsbl.main_id = sbd.source_id " +
                        " AND fsbl.is_deleted = FALSE " +
                        " AND fsbl.logistics_label_url IS NOT NULL " +
                        " AND fsbl.logistics_label_url != '')";
            } else {
                return " NOT EXISTS ( " +
                        " SELECT 1 " +
                        " FROM " + tableName + " fsbl " +
                        " WHERE fsbl.main_id = sbd.source_id " +
                        " AND fsbl.is_deleted = FALSE " +
                        " AND fsbl.logistics_label_url IS NOT NULL " +
                        " AND fsbl.logistics_label_url != '')";
            }

        }
        return null;
    }

    private String getDeliveryInterceptFilterSql() {
        return "  NOT EXISTS ( " +
                " SELECT 1 " +
                " FROM so_b2c_delivery_intercept sbdi " +
                " WHERE sbdi.source_id = sbd.source_id " +
                " AND sbdi.handle_status = 'waitHandle' " +
                " AND sbdi.is_deleted = false)";
    }
    private String getWaveCodeCompareCodeSplicingValueSql(String compareCodeSplicingValueSql) {
        return "  EXISTS ( " +
                " SELECT 1  " +
                " FROM wave_list AS pw " +
                " LEFT JOIN wave_list_detail AS pwd ON pw.ID = pwd.main_id AND pwd.is_deleted = FALSE  " +
                " WHERE " +
                " pw.is_deleted = FALSE " +
                " AND pw.code " + compareCodeSplicingValueSql +
                " AND pwd.delivery_id = sbd.ID )";
    }
}
