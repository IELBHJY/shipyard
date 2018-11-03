package shipyard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/***
 * 本类是遗传算法求解shipyard_model1，和小论文中方法一致。
 */
public class Genetic {
    /*
    * 任务个数，平板车个数
    * */
     int task;
     int truck;
     int popsize;
     double elipsion=1e-3;
     double time_penty_weight=10;
     double crossover_rate=0.4;
     double mutation_rate=0.4;
     double reproduction_rate=0.2;
     double best_value;
     int[][] parent_tasks;
     int[][] parent_trucks;
     int[][] son_tasks;
     int[][] son_trucks;
     int[] isFeason;
     double[][] taskTimes;
     int[] select;
     double[] fitness;
     Data data;

    public Genetic(int task, int truck,int popsize,Data data) {
        this.task = task;
        this.truck = truck;
        this.popsize=popsize;
        isFeason=new int[popsize+1];
        select=new int[popsize+1];
        fitness=new double[popsize+1];
        this.data=data;
        parent_tasks=new int[popsize+1][task+1];
        parent_trucks=new int[popsize+1][task+1];
        taskTimes=new double[popsize+1][task+1];
        son_tasks=new int[popsize+1][task+1];
        son_trucks=new int[popsize+1][task+1];
    }

    private void creatInitialSolution(){
         for(int i=1;i<=popsize;i++){
             List<Integer> list=new ArrayList<>();
             for(int j=1;j<=task;j++) {
                 int num = 1 + (int) (Math.random() * task);
                 while (list.contains(num)) {
                     num = 1 + (int) (Math.random() * task);
                 }
                 list.add(num);
                 parent_tasks[i][j]=num;
                 int weight=data.taskWeights[num];
                 int k=1;
                 if(weight<=data.truckCaptitys[k]){
                     parent_trucks[i][j]=1+(int)(Math.random()*truck);
                     continue;
                 }
                 while (data.truckCaptitys[k] < weight) {
                     k++;
                 }
                 num = k + (int) (Math.random() * (truck + 1 - k));
                 parent_trucks[i][j] = num;
             }
         }
    }

    private void calFitness(){
        //1、先将每个染色体解码
        //2、再分别计算每个染色体的fitness
        for(int i=1;i<=popsize;i++){
            HashMap<Integer,List<Integer>> solution=new HashMap<>();
            decoding(parent_tasks[i],parent_trucks[i],solution);
            for(Integer truck_num:solution.keySet()){
                List<Integer> path=solution.get(truck_num);
                if(path.size()==0){
                    fitness[i]+=Parameter.PENTY_VALUE;
                    continue;
                }
                fitness[i]+=calPathCost(path);
                double time_penty=calTasksTime(i,path);
                if(time_penty<elipsion){
                    isFeason[i]=1;
                }
                fitness[i]+=time_penty*time_penty_weight;
            }
        }
        //将最好的个体存放到int[0][] parent_tasks，parent_trucks
    }

    private void decoding(int[] task, int[] truck, HashMap<Integer,List<Integer>> solution){
        for(int k=1;k<=this.truck;k++){
            solution.put(k,new ArrayList<>());
        }
        for(int i=1;i<=this.task;i++){
            if(solution.get(truck[i]).size()!=0){
                solution.get(truck[i]).add(task[i]);
            }else{
                List<Integer> list=new ArrayList<>();
                list.add(task[i]);
                solution.put(truck[i],list);
            }
        }
    }

    private double calPathCost(List<Integer> path){
        double res=0.0;
          int size=path.size();
          res+=data.w[0][path.get(0)];
          for(int i=0;i<size-1;i++){
              res+=data.w[path.get(i)][path.get(i+1)];
          }
          res+=data.w[path.get(size-1)][0];
          return res;
    }

    private double calTasksTime(int m,List<Integer> list){
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

    private void selected(){
        double sum=0.0;
        int i;
        for(i=1;i<=popsize;i++){
            sum+=fitness[i];
        }
        for(i=1;i<=popsize*(crossover_rate+mutation_rate);i++){
            double rand=Math.random()*((int)sum);
            double temp=0.0;
            int label=0;
            while(temp<rand){
                label++;
                temp+=fitness[label];
            }
            select[i]=label;
        }
        double min=fitness[1];
        int min_label=1;
        for(int j=1;j<=popsize;j++){
            if(j<=popsize*reproduction_rate){
                select[i++]=j;
                if(fitness[j]<min){
                    min=fitness[j];
                    min_label=j;
                }
            }else{
                if(fitness[j]<min){
                    for(int k=(int) (popsize*(crossover_rate+mutation_rate))+1;k<=popsize;k++){
                        if(select[k]==min_label){
                            select[k]=j;
                            min=fitness[j];
                            min_label=j;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void creatSonSolution(){
        for(int i=1;i<=(int)(popsize*crossover_rate);i+=2){
            crossover(select[i],select[i+1],i,i+1);
        }
        for(int i=(int)(popsize*crossover_rate)+1;i<=(int)(popsize*(crossover_rate+mutation_rate));i++){
            mutation(select[i],i);
        }
        for(int i=(int)(popsize*(crossover_rate+mutation_rate))+1;i<=popsize;i++){
            reproduction(select[i],i);
        }
    }

    private void crossover(int first,int second,int p1,int p2){

    }

    private void mutation(int first,int p1){

    }

    private void reproduction(int first,int p1){

    }

    private void copySon2Parent(){

    }

    public void Solve(){
       creatInitialSolution();
       calFitness();
       selected();
    }
}
