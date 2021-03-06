package shipyard;

import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CG {
    IloCplex MP;//主问题模型
    IloCplex SP;//子问题
    int H;//任务数
    int T;//平板车种类个数
    int z;//路径个数
    int[][] a;//系数矩阵
    double[] c;//费用系数
    int[] b;
    Data data;
    IloObjective MPCosts;
    IloLinearNumExpr SPCosts;
    IloRange[]   Fill;
    IloNumVarArray path;
    HashMap<Integer,List<Integer>> paths;
    //---------------------
    IloNumVar[] alpha;
    IloNumVar[] beta;
    IloNumVar[][] x;
    IloNumVar[] st;
    IloNumVar[][] y;
    int Q=10000;

    public CG(int h,int t,int z,Data data) throws IloException {
        MP=new IloCplex();
        SP=new IloCplex();
        this.H=h;
        this.T=t;
        this.z=z;
        this.a=new int[h+t][z];
        this.c=new double[z];
        this.b=new int[h+t];
        this.data=data;
        paths=new HashMap<>();
    }

    public CG(){

    }

    //初始化主问题矩阵系数,子问题需要的变量和系数
    public void inital(HashMap<Integer,List<Integer>> solution, double[] cost, int[] b)
          throws IloException {
        //MP
        this.c=cost.clone();
        this.b=b.clone();
        for(Integer key:solution.keySet()){
            for(int i=0;i<H;i++){
                if(solution.get(key).contains(i+1)){
                    a[i][key-1]=1;
                }else{
                    a[i][key-1]=0;
                }
            }
            a[H+key-1][key-1]=1;
            paths.put(key,solution.get(key));
        }
        //SP
        alpha=new IloNumVar[H+1];//index from 1 to H
        beta=new IloNumVar[T+1];
        x=new IloNumVar[H+1][H+1];//index from 0 to H
        st=new IloNumVar[H+1];
        y=new IloNumVar[H+1][T+1];
        int i,j;
        for(i=1;i<=H;i++){
            alpha[i]=MP.numVar(0,1,IloNumVarType.Int,"alpha_"+i);
            st[i]=MP.numVar(0,Double.MAX_VALUE,IloNumVarType.Float,"st("+i+")");
        }
        for(i=1;i<=T;i++){
            beta[i]=MP.numVar(0,1,IloNumVarType.Int,"beta_"+i);
        }
        for(i=0;i<=H;i++){
            for(j=0;j<=H;j++){
               if(i!=j){
                   x[i][j]=MP.numVar(0,1,IloNumVarType.Int,"x("+i+","+j+")");
               }
            }
        }
        for(i=1;i<=H;i++){
            for(j=1;j<=T;j++){
                y[i][j]= MP.numVar(0,1,IloNumVarType.Int,"y("+i+","+j+")");
            }
        }
    }

    public void buildMPModel() throws IloException{
        MP.setOut(null);
        MPCosts = MP.addMinimize();
        Fill = new IloRange[a.length];
        for (int f = 0; f < a.length; f++ ) {
            Fill[f] = MP.addRange(b[f],Double.MAX_VALUE);
        }
        path = new IloNumVarArray();
        for(int i=0;i<z;i++){
            IloColumn column = MP.column(MPCosts, c[i]);
            for (int p = 0; p < a.length; p++)
                column = column.and(MP.column(Fill[p], a[p][i]));
            path.add(MP.numVar(column, 0., Double.MAX_VALUE, IloNumVarType.Float));
        }
        //MP.exportModel("MP.lp");
        solveMPModel();
    }

    private void solveMPModel() throws IloException {
        boolean isSolution=MP.solve();
        if(!isSolution){
            System.out.println("no solve");
            return;
        }
        report1(MP,path,Fill);
        //buildSPModel();
        //solveSPModel();
        //updateMPModel();
        DPSolve();
    }

    //根据子问题添加列和相应的系数
    private void updateMPModel() throws IloException{
        List<Integer> list=new ArrayList<>();
        int vechile=0;
        int i;
        int j;
        for(i=1;i<=T;i++){
            if(SP.getValue(beta[i])>0.99){
                vechile=i;
                break;
            }
        }
        //find path
        int first=0;
        for(i=1;i<=H;i++){
            if(SP.getValue(x[0][i])>0.99){
                first=i;
            }
        }
        list.add(first);
        int next=first;
        while(SP.getValue(x[first][0])<0.99 || SP.getValue(x[next][0])<0.99){
            for(i=1;i<=H;i++){
                if(i==first) continue;
                if(SP.getValue(x[first][i])>0.99){
                    next=i;
                    list.add(next);
                    break;
                }
            }
            first=next;
        }
        //update MP and solve vechile tasks
        double c=getCostOfNewPath();
        System.out.println("c: "+c);
        int size=paths.keySet().size();
        paths.put(size+1,list);
        IloColumn column = MP.column(MPCosts, c);
        for ( int p = 0; p <H; p++ ){
            if(list.contains(p+1)){
               column = column.and(MP.column(Fill[p], 1));
            }else{
                column = column.and(MP.column(Fill[p], 0));
            }
        }
        for(int p=H;p<H+T;p++){
            if(p-H+1==vechile) {
                column = column.and(MP.column(Fill[p], 1));
            }else{
                column = column.and(MP.column(Fill[p], 0));
            }
        }
        path.add(MP.numVar(column, 0.,Double.MAX_VALUE,IloNumVarType.Float));
        //MP.exportModel("MP1.lp");
        MP.solve();
        report1(MP,path,Fill);
        System.out.println("update solve");
    }

    private double getCostOfNewPath() throws IloException{
        double res=0.0;
        int i,j;
        for(i=0;i<=H;i++){
            for(j=0;j<=H;j++){
                if(i!=j){
                    if(SP.getValue(x[i][j])>0.99){
                        res+=data.w[i][j]*SP.getValue(x[i][j]);
                    }
                }
            }
        }
        return res;
    }
    //建立子模型
    private void buildSPModel() throws IloException{
        SP.setOut(null);
        SPCosts=SP.linearNumExpr();
        //构建目标函数
        int i,j;
        for(i=0;i<=H;i++){
            for(j=0;j<=H;j++){
                if(i!=j){
                    SPCosts.addTerm(data.w[i][j],x[i][j]);
                }
            }
        }
        for(i=1;i<=H;i++){
            SPCosts.addTerm(-MP.getDual(Fill[i-1]),alpha[i]);
        }
        for(j=1;j<=T;j++){
            SPCosts.addTerm(-MP.getDual(Fill[H+j-1]),beta[j]);
        }
        SP.addMinimize(SPCosts);
        //约束条件
        //(1)
        for(i=1;i<=H;i++){
            IloLinearNumExpr numExpr=SP.linearNumExpr();
            for(j=0;j<=H;j++){
                if(j!=i){
                    numExpr.addTerm(1,x[i][j]);
                }
            }
            SP.addEq(numExpr,alpha[i]);
        }
        //(2)
        for(i=1;i<=H;i++){
            IloLinearNumExpr numExpr=SP.linearNumExpr();
            for(j=0;j<=H;j++){
                if(j!=i){
                    numExpr.addTerm(1,x[j][i]);
                }
            }
            SP.addEq(numExpr,alpha[i]);
        }
        //(3)
        IloLinearNumExpr numExpr1=SP.linearNumExpr();
        for(j=1;j<=H;j++){
            numExpr1.addTerm(1,x[0][j]);
        }
        SP.addEq(numExpr1,1);
        //(4)
        IloLinearNumExpr numExpr2=SP.linearNumExpr();
        for(j=1;j<=H;j++){
            numExpr2.addTerm(1,x[j][0]);
        }
        SP.addEq(numExpr2,1);
        //(5)
        for(j=1;j<=H;j++){
            IloLinearNumExpr numExpr3=SP.linearNumExpr();
            for(i=0;i<=H;i++){
                if(i!=j) {
                    numExpr3.addTerm(1, x[i][j]);
                }
            }
            IloLinearNumExpr numExpr4=SP.linearNumExpr();
            for(i=0;i<=H;i++){
                if(i!=j) {
                    numExpr4.addTerm(1, x[j][i]);
                }
            }
            SP.addEq(numExpr3,numExpr4);
        }
        //(6)
        IloLinearNumExpr numExpr5=SP.linearNumExpr();
        for(i=1;i<=T;i++){
            numExpr5.addTerm(1,beta[i]);
        }
        SP.addEq(numExpr5,1);
        //(7)
        for(i=1;i<=T;i++){
            IloLinearNumExpr numExpr6=SP.linearNumExpr();
            for(j=1;j<=H;j++){
                numExpr6.addTerm(1,y[j][i]);
            }
            SP.addLe(numExpr6,SP.prod(Q,beta[i]));
        }
        //(8)
        for(i=1;i<=H;i++){
            IloLinearNumExpr numExpr7=SP.linearNumExpr();
            for(j=1;j<=T;j++){
                numExpr7.addTerm(1,y[i][j]);
            }
            SP.addEq(numExpr7,alpha[i]);
        }
        //(9)
        for(i=1;i<=H;i++){
            IloLinearNumExpr numExpr8=SP.linearNumExpr();
            for(j=1;j<=T;j++){
                numExpr8.addTerm(data.truckCaptitys[j],y[i][j]);
            }
            numExpr8.addTerm(-Q,alpha[i]);
            SP.addGe(numExpr8,data.taskWeights[i]-Q);
        }
        //(10)

        for(i=1;i<=H;i++){
            IloLinearNumExpr numExpr9=SP.linearNumExpr();
            numExpr9.addTerm(1,st[i]);
            numExpr9.addTerm(Q,alpha[i]);
            SP.addLe(numExpr9,data.lateTime[i]+Q);
            IloLinearNumExpr numExpr10=SP.linearNumExpr();
            numExpr10.addTerm(1,st[i]);
            numExpr10.addTerm(Q,alpha[i]);
            SP.addGe(numExpr10,data.earlyTime[i]-Q);
        }
        //(11)
        for(i=1;i<=H;i++){
            for(j=1;j<=H;j++){
                if(j!=i){
                    IloLinearNumExpr numExpr11=SP.linearNumExpr();
                    numExpr11.addTerm(1,st[i]);
                    numExpr11.addTerm(data.w[i][j],x[i][j]);
                    numExpr11.addTerm(-1,st[j]);
                    numExpr11.addTerm(Q,x[i][j]);
                    SP.addLe(numExpr11,Q-data.carryTaskTime[i]);
                }
            }
        }
        //SP.exportModel("SP.lp");
    }

    private void DPSolve() throws IloException{
        double[] price1=new double[H];
        double[] price2=new double[T];
        for(int i=0;i<Fill.length;i++){
            if(i<H){
                price1[i]=MP.getDual(Fill[i]);
            }else{
                price2[i-H]=MP.getDual(Fill[i]);
            }
        }
        DP dp=new DP(100,data,price1,price2);
        dp.findRoutes();
    }

    private void solveSPModel() throws IloException{
        SP.solve();
        System.out.println("Solve");
        report2(SP,y,x);
    }

    public void report1(IloCplex cutSolver, IloNumVarArray Cut, IloRange[] Fill)
            throws IloException {
        System.out.println();
        System.out.println("目标函数：" + cutSolver.getObjValue());
        System.out.println();
        for (int j = 0; j < Cut.getSize(); j++) {
            System.out.println("Route_" + (j+1) + " = " +
                    cutSolver.getValue(Cut.getElement(j)));
        }
        System.out.println();
        for (int i = 0; i < Fill.length; i++)
            System.out.println("Dual_" + (i+1) + " = " + cutSolver.getDual(Fill[i]));
        System.out.println();
    }

    private void report2(IloCplex model,IloNumVar[][] y,IloNumVar[][] x)
            throws IloException{
        System.out.println("目标函数："+model.getObjValue());
        int i,t;
        for(t=1;t<=T;t++){
            for(i=1;i<=H;i++){
                System.out.print(model.getValue(y[i][t])+",");
            }
            System.out.println();
        }
    }

    public IloCplex getMP(){
        return this.MP;
    }

}
