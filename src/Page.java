import java.io.PrintWriter;
import java.util.*;
public class Page {
    public int pname;//process id
    public int index;
    public int pno;//page number of process
    public int counter;//MFU
    public int bring_in;//FIFO
    public int last_used;//LRU
    public LinkedList<Page> memory;
    //default is null
    Page(){
        pname=-1;
        pno=-1;
    }
    Page(int i){
        pname=-1;
        pno=-1;//default is null
        index=i;
    }
    Page(int pid,int i,int no,int c,int b,int lu){
        pname=pid;
        index=i;
        pno=no;
        counter=c;
        bring_in=b;
        last_used=lu;
    }
    public void init(LinkedList<Page> memory,int num){
        for(int i=0;i<num;i++){
            memory.add(new Page(i));
        }
    }
    public LinkedList<Page> process_in(LinkedList<Page> memory,int pid){
        LinkedList<Page> process_in_memory=new LinkedList<>();
        for(Page m:memory){
            if(m.pname==pid){
                process_in_memory.add(m);
            }
        }
        return process_in_memory;
    }
    public int page_exist(LinkedList<Page> process_in_memory,int no){
        for(Page m:process_in_memory){
            if(m.pno==no){
                return m.index;
            }
        }
        return -1;
    }
    public void update_page(LinkedList<Page> memory,int index,int clock){
        Page p=memory.get(index);
        p.counter++;
        p.last_used=clock;
        memory.set(index,p);
    }
    public boolean page_free(LinkedList<Page> memory,int avail){
        int num=avail;
        for(Page p:memory){
            if(p.pname==-1){
                num--;
            }
            if(num==0) return true;
        }
        return false;
    }
    public int get_page_free(LinkedList<Page> memory){
        for(int i=0;i<memory.size();i++){
            if(memory.get(i).pname==-1){
                return i;
            }
        }
        return -1;
    }
    public void free_memory(LinkedList<Page> memory,int pid){
        for(Page p:memory){
            if(p.pname==pid){
                p.pname=-1;
                p.pno=-1;
            }
        }

    }
    public void printMemoryMap(LinkedList<Page> memory, PrintWriter pw) {
        System.out.println("Memory map:");
        for (Page p: memory) {
            if (p.pname == -1) {
                System.out.print(". ");
                pw.print(". ");
            }
            else
            {
                System.out.print(p.pname + " ");
                pw.print(p.pname + " ");
            }
        }
        System.out.println("\n");
        pw.println("\n");
    }
}



