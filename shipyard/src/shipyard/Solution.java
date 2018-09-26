package shipyard;

import java.util.HashMap;
import java.util.List;

public class Solution {
    Data data;
    final double epsilon = 0.01;
    HashMap<Integer,List<Integer>> res;
    HashMap<Integer,Double> serverTimes;
    double[] times;
    double objection;
    public Solution(Data data,double objection,HashMap<Integer,List<Integer>> paths,HashMap<Integer,Double> serverTimes,double[] times) {
        this.data = data;
        this.res=paths;
        this.serverTimes=serverTimes;
        this.objection=objection;
        this.times=times;
    }

    public Solution(Data data,double objection,HashMap<Integer,List<Integer>> paths,double[] times) {
        this.data = data;
        this.res=paths;
        this.objection=objection;
        this.times=times;
    }

    public void feasion(){
        //目标函数判断
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
            System.exit(0);
        }
        //车载荷判断
        for(Integer key:res.keySet()){
            List<Integer> list=res.get(key);
            for(Integer i:list){
                if(data.taskWeights[i]>data.truckCaptitys[key]){
                    System.out.println("任务"+i+"在车"+key+"上，不满足载荷。");
                    System.exit(0);
                }
            }
        }
        //时间窗判断
        for(Integer key:serverTimes.keySet()){
            if(serverTimes.get(key)+epsilon<data.earlyTime[key] || serverTimes.get(key)-epsilon>data.lateTime[key]){
                System.out.println("任务"+key+"不满足时间窗约束。");
                System.out.println(serverTimes.get(key)+"  "+data.earlyTime[key]+"  "+data.lateTime[key]);
                System.exit(0);
            }
        }
        for(Integer key:serverTimes.keySet()){
            if(serverTimes.get(key)+data.carryTaskTime[key]+epsilon<Data.E || serverTimes.get(key)+data.carryTaskTime[key]-epsilon>Data.L){
                System.out.println("任务"+key+"不满足车场时间窗约束。");
                System.exit(0);
            }
        }
        for(Integer key:res.keySet()){
            if(res.get(key).size()==0) continue;
            int firstTask=res.get(key).get(0);
            if(serverTimes.get(firstTask)+epsilon<data.w[0][firstTask]){
                System.out.println("车"+key+"上的第一个任务的开始时间小于车从车场行驶到其位置消耗的时间。");
                System.out.println(serverTimes.get(firstTask)+" "+data.w[0][firstTask]);
                System.exit(0);
            }
        }
        showSolution();
    }

    public void feasion(boolean noTWCheck){
        //车载荷判断
        for(Integer key:res.keySet()){
            List<Integer> list=res.get(key);
            for(Integer i:list){
                if(data.taskWeights[i]>data.truckCaptitys[key]){
                    System.out.println("任务"+i+"在车"+key+"上，不满足载荷。");
                    System.exit(0);
                }
            }
        }
        //时间窗
        for(int i=1;i<data.n;i++){
            if(times[i]+epsilon<data.earlyTime[i] || times[i]-epsilon>data.lateTime[i]){
                System.out.println("不满足时间窗");
                System.exit(0);
            }
        }
        //优先级
        for(int i=1;i<data.n;i++){
            if(data.prior[i]>0){
                int num=data.prior[i];
                if(times[i]+data.carryTaskTime[i]+epsilon<times[num]){
                    System.out.println(times[i]+" "+data.carryTaskTime[i]+" "+times[num]);
                    System.out.println("不满足任务优先级约束");
                    System.exit(0);
                }
            }
        }
        //showSolution();
    }

    public void showSolution(){
        System.out.println("objection is "+data.double_truncate(objection));
        System.out.println("The solution is as follow:");
        for(Integer key:res.keySet()){
            System.out.print("第"+key+"个车执行任务序列：");
            List<Integer> list=res.get(key);
            for(Integer i:list){
                System.out.print(i+"  ");
            }
            System.out.println();
        }
        System.out.println("Carrying task Time is as follow:");
        for(int i=1;i<data.n;i++){
            System.out.println("第"+i+"个任务执行时间："+Data.double_truncate(times[i]));
        }
    }

}
