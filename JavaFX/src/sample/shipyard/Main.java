package sample.shipyard;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IloException {
        int task=Parameter.task;
        int truck=Parameter.truck;
        /*输入、 任务数、车数、路口数、堆位数*/
        List<Integer> trucks=new ArrayList<Integer>();
        trucks.add(2);
        trucks.add(8);
        trucks.add(10);
        trucks.add(13);
        trucks.add(16);
        Data data=new Data(task,truck,16,237,trucks);
        try {
            data.readData("Data/shipyard_ovoc.xls");
        }catch (Exception e){
            e.printStackTrace();
        }
        String temp="1";
        if(temp.equals("1")) {
            System.out.println("GA");
            Genetic ga = new Genetic(task, truck, Parameter.ga_size, data);
            ga.Solve();
        }else if(temp.equals("2")) {
            TabuSearch ts = new TabuSearch(data, Parameter.size);
            ts.Solve();
        }else if(temp.equals("3")) {
            //Genetic ga = new Genetic(task, truck, Parameter.ga_size, data);
            //ga.Solve1();
            //int[] tasks = ga.getTasks();
            //int[] trucks = ga.getTrucks();
            //TabuSearch ts = new TabuSearch(data, Parameter.size);
            //ts.setInitialSolution(tasks, trucks);
        }else if(temp.equals("4")) {
            System.out.println("cplex");
            long start = System.currentTimeMillis();
            VRPTW problem = new VRPTW(data);
            problem.build_model();
            problem.Solve();
            long end = System.currentTimeMillis();
            System.out.println("Solving Time:" + (end - start) / 1000.0 + " seconds");
        }

        //BranchAndBound b=new BranchAndBound(task,truck,data);
        //b.Solve();
    }
}
