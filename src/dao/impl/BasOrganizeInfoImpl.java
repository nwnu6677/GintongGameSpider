package dao.impl;

import dao.BasOrganizeInfoDao;
import JavaBean.BasOrganizeInfo;

import java.util.List;

/**
 * Created by lenovo on 2017/2/10.
 */
public class BasOrganizeInfoImpl extends BaseDaoImpl<List> implements BasOrganizeInfoDao {
    public BasOrganizeInfoImpl(){
        this.setNs("com.gintongame.mapping.BasOrganizeInfoMapper.");
    }


    public void insertBatch(List<BasOrganizeInfo> basorganizeinfos) {
        this.getSqlSession().insert(this.getNs() + "insertBatch", basorganizeinfos);
}

    @Override
    public void insertSingle(BasOrganizeInfo ts) {
        this.getSqlSession().insert(this.getNs() + "insertSingle", ts);
    }
}
