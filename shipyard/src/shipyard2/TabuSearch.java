package shipyard2;



import java.util.*;

public class TabuSearch {
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
    double[] objections;
    double best;
    int[] bestTasks;
    int[] bestTrucks;
    int[] bestTrucks1;
    double[] bestTimes;
    double count1;
    double count2;
    LinkedHashMap<Integer,Double>[][] times;//times[i][j]
    LinkedHashMap<Integer,Double>[] currentTimes;
    int[] action;//每次迭代中的最优个体的操作。
    static boolean[] isFeasion;
    HashMap<Integer,List<Integer>> res;
    HashMap<Integer,int[]> operations;
    ArrayList<String> tabuTable1;//任务号-车号的禁忌表
    ArrayList<Integer> tabuLength1;//禁忌表的步长
    ArrayList<String> tabuTable2;//任务号-车号的禁忌表
    ArrayList<Integer> tabuLength2;//禁忌表的步长
    ArrayList<String> tabuTable3;//任务号-车号的禁忌表
    ArrayList<Integer> tabuLength3;//禁忌表的步长
    ArrayList<String> tabuTable4;//任务号-车号的禁忌表
    ArrayList<Integer> tabuLength4;//禁忌表的步长
    Data data;
    HashMap<Integer,List<Integer>> methods;
    public TabuSearch(Data data) {
        count1=0.5;
        count2=0.5;
        best=inf;
        this.data = data;
        task=data.n-1;
        truck=data.t-1;
        action=new int[2];
        this.size=Parameter.size;
        tasks=new int[size][data.n];
        trucks=new int[size][data.n];
        trucks1=new int[size][data.n];
        bestTasks=new int[data.n];
        bestTrucks=new int[data.n];
        bestTimes=new double[data.n];
        objections=new double[size];
        tabuTable1=new ArrayList<>();
        tabuLength1=new ArrayList<>();
        tabuTable2=new ArrayList<>();
        tabuLength2=new ArrayList<>();
        tabuTable3=new ArrayList<>();
        tabuLength3=new ArrayList<>();
        tabuTable4=new ArrayList<>();
        tabuLength4=new ArrayList<>();
        operations=new HashMap<>();
        isFeasion=new boolean[size];
        times=new LinkedHashMap[size][data.t];
        currentTimes=new LinkedHashMap[data.t];
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
        for(int i=1;i<size;i++){
            for(int j=1;j<data.t;j++){
                times[i][j]=new LinkedHashMap<>();
            }
        }
    }

    public void setSolution(int[] a,int[] b,int[] c,LinkedHashMap<Integer,Double>[] times){
        tasks[0]=a.clone();
        trucks[0]=b.clone();
        trucks1[0]=c.clone();
        currentTimes=times;
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
        String TAG="calObjection(int m)";
        HashMap<Integer,List<Integer>> solution=new HashMap<>();
        for(int i=1;i<data.t;i++){
            solution.put(i,new ArrayList<>());
        }
        for(int i=1;i<data.n;i++){
            if(trucks[m][i]==trucks1[m][i]) {
                solution.get(trucks[m][i]).add(tasks[m][i]);
                if(data.s[tasks[m][i]]==1){
                    System.out.println(TAG +" data.s[tasks[m][i]]==1");
                    System.exit(0);
                }
            }else{
                if(trucks[m][i]==0){
                    System.out.println(TAG+ " trucks[m][i]==0");
                    System.exit(0);
                }
                solution.get(trucks[m][i]).add(tasks[m][i]);
                solution.get(trucks1[m][i]).add(tasks[m][i]);
            }
        }
        for(int i=1;i<data.t;i++){
            times[m][i].clear();
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
            //计算等待时间和惩罚时间
            res+=calObjection(m,key,list,pentys);
            if(pentys[1]>epsilon) {
                penty += pentys[1]*10;
            }
            if(pentys[0]>epsilon){
                System.out.println("penty[0]>epsilon");
                System.exit(0);
            }
        }
        res+=gamma*penty;
        return res;
    }

    private double calObjection(int m,int key,List<Integer> list,double[] pentys){
        String TAG="calObjection";
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
                System.out.println(TAG+" 车上任务时间出错");
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
        //更新后边的时间
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
        //更新前边的时间
        for(int i=index;i>=1;i--){
            double temp=times[m][truck].get(list.get(i))-data.w[list.get(i-1)][list.get(i)]-data.carryTaskTime[list.get(i-1)];
            if(temp>=data.earlyTime[list.get(i-1)] && temp<=data.lateTime[list.get(i-1)]){
                times[m][truck].put(list.get(i-1),temp);
            }else if(temp<data.earlyTime[list.get(i-1)]){
                times[m][truck].put(list.get(i-1),(double)data.earlyTime[list.get(i-1)]);
            }else if(temp>data.lateTime[list.get(i-1)]){
                times[m][truck].put(list.get(i-1),temp);
            }
        }
    }

    public void evaluteSolution(){
        boolean res;
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
        res=testTime();
        copyCurrentSolution(res);
        copyBestSolution(res);
    }

    public void updateWeights(){
        //根据本次迭代中的解情况，调整构建邻域结构的权重
        for(Integer key:methods.keySet()){
            if(methods.get(key).contains(label)){
                if(key==1){
                    count1+=0.1;
                    if(count1>1){
                        count1=1;
                    }else if(count1<0){
                        count1=0;
                    }
                }else if(key==2){
                    count1-=0.1;
                    if(count1>1){
                        count1=1;
                    }else if(count1<0){
                        count1=0;
                    }
                }
                break;
            }
        }
    }

    public void copyBestSolution(boolean res){
        //判断这个解是不是可行解再加入最优解／／System.out.println(res);
        if(objections[0]<best && res) {
            best=objections[0];
            //System.out.println("最优解"+best+"已经更新");
            //System.out.println(label);
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

    public void copyCurrentSolution(boolean res){
        tasks[0]=tasks[label].clone();
        trucks[0]=trucks[label].clone();
        trucks1[0]=trucks1[label].clone();
        for(int k=1;k<data.t;k++){
            currentTimes[k]=new LinkedHashMap<>();
            currentTimes[k]=(LinkedHashMap<Integer, Double>) times[label][k].clone();
        }
        //根据当前解是否可行，调整参数。
        if(res){
            gamma*=0.9;
        }else{
            gamma/=0.9;
        }
    }

    public boolean testTime(){
        String TAG="testTime";
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
                res=false;
            }
            if(data.s[first]==0 && data.taskWeights[first]>data.truckCaptitys[k]){
                System.out.println("不满足重量约束约束");
                System.exit(0);
            }
            for(int i=0;i<size-1;i++){
                if(map.get(list.get(i))+data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)]-epsilon>map.get(list.get(i+1))){
                    System.out.println(map.get(list.get(i))+data.carryTaskTime[list.get(i)]+data.w[list.get(i)][list.get(i+1)]);
                    System.out.println(map.get(list.get(i+1)));
                    System.out.println(TAG+" 时间窗不满足");
                    System.exit(0);
                }
                if(map.get(list.get(i+1))>data.lateTime[list.get(i+1)]+epsilon){
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

    public void creatNeighbours(){
        operations=new HashMap<>();
        methods=new HashMap<>();
        List<Integer> list1=new ArrayList<>();
        List<Integer> list2=new ArrayList<>();
        for(int i=1;i<size;i++){
            int[] temp1=tasks[0].clone();
            int[] temp2=trucks[0].clone();
            int[] temp3=trucks1[0].clone();
            if(i<1*size/4) {
                int[] a=crossover(temp1,temp2,temp3);
                operations.put(i,a);
            }else if(i<2*size/4){
                int[] a=insert(temp1,temp2,temp3);
                operations.put(i,a);
            }/*else if(i<3*size/5){
                int[] a=insertTW(temp1,temp2,temp3);
                operations.put(i,a);
            }*/
            else if (i<3*size/4){
                int[] a=change(temp1,temp2,temp3);
                operations.put(i,a);
            }else if(i<4*size/4){
                int[] a=change1(temp1,temp2,temp3);
                operations.put(i,a);
            }
            tasks[i]=temp1.clone();
            trucks[i]=temp2.clone();
            trucks1[i]=temp3.clone();
        }
    }

    // 返回任务号-变化前车号-变化后车号，如果是多车运输，取其中一个就可以
    private int[] insert(int[] a,int[] b,int[] c){
        int[] ans=new int[3];
        int first=1+(int)(Math.random()*task);
        int p=0;
        for(int i=1;i<data.n;i++){
            if(a[i]==first){
               p=i;
               break;
            }
        }
        if(b[p]==c[p]){
            int weight = data.taskWeights[first];
            int k = 1;
            while (data.truckCaptitys[k] < weight) {
                k++;
            }
            k = k + (int) (Math.random() * (truck + 1 - k));
            String actions=String.valueOf(first)+"-"+String.valueOf(b[p])+"-"+String.valueOf(k);
            while(tabuTable2.contains(actions)){
                boolean temp=true;
                while(b[p]!=c[p] || temp) {
                    first = 1 + (int) (Math.random() * task);
                    p = 0;
                    for (int i = 1; i < data.n; i++) {
                        if (a[i] == first) {
                            p = i;
                        }
                    }
                    temp=false;
                }
                weight=data.taskWeights[first];
                k = 1;
                while (data.truckCaptitys[k] < weight) {
                    k++;
                }
                k = k + (int) (Math.random() * (truck + 1 - k));
                actions=String.valueOf(first)+"-"+String.valueOf(b[p])+"-"+String.valueOf(k);
            }
            ans[0]=first;
            ans[1]=k;
            ans[2]=b[p];
            b[p] = k;
            c[p] = k;
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
            int num=(int)(Math.random()*temp.size());
            b[p]=k;
            c[p]=temp.get(num);
        }
        return ans;
    }

    //返回任务号-任务号-车号
    private int[] crossover(int[] a,int[] b,int[] c){
        int[] res=new int[3];
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
        String actions2=String.valueOf(num1)+"-"+String.valueOf(num2)+"-"+String.valueOf(k);
        String actions1=String.valueOf(num2)+"-"+String.valueOf(num1)+"-"+String.valueOf(k);
        while(tabuTable1.contains(actions2) || tabuTable1.contains(actions1)){
            k=1+(int)(Math.random()*(data.t-1));
            list=currentTimes[k];
            while(list.size()<2) {
                k=1+(int)(Math.random()*(data.t-1));
                list=currentTimes[k];
            }
            p1=(int)(Math.random()*list.size());
            num1=new ArrayList<>(list.keySet()).get(p1);
            p2=(int)(Math.random()*list.size());
            while(p2==p1){
                p2=(int)(Math.random()*list.size());
            }
            num2=new ArrayList<>(list.keySet()).get(p2);
            actions2=String.valueOf(num1)+"-"+String.valueOf(num2)+"-"+String.valueOf(k);
            actions1=String.valueOf(num2)+"-"+String.valueOf(num1)+"-"+String.valueOf(k);
            //System.out.println("tabuTable1.contains(actions2) || tabuTable1.contains(actions1)");
            //System.out.println(tabuLength2.size());
        }
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
        res[0]=a[first];
        res[1]=a[second];
        res[2]=k;
        return res;
    }

    //3.将第k辆车上的随机一个任务插入到另外一个车上时间窗顺序最适当的位置,让任务的车辆发生变化----insertTW
    //1.将第k辆车上的随机两个任务交换其顺序  ------crossover
    //4.将第k辆车上的随机一个任务插入到第k辆车上一个新位置，该位置要符合时间窗因素
    //2.将第k辆车上的随机一个任务插入到随机一个车上的随机位置  ------insert
    //5.观看启发式解和最优解的差别，发现将一辆车上的一个任务插入到该车上的另一个位置上 ------change
    //返回任务号-车号
    private int[] insertTW(int[] a,int[] b,int[] c){
        int[] res=new int[3];
        int k=1+(int)(Math.random()*(data.t-1));
        LinkedHashMap<Integer,Double> list=currentTimes[k];
        while(list.size()<2) {
            k=1+(int)(Math.random()*(data.t-1));
            list=currentTimes[k];
        }
        List<Integer> indexs=new ArrayList<>();
        List<Integer> values=new ArrayList<>();
        HashMap<Integer,int[]> map=new HashMap<>();
        for(int i=1;i<data.n;i++){
            if(b[i]==k || c[i]==k){
               indexs.add(i);
               values.add(a[i]);
               map.put(a[i],new int[]{b[i],c[i]});//保存任务的车号
            }
        }
        int p1=(int)(Math.random()*list.size());
        int num1=new ArrayList<>(list.keySet()).get(p1);//获得任务号，插入到时间窗因素上最合适的位置
        String actions=String.valueOf(num1)+"-"+String.valueOf(k);
        while(tabuTable3.contains(actions)){
            k=1+(int)(Math.random()*(data.t-1));
            list=currentTimes[k];
            while(list.size()<2) {
                k=1+(int)(Math.random()*(data.t-1));
                list=currentTimes[k];
            }
            indexs.clear();
            values.clear();
            map=new HashMap<>();
            for(int i=1;i<data.n;i++){
                if(b[i]==k || c[i]==k){
                    indexs.add(i);
                    values.add(a[i]);
                    map.put(a[i],new int[]{b[i],c[i]});//保存任务的车号
                }
            }
            p1=(int)(Math.random()*list.size());
            num1=new ArrayList<>(list.keySet()).get(p1);
            actions=String.valueOf(num1)+"-"+String.valueOf(k);
        }
        res[0]=num1;
        res[1]=k;
        res[2]=k;
        for(int i=0;i<values.size();i++){
            if(values.get(i)==num1) values.remove(i);
        }
        int j=0;
        boolean temp=false;
        for(int i=0;i<indexs.size();i++){
            if(i==indexs.size()-1 && !temp){
                a[indexs.get(i)]=num1;
                b[indexs.get(i)]=map.get(num1)[0];
                c[indexs.get(i)]=map.get(num1)[1];
                continue;
            }
            if(data.earlyTime[num1]<=data.earlyTime[values.get(j)] && !temp){
                a[indexs.get(i)]=num1;
                b[indexs.get(i)]=map.get(num1)[0];
                c[indexs.get(i)]=map.get(num1)[1];
                temp=true;
            }else{
                a[indexs.get(i)]=values.get(j);
                b[indexs.get(i)]=map.get(values.get(j))[0];
                c[indexs.get(i)]=map.get(values.get(j))[1];
                j++;
            }
        }
        return res;
    }
    /**
     * first insert before last in truck t
     * **/
    private int[] change(int[] a,int[] b,int[] c){
        int[] ans=new int[3];
        HashMap<Integer,int[]> map=new HashMap<>();
        int first=1+(int)(Math.random()*task);
        int p=0;
        for(int i=1;i<data.n;i++){
            if(a[i]==first){
                p=i;
                break;
            }
        }
        /**
         * 1 3 4 5 --a
         * 1 2 1 2 --b
         * 1 1 2 2 --c
         * 1 2 3 4 --p
         * **/
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < data.n; i++) {
            if (i == p) {
                continue;
            }
            if (b[i] == b[p] || c[i] == b[p]) {
                list.add(i);
            }
        }
        if (list.size() == 0) {
            return new int[]{0, 0, 0};
        }
        int new_num = (int) (Math.random() * list.size());
        int last_position = list.get(new_num);
        int last=a[last_position];
        String action=String.valueOf(first)+"-"+String.valueOf(last)+"-"+String.valueOf(b[p]);
        while(tabuTable4.contains(action)){
            new_num=(int) (Math.random() * list.size());
            last_position = list.get(new_num);
            last=a[last_position];
            action=String.valueOf(first)+"-"+String.valueOf(last)+"-"+String.valueOf(b[p]);
        }
        map.put(p,new int[]{a[p],b[p],c[p]});
        for(Integer key:list){
            map.put(key,new int[]{a[key],b[key],c[key]});
        }
        list.add(new_num,p);
        List<Integer> list1=new ArrayList<>(list);
        Collections.sort(list1);
        for(int i=0;i<list.size();i++){
            a[list1.get(i)]=map.get(list.get(i))[0];
            b[list1.get(i)]=map.get(list.get(i))[1];
            c[list1.get(i)]=map.get(list.get(i))[2];
        }
        ans[0]=first;
        ans[1]=last;
        ans[2]=b[p];
        return ans;
    }

    private int[] change1(int[] a,int[] b,int[] c){
        int[] ans=new int[3];
        HashMap<Integer,int[]> map=new HashMap<>();
        int first=1+(int)(Math.random()*task);
        int p=0;
        for(int i=1;i<data.n;i++){
            if(a[i]==first){
                p=i;
                break;
            }
        }
        /**
         * 1 3 4 5 --a
         * 1 2 1 2 --b
         * 1 1 2 2 --c
         * 1 2 3 4 --p
         * **/
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < data.n; i++) {
            if (i == p) {
                continue;
            }
            if (b[i] == b[p] || c[i] == b[p]) {
                list.add(i);
            }
        }
        if (list.size() == 0) {
            return new int[]{0, 0, 0};
        }
        int new_num = (int) (Math.random() * list.size());
        int last_position = list.get(new_num);
        int last=a[last_position];
        String action=String.valueOf(first)+"-"+String.valueOf(last)+"-"+String.valueOf(b[p]);
        while(tabuTable4.contains(action)){
            new_num=(int) (Math.random() * list.size());
            last_position = list.get(new_num);
            last=a[last_position];
            action=String.valueOf(first)+"-"+String.valueOf(last)+"-"+String.valueOf(b[p]);
        }
        map.put(p,new int[]{a[p],b[p],c[p]});
        for(Integer key:list){
            map.put(key,new int[]{a[key],b[key],c[key]});
        }
        if(new_num==list.size()-1){
            list.add(p);
        }else{
            list.add(new_num+1,p);
        }
        List<Integer> list1=new ArrayList<>(list);
        Collections.sort(list1);
        for(int i=0;i<list.size();i++){
            a[list1.get(i)]=map.get(list.get(i))[0];
            b[list1.get(i)]=map.get(list.get(i))[1];
            c[list1.get(i)]=map.get(list.get(i))[2];
        }
        ans[0]=first;
        ans[1]=last;
        ans[2]=b[p];
        return ans;
    }

    private void updateTable(){
        if(operations.keySet().contains(label)){
            action=operations.get(label);
            if(label<1*size/4){
                updateTable1();
            }else if(label<2*size/4){
                updateTable2();
            }
        }else{
            System.out.println("!operations.keySet().contains(label)");
            return;
        }
    }

    private void updateTable1(){
        int size=tabuLength1.size();
        boolean isRemoveTop=false;
        for(int i=0;i<size;i++){
            int length=tabuLength1.get(i);
            length--;
            tabuLength1.add(i,length);
            tabuLength1.remove(i+1);
            if(tabuLength1.get(i)==0){
                isRemoveTop=true;
            }
        }
        if(isRemoveTop){
            tabuTable1.remove(0);
            tabuLength1.remove(0);
        }
        String act=String.valueOf(action[0])+"-"+String.valueOf(action[1])+"-"+String.valueOf(action[2]);
        tabuTable1.add(act);
        tabuLength1.add(Parameter.tabuLength);
    }

    private void updateTable2(){
        int size=tabuLength2.size();
        boolean isRemoveTop=false;
        for(int i=0;i<size;i++){
            int length=tabuLength2.get(i);
            length--;
            tabuLength2.add(i,length);
            tabuLength2.remove(i+1);
            if(tabuLength2.get(i)==0){
                isRemoveTop=true;
            }
        }
        if(isRemoveTop){
            tabuTable2.remove(0);
            tabuLength2.remove(0);
        }
        String act=String.valueOf(action[0])+"-"+String.valueOf(action[2])+"-"+String.valueOf(action[1]);
        tabuTable2.add(act);
        tabuLength2.add(Parameter.tabuLength);
    }

    private void updateTable3(){
        int size=tabuLength3.size();
        boolean isRemoveTop=false;
        for(int i=0;i<size;i++){
            int length=tabuLength3.get(i);
            length--;
            tabuLength3.add(i,length);
            tabuLength3.remove(i+1);
            if(tabuLength3.get(i)==0){
                isRemoveTop=true;
            }
        }
        if(isRemoveTop){
            tabuTable3.remove(0);
            tabuLength3.remove(0);
        }
        String act=String.valueOf(action[0])+"-"+String.valueOf(action[1]);
        tabuTable3.add(act);
        tabuLength3.add(Parameter.tabuLength);
    }

    private void updateTable4(){

    }

    public void update(){
        long start=System.currentTimeMillis();
        creatFirstSolutions();
        evaluteSolution();
        int current_iteration=0;
        while(current_iteration<Parameter.max_iteration) {
            //产生邻域解
            creatNeighbours();
            //评估解
            evaluteSolution();
            //updateWeights();
            //特赦规则是否满足

            //更新禁忌表
            updateTable();
            current_iteration++;
            if(current_iteration % 10==0){
                System.out.println("迭代了"+current_iteration+"次，目标函数："+best);
            }
        }
        long end=System.currentTimeMillis();
        System.out.println((end-start)/1000.0);
        System.out.println(best);
        //showBestSolution();
        showSolutions1();
        Solution solution=new Solution(data,best,res,bestTimes);
        solution.feasion(true);
    }

    public void showSolutions1(){
        res=new HashMap<>();
        for(int i=1;i<data.n;i++){
            if(bestTrucks[i]==bestTrucks1[i]){
                if(!res.keySet().contains(bestTrucks[i])){
                    List<Integer> list=new ArrayList<>();
                    list.add(bestTasks[i]);
                    res.put(bestTrucks[i],list);
                }else{
                    res.get(bestTrucks[i]).add(bestTasks[i]);
                }
            }else{
                if(!res.keySet().contains(bestTrucks[i])){
                    List<Integer> list=new ArrayList<>();
                    list.add(bestTasks[i]);
                    res.put(bestTrucks[i],list);
                }else{
                    res.get(bestTrucks[i]).add(bestTasks[i]);
                }
                if(!res.keySet().contains(bestTrucks1[i])){
                    List<Integer> list=new ArrayList<>();
                    list.add(bestTasks[i]);
                    res.put(bestTrucks1[i],list);
                }else{
                    res.get(bestTrucks1[i]).add(bestTasks[i]);
                }
            }
        }
    }

    public double startTS(){
        int current_iteration=0;
        while(current_iteration<Parameter.max_iteration) {
            //产生邻域解
            creatNeighbours();
            //评估解
            evaluteSolution();
            //updateWeights();
            //特赦规则是否满足
            //更新禁忌表
            //updateTable();
            current_iteration++;

        }
        return best;
    }

    public List<int[]> outputSolution(){
        List<int[]> output=new ArrayList<>();
        output.add(bestTasks);
        output.add(bestTrucks);
        output.add(bestTrucks1);
        return output;
    }

    private void Structure1(LinkedHashMap<Integer,Double>[] solution){
        int k=1+(int)(Math.random()*(data.t-1));
        LinkedHashMap<Integer,Double> list=solution[k];
        while(list.size()<2) {
            k=1+(int)(Math.random()*(data.t-1));
            list=solution[k];
        }
        int num=(int)(Math.random()*list.keySet().size());
        int task=0,j=0;
        for(int i:list.keySet()){
            if(j==num){
                task=i;
                break;
            }
            j++;
        }
        int k1=k;
        if(data.s[task]==1) {
            while (k1 == k || solution[k1].keySet().contains(task)) {
                k1 = 1 + (int) (Math.random() * (data.t - 1));
            }
        }else{
            if(k==data.t-1) return;
            while (k1 == k || data.truckCaptitys[k1]<data.taskWeights[task]) {
                k1 = 1 + (int) (Math.random() * (data.t - 1));
            }
        }
        //System.out.println(k+"   "+k1);
        solution[k].remove(task);
        List<Integer> list1=new ArrayList<>();
        for(int t:solution[k1].keySet()){
            if(data.earlyTime[t]<=data.earlyTime[task]){
                list1.add(t);
            }else{
                if(!list1.contains(task)){
                    list1.add(task);
                    list1.add(t);
                }else{
                    list1.add(t);
                }
            }
        }
        if(!list1.contains(task)){
            list1.add(task);
        }
        solution[k1]=new LinkedHashMap<>();
        for(int t:list1){
            solution[k1].put(t,0.0);
        }
    }

    private void Structure2(LinkedHashMap<Integer,Double>[] solution){
        int k=1+(int)(Math.random()*(data.t-1));
        LinkedHashMap<Integer,Double> list=solution[k];
        while(list.size()<2) {
            k=1+(int)(Math.random()*(data.t-1));
            list=solution[k];
        }

    }

    public void creatNeighbours1(){
        for(int i=1;i<size;i++){
            for(int k=1;k<data.t;k++){
                currentTimes[k]=(LinkedHashMap<Integer, Double>) times[label][k].clone();
            }
            //show(currentTimes);
            if(i<size) {
                //Structure1(currentTimes);
            }else if(i<4*size/4){

            }
            //show(currentTimes);
            //test1(currentTimes);
            int j=1;
            List<Integer> temp=new ArrayList<>();
            for(int t=1;t<data.t;t++){
                for(int w:currentTimes[t].keySet()){
                    if(data.s[w]==0){
                        tasks[i][j]=w;
                        trucks[i][j]=t;
                        trucks1[i][j]=t;
                        j++;
                    }else{
                        if(temp.contains(w)){
                            for(int z=1;z<=j;z++){
                                if(tasks[i][z]==w){
                                    trucks1[i][z]=t;
                                    break;
                                }
                            }
                        }else{
                            tasks[i][j]=w;
                            trucks[i][j]=t;
                            temp.add(w);
                            j++;
                        }
                    }
                }
            }
            //System.out.println("Current iteration:"+i);
            //test2(tasks[i],trucks[i],trucks1[i]);
        }
    }

    private void show(LinkedHashMap<Integer,Double>[] solution){
        int size=solution.length;
        for(int i=1;i<size;i++){
            System.out.print("Truck"+i+": ");
            for(Integer task:solution[i].keySet()){
                System.out.print(task+" ");
            }
            System.out.println();
        }
    }

    private void test1(LinkedHashMap<Integer,Double>[] solution){
        int size=solution.length;
        List<Integer> list=new ArrayList<>();
        for(int i=1;i<size;i++){
            for(Integer task:solution[i].keySet()){
                if(!list.contains(task)){
                    list.add(task);
                }else{
                    if(data.s[task]==0){
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void test2(int[] task,int[] truck,int[] truck1){
        for(int i=1;i<task.length;i++){
            if(truck[i]==truck1[i]){
                if(data.s[task[i]]==1) System.exit(0);
            }else{
                if(data.s[task[i]]==0) System.exit(0);
            }
        }
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
}
