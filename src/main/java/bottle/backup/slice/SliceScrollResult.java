package bottle.backup.slice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/11/24.
 */
public class SliceScrollResult {
    private List<SliceMapper> list_same;
    private List<SliceMapper> list_diff;
    public SliceScrollResult() {
        this.list_same = new ArrayList<>();
        this.list_diff = new ArrayList<>();
    }

    public List<SliceMapper> getList_same() {
        return list_same;
    }

    public List<SliceMapper> getList_diff() {
        return list_diff;
    }

    public void setList_same(List<SliceMapper> list_same) {
        this.list_same = list_same;
    }

    public void setList_diff(List<SliceMapper> list_diff) {
        this.list_diff = list_diff;
    }

    public int getDifferentSize(){
        return list_diff.size();
    }

    @Override
    public String toString() {
        return "SliceScrollResult{" +
                "list_same=" + list_same +
                ", list_diff=" + list_diff +
                '}';
    }

    public String getDifferentBlockSequence() {
        StringBuilder sb = new StringBuilder();
        for (SliceMapper mapper : list_diff){
                sb.append(mapper.getPosition()).append("-").append(mapper.getLength()).append("#");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String getSameBlockSequence() {
        StringBuilder sb = new StringBuilder();
        for (SliceMapper mapper : list_same){
            sb.append(mapper.getPosition()).append("-").append(mapper.getLength()).append("-").append(mapper.getSliceInfo().getPosition()).append("#");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public int getSameSize() {
        return list_same.size();
    }
}
