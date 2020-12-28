package com.renxl.rotter.pipeline.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.renxl.rotter.pipeline.domain.PipelineTaskReading;
import com.renxl.rotter.pipeline.service.IPermitService;
import com.renxl.rotter.pipeline.service.IPipelineTaskReadingService;
import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.rpcclient.events.LoadReadingEvent;
import com.renxl.rotter.rpcclient.events.SelectReadingEvent;
import com.renxl.rotter.rpcclient.events.TaskEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 15:45
 */
@Component
public class RotterHeartBeatEventListener {



}

