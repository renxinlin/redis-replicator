package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 23:03
 */
@Data
public class StartTaskResult implements Serializable {

  private boolean success;

}
