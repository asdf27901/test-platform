package com.lmj.platformserver.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 数据看板聚合数据VO
 */
@Data
@NoArgsConstructor // 添加无参构造函数，方便框架（如Jackson）反序列化
public class DataBoardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 基础数据统计
     */
    private BasicData basicData;

    /**
     * 请求成功/失败比例 (饼图数据)
     */
    private List<NameValueVo> requestRatios;

    /**
     * 创建测试用例最多的用户Top5 (柱状图数据)
     */
    private List<NameValueVo> testCaseCreationTop5;

    private List<DailyRequestDetail> dailyRequestDetails;


    // ----------------- 静态内部类定义 -----------------

    /**
     * 基础数据统计
     */
    @Data
    @NoArgsConstructor
    public static class BasicData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long totalRequests;
        private Long totalUsers;
        private Long totalInterfaceTestcases;
        private Long totalInterfaces;
    }

    /**
     * 通用的图表数据项VO (适用于饼图、柱状图等)
     * 例如：{ name: '成功', value: 950 }, { name: '失败', value: 50 }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor // 添加全参构造函数，方便在service层快速创建对象
    public static class NameValueVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String name;
        private Long value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor // 添加全参构造函数，方便在service层快速创建对象
    public static class DailyRequestDetail implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private LocalDate dateTime;
        private Long singleRequestCount;
        private Long chainRequestCount;
        private Long successCount;
        private Long failCount;
        private Long errorCount;
    }
}