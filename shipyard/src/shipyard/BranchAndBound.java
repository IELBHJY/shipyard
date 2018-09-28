package shipyard;

import ilog.concert.IloException;

/**
 * 该类作为分支定价算法框架中的分支定界
 * **/
public class BranchAndBound {

    private int task;
    private int truck;
    private Data data;
    private Double bestValue;


    public BranchAndBound(int task,int truck,Data data) {
        this.task=task;
        this.truck=truck;
        this.data=data;
    }

    private void firstMPModel() throws IloException {
         CG mp=new CG(task,truck,task,data);
         mp.creatInitalSolution();
         mp.buildMPModel();

     }

     public void Solve() throws IloException{
        firstMPModel();
     }
}
