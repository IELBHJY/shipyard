package shipyard2;


import java.util.HashMap;
import java.util.List;

public class Solution {
    Data data;
    final double epsilon = 0.0001;
    HashMap<Integer,List<Integer>> res;
    HashMap<Integer,Double> serverTimes;
    HashMap<Integer,List<Double>> time;
    double[] times;
    double objection;
    public Solution(Data data, double objection, HashMap<Integer,List<Integer>> paths, HashMap<Integer,Double> serverTimes,double[] times) {
        this.data = data;
        this.res=paths;
        this.serverTimes=serverTimes;
        this.objection=objection;
        this.times=times;
    }

    public Solution(Data data, double objection, HashMap<Integer,List<Integer>> paths, double[] times) {
        this.data = data;
        this.res=paths;
        this.objection=objection;
        this.times=times;
    }

    public void feasion(){
        //目标函数判断----没有加等待时间
        double sum=0.0;
        for(Integer key:res.keySet()){
            List<Integer> list=res.get(key);
            int size=list.size();
            if(size==0) continue;
            sum+=data.w[0][list.get(0)];
            for(int i=0;i<size-1;i++){
                sum+=data.w[list.get(i)][list.get(i+1)];
            }
            sum+=data.w[list.get(size-1)][0];
        }
        if(Math.abs(sum-objection)>1){
            System.out.println("解决方案计算得到的目标函数不等于模型目标函数值");
            System.out.println(sum+" "+objection);
            //System.exit(0);
        }
        //车载荷判断
        for(Integer key:res.keySet()){
            List<Integer> list=res.get(key);
            for(Integer i:list){
                if(data.taskWeights[i]>data.truckCaptitys[key] && data.s[i]==0){
                    System.out.println("任务"+i+"在车"+key+"上，不满足载荷。");
                    //System.exit(0);
                }
            }
        }
        //时间窗判断
        for(Integer key:serverTimes.keySet()){
            if(serverTimes.get(key)+epsilon<data.earlyTime[key] || serverTimes.get(key)-epsilon>data.lateTime[key]){
                System.out.println("任务"+key+"不满足时间窗约束。");
                System.out.println(serverTimes.get(key)+"  "+data.earlyTime[key]+"  "+data.lateTime[key]);
                //System.exit(0);
            }
        }
        for(Integer key:serverTimes.keySet()){
            if(serverTimes.get(key)+data.carryTaskTime[key]+epsilon< shipyard.Data.E || serverTimes.get(key)+data.carryTaskTime[key]-epsilon>Data.L){
                System.out.println("任务"+key+"不满足车场时间窗约束。");
                //System.exit(0);
            }
        }
        for(Integer key:res.keySet()){
            int firstTask=res.get(key).get(0);
            if(serverTimes.get(firstTask)+epsilon<data.w[0][firstTask]){
                System.out.println("车"+key+"上的第一个任务的开始时间小于车从车场行驶到其位置消耗的时间。");
                System.out.println(serverTimes.get(firstTask)+" "+data.w[0][firstTask]);
                //System.exit(0);
            }
        }
    }

    public void feasion(boolean noTWCheck){
        //车载荷判断 && 时间连贯性检查
        for(Integer key:res.keySet()){
            List<Integer> list=res.get(key);
            for(Integer i:list){
                if(data.taskWeights[i]>data.truckCaptitys[key] && data.s[i]==0){
                    System.out.println("任务"+i+"在车"+key+"上，不满足载荷。");
                    System.exit(0);
                }
            }
            int size=list.size();
            for(int i=0;i<size-1;i++){
                if(times[list.get(i)]+data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)]
                        >times[list.get(i+1)]+epsilon){
                    System.out.println("同一个车上的任务不满足时间连贯性");
                    System.out.println(times[list.get(i)]+data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)]);
                    System.out.println(times[list.get(i)]);
                    System.out.println(data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)]);
                    System.out.println(times[list.get(i+1)]);
                    System.exit(0);
                }
            }
        }
        System.out.println("承重和时间连续性，检查通过");

        //时间窗
        for(int i=1;i<data.n;i++){
            if(times[i]+epsilon<data.earlyTime[i] || times[i]-epsilon>data.lateTime[i]){
                System.out.println("不满足时间窗");
                System.exit(0);
            }
        }
        System.out.println("时间窗检查通过");
        showSolution();
    }

    public void showSolution(){
        System.out.println("objection is "+objection);
        System.out.println("The solution is as follow:");
        for(Integer key:res.keySet()){
            System.out.print("第"+key+"个车执行任务序列：");
            List<Integer> list=res.get(key);
            for(Integer i:list){
                System.out.print(i+"  ");
            }
            System.out.println();
            /*List<Double> list1=time.get(key);
            int size=list1.size();
            for(int i=1;i<size;i++){
                System.out.print(time.get(key).get(i)-time.get(key).get(i-1)-data.carryTaskTime[list.get(i-1)]-data.w[list.get(i-1)][list.get(i)]+" ");
            }
            System.out.println();*/
        }
        System.out.println("Carrying task Time is as follow:");
        for(int i=1;i<data.n;i++){
            System.out.println("第"+i+"个任务执行时间："+Data.double_truncate(times[i])+
            ","+Data.double_truncate(times[i]+data.carryTaskTime[i]));
        }
    }

    public  double calObjection(HashMap<Integer,List<Integer>> res){
        double sum=0.0;
        for(Integer key:res.keySet()){
            List<Integer> list=res.get(key);
            int size=list.size();
            if(size==0) continue;
            sum+=data.w[0][list.get(0)];
            for(int i=0;i<size-1;i++){
                sum+=data.w[list.get(i)][list.get(i+1)];
            }
            sum+=data.w[list.get(size-1)][0];
        }
        return sum;
    }
}
