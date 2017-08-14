package m.lanshare.beads;

import java.net.SocketAddress;

/**
 * Created by user on 2017/6/19.
 */
public class DataSource {
    private SocketAddress toAddress;
    private byte[] filePathBytes;
    public DataSource(SocketAddress toAddress, byte[] filePathBytes) {
        this.toAddress = toAddress;
        this.filePathBytes = filePathBytes;
    }

    public SocketAddress getToAddress() {
        return toAddress;
    }

    public byte[] getFilePathBytes() {
        return filePathBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof DataSource){
            return this.toAddress.equals (((DataSource)o).getToAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toAddress.hashCode();
    }
}
