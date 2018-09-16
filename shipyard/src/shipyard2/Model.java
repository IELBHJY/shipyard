package shipyard2;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Model {
    final static double Q=1e8;
    Data data;
    IloCplex model;
    public static HashMap<Integer,List<Integer>> paths;
    public static HashMap<Integer,Double> serverTimes;
    public static HashMap<Integer,List<Double>> times;
    public IloNumVar[][] y;// y[i][j] 表示任务i被车j执行
    public IloNumVar[][][] z;//x[i][j][k]表示车辆k执行完任务i 接着去执行
    public IloNumVar[] s;//任务的开始执行时间
    public IloNumVar[][] z_o;//z_o[i][l]:  if task i is the first task in car l  then z_o[i][l]=1
    public IloNumVar[][] z_i;//z_i[i][l]  if task i is the last task in car l then z_i[i][l]=1

    public Model(Data data) {
        this.data=data;
    }

    public void build_model() throws IloException {
        model=new IloCplex();
        model.setOut(null);
        model.setParam(IloCplex.DoubleParam.TimeLimit,3600);
        y=new IloNumVar[data.n][data.t]; //y[i][l]
        z=new IloNumVar[data.n][data.n][data.t];//z[i][j][k]
        z_o=new IloNumVar[data.n][data.t];
        z_i=new IloNumVar[data.n][data.t];
        s=new IloNumVar[data.n];//s[i] :start time of task i

        for(int i=1;i<data.n;i++){
            for(int j=1;j<data.t;j++) {
                y[i][j] = model.numVar(0, 1, IloNumVarType.Int, "y"+i+","+j);
                z_o[i][j]=model.numVar(0,1,IloNumVarType.Int);
                z_i[i][j]=model.numVar(0,1,IloNumVarType.Int);
            }
            for(int j=1;j<data.n;j++){
                if(j!=i) {
                    for (int k = 1; k < data.t; k++) {
                        z[i][j][k] = model.numVar(0, 1, IloNumVarType.Int, "z" + i + "," + j + "," + k);
                    }
                }
            }
            s[i] = model.numVar(0, 1e8, IloNumVarType.Float, "s" + i);
        }
        //目标函数：任务之间的空载行驶时间和车场到每个车上第一个任务的行驶时间和车上最后一个任务回到车场的行驶时间以及由于
        // 协同运输所产生的等待时间。
        //目标函数的实际意义是减少完成任务过程中的浪费。前提是任务之间如何行驶的路径已经确定。即最短路径已经求出。
        //objection1：任务间空驶时间
        IloNumExpr obj=model.numExpr();
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                for(int h=1;h<data.n;h++){
                    if(h!=i){
                        obj= model.sum(obj,model.prod(z[i][h][k],data.w[i][h]));
                    }
                }
            }
        }
        // objection2：车场到第一个任务以及完成最后一个任务返回车场的行驶时间。
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                obj=model.sum(obj,model.prod(z_o[i][k],data.w[0][i]));
            }
        }
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                obj=model.sum(obj,model.prod(z_i[i][k],data.w[i][0]));
            }
        }
        // objection3: 车在执行任务时的等待时间
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                for(int h=1;h<data.n;h++){
                    if(h!=i){
                        IloNumExpr numExpr;
                        numExpr=model.sum(s[h],-data.carryTaskTime[i]);
                        numExpr=model.sum(numExpr,-data.w[i][h]);
                        numExpr=model.sum(numExpr,model.prod(-1,s[i]));
                        obj=model.sum(obj,model.prod(z[i][h][k],numExpr));
                    }
                }
            }
        }

        model.addMinimize(obj);
        //约束（1）y[i][l]=1+s[i]
        for(int i=1;i<data.n;i++){
            IloNumExpr constrain1=model.numExpr();
            for(int k=1;k<data.t;k++){
                constrain1=model.sum(constrain1,y[i][k]);
            }
            model.addEq(constrain1,1+data.s[i]);
        }
        //(2)
        for(int k=1;k<data.t;k++){
            IloNumExpr numExpr=model.numExpr();
            IloNumExpr numExpr1=model.numExpr();
            for(int i=1;i<data.n;i++){
                numExpr=model.sum(numExpr,z_o[i][k]);
                numExpr1=model.sum(numExpr1,z_i[i][k]);
            }
            model.addEq(numExpr,1);
            model.addEq(numExpr1,1);
        }
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                IloNumExpr numExpr=model.numExpr();
                for(int j=1;j<data.n;j++){
                    if(j!=i) {
                        numExpr = model.sum(numExpr, z[i][j][k]);
                    }
                }
                model.addLe(numExpr,y[i][k]);
            }
        }
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                IloNumExpr numExpr=model.numExpr();
                IloNumExpr numExpr1=model.numExpr();
                for(int j=1;j<data.n;j++){
                    if(j!=i) {
                        numExpr = model.sum(numExpr, z[j][i][k]);
                        numExpr1=model.sum(numExpr1,z[i][j][k]);
                    }
                }
                numExpr=model.sum(numExpr,z_o[i][k]);
                numExpr1=model.sum(numExpr1,z_i[i][k]);
                model.addEq(numExpr,y[i][k]);
                model.addEq(numExpr1,y[i][k]);
            }
        }

        for(int i=1;i<data.n;i++){
            IloNumExpr numExpr=model.numExpr();
            for(int k=1;k<data.t;k++){
                numExpr=model.sum(numExpr,model.prod(y[i][k],data.truckCaptitys[k]));
            }
            model.addLe(data.taskWeights[i],numExpr);
        }
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                for(int h=1;h<data.n;h++){
                    if(h!=i){
                        IloNumExpr numExpr=model.numExpr();
                        IloNumExpr numExpr1=model.numExpr();
                        IloNumExpr numExpr2=model.numExpr();
                        numExpr=model.sum(s[i],data.carryTaskTime[i]);
                        numExpr1=model.prod(z[i][h][k],data.w[i][h]);
                        numExpr2=model.prod(-1,s[h]);
                        IloNumExpr numExpr3=model.numExpr();
                        numExpr3=model.sum(numExpr,numExpr1,numExpr2);
                        IloNumExpr numExpr4=model.numExpr();
                        numExpr4=model.sum(1,model.prod(-1,z[i][h][k]));
                        numExpr4=model.prod(numExpr4,Q);
                        model.addLe(numExpr3,numExpr4);
                    }
                }
            }
        }
        for(int k=1;k<data.t;k++){
            for(int i=1;i<data.n;i++){
                for(int h=1;h<data.n;h++){
                    if(h!=i){
                        IloNumExpr numExpr=model.numExpr();
                        IloNumExpr numExpr1=model.numExpr();
                        IloNumExpr numExpr2=model.numExpr();
                        numExpr=model.sum(s[i],data.carryTaskTime[i]);
                        numExpr1=model.prod(z[i][h][k],data.w[i][h]);
                        numExpr2=model.prod(-1,s[h]);
                        IloNumExpr numExpr3=model.numExpr();
                        numExpr3=model.sum(numExpr,numExpr1,numExpr2);
                        IloNumExpr numExpr4=model.numExpr();
                        numExpr4=model.sum(1,model.prod(-1,model.prod(data.s[h],z[i][h][k])));
                        numExpr4=model.prod(numExpr4,Q);
                        model.addLe(numExpr3,numExpr4);
                    }
                }
            }
        }
        //time windows
        for(int i=1;i<data.n;i++){
            model.addLe(s[i],data.lateTime[i]);
            model.addGe(s[i],data.earlyTime[i]);
        }
        for(int i=1;i<data.n;i++){
            model.addLe(model.sum(s[i],data.carryTaskTime[i]), shipyard.Data.L);
            model.addGe(s[i],Data.E);
        }
        for(int i=1;i<data.n;i++){
            IloNumExpr numExpr=model.numExpr();
            for(int k=1;k<data.t;k++){
                numExpr=model.sum(numExpr,z_o[i][k]);
            }
            model.addGe(s[i],model.prod(data.w[0][i],numExpr));
        }
    }

    public void Solve() throws IloException {
        if (model.solve() == true) {
            System.out.println("solve "+model.getObjValue());
            paths = new HashMap<>();
            serverTimes=new HashMap<>();
            times=new HashMap<>();
            for(int i=1;i<data.n;i++){
                serverTimes.put(i,model.getValue(s[i]));
            }
            for (int k = 1; k < data.t; k++) {
                List<Integer> list = new ArrayList<>();
                List<Double> list1=new ArrayList<>();
                for (int i = 1; i < data.n; i++) {
                    if (model.getValue(z_o[i][k]) == 1) {
                        list.add(i);
                        list1.add(serverTimes.get(i));
                    }
                }
                boolean isTermater = true;
                while (isTermater) {
                    int size = list.size();
                    if(size==0) break;
                    for (int i = 1; i < data.n; i++) {
                        int start = list.get(size - 1);
                        if (i != start) {
                            if (model.getValue(z[start][i][k]) == 1) {
                                list.add(i);
                                list1.add(serverTimes.get(i));
                            }
                        }
                    }
                    if (list.size() == size)
                        isTermater = false;
                }
                paths.put(k, list);
                times.put(k,list1);
            }
            Solution solution=new Solution(data,model.getObjValue(),paths,serverTimes,times);
            solution.feasion();
            solution.showSolution();
        } else{
            System.out.println("No solve");
        }
    }
}
