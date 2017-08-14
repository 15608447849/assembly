package m.sqlites;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by user on 2017/7/3.
 */
class SQLiteConnect{
    protected static final int NORMAL = 0;
    protected static final int USED = 1;


    private Connection connection;
    private final WeakReference<SQLiteHelper> sqLiteHelperWr;
    private long time;
    private int state = NORMAL;
    public SQLiteConnect(SQLiteHelper sqLiteHelper) {
        this.sqLiteHelperWr = new WeakReference<>(sqLiteHelper);
        createConnect();
        updateTime();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    private void createConnect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite"+sqLiteHelperWr.get().path+sqLiteHelperWr.get().dbName);//连接数据库
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //更新时间
    public void updateTime(){
        time = System.currentTimeMillis();
    }
    //超时结束
    public boolean isOuttime(){
        if (sqLiteHelperWr.get()==null) return true;
        return (System.currentTimeMillis() - time) > sqLiteHelperWr.get().getOutTime();
    }
    //是否关闭
    public boolean isClose(){
        try {
            return connection==null || connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
    public void close(){
        if (!isClose()){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                connection = null;
            }
        }
    }

    public Connection getConnect() {
        return connection;
    }
}
