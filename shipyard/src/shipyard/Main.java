package shipyard;

import ilog.concert.IloException;

public class Main {

    public static void main(String[] args) throws IloException {
        int task=Parameter.task;
        int truck=Parameter.truck;
        /*输入、 任务数、车数、路口数、堆位数*/
        Data data=new Data(task,truck,16,224);
        try {
            data.readData("data/shipyard_ovoc.xls");
        }catch (Exception e){
            e.printStackTrace();
        }
        Genetic ga=new Genetic(task,truck,Parameter.ga_size,data);
        ga.Solve();
        int[] tasks=ga.getTasks();
        int[] trucks=ga.getTrucks();
        TabuSearch ts=new TabuSearch(data,Parameter.size);
        ts.setInitialSolution(tasks,trucks);
        //ts.Solve();
/*
        long start=System.currentTimeMillis();
        VRPTW problem=new VRPTW(data);
        problem.build_model();
        problem.Solve();
        long end=System.currentTimeMillis();
        System.out.println("Solving Time:"+ (end-start)/1000.0 +" seconds");
*/
        //BranchAndBound b=new BranchAndBound(task,truck,data);
        //b.Solve();
    }
}
