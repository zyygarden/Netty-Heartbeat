package cn.yuyang.pojo;

/**
 * @package cn.myzf.netty.common.entity
 * @Date Created in 2019/2/23 22:46
 * @Author myzf
 */
public class MonitorVo {

    private String clientName;

    private String dateList;

    private Long valueList;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDateList() {
        return dateList;
    }

    public void setDateList(String dateList) {
        this.dateList = dateList;
    }


    public Long getValueList() {
        return valueList;
    }

    public void setValueList(Long valueList) {
        this.valueList = valueList;
    }

    @Override
    public String toString() {
        return "MonitorVo{" +
                "clientName='" + clientName + '\'' +
                ", dateList='" + dateList + '\'' +
                ", valueList=" + valueList +
                '}';
    }
}