package m.backup.backup.slice;

/**
 * Created by user on 2017/11/22.
 */
public class SliceMapper extends Slice{
    private SliceInfo sliceInfo;

    private int type = 0; // 0 相同片段 1不同片段

    public SliceMapper(int type) {
        this.type = type;
    }

    public SliceInfo getSliceInfo() {
        return sliceInfo;
    }

    public void setSliceInfo(SliceInfo sliceInfo) {
        this.sliceInfo = sliceInfo;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SliceMapper{" +
                "position=" + position +
                ", length=" + length +
//                ", server_position="+getSliceInfo().getPosition()+
                '}';
    }
}
