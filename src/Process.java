import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Process {

    public int pname;
    public int page_size;
    public int arrival_time;
    public int duration;
    public int current;
    Process(int name,int size,int time,int d,int c){
        pname=name;
        page_size=size;
        arrival_time=time;
        duration=d;
        current=c;
    }
    // generate processes randomly

}
