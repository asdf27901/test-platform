package com.lmj.platformserver.mapper;

import com.lmj.platformserver.vo.DataBoardVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataBoardMapper {
    DataBoardVo.BasicData getBasicData();

    List<DataBoardVo.NameValueVo> getRequestRatios();

    List<DataBoardVo.NameValueVo> getTestCaseCreationTop5();

    List<DataBoardVo.DailyRequestDetail> getDailyRequestDetails();
}
