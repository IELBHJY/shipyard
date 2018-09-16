package shipyard;

import ilog.concert.IloException;

public class Main {

    public static void main(String[] args) throws IloException {
        int task=12;
        int truck=4;
        /*输入、 任务数、车数、路口数、堆位数*/
        Data data=new Data(task,truck,16,224);
        try {
            data.readData("data/shipyard1.xls");
        }catch (Exception e){
            e.printStackTrace();
        }
        long start=System.currentTimeMillis();
        VRPTW problem=new VRPTW(data);
        problem.build_model();
        problem.Solve();
        long end=System.currentTimeMillis();
        System.out.println("Solving Time:"+ (end-start)/1000.0 +" seconds");
        TabuSearch ts=new TabuSearch(data,Parameter.size);
        ts.update();
        CG masterproblem=new CG(task,truck,4,data);
        double[] c=ts.getBestPerTruckcost();
        System.out.println(c[0]+" "+c[1]+" "+c[2]+" "+c[3]);
        int[] b=new int[16];
        for(int i=0;i<task+truck;i++){
            b[i]=1;
        }
        masterproblem.inital(TabuSearch.res,c,b);
        masterproblem.buildMPModel();
    }
}
