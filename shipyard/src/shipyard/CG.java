package shipyard;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/***
 * @author libaihe
 * @time 2018.09.18
 */
public class CG {
    IloCplex MP;
    IloCplex SP;
    int H;
    int T;
    int z;
    int[][] a;
    double[] c;
    int[] b;
    Data data;
    IloObjective MPCosts;
    IloLinearNumExpr SPCosts;
    IloRange[]   Fill;
    IloNumVarArray path;
    HashMap<Integer,List<Integer>> paths;
    HashMap<Integer,Integer> trucks;
    HashMap<Integer,List<Integer>> bestPaths;
    HashMap<Integer,Integer> bestTrucks;
    HashMap<Integer,Double> srcs;
    double best;
    boolean isInteger;
    List<Integer>[] newRoutes;
    int[] newVechiles;
    IloNumVar[] alpha;
    IloNumVar[] beta;
    IloNumVar[][] x;
    IloNumVar[] st;
    IloNumVar[][] y;
    int Q=10000;
    int findRoute=100;
    int[][] tabuEdges;//由于分支造成的不能选，或者必须选的边，不能选的边用-1表示，必须选的边用1表示。
    boolean hasPaths=true;
    String name="name";

    public CG(int h,int t,int z,Data data) throws IloException {
        MP=new IloCplex();
        this.H=h;
        this.T=t;
        this.z=z;
        this.a=new int[h+t+1][z+1];
        this.c=new double[z+1];
        this.b=new int[h+t+1];
        this.data=data;
        paths=new HashMap<>();
        trucks=new HashMap<>();
        bestPaths=new HashMap<>();
        bestTrucks=new HashMap<>();
        srcs=new HashMap<>();
        tabuEdges=new int[h+1][h+1];
    }

    public CG(){

    }

    /**
       初始化主问题矩阵系数,用于建模的信息都存储好
    **/
    public void initalModelInfor(HashMap<Integer,List<Integer>> paths, HashMap<Integer,Integer> trucks,
                                    int[][] a,double[] cost, int[] b) throws IloException
    {
        this.c=cost.clone();
        this.b=b.clone();
        for(int i=0;i<a.length;i++){
            this.a[i]=a[i].clone();
        }
        for(Integer key:paths.keySet()){
            List<Integer> path=new ArrayList<>();
            for(Integer p:paths.get(key)){
                path.add(p);
            }
            this.paths.put(key,path);
        }
        for(Integer key:trucks.keySet()){
            this.trucks.put(key,trucks.get(key));
        }
    }

    /**
     * 以每个任务一个车构造模型的系数，和路径，车型信息
     * */
    public void creatInitalSolution() throws IloException{
        for(int i=1;i<=H;i++){
            c[i]+=data.w[0][i]+data.w[i][0];
            List<Integer> list=new ArrayList<>();
            list.add(i);
            paths.put(i,list);
        }
        for(int i=1;i<=H+T;i++){
            b[i]=1;
        }
        for(int i=1;i<=z;i++){
            a[i][i]=1;
        }
        for(int i=1;i<=z;i++){
            int t=1;
            while(data.taskWeights[i]>data.truckCaptitys[t]){
                t++;
            }
            a[H+t][i]=1;
            trucks.put(i,t);
        }
        buildMPModel();
    }
    /**
     * 构造主问题模型
     * **/
    public void buildMPModel() throws IloException{
        MP.setOut(null);
        /*for(int i=0;i<a[0].length;i++){
            if(a[3][i]==1 && a[7][i]==1 && a[8][i]==1 && a[10][i]==1 && a[11][i]==1
                    && a[1][i]==0 && a[2][i]==0 && a[4][i]==0 && a[5][i]==0 &&
                    a[6][i]==0 && a[9][i]==0 && a[12][i]==0){
                System.out.println("first path:"+i+" "+this.c[i]);
            }
            if(a[1][i]==1 && a[2][i]==1 && a[4][i]==1 && a[5][i]==1 && a[6][i]==1 && a[9][i]==1 &&a[12][i]==1
                    && a[3][i]==0 && a[7][i]==0 && a[8][i]==0 && a[10][i]==0 && a[11][i]==0 ){
                System.out.println("second path:"+i+" "+this.c[i]);
            }
        }*/
        MPCosts = MP.addMinimize();
        Fill = new IloRange[a.length];
        for (int f = 1; f < a.length; f++ ) {
            Fill[f] = MP.addRange(b[f], Double.MAX_VALUE);
        }
        path = new IloNumVarArray();
        for(int i=1;i<c.length;i++){
            IloColumn column = MP.column(MPCosts, c[i]);
            for (int p = 1; p < a.length; p++) {
                column = column.and(MP.column(Fill[p], a[p][i]));
            }
            path.add(MP.numVar(column, 0., Double.MAX_VALUE, IloNumVarType.Float));
        }
    }

    public void test(){
        if(trucks.keySet().size()!=paths.keySet().size()){
            System.exit(0);
        }
        for(Integer num:trucks.keySet()){
            if(trucks.get(num)==1){
                List<Integer> path=paths.get(num);
                if(path.size()!=4) continue;
                if(path.get(0)==3 && path.get(1)==10 && path.get(2)==7 && path.get(3)==8){
                    System.out.println(this.c[num]);
                }
            }else if(trucks.get(num)==2){
                List<Integer> path=paths.get(num);
                if(path.size()!=6) continue;
                if(path.get(0)==2 && path.get(1)==1 && path.get(2)==4 && path.get(3)==6 && path.get(4)==5
                        && path.get(5)==9){
                    System.out.println(this.c[num]);
                }
            }
        }
    }
    /**
     * 求解主问题模型
     * */
    public void solveMPModel() throws IloException {
        boolean isSolution=MP.solve();
        if(!isSolution){
            System.out.println("no solve");
            return;
        }
        report1(MP,path,Fill);
    }

    private double getCostOfNewPath(List<Integer> list){
        double ans=0.0;
        if(list.size()==0){
            System.out.println("该路径为空");
            System.exit(0);
        }
        ans+=data.w[0][list.get(0)];
        for(int i=0;i<list.size()-1;i++){
            ans+=data.w[list.get(i)][list.get(i+1)];
        }
        ans+=data.w[list.get(list.size()-1)][0];
        return ans;
    }

    /**
     * 初始化子问题，并求解子问题
     * */
    private void DPSolve() throws IloException{
        double[] price1=new double[H+1];
        double[] price2=new double[T+1];
        for(int i=1;i<Fill.length;i++){
            if(i<=H){
                price1[i]=MP.getDual(Fill[i]);
            }else{
                price2[i-H]=MP.getDual(Fill[i]);
            }
        }
        /*还需要传入tabuEdges，用于找到符合条件的列
        * */
        DP dp=new DP(findRoute,data,price1,price2,paths,tabuEdges);
        int sum=dp.findAllRoutes();
        if(sum==0){
            System.out.println("找不到那么多小于0的列");
            solveMPModel();
            System.exit(0);
        }
        findRoute=sum;
        /*boolean res=dp.findRoutes();
        if(!res){
            System.out.println("找不到那么多小于0的列");
            solveMPModel();
            hasPaths=false;
            return;
        }*/
        newRoutes=new List[findRoute];
        newVechiles=new int[findRoute];
        newRoutes=dp.getRoute();
        newVechiles=dp.getVechile();
    }

    /**
     * 根据子问题信息，更新主问题
     * */
    private void updateModel() throws IloException{
        int length=this.c.length;
        double[] cost=new double[length+findRoute];
        for(int i=0;i<length;i++){
            cost[i]=this.c[i];
        }
        int[][] A=new int[this.a.length][length+findRoute];
        for(int i=0;i<this.a.length;i++){
            for(int j=0;j<this.a[1].length;j++){
                A[i][j]=this.a[i][j];
            }
        }
        for(int i=0;i<findRoute;i++){
            cost[i+length]=getCostOfNewPath(newRoutes[i]);
            int size=paths.keySet().size();
            paths.put(size+1,newRoutes[i]);
            trucks.put(size+1,newVechiles[i]);
            IloColumn column = MP.column(MPCosts, cost[i+length]);
            for ( int p = 1; p <= H; p++ ){
                if(newRoutes[i].contains(p)){
                    A[p][H+i+1]=1;
                    column = column.and(MP.column(Fill[p], 1));
                }else{
                    A[p][H+i+1]=0;
                    column = column.and(MP.column(Fill[p], 0));
                }
            }
            for(int p=H+1;p<=H+T;p++){
                if(p-H==newVechiles[i]) {
                    A[p][H+i+1]=1;
                    column = column.and(MP.column(Fill[p], 1));
                }else{
                    A[p][H+i+1]=0;
                    column = column.and(MP.column(Fill[p], 0));
                }
            }
            path.add(MP.numVar(column, 0.,1,IloNumVarType.Float));
        }
        this.c=new double[cost.length];
        this.c=cost.clone();
        this.a=new int[A.length][A[1].length];
        for(int i=0;i<a.length;i++){
            this.a[i]=A[i].clone();
        }
    }

    public void Solve() throws IloException{
        solveMPModel();
        DPSolve();
        updateModel();
        solveMPModel();
    }

    //列生产部分的求解过程和终止条件
    public void Solve1() throws IloException{
        while (true) {
            solveMPModel();
            DPSolve();
            if(!hasPaths){
                break;
            }
            updateModel();
            solveMPModel();
        }
    }


    public void report1(IloCplex cutSolver, IloNumVarArray Cut, IloRange[] Fill)
            throws IloException {
        System.out.println("------Solve details-------");
        System.out.println(getName());
        System.out.println("目标函数：" + cutSolver.getObjValue());
        best=cutSolver.getObjValue();
        System.out.println("路径个数: "+Cut.getSize());
        bestPaths.clear();
        bestTrucks.clear();
        setInteger(true);
        for (int j = 0; j < Cut.getSize(); j++) {
            if(cutSolver.getValue(Cut.getElement(j))>0){
                List<Integer> list=paths.get(j+1);
                if(list.size()==0){
                    System.exit(0);
                }else{
                    System.out.print(trucks.get(j+1)+": ");
                    for(Integer p:list){
                        System.out.print(p+" ");
                    }
                }
                bestPaths.put(j+1,list);
                bestTrucks.put(j+1,trucks.get(j+1));
                srcs.put(j+1,cutSolver.getValue(Cut.getElement(j)));
                System.out.println();
            }
        }
        for(int i=0;i<Cut.getSize();i++){
            if(cutSolver.getValue(Cut.getElement(i))>0) {
                double var1=cutSolver.getValue(Cut.getElement(i));
                int var2=(int) var1;
                if(var2!=var1){
                    setInteger(false);
                    System.out.println("不是可行解");
                }
                System.out.println("Route_" + (i + 1) + "=" + cutSolver.getValue(Cut.getElement(i)));
            }
        }
        System.out.println();
        /*for (int i = 1; i < Fill.length; i++) {
            System.out.println("Dual_" + (i) + " = " + cutSolver.getDual(Fill[i]));
        }*/
        System.out.println("------Solve details-------");
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

    private double getCostOfNewPath() throws IloException{
        double res=0.0;
        int i,j;
        for(i=0;i<=H;i++){
            for(j=0;j<=H;j++){
                if(i!=j){
                    if(SP.getValue(x[i][j])>0.9){
                        res+=data.w[i][j]*SP.getValue(x[i][j]);
                    }
                }
            }
        }
        return res;
    }

    public HashMap<Integer,List<Integer>> getPaths(){
        return paths;
    }

    public void setPaths(HashMap<Integer,List<Integer>> paths){
        this.paths=paths;
    }

    public HashMap<Integer,Integer> getTrucks(){
        return trucks;
    }

    public HashMap<Integer,Double> getSrcs(){
        return srcs;
    }

    public double getBest() {
        return best;
    }

    public void setBest(double value){
        this.best=value;
    }

    public HashMap<Integer,List<Integer>> getBestPaths(){
        return this.bestPaths;
    }

    public int getPathNum(){
        return paths.keySet().size();
    }

    public boolean getInteger(){
        return this.isInteger;
    }

    public void setInteger(boolean value){
        this.isInteger=value;
    }

    public void setC(int index,double value){
        this.c[index]=value;
    }

    public double[] getC(){
        return this.c;
    }

    public int[][] getA(){
        return this.a;
    }

    public int[] getB(){
        return this.b;
    }

    public void setTabuEdges(int edge1,int edge2,int value){
        this.tabuEdges[edge1][edge2]=value;
    }
    public void setTabuEdges(int[][] edges){
        for(int i=0;i<edges.length;i++){
            this.tabuEdges[i]=edges[i].clone();
        }
    }

    public int[][] getTabuEdges(){
        return this.tabuEdges;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String var){
        this.name=var;
    }

    public IloCplex getMP(){
        return this.MP;
    }

    private void setMP(IloCplex mp){
        this.MP=mp;
    }

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
        MP.solve();
        report1(MP,path,Fill);
        System.out.println("update solve");
    }

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

    private void solveSPModel() throws IloException{
        SP.solve();
        System.out.println("Solve");
        report2(SP,y,x);
    }

}
