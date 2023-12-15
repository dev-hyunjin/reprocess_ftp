package com.app.find.mapper.mysql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface Mysql {
    public List<Map<String, Object>> selectUCIDList();

    public void updateFindStatus(String ucid, String ext, String recDate);
}
