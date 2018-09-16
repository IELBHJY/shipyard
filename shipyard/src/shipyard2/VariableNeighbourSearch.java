package shipyard2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class VariableNeighbourSearch {
    final double inf=Double.MAX_VALUE;
    final double epsilon = 0.1;
    int task;
    int truck;
    int size;
    int label;
    double gamma=1.0;
    public int[][] tasks;//从1开始
    public int[][] trucks;//从1开始
    public int[][] trucks1;
    public int[] x_tasks;
    public int[] x_trucks;
    public int[] x_trucks1;
    double x_value;
    double[] objections;
    double best;
    double currentValues;
    int[] bestTasks;
    int[] bestTrucks;
    int[] bestTrucks1;
    double[] bestTimes;
    double count1;
    double count2;
    boolean res=true;
    LinkedHashMap<Integer,Double>[][] times;//times[i][j]
    LinkedHashMap<Integer,Double>[] currentTimes;
    HashMap<Integer,int[]> operations;
    HashMap<Integer,int[]> operations1;
    ArrayList<String> tabuTable;//任务号-车号的禁忌表
    ArrayList<Integer> tabuLength;//禁忌表的步长
    int[] action;//每次迭代中的最优个体的操作。
    static boolean[] isFeasion;
    //HashMap<Integer,List<Integer>> res;
    Data data;

    public VariableNeighbourSearch(Data data) {
        count1=0.5;
        count2=0.5;
        best=inf;
        currentValues=inf;
        this.data = data;
        task=data.n-1;//15
        truck=data.t-1;//4
        action=new int[2];
        this.size=Parameter.size;
        tasks=new int[size][data.n];
        trucks=new int[size][data.n];
        trucks1=new int[size][data.n];
        bestTasks=new int[data.n];
        bestTrucks=new int[data.n];
        bestTimes=new double[data.n];
        objections=new double[size];
        isFeasion=new boolean[size];
        times=new LinkedHashMap[size][data.t];
        currentTimes=new LinkedHashMap[data.t];
        operations=new HashMap<>();
        operations1=new HashMap<>();
        tabuTable=new ArrayList<>();
        tabuLength=new ArrayList<>();
    }

    public void creatFirstSolutions(){
        for(int i=1;i<size;i++){
            List<Integer> list=new ArrayList<>();
            for(int j=1;j<data.n;j++) {
                int num = 1 + (int) (Math.random() * task);
                while (list.contains(num)) {
                    num = 1 + (int) (Math.random() * task);
                }
                list.add(num);
                tasks[i][j]=num;
                if(data.s[num]==0) {
                    int weight = data.taskWeights[num];
                    int k = 1;
                    if (weight <= data.truckCaptitys[k]) {
                        num=1+(int) (Math.random()*(truck));
                        trucks[i][j]=num;
                        trucks1[i][j]=num;
                        continue;
                    }
                    while (data.truckCaptitys[k] < weight) {
                        k++;
                    }
                    num = k + (int) (Math.random() * (truck + 1 - k));
                    trucks[i][j] = num;
                    trucks1[i][j]= num;
                }else{
                    int weight = data.taskWeights[num];
                    int k=1+(int)(Math.random()*truck);
                    List<Integer> temp=new ArrayList<>();
                    for(int l=1;l<=truck;l++){
                        if(l==k) continue;
                        if(weight<=data.truckCaptitys[k]+data.truckCaptitys[l]){
                            temp.add(l);
                        }
                    }
                    num=(int)(Math.random()*temp.size());
                    trucks[i][j]=k;
                    trucks1[i][j]=temp.get(num);
                    if(temp.get(num)==k){
                        System.out.println("k==temp.get(num)");
                        System.exit(0);
                    }
                }
            }
        }
    }

    public void showSolutions(){
        for(int i=1;i<=task;i++){
            System.out.print(tasks[1][i]+" ");
        }
        System.out.println();
        for(int i=1;i<=task;i++){
            System.out.print(trucks[1][i]+" ");
        }
        System.out.println();
        for(int i=1;i<=task;i++){
            System.out.print(trucks1[1][i]+" ");
        }
        System.out.println();
    }

    public void showBestSolution(){
        HashMap<Integer,List<Integer>> solution=new HashMap<>();
        for(int i=1;i<data.t;i++){
            solution.put(i,new ArrayList<>());
        }
        for(int i=1;i<data.n;i++){
            if(bestTrucks[i]==bestTrucks1[i]) {
                solution.get(bestTrucks[i]).add(bestTasks[i]);
            }else{
                solution.get(bestTrucks[i]).add(bestTasks[i]);
                solution.get(bestTrucks1[i]).add(bestTasks[i]);
            }
        }
        for(Integer key:solution.keySet()){
            List<Integer> list=solution.get(key);
            if(list.size()>0){
                System.out.print("第"+key+"车： ");
                for(Integer i:list){
                    System.out.print(i+" ");
                }
            }
            System.out.println();
        }
        for(int i=1;i<data.n;i++){
            System.out.print(bestTimes[i]+"   ");
        }
    }

    public void calTaskTimes(int m,HashMap<Integer,List<Integer>> solution){
        for(Integer key:solution.keySet()){
            List<Integer> list=solution.get(key);
            if(list.size()==0) continue;
            int first=list.get(0);
            if(data.w[0][first]>=data.earlyTime[first] && data.w[0][first]<=data.lateTime[first]){
                times[m][key].put(first,data.w[0][first]);
            }else if(data.w[0][first]<data.earlyTime[first]){
                times[m][key].put(first,(double)data.earlyTime[first]);
            }else if(data.w[0][first]>data.lateTime[first]){
                times[m][key].put(first,data.w[0][first]);
            }
            for(int i=1;i<list.size();i++){
                double temp=times[m][key].get(list.get(i-1))+data.carryTaskTime[list.get(i-1)]+data.w[list.get(i-1)][list.get(i)];
                if(temp>=data.earlyTime[list.get(i)] && temp<=data.lateTime[list.get(i)]){
                    times[m][key].put(list.get(i),temp);
                }else if(temp<data.earlyTime[list.get(i)]){
                    times[m][key].put(list.get(i),(double)data.earlyTime[list.get(i)]);
                }else if(temp>data.lateTime[list.get(i)]){
                    times[m][key].put(list.get(i),temp);
                }
            }
        }
    }

    public double calObjection(int m){
        HashMap<Integer,List<Integer>> solution=new HashMap<>();
        for(int i=1;i<data.t;i++){
            solution.put(i,new ArrayList<>());
        }
        for(int i=1;i<data.n;i++){
            if(trucks[m][i]==trucks1[m][i]) {
                solution.get(trucks[m][i]).add(tasks[m][i]);
                if(data.s[tasks[m][i]]==1){
                    System.out.println("data.s[tasks[m][i]]==1");
                    System.exit(0);
                }
            }else{
                if(trucks[m][i]==0){
                    System.out.println("trucks[m][i]==0");
                    System.exit(0);
                }
                solution.get(trucks[m][i]).add(tasks[m][i]);
                solution.get(trucks1[m][i]).add(tasks[m][i]);
            }
        }
        for(int i=1;i<data.t;i++){
            times[m][i]=new LinkedHashMap<>();
        }
        //计算任务执行时间
        calTaskTimes(m,solution);
        //修改任务时间
        repairTimes(m,solution);
        //计算目标函数
        double[] pentys=new double[2];
        double res=calObjection(m,solution,pentys);
        return Data.double_truncate(res);
    }

    public  double calObjection(int m,HashMap<Integer,List<Integer>> solution,double[] pentys){
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
            res+=calObjection(m,key,list,pentys);
            if(pentys[1]>epsilon) {
                penty += pentys[1];
            }
            if(pentys[0]>epsilon){
                System.out.println("penty[0]>0");
                System.exit(0);
            }
        }
        res+=gamma*penty;
        return res;
    }

    private double calObjection(int m,int key,List<Integer> list,double[] pentys){
        double res=0.0;
        int size=list.size();
        if(size==0) return res;
        int first=list.get(0);
        if(times[m][key].get(first)<data.earlyTime[first]){
            res+=data.earlyTime[first]-times[m][key].get(first);
            pentys[0]+=data.earlyTime[first]-times[m][key].get(first);
        }else if(times[m][key].get(first)>data.lateTime[first]){
            pentys[1]+=times[m][key].get(first)-data.lateTime[first];
        }
        for(int i=1;i<size;i++){
            double temp=times[m][key].get(list.get(i))-(times[m][key].get(list.get(i-1))+data.carryTaskTime[list.get(i-1)]+data.w[list.get(i-1)][list.get(i)]);
            if(temp+epsilon<0){
                System.out.println("车上任务时间出错");
                System.exit(0);
            }
            res+=temp;
            if(times[m][key].get(list.get(i))<data.earlyTime[list.get(i)]){
                pentys[0]+=data.earlyTime[list.get(i)]-times[m][key].get(list.get(i));
            }else if(times[m][key].get(list.get(i))>data.lateTime[list.get(i)]){
                pentys[1]+=times[m][key].get(list.get(i))-data.lateTime[list.get(i)];
            }
        }
        return res;
    }

    public void repairTimes(int m,HashMap<Integer,List<Integer>> solution){
        List<Integer> saveSuperTasks=new ArrayList<>();
        int size=saveSuperTasks.size();
        while(size<Parameter.superNum){
            int[] ans=find(solution,saveSuperTasks);
            int task=ans[0];
            int t1=ans[1];
            int t2=ans[2];
            try {
                double time1 = times[m][t1].get(task);
                double time2 = times[m][t2].get(task);
                if (time1 > time2) {
                    times[m][t2].put(task, time1);
                    //更新t2车后边的任务时间
                    repairTruckTimes(m, solution, t2, task);
                } else if (time1 < time2) {
                    times[m][t1].put(task, time2);
                    //更新t1车后边任务时间
                    repairTruckTimes(m, solution, t1, task);
                }
            }catch (Exception e){
                System.out.println(ans[0]+" "+ans[1]+" "+ans[2]);
                e.printStackTrace();
                System.exit(0);
            }
            size=saveSuperTasks.size();
        }
    }

    private int[] find(HashMap<Integer,List<Integer>> solution,List<Integer> saveSuperTasks){
        int[] ans=new int[3];
        int[] temps=new int[solution.keySet().size()+1];
        int superTask=0;
        for(Integer key:solution.keySet()){
            List<Integer> list=solution.get(key);
            for(Integer i:list){
                if(data.s[i]==1 && !saveSuperTasks.contains(i)){
                    if(superTask==0) {
                        temps[key] = i;
                        superTask=i;
                        break;
                    }else{
                        if(i==superTask){
                            temps[key]=i;
                            break;
                        }
                    }
                }
            }
        }
        for(int i=1;i<temps.length;i++){
            for(int j=i+1;j<temps.length;j++){
                if(temps[i]==temps[j] && temps[i]!=0){
                    ans[0]=temps[i];
                    ans[1]=i;
                    ans[2]=j;
                }
            }
        }
        if(ans[0]==0 && ans[1]==0 && ans[2]==0){
            System.out.println(temps[1]+" "+temps[2]+" "+temps[3]+" "+temps[4]);
        }
        saveSuperTasks.add(ans[0]);
        return ans;
    }

    private void repairTruckTimes(int m,HashMap<Integer,List<Integer>> solution,int truck,int task){
        List<Integer> list=solution.get(truck);
        int index=list.indexOf(task);
        if(index==-1) System.exit(0);
        int size=list.size();
        for(int i=index;i<size-1;i++){
            double temp=times[m][truck].get(list.get(i))+data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)];
            if(temp>=data.earlyTime[list.get(i+1)] && temp<=data.lateTime[list.get(i+1)]){
                times[m][truck].put(list.get(i+1),temp);
            }else if(temp<data.earlyTime[list.get(i+1)]){
                times[m][truck].put(list.get(i+1),(double)data.earlyTime[list.get(i+1)]);
            }else if(temp>data.lateTime[list.get(i+1)]){
                times[m][truck].put(list.get(i+1),temp);
            }
        }
    }

    public void evaluteSolution(){
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

    public void copyBestSolution(){
        //判断这个解是不是可行解再加入最优解中
        if(objections[0]<best && res) {
            best=objections[0];
            System.out.println("最优解"+best+"已经更新");
            bestTasks = tasks[label].clone();
            bestTrucks = trucks[label].clone();
            bestTrucks1 = trucks1[label].clone();
            for(int t=1;t<data.t;t++){
                for(Integer key:times[label][t].keySet()){
                    bestTimes[key]=times[label][t].get(key);
                }
            }
        }
    }

    public void copyCurrentSolution(){
        currentValues=objections[0];
        tasks[0]=tasks[label].clone();
        trucks[0]=trucks[label].clone();
        trucks1[0]=trucks1[label].clone();
        for(int k=1;k<data.t;k++){
            currentTimes[k]=new LinkedHashMap<>();
            currentTimes[k]=(LinkedHashMap<Integer, Double>) times[label][k].clone();
        }
    }

    public boolean isFeasion(){
        boolean res=true;
        for(int k=1;k<data.t;k++){
            LinkedHashMap<Integer,Double> map=times[label][k];
            List<Integer> list=new ArrayList<>(map.keySet());
            int size=list.size();
            if(size==0) continue;
            int first=list.get(0);
            if(data.w[0][first]>map.get(first)){
                System.out.println("data.w[0][first]>s[first]");
                System.exit(0);
            }
            if(map.get(first)>data.lateTime[first]){
                //System.out.println("不满足时间窗口约束");
                res=false;
            }
            if(data.s[first]==0 && data.taskWeights[first]>data.truckCaptitys[k]){
                System.out.println("不满足重量约束约束");
                System.exit(0);
            }
            for(int i=0;i<size-1;i++){
                if(map.get(list.get(i))+data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)]>map.get(list.get(i+1))){
                    System.out.println("error");
                    System.exit(0);
                }
                if(map.get(list.get(i+1))>data.lateTime[list.get(i+1)]+epsilon){
                    //System.out.println("不满足时间窗口约束");
                    res=false;
                }
                if(data.s[list.get(i+1)]==0 && data.taskWeights[list.get(i+1)]>data.truckCaptitys[k]){
                    System.out.println("不满足重量约束约束");
                    System.exit(0);
                }
            }
        }
        return res;
    }

    public void creatNeighbours(int current_way){
        for(int i=1;i<size;i++){
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            int[] temp3=trucks1[0].clone();
            if(current_way==1){
                int[] a=insert(temp1,temp2,temp3);
                operations.put(i,a);
            }else if(current_way==2){
                int[] a=crossover(temp1,temp2,temp3);
                operations.put(i,a);
            }else if(current_way==3){
                inverseMutation(temp1,temp2,temp3);
            }
            tasks[i]=temp1.clone();
            trucks[i]=temp2.clone();
            trucks1[i]=temp3.clone();
        }
    }

    private int[] insert(int[] a,int[] b,int[] c){
        int[] ans=new int[3];
        int first=1+(int)(Math.random()*task);
        int p=0;
        for(int i=1;i<data.n;i++){
            if(a[i]==first){
                p=i;
            }
        }
        ans[0]=first;
        if(b[p]==c[p]){
            int weight = data.taskWeights[first];
            int k = 1;
            while (data.truckCaptitys[k] < weight) {
                k++;
            }
            ans[1]=b[p];
            k = k + (int) (Math.random() * (truck + 1 - k));
            b[p] = k;
            c[p] = k;
            ans[2]=b[p];
        }else{
            int weight = data.taskWeights[first];
            int k=1+(int)(Math.random()*truck);
            List<Integer> temp=new ArrayList<>();
            for(int l=1;l<=truck;l++){
                if(l==k) continue;
                if(weight<=data.truckCaptitys[k]+data.truckCaptitys[l]){
                    temp.add(l);
                }
            }
            if(b[p]!=k){
                ans[1]=b[p];
            }else if(c[p]!=k){
                ans[1]=c[p];
            }
            int num=(int)(Math.random()*temp.size());
            b[p]=k;
            c[p]=temp.get(num);
            if(b[p]!=ans[1]){
                ans[2]=b[p];
            }else if(c[p]!=ans[1]){
                ans[2]=c[p];
            }else{
                System.out.println("insert ");
                System.exit(0);
            }
        }
        return ans;
    }

    private int[] crossover(int[] a,int[] b,int[] c){
        int[] ans=new int[3];
        int k=1+(int)(Math.random()*(data.t-1));
        LinkedHashMap<Integer,Double> list=currentTimes[k];
        while(list.size()<2) {
            k=1+(int)(Math.random()*(data.t-1));
            list=currentTimes[k];
        }
        int p1=(int)(Math.random()*list.size());
        int num1=new ArrayList<>(list.keySet()).get(p1);
        int p2=(int)(Math.random()*list.size());
        while(p2==p1){
            p2=(int)(Math.random()*list.size());
        }
        int num2=new ArrayList<>(list.keySet()).get(p2);
        int first = 0, second = 0;
        for (int i = 1; i < data.n; i++) {
            if (b[i] == k || c[i] == k) {
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
        temp = c[first];
        c[first] = c[second];
        c[second] = temp;
        ans[0]=a[first];
        ans[1]=a[second];
        ans[2]=k;
        return ans;
    }

    private void inverseMutation(int[] a,int[] b,int[] c){
        int first=1+(int)(Math.random()*task);
        int second=1+(int)(Math.random()*task);
        while(second==first){
            second=1+(int)(Math.random()*task);
        }
        int before=first>second ? first:second;
        int last=first>second ? second:first;
        List<Integer> list=new ArrayList<>();
        for(int i=before;i<=last;i++){
            list.add(a[i]);
        }
        for(int i=before;i<=last;i++){
            first=(int)(Math.random()*list.size());
            second=list.get(first);
            a[i]=second;
            if(data.s[a[i]]==0){
                if(b[i]==c[i]){
                    if(data.taskWeights[a[i]]>data.truckCaptitys[b[i]]){
                        int k=b[i];
                        while(data.truckCaptitys[k]>data.taskWeights[a[i]]){
                            k++;
                        }
                        b[i]=k+(int)(Math.random()*(truck+1-k));
                        c[i]=b[i];
                    }
                }else{
                    if(data.taskWeights[a[i]]>data.truckCaptitys[b[i]] &&
                            data.taskWeights[a[i]]>data.truckCaptitys[c[i]]){
                        int k=1;
                        while(data.truckCaptitys[k]>data.taskWeights[a[i]]){
                            k++;
                        }
                        b[i]=k+(int)(Math.random()*(truck+1-k));
                        c[i]=b[i];
                    }else if(data.taskWeights[a[i]]<=data.truckCaptitys[b[i]]){
                        c[i]=b[i];
                    }else if(data.taskWeights[a[i]]<=data.truckCaptitys[c[i]]){
                        b[i]=c[i];
                    }
                }
            }else{
                if(b[i]==c[i]){
                    List<Integer> temp=new ArrayList<>();
                    for(int l=1;l<=truck;l++){
                        if(l==b[i]) continue;
                        if(data.taskWeights[a[i]]<=data.truckCaptitys[b[i]]+data.truckCaptitys[l]){
                            temp.add(l);
                        }
                    }
                    int num=(int)(Math.random()*temp.size());
                    b[i]=temp.get(num);
                }else{
                    if(data.taskWeights[a[i]]>data.truckCaptitys[b[i]]+data.truckCaptitys[c[i]]){
                        int min=b[i]>c[i] ? c[i]:b[i];
                        List<Integer> temp=new ArrayList<>();
                        for(int l=1;l<=truck;l++){
                            if(l==min) continue;
                            if(data.taskWeights[a[i]]<=data.truckCaptitys[min]+data.truckCaptitys[l]){
                                temp.add(l);
                            }
                        }
                        int num=(int)(Math.random()*temp.size());
                        b[i]=min;
                        c[i]=num;
                    }
                }
            }
            list.remove(first);
        }
    }

    public void update(){
        //定义三种结构，N(k) is the same with N(l)
        //1、insert  2、crossover  3、inverseMutation
        //creat initial solution
        creatFirstSolutions();
        evaluteSolution();
        copyCurrentSolution();
        res=isFeasion();
        copyBestSolution();
        int k,l=1;
        int max_k=2;
        for(k=1;k<=max_k;k++){
            //shaking at x` with k structure
            l=1;
            creatNeighbours(k);
            evaluteSolution();
            copyCurrentSolution();
            res=isFeasion();
            copyBestSolution(); // x`
            //vns local search
            while(l<=max_k){
                int current_iteration=0;
                while(current_iteration<Parameter.ts_iteration) {
                    creatNeighbours(l);
                    evaluteSolution();
                    copyCurrentSolution();
                    current_iteration++;
                }
                creatNeighbours(l);
                evaluteSolution();
                if(objections[0]<currentValues){
                    l=1;
                    copyCurrentSolution();
                }else{
                    l++;
                }
                if(currentValues<best){
                    res=isFeasion();
                    copyBestSolution();
                }
            }
            /*TabuSearch ts=new TabuSearch(data);
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            int[] temp3=trucks1[0].clone();
            LinkedHashMap<Integer,Double>[] times=currentTimes.clone();
            ts.setSolution(temp1,temp2,temp3,times);
            double ts_result=ts.startTS();
            System.out.println(ts_result+"  "+currentValues);
            if(ts_result<currentValues){
                List<int[]> output=ts.outputSolution();
                tasks[0]=output.get(0);
                trucks[0]=output.get(1);
                trucks1[0]=output.get(2);
                currentValues=ts_result;
                res=true;
                copyBestSolution();
            }*/
        }
        System.out.println(best);
        showBestSolution();
    }

    private void updateTable(){
        if(operations.keySet().contains(label)){
            action=operations.get(label);
        }else{
            System.out.println("!operations.keySet().contains(label)");
            System.exit(0);
        }
        int size=tabuLength.size();
        for(int i=0;i<size;i++){
            int length=tabuLength.get(i);
            length--;
            tabuLength.add(i,length);
            tabuLength.remove(i+1);
            if(tabuLength.get(i)==0){
                tabuTable.remove(i);
            }
        }
        String act=String.valueOf(action[0])+'-'+String.valueOf(action[1])+'-'+String.valueOf(action[2]);
        tabuTable.add(act);
        tabuLength.add(Parameter.tabuLength);
    }
}
