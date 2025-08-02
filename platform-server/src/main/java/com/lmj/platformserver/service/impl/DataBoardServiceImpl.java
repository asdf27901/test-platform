package com.lmj.platformserver.service.impl;

import com.lmj.platformserver.mapper.DataBoardMapper;
import com.lmj.platformserver.service.DataBoardService;
import com.lmj.platformserver.vo.DataBoardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataBoardServiceImpl implements DataBoardService {

    @Autowired
    private DataBoardMapper dataBoardMapper;

    @Override
    public DataBoardVo getData() {
        DataBoardVo dataBoardVo = new DataBoardVo();
        DataBoardVo.BasicData basicData = dataBoardMapper.getBasicData();
        List<DataBoardVo.NameValueVo> requestRatios = dataBoardMapper.getRequestRatios();
        List<DataBoardVo.NameValueVo> testCaseCreationTop5 = dataBoardMapper.getTestCaseCreationTop5();
        List<DataBoardVo.DailyRequestDetail> dailyRequestDetails = dataBoardMapper.getDailyRequestDetails();

        dataBoardVo.setBasicData(basicData);
        dataBoardVo.setRequestRatios(requestRatios);
        dataBoardVo.setTestCaseCreationTop5(testCaseCreationTop5);
        dataBoardVo.setDailyRequestDetails(dailyRequestDetails);
        return dataBoardVo;
    }
}
