import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

public class MacProcesses {

    public interface ApplicationServices extends Library {
        ApplicationServices INSTANCE = Native.load("ApplicationServices", ApplicationServices.class);

        int GetFrontProcess(LongByReference processSerialNumber);
        int GetProcessPID(LongByReference processSerialNumber, IntByReference pid);
    }

    public void macProcess() {
        LongByReference psn = new LongByReference();
        IntByReference pid = new IntByReference();
        ApplicationServices.INSTANCE.GetFrontProcess(psn);
        ApplicationServices.INSTANCE.GetProcessPID(psn, pid);
        System.out.println("Front process pid: " + pid.getValue());
    }

//    https://stackoverflow.com/questions/63469738/how-to-get-foreground-window-process-in-java-using-jna-on-macos

}
