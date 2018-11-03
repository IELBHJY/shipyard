package shipyard;

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
     double best_value;
     int[][] parent_tasks;
     int[][] parent_trucks;
     int[][] son_tasks;
     int[][] son_trucks;

    public Genetic(int task, int truck,int popsize) {
        this.task = task;
        this.truck = truck;
        this.popsize=popsize;
        parent_tasks=new int[popsize][task];
        parent_trucks=new int[popsize][task];
        son_tasks=new int[popsize][task];
        son_trucks=new int[popsize][task];
    }

    private void creatInitialSolution(){

    }

    private void calFitness(){

    }

    private void selected(){

    }

    private void creatSonSolution(){

    }

    private void crossover(){

    }

    private void mutation(){

    }

    private void elite(){

    }

    public void Solve(){

    }



}
