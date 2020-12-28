package com.renxl.rotter.manager;

import com.renxl.rotter.config.CompomentManager;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:04
 */
@Data
public class MetaManager {

    private ManagerInfo manager;


    public void init(){

        String adress = CompomentManager.getInstance().getManagerAdress();
        if(StringUtils.isEmpty(adress)){
            return;
        }
        manager = new ManagerInfo();
        manager.setManagerAddress(adress);

    }


}
