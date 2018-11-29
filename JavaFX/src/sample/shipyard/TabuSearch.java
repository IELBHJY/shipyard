package sample.shipyard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TabuSearch {
    final double inf=Double.MAX_VALUE;
    int task;
    int truck;
    int size;
    int label;
    public int[][] tasks;//从1开始
    public int[][] trucks;//从1开始
    int[] prior;//从1开始 prior[j]=i means the end time if i before the start time j
    double[] objections;
    double best;
    int[] bestTasks;
    int[] bestTrucks;
    double[] bestTimes;
    int[] action;//每次迭代中的最优个体的操作。
    int[] labels;
    static boolean[] isFeasion;
    static boolean[] isPrior;
    static double[][] taskTimes;
    double[] teshe_time;
    double[][] AllperTruckcost;
    double[] bestPerTruckcost;
    static double weight1=1.0;
    static double weight2=1.0;
    public static HashMap<Integer,List<Integer>> res;
    static HashMap<Integer,int[]> operations;
    static HashMap<String,Integer> tabuTable;
    static HashMap<String,Integer> tabuTable1;
    static HashMap<String,Integer> tabuTable2;
    static Data data;

    public TabuSearch(Data data,int size) {
        best=inf;
        this.data = data;
        task=data.n-1;
        truck=data.t-1;
        action=new int[3];
        tasks=new int[size][data.n];
        trucks=new int[size][data.n];
        bestTasks=new int[data.n];
        bestTrucks=new int[data.n];
        bestTimes=new double[data.n];
        taskTimes=new double[size][data.n];
        objections=new double[size];
        tabuTable=new HashMap<String, Integer>();
        tabuTable1=new HashMap<String, Integer>();
        tabuTable2=new HashMap<String, Integer>();
        operations=new HashMap<Integer, int[]>();
        isFeasion=new boolean[size];
        isPrior=new boolean[size];
        prior=data.prior;
        AllperTruckcost=new double[size][truck];
        bestPerTruckcost=new double[truck];
        this.size=size;
        labels=new int[size];
    }

    public void setInitialSolution(int[] tasks,int[] trucks){
        this.tasks[0]=tasks.clone();
        this.trucks[0]=trucks.clone();
        long start=System.currentTimeMillis();
        int current_iteration=0;
        int improve_count=0;
        while(current_iteration<Parameter.tsga_iteration && improve_count<Parameter.improve_iteration) {
            double before=best;
            creatNeighbours1();
            evaluteSolution();
            updateSolution(current_iteration);
            specialAmnesty();
            specialAmnesty1();
            specialAmnesty2();
            current_iteration++;
            double after=best;
            if(before-after<=Parameter.epsilon){
                improve_count++;
            }else{
                improve_count=0;
            }
        }
        long end=System.currentTimeMillis();
        showSolutions();
        Solution solution=new Solution(data,best,res,bestTimes);
        solution.feasion(true);
        System.out.println("共迭代了:"+current_iteration+"次,Solving Time:"+(end-start)/1000.0+" seconds");
    }

    public void creatFirstSolutions(){
        for(int i=1;i<size;i++){
            List<Integer> list=new ArrayList<Integer>();
            for(int j=1;j<data.n;j++) {
                int num = 1 + (int) (Math.random() * task);
                while (list.contains(num)) {
                    num = 1 + (int) (Math.random() * task);
                }
                list.add(num);
                tasks[i][j]=num;
                int weight=data.taskWeights[num];
                int k=1;
                if(weight<=data.truckCaptitys[k]){
                    trucks[i][j]=k;
                    continue;
                }
                while (data.truckCaptitys[k] < weight) {
                    k++;
                }
                num = k + (int) (Math.random() * (truck + 1 - k));
                trucks[i][j] = num;
            }
        }
    }

    public double calObjection(int m){
        HashMap<Integer,List<Integer>> solution=new HashMap<Integer, List<Integer>>();
        for(int i=1;i<data.t;i++){
            solution.put(i,new ArrayList<Integer>());
        }
        for(int i=1;i<data.n;i++){
            solution.get(trucks[m][i]).add(tasks[m][i]);
        }
        //计算目标函数
        double res=calObjection(m,solution);
        //判断优先级是否满足，不满足则加惩罚
        /*double penty=0.0;
        for(int i=1;i<=task;i++){
            if(prior[i]>0){
                int priorTask=prior[i];
                if(taskTimes[m][priorTask]+data.carryTaskTime[priorTask]>taskTimes[m][i]){
                    penty+=10*(taskTimes[m][priorTask]+data.carryTaskTime[priorTask]-taskTimes[m][i]);
                }
            }
        }
        isPrior[m]=(penty==0) ? true :false;
        //res+=weight2*penty;*/
        return res;
    }

    public  double calObjection(int m,HashMap<Integer,List<Integer>> solution){
        double res=0;
        double penty=0.0;
        for(Integer key:solution.keySet()){
            List<Integer> list=solution.get(key);
            if(list.size()==0) {
                res+=Parameter.PENTY_VALUE;
                continue;
            }
            int size=list.size();
            int first=list.get(0);
            int last=list.get(size-1);
            res+=data.w[0][first];
            for(int i=0;i<size-1;i++){
                res+=data.w[list.get(i)][list.get(i+1)];
            }
            res+=data.w[last][0];
            penty+=calTasksTime(m,list);
            AllperTruckcost[m][key-1]=res+10*penty;
        }
        res+=weight1*50*penty;
        if(penty<0.1) {
            isFeasion[m] = true;
        }else{
            isFeasion[m]=false;
        }
        return res;
    }

    public static double calTasksTime(int m,List<Integer> list){
        double res=0;
        if(list.size()==0){
            return res;
        }
        int first=list.get(0);
        if(data.w[0][first]>=data.earlyTime[first] && data.w[0][first]<=data.lateTime[first]){
            taskTimes[m][first]=data.w[0][first];
        }else if(data.w[0][first]<data.earlyTime[first]){
            taskTimes[m][first]=data.earlyTime[first];
        }else if(data.w[0][first]>data.lateTime[first]){
            res+=data.w[0][first]-data.lateTime[first];
            taskTimes[m][first]=data.w[0][first];
        }
        for(int i=1;i<list.size();i++){
            double temp=taskTimes[m][list.get(i-1)]+data.carryTaskTime[list.get(i-1)]+data.w[list.get(i-1)][list.get(i)];
            if(temp>=data.earlyTime[list.get(i)] && temp<=data.lateTime[list.get(i)]){
                taskTimes[m][list.get(i)]=temp;
            }else if(temp<data.earlyTime[list.get(i)]){
                taskTimes[m][list.get(i)]=data.earlyTime[list.get(i)];
            }else if(temp>data.lateTime[list.get(i)]){
                res+=temp-data.lateTime[list.get(i)];
                taskTimes[m][list.get(i)]=temp;
            }
        }
        return res;
    }

    public void creatNeighbours(){
        operations.clear();
        for(int i=1;i<size;i++){
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            int num=(int) (Math.random()*10);
            if(num<5) {
                int[] a=crossover(temp1,temp2);
                operations.put(i,a);
            }else {
                int[] a=mutation(temp1,temp2);
                operations.put(i,a);
            }
            tasks[i]=temp1.clone();
            trucks[i]=temp2.clone();
        }
    }

    public void creatNeighbours1(){
        operations.clear();
        for(int i=1;i<size;i++){
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            int num=(int) (Math.random()*5);
            if(i<size/4) {
                int[] a=crossover1(temp1,temp2);
                operations.put(i,a);
                labels[i]=1;
            } else if(i<size/2){
                int[] a=mutation1(temp1,temp2);
                operations.put(i,a);
                labels[i]=2;
            } else if(i<3*size/4){
                int[] a=crossover(temp1,temp2);
                operations.put(i,a);
                labels[i]=3;
            }else{
                int[] a=mutation(temp1,temp2);
                operations.put(i,a);
                labels[i]=4;
            }
            tasks[i]=temp1.clone();
            trucks[i]=temp2.clone();
        }
    }

    private int[] crossover(int[] a,int[] b){
        int first=1+(int)(Math.random()*task);
        int last=1+(int)(Math.random()*task);
        int task1=a[first];
        int task2=a[last];
        String temp1=String.valueOf(task1)+"-"+String.valueOf(task2);
        if(task1>task2) {
            temp1 = String.valueOf(task2) + "-" + String.valueOf(task1);
        }
        while(last==first || tabuTable.keySet().contains(temp1)){
            last=1+(int)(Math.random()*task);
            task2=a[last];
            temp1=String.valueOf(task1)+"-"+String.valueOf(task2);
            if(task1>task2) {
                temp1 = String.valueOf(task2) + "-" + String.valueOf(task1);
            }
        }
        task1=a[first];
        task2=a[last];
        int temp=a[first];
        a[first]=a[last];
        a[last]=temp;
        if(data.truckCaptitys[b[first]]<data.taskWeights[a[first]]){
            int weight = data.taskWeights[a[first]];
            int k = 1;
            while (data.truckCaptitys[k] < weight) {
                k++;
            }
            b[first] = k + (int) (Math.random() * (truck + 1 - k));
        }
        if(data.truckCaptitys[b[last]]<data.taskWeights[a[last]]){
            int weight = data.taskWeights[a[last]];
            int k = 1;
            while (data.truckCaptitys[k] < weight) {
                k++;
            }
            b[last] = k + (int) (Math.random() * (truck + 1 - k));
        }
        /*temp=b[first];
        b[first]=b[last];
        b[last]=temp;*/
        return new int[]{task1,task2};
    }

    private int[] crossover1(int[] a,int[] b){
        int[] res=new int[3];
        HashMap<Integer,List<Integer>> solution=new HashMap<Integer, List<Integer>>();
        for(int i=1;i<data.t;i++){
            solution.put(i,new ArrayList<Integer>());
        }
        for(int i=1;i<data.n;i++){
            solution.get(b[i]).add(a[i]);
        }
        int k=1+(int)(Math.random()*(data.t-1));
        List<Integer> list=solution.get(k);
        while(solution.get(k).size()<2){
            k=1+(int)(Math.random()*(data.t-1));
            list=solution.get(k);
        }
        int p1=(int)(Math.random()*list.size());
        int num1=list.get(p1);
        int p2=(int)(Math.random()*list.size());
        while(p2==p1){
            p2=(int)(Math.random()*list.size());
        }
        int num2=list.get(p2);
        String actions2=String.valueOf(num1)+"-"+String.valueOf(num2)+"-"+String.valueOf(k);
        String actions1=String.valueOf(num2)+"-"+String.valueOf(num1)+"-"+String.valueOf(k);
        while(tabuTable1.keySet().contains(actions1) || tabuTable1.keySet().contains(actions2)){
            k=1+(int)(Math.random()*(data.t-1));
            list=solution.get(k);
            while(solution.get(k).size()<2){
                k=1+(int)(Math.random()*(data.t-1));
                list=solution.get(k);
            }
            p1=(int)(Math.random()*list.size());
            num1=list.get(p1);
            p2=(int)(Math.random()*list.size());
            while(p2==p1){
                p2=(int)(Math.random()*list.size());
            }
            num2=list.get(p2);
            actions2=String.valueOf(num1)+"-"+String.valueOf(num2)+"-"+String.valueOf(k);
            actions1=String.valueOf(num2)+"-"+String.valueOf(num1)+"-"+String.valueOf(k);
        }
        int first = 0, second = 0;
        for (int i = 1; i < data.n; i++) {
            if (b[i] == k) {
                if (a[i] == num1 || a[i] == num2) {
                    if (first == 0) first = i;
                    if (first > 0) second = i;
                }
            }
        }
        int temp = a[first];
        a[first] = a[second];
        a[second] = temp;
        temp = b[first];
        b[first] = b[second];
        b[second] = temp;
        res[0]=a[first];
        res[1]=a[second];
        res[2]=k;
        return res;
    }

    private int[] mutation(int[] a,int[] b){
        int first=1+(int)(Math.random()*task);
        int last=1+(int)(Math.random()*task);
        int task1=a[first];
        int task2=a[last];
        String temp1=String.valueOf(task1)+"-"+String.valueOf(task2);
        if(task1>task2) {
            temp1 = String.valueOf(task2) + "-" + String.valueOf(task1);
        }
        while(last==first || tabuTable.keySet().contains(temp1)){
            last=1+(int)(Math.random()*task);
            task2=a[last];
            temp1=String.valueOf(task1)+"-"+String.valueOf(task2);
            if(task1>task2) {
                temp1 = String.valueOf(task2) + "-" + String.valueOf(task1);
            }
        }
        task1=a[first];
        task2=a[last];
        int k=1;
        while(data.truckCaptitys[k]<data.taskWeights[task1]){
            k++;
        }
        int num=k+(int)(Math.random()*(truck+1-k));
        b[first]=num;
        k=1;
        while(data.truckCaptitys[k]<data.taskWeights[task2]){
            k++;
        }
        num=k+(int)(Math.random()*(truck+1-k));
        b[last]=num;
        return new int[]{task1,task2};
    }

    private int[] mutation1(int[] a,int[] b){
        int[] res=new int[3];
        int first=1+(int)(Math.random()*task);
        int p=0;
        for(int i=1;i<data.n;i++){
            if(a[i]==first){
                p=i;
                break;
            }
        }
        int weight = data.taskWeights[first];
        int k = 1;
        while (data.truckCaptitys[k] < weight) {
            k++;
        }
        k = k + (int) (Math.random() * (truck + 1 - k));
        String actions=String.valueOf(first)+"-"+String.valueOf(b[p])+"-"+String.valueOf(k);
        while(tabuTable2.keySet().contains(actions)) {
            first = 1 + (int) (Math.random() * task);
            p = 0;
            for (int i = 1; i < data.n; i++) {
                if (a[i] == first) {
                    p = i;
                    break;
                }
            }
            weight = data.taskWeights[first];
            k = 1;
            while (data.truckCaptitys[k] < weight) {
                k++;
            }
            k = k + (int) (Math.random() * (truck + 1 - k));
            actions = String.valueOf(first) + "-" + String.valueOf(b[p]) + "-" + String.valueOf(k);
        }
        //remove task first from b[p] to k
        res[0]=first;
        res[1]=k;
        res[2]=b[p];
        b[p]=k;
        return res;
    }

    public void evaluteSolution(){
        /*
        * objections[0]存储本次中最好的值
        * */

        for(int i=1;i<size;i++){
            objections[i]=calObjection(i);
        }
        label=1;
        objections[0]=objections[1];
        for(int i=1;i<size;i++){
            if(objections[0]>objections[i]) {
                objections[0] = objections[i];
                label=i;
            }
        }
    }

    public void updateSolution(int current_iteration){
        if (best > objections[0] && isFeasion[label]) {
            best = objections[0];
            System.out.println("第"+current_iteration+"代，目标函数："+best);
            bestTasks = tasks[label].clone();
            bestTrucks = trucks[label].clone();
            bestTimes = taskTimes[label].clone();;
        }
        if(isFeasion[label]){
            weight1*=0.99;
        }else{
            weight1/=0.99;
        }
        for (int i = 1; i < data.n; i++) {
            tasks[0][i] = tasks[label][i];
            trucks[0][i] = trucks[label][i];
            taskTimes[0][i]=taskTimes[label][i];
        }
        if(operations.keySet().contains(label)){
            action=operations.get(label);
            //updateTabuTable();
            if(labels[label]==1){
                updateTabuTable11();
            }else if(labels[label]==2){
                updateTabuTable12();
            }else{
                updateTabuTable();
            }
        }
    }

    public void updateTabuTable(){
         String key=String.valueOf(action[0])+"-"+String.valueOf(action[1]);
         if(action[0]>action[1]) {
             key = String.valueOf(action[1]) + "-" + String.valueOf(action[0]);
         }
         List<String> list=new ArrayList<String>(tabuTable.keySet());
         for(String keys:list){
             if(tabuTable.get(keys)==0){
                 tabuTable.remove(keys);
             } else {
                 tabuTable.put(keys,tabuTable.get(keys)-1);
             }
         }
         tabuTable.put(key,Parameter.tabuLength);
    }

    public void updateTabuTable11(){
        String key1=String.valueOf(action[0])+"-"+String.valueOf(action[1])+"-"+String.valueOf(action[2]);
        List<String> list=new ArrayList<String>(tabuTable1.keySet());
        for(String keys:list){
            if(tabuTable1.get(keys)==0){
                tabuTable1.remove(keys);
            } else {
                tabuTable1.put(keys,tabuTable1.get(keys)-1);
            }
        }
        tabuTable1.put(key1,Parameter.tabuLength);
    }

    public void updateTabuTable12(){
        String key1=String.valueOf(action[0])+"-"+String.valueOf(action[1])+"-"+String.valueOf(action[2]);
        List<String> list=new ArrayList<String>(tabuTable2.keySet());
        for(String keys:list){
            if(tabuTable2.get(keys)==0){
                tabuTable2.remove(keys);
            } else {
                tabuTable2.put(keys,tabuTable2.get(keys)-1);
            }
        }
        tabuTable2.put(key1,Parameter.tabuLength);
    }

    public boolean specialAmnesty(){
        if(tabuTable==null) return false;
        double min=inf;
        boolean isSA=false;
        for(String key:tabuTable.keySet()){
            teshe_time=new double[task+1];
            int task1=Integer.parseInt(key.split("-")[0]);
            int task2=Integer.parseInt(key.split("-")[1]);
            int first=0,last=0;
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            for(int i=1;i<data.n;i++){
                if(temp1[i]==task1)
                    first=i;
                if(temp1[i]==task2)
                    last=i;
            }
            int c1=temp2[first];
            int c2=temp2[last];
            int temp=temp1[first];
            temp1[first]=temp1[last];
            temp1[last]=temp;
            if(data.taskWeights[task1]>data.truckCaptitys[c2]){
                int k=1;
                while(data.truckCaptitys[k]<data.taskWeights[task1]){
                    k++;
                }
                int num=k+(int)(Math.random()*(truck+1-k));
                temp2[last]=num;
            }
            if(data.taskWeights[task2]>data.truckCaptitys[c1]){
                int k=1;
                while(data.truckCaptitys[k]<data.taskWeights[task2]){
                    k++;
                }
                int num=k+(int)(Math.random()*(truck+1-k));
                temp2[first]=num;
            }
            HashMap<Integer,List<Integer>> map=new HashMap<Integer, List<Integer>>();
            for(int i=1;i<data.t;i++){
                List<Integer> list=new ArrayList<Integer>();
                map.put(i,list);
            }
            for(int i=1;i<data.n;i++){
                map.get(temp2[i]).add(temp1[i]);
            }
            double result=calObjection(map);
            if(result<best && result<min){
                System.out.println("特赦规则成立: "+result+" "+best);
                min=result;
                best=result;
                //objections[0]=result;
                bestTasks=temp1.clone();
                bestTrucks=temp2.clone();
                bestTimes=teshe_time.clone();
                //tasks[0]=temp1.clone();
                //trucks[0]=temp2.clone();
                //taskTimes[0]=teshe_time.clone();
                isSA=true;
            }
        }
        return isSA;
    }

    public boolean specialAmnesty1(){
        if(tabuTable1==null) return false;
        double min=inf;
        boolean isSA=false;
        for(String key:tabuTable1.keySet()){
            teshe_time=new double[task+1];
            int task1=Integer.parseInt(key.split("-")[0]);
            int task2=Integer.parseInt(key.split("-")[1]);
            int truck=Integer.parseInt(key.split("-")[2]);
            int first=0,last=0;
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            for(int i=1;i<data.n;i++){
                if(temp1[i]==task1)
                    first=i;
                if(temp1[i]==task2)
                    last=i;
            }
            if(temp2[first]!=truck || temp2[last]!=truck){
                continue;
            }
            int temp=temp1[first];
            temp1[first]=temp1[last];
            temp1[last]=temp;
            HashMap<Integer,List<Integer>> map=new HashMap<Integer, List<Integer>>();
            for(int i=1;i<data.t;i++){
                List<Integer> list=new ArrayList<Integer>();
                map.put(i,list);
            }
            for(int i=1;i<data.n;i++){
                map.get(temp2[i]).add(temp1[i]);
            }
            double result=calObjection(map);
            if(result<best && result<min){
                System.out.println("特赦规则成立: "+result+" "+best);
                min=result;
                best=result;
                //objections[0]=result;
                bestTasks=temp1.clone();
                bestTrucks=temp2.clone();
                bestTimes=teshe_time.clone();
                //tasks[0]=temp1.clone();
                //trucks[0]=temp2.clone();
                //taskTimes[0]=teshe_time.clone();
                isSA=true;
            }
        }
        return isSA;
    }

    public boolean specialAmnesty2(){
        if(tabuTable2==null) return false;
        double min=inf;
        boolean isSA=false;
        for(String key:tabuTable2.keySet()){
            teshe_time=new double[task+1];
            int task1=Integer.parseInt(key.split("-")[0]);
            int truck1=Integer.parseInt(key.split("-")[1]);
            int truck2=Integer.parseInt(key.split("-")[2]);
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            int p=0;
            for(int i=1;i<data.n;i++){
                if(temp1[i]==task1){
                    p=i;
                    break;
                }
            }
            if(temp2[p]!=truck1){
                continue;
            }
            if(data.taskWeights[task1]<=data.truckCaptitys[truck2]) {
                temp2[p] = truck2;
            }else{
                System.out.println("tabuTable2 has error");
                System.exit(0);
            }
            HashMap<Integer,List<Integer>> map=new HashMap<Integer, List<Integer>>();
            for(int i=1;i<data.t;i++){
                List<Integer> list=new ArrayList<Integer>();
                map.put(i,list);
            }
            for(int i=1;i<data.n;i++){
                map.get(temp2[i]).add(temp1[i]);
            }
            double result=calObjection(map);
            if(result<best && result<min){
                System.out.println("特赦规则成立: "+result+" "+best);
                min=result;
                best=result;
                //objections[0]=result;
                bestTasks=temp1.clone();
                bestTrucks=temp2.clone();
                bestTimes=teshe_time.clone();
                //tasks[0]=temp1.clone();
                //trucks[0]=temp2.clone();
                //taskTimes[0]=teshe_time.clone();
                isSA=true;
            }
        }
        return isSA;
    }

    public  double calObjection(HashMap<Integer,List<Integer>> solution){
        double res=0;
        double penty=0.0;
        for(Integer key:solution.keySet()){
            List<Integer> list=solution.get(key);
            if(list.size()==0) {
                res+=10000;
                continue;
            }
            int size=list.size();
            int first=list.get(0);
            int last=list.get(size-1);
            res+=data.w[0][first];
            for(int i=0;i<size-1;i++){
                res+=data.w[list.get(i)][list.get(i+1)];
            }
            res+=data.w[last][0];
            penty+=calTasksTime(list);
        }
        res+=100*penty;
        return res;
    }

    public  double calTasksTime(List<Integer> list){
        double res=0;
        if(list.size()==0){
            return res;
        }
        int first=list.get(0);
        if(data.w[0][first]>=data.earlyTime[first] && data.w[0][first]<=data.lateTime[first]){
            teshe_time[first]=data.w[0][first];
        }else if(data.w[0][first]<data.earlyTime[first]){
            teshe_time[first]=data.earlyTime[first];
        }else if(data.w[0][first]>data.lateTime[first]){
            res+=data.w[0][first]-data.lateTime[first];
            teshe_time[first]=data.w[0][first];
        }
        for(int i=1;i<list.size();i++){
            double temp=teshe_time[list.get(i-1)]+data.carryTaskTime[list.get(i-1)]+data.w[list.get(i-1)][list.get(i)];
            if(temp>=data.earlyTime[list.get(i)] && temp<=data.lateTime[list.get(i)]){
                teshe_time[list.get(i)]=temp;
            }else if(temp<data.earlyTime[list.get(i)]){
                teshe_time[list.get(i)]=data.earlyTime[list.get(i)];
            }else if(temp>data.lateTime[list.get(i)]){
                res+=temp-data.lateTime[list.get(i)];
                teshe_time[list.get(i)]=temp;
            }
        }
        return res;
    }

    public void Solve(){
        int current_iteration=0;
        int improve_count=0;
        long start=System.currentTimeMillis();
        creatFirstSolutions();
        evaluteSolution();
        updateSolution(current_iteration);
        while(current_iteration<Parameter.max_iteration && improve_count<Parameter.improve_iteration) {
            double before=best;
            creatNeighbours1();
            evaluteSolution();
            updateSolution(current_iteration);
            specialAmnesty();
            specialAmnesty1();
            specialAmnesty2();
            current_iteration++;
            double after=best;
            if(before-after<=Parameter.epsilon){
                improve_count++;
            }else{
                improve_count=0;
            }
        }
        long end=System.currentTimeMillis();
        showSolutions();
        Solution solution=new Solution(data,best,res,bestTimes);
        solution.feasion(true);
        System.out.println("共迭代了:"+current_iteration+"次,Solving Time:"+(end-start)/1000.0+" seconds");
    }

    public void showSolutions(){
        res=new HashMap<Integer, List<Integer>>(16);
        for(int i=1;i<data.n;i++){
            if(!res.keySet().contains(bestTrucks[i])){
                List<Integer> list=new ArrayList<Integer>();
                list.add(bestTasks[i]);
                res.put(bestTrucks[i],list);
            }else{
                res.get(bestTrucks[i]).add(bestTasks[i]);
            }
        }
        /*System.out.println(calObjection(res));
        for(Integer key:res.keySet()){
            System.out.print(key+" car: ");
            int size=res.get(key).size();
            for(int i=0;i<size;i++){
                System.out.print(res.get(key).get(i)+" ");
            }
            System.out.println();
        }*/
    }


    //-----------------------分割线-----------------------//

    public double[] getBestPerTruckcost(){
        double[] ans=new double[bestPerTruckcost.length];
        ans[0]=bestPerTruckcost[0];
        for(int i=1;i<bestPerTruckcost.length;i++){
            ans[i]=bestPerTruckcost[i]-bestPerTruckcost[i-1];
        }
        return ans;
    }

    private void updateTable(){
        if(operations.keySet().contains(label)){
            action=operations.get(label);
            if(label<size/2){
                updateTabuTable1();
            }else{
                updateTabuTable2();
            }
        }else{
            System.out.println("operations.keySet().contains(label)");
            return;
        }
    }

    public void updateTabuTable1(){
        String key=String.valueOf(action[0])+"-"+String.valueOf(action[1])+"-"+String.valueOf(action[2]);
        tabuTable1.put(key,Parameter.tabuLength);
        for(String keys:tabuTable1.keySet()){
            if(tabuTable1.get(key)==0){
                tabuTable1.remove(keys);
            }else{
                tabuTable1.put(keys,tabuTable1.get(keys)-1);
            }
        }
    }

    public void updateTabuTable2(){
        String key=String.valueOf(action[0])+"-"+String.valueOf(action[1])+"-"+String.valueOf(action[2]);
        tabuTable2.put(key,Parameter.tabuLength);
        for(String keys:tabuTable2.keySet()){
            if(tabuTable2.get(key)==0){
                tabuTable2.remove(keys);
            }else{
                tabuTable2.put(keys,tabuTable2.get(keys)-1);
            }
        }
    }

}
