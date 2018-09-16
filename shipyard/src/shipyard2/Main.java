package shipyard2;

import ilog.concert.IloException;


public class Main {
    private static Data data;
    public static void main(String[] args) throws IloException {
        getData();
        /*long start=System.currentTimeMillis();
        Model problem=new Model(data);
        problem.build_model();
        problem.Solve();
        long end=System.currentTimeMillis();
        System.out.println("求解耗时："+(end-start)/1000.0);*/
        TabuSearch ts=new TabuSearch(data);
        ts.update();
        //VariableNeighbourSearch VNS=new VariableNeighbourSearch(data);
        //VNS.update();
    }

    private static void getData(){
        data=new Data(Parameter.N,Parameter.T,16,224);//输入  任务数、车数、路口数、堆位数
        try {
            data.readData("data/shipyard.xls");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
