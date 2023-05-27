import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static int NUM_OF_PROC = 150;
    public static int PAGE_LIST_SIZE = 100;
    public static int NUM_OF_RUN=5;
    public static int[] PAGE_SIZE ={5,11,17,31};
    public static int SIMULATION_DURATION = 600;//1 min=600*100 msec
    public static int[] PROC_DURATION ={10,20,30,40,50};
    public static int get_next_page(int current, int max){
        Random rand = new Random();
        int x = rand.nextInt(10);
        if(x < 7){
            //there is a 70% probability that the next reference will be to page i, i-1, or i+1
            x = current+(rand.nextInt(3))-1+max;
        }
        else{
            x = current+rand.nextInt(max-2)+2;

        }
        x=x%(max);
        return x;
    }
    public static Page fifo(LinkedList<Page> memory){
        Page p=memory.get(0);
        for(Page m:memory){
            if (m.bring_in<p.bring_in){
                p=m;
            }
        }
        return p;
    }
    public static Page lru(LinkedList<Page> memory){
        Page p=memory.get(0);
        for(Page m:memory){
            if (m.last_used<p.last_used){
                p=m;
            }
        }
        return p;
    }
    public static Page lfu(LinkedList<Page> memory){
        Page p=memory.get(0);
        for(Page m:memory){
            if (m.counter<p.counter){
                p=m;
            }
        }
        return p;
    }
    public static Page mfu(LinkedList<Page> memory){
        Page p=memory.get(0);
        for(Page m:memory){
            if (m.counter>p.counter){
                p=m;
            }
        }
        return p;
    }
    public static Page rp(LinkedList<Page> memory){
        Random rand=new Random();
        Page p=new Page();
        do{
            p=memory.get(rand.nextInt(PAGE_LIST_SIZE));
        }while (p.pname==-1);
        return p;
    }
    public static Process[] GenerateProcesses(){
        Random rand = new Random();
        Process[] jobs=new Process[NUM_OF_PROC];
        for(int i=0;i<NUM_OF_PROC;i++){
            rand.setSeed(i);
            jobs[i]=new Process(i,PAGE_SIZE[rand.nextInt(PAGE_SIZE.length)],
                    rand.nextInt(SIMULATION_DURATION),
                    PROC_DURATION[rand.nextInt(PROC_DURATION.length)],0);
        }
        //sorted by arrival of time
        Arrays.sort(jobs, new Comparator<Process>() {
            @Override
            public int compare(Process p, Process t) {
                return p.arrival_time-t.arrival_time;
            }
        });
        return jobs;
    }
    public static int start_process(PrintWriter pw,Process[] jobs, LinkedList<Page> memory, Page pl, int j, int clock){
        int swapped=0;
        while (j < NUM_OF_PROC && jobs[j].arrival_time < clock) {
            if (pl.page_free(memory,4)) {// at least 4 free pages
                int free = pl.get_page_free(memory);
                Page p = new Page(jobs[j].pname, free, jobs[j].current, 1, clock , clock );
                //create new page with process id,the first available page,current page,counter,start time,and last recently used time
                memory.set(free, p);
                pl.printMemoryMap(memory,pw);
                System.out.println("Started the process "+ jobs[j].pname+ " Time: " + clock/10.0 +  " Size: "+ jobs[j].page_size  + " duration: "+ jobs[j].duration);
                pw.println("Started the process "+ jobs[j].pname+ " Time: " + clock/10.0 +  " Size: "+ jobs[j].page_size  + " duration: "+ jobs[j].duration);
                swapped++;
                j++;

            } else {
                System.out.println("No more memory");
                break;
            }
        }
        pl.printMemoryMap(memory,pw);
        System.out.printf("Swapped in %d\n",swapped);
        return swapped;

    }
    public static int reference(PrintWriter pw,int al,Process p,LinkedList<Page> memory,Page pl,int clock)throws NullPointerException{
        int hit=0;
        int next= get_next_page(p.current,p.page_size);
        p.current=next;
        LinkedList<Page> in_memory=pl.process_in(memory,p.pname);
        int index=pl.page_exist(in_memory,next);
        if(index>0){
            pl.update_page(memory,index,clock);
            System.out.println(clock/10.0+" :Page "+next+ " for process "+ p.pname +" exists in memory loction " + index);
            pw.println(clock/10.0+" :Page "+next+ " for process "+ p.pname +" exists in memory loction " + index);
            hit++;
        }else{
            if(pl.page_free(memory,1)){
                int free= pl.get_page_free(memory);
                Page pg = new Page(p.pname, free, next, 1, clock , clock );
                //create new page with process id,the first available page,current page,counter,start time,and last recently used time
                memory.set(free, pg);
                System.out.println(clock/10.0+" :Page "+next+ " for process "+ p.pname +" brought into memory page number " + free+ ". Eviction needed: false");
                pw.println(clock/10.0+" :Page "+next+ " for process "+ p.pname +" brought into memory page number " + free+ ". Eviction needed: false");
            }
            else{
                //replacement algorithm
                Page evict=new Page();
                switch (al){
                    case 0:
                        evict=fifo(memory);
                        break;
                    case 1:
                        evict=lru(memory);
                        break;
                    case 2:
                        evict=lfu(memory);
                        break;
                    case 3:
                        evict=mfu(memory);
                        break;
                    case 4:
                        evict=rp(memory);
                        break;
                }

                Page pg = new Page(p.pname, evict.index, next, 1, clock , clock );
                //create new page with process id,the first available page,current page,counter,start time,and last recently used time
                System.out.println("Page "+next+ "for process "+ p.pname +"brought into memory page number" +evict.index+"Process "+evict.pname+"  page number "+evict.pno+" was evicted.  ");
                pw.println("Page "+next+ "for process "+ p.pname +"brought into memory page number" +evict.index+"Process "+evict.pname+"  page number "+evict.pno+" was evicted.  ");
                memory.set(evict.index, pg);
            }

        }
        return hit;
    }
    public static double[] simulate(PrintWriter pw,int al){
        double[] res=new double[4];
        Process[] jobs=GenerateProcesses();
        Page pl=new Page();
        LinkedList<Page> memory=new LinkedList<>();
        pl.init(memory,PAGE_LIST_SIZE);
        pl.printMemoryMap(memory,pw);
        int head=0;//the index of the last process in queue
        int swapped_total=0;
        int hits=0;
        int accesses=0;
        for(int clock=0;clock<SIMULATION_DURATION;clock++){
            int swapped=start_process(pw,jobs,memory,pl,head,clock);
            swapped_total+=swapped;
            head+=swapped;

            for(int i=0;i<head;i++){
                if(jobs[i].arrival_time+jobs[i].duration>clock){
                    hits+=reference(pw,al,jobs[i],memory,pl,clock);
                    accesses++;
                }else if(jobs[i].arrival_time+jobs[i].duration==clock){
                    pl.free_memory(memory,jobs[i].pname);
                    pl.printMemoryMap(memory,pw);
                    System.out.println("Completed the process "+ jobs[i].pname+ " Time: " + clock/10.0 +  " Size: "+ jobs[i].page_size  + " duration: "+ jobs[i].duration);
                    pw.println("Completed the process "+ jobs[i].pname+ " Time: " + clock/10.0 +  " Size: "+ jobs[i].page_size  + " duration: "+ jobs[i].duration);
                }
            }
        }
        res[0]=swapped_total;
        res[1]=hits*1.0/accesses;
        res[2]=(accesses-hits)*1.0/accesses;
        res[3]=hits*1.0/(accesses-hits);
        return res;
    }

    public static void main(String[] args) throws IOException {
        File outputFile = new File("output.txt");
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        int[] algorithm=new int[]{0,1,2,3,4};//fifo,lru,lfu,mfu,random pick
        String[] a=new String[]{"FIFO","LRU","LFU","MFU","Random pick"};

        for(int al:algorithm){
            int swapped=0;
            double hitratio=0;
            double missratio=0;
            double hitmiss=0;
            for(int i=0;i<NUM_OF_RUN;i++){
                double[] out=simulate(printWriter,al);
                swapped+=(int) out[0];
                hitratio+=out[1];
                missratio+=out[2];
                hitmiss+=out[3];
            }
            System.out.println("Algorithm:" +a[al]);
            System.out.println("Averge number of processes that were successfully swapped in :" + (swapped / NUM_OF_RUN));
            System.out.println("Averge hit ratio of pages referenced by the running jobs for each run : " + (hitratio / NUM_OF_RUN));
            System.out.println("Averge miss ratio of pages referenced by the running jobs for each run : " + (missratio / NUM_OF_RUN));
            System.out.println("Averge hit/miss ratio of pages referenced by the running jobs for each run : " + (hitmiss / NUM_OF_RUN));
            printWriter.println("Algorithm:" +a[al]);
            printWriter.println("Averge number of processes that were successfully swapped in :" + (swapped / NUM_OF_RUN));
            printWriter.println("Averge hit ratio of pages referenced by the running jobs for each run : " + (hitratio / NUM_OF_RUN));
            printWriter.println("Averge miss ratio of pages referenced by the running jobs for each run : " + (missratio / NUM_OF_RUN));
            printWriter.println("Averge hit/miss ratio of pages referenced by the running jobs for each run : " + (hitmiss / NUM_OF_RUN));
        }
        printWriter.close();
        fileWriter.close();



    }
}