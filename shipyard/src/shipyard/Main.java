package shipyard;

import ilog.concert.IloException;

public class Main {

    public static void main(String[] args) throws IloException {
        int task=Parameter.task;
        int truck=Parameter.truck;
        /*输入、 任务数、车数、路口数、堆位数*/
        Data data=new Data(task,truck,16,224);
        try {
            data.readData("data/shipyard1.xls");
        }catch (Exception e){
            e.printStackTrace();
        }
        /*System.out.println(data.w[0][5]+data.w[5][12]+data.w[12][4]+data.w[4][6]+data.w[6][10]+data.w[10][11]+data.w[11][0]);
        System.out.println(data.w[0][3]+data.w[3][12]+data.w[12][8]+data.w[8][7]+data.w[7][9]+data.w[9][0]);
        System.out.println(data.w[0][2]+data.w[2][1]+data.w[1][0]);
        System.out.println(data.w[0][3]+data.w[3][12]+data.w[12][4]+data.w[4][6]+data.w[6][8]+data.w[8][0]);
        System.out.println(data.w[0][5]+data.w[5][10]+data.w[10][7]+data.w[7][9]+data.w[9][11]+data.w[11][0]);*/
        long start=System.currentTimeMillis();
        VRPTW problem=new VRPTW(data);
        problem.build_model();
        problem.Solve();
        long end=System.currentTimeMillis();
        System.out.println("Solving Time:"+ (end-start)/1000.0 +" seconds");
        //TabuSearch ts=new TabuSearch(data,Parameter.size);
        //ts.update();
        /*CG masterproblem=new CG(task,truck,2,data);
        double[] c=ts.getBestPerTruckcost();
        int[] b=new int[task+truck];
        for(int i=0;i<task+truck;i++){
            b[i]=1;
        }
        masterproblem.inital(TabuSearch.res,c,b);*/
        //masterproblem.buildMPModel();
        BranchAndBound b=new BranchAndBound(task,truck,data);
        b.Solve();
    }
}
