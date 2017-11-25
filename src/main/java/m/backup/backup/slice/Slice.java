package m.backup.backup.slice;

/**
 * Created by user on 2017/11/22.
 */
public class Slice {
    //下标
    protected long position;
    //长度
    protected long length;

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
